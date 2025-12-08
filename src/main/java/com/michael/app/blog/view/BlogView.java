package com.michael.app.blog.view;

import java.util.List;

import com.michael.app.blog.model.Article;
import com.michael.app.blog.model.Tag;

public interface BlogView {

	void showAllArticles(List<Article> articles);
	
	void showAllArticlesWithTag(List<Article> articles);

	void articleAdded(Article article);

	void showError(String errorMessage);

	void articleUpdated(Article updatedArticle);

	void articleDeleted();

	void addedTag(Tag tag);

	void removedTag();
}