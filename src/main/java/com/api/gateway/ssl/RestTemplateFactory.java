package com.api.gateway.ssl;



import javax.net.ssl.SSLContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.springframework.web.client.RestTemplate;




@Component
public class RestTemplateFactory {
	
	private SslContextBuilder sslContextBuilder ;
	private SslInvokerProperties sslInvokerProperties;
	
	@Autowired
	public RestTemplateFactory(final SslContextBuilder sslContextBuilder,
								final  SslInvokerProperties sslInvokerProperties) {
		this.sslContextBuilder=sslContextBuilder;
		this.sslInvokerProperties=sslInvokerProperties;
	}
	public RestTemplate buildRestTemplate(final String sslAPI) { // sslAPI= sslDetails
		RestTemplate restTemplate= new RestTemplate();
		SSLContext SSLContext = sslContextBuilder.build(sslAPI);
		
		String[] supportedProtocols = new String[] {"TLSv1","TLSv1.1","TLSv1.2"};
		if(StringUtils.isNotBlank(sslInvokerProperties.getSupportedProtocols())) {
			supportedProtocols = StringUtils.split(sslInvokerProperties.getSupportedProtocols(),',');
		}
		
		SSLConnectionSocketFactory sslsf= new SSLConnectionSocketFactory(
				SSLContext,
				supportedProtocols,
				null,
				null);
		
		HttpClientConnectionManager cm = buildConnectionManager(sslsf);
		
		return restTemplate;
	}
	
	public HttpClientConnectionManager buildConnectionManager(final SSLConnectionSocketFactory sslsf) {	
		Registry<ConnectionSocketFactory> connFactoryRegistry =null;
		if(sslsf!=null) {
			connFactoryRegistry= RegistryBuilder.<ConnectionSocketFactory>create()
					.register("http", PlainConnectionSocketFactory.getSocketFactory())
					.register("https", sslsf)
					.build();
		}else {
			connFactoryRegistry= RegistryBuilder.<ConnectionSocketFactory>create()
					.register("http", PlainConnectionSocketFactory.getSocketFactory())
					.build();
		}
		
		final PoolingHttpClientConnectionManager poolingmgr= 
				new PoolingHttpClientConnectionManager(
						connFactoryRegistry,null,null,null,null);
		if(sslInvokerProperties.getMaxConnectionsTotal()> 0) {
			poolingmgr.setMaxTotal(sslInvokerProperties.getMaxConnectionsTotal());
		}
		
		if(sslInvokerProperties.getMaxConnectionsPerRoute()> 0) {
			poolingmgr.setDefaultMaxPerRoute(sslInvokerProperties.getMaxConnectionsPerRoute());
		}
		
		return poolingmgr;
	}
}
