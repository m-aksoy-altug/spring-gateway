package com.api.gateway.filter;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.api.gateway.client.RequestHelper;
import com.api.gateway.utl.Constants;
import com.api.gateway.utl.JwtUtil;
import com.api.gateway.validator.RouteValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.jsonwebtoken.Claims;
import reactor.core.publisher.Mono;

@Component
@RefreshScope
public class AuthFilter implements GatewayFilter {
	@Autowired
	private JwtUtil jwtUtil;
	@Autowired
	private RequestHelper requestHelper;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		ObjectMapper objectMapper = new ObjectMapper();
		ObjectNode responseNode = objectMapper.createObjectNode();
		ServerHttpRequest request = exchange.getRequest();
		if (!request.getHeaders().containsKey(Constants.CODE)) {
			return this.onError(exchange);
		}
		String code = request.getHeaders().getFirst(Constants.CODE);
		ResponseEntity<ObjectNode> response = requestHelper.requestToResourceServer(code);
		Map<String, String> newReponse=new HashMap<>();
		try {
			if(response.getStatusCode().is2xxSuccessful() && response.hasBody()){
				responseNode = response.getBody();
				Map<String, Object> map = objectMapper.convertValue((JsonNode)responseNode, Map.class);
				 // access_token // expires_in //refresh_expires_in // refresh_token  //token_type // not-before-policy // session_state // scope
				 	String[] chunks = map.get("access_token").toString().split("\\.");
			    	Base64.Decoder decoder = Base64.getUrlDecoder();
			    	String header = new String(decoder.decode(chunks[0]));
			    	String payload = new String(decoder.decode(chunks[1]));
			    	String signature = new String(decoder.decode(chunks[2]));
			    	System.out.println("payload "+payload);
			    	JsonNode jsonNodePayload = objectMapper.readTree(payload);
			    	newReponse.put("username", jsonNodePayload.get("preferred_username").asText());
			    	newReponse.put("email", jsonNodePayload.get("email").asText());
			}else {
				return this.onError(exchange);
			}
		}catch(Exception e) {
			return this.onError(exchange);
		}
		byte[]  responseObjects;
		try {
			responseObjects = new ObjectMapper().writeValueAsBytes(newReponse);
		} catch (JsonProcessingException e) {
			return this.onError(exchange);
		}
		
		HttpHeaders headersResponse = exchange.getResponse().getHeaders();
		headersResponse.add(Constants.CONTENT_TYPE,Constants.APPLICATION_JSON);
		return exchange.getResponse()
		        .writeWith(Mono.just(exchange.getResponse()
		        .bufferFactory().wrap(responseObjects)));
	}
	
	
	
	private Mono<Void> onError(ServerWebExchange exchange) {
		ServerHttpResponse responseError = exchange.getResponse();
		responseError.setStatusCode(HttpStatus.UNAUTHORIZED);
		return responseError.setComplete();
	}

	private String getAuthHeader(ServerHttpRequest request) {
		return request.getHeaders().getOrEmpty("Authorization").get(0);
	}

//	private boolean isCredsMissing(ServerHttpRequest request) {
//		return !(request.getHeaders().containsKey("userName") && request.getHeaders().containsKey("role"))
//				&& !request.getHeaders().containsKey("Authorization");
//	}

//	private void populateRequestWithHeaders(ServerWebExchange exchange, String token) {
//		Claims claims = jwtUtil.getALlClaims(token);
//		exchange.getRequest().mutate().header("id", String.valueOf(claims.get("id")))
//				.header("role", String.valueOf(claims.get("role"))).build();
//	}
}