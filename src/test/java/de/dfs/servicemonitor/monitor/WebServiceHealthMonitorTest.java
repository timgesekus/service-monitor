package de.dfs.servicemonitor.monitor;

import static play.mvc.Results.ok;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import akka.actor.Actor;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;
import de.dfs.servicemonitor.monitor.WebServiceHealthMonitor.Alive;
import de.dfs.servicemonitor.monitor.WebServiceHealthMonitor.Dead;
import de.dfs.servicemonitor.monitor.WebServiceHealthMonitor.MonitorHealth;
import play.api.routing.Router;
import play.libs.ws.WS;
import play.libs.ws.WSClient;
import play.routing.RoutingDsl;
import play.server.Server;

public class WebServiceHealthMonitorTest
{
  private static final String MONITOR = "monitor";
  private TestActorRef< Actor > monitorActorRef;
  private ActorSystem system;
  private JavaTestKit testKit;
  private WSClient ws;
  private Server server;
  private WSClient timeOutWs;


  @BeforeClass
  public static void setupClass()
  {
  }


  @Before
  public void setup()
  {
    system = ActorSystem.create();
    testKit = new JavaTestKit(system);
    Router router = new RoutingDsl().GET("/")
        .routeTo(() -> {
          return ok("alive");
        })
        .build();
    server = Server.forRouter(router);
    ws = WS.newClient(server.httpPort());

    Router timeoutRouter = new RoutingDsl().GET("/")
        .routeTo(() -> {
          Thread.sleep(20000);
          return ok("alive");
        })
        .build();
   Server timoutServer = Server.forRouter(timeoutRouter);
   timeOutWs = WS.newClient(timoutServer.httpPort());
  }


  @After
  public void teardown()
  {
    JavaTestKit.shutdownActorSystem(system);
    system = null;
  }


  @Test
  public void normalFlow()
  {
    Props socketPublishingMonitorProps = Props.create(WebServiceHealthMonitor.class, ws);
    monitorActorRef = TestActorRef.create(system, socketPublishingMonitorProps, MONITOR);
    MonitorHealth monitorHealth = new WebServiceHealthMonitor.MonitorHealth("/");
    monitorActorRef.tell(monitorHealth, testKit.getRef());
    testKit.expectMsgClass(Alive.class);
    testKit.expectMsgClass(Alive.class);

  }
  
  @Test
  public void timeoutFlow()
  {
    Props socketPublishingMonitorProps = Props.create(WebServiceHealthMonitor.class, timeOutWs);
    monitorActorRef = TestActorRef.create(system, socketPublishingMonitorProps, MONITOR);
    MonitorHealth monitorHealth = new WebServiceHealthMonitor.MonitorHealth("/");
    monitorActorRef.tell(monitorHealth, testKit.getRef());
    testKit.expectMsgClass(Dead.class);

  }
}
