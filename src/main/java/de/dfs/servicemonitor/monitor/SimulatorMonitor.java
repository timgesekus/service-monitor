package de.dfs.servicemonitor.monitor;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import de.dfs.servicemonitor.etcd.responsemodel.EtcdClient;

public class SimulatorMonitor extends AbstractActor
{
  private EtcdClient etcdClient;

  private final LoggingAdapter log = Logging.getLogger(context().system(), this);
  
  public static class WaitForSocketPublishing
  {
    public String simulatorKey;


    public WaitForSocketPublishing(String simulatorKey)
    {
      this.simulatorKey = simulatorKey;
    }
  }


  public SimulatorMonitor(EtcdClient etcdClient)
  {
    this.etcdClient = etcdClient;
    receive(ReceiveBuilder.match(WaitForSocketPublishing.class, this::handleMonitor)
        .matchAny(o -> log.info("Received unkown message " + o)).build());
  }


  public void handleMonitor(WaitForSocketPublishing waForSocketPublishing)
  { 
    etcdClient.watch(waForSocketPublishing.simulatorKey + "/" + "socket");
  }
}
