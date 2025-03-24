package com.api.gateway.ssl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

@Component
public class SecureCredentialServiceClient {
	// add logic here for reading from Json
	public SslDetails populateSslData(final JSONObject json) {

		SslDetails sslDetails = new SslDetails();
		List<String> ca = new ArrayList<String> ();
		String cert=null;
		String key=null;
		cert= json.getString("cert");
		key= json.getString("key");
		JSONArray sslCaArray = json.getJSONArray("ca");
		if(sslCaArray!=null) {
			Iterator<Object> caItr= sslCaArray.iterator();
			while(caItr.hasNext()) {
				ca.add((String) caItr.next());
			}
		}
		sslDetails.setCert(cert);
		sslDetails.setKey(key);
		return sslDetails;
	} 
	
	public String getCertificateDetails(final String key) {
		if(key.equals("keyStorePassword")) {
			return "changeit";
		}
		return "";
	}
}
