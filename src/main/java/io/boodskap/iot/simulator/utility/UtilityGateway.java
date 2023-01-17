package io.boodskap.iot.simulator.utility;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import io.boodskap.iot.simulator.AbstractGateway;
import io.boodskap.iot.simulator.Config;
import io.boodskap.iot.simulator.ISimulator;
import io.boodskap.iot.simulator.common.ElectricityMeter;
import io.boodskap.iot.simulator.common.GasMeter;
import io.boodskap.iot.simulator.common.WaterMeter;

public class UtilityGateway extends AbstractGateway {
	
	private final Set<ISimulator> simulators = new HashSet<>();
	private final Set<String> TOPICS = new HashSet<>();
	
	private final String gatewayId;
	
	public UtilityGateway(String gatewayId) {
		this.gatewayId = gatewayId;
	}

	@Override
	public void init() throws Exception {
		
		TOPICS.add(String.format("/%s/sub/downlink/%s", Config.get().getDomainKey(), getId()));
		
		for(int i=0;i<30;i++) {
			
			WaterMeter wm = new WaterMeter(this, "WM0" + (i+1), getDeviceToken());
			ElectricityMeter em = new ElectricityMeter(this, "EM0" + (i+1), getDeviceToken());
			GasMeter gm = new GasMeter(this, "GM0" + (i+1), getDeviceToken());
			
			simulators.addAll(Arrays.asList(wm, em, gm));
		}
		
		
	}

	@Override
	public String getDeviceToken() {
		return Config.get().getUtilityDevToken();
	}

	@Override
	public Set<ISimulator> getSimulators() {
		return simulators;
	}

	@Override
	public void connectionLost(Throwable cause) {
		System.err.format("MQTT disconnected. Cause:%s\n", null != cause ? cause.getMessage() : "");
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		
		try {
			
			System.out.println("*** MESSAGE ARRIVED ***");
			
			JSONObject json = new JSONObject(new String(message.getPayload()));
			
			System.out.println(json);
			
		}catch(Exception ex) {
			
		}
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
	}

	@Override
	public Set<String> getIncomingTopics() {
		return TOPICS;
	}

	@Override
	public String getId() {
		return gatewayId;
	}

}
