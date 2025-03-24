package com.api.gateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.WebFilter;

import com.api.gateway.filter.AuthFilter;
import com.api.gateway.filter.PostGlobalFilter;
import com.api.gateway.filter.RequestFilter;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
//import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
//import org.springframework.security.config.web.server.ServerHttpSecurity;
//import org.springframework.security.web.server.SecurityWebFilterChain;
//import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;

@Configuration
//@EnableWebFluxSecurity
public class GatewayConfig {
	private static final Logger log = LoggerFactory.getLogger(GatewayConfig.class);
	@Autowired
	RequestFilter requestFilter;
	@Autowired
	AuthFilter authFilter;

	@Bean
	public RestTemplate getRestTemplate() {
		return new RestTemplate();
	}

	@Bean
	public WebFilter responseFilter() {
		return new PostGlobalFilter();
	}

	@Value("${ui-uri}")
	private String uiUri;
	@Value("${oauth2-uri}")
	private String oauth2Uri;

	@Bean
	public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
		return builder.routes()
				.route("home",
						r -> r.path("/bff").and().method("POST").and().readBody(Object.class, s -> true)
								.filters(f -> f.filters(requestFilter)).uri(uiUri))
				.route("auth-server", r -> r.path("/bff/login").and()
						.method("POST").filters(f -> f.filters(authFilter))
						.uri(uiUri + "/bff/login"))
				.build();
	}

	@Bean
	public CorsWebFilter corsFilter() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(Arrays.asList(uiUri));
		config.setAllowedHeaders(List.of("*"));
		config.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
		config.setAllowCredentials(true);
		config.setExposedHeaders(List.of("Authorization", "Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"));
		config.setMaxAge(3600L); // Cache preflight response for an hour
		log.info("CORS Configuration Applied: Origins={}, Headers={}, Methods={}, Exposed={}",
				config.getAllowedOrigins(), config.getAllowedHeaders(), config.getAllowedMethods(),
				config.getExposedHeaders());
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return new CorsWebFilter(source);
	}
	

//	@Bean
//	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
//		 return http
////		            .cors().and()
//		            .csrf().disable() // Disable CSRF for simplicity
//		            .authorizeExchange()
//		            .pathMatchers("/**").permitAll() // Allow all requests
//		            .and()
//		            .build();	
//		 }
	
	
//	 @Bean
//	 public SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity serverHttpSecurity) {
//	        return serverHttpSecurity
//	                .csrf(ServerHttpSecurity.CsrfSpec::disable)
//	                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
//	                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
//	                .authorizeExchange(exchanges -> exchanges
//	                        .pathMatchers("/**")
//	                        .permitAll()
//	                        .anyExchange()
//	                        .authenticated()
//	                )
//	                .build();
//	    }

	
//	@Bean
//  public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
//      http
//          .cors() // Enable CORS
//          .and()
//          .csrf()
//          .csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse()) // Store CSRF token in a cookie
//          .and()
//          .authorizeExchange()
//          .pathMatchers("/api/public/**").permitAll() // Ignore CORS and CSRF for public APIs
//          .pathMatchers("/api/test").permitAll() // Allow public access to /api/test
//          .anyExchange().authenticated(); // Require authentnication for other requests
//      // https://medium.com/@mbanaee61/implementing-cors-and-csrf-security-in-spring-boot-webflux-applications-cd8ca162d97a
//      return http.build();
//  }
//	
	
}
