package de.dfs.servicemonitor.etcd;

import de.dfs.servicemonitor.etcd.responsemodel.Converter;
import de.dfs.servicemonitor.etcd.responsemodel.Response;
import play.libs.F.Promise;
import play.libs.ws.WS;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;

public class EtcdClient {
  private WSClient wsClient;
  private String baseUrl;
  private Converter converter;

  public EtcdClient(WSClient wsClient, String baseUrl, Converter converter) {
    this.converter = converter;
    this.baseUrl = baseUrl + "/v2/";
    this.wsClient = wsClient;
  }

  public Request create() {
    return new Request(wsClient, baseUrl, converter);
  }

  public static class Request {

    private WSClient wsClient;
    private String baseUrl;
    private Converter converter;
    private boolean isRecursive;
    private boolean isWait;

    private Request(WSClient wsClient, String baseUrl, Converter converter) {
      this.wsClient = wsClient;
      this.baseUrl = baseUrl;
      this.converter = converter;
      
      isRecursive = false;

    }


    public Promise<Response> get(String key) {
      String getUrl = baseUrl + "keys/" + key;
      WSRequest wsRequest = wsClient.url(getUrl);
      if (isRecursive) {
        wsRequest = wsRequest.setQueryParameter("recursive", "true");
      }
      if (isWait) {
        wsRequest = wsRequest.setQueryParameter("wait", "true");
      }
      wsRequest = wsRequest.setContentType("application/json");
      Promise<WSResponse> responseAsJson = wsRequest.get();

      Promise<Response> etcdResponse = responseAsJson.map(response -> {
        return converter.fromJson(response.getBody());
      });
      return etcdResponse;

    }

    public void setRecursive() {
      isRecursive = true;
    }
    
    public void setWait() {
     isWait = true; 
    }
  }

}
