package com.michael.app.blog.repository;

import java.util.List;

import com.michael.app.blog.model.Article;
import com.michael.app.blog.model.Tag;

public interface BlogRepository {

	List<Article> findAll();

	List<Article> findAllWithTag(Tag tag);
	
	Article findById(int id);

	void save(Article article);

	void delete(int id);
}
