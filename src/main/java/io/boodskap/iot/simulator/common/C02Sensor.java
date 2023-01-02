package io.boodskap.iot.simulator.common;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.RandomUtils;

import io.boodskap.iot.simulator.AbstractSimulator;
import io.boodskap.iot.simulator.Config;
import io.boodskap.iot.simulator.IGateway;

public class C02Sensor extends AbstractSimulator<IGateway> {
	
	private final Odometer odometer;
	private final String deviceId;
	private final String deviceToken;
	private final String uniqueId;
	
	private int messageFrequency = 60000; //seconds
	private long nextSimulationTime = RandomUtils.nextLong(5000, 25000);
	private long lastMaintenance;

	public C02Sensor(Odometer odometer, String deviceId, String deviceToken) {
		
		super(odometer.getGateway());
		
		this.odometer = odometer;
		this.deviceId = deviceId;
		this.deviceToken = deviceToken;
		this.uniqueId = String.format("%s.%s", getGateway().getId(), getDeviceId());
		
		try {
			this.lastMaintenance = Config.get().getOrCreateStamp(uniqueId);
		}catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public String getSensorType() {
		return "C02";
	}

	@Override
	public String getDeviceToken() {
		return deviceToken;
	}

	@Override
	public String getDeviceId() {
		return deviceId;
	}

	@Override
	public boolean canSimulate() {
		
		if(!odometer.isRunning()) return false;
		
		return (nextSimulationTime - System.currentTimeMillis()) <= 0;
	}
	
	protected void calculateNextSimulationTime() {
		nextSimulationTime = System.currentTimeMillis() + messageFrequency;		
	}
	
	public void setMaintenanceDone() throws Exception {
		
		lastMaintenance = Config.get().putStamp(uniqueId);
		
		System.out.format("\n** Maintenance Reset At %s **\n", new Date(lastMaintenance));
	}
	
	@Override
	public Map<String, Object>  simulate() throws Exception {
		
		try {
			
			Map<String, Object> data = new HashMap<>();
			
			final double elapsed = (((System.currentTimeMillis() - lastMaintenance)/1000D)/60D)/60D;
			final Double reading = 98.06D + (elapsed * 0.001D);
			
			data.put("c02", (reading.longValue()));
			
			return data;
			
		}finally {
			calculateNextSimulationTime();
		}
	}

}
