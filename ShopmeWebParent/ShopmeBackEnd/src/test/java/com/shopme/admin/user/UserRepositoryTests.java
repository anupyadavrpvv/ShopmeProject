package com.shopme.admin.user;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.Rollback;

import com.shopme.common.entity.Role;
import com.shopme.common.entity.User;

@DataJpaTest(showSql = false)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
public class UserRepositoryTests {
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private TestEntityManager entityManager;
	
	@Test
	public void testCreateNewUserWithOneRole() {
		//here we are defining the Role to the user using the Role entity class as Admin whose id is 1
		Role adminRole = entityManager.find(Role.class, 1);
		///creating the user object
		User user = new User("anup@java.net", "anup2021", "Anup", "Yadav");
		//define the role of the user
		user.addRole(adminRole);
		//save the user
		User saveUser = userRepo.save(user);
		//check if it was success
		assertThat(saveUser.getId()).isGreaterThan(0);
	}
	
	@Test
	public void testCreateNewUserWithTwoRoles() {
		User newUser = new User("ramkumar@gmail", "ram123", "Ram", "Kumar");
		Role editorRole = new Role(3);
		Role assistantRole = new Role(5);
		
		newUser.addRole(editorRole);
		newUser.addRole(assistantRole);
		
		User saveNewUser = userRepo.save(newUser);
		
		assertThat(saveNewUser.getId()).isGreaterThan(0);
	}
	
	@Test
	public void testListAllUser() {
		Iterable<User> usersListIterable = userRepo.findAll();
		usersListIterable.forEach(user -> System.out.println(user));
	}
	
	@Test
	public void testGetUserById() {
		User getUser = userRepo.findById(1).get();
		System.out.println(getUser);
		assertThat(getUser).isNotNull();
	}
	
	@Test
	public void testUpdateUserDetails() {
		User updateUser = userRepo.findById(1).get();
		updateUser.setEnabled(true);
		updateUser.setEmail("anupyadav@yahoo.com");
		userRepo.save(updateUser);
	}
	
	@Test
	public void testUpdateUserRoles() {
		User updateUserRole = userRepo.findById(2).get();
		Role deleteRole = new Role(3);
		Role giveNewRole = new Role(2);
		
		updateUserRole.getRoles().remove(deleteRole);
		updateUserRole.getRoles().add(giveNewRole);
		
		userRepo.save(updateUserRole);
	}
	
	@Test
	public void testDeleteUserById() {
		Integer deleteUserId = 2;
		userRepo.deleteById(deleteUserId);
		
	}
	
	@Test
	public void testGetUserByEmail() {
		String email = "ramann@gmail.com";
		User user = userRepo.getUserByEmail(email);
		assertThat(user).isNotNull();
	}
	
	@Test
	public void testCountById() {
		Integer id = 1;
		Long countByIdLong = userRepo.countById(id);
		assertThat(countByIdLong).isNotNull().isGreaterThan(0);
	}
	
	@Test
	public void testDisableUser() {
		Integer id = 1;
		userRepo.updateEnabledStatus(id, false);
	}
	
	@Test
	public void testEnableUser() {
		Integer id = 5;
		userRepo.updateEnabledStatus(id, true);
	}
	
	@Test
	public void testListFirstPage() {
		int pageNumber = 0;
		int pageSize = 4;
		
		Pageable pageable = PageRequest.of(pageNumber, pageSize);
		Page<User> page = userRepo.findAll(pageable);
		
		List<User> listUsers = page.getContent();
		listUsers.forEach(user -> System.out.println(user));
		assertThat(listUsers.size()).isEqualTo(pageSize);
	}
	
	@Test
	public void testSearchUsers() {
		String keyword = "bruce";
		
		int pageNumber = 0;
		int pageSize = 4;
		Pageable pageable = PageRequest.of(pageNumber, pageSize);
		Page<User> page = userRepo.findAll(keyword, pageable);
		List<User> listUsers = page.getContent();
		listUsers.forEach(user -> System.out.println(user));
		assertThat(listUsers.size()).isGreaterThan(0);
	}
}
