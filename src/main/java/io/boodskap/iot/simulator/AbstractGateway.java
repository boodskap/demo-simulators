package io.boodskap.iot.simulator;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
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
	private boolean subscribed;
	
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
			
			final String clientId = getDeviceToken();
			
			if(StringUtils.isBlank(clientId)) {
				System.err.format("%s getDeviceToken is null\n", getClass().getSimpleName());
			}
			
			mqtt = new MqttClient(Config.get().getMqttUrl(), getDeviceToken(), persistence);
			
			try {mqtt.disconnect();}catch(Exception ex) {}
			
			mqtt.setCallback(this);
			
		}
		
		if(!mqtt.isConnected()) {
			try { mqtt.connect(opts); }catch(Exception ex) {System.err.println(ex.getMessage());};
		}
		
		if(mqtt.isConnected() && !subscribed) {
			getIncomingTopics().forEach(t -> {
				System.out.format("Subscribing to %s\n", t);
				try { mqtt.subscribe(t, 0); }catch(Exception ex) {ex.printStackTrace();}
				subscribed = true;
			});
		}
		
		
		return mqtt;
	}

	@Override
	public void send(String deviceToken, long specId, Map<String, Object> message) throws Exception{
		sendMqtt(deviceToken, specId, message);
	}
	
	@Override
	public void sendHttp(String deviceToken, long specId, Map<String, Object> message) throws Exception {
		
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
	public void sendMqtt(String deviceToken, long specId, Map<String, Object> message) throws Exception {

		MqttClient mqtt = getMqtt();
		
		if(!mqtt.isConnected()) {
			subscribed = false;
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


	@Override
	public void send(String deviceToken, String ruleType, DataType dataType, byte[] data, String contentType, String properties) throws Exception {

		MqttClient mqtt = getMqtt();
		
		if(!mqtt.isConnected()) {
			System.err.println("MQTT not connected.");
			return;
		}
		
		MqttMessage packet = new MqttMessage(data);
		packet.setQos(2);
		
		final String topic = String.format("/%s/pub/bin/%s/%s/%s", deviceToken, ruleType, contentType, properties);
		
		switch(dataType) {
		case HEX:
			System.out.format("Publishing to %s data:%s\n", topic, Hex.encodeHexString(data));
			break;
		case JSON:
			System.out.format("Publishing to %s data:%s\n", topic, new JSONObject(new String(data)));
			break;
		case STRING:
			System.out.format("Publishing to %s data:%s\n", topic, new String(data));
			break;
		default:
		case BASE64:
			System.out.format("Publishing to %s data:%s\n", topic, Base64.encodeBase64String(data));
			break;
		
		}
		
		mqtt.publish(topic, packet);
		
	}

	@Override
	public Set<String> getIncomingTopics() {
		return new HashSet<String>();
	}


}
