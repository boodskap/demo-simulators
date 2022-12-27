package io.boodskap.iot.simulator;

import java.util.Map;
import java.util.Set;

public interface IGateway {

	public void init() throws Exception;
	
	public String getDeviceToken();
	
	public Set<? extends ISimulator> getSimulators();
	
	public void send(String deviceToken, long specId, Map<String, Object> message) throws Exception;
	
	public void sendMqtt(String deviceToken, long specId, Map<String, Object> message) throws Exception;

	public void sendHttp(String deviceToken, long specId, Map<String, Object> message) throws Exception;

	public void send(String deviceToken, String ruleType, DataType dataType, byte[] data, String contentType, String properties) throws Exception;
	
	public Set<String> getIncomingTopics();
	
}
