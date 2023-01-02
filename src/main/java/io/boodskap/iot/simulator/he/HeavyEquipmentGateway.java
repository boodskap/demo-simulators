package io.boodskap.iot.simulator.he;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import io.boodskap.iot.simulator.AbstractGateway;
import io.boodskap.iot.simulator.Config;
import io.boodskap.iot.simulator.ISimulator;
import io.boodskap.iot.simulator.common.C02Sensor;
import io.boodskap.iot.simulator.common.Odometer;

public class HeavyEquipmentGateway extends AbstractGateway {
	
	private final Set<ISimulator> simulators = new HashSet<>();
	private final Set<String> TOPICS = new HashSet<>();
	
	private final String gatewayId;
	private final Odometer.DrivingMode drivingMode;
	
	private Odometer odometer;
	private C02Sensor c02;
	
	public HeavyEquipmentGateway(String gatewayId, Odometer.DrivingMode drivingMode) {
		this.gatewayId = gatewayId;
		this.drivingMode = drivingMode;
	}

	@Override
	public void init() throws Exception {
		
		TOPICS.add(String.format("/%s/sub/downlink/%s", Config.get().getDomainKey(), getId()));
		
		odometer = new Odometer(this, drivingMode, "ODO01", getDeviceToken());
		c02 = new C02Sensor(odometer, "C0201", getDeviceToken());
		
		simulators.addAll(Arrays.asList(odometer, c02));
		
	}

	@Override
	public String getDeviceToken() {
		return Config.get().getHequipDevToken();
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
			
			if(json.optBoolean("odometer")) {
				odometer.setMaintenanceDone();
			}
			
			if(json.optBoolean("c02")) {
				c02.setMaintenanceDone();
			}
			
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
