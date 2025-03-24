package com.api.gateway.ssl;

import java.util.List;

public class SslDetails {
	private List<String> ca;
	private String cert;
	private String key;
	
	public List<String> getCA() {
		return ca;
	}
	public void setCa(final List<String> ca) {
		this.ca = ca;
	}
	public String getCert() {
		return cert;
	}
	public void setCert(final String cert) {
		this.cert = cert;
	}
	public String getKey() {
		return key;
	}
	public void setKey(final String key) {
		this.key = key;
	}
	
}
