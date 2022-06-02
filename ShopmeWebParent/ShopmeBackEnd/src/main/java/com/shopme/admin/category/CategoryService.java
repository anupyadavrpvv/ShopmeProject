package com.shopme.admin.category;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.shopme.common.entity.Category;

@Service
@Transactional
public class CategoryService {
	private static final int ROOT_CATEGORIES_PER_PAGE = 4;
	
	@Autowired
	private CategoryRepository repo;
	
	public List<Category> listByPage(CategoryPageInfo pageInfo, int pageNum, String sortDir, String keyword) {
		Sort sort = Sort.by("name");
		
		if(sortDir.equals("asc")) {
			sort = sort.ascending();
		}else if(sortDir.equals("desc")){
			sort = sort.descending();
		}
		
		Pageable pageable = PageRequest.of(pageNum - 1, ROOT_CATEGORIES_PER_PAGE, sort);
		
		Page<Category> pageCategory = null;
		if(keyword == null) {
			pageCategory = repo.search(keyword, pageable);
		}else {
			pageCategory = repo.findRootCategories(pageable);
		}
		
		
		List<Category> rootCategories =  pageCategory.getContent();
		
		pageInfo.setTotalElements(pageCategory.getTotalElements());
		pageInfo.setTotalPages(pageCategory.getTotalPages());
		
		if(keyword == null) {
			List<Category> searchResult = pageCategory.getContent();
			for (Category category : searchResult) {
				category.setHasChildren(category.getChildren().size() > 0);
			}
			
			return searchResult;
		}else {
			return listHierarchicalCategories(rootCategories, sortDir);
		}
		
		
	}
	
	public List<Category> listHierarchicalCategories(List<Category> rootCategories, String sortDir) {
		List<Category> hierarchicalCategories = new ArrayList<>();
		
		for (Category rootCategory : rootCategories) {
			hierarchicalCategories.add(Category.copyFullCategory(rootCategory));
			
			Set<Category> childrenCategories = sortSubCategories(rootCategory.getChildren(), sortDir);
			
			for (Category subCategory : childrenCategories) {
				String name = "--" + subCategory.getName();
				hierarchicalCategories.add(Category.copyFullCategory(subCategory, name));
				
				listSubHierarchicalCategories(hierarchicalCategories, subCategory, 1, sortDir);
			}
		}
		
		return hierarchicalCategories;
	}
	
	private void listSubHierarchicalCategories(List<Category> hierarchicalCategories,
			Category parent, int subLevel, String sortDir) {
		
		Set<Category> children = sortSubCategories(parent.getChildren(), sortDir);
		int newSubLevel = subLevel + 1;
		for(Category subCategory : children) {
			String name="";
			for (int i = 0; i < newSubLevel; i++) {
				name += "--";
			}
			name += subCategory.getName();
			
			hierarchicalCategories.add(Category.copyFullCategory(subCategory, name));
			
			listSubHierarchicalCategories(hierarchicalCategories, subCategory, newSubLevel, sortDir);
		}
	}
	
	public Category save(Category category) {
		return repo.save(category);
	}
	
	public List<Category> listCategoriesUsedInForm() {
		List<Category> categoriesUsedInForm = new ArrayList<>();
		
		Iterable<Category> categoriesInDB = repo.findRootCategories(Sort.by("name").ascending());
		
		for(Category category : categoriesInDB) {
			if(category.getParent() == null) {
				categoriesUsedInForm.add(Category.copyIdAndName(category));
				
				Set<Category> children = sortSubCategories(category.getChildren());
				
				for(Category subCategory : children) {
					String name = "--" + subCategory.getName();
					categoriesUsedInForm.add(Category.copyIdAndName(subCategory.getId(), name));
				
					listSubCategoriesUsedInForm(categoriesUsedInForm, subCategory, 1);
				}
			}
		}
		
		return categoriesUsedInForm;
	}
	
	//method for print children categories of a parent category
	private void listSubCategoriesUsedInForm(List<Category> categoriesUsedInForm, Category parent, int subLevel) {
		int newSubLevel = subLevel + 1;
		Set<Category> children = sortSubCategories(parent.getChildren());
		
		for(Category subCategory : children) {
			String name="";
			for (int i = 0; i < newSubLevel; i++) {
				name += "--";
			}
			name += subCategory.getName();
			
			categoriesUsedInForm.add(Category.copyIdAndName(subCategory.getId(), name));
			
			listSubCategoriesUsedInForm(categoriesUsedInForm, subCategory, newSubLevel);
		}
	}
	
	public Category get(Integer id) throws CategoryNotFoundException {
		try {
			return repo.findById(id).get();
		} catch (NoSuchElementException e) {
			throw new CategoryNotFoundException("Could not find any category with ID" + id);
		}
	}
	
	public String checkUnique(Integer id, String name, String alias) {
		boolean isCreatingNew = (id == null || id == 0);
		Category categoryByName = repo.findByName(name);
		
		if(isCreatingNew) {
			if(categoryByName != null) {
				return "DuplicateName";
			}else {
				Category categoryByAliasName = repo.findByAlias(alias);
				if(categoryByAliasName != null) {
					return "DuplicateAlias";
				}
			}
		}else {
			if (categoryByName != null && categoryByName.getId() != id) {
				return "DuplicateName";
			}

			Category categoryByAliasName = repo.findByAlias(alias);
			if (categoryByAliasName != null && categoryByAliasName.getId() != id) {
				return "DuplicateAlias";
			}
			
		}
		
		return "OK";
	}
	
	//method to sort the Set of children categories
	
	public SortedSet<Category> sortSubCategories(Set<Category> children) {
		return sortSubCategories(children, "asc");
	}
	
	public SortedSet<Category> sortSubCategories(Set<Category> children, String sortDir) {
		SortedSet<Category> sortedChildren = new TreeSet<>(new Comparator<Category>() {

			@Override
			public int compare(Category cat1, Category cat2) {
				if (sortDir.equals("asc")) {
					return cat1.getName().compareTo(cat2.getName());
				} else {
					return cat2.getName().compareTo(cat1.getName());
				}
			}
			
		});
		sortedChildren.addAll(children);
		return sortedChildren;
	}

	public void updateCategoriesEnabledStatus(Integer id, boolean enabled) {
		repo.updateEnabledStatus(id, enabled);
		
	}

	public void deleteCategory(Integer id) throws CategoryNotFoundException {
		Long countById = repo.countById(id);
		if(countById == null || countById == 0) {
			throw new CategoryNotFoundException("Could not find any category with given ID:" + id );
		}
		repo.deleteById(id);
	}
}
