package io.boodskap.iot.simulator;

public interface ISimulator {
	
	public IGateway getGateway();
	
	public String getSensorType();
	
	public String getDeviceToken();
	
	public String getDeviceId();
	
	public boolean canSimulate();

	/**
	 * 
	 * @return Either Map<String, Object> or byte[]
	 */
	public Object simulate()throws Exception;
	
	public long getSpecId();
	
	public String getRuleType();
	
	public String getContentType();
	
	public String getProperties();
	
	public DataType getBinaryDataType();
}
