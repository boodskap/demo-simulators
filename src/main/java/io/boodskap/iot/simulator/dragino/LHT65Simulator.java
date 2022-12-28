package io.boodskap.iot.simulator.dragino;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.RandomUtils;

import io.boodskap.iot.simulator.AbstractSimulator;
import io.boodskap.iot.simulator.BitInputStream;
import io.boodskap.iot.simulator.BitOutputStream;
import io.boodskap.iot.simulator.Config;
import io.boodskap.iot.simulator.DataType;

public class LHT65Simulator extends AbstractSimulator<DraginoGateway> {

	private static final Map<Integer, List<Float>> HUMIDITY_CHART = new HashMap<>();
	
	static {
		/** https://www.tis-gdv.de/tis_e/misc/klima-htm **/
		HUMIDITY_CHART.put(-25, Arrays.asList(0.1F, 0.1F, 0.2F, 0.2F, 0.3F, 0.3F, 0.4F, 0.4F, 0.5F, 0.6F));
		HUMIDITY_CHART.put(-20, Arrays.asList(0.1F, 0.2F, 0.3F, 0.4F, 0.4F, 0.5F, 0.6F, 0.7F, 0.8F, 0.9F));
		HUMIDITY_CHART.put(-15, Arrays.asList(0.2F, 0.3F, 0.5F, 0.6F, 0.8F, 1.0F, 1.1F, 1.3F, 1.5F, 1.6F));
		HUMIDITY_CHART.put(-10, Arrays.asList(0.2F, 0.3F, 0.7F, 0.9F, 1.2F, 1.4F, 1.6F, 1.9F, 2.1F, 2.3F));
		HUMIDITY_CHART.put(-5, Arrays.asList(0.3F, 0.7F, 1.0F, 1.4F, 1.7F, 2.1F, 2.4F, 2.7F, 3.1F, 3.4F));
		HUMIDITY_CHART.put(0, Arrays.asList(0.5F, 1.0F, 1.5F, 1.9F, 2.4F, 2.9F, 3.4F, 3.9F, 4.4F, 4.8F));
		HUMIDITY_CHART.put(5, Arrays.asList(0.7F, 1.4F, 2.0F, 2.7F, 3.4F, 4.1F,	4.8F, 5.4F,	6.1F, 6.8F));
		HUMIDITY_CHART.put(10, Arrays.asList(0.9F, 1.9F, 2.8F, 3.8F, 4.7F, 5.6F, 6.6F, 7.5F, 8.5F, 9.4F));
		HUMIDITY_CHART.put(15, Arrays.asList(1.3F, 2.6F, 3.9F, 5.1F, 6.4F, 7.7F, 9.0F, 10.3F, 11.5F, 12.8F));
		HUMIDITY_CHART.put(20, Arrays.asList(1.7F, 3.5F, 5.2F, 6.9F, 8.7F, 10.4F, 12.1F, 13.8F, 15.6F, 17.3F));
		HUMIDITY_CHART.put(25, Arrays.asList(2.3F, 4.6F, 6.9F, 9.2F, 11.5F, 13.8F, 16.1F, 18.4F, 20.7F, 23.0F));
		HUMIDITY_CHART.put(30, Arrays.asList(3.0F, 6.1F, 9.1F, 12.1F, 15.2F, 18.2F, 21.3F, 24.3F, 27.3F, 30.4F));
		HUMIDITY_CHART.put(35, Arrays.asList(4.0F, 7.9F, 11.9F, 15.8F, 19.8F, 23.8F, 27.7F, 31.7F, 35.6F, 39.6F));
		HUMIDITY_CHART.put(40, Arrays.asList(5.1F, 10.2F, 15.3F, 20.5F, 25.6F, 30.7F, 35.8F, 40.9F, 46.0F, 51.1F));
		HUMIDITY_CHART.put(45, Arrays.asList(6.5F, 13.1F, 19.6F, 26.2F, 32.7F, 39.3F, 45.8F, 52.4F, 58.9F, 65.4F));
		HUMIDITY_CHART.put(50, Arrays.asList(8.3F, 16.6F, 24.9F, 33.2F, 41.5F, 49.8F, 58.1F, 66.4F, 74.7F, 83.0F));
	}
	
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
	
	private static final double FULL_BATTERY = 3200;
	
	private final long installationDate;
	private final float minTemp, maxTemp;
	private final int relativeHum;
	private int messageFrequency = 60000; //seconds
	private final String sensorId;
	
	private long nextSimulationTime = RandomUtils.nextLong(5000, 25000);

