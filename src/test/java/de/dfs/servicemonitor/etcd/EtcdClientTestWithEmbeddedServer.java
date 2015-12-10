package de.dfs.servicemonitor.etcd;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static play.mvc.Results.ok;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import de.dfs.servicemonitor.etcd.EtcdClient;
import de.dfs.servicemonitor.etcd.responsemodel.Converter;
import de.dfs.servicemonitor.etcd.responsemodel.Node;
import de.dfs.servicemonitor.etcd.responsemodel.Response;
import play.api.routing.Router;
import play.libs.F.Promise;
import play.libs.ws.WS;
import play.libs.ws.WSClient;
import play.routing.RoutingDsl;
import play.server.Server;

@RunWith(MockitoJUnitRunner.class)
public class EtcdClientTestWithEmbeddedServer
{

  
  private EtcdClient etcdClient;
  private Server server;
  private WSClient ws;
  private Response getResponse;
  private Converter convert;


  @Before
  public void setUp() throws Exception
  {
    convert = new Converter();
   Router router = new RoutingDsl().GET("/v2/keys/test")
        .routeTo(() -> {
          createGetResponse();
          String json = convert.toJson(getResponse);
           return ok(json);
        })
        .build();

    server = Server.forRouter(router);
    ws = WS.newClient(server.httpPort());
    etcdClient = new EtcdClient(ws, "", new Converter()); 
  }
  
  @After
  public void tearDown() {
    server.stop();
  }


  @Test
  public void testSimpleGetWithEmbeddedServer()
  {
    Promise< Response > promise = etcdClient.create().get("test");
    Response response = promise.get(1000);
    assertThat(getResponse.action, equalTo(response.action));
    assertThat(getResponse.node.createdIndex, equalTo(response.node.createdIndex));
    assertThat(getResponse.node.modifiedIndex, equalTo(response.node.modifiedIndex)); 
    assertThat(getResponse.prevNode, equalTo(response.prevNode));
  }

  private void createGetResponse()
  {
    getResponse = new Response();
    getResponse.action = "get";
    getResponse.node = new Node();
    getResponse.node.createdIndex = 1;
    getResponse.node.modifiedIndex = 2;
    getResponse.node.key = "test";
    getResponse.node.value = "testValue";
  }

}
