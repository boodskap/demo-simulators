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

public class WaterMeter extends AbstractSimulator<IGateway> {
	
	static {
		boolean success = false;
		try {
			
			Connection c = Config.getConnection();
			
			c.createStatement().execute("CREATE TABLE IF NOT EXISTS WATERMETERS(ID VARCHAR(255) NOT NULL, READING DECIMAL NOT NULL, PRIMARY KEY(ID) );");
			
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
	
	public WaterMeter(IGateway gateway, String deviceId, String deviceToken) {
		
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
		return "WATERMETER";
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
			final double gallons;
			
			if(hour >= 0 && hour < 4){
				gallons = 10D;
			}else if(hour >= 4 && hour < 6){
				gallons = 20D;
			}else if(hour >= 6 && hour < 8){
				gallons = 30D;
			}else if(hour >= 8 && hour < 9){
				gallons = 12D;
			}else if(hour >= 9 && hour < 11){
				gallons = 5D;
			}else if(hour >= 11 && hour < 13){
				gallons = 2D;
			}else if(hour >= 13 && hour < 16){
				gallons = 8D;
			}else if(hour >= 16 && hour < 19){
				gallons = 20D;
			}else if(hour >= 19 && hour < 22){
				gallons = 5D;
			}else if(hour >= 22 && hour < 23){
				gallons = 3D;
			}else{
				gallons = 2D;
			}
			
			final double min = gallons / 60D;
			final double max = gallons / 50D;
			
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
			
			PreparedStatement ps = c.prepareStatement("SELECT READING FROM WATERMETERS WHERE ID=?");
			
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
			
			PreparedStatement dps = c.prepareStatement("DELETE FROM WATERMETERS WHERE ID=?");
			dps.setString(1, uniqueId);
			dps.executeUpdate();
			
			PreparedStatement ps = c.prepareStatement("INSERT INTO WATERMETERS(ID,READING) VALUES(?,?)");
			
			ps.setString(1, uniqueId);
			ps.setDouble(2, reading);
			
			ps.executeUpdate();
			
			//System.err.format("ODOMETER:%f\n", odometer);
			
		}finally {
			Config.closeConnection();
		}
		
	}
}
