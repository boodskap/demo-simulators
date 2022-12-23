package io.boodskap.iot.simulator;

import java.util.Map;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;

public abstract class AbstractGateway implements IGateway, MqttCallback {
	
	private MqttConnectOptions opts;
	private MqttClient mqtt;
	
	protected AbstractGateway() {
	}
	
	protected MqttClient getMqtt() throws Exception {
		
		if(null == opts) {
			
			opts = new MqttConnectOptions();
			opts.setCleanSession(true);
			opts.setAutomaticReconnect(false);
			opts.setKeepAliveInterval(60);
			
		}
		
		if(null == mqtt) {
			
			MemoryPersistence persistence = new MemoryPersistence();
			
			mqtt = new MqttClient(Config.get().getMqttUrl(), getDeviceToken(), persistence);
			
			try {mqtt.disconnect();}catch(Exception ex) {}
			
			mqtt.setCallback(this);
			
		}
		
		if(!mqtt.isConnected()) {
			try { mqtt.connect(opts); }catch(Exception ex) {System.err.println(ex.getMessage());};
		}
		
		return mqtt;
	}

	@Override
	public void sendHttp(String deviceToken, int specId, Map<String, Object> message) throws Exception {
		
		JSONObject json = new JSONObject(message);
		
		HttpResponse<JsonNode> resp = Unirest.post(String.format("%s/push/message/{mid}", Config.get().getHttpUrl()))
		.routeParam("mid", String.valueOf(specId))
		.header("TOKEN", deviceToken)
		.header("accept", "application/json")
		.header("content-type", "application/json")
		.body(json.toString())
		.asJson()
		;
		
		System.out.format("Published data:%s response:%s\n", json, resp.getBody().toString());

	}
	
	@Override
	public void sendMqtt(String deviceToken, int specId, Map<String, Object> message) throws Exception {

		MqttClient mqtt = getMqtt();
		
		if(!mqtt.isConnected()) {
			System.err.println("MQTT not connected.");
			return;
		}
		
		JSONObject json = new JSONObject(message);
		byte[] data = json.toString().getBytes();
		
		
		MqttMessage packet = new MqttMessage(data);
		packet.setQos(2);
		
		final String topic = String.format("/%s/pub/%d", deviceToken, specId);
		
		mqtt.publish(topic, packet);
		
		System.out.format("Published to %s data:%s\n", topic, new String(data));
	}


}
