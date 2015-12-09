package de.dfs.servicemonitor.monitor;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import akka.actor.Actor;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;
import de.dfs.servicemonitor.etcd.responsemodel.EtcdClient;
import de.dfs.servicemonitor.etcd.responsemodel.Response;
import play.libs.F.Promise;
import scala.concurrent.Future;

@RunWith(MockitoJUnitRunner.class)
public class SimulatorMonitorTest
{

  private static final String SESSION_1 = "session-1";

  private static final String MONITOR_NAME = "monitor";

  private static ActorSystem system;
  final JavaTestKit testKit = new JavaTestKit(system);

  @Mock
  EtcdClient etcdClient;

  @Mock 
  Response response;
  
  private TestActorRef< Actor > monitorActorRef;
  
  @BeforeClass
  public static void setupClass()
  {
    system = ActorSystem.create();  
  }
  
  
  @Before
  public void setup() {
  }


  @AfterClass
  public static void teardown()
  {
    JavaTestKit.shutdownActorSystem(system);
    system = null;
  }

  @Before
  public void setUp() throws Exception
  {
  }


  @Test
  public void test()
  {
    Props simulatorMonitorProps = Props.create(SimulatorMonitor.class, etcdClient);
    Promise< Response > responsePromise = Promise.timeout(response,100);
    when(etcdClient.watch(SESSION_1)).thenReturn(responsePromise);
    monitorActorRef = TestActorRef.create(system, simulatorMonitorProps, MONITOR_NAME);
    monitorActorRef.tell(new SimulatorMonitor.WaitForSocketPublishing(SESSION_1), testKit.getRef());
    verify(etcdClient).watch(SESSION_1+"/"+"socket");
    
  }

}
