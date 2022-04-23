package com.shopme.admin.user;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderTest {
	//checking if the password encoder is working 
	@Test
	public void testEncodePassword() {
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		String rawPasswordString = "anup123";
		String encodePasswordString = passwordEncoder.encode(rawPasswordString);
		System.out.println(encodePasswordString);
		
		boolean matches = passwordEncoder.matches(rawPasswordString, encodePasswordString);
		assertThat(matches).isTrue();
	}

}
