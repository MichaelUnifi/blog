package com.michael.app.blog.controller;

import com.michael.app.blog.model.Article;
import com.michael.app.blog.model.Tag;
import com.michael.app.blog.repository.BlogRepository;
import com.michael.app.blog.view.BlogView;

public class BlogController {
	private BlogRepository repository;
	private BlogView view;
	
	public BlogController(BlogRepository repository, BlogView view) {
		this.repository = repository;
		this.view = view;
	}
	
	public void allArticles() {
		view.showAllArticles(repository.findAll());
	}
	
	public void allArticlesWithTag(Tag tag) {
		view.showAllArticles(repository.findAllWithTag(tag));
	}
	
	public void addArticle(Article article) {
		Article foundArticle = repository.findById(article.getId());
		if(foundArticle == null) {
			repository.save(article);
			view.articleAdded(article);
		} else {
			view.showError("An article with title " + article.getTitle() + " is already present!", foundArticle);
		}
	}
	
	public void deleteArticle(Article article) {
		Article foundArticle = repository.findById(article.getId());
		if(foundArticle == null) {
			view.showError("No article with title " + article.getTitle(), article);
		} else {
			repository.delete(article.getId());
			view.articleDeleted(article);
		}
	}

	public void tag(Article article, Tag tag) {
		Article foundArticle = repository.findById(article.getId());
		if(foundArticle == null) {
			view.showError("Unable to tag: article does not exist", article);
		} else if(foundArticle.getTags().contains(tag)) {
			view.showError("Article already has tag " + tag.getLabel(), article);
		} else {
			article.addTag(tag);
			repository.save(article);
			view.addedTag(article, tag);
		}
	}

	public void untag(Article article, Tag tag) {
		Article foundArticle = repository.findById(article.getId());
		
		if(foundArticle == null) {
			view.showError("Unable to untag: article does not exist", article);
			
		} else if(foundArticle.getTags().contains(tag)){
			article.removeTag(tag);
			repository.save(foundArticle);
			view.removedTag(foundArticle, tag);
		} else {
			view.showError("Article is not tagged with " + tag.getLabel(), article);
		}
		
	}

	
}
