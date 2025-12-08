package com.michael.app.blog.service;

import java.util.List;
import java.util.Set;

import com.michael.app.blog.model.Article;

public interface BlogService {
	List<Article> getAllArticles();
	List<Article> getArticlesByTag(String tagLabel);
	Article saveArticle(String title, String content, Set<String> tags);
	Article updateArticle(String id, String title, String content, Set<String> tags);
	void deleteArticle(String id);
	void addTag(String id1, String string);
}