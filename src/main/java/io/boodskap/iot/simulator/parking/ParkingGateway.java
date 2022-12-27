package io.boodskap.iot.simulator.parking;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import io.boodskap.iot.simulator.AbstractGateway;
import io.boodskap.iot.simulator.Config;
import io.boodskap.iot.simulator.ISimulator;

public class ParkingGateway extends AbstractGateway {
	
	private final String garageId;
	private final int rows;
	private final int columns;
	private boolean configSent;
	
	private final Set<ISimulator> simulators = new HashSet<>();
	
	public ParkingGateway(String garageId, int rows, int columns) {
		this.garageId = garageId;
		this.rows = rows;
		this.columns = columns;
	}

	/**
	@Override
	public void send(String deviceToken, long specId, Map<String, Object> message) throws Exception {
		sendHttp(deviceToken, specId, message);
	}
	**/

	@Override
	public void init() throws Exception {
		for(int row=1;row<=rows;row++) {
			for(int column=1;column<=columns;column++) {
				simulators.add(new ParkingSimulator(this, row, column));
			}
		}
	}

	@Override
	public String getDeviceToken() {
		return Config.get().getParkingDevToken();
	}

	@Override
	public Set<ISimulator> getSimulators() {
		return simulators;
	}

	public String getGarageId() {
		return garageId;
	}

	public int getRows() {
		return rows;
	}

	public int getColumns() {
		return columns;
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

	public boolean isConfigSent() {
		return configSent;
	}

	public void setConfigSent(boolean configSent) {
		this.configSent = configSent;
	}

}
