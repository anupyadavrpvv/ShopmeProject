package com.shopme.admin.user.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.shopme.admin.FileUploadUtil;
import com.shopme.admin.user.UserNotFoundException;
import com.shopme.admin.user.UserService;
import com.shopme.admin.user.export.UserCSVExporter;
import com.shopme.admin.user.export.UserExcelExporter;
import com.shopme.admin.user.export.UserPDFExporter;
import com.shopme.common.entity.Role;
import com.shopme.common.entity.User;


@Controller
public class UserController {
	@Autowired
	private UserService service;
	
	@GetMapping("/users")
	public String listFirstPage(Model model) {
//		List<User> listUsers = service.listAllUsers();
//		model.addAttribute("listUsers", listUsers);
//		
//		return "users";
		return listByPage(1, model, "firstName", "asc", null);
	}
	
	@GetMapping("/users/page/{pageNumber}")
	public String listByPage(@PathVariable(name = "pageNumber") int pageNumber, Model model,
			@Param("sortField") String sortField, @Param("sortDir") String sortDirection,
			@Param("keyword") String keyword
			) {
		System.out.println("Sort Field: " + sortField);
		System.out.println("Sort Direction: " + sortDirection);
		Page<User> page = service.listByPage(pageNumber, sortField, sortDirection, keyword);
		List<User> listUsers = page.getContent();
//		System.out.println("PageNumber: "+pageNumber);
//		System.out.println("Total elements: "+ page.getTotalElements());
//		System.out.println("Total pages: "+ page.getTotalPages());
		long startCount = (pageNumber - 1) * UserService.USER_PER_PAGE + 1;
		long endCount = startCount + UserService.USER_PER_PAGE - 1;
		if(endCount > page.getTotalElements()) {
			endCount = page.getTotalElements();
		}
		String reverseSortDir = sortDirection.equals("asc") ? "desc" : "asc";
		
		model.addAttribute("currentPage", pageNumber);
		model.addAttribute("totalPages", page.getTotalPages());
		model.addAttribute("startCount", startCount);
		model.addAttribute("endCount", endCount);
		model.addAttribute("totalItems", page.getTotalElements());
		model.addAttribute("listUsers", listUsers);
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortDirection", sortDirection);
		model.addAttribute("reverseSortDir", reverseSortDir);
		model.addAttribute("keyword", keyword);
		return "users/users";
	}
	
	@GetMapping("/users/new")
	public String newUser(Model model) {
		List<Role> rolesList =  service.listOfRoles();
		User user = new User();
		user.setEnabled(true);
		model.addAttribute("user", user);
		model.addAttribute("rolesList", rolesList);
		model.addAttribute("pageTitle", "Create New User");
		return "users/user_form";
	}
	
	@PostMapping("/users/save")
	public String saveUser(User user, RedirectAttributes redirectAttributes,
			@RequestParam("image") MultipartFile multipartFile) throws IOException {
//		System.out.println(user);
//		System.out.println(multipartFile.getOriginalFilename());
		if(!multipartFile.isEmpty()) {
			String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
			user.setPhotos(fileName);
			User savedUser = service.save(user);
			String uploadDirectory = "user-photos/" + savedUser.getId();
			FileUploadUtil.cleanDir(uploadDirectory);
			FileUploadUtil.saveFile(uploadDirectory, fileName, multipartFile);
		} else {	
			if(user.getPhotos().isEmpty()) user.setPhotos(null);
			service.save(user);
		}
		
//		service.save(user);
		redirectAttributes.addFlashAttribute("message", "The user has been saved successfully!");
		
		return getRedirectURLforAffectedUser(user);
	}

	private String getRedirectURLforAffectedUser(User user) {
		String firstPartOfEmailString = user.getEmail().split("@")[0];
		return "redirect:/users/page/1?sortField=id&sortDirection=asc&keyword=" + firstPartOfEmailString;
	}
	
	@GetMapping("/users/edit/{id}")
	public String editUser(@PathVariable(name = "id") Integer id,
			Model model,
			RedirectAttributes redirectAttributes) {
		try {
			List<Role> rolesList =  service.listOfRoles();
			User user = service.get(id);
			model.addAttribute("user", user);
			model.addAttribute("rolesList", rolesList);
			model.addAttribute("pageTitle", "Edit User (Id: "+id+")");
			return "users/user_form";
		} catch (UserNotFoundException e) {
			redirectAttributes.addFlashAttribute("message", e.getMessage());
			return "redirect:/users";
		}
		
	}
	
	@GetMapping("/users/delete/{id}")
	public String deleteUser(@PathVariable(name = "id") Integer id,
				Model model,
				RedirectAttributes redirectAttributes) {
		try {
			service.deleteUser(id);
			redirectAttributes.addFlashAttribute("message", "The user ID: "+ id + "has been deleted successfully");
		}catch (UserNotFoundException e) {
			redirectAttributes.addFlashAttribute("message", e.getMessage());
		}
		return "redirect:/users";
	}
	
	@GetMapping("/users/{id}/enabled/{status}")
	public String updateUserEnabledStatus(@PathVariable("id") Integer id,
			@PathVariable("status") boolean enabled,
			RedirectAttributes redirectAttributes) {
		
		service.updateUserEnabledStatus(id, enabled);
		String status = enabled ? "enabled" : "disabled";
		String message = "The user ID " + id + "has been " + status;
		
		redirectAttributes.addFlashAttribute("message", message);
		return "redirect:/users";
	}
	
	@GetMapping("/users/export/csv")
	public void exportToCSV(HttpServletResponse response) throws IOException { 
		List<User> listUsers = service.listAllUsers();
		UserCSVExporter exporter = new UserCSVExporter();
		exporter.export(listUsers, response);
	}
	
	@GetMapping("/users/export/excel")
	public void exportToExcel(HttpServletResponse response) throws IOException { 
		List<User> listUsers = service.listAllUsers();
		UserExcelExporter exporter = new UserExcelExporter();
		exporter.export(listUsers, response);
	}
	
	@GetMapping("/users/export/pdf")
	public void exportToPDF(HttpServletResponse response) throws IOException { 
		List<User> listUsers = service.listAllUsers();
		UserPDFExporter exporter = new UserPDFExporter();
		exporter.export(listUsers, response);
	}
}
