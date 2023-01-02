package io.boodskap.iot.simulator.parking;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.RandomUtils;

import io.boodskap.iot.simulator.AbstractSimulator;
import io.boodskap.iot.simulator.Config;

public class ParkingSimulator extends AbstractSimulator<ParkingGateway> {
	
	private long nextSimulationTime;
	private boolean occupied;
	
	private final int row;
	private final int column;
	private final String deviceId;
	private final int minTime, maxTime;
	
	public ParkingSimulator(ParkingGateway gateway, int row, int column) {
		super(gateway);
		this.row = row;
		this.column = column;
		this.deviceId = String.format("PKGS%d%d", row, column);
		this.nextSimulationTime = System.currentTimeMillis() + ( RandomUtils.nextInt(0, 180) * 1000 );
		
		if(RandomUtils.nextBoolean()) {
			minTime = RandomUtils.nextInt(600, 1800);
			maxTime = RandomUtils.nextInt(3600, 10800);
		}else {
			minTime = RandomUtils.nextInt(600, 900);
			maxTime = RandomUtils.nextInt(901, 2700);
		}
		
	}

	@Override
	public String getDeviceToken() {
		return Config.get().getParkingDevToken();
	}

	@Override
	public String getDeviceId() {
		return deviceId;
	}

	@Override
	public String getSensorType() {
		return "PARKING";
	}

	@Override
	public boolean canSimulate() {
		return (nextSimulationTime - System.currentTimeMillis()) <= 0;
	}
	
	protected void calculateNextSimulationTime() {
		nextSimulationTime = System.currentTimeMillis() + ( RandomUtils.nextInt(minTime, maxTime) * 1000 );		
	}

	@Override
	public Map<String, Object> simulate() {
		
		try {
			
			Map<String, Object> data = new HashMap<>();
			
			data.put("garage", getGateway().getId());
			
			if(!getGateway().isConfigSent()) {
				data.put("lot", String.format("%d,%d", getGateway().getRows(), getGateway().getColumns()));
				getGateway().setConfigSent(true);
				return data;
			}
			
			//System.exit(-1);
			
			data.put("lot", String.format("ROW%dCOL%d", row, column));
			
			if(occupied) {
				data.put("distance", RandomUtils.nextInt(5500, 7501));
			}else {
				data.put("distance", RandomUtils.nextInt(0, 250));
			}
			 
			return data;
			
		}finally {
			occupied = !occupied;
			calculateNextSimulationTime();
		}
	}

}
