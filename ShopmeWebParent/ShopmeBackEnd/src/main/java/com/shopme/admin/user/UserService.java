package com.shopme.admin.user;

import java.util.List;
import java.util.NoSuchElementException;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.shopme.common.entity.Role;
import com.shopme.common.entity.User;

@Service
@Transactional
public class UserService {
	public static final int USER_PER_PAGE = 4;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private RoleRepository roleRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;

	public List<User> listAllUsers(){
		return (List<User>)userRepository.findAll(Sort.by("firstName").ascending());
	}
	
	public List<Role> listOfRoles(){
		return (List<Role>) roleRepository.findAll();
	}

	public Page<User> listByPage(int pageNumber, String sortField, String sortDirection, String keyword) {
		Sort sort = Sort.by(sortField);
		sort = sortDirection.equals("asc") ? sort.ascending() : sort.descending();
		Pageable pageable = PageRequest.of(pageNumber - 1, USER_PER_PAGE, sort);
		if(keyword != null) {
			return userRepository.findAll(keyword, pageable);
		}
		return userRepository.findAll(pageable);
	}
	
	public User save(User user) {
		/* 
		 * here we are checking if it's a new user or an existing user then we only change the password
		 * only if it's a new user of an existing user password is being changed
		 * */
		boolean isUpdatingUser = (user.getId() != null);
		if(isUpdatingUser) {
			User existingUser = userRepository.findById(user.getId()).get();
			if(user.getPassword().isEmpty()) {
				user.setPassword(existingUser.getPassword());
			} else {
				encodePassword(user);
			}
		}else {
			encodePassword(user);
		}
		return userRepository.save(user);
	}
	
	/* below method is for users for updating their account details */
	public User updateAccount(User userInForm) {
		User userInDB =  userRepository.findById(userInForm.getId()).get();
		if(!userInForm.getPassword().isEmpty()) {
			userInDB.setPassword(userInForm.getPassword());
			encodePassword(userInDB);
		}
		
		if(userInForm.getPhotos() != null) {
			userInDB.setPhotos(userInForm.getPhotos());
		}
		
		userInDB.setFirstName(userInForm.getFirstName());
		userInDB.setLastName(userInForm.getLastName());
		
		return userRepository.save(userInDB);
	}
	
	private void encodePassword(User user) {
		String encodedPasssword = passwordEncoder.encode(user.getPassword());
		user.setPassword(encodedPasssword);
	}
	
	public boolean isEmailUnique(Integer id, String email) {
		/*
		 * here we are checking for the uniqueness in the email only if a new user is being
		 * created else if the existing user is being edited then we check if the email id belongs
		 * to that user or not if not return false and also check for uniqueness in email if it also
		 * get edited.
		 * */
		User userEmail = userRepository.getUserByEmail(email);
		if(userEmail == null) return true;
		boolean isCreatingNew = (id == null);
		if(isCreatingNew) {
			if(userEmail != null) return false;
		}else {
			if(userEmail.getId() != id) {
				return false;
			}
		}
		return true;
	}

	public User get(Integer id) throws UserNotFoundException {
		try {
			return userRepository.findById(id).get();
		} catch (NoSuchElementException e) {
			throw new UserNotFoundException("Counld not find any user with ID: "+id);
		}
	}
	
	public void deleteUser(Integer id) throws UserNotFoundException {
		Long countById = userRepository.countById(id);
		if(countById == null || countById == 0) {
			throw new UserNotFoundException("Counld not find any user with ID: "+id);
		}
		userRepository.deleteById(id);
	}
	
	public void updateUserEnabledStatus(Integer id, boolean enabled) {
		userRepository.updateEnabledStatus(id, enabled);
	}

	public User getByEmail(String email) {
		return userRepository.getUserByEmail(email);
	}
}
