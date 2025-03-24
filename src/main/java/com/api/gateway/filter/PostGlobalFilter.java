package com.api.gateway.filter;

import com.api.gateway.client.RequestHelper;
import com.api.gateway.validator.RouteValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class PostGlobalFilter implements WebFilter {
	
	@Autowired
	RouteValidator routeValidator;
	@Autowired
	RequestHelper requestHelper;
	
	@Value("${ui-uri}")
	private String uiUri;
	
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		String path = exchange.getRequest().getPath().toString();
		ServerHttpResponse response = exchange.getResponse();
		ServerHttpRequest request = exchange.getRequest();
		HttpHeaders headersRequest = exchange.getRequest().getHeaders();
		HttpMethod methodRequest = exchange.getRequest().getMethod();
		MultiValueMap<String, HttpCookie> cookies = exchange.getRequest().getCookies();
		System.out.println("MultiValueMap<String, HttpCookie> cookies::"+cookies);
		System.out.println("exchange.getRequest().getPath().toString::"+path);
		System.out.println("exchange.getRequest().exchange.getSession::"+exchange.getSession());
		System.out.println("exchange.getRequest().getHeaders::"+headersRequest);
		System.out.println("exchange.getRequest().toString()"+methodRequest);
		System.out.println("exchange.getRequest().getBody()"+exchange.getRequest().getBody());
		System.out.println("exchange.getResponse().getHeaders::"+response.getHeaders());
		System.out.println("exchange.getResponse().toString()"+response.toString());
		
		if(methodRequest.equals(HttpMethod.OPTIONS) && 
				headersRequest.containsKey("Origin") &&  
				headersRequest.getFirst("Origin").startsWith("http://172.20.0.5")) {
			System.out.println("SETTING RESPONSE HERE:::"+headersRequest.getFirst("Origin"));
			HttpHeaders headersResponse = response.getHeaders();
			headersResponse.add("Access-Control-Allow-Methods","POST, GET, OPTIONS, DELETE");
			headersResponse.add("Access-Control-Allow-Origin", headersRequest.getFirst("Origin"));
			headersResponse.add("Access-Control-Allow-Headers",headersRequest.getFirst("Access-Control-Request-Headers"));
//			headersResponse.add("Access-Control-Max-Age","*");
			response.setStatusCode(HttpStatus.NO_CONTENT);
			chain.filter(exchange.mutate().response(response).build());
			return response.setComplete();
		}
		
		if(methodRequest.equals(HttpMethod.POST) && 
				headersRequest.containsKey("Origin") &&  
				headersRequest.getFirst("Origin").startsWith("http://172.20.0.5")) {
			System.out.println("SETTING POST HEADERS HERE:::"+headersRequest.getFirst("Origin"));
			HttpHeaders headersResponse = response.getHeaders();
			headersResponse.add("Access-Control-Allow-Methods","POST, GET, OPTIONS, DELETE");
			headersResponse.add("Access-Control-Allow-Origin", headersRequest.getFirst("Origin"));
			headersResponse.add("Access-Control-Allow-Headers",headersRequest.getFirst("Access-Control-Request-Headers"));
//			headersResponse.add("Access-Control-Max-Age","*");
			System.out.println("PASSSING THROUGH ROUTE ALLOCOTER NOW>>");
		}
		if (routeValidator.isSecured.test(exchange.getRequest()) 
				|| !headersRequest.containsKey("Code")) {
			response.setStatusCode(HttpStatus.UNAUTHORIZED);
			chain.filter(exchange.mutate().response(response).build());
			return response.setComplete();
		}
//		DataBufferFactory dataBufferFactory = response.bufferFactory();
//		ServerHttpResponseDecorator decoratedResponse = getDecoratedResponse(path, response, request,
//				dataBufferFactory);
//		return chain.filter(exchange.mutate().response(decoratedResponse).build());
		return chain.filter(exchange);
	}
	
	
	private ServerHttpResponseDecorator getDecoratedResponse(String path, ServerHttpResponse response,
			ServerHttpRequest request, DataBufferFactory dataBufferFactory) {
		return new ServerHttpResponseDecorator(response) {
			@Override
			public Mono<Void> writeWith(final Publisher<? extends DataBuffer> body) {
				if (body instanceof Flux) {
					Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;
					return super.writeWith(fluxBody.buffer().map(dataBuffers -> {
						DefaultDataBuffer joinedBuffers = new DefaultDataBufferFactory().join(dataBuffers);
						byte[] content = new byte[joinedBuffers.readableByteCount()];
						joinedBuffers.read(content);
						// MODIFY RESPONSE and Return the Modified response
						String responseBody = new String(content, StandardCharsets.UTF_8);
						System.out.println("requestId: " + request.getId() + ", method: " + request.getMethod()
								+ ", req url: " + request.getURI() + ", response body :" + responseBody);
//						try {
							if (request.getURI().getPath().equals("/first") && request.getMethod().equals("GET")) {
//								List<Student> student = new ObjectMapper().readValue(responseBody, List.class);
								System.out.println("student:"); //+ student);
							} else if (request.getURI().getPath().equals("/second")
									&& request.getMethod().equals("GET")) {
//									&& request.getMethodValue().equals("GET")) {
//								List<Company> companies = new ObjectMapper().readValue(responseBody, List.class);
								System.out.println("companies:");// + companies);
							}
//						} catch (JsonProcessingException e) {
//							throw new RuntimeException(e);
//						}
						return dataBufferFactory.wrap(responseBody.getBytes());
					})).onErrorResume(err -> {
						System.out.println("error while decorating Response: {}" + err.getMessage());
						return Mono.empty();
					});
				}
				return super.writeWith(body);
			}
		};
	}

}