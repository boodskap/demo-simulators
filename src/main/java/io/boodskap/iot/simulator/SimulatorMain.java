package io.boodskap.iot.simulator;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import io.boodskap.iot.simulator.parking.ParkingGateway;

public class SimulatorMain{
	
	public static final LinkedBlockingQueue<ISimulator> queue = new LinkedBlockingQueue<>();
	
	private static final Set<IGateway> GATEWAYS = new HashSet<>();
	
	static {

		GATEWAYS.add(new ParkingGateway(Config.get().getParkingGarageId(), Config.get().getParkingGarageRows(), Config.get().getParkingGarageCols()));
	}
	
	static {
		GATEWAYS.forEach(gw -> {
			try {
				gw.init();
			}catch(Exception ex) {
				ex.printStackTrace();
				System.exit(-1);
			}
		});
	}
	
	private static final int THREADS = 5;
	private ExecutorService exec = Executors.newFixedThreadPool(THREADS);
	
	private SimulatorMain() {
		
	}
	
	private void start() {
		
		for(int i=0;i<THREADS;i++) {
			exec.submit(new MessageSenders());
		}
		
		try {
			
			while(!Thread.currentThread().isInterrupted()) {
				
				GATEWAYS.forEach(gw -> {
					gw.getSimulators().forEach(s -> {
						if(s.canSimulate()) {
							queue.offer(s);
						}
					});
				});
				
				Thread.sleep(1000);
				
				if(Thread.currentThread().isInterrupted()) break;
			}
			
		}catch(Exception ex) {
			ex.printStackTrace();
			System.exit(-1);
		}finally {
			System.err.println("Exitted...");
			System.exit(-1);
		}
	}

	class MessageSenders implements Runnable{
		
		@Override
		public void run() {
			try {
				
				while(!Thread.currentThread().isInterrupted()) {
					
					ISimulator simulator = queue.take();
					
					try {
						
						Map<String, Object> data = simulator.simulate();
						
						data.put("sensor", simulator.getSensorType());
						
						//System.out.format("Sending spec:%d data:%s\n", simulator.getSpecId(), data);
						
						simulator.getGateway().send(simulator.getDeviceToken(), Config.get().getSpecId(), data);
						
					}catch(Exception ex) {
						ex.printStackTrace();
					}
					
				}
				
			}catch(Exception ex) {
				ex.printStackTrace();
			}
			
		}
		
	}

	public static void main(String[] args) {
		new SimulatorMain().start();
	}

}
