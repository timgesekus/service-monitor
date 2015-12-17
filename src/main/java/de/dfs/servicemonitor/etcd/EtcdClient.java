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

	public Connection createConnection() {
		return new Connection(wsClient, baseUrl, converter);
	}

	public static class Connection {

		private WSClient wsClient;
		private String baseUrl;
		private Converter converter;
		private boolean isRecursive;
		private boolean isWait;
		private int ttl;

		private Connection(WSClient wsClient, String baseUrl, Converter converter) {
			this.wsClient = wsClient;
			this.baseUrl = baseUrl;
			this.converter = converter;
			ttl = 0;
			isRecursive = false;

		}

		public Promise<Response> get(String key) {
			WSRequest wsRequest = createRequest(key);
			Promise<WSResponse> responseAsJson = wsRequest.get();

			Promise<Response> etcdResponse = responseAsJson.map(response -> {
				return converter.fromJson(response.getBody());
			});
			return etcdResponse;

		}

		private WSRequest createRequest(String key) {
			String getUrl = baseUrl + "keys/" + key;
			WSRequest wsRequest = wsClient.url(getUrl);
			if (isRecursive) {
				wsRequest = wsRequest.setQueryParameter("recursive", "true");
			}
			if (isWait) {
				wsRequest = wsRequest.setQueryParameter("wait", "true");
			}
			wsRequest = wsRequest.setContentType("application/json");
			return wsRequest;
		}

		public void setRecursive() {
			isRecursive = true;
		}

		public void setWait() {
			isWait = true;
		}

		public void setTTL(int ttl) {
			this.ttl = ttl;
		}

		public Promise<Response> set(String key, String value) {
			WSRequest wsRequest = createRequest(key);
			String postData = key+ "=" + value;
			if (ttl > 0) {
				postData = postData + "?ttl=" + ttl;
			}
			Promise<WSResponse> responseAsJson = wsRequest.post(postData);

			Promise<Response> etcdResponse = responseAsJson.map(response -> {
				return converter.fromJson(response.getBody());
			});
			return etcdResponse;

		}
	}

}
