package de.dfs.servicemonitor.monitor;

import de.dfs.servicemonitor.etcd.EtcdClient;
import de.dfs.servicemonitor.etcd.EtcdClient.Connection;
import de.dfs.servicemonitor.etcd.responsemodel.Response;
import play.libs.F.Promise;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;

public class ServiceMonitor {
	private final int millisBetweenConnectionRetries ;
	private final int timeoutForConnection;
	private final int maxNumberOfConnectionRetries ;
	private final int waitForSocketPublishing;
	private EtcdClient etcdClient;
	private WSClient wsClient;
	private String url;
	private String session;
	
	public ServiceMonitor(EtcdClient etcdClient, WSClient wsClient, String url, String sessionId, ServiceMonitorConfig config) {
		this.etcdClient = etcdClient;
		this.wsClient = wsClient;
		this.url = url;
		session = sessionId;
		millisBetweenConnectionRetries = config.getMillisBetweenConnectionRetries();
		timeoutForConnection = config.getTimeoutForConnection();
		maxNumberOfConnectionRetries = config.getMaxNumberOfConnectionRetries();
		waitForSocketPublishing = config.getWaitForSocketPublishing();
	}

	public void start(String socketKey, long waitForSocket) {
		waitForSocketPublishing();
		tryInitialConnect();
		monitorService();
	}

	private void waitForSocketPublishing() {
		Connection request = etcdClient.createConnection();
		request.setWait();
		Promise<Response> promise = request.get(session + "/" + "socket");
		try {
			promise.get(waitForSocketPublishing);
		} catch (Throwable t) {
			publishDeadState();
			exit("Wait for socket failed");
		}
	}

	private void tryInitialConnect() {
		boolean isConnected = false;
		int numberOfRetries = 0;
		do {
			try {
				WSRequest request = wsClient.url(url).setRequestTimeout(timeoutForConnection);
				request.get();
				isConnected = true;
				publishAliveState();
			} catch (Throwable t) {
				numberOfRetries++;
			}
		} while (!isConnected || numberOfRetries < maxNumberOfConnectionRetries);
		if (!isConnected) {
			publishDeadState();
			exit("Initial connect failed");
		}
	}

	public void monitorService() {
		boolean isConnected = false;
		do {
			try {
				WSRequest request = wsClient.url(url).setRequestTimeout(timeoutForConnection);
				request.get();
				isConnected = true;
				publishAliveState();
				Thread.sleep(millisBetweenConnectionRetries);
			} catch (Throwable t) {
				isConnected = false;
			}
		} while (isConnected);
		publishDeadState();
		exit("Initial connect failed");
	}


	private void exit(String errorMsg) {
		System.err.println(errorMsg);
	}

	public void publishDeadState() {
		Connection create = etcdClient.createConnection();
		create.set(session + "/state", "dead");
	}

	public void publishAliveState() {
		Connection connection = etcdClient.createConnection();
		connection.setTTL(1000);
		connection.set(session + "/state", "alive");
	}
}
