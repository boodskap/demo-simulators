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

public class Odometer extends AbstractSimulator<IGateway> {
	
	static {
		boolean success = false;
		try {
			
			Connection c = Config.getConnection();
			
			c.createStatement().execute("CREATE TABLE IF NOT EXISTS ODOMETERS(ID VARCHAR(255) NOT NULL, READING DECIMAL NOT NULL, PRIMARY KEY(ID) );");
			
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
	
	public static enum DrivingMode{
		DAY,
		NIGHT,
		DAY_NIGHT
	}

	private final DrivingMode drivingMode;
	private final String deviceId;
	private final String deviceToken;
	private final String uniqueId;

	private int messageFrequency = 60000; //seconds
	private long nextSimulationTime = RandomUtils.nextLong(5000, 25000);
	private Double reading;
	private long lastSent;
	private final boolean runOnSundays;
	
	public Odometer(IGateway gateway, DrivingMode drivingMode, String deviceId, String deviceToken) {
		
		super(gateway);
		
		try {
			this.drivingMode = drivingMode;
			this.deviceId = deviceId;
			this.deviceToken = deviceToken;
			this.uniqueId = String.format("%s.%s", getGateway().getId(), getDeviceId());
			this.reading = getReading();		
			this.lastSent = Config.get().putStamp(uniqueId);
			
			boolean sunday = RandomUtils.nextBoolean();
			
			runOnSundays = Boolean.valueOf(Config.get().getValue("RUN_ON_SUNDAYS", String.valueOf(sunday)));
			
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
		return "ODOMETER";
	}

	@Override
	public String getDeviceToken() {
		return deviceToken;
	}

	@Override
	public boolean canSimulate() {
		return (nextSimulationTime - System.currentTimeMillis()) <= 0;
	}
	
	protected void calculateNextSimulationTime() {
		nextSimulationTime = System.currentTimeMillis() + messageFrequency;		
	}
	
	public void setMaintenanceDone() throws Exception {
		
		final String key = String.format("%s.%s.maintenance", getGateway().getId(), getDeviceId());
		final String value = String.valueOf(getReading());
		
		Config.get().setValue(key, value);
		
		System.out.format("\n** Maintenance Reset At %s **\n", value);
	}
	
	public boolean isRunning() {
		
		final Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		
		boolean canProceed;
		final int hour = cal.get(Calendar.HOUR_OF_DAY);
		
		switch(drivingMode) {
		case DAY:
			canProceed = (hour >= 5 && hour <= 20);
			break;
		case NIGHT:
			canProceed = (hour >= 18 && hour <= 24 || hour >= 0 && hour <= 6);
			break;
		default:
		case DAY_NIGHT:
			canProceed = true;
			break;
		
		}
		
		if(canProceed) {
			canProceed = (runOnSundays || (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY));
		}
		
		return canProceed;
	}


	@Override
	public Map<String, Object> simulate() throws Exception {
		
		try {
			
			final boolean canProceed = isRunning();
			int mph = 0;
			
			if (canProceed) {
				
				final double elapsed = (((System.currentTimeMillis() - lastSent)/1000D)/60D)/60D;
				final double speed = RandomUtils.nextDouble(45D, 90D);
				final double distance = (speed * elapsed);
				
				reading += distance;
				mph = (int) speed;
				
				putReading(reading);
			}
			
			Map<String, Object> data = new HashMap<>();
			
			data.put("odometer", (reading.longValue()));
			data.put("mph", mph);
			
			return data;
			
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
	
	public double getReading() throws Exception{
		
		try {
			
			final Double value;
			
			Connection c = Config.getConnection();
			
			PreparedStatement ps = c.prepareStatement("SELECT READING FROM ODOMETERS WHERE ID=?");
			
			ps.setString(1, uniqueId);
			
			ResultSet rs = ps.executeQuery();
			
			if(rs.next()) {
				value = rs.getDouble("READING");
			}else {
				value = RandomUtils.nextDouble(5000D, 85000D);
				System.err.format("New READING:%f\n", value);
				System.err.flush();
				putReading(value);
			}
			
			return value;
			
		}finally {
			Config.closeConnection();
		}
		
	}
	
	private void putReading(Double odometer) throws Exception {
		
		try {
			
			Connection c = Config.getConnection();
			
			PreparedStatement dps = c.prepareStatement("DELETE FROM ODOMETERS WHERE ID=?");
			dps.setString(1, uniqueId);
			dps.executeUpdate();
			
			PreparedStatement ps = c.prepareStatement("INSERT INTO ODOMETERS(ID,READING) VALUES(?,?)");
			
			ps.setString(1, uniqueId);
			ps.setDouble(2, odometer);
			
			ps.executeUpdate();
			
			lastSent = Config.get().putStamp(uniqueId);
			
			//System.err.format("ODOMETER:%f\n", odometer);
			
		}finally {
			Config.closeConnection();
		}
		
	}
}
