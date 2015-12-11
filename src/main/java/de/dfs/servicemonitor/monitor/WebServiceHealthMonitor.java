package de.dfs.servicemonitor.monitor;

import java.util.concurrent.TimeUnit;

import akka.actor.AbstractActor;
import akka.actor.ActorContext;
import akka.actor.ActorRef;
import akka.actor.Scheduler;
import akka.dispatch.Dispatcher;
import akka.japi.pf.ReceiveBuilder;
import play.libs.F.Promise;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import scala.concurrent.ExecutionContextExecutor;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

public class WebServiceHealthMonitor extends AbstractActor
{
  private WSClient wsclient;
  private ActorRef sender;
  private ActorRef self;


  public static class MonitorHealth
  {
    public String url;


    public MonitorHealth(String url)
    {
      this.url = url;
    }
  }

  public static class CheckIfStillAlive
  {
  }

  public static class Alive
  {
  }

  public static class Dead
  {
  }


  public WebServiceHealthMonitor(WSClient wsclient)
  {
    this.wsclient = wsclient;
    receive(ReceiveBuilder.match(MonitorHealth.class, this::monitorHealth)
        .build());
  }


  public void monitorHealth(MonitorHealth monitorHealth)
  {
    Alive alive = new Alive();
    Promise< WSResponse > wsResponse = wsclient.url(monitorHealth.url)
        .setRequestTimeout(100)
        .get();
    sender = sender();
    self = self();
    ActorContext context = context();
    Scheduler scheduler = context().system().scheduler();
    ExecutionContextExecutor dispatcher = context().system().dispatcher();
    wsResponse.onFailure(t -> sender.tell(new Dead(), self()));
    wsResponse.onRedeem(r -> {
      sender.tell(new Alive(), self());
      scheduler.scheduleOnce(FiniteDuration.create(500, TimeUnit.MILLISECONDS), self, context.dispatcher());
      
    });
  }
}
