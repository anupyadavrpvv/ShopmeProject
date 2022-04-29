package com.shopme.admin.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	
	@Bean
	public UserDetailsService userDetailsService() {
		return new ShopmeUserDetailsService();
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(userDetailsService());
		authProvider.setPasswordEncoder(passwordEncoder());
		
		return authProvider;
	}
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(authenticationProvider());
	}

	/*
	 * Below method allows the user to access the application without logging-in using
	 * username and password i.e. it bypasses spring security if you don't want to login each time
	 */
//	@Override
//	protected void configure(HttpSecurity http) throws Exception {
//		http.authorizeRequests().anyRequest().permitAll();
//	}
	

	/* 
	 * Below method only allows access to the authenticated user 
	 */
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
			.anyRequest().authenticated()
			.and()
			.formLogin()
				.loginPage("/login")//redirect to login page that we have customized
				.usernameParameter("email")//take parameter name as email because by it is default username in spring security
				.permitAll()
			.and().logout().permitAll()  //here we're using default implementation for logout by spring boot
			.and()
				.rememberMe() //default implementation by spring boot for remembering the user using browser cookies
					.key("AbcDefgHihklmnopQrstuvwxyz_1234567890")//is used for defining key that will be used by application to remember the user even after the application gets restarted
					.tokenValiditySeconds(7 * 24 * 60 * 60);//is used to define the time interval to which the cookie will survive
															//default value is 2 weeks here we have given 1 week value in seconds
	}

	/*
	 * Below method is for allowing static files like images and JS files to be
	 * Accessible without user logged in
	 */
	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/images/**", "/js/**", "/webjars/**");
	}
	
	

	
}
