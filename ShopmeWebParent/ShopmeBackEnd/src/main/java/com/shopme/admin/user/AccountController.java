package com.shopme.admin.user;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.shopme.admin.FileUploadUtil;
import com.shopme.admin.security.ShopmeUserDetails;
import com.shopme.common.entity.User;

@Controller
public class AccountController {
	@Autowired
	private UserService service;
	
	/*
	 * handler method for redirecting user to update account form 
	 * when use clicks on his name to update user details
	 */
	@GetMapping("/account")
	public String viewDetails(@AuthenticationPrincipal ShopmeUserDetails loggedUser,
					Model model) {
		String email = loggedUser.getUsername();
		User user = service.getByEmail(email);
		model.addAttribute("user", user);
		
		return "account_form";
	}
	
	/*
	 * handler method for updating the user details when a user updates it by
	 * himself
	 */
	@PostMapping("/account/update")
	public String updateUserDetails(User user, RedirectAttributes redirectAttributes,
			@AuthenticationPrincipal ShopmeUserDetails loggedUser,
			@RequestParam("image") MultipartFile multipartFile) throws IOException {
//		System.out.println(user);
//		System.out.println(multipartFile.getOriginalFilename());
		if(!multipartFile.isEmpty()) {
			String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
			user.setPhotos(fileName);
			User savedUser = service.updateAccount(user);
			String uploadDirectory = "user-photos/" + savedUser.getId();
			FileUploadUtil.cleanDir(uploadDirectory);
			FileUploadUtil.saveFile(uploadDirectory, fileName, multipartFile);
		} else {	
			if(user.getPhotos().isEmpty()) user.setPhotos(null);
			service.updateAccount(user);
		}
		
		loggedUser.setFirstName(user.getFirstName());
		loggedUser.setLastName(user.getLastName());
		
//		service.save(user);
		redirectAttributes.addFlashAttribute("message", "Your account detailos has been updated successfully!");
		
		return "redirect:/account";
	}
}
