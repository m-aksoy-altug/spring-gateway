package com.api.gateway.validator;

import java.util.List;
import java.util.function.Predicate;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class RouteValidator {
	public static final List<String> unprotectedURLs = List.of("/bff/login","/bff");

	public Predicate<ServerHttpRequest> isSecured = 
			request -> unprotectedURLs.stream()
			.noneMatch(uri -> request.getURI().getPath().contains(uri));
}