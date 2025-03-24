package com.api.gateway.filter;

import java.net.http.HttpResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.api.gateway.ApiGateway;
import com.api.gateway.validator.RouteValidator;

import reactor.core.publisher.Mono;

@Component
public class RequestFilter implements GatewayFilter {
	private static final Logger log= LoggerFactory.getLogger(RequestFilter.class);
	
	@Autowired
	RouteValidator routeValidator;
	
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//    	HttpHeaders headers = exchange.getRequest().getHeaders();   	
//    	Object body = exchange.getAttribute("cachedRequestBodyObject");
//    	if (routeValidator.isSecured.test(exchange.getRequest()) || !headers.containsKey("code")) {
//			ServerHttpResponse response= exchange.getResponse();
//			response.setStatusCode(HttpStatus.UNAUTHORIZED);
//			return response.setComplete();
//		}
        return chain.filter(exchange);
    }
}