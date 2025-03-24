
package com.api.gateway.ssl;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SslContextBuilder {
	private static final Logger log = LoggerFactory.getLogger(SslContextBuilder.class);
	
	private static final String SSL_CONTEXT_TLS = "TLS";

	@Autowired
	private SecureCredentialServiceClient secureCredentialServiceClient; // SslDetails
	
	@Autowired
	CertificateConverter certificateConverter;
	
	private String keystorePassword = "";
	private String keyPassword = "";
	

	public SSLContext build(final String sslAPI) {
		SslDetails sslDetails = new SslDetails();
		// I reckon there two passwords must be same or keystorePassword can be empty
		keystorePassword = secureCredentialServiceClient.getCertificateDetails("keyStorePassword"); //"cert_Keystore_password");
		keyPassword = secureCredentialServiceClient.getCertificateDetails("keyStorePassword");
		sslDetails = secureCredentialServiceClient.populateSslData(null);
		List<String> calist = new ArrayList<>();
		calist.add("aaa");
		sslDetails.setCa(calist);
		sslDetails.setCert(" ");
		sslDetails.setKey(" ");

		KeyStore trustStore;
		try {
			trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			if (keystorePassword != null) {
				trustStore.load(null, keystorePassword.toCharArray());
			}
		} catch (Exception e) {
			throw new RuntimeException("Exception occured creating the empty trust key store", e);
		}

		KeyStore keyStore;
		try {
			keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			if (keystorePassword != null) {
				keyStore.load(null, keystorePassword.toCharArray());
			}
		} catch (Exception e) {
			throw new RuntimeException("Exception occured creating the empty trust key store", e);
		}
		
		// Load the certificates AUthority certificates into the key store.
		CertificateConverter.loadCertificateAuthorities(sslDetails,trustStore);
		
		// Load the my public certificate and key into the key store
		certificateConverter.loadCertificateAndKey(sslDetails, keyStore);
		
		// Build trust manager
		log.debug("Building the trust store");
		TrustManagerFactory tmf;
		try {
			tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(trustStore);
		}catch(Exception e) {
			throw new RuntimeException("Exception occured creating trust manager factory", e);
		
		}
		
		// Build the key manager
		KeyManagerFactory kmf;
		try {
			kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			if(keyPassword!=null) {
				kmf.init(keyStore,keyPassword.toCharArray());
			}
			
		}catch(Exception e) {
			throw new RuntimeException("Exception occured creating trust manager factory", e);
		
		}
		
		// Build SSL Context
		SSLContext sslContext;
		try {
			sslContext = SSLContext.getInstance(SSL_CONTEXT_TLS);
			sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
		}catch(Exception e) {
			throw new RuntimeException("Exception occured creating SSLContext", e);
		}
	
		return sslContext;
	}

}
