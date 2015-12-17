package de.dfs.servicemonitor.monitor;

import java.time.Duration;

import com.typesafe.config.Config;

public class ServiceMonitorConfig {
  private final Duration durationBetweenConnectionRetries;
  private final Duration waitForConnectTimeout;
  private final int maxNumberOfConnectionRetries;
  private final Duration waitForSocketPublishing;
  private final String etcdSocketKey;

  public ServiceMonitorConfig(Config config) {
    durationBetweenConnectionRetries = config.getDuration("millis-between-connection-retries");
    waitForConnectTimeout = config.getDuration("wait-for-connect");
    maxNumberOfConnectionRetries = config.getInt("max-number-of-connection-retries");
    waitForSocketPublishing = config.getDuration("wait-for-socket-publishing");
    etcdSocketKey = config.getString("etcd-socket-key");
  }

  public Duration getDurationBetweenConnectionRetries() {
    return durationBetweenConnectionRetries;
  }

  public Duration getTimeoutForConnection() {
    return waitForConnectTimeout;
  }

  public int getMaxNumberOfConnectionRetries() {
    return maxNumberOfConnectionRetries;
  }

  public Duration getWaitForSocketPublishing() {
    return waitForSocketPublishing;
  }

  public String getSocketKey() {
    return etcdSocketKey;
  }
}
