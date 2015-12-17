package de.dfs.servicemonitor.monitor;

import java.util.Optional;

import play.libs.F.Promise;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;

public class ServicePinger {

  /**
   * Poor mans either. *sigh*
   */
  public static class PingResult {
    private Optional<WSResponse> wsResponseOp;
    private Optional<Throwable> errorOp;

    @SuppressWarnings("unused")
    private PingResult() {
    }

    public PingResult(Throwable error) {
      errorOp = Optional.of(error);
    }

    public PingResult(WSResponse wsResponse) {
      wsResponseOp = Optional.of(wsResponse);
    }

    public boolean isError() {
      return errorOp.isPresent();
    }

    public Throwable getError() {
      return errorOp.get();
    }

    public WSResponse getResult() {
      return wsResponseOp.get();
    }

  }

  private final String url;
  private final WSClient wsClient;

  public ServicePinger(String url, WSClient wsClient) {
    this.wsClient = wsClient;
    this.url = url;
  }

  public PingResult ping(java.time.Duration duration) {
    try {
      WSRequest wsRequest = wsClient.url(url).setRequestTimeout(duration.toMillis());
      Promise<WSResponse> promise = wsRequest.get();
      WSResponse wsResponse = promise.get(duration.toMillis());
      return new PingResult(wsResponse);
    } catch (Throwable t) {
      return new PingResult(t);
    }
  }

}
