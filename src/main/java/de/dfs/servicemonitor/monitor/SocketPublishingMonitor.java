package de.dfs.servicemonitor.monitor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import de.dfs.servicemonitor.etcd.EtcdClient;
import de.dfs.servicemonitor.etcd.EtcdClient.Request;
import de.dfs.servicemonitor.etcd.responsemodel.Response;
import play.libs.F.Promise;

public class SocketPublishingMonitor extends AbstractActor {
  private EtcdClient etcdClient;

  private final LoggingAdapter log = Logging.getLogger(context().system(), this);

  private ActorRef sender;

  private ActorRef self;

  public static class WaitForSocketPublishing {
    public String simulatorKey;

    public WaitForSocketPublishing(String simulatorKey) {
      this.simulatorKey = simulatorKey;
    }
  }

  public static class WaitForSocketFailed {
    public String message;
    public Throwable t;

    public WaitForSocketFailed(String message, Throwable t) {
      this.message = message;
      this.t = t;
    }
  }

  public static class SocketPublished {
    public String socket;

    public SocketPublished(String socket) {
      this.socket = socket;
    }
  }

  public SocketPublishingMonitor(EtcdClient etcdClient) {
    this.etcdClient = etcdClient;
    receive(ReceiveBuilder.match(WaitForSocketPublishing.class, this::handleMonitor)
        .matchAny(o -> log.info("Received unkown message " + o)).build());
  }

  public void handleMonitor(WaitForSocketPublishing waForSocketPublishing) {
    Request request = etcdClient.create();
    request.setWait();
    Promise<Response> promise = request.get(waForSocketPublishing.simulatorKey + "/" + "socket");
    sender = sender();
    self = self();

    promise.onRedeem(this::sendSocketPublished);
    promise.onFailure(this::sendError);
  }

  private void sendSocketPublished(Response response) {
    String socket = response.node.value;
    SocketPublished socketPublished = new SocketPublished(socket);
    sender.tell(socketPublished, self);
  }

  private void sendError(Throwable t) {
    WaitForSocketFailed waitForSocketFailed = new WaitForSocketFailed(t.getMessage(), t);
    sender.tell(waitForSocketFailed, self);
  }
}
