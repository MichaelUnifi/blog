package com.michael.app.blog.repository.mongo;

import java.util.List;
import java.util.Set;

import org.bson.Document;


import com.michael.app.blog.model.Article;
import com.michael.app.blog.model.Tag;
import com.michael.app.blog.repository.BlogRepository;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class BlogMongoRepository implements BlogRepository {
	public static final String BLOG_DB_NAME = "blog";
	public static final String ARTICLE_COLLECTION_NAME = "blog";
	private MongoCollection<Document> articleCollection;
	
	public BlogMongoRepository(MongoClient client) {
		this.articleCollection = client.getDatabase(BLOG_DB_NAME).getCollection(ARTICLE_COLLECTION_NAME);
	}

	@Override
	public List<Article> findAll() {
		return StreamSupport.stream(articleCollection.find().spliterator(), false)
			.map(this::documentToArticle)
			.toList();
	}
	
	@Override
	public List<Article> findAllWithTag(Tag tag) {
		return StreamSupport.stream(articleCollection.find(Filters.eq("tags", tag.getLabel())).spliterator(), false)
			.map(this::documentToArticle)
			.toList();
	}

	@Override
	public Article findById(int id) {
		Document d = articleCollection.find(Filters.eq("_id", id)).first();
		if(d != null)
			return this.documentToArticle(d);
		return null;
	}

	@Override
	public void save(Article article) {
		if(article == null)
			throw new IllegalArgumentException("Cannot save null article!");
		Document doc = articleToDocument(article);
		articleCollection.replaceOne(
			new Document("_id", article.getId()),
			doc,
			new ReplaceOptions().upsert(true)
		);
	}

	@Override
	public void delete(int id) {
		articleCollection.deleteOne(new Document("_id", id));
	}

	private Article documentToArticle(Document doc) {
		List<String> tagList = doc.getList("tags", String.class);
		Set<Tag> tags = tagList.stream()
			.map(Tag::new)
			.collect(Collectors.toSet());
		return new Article(
			doc.getInteger("_id"),
			doc.getString("title"),
			doc.getString("content"),
			tags
			);
	}

	private Document articleToDocument(Article article) {
		List<String> tagLabels = article.getTags().stream()
			.map(Tag::getLabel)
			.toList();
		return new Document(new Document("_id", article.getId())
			.append("title", article.getTitle())
			.append("content", article.getContent())
			.append("tags", tagLabels));
	}
}