	public LHT65Simulator(DraginoGateway gateway, String sensorId, String doi, float minTemp, float maxTemp, int relativeHum) {
		
		super(gateway);

		try {
			this.installationDate = sdf.parse(doi).getTime();
			this.sensorId = sensorId;
			this.minTemp = minTemp;
			this.maxTemp = maxTemp;
			this.relativeHum = relativeHum;
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public String getContentType() {
		return "application\\octet-stream";
	}

	@Override
	public DataType getBinaryDataType() {
		return DataType.HEX;
	}

	@Override
	public String getDeviceId() {
		return sensorId;
	}

	@Override
	public String getProperties() {
		return getDeviceId();
	}

	@Override
	public String getSensorType() {
		return "LHT65";
	}

	@Override
	public String getRuleType() {
		return "LHT65Parser";
	}

	@Override
	public String getDeviceToken() {
		return Config.get().getLht65DevToken();
	}

	@Override
	public boolean canSimulate() {
		return (nextSimulationTime - System.currentTimeMillis()) <= 0;
	}
	
	protected void calculateNextSimulationTime() {
		nextSimulationTime = System.currentTimeMillis() + messageFrequency;		
	}


	@Override
	public byte[] simulate() {
		
		try {
			
			
			double minutes = (int) Duration.between(new Date(installationDate).toInstant(), new Date().toInstant()).toMinutes();
			
			double loss =  FULL_BATTERY - (minutes * (0.000265508225D));
			
			int voltage = (int)loss;
			int status;
			
			if(voltage <= 2499) {
				status = 0;
			}else if(voltage <= 2550) {
				status = 1;
			}else if(voltage <= 2600) {
				status = 2;
			}else{
				status = 3;
			}
			
			float temperature = RandomUtils.nextFloat(minTemp, maxTemp);
			int hi;

			if(temperature <= -21F) {
				hi = -25;
			}else if(temperature >= -16F && temperature <= -20F) {
				hi = -20;
			}else if(temperature >= -11F && temperature <= -15F) {
				hi = -15;
			}else if(temperature >= -6F && temperature <= -10F) {
				hi = -10;
			}else if(temperature >= -1F && temperature <= -5F) {
				hi = -5;
			}else if(temperature >= 0F && temperature <= 3F) {
				hi = 0;
			}else if(temperature >= 4F && temperature <= 8F) {
				hi = 5;
			}else if(temperature >= 9F && temperature <= 13F) {
				hi = 10;
			}else if(temperature >= 14F && temperature <= 18F) {
				hi = 15;
			}else if(temperature >= 19F && temperature <= 23F) {
				hi = 20;
			}else if(temperature >= 24F && temperature <= 28F) {
				hi = 25;
			}else if(temperature >= 29F && temperature <= 33F) {
				hi = 30;
			}else if(temperature >= 34F && temperature <= 38F) {
				hi = 35;
			}else if(temperature >= 39F && temperature <= 43F) {
				hi = 40;
			}else if(temperature >= 44F && temperature <= 48F) {
				hi = 45;
			}else {
				hi = 50;
			}
			
			temperature = temperature * 100;
			
			float humidity = (HUMIDITY_CHART.get(hi).get(relativeHum) * 10F);
			
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			BitOutputStream bitO = new BitOutputStream(bout);
			bitO.writeBits(2, status);
			bitO.writeBits(14, voltage);
			bitO.writeBits(16, (int)temperature);
			bitO.writeBits(16, (int)humidity);
			bitO.writeBits(8, 0);
			bitO.flush();
			bitO.close();
			
			return bout.toByteArray();
			
		}finally {
			calculateNextSimulationTime();
		}
	}

	public int getMessageFrequency() {
		return messageFrequency;
	}

	public void setMessageFrequency(int messageFrequency) {
		this.messageFrequency = messageFrequency;
	}
	
	public static void main(String[] args) throws Exception{
		
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		BitOutputStream bitO = new BitOutputStream(bout);
		bitO.writeBits(2, 3);
		bitO.writeBits(14, 3062);
		bitO.writeBits(16, -12500);
		bitO.flush();
		bitO.close();
		
		System.out.println(Hex.encodeHexString(bout.toByteArray()));
		
		BitInputStream bitI = new BitInputStream(new ByteArrayInputStream(bout.toByteArray()));
		
		int status = bitI.readBits(2);
		float voltage = (float)bitI.readBits(14)/1000F;
		int temp = bitI.readBits(16);
		float tempC;
		
		if(temp == 0) {
			tempC = 0F;
		}else if(temp > 12500) {
			tempC = (temp - 65536)/100F;
		}else {
			tempC = temp/100F;
		}
		
		System.out.format("Status:%d, Voltage=%f, .C=%f\n", status, voltage, tempC);
	}

}
