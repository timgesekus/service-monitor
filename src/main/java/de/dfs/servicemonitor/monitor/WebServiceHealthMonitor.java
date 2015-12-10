package de.dfs.servicemonitor.monitor;

import akka.actor.AbstractActor;

public class WebServiceHealthMonitor extends AbstractActor {
  public static class MonitorHealth {
    public String url;

    public MonitorHealth(String url) {
      this.url = url;
    }
  }
}
