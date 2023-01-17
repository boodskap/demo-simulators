package io.boodskap.iot.simulator.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.RandomUtils;

import io.boodskap.iot.simulator.AbstractSimulator;
import io.boodskap.iot.simulator.Config;
import io.boodskap.iot.simulator.IGateway;

public class GasMeter extends AbstractSimulator<IGateway> {
	
	static {
		boolean success = false;
		try {
			
			Connection c = Config.getConnection();
			
			c.createStatement().execute("CREATE TABLE IF NOT EXISTS GASMETERS(ID VARCHAR(255) NOT NULL, READING DECIMAL NOT NULL, PRIMARY KEY(ID) );");
			
			success = true;
			
		}catch(Exception ex) {
			ex.printStackTrace();
		}finally {
			Config.closeConnection();
			if(!success) {
				System.exit(-1);
			}
		}
	}
	
	private final String deviceId;
	private final String deviceToken;
	private final String uniqueId;

	private final int messageFrequency = 60000; //milliseconds
	private long nextSimulationTime = RandomUtils.nextLong(5000, 25000);
	private Double reading;
	
	public GasMeter(IGateway gateway, String deviceId, String deviceToken) {
		
		super(gateway);
		
		try {
			this.deviceId = deviceId;
			this.deviceToken = deviceToken;
			this.uniqueId = String.format("%s.wm.%s", getGateway().getId(), deviceId);
			this.reading = getReading();		
		}catch(Exception ex) {
			throw new RuntimeException(ex);
		}
		
	}
	
	@Override
	public String getDeviceId() {
		return deviceId;
	}

	@Override
	public String getSensorType() {
		return "GASMETER";
	}

	@Override
	public String getDeviceToken() {
		return deviceToken;
	}

	@Override
	public boolean canSimulate() {
		return (System.currentTimeMillis() - nextSimulationTime) >= messageFrequency;
	}
	
	protected void calculateNextSimulationTime() {
		nextSimulationTime = System.currentTimeMillis() + messageFrequency;		
	}
	
	@Override
	public Map<String, Object> simulate() throws Exception {
		
		try {
			
			Calendar c = Calendar.getInstance();
			final int day = c.get(Calendar.DAY_OF_WEEK);
			final int hour = c.get(Calendar.HOUR_OF_DAY);
			final double ccf;
			
			if(hour >= 0 && hour < 4){
				ccf = 100D;
			}else if(hour >= 4 && hour < 6){
				ccf = 450D;
			}else if(hour >= 6 && hour < 9){
				ccf = 1250D;
			}else if(hour >= 9 && hour < 11){
				ccf = 750D;
			}else if(hour >= 11 && hour < 13){
				ccf = 500D;
			}else if(hour >= 13 && hour < 16){
				ccf = 150D;
			}else if(hour >= 16 && hour < 19){
				ccf = 500D;
			}else if(hour >= 19 && hour < 22){
				ccf = 750D;
			}else if(hour >= 22 && hour < 23){
				ccf = 150D;
			}else{
				ccf = 75D;
			}
			
			final double min = ccf / 70D;
			final double max = ccf / 45D;
			
			reading = getReading();
			reading += RandomUtils.nextDouble(min, max);
			
			if ((day == Calendar.SATURDAY)  || day == Calendar.SUNDAY) { 
			    reading = reading * RandomUtils.nextDouble(0.5, 1.0D);;
			}
			
			putReading(reading);
			
			Map<String, Object> data = new HashMap<>();
			data.put("reading", reading);
			
			return data;
			
		}finally {
			calculateNextSimulationTime();
		}
	}

	public int getMessageFrequency() {
		return messageFrequency;
	}

	public double getReading() throws Exception{
		
		try {
			
			final Double value;
			
			Connection c = Config.getConnection();
			
			PreparedStatement ps = c.prepareStatement("SELECT READING FROM GASMETERS WHERE ID=?");
			
			ps.setString(1, uniqueId);
			
			ResultSet rs = ps.executeQuery();
			
			if(rs.next()) {
				value = rs.getDouble("READING");
			}else {
				value = RandomUtils.nextDouble(15D, 25D);
				System.err.format("New READING:%f\n", value);
				System.err.flush();
				putReading(value);
			}
			
			return value;
			
		}finally {
			Config.closeConnection();
		}
		
	}
	
	private void putReading(Double reading) throws Exception {
		
		try {
			
			Connection c = Config.getConnection();
			
			PreparedStatement dps = c.prepareStatement("DELETE FROM GASMETERS WHERE ID=?");
			dps.setString(1, uniqueId);
			dps.executeUpdate();
			
			PreparedStatement ps = c.prepareStatement("INSERT INTO GASMETERS(ID,READING) VALUES(?,?)");
			
			ps.setString(1, uniqueId);
			ps.setDouble(2, reading);
			
			ps.executeUpdate();
			
			//System.err.format("ODOMETER:%f\n", odometer);
			
		}finally {
			Config.closeConnection();
		}
		
	}
}
