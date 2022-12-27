package io.boodskap.iot.simulator;

public abstract class AbstractSimulator<T extends IGateway> implements ISimulator {

	private final T gateway;
	
	public AbstractSimulator(T gateway) {
		this.gateway = gateway;
	}

	@Override
	public T getGateway() {
		return gateway;
	}

	@Override
	public long getSpecId() {
		return Config.get().getSpecId();
	}

	@Override
	public String getRuleType() {
		return null;
	}

	@Override
	public String getContentType() {
		return "application/octet-stream";
	}

	@Override
	public String getProperties() {
		return "";
	}

	@Override
	public DataType getBinaryDataType() {
		return DataType.BASE64;
	}

}
