package com.api.gateway.ssl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SslInvokerProperties {
	
	@Value("${ssl.timeToLiveMilliseconds:3000}")
	private long timeTolive;
	
	@Value("${ssl.maxConnectionsTotal:200}")
	private int maxConnectionsTotal;
	
	@Value("${ssl.maxConnectionsPerRoute:200}")
	private int maxConnectionsPerRoute;
	
	@Value("${ssl.supportedProtocols:}")
	private String supportedProtocols;

	public long getTimeTolive() {
		return timeTolive;
	}

	public void setTimeTolive(long timeTolive) {
		this.timeTolive = timeTolive;
	}

	public int getMaxConnectionsTotal() {
		return maxConnectionsTotal;
	}

	public void setMaxConnectionsTotal(int maxConnectionsTotal) {
		this.maxConnectionsTotal = maxConnectionsTotal;
	}

	public int getMaxConnectionsPerRoute() {
		return maxConnectionsPerRoute;
	}

	public void setMaxConnectionsPerRoute(int maxConnectionsPerRoute) {
		this.maxConnectionsPerRoute = maxConnectionsPerRoute;
	}

	public String getSupportedProtocols() {
		return supportedProtocols;
	}

	public void setSupportedProtocols(String supportedProtocols) {
		this.supportedProtocols = supportedProtocols;
	}
	
}
