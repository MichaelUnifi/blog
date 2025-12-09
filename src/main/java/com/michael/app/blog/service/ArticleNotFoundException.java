package com.michael.app.blog.service;

public class ArticleNotFoundException extends RuntimeException {
	
	private static final long serialVersionUID = -8872076692643773036L;

	public ArticleNotFoundException(String id) {
		super("Article not found with ID: " + id);
	}
}