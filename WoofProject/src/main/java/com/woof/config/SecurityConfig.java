package com.woof.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import com.zeus.common.security.CustomLoginSuccessHandler;
import com.zeus.common.security.CustomUserDetailsService;

import lombok.extern.java.Log;

@Log
// @Configuration: class will define beans
@Configuration
// @EnableWebSecurity enables web security features from Spring Security
@EnableWebSecurity
/* 
	@EnableMethodSecurity enables method-level security features
	prePostEnabled true enables use of @PreAuthorize and @PostAuthorize security checks
	securedEnabled true enables use of @Secured
*/
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig {
	@Autowired
	DataSource dataSource;

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		log.info("SecurityFilterChain called");

		// disables CSRF protection
		http.csrf().disable();

		// exceptions handled with GlobalExceptionHandler
		// this would replace 403 error with redirect to /accessError
//		http.exceptionHandling().accessDeniedHandler(createAccessDeniedHandler());

		// configure login settings
		http.formLogin().loginPage("/login").successHandler(createAuthenticationSuccessHandler()).failureUrl("/account/loginFail");
		
		// when logging out, delete cookies used for automatic login
		http.logout().logoutUrl("/account/logout").logoutSuccessUrl("/").invalidateHttpSession(true).deleteCookies("remember-me","JSESSION_ID");

		// set validity period of remember-me cookie to 24 hours
		http.rememberMe().key("zeus").tokenRepository(createJDBCRepository()).tokenValiditySeconds(60 * 60 * 24 * 30);

		return http.build();
	}

	// create JDBC-based persistent token repository
	private PersistentTokenRepository createJDBCRepository() {
		JdbcTokenRepositoryImpl repo = new JdbcTokenRepositoryImpl();
		repo.setDataSource(dataSource);
		return repo;
	}
	
	// configure authentication manager with userDetailsService and password encoder
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(createUserDetailsService()).passwordEncoder(createPasswordEncoder());
	}
	
	// create and return a BCrypt password encoder
	@Bean
	PasswordEncoder createPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	// register class implementing UserDetailsService interface from Spring Security
	@Bean
	UserDetailsService createUserDetailsService() { 
		return new CustomUserDetailsService();
	}
		
	// register CustomLoginSuccessHandler as a bean
	@Bean
	AuthenticationSuccessHandler createAuthenticationSuccessHandler() {
		return new CustomLoginSuccessHandler();
	}
}


