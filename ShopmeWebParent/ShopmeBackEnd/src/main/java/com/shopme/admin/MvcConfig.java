package com.shopme.admin;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		String dirName = "user-photos";
		Path userPhotosDir = Paths.get(dirName);
		String userPhotosPath = userPhotosDir.toFile().getAbsolutePath();
//		System.out.println(userPhotosPath);
//		System.out.println("file:/" + userPhotosPath + "/");
//		System.out.println("/" + dirName + "/**");
		registry.addResourceHandler("/" + dirName + "/**")
				.addResourceLocations("file:/" + userPhotosPath + "/");
		
		
		String categoryImageDirName = "../category-images";
		Path categoriesImageDir = Paths.get(categoryImageDirName);
		String categoryImagePath = categoriesImageDir.toFile().getAbsolutePath();

		registry.addResourceHandler("/category-images/**")
				.addResourceLocations("file:/" + categoryImagePath + "/");
		
	}

}
