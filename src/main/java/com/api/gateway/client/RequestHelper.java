package com.api.gateway.client;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.api.gateway.config.GatewayConfig;
import com.api.gateway.ssl.RestInvoker;
import com.api.gateway.ssl.RestTemplateFactory;

@Component
public class RequestHelper {
	private static final Logger log = LoggerFactory.getLogger(RequestHelper.class);
	
	@Autowired
	RestTemplate restTemplate;
	@Autowired
	RestTemplateFactory restTemplateFactory; 
	@Autowired
	RestInvoker restInvoker; 
	
	@Value("${oauth2-uri}")
	private String oauth2Uri;
	@Value("${oauth2-client-redirect-uri}")
	private String oauth2ClientRedirectUri;
	@Value("${oauth2-client-id}")
	private String oauth2ClientId;
	@Value("${oauth2-client-secret}")
	private String oauth2ClientSecret;

	
//	http://172.20.0.6:8080/realms/keycloak/.well-known/openid-configuration

	public ResponseEntity<ObjectNode> requestToResourceServer(final String code) {
		// implement SSL connection 
		// https://medium.com/coderbyte/using-resttemplate-with-client-certificates-a25feb2d9918
		final URI uri =UriComponentsBuilder.fromHttpUrl(oauth2Uri).build().encode().toUri();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("client_id", oauth2ClientId);
		map.add("client_secret", oauth2ClientSecret);
		map.add("scope", "openid");
		map.add("grant_type", "authorization_code");
		map.add("code", code);
		map.add("redirect_uri",oauth2ClientRedirectUri);
		HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(map, headers);
		System.out.println("URL"+uri);
		System.out.println("\n\n\nrequest"+httpEntity);
		
//		// this is where SSL starts
		ResponseEntity<ObjectNode> response =null;
		try {
			restInvoker.setRestOperations(restTemplateFactory.buildRestTemplate("sslDetails"));
			response = restInvoker.performRestOperations(uri.toString(), HttpMethod.POST, httpEntity, ObjectNode.class);
		} catch (RestClientException | URISyntaxException e) {
			e.printStackTrace();
			log.error("Error", httpEntity, e);
		}
		
//		ResponseEntity<ObjectNode> response = 
//				restTemplate.exchange(uri, HttpMethod.POST, httpEntity, ObjectNode.class);
		System.out.println("\n\n\nresponse"+response);
		return response;
	}
}
