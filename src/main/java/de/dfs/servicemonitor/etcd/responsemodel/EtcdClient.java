package de.dfs.servicemonitor.etcd.responsemodel;

import play.libs.F.Promise;
import play.libs.ws.WS;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;

public class EtcdClient
{
  private WSClient wsClient;
  private String baseUrl;
  private Converter converter;


  public EtcdClient(WSClient wsClient, String baseUrl, Converter converter)
  {
    this.converter = converter;
    this.baseUrl = baseUrl + "/v2/";
    this.wsClient = wsClient;
  }


  public Promise< Response > get(String key)
  {
    return get(key, false, false);
  }


  public Promise< Response > watch(String key)
  {
    return get(key, false, true);
  }


  public Promise< Response > watchRecursive(String key)
  {
    return get(key, true, true);
  }


  private Promise< Response > get(String key, boolean recursive, boolean wait)
  {
    String getUrl = baseUrl + "keys/" + key;
    WSRequest wsRequest = wsClient.url(getUrl);
    if (recursive)
    {
      wsRequest = wsRequest.setQueryParameter("recursive", "true");
    }
    if (wait)
    {
      wsRequest = wsRequest.setQueryParameter("wait", "true");
    }
    wsRequest = wsRequest.setContentType("application/json");
    Promise< WSResponse > responseAsJson = wsRequest.get();

    Promise< Response > etcdResponse = responseAsJson.map(response -> {
      return converter.fromJson(response.getBody());
    });
    return etcdResponse;

  }

}
