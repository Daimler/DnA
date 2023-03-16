package com.mb.dna.data.application.config.filter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

import io.micronaut.http.*;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mb.dna.data.application.adapter.dna.DnaClientConfig;
import com.mb.dna.data.application.adapter.dna.DnaHttpClient;
import com.mb.dna.data.application.adapter.dna.UserInfo;
import com.mb.dna.data.application.adapter.dna.UserRole;
import com.mb.dna.data.application.adapter.dna.UserStore;

import io.jsonwebtoken.Claims;
import io.micronaut.context.ApplicationContext;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Filter("/api/**") 
@Singleton
public class JWTAuthenticationFilter implements HttpServerFilter{

	private Logger log = LoggerFactory.getLogger(JWTAuthenticationFilter.class);

	@Inject
	DnaClientConfig dnaClientConfig;

	@Inject
	private UserStore userStore;
	
	@Inject
	ApplicationContext applicationContext;
	
	@Inject
	private DnaHttpClient dnaHttpClient;
	
	@SuppressWarnings("unused")
	@Override
	public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain filterChain) {
		String jwt = request.getHeaders().get("Authorization");
		
		String secretKey = dnaClientConfig.getJwt();
		String dnaAuthEnableString = dnaClientConfig.getDnaAuthEnable();
		boolean dnaAuthEnable = Boolean.valueOf(dnaAuthEnableString);
		if (jwt==null || jwt.isBlank() || jwt.isEmpty()) {
			log.error("Request UnAuthorized,No JWT available");
			Optional<MutableHttpResponse<?>> response = Optional.of(HttpResponse.status(HttpStatus.FORBIDDEN));
			return Publishers.just(response.get());
		} else {
			Claims claims = JWTGenerator.decodeJWT(jwt,secretKey);
			if (claims == null) {
				log.error("Invalid  JWT!");
				Optional<MutableHttpResponse<?>> response = Optional.of(HttpResponse.status(HttpStatus.UNAUTHORIZED));
				return Publishers.just(response.get());
			} else {
				if (dnaAuthEnable) {
					UserInfo res = dnaHttpClient.verifyLogin(jwt);
					if (res != null) {
						try {
							setUserDetailsToStore(res);
						} catch(Exception e) {
							this.userStore.clear();
						}

					} else {
						log.error("Request UnAuthorized,No JWT available");
						Optional<MutableHttpResponse<?>> response = Optional.of(HttpResponse.status(HttpStatus.FORBIDDEN));
						return Publishers.just(response.get());
					}

				} else {
					try {
						log.info(
								"Request validation successful, set request user details in the store for further access");
						setUserDetailsToStore(claims);
					} catch(Exception e) {
						this.userStore.clear();
					}
				}

			}
		}
		return Publishers.map(filterChain.proceed(request), mutableHttpResponse -> {
	          return mutableHttpResponse;
	        });
	}
	
	private void setUserDetailsToStore(Claims claims) {
		// To Set user details for local development
		UserInfo user = UserInfo.builder().id((String) claims.get("id"))
				.firstName((String) claims.get("firstName")).lastName((String) claims.get("lastName"))
				.email((String) claims.get("email")).department((String) claims.get("department"))
				.mobileNumber((String) claims.get("mobileNumber")).build();
		// To Set user Roles for local development
		List<LinkedHashMap> claimedRoles = (ArrayList) claims.get("digiRole");
		List<UserRole> roles = new ArrayList<>();
		claimedRoles.forEach(roleMapEntity -> {
			roles.add(UserRole.builder().id((String) roleMapEntity.get("id")).name((String) roleMapEntity.get("name"))
					.build());
		});
		user.setUserRole(roles);
		this.userStore.setUserInfo(user);
	}

	/**
	 * method to set user info from dnabmc client
	 * 
	 * @param claims
	 */
	private void setUserDetailsToStore(UserInfo loggedInUser) {
		this.userStore.setUserInfo(loggedInUser);
	}

}
