package com.michael.app.blog.controller;

import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.michael.app.blog.model.Article;
import com.michael.app.blog.model.Tag;
import com.michael.app.blog.service.BlogService;
import com.michael.app.blog.view.BlogView;

public class BlogController {
	private BlogService service;
	private BlogView view;
	
	@Inject
	public BlogController(BlogService service, @Assisted BlogView view) {
		this.service = service;
		this.view = view;
	}
	
	public void allArticles() {
		view.showAllArticles(service.getAllArticles());
	}
	
	public void allArticlesWithTag(String tagLabel) {
		view.showAllArticles(service.getArticlesByTag(tagLabel));
	}
	
	public void saveArticle(String title, String content, Set<String> tagLabels) {
		try {
			Article savedArticle = service.saveArticle(title, content, tagLabels);
			view.articleAdded(savedArticle);
		} catch(Exception e) {
			view.showError("Error in article save - " + e.getMessage());
		}
	}
	
	public void updateArticle(String id, String title, String content, Set<String> tagLabels) {
		try {
			Article updatedArticle = service.updateArticle(id, title, content, tagLabels);
			view.articleUpdated(updatedArticle);
		} catch(Exception e) {
			view.showError("Error in article update - " + e.getMessage());
		}
	}
	
	public void deleteArticle(String id) {
		try {
			service.deleteArticle(id);
			view.articleDeleted();
		} catch(Exception e) {
			view.showError("Error in article delete - " + e.getMessage());
		}
	}

	public void tag(String tagLabel) {
		try {
			Tag tag = new Tag(tagLabel);
			view.addedTag(tag);	
		} catch(Exception e) {
			view.showError(e.getMessage());
		}
	}
}
