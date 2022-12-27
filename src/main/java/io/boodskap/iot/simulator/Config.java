package io.boodskap.iot.simulator;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

public class Config {
	
	private static final Properties props = new Properties();

	static {
		try {
			
			final File homeDir =  new File(System.getProperty("user.home"));
			final File configDir = new File(homeDir, "config");
			final String defConfigPath = new File(configDir, "simulator.properties").getAbsolutePath();
			final String configPath = Config.getArgOrEnv("CONFIG", defConfigPath);
			final File configFile = new File(configPath);
			
			if(!configFile.exists()) {
				System.err.format("Config file %s not found, using defaults...\n", configFile.getAbsolutePath());
				props.load(Config.class.getResourceAsStream("/config/simulator.properties"));
			}else {
				System.out.format("Config loading from %s\n", configFile.getAbsolutePath());
				props.load(new FileInputStream(configFile));
			}
			
		}catch(Exception ex) {
			ex.printStackTrace();
			System.exit(-1);
		}
	}
	
	private static final Config instance = new Config();
	
	private final String httpUrl = Config.getArgOrEnvOrCfg("HTTP_URL", "https://v5.boodskap.io/api");
	private final String mqttUrl = Config.getArgOrEnvOrCfg("MQTT_URL", "tcp://v5.boodskap.io:1883");
	private final String domainKey = Config.getArgOrEnvOrCfg("DOMAIN_KEY", null);
	private final String apiKey = Config.getArgOrEnvOrCfg("API_KEY", null);
	private final int specId = Integer.valueOf(Config.getArgOrEnvOrCfg("SPEC_ID", "100"));
	
	private final String parkingDevToken = Config.getArgOrEnvOrCfg("PARKING_DEV_TOKEN", null);
	private final String parkingGarageId = Config.getArgOrEnvOrCfg("PARKING_GARAGE_ID", "1 Penn Plazza");
	private final int parkingGarageRows = Integer.valueOf(Config.getArgOrEnvOrCfg("PARKING_GARAGE_ROWS", "2"));
	private final int parkingGarageCols = Integer.valueOf(Config.getArgOrEnvOrCfg("PARKING_GARAGE_COLS", "10"));
	
	private final String lht65DevToken = Config.getArgOrEnvOrCfg("LHT65_DEV_TOKEN", null); 

	private Config() {
	}
	
	public static final String getArg(String key, String def){
		String value = System.getProperty(key);
		return StringUtils.isBlank(value) ? def : value;
	}

	public static final String getArgOrEnv(String key, String def){
		String value = getArg(key, null);
		if(StringUtils.isBlank(value)){
			value = System.getenv(key);
		}
		return StringUtils.isBlank(value) ? def : value;
	}
	
	public static final String getArgOrEnvOrCfg(String key, String def){
		String value = getArgOrEnv(key, null);
		if(StringUtils.isBlank(value)){
			value = props.getProperty(key);
		}
		return StringUtils.isBlank(value) ? def : value;
	}
	
	public static final Config get() {
		return instance;
	}

	public String getMqttUrl() {
		return mqttUrl;
	}

	public String getDomainKey() {
		return domainKey;
	}

	public String getApiKey() {
		return apiKey;
	}

	public String getHttpUrl() {
		return httpUrl;
	}

	public String getParkingDevToken() {
		return parkingDevToken;
	}

	public String getParkingGarageId() {
		return parkingGarageId;
	}

	public int getParkingGarageRows() {
		return parkingGarageRows;
	}

	public int getParkingGarageCols() {
		return parkingGarageCols;
	}

	public int getSpecId() {
		return specId;
	}

	public String getLht65DevToken() {
		return lht65DevToken;
	}

}
