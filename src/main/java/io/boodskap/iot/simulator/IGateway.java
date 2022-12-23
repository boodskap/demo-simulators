package io.boodskap.iot.simulator;

import java.util.Map;
import java.util.Set;

public interface IGateway {

	public void init() throws Exception;
	
	public String getDeviceToken();
	
	public Set<ISimulator> getSimulators();
	
	public void send(String deviceToken, int specId, Map<String, Object> message) throws Exception;
	
	public void sendMqtt(String deviceToken, int specId, Map<String, Object> message) throws Exception;

	public void sendHttp(String deviceToken, int specId, Map<String, Object> message) throws Exception;
}
