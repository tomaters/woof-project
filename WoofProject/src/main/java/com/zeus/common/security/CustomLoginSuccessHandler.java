package com.zeus.common.security;
// custom AuthenticationSuccessHandler to handle authentication events 
import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CustomLoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication auth)
			throws IOException, ServletException {
		// provide logged in user's information
		User customAccount = (User) auth.getPrincipal();
		// ... optional logic using customAccount to customize authentication success 
		
		// contine with default behavior
		super.onAuthenticationSuccess(request, response, auth);
	}
}
