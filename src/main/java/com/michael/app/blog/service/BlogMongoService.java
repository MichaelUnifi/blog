package com.michael.app.blog.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.inject.Inject;
import com.michael.app.blog.model.Article;
import com.michael.app.blog.model.Tag;
import com.michael.app.blog.repository.BlogRepository;
import com.michael.app.blog.transaction.TransactionManager;

public class BlogMongoService implements BlogService {

	private TransactionManager transactionManager;

	@Inject
	public BlogMongoService(TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	@Override
	public List<Article> getAllArticles() {
		return transactionManager.doInTransaction(BlogRepository::findAll);
	}

	@Override
	public List<Article> getArticlesByTag(String tagLabel) {
		return transactionManager.doInTransaction(repository -> {
			Tag tag = new Tag(tagLabel);
			return repository.findAllWithTag(tag);
		});
	}

	@Override
	public Article saveArticle(String title, String content, Set<String> tagLabels) {
		return transactionManager.doInTransaction(repository -> {
			Article article = new Article(null, title, content, toTagSet(tagLabels));
			return repository.save(article);
		});
	}
	
	@Override
	public Article updateArticle(String id, String title, String content, Set<String> tagLabels) {
		return transactionManager.doInTransaction(repository -> {
			validateId(id, repository);
			Article updatedArticle = new Article(id, title, content, toTagSet(tagLabels));
			repository.update(updatedArticle);
			return updatedArticle;
		});
	}

	@Override
	public void deleteArticle(String id) {
		transactionManager.doInTransaction(repository -> {
			validateId(id, repository);
			repository.delete(id);
			return null;
		});
	}
	
	private void validateId(String id, BlogRepository repository) {
		if(repository.findById(id) == null)
			throw new ArticleNotFoundException(id);
	}
	
	private Set<Tag> toTagSet(Set<String> tagLabels) throws IllegalArgumentException {
		try {
			return tagLabels.stream()
				.map(Tag::new)
				.collect(Collectors.toSet());
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Tag label validation failed: " + e.getMessage(), e);
		}
	}

}
