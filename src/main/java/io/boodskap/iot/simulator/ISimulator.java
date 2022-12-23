package io.boodskap.iot.simulator;

import java.util.Map;

public interface ISimulator {
	
	public IGateway getGateway();
	
	public String getSensorType();
	
	public String getDeviceToken();
	
	public boolean canSimulate();

	public Map<String, Object> simulate();
}
