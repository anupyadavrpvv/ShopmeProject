package com.shopme.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {
	
	/*Handler method for admin home page*/
	@GetMapping("")
	public String viewHomePage() {
		return "index";
	}
	
	/* Handler method for user login page */
	@GetMapping("/login")
	public String viewLoginPage() {
		return "login";
	}
}
