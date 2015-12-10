package de.dfs.servicemonitor.monitor;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import akka.actor.Actor;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;
import de.dfs.servicemonitor.monitor.WebServiceHealthMonitor.MonitorHealth;

public class WebServiceHealthMonitorTest {
 private static final String MONITOR = "monitor";
private TestActorRef< Actor > monitorActorRef;
private ActorSystem system;
private JavaTestKit testKit;
  
  @BeforeClass
  public static void setupClass()
  {
  }
  
  
  @Before
  public void setup() {
    system = ActorSystem.create();  
    testKit = new JavaTestKit(system);

  }


  @After
  public void teardown()
  {
    JavaTestKit.shutdownActorSystem(system);
    system = null;
  }
  
  @Test
  public void normalFlow() {
    Props socketPublishingMonitorProps = Props.create(WebServiceHealthMonitor.class);
    monitorActorRef = TestActorRef.create(system, socketPublishingMonitorProps, MONITOR);
    MonitorHealth monitorHealth = new WebServiceHealthMonitor.MonitorHealth("SOCKET");
    monitorActorRef.tell(monitorHealth, testKit.getRef());
    
  }
}
