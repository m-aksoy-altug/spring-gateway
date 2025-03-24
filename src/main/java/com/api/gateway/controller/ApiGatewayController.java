package com.api.gateway.controller;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.gateway.ApiGateway;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.micrometer.observation.annotation.Observed;
import jakarta.validation.constraints.NotEmpty;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;


@RestController
@Observed(name = "GatewayController")
@RequestMapping("/process")
public class ApiGatewayController {
	private static final Logger log = LoggerFactory.getLogger(ApiGatewayController.class);
	@Autowired
	RestTemplate restTemplate;
//	https://github.com/oldfr/spring_cloud_API_gateway_overview/blob/master/src/main/java/com/example/springcloudgatewayoverview
//	https://www.baeldung.com/postman-keycloak-endpoints
//	https://www.baeldung.com/spring-cloud-gateway-bff-oauth2
//	https://medium.com/@jancalve/integrating-spring-cloud-gateway-and-react-with-keycloak-using-token-relay-pattern-86f2a564fd16
//	"http://172.20.0.6:8080/realms/keycloak/protocol/openid-connect/auth"
	@PostMapping
	public String getType(@RequestBody Type type, ServerHttpRequest inRequest) {
		System.out.println("types:" + type.getTypes());
		ObjectMapper objectMapper = new ObjectMapper();
		ObjectNode emptyRequest = objectMapper.createObjectNode();
		HttpEntity<ObjectNode> request = new HttpEntity<>(emptyRequest);
		type.getTypes().forEach(f -> {
			if (f.equals("Student")) {
//				HttpEntity<ObjectNode> request = new HttpEntity<>(emptyRequest,
//						setAuthHeader(inRequest.getHeaders().get("userName").toString(),
//								inRequest.getHeaders().get("role").toString()));
				restTemplate.exchange("http://localhost:8080/first", HttpMethod.POST, request, String.class);
			}
			if (f.equals("Company")) {
				System.out.println("calling second microservice - company");
//				HttpEntity<Company> request = new HttpEntity<>(new Company(1, "Test", "Company"),
//						setAuthHeader(inRequest.getHeaders().get("userName").toString(),
//								inRequest.getHeaders().get("role").toString()));
//				HttpEntity<ObjectNode> request = new HttpEntity<>(emptyRequest,
//						setAuthHeader(inRequest.getHeaders().get("userName").toString(),
//								inRequest.getHeaders().get("role").toString()));
				restTemplate.exchange("http://localhost:8080/second", HttpMethod.POST, request, String.class);
			}
		});
		return "done";
	}

//	private HttpHeaders setAuthHeader(String userName, String role) {
//		HttpHeaders headers = new HttpHeaders();
//		headers.set("Authorization", "Bearer " + authUtil.getToken(userName, role,"http://localhost:8088/login"));
//		return headers;
//	}


	public class Type {

	    private List<String> types;

	    public Type() {

	    }

	    public Type(List<String> types) {
	        this.types = types;
	    }

	    public List<String> getTypes() {
	        return types;
	    }

	    public void setTypes(List<String> type) {
	        this.types = type;
	    }
	}	
}


//private final List<LoginOptionDto> loginOptions = new ArrayList<>();
//public GatewayController() {
//	this.addonsClientProperties = addonsProperties.getClient();
//	this.loginOptions = clientProps.getRegistration().entrySet().stream().
//			filter(e -> "authorization_code".equals(e.getValue().g
//					etAuthorizationGrantType()))
//			.map(
//					e -> new LoginOptionDto(
//							e.getValue().getProvider(),
//							"%s/oauth2/authorization/%s".
//formatted(addonsClientProperties.getClientUri(), e.getKey())))
//			.toList();
//}


//@GetMapping(path = "/login-options", produces = "application/json")
//public Mono<List<LoginOptionDto>> getLoginOptions(Authentication auth) throws URISyntaxException {
//	final boolean isAuthenticated = auth instanceof OAuth2AuthenticationToken;
//	System.out.println("getting type");
//	return Mono.just(isAuthenticated ? List.of() : this.loginOptions);
//}
//
//static record LoginOptionDto(@NotEmpty String label, @NotEmpty String loginUri) {
//}

