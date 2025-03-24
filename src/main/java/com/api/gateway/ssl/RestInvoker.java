package com.api.gateway.ssl;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


@Component
public class RestInvoker {
	private static final Logger log = LoggerFactory.getLogger(RestInvoker.class);
	private static final ObjectMapper MAPPER= new ObjectMapper();
	
	private RestOperations restOperations;
	
	public  RestOperations getRestOperations() {
		return restOperations;
	}
	
	public void setRestOperations(RestOperations restOperations) {
		this.restOperations = restOperations;
	}
	
	// Perform specified HTTP Method on give URL and payload
	public <T> ResponseEntity<T> performRestOperations(final String url,
			final HttpMethod method,
			final HttpEntity<?> requestEntity,
			final Class<T> responseType) throws RestClientException, URISyntaxException{
	
		log.info("Response entity method parameters:: "+url+" "+method+" "+requestEntity+" "+responseType);
		long start= System.currentTimeMillis();
		String correlationId= StringUtils.EMPTY;		
		String requestId= StringUtils.EMPTY;		
		
		if(null !=requestEntity && requestEntity.hasBody()) {
			try {
				JsonNode node =MAPPER.convertValue(requestEntity.getBody(), JsonNode.class);
				if(null !=node) {
					if(null !=node.get("correlationId")) {
						correlationId = node.get("correlationId").asText();
					}
					if(null !=node.get("requestId")) {
						requestId = node.get("requestId").asText(StringUtils.EMPTY);
					}
				}
				
			}catch(Throwable e) {
				log.error("POST PAYLOAD TO", requestEntity.getBody().toString(), e);				
			}
		}
	
		URI uri = new URI(url);
		ResponseEntity<T> responseEntity = restOperations.exchange(uri, method, requestEntity, responseType); 
		if(null !=responseEntity) {
			log.info("ResponseEntity : "+responseEntity);	
			try {
				log.info("Rest Operation Response status code : "+responseEntity.getStatusCode());	
				log.info("Rest Operation Response body : "+responseEntity.getBody());
			}catch(Throwable e) {
				log.error("POST PAYLOAD TO", requestEntity.getBody().toString(), e);			
			}
		}
		return responseEntity;
	}
	
	
	
	
}

