package io.boodskap.iot.simulator;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

public class Config {
	
	private static final Properties props = new Properties();

	private static final String dbPath;
	
	private static final ThreadLocal<Connection> MYCONNECTION = new ThreadLocal<Connection>();
	
	static {
		try {
			
			final File homeDir =  new File(System.getProperty("user.home"));
			final File configDir = new File(homeDir, "config");
			configDir.mkdirs();
			
			final String defConfigPath = new File(configDir, "simulator.properties").getAbsolutePath();
			final String configPath = Config.getArgOrEnv("CONFIG", defConfigPath);
			final File configFile = new File(configPath);
			
			if(!configFile.exists()) {
				System.err.format("Config file %s not found, using defaults...\n", configFile.getAbsolutePath());
				InputStream in = Config.class.getResourceAsStream("/config/simulator.properties");
				if(null != in) {
					props.load(in);
				}
			}else {
				System.out.format("Config loading from %s\n", configFile.getAbsolutePath());
				props.load(new FileInputStream(configFile));
			}
			
			File file = new File(configDir, "simulators.db");
			dbPath = file.getAbsolutePath();
			
			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {	
				@Override
				public void run() {
					try {
						System.err.println("Shutting down....");
						System.err.flush();
						Config.getConnection().createStatement().execute("SHUTDOWN COMPACT");
						Config.closeConnection();
					}catch(Exception ex) {
						ex.printStackTrace();
					}
				}
			}));
			
			Class.forName("org.hsqldb.jdbc.JDBCDriver");
			
			Connection c = Config.getConnection();
			
			c.createStatement().execute("CREATE TABLE IF NOT EXISTS STAMPS(KEY VARCHAR(255) NOT NULL, STAMP NUMERIC NOT NULL, PRIMARY KEY (KEY));");
			c.createStatement().execute("CREATE TABLE IF NOT EXISTS PROPS(KEY VARCHAR(255) NOT NULL, VALUE VARCHAR(2048) NOT NULL, PRIMARY KEY (KEY));");
			
			Config.closeConnection();
			
			
		}catch(Exception ex) {
			ex.printStackTrace();
			System.exit(-1);
			throw new RuntimeException();//This should never happen
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
	private final String hequipDevToken = Config.getArgOrEnvOrCfg("HEQUIP_DEV_TOKEN", null); 
	private final String utilityDevToken = Config.getArgOrEnvOrCfg("UTILITY_DEV_TOKEN", null); 

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
	
	public static final Connection getConnection() throws SQLException {
		
		Connection c = MYCONNECTION.get();
		
		if(null == c || c.isClosed()) {
			c = DriverManager.getConnection(String.format("jdbc:hsqldb:file:%s;", dbPath), "SA", "");
			MYCONNECTION.set(c);
		}
		
		c.setAutoCommit(true);
		
		return c;
		
	}
	
	public static final void closeConnection(){
		
		try {
			
			Connection c = MYCONNECTION.get();
			
			if(null != c && !c.isClosed()) {
				c.commit();
				c.close();
				MYCONNECTION.remove();
			}
			
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		
	}

	public long getOrCreateStamp(String key) throws SQLException {
		
		final Long value;
		
		try {
			
			Connection c = Config.getConnection();
			
			ResultSet rs = c.createStatement().executeQuery("SELECT STAMP FROM STAMPS WHERE KEY='" + key + "'");
			
			if(rs.next()) {
				value = rs.getLong("STAMP");
			}else {
				value = putStamp(key);
			}
			
		}finally {
			Config.closeConnection();
		}
		
		return value;
	}
	
	public long putStamp(String key) throws SQLException {
		try {
			
			long stamp = System.currentTimeMillis();
			
			Connection c = Config.getConnection();
			
			PreparedStatement dps = c.prepareStatement("DELETE FROM STAMPS WHERE KEY=?");
			dps.setString(1, key);
			dps.executeUpdate();
			
			PreparedStatement ps = c.prepareStatement("INSERT INTO STAMPS(KEY,STAMP) VALUES(?,?)");
			ps.setString(1, key);
			ps.setLong(2, stamp);
			ps.executeUpdate();
			
			return stamp;
			
		}finally {
			Config.closeConnection();
		}
	}
	
	public String getValue(String key) throws SQLException {
		
		final String value;
		
		try {
			
			Connection c = Config.getConnection();
			
			ResultSet rs = c.createStatement().executeQuery("SELECT VALUE FROM PROPS WHERE KEY='" + key + "'");
			
			if(rs.next()) {
				value = rs.getString("STAMP");
			}else {
				value = null;;
			}
			
		}finally {
			Config.closeConnection();
		}
		
		return value;
	}

	public String getValue(String key, String def) throws SQLException {
		
		String value;
		
		try {

			value = getValue(key);
			
			if(null == value) {
				value = def;
				setValue(key, value);
			}
			
		}finally {
			Config.closeConnection();
		}
		
		return value;
	}

	public void setValue(String key, String value) throws SQLException {
		try {
			
			Connection c = Config.getConnection();
			
			PreparedStatement dps = c.prepareStatement("DELETE FROM PROPS WHERE KEY=?");
			dps.setString(1, key);
			dps.executeUpdate();
			
			PreparedStatement ps = c.prepareStatement("INSERT INTO PROPS(KEY,VALUE) VALUES(?,?)");
			ps.setString(1, key);
			ps.setString(2, value);
			ps.executeUpdate();
			
		}finally {
			Config.closeConnection();
		}
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

	public String getHequipDevToken() {
		return hequipDevToken;
	}

	public String getUtilityDevToken() {
		return utilityDevToken;
	}

}
