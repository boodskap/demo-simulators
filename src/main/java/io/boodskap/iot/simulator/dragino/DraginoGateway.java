package io.boodskap.iot.simulator.dragino;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import io.boodskap.iot.simulator.AbstractGateway;
import io.boodskap.iot.simulator.Config;
import io.boodskap.iot.simulator.ISimulator;

public class DraginoGateway extends AbstractGateway {
	
	private final Set<ISimulator> simulators = new HashSet<>();
	private final Set<String> TOPICS = new HashSet<>();
	
	private final String gatewayId;
	
	public DraginoGateway(String gatewayId) {
		this.gatewayId = gatewayId;
	}

	@Override
	public void init() throws Exception {
		
		TOPICS.add(String.format("/%s/sub/downlink/%s", Config.get().getDomainKey(), getId()));
		
		simulators.add(new LHT65Simulator(this, "LHT65001", "2015/01/27", 20, 30, 6));
		simulators.add(new LHT65Simulator(this, "LHT65002", "2021/03/06", 30, 35, 7));
		simulators.add(new LHT65Simulator(this, "LHT65003", "2016/01/27", 0, 10, 0));
		simulators.add(new LHT65Simulator(this, "LHT65004", "2017/12/08", 10, 20, 1));
		simulators.add(new LHT65Simulator(this, "LHT65005", "2018/11/11", 20, 30, 2));
		simulators.add(new LHT65Simulator(this, "LHT65006", "2019/07/16", 30, 40, 9));
		simulators.add(new LHT65Simulator(this, "LHT65007", "2020/05/13", 40, 50, 9));
		simulators.add(new LHT65Simulator(this, "LHT65008", "2021/04/29", 10, 30, 5));
		simulators.add(new LHT65Simulator(this, "LHT65009", "2022/01/09", 20, 40, 6));
		simulators.add(new LHT65Simulator(this, "LHT65010", "2013/11/11", 20, 50, 7));
		simulators.add(new LHT65Simulator(this, "LHT65011", "2014/12/27", 10, 30, 8));
		simulators.add(new LHT65Simulator(this, "LHT65012", "2022/02/03", 30, 50, 9));
		simulators.add(new LHT65Simulator(this, "LHT65013", "2022/03/02", 0, 30, 2));
		simulators.add(new LHT65Simulator(this, "LHT65014", "2022/04/07", 0, 50, 3));
		simulators.add(new LHT65Simulator(this, "LHT65015", "2022/06/01", 20, 40, 4));
	}

	@Override
	public String getDeviceToken() {
		return Config.get().getLht65DevToken();
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
