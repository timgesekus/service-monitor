package de.dfs.servicemonitor.etcd;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import de.dfs.servicemonitor.etcd.EtcdClient;
import de.dfs.servicemonitor.etcd.EtcdClient.Request;
import de.dfs.servicemonitor.etcd.responsemodel.Converter;
import de.dfs.servicemonitor.etcd.responsemodel.Response;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.libs.F.Promise;
import play.libs.ws.WSClient;

@RunWith(MockitoJUnitRunner.class)
public class EtcdClientTestWithoutEmbeddedServer
{

  private static final String APPLICATION_JSON = "application/json";
  private static final String RESPONSE_BODY = "JSON";

  @Mock
  WSClient wsClient;

  @Mock
  Converter converter;
  private static final String BASE_URL = "http://test";

  @Mock
  WSRequest wsRequest;

  @Mock
  WSResponse wsResponse;

  @Mock
  Response response;


  @Test
  public void testSimpleGet() throws JsonParseException, JsonMappingException, IOException
  {
    setupWsClientToReturnMockedResponse();

    EtcdClient etcdClient = new EtcdClient(wsClient, BASE_URL, converter);

    Promise< Response > responsePromise = etcdClient.create().get("test");
    Response actualResponse = responsePromise.get(1000);
    assertApplicationJasionSetAsContentType();
    assertThatGetWasCalled();
    assertNoMoreQueryParamsSet();
    assertResponseIsMockedResponse(actualResponse);
  }


  @Test
  public void testWatch() throws JsonParseException, JsonMappingException, IOException
  {
    setupWsClientToReturnMockedResponse();

    EtcdClient etcdClient = new EtcdClient(wsClient, BASE_URL, converter);
    Request request = etcdClient.create();
    request.setWait();
   
    Promise< Response > responsePromise = request.get("test");
    Response actualResponse = responsePromise.get(1000);
    assertQueryParamWaitTrue();
    assertApplicationJasionSetAsContentType();
    assertThatGetWasCalled();
    assertNoMoreQueryParamsSet();
    assertResponseIsMockedResponse(actualResponse);
  }


  @Test
  public void testRecursiveWatch() throws JsonParseException, JsonMappingException, IOException
  {
    setupWsClientToReturnMockedResponse();

    EtcdClient etcdClient = new EtcdClient(wsClient, BASE_URL, converter);
    Request request = etcdClient.create();
    request.setRecursive();
    request.setWait();
    Promise< Response > responsePromise = request.get("test");
    Response actualResponse = responsePromise.get(1000);
    assertQueryParamRecursivTrue();
    assertQueryParamWaitTrue();
    assertApplicationJasionSetAsContentType();
    assertThatGetWasCalled();
    assertNoMoreQueryParamsSet();
    assertResponseIsMockedResponse(actualResponse);
  }


  private void assertQueryParamRecursivTrue()
  {
    verify(wsRequest).setQueryParameter("recursive", "true");
  }


  private void assertQueryParamWaitTrue()
  {
    verify(wsRequest).setQueryParameter("wait", "true");
  }


  private void assertResponseIsMockedResponse(Response actualResponse)
  {
    assertThat(actualResponse, equalTo(actualResponse));
  }


  private void assertNoMoreQueryParamsSet()
  {
    verifyNoMoreInteractions(wsRequest);
  }


  private void assertThatGetWasCalled()
  {
    verify(wsRequest).get();
  }


  private void assertApplicationJasionSetAsContentType()
  {
    verify(wsRequest).setContentType(APPLICATION_JSON);
  }


  private void setupWsClientToReturnMockedResponse() throws JsonParseException, JsonMappingException,
      IOException
  {
    when(wsResponse.getBody()).thenReturn(RESPONSE_BODY);
    Promise< WSResponse > wsResponsePromise = Promise.pure(wsResponse);
    when(wsRequest.get()).thenReturn(wsResponsePromise);
    when(wsRequest.setContentType(APPLICATION_JSON)).thenReturn(wsRequest);
    when(wsRequest.setQueryParameter(anyString(), anyString())).thenReturn(wsRequest);
    when(wsClient.url(anyString())).thenReturn(wsRequest);
    when(converter.fromJson(RESPONSE_BODY)).thenReturn(response);
  }
}
