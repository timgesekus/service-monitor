package de.dfs.servicemonitor.monitor;

import static akka.pattern.Patterns.pipe;

import java.util.Optional;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import play.libs.F.Promise;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

public class WebServiceHealthMonitor extends AbstractActor {
	private static final int MAX_RETRIES = 5;
	private WSClient wsclient;
	private String url;
	private int retries;
	private final LoggingAdapter log = Logging.getLogger(context().system(), this);
	private ActorRef initialSender;

	public static class MonitorHealth {
		public String url;

		public MonitorHealth(String url) {
			this.url = url;
		}
	}

	public static class CheckIfStillAlive {
	}

	public static class Alive {
	}

	public static class Dead {
	}

	private static class AliveCheckResult {
		private final boolean isAlive;
		private Optional<WSResponse> wsResponseOp;
		private Optional<Throwable> errorOp;

		public AliveCheckResult(WSResponse wsResponse) {
			isAlive = true;
			wsResponseOp = Optional.of(wsResponse);
		}

		public AliveCheckResult(Throwable t) {
			isAlive = false;
			errorOp = Optional.of(t);
		}
	}

	private static class TryConnect {
	}

	public WebServiceHealthMonitor(WSClient wsclient) {
		this.wsclient = wsclient;
		receive(waitForMonitorHealthCommand());
	}

	public PartialFunction<Object, BoxedUnit> waitForMonitorHealthCommand() {
		return ReceiveBuilder.match(MonitorHealth.class, this::hanldeMonitorHealth).matchAny(this::handleUnkownMessage)
				.build();
	}

	public PartialFunction<Object, BoxedUnit> tryInitalConnect() {
		return ReceiveBuilder.match(TryConnect.class, this::handleTryToConnect)
				.match(AliveCheckResult.class, this::handleInitialAliveCheck).matchAny(this::handleUnkownMessage)
				.build();

	}

	public PartialFunction<Object, BoxedUnit> monitorService() {
		return ReceiveBuilder.match(TryConnect.class, this::handleTryToConnect)
				.match(AliveCheckResult.class, this::handleMonitoringAliveCheck).matchAny(this::handleUnkownMessage)
				.build();

	}

	private void handleUnkownMessage(Object m) {
		log.error("Unhandled message {}", m);
		unhandled(m);
	}

	public void hanldeMonitorHealth(MonitorHealth monitorHealth) {
		retries = 0;
		url = monitorHealth.url;
		initialSender = sender();
		context().become(tryInitalConnect());
		tryToConnect();
	}

	private void handleInitialAliveCheck(AliveCheckResult aliveCheckResult) {
		if (aliveCheckResult.isAlive) {
			initialSender.tell(new Alive(), self());
			context().become(monitorService());
			tryToConnect();
		} else {
			if (retries < MAX_RETRIES) {
				tryToConnect();
			} else {
				initialSender.tell(new Dead(), self());
				log.info("Could not reach client", aliveCheckResult.errorOp);
			}
		}

	}

	private void handleMonitoringAliveCheck(AliveCheckResult aliveCheckResult) {
		if (aliveCheckResult.isAlive) {
			initialSender.tell(new Alive(), self());
			tryToConnect();
		} else {
			initialSender.tell(new Dead(), self());
			log.info("Could not reach client", aliveCheckResult.errorOp);
		}
	}

	private void tryToConnect() {
		self().tell(new TryConnect(), self());
	}

	private void handleTryToConnect(TryConnect tryConnect) {
		Promise<WSResponse> wsResponse = wsclient.url(url).setRequestTimeout(100).get();
		Promise<AliveCheckResult> goodResult = wsResponse.map(w -> new AliveCheckResult(w));
		Promise<AliveCheckResult> errorResult = goodResult.recover(t -> new AliveCheckResult(t));
		pipe(errorResult.wrapped(), context().dispatcher()).to(self());
	}
}
