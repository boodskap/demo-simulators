package io.boodskap.iot.simulator.parking;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.RandomUtils;

import io.boodskap.iot.simulator.Config;
import io.boodskap.iot.simulator.ISimulator;

public class ParkingSimulator implements ISimulator {
	
	private long nextSimulationTime;
	private boolean occupied;
	
	private final ParkingGateway gateway;
	private final int row;
	private final int column;
	
	public ParkingSimulator(ParkingGateway gateway, int row, int column) {
		this.gateway = gateway;
		this.row = row;
		this.column = column;
		calculateNextSimulationTime();
	}

	@Override
	public String getDeviceToken() {
		return Config.get().getParkingDevToken();
	}

	@Override
	public String getSensorType() {
		return "PARKING";
	}

	@Override
	public ParkingGateway getGateway() {
		return gateway;
	}

	@Override
	public boolean canSimulate() {
		return (nextSimulationTime - System.currentTimeMillis()) <= 0;
	}
	
	protected void calculateNextSimulationTime() {
		nextSimulationTime = System.currentTimeMillis() + ( RandomUtils.nextInt(0, 180) * 1000 );		
	}

	@Override
	public Map<String, Object> simulate() {
		
		try {
			
			Map<String, Object> data = new HashMap<>();
			
			data.put("garage", gateway.getGarageId());
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
