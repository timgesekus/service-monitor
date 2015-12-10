package de.dfs.servicemonitor.monitor;

import org.jboss.netty.handler.timeout.TimeoutException;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import akka.actor.Actor;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;
import de.dfs.servicemonitor.etcd.EtcdClient;
import de.dfs.servicemonitor.etcd.EtcdClient.Request;
import de.dfs.servicemonitor.etcd.responsemodel.Response;
import de.dfs.servicemonitor.monitor.SocketPublishingMonitor.SocketPublished;
import de.dfs.servicemonitor.monitor.SocketPublishingMonitor.WaitForSocketFailed;
import play.libs.F.Promise;

@RunWith(MockitoJUnitRunner.class)
public class SocketPublishingMonitorTest
{

  private static final String TIMEOUT = "TIMEOUT";

  private static final String SOCKET = "SOCKET";

  private static final String SESSION_1 = "session-1";

  private static final String MONITOR_NAME = "monitor";

  private ActorSystem system;
  private JavaTestKit testKit;

  @Mock
  EtcdClient etcdClient;

  @Mock
  Request request;
  
  Response response = new Response();
  
  private TestActorRef< Actor > monitorActorRef;
  
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
  public void testNormalFlow()
  {
    setupEtcdClientToReturnResponseWithSocket();
    createSocketPublishingMonitor();
    sendWaitForSocketPublishing();
    assertSocketPublishedMessageReceived();    
  }

  @Test
  public void testTimeOut()
  {
    setupEtcdClientToThrowTimeoutRequest();
    createSocketPublishingMonitor();
    sendWaitForSocketPublishing();
    assertSocketPublishingFailedMessageReceived();
  }

  private void setupEtcdClientToReturnResponseWithSocket() {
    Promise< Response > responsePromise = Promise.timeout(response,100);
    response.node.value=SOCKET;
    when(etcdClient.create()).thenReturn(request);
    when(request.get(SESSION_1+"/"+"socket")).thenReturn(responsePromise);
  }

  private void createSocketPublishingMonitor() {
    Props socketPublishingMonitorProps = Props.create(SocketPublishingMonitor.class, etcdClient);
    monitorActorRef = TestActorRef.create(system, socketPublishingMonitorProps, MONITOR_NAME);
  }

  private void sendWaitForSocketPublishing() {
    monitorActorRef.tell(new SocketPublishingMonitor.WaitForSocketPublishing(SESSION_1), testKit.getRef());
  }


  private void assertSocketPublishedMessageReceived() {
    SocketPublished socketPublished = testKit.expectMsgClass(SocketPublished.class);
    assertThat(socketPublished.socket, equalTo(SOCKET));
  }
 

 

  private void assertSocketPublishingFailedMessageReceived() {
    WaitForSocketFailed socketPublished = testKit.expectMsgClass(WaitForSocketFailed.class);
    assertThat(socketPublished.message, equalTo(TIMEOUT));   
    assertTrue(socketPublished.t instanceof TimeoutException);
  }


  private void setupEtcdClientToThrowTimeoutRequest() {
    Promise< Response > responsePromise = Promise.throwing(new TimeoutException(TIMEOUT));
    when(etcdClient.create()).thenReturn(request);
    when(request.get(SESSION_1+"/"+"socket")).thenReturn(responsePromise);
  }
}
