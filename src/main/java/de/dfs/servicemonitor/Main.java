package de.dfs.servicemonitor;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import de.dfs.servicemonitor.etcd.EtcdClient;
import de.dfs.servicemonitor.etcd.responsemodel.Converter;
import de.dfs.servicemonitor.monitor.ServiceMonitor;
import de.dfs.servicemonitor.monitor.ServiceMonitorConfig;
import play.libs.ws.WSClient;

public class Main {

	public static void main(String[] args) {
		Config config = ConfigFactory.load();
		ServiceMonitorConfig serviceMonitorConfig = new ServiceMonitorConfig(config);
		//WSClient wsClient=;
		//Converter converter = new Converter();
	//	EtcdClient etcdClient = new EtcdClient(wsClient, baseUrl, converter);
	//	new ServiceMonitor(etcdClient, wsClient, url, sessionId, serviceMonitorConfig);
	}

}
