package io.boodskap.iot.simulator.he;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import io.boodskap.iot.simulator.AbstractGateway;
import io.boodskap.iot.simulator.Config;
import io.boodskap.iot.simulator.ISimulator;
import io.boodskap.iot.simulator.common.Odometer;

public class HeavyEquipmentGateway extends AbstractGateway {
	
	private final Set<ISimulator> simulators = new HashSet<>();
	private final Set<String> TOPICS = new HashSet<>();
	
	private final String gatewayId;
	private final Odometer.DrivingMode drivingMode;
	
	public HeavyEquipmentGateway(String gatewayId, Odometer.DrivingMode drivingMode) {
		this.gatewayId = gatewayId;
		this.drivingMode = drivingMode;
	}

	@Override
	public void init() throws Exception {
		
		TOPICS.add(String.format("/%s/sub/downlink/%s", Config.get().getDomainKey(), getId()));
		
		simulators.add(new Odometer(this, drivingMode, "ODO01", getDeviceToken()));
		
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
		System.out.println("*** MESSAGE ARRIVED ***");
		System.out.println(new String(message.getPayload()));
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
