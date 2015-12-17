package de.dfs.servicemonitor.monitor;

import com.typesafe.config.Config;

public class ServiceMonitorConfig {
	private final int millisBetweenConnectionRetries;
	private final int waitForConnect;
	private final int maxNumberOfConnectionRetries;
	private final int waitForSocketPublishing;

	public ServiceMonitorConfig(Config config) {
		millisBetweenConnectionRetries = config.getInt("millis-between-connection-retries");
		waitForConnect = config.getInt("wait-for-connect");
		maxNumberOfConnectionRetries = config.getInt("max-number-of-connection-retries");
		waitForSocketPublishing = config.getInt("wait-for-socket-publishing");
	}

	public int getMillisBetweenConnectionRetries() {
		return millisBetweenConnectionRetries;
	}

	public int getTimeoutForConnection() {
		return waitForConnect;
	}

	public int getMaxNumberOfConnectionRetries() {
		return maxNumberOfConnectionRetries;
	}

	public int getWaitForSocketPublishing() {
		return waitForSocketPublishing;
	}
}
