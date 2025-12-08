package com.michael.app.blog.repository.mongo;

import java.util.List;
import java.util.Set;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.michael.app.blog.model.Article;
import com.michael.app.blog.model.Tag;
import com.michael.app.blog.repository.BlogRepository;
import com.mongodb.client.ClientSession;
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
	private ClientSession session = null;
	
	public BlogMongoRepository(MongoClient client, ClientSession session) {
		this.session = session;
		this.articleCollection = client.getDatabase(BLOG_DB_NAME).getCollection(ARTICLE_COLLECTION_NAME);
	}

	public BlogMongoRepository(MongoClient client) {
		this(client, null);
	}

	@Override
	public List<Article> findAll() {
		if(session != null)
			return StreamSupport.stream(articleCollection.find(session).spliterator(), false)
				.map(this::documentToArticle)
				.toList();
		else
			return StreamSupport.stream(articleCollection.find().spliterator(), false)
					.map(this::documentToArticle)
					.toList();
	}
	
	@Override
	public List<Article> findAllWithTag(Tag tag) {
		if(session != null)
			return StreamSupport.stream(articleCollection.find(session, Filters.eq("tags", tag.getLabel())).spliterator(), false)
				.map(this::documentToArticle)
				.toList();
		else
			return StreamSupport.stream(articleCollection.find(Filters.eq("tags", tag.getLabel())).spliterator(), false)
					.map(this::documentToArticle)
					.toList();
	}

	@Override
	public Article findById(String id) {
		Document d;
		if(session != null)
			d = articleCollection.find(session, Filters.eq("_id", new ObjectId(id))).first();
		else
			d = articleCollection.find(Filters.eq("_id", new ObjectId(id))).first();
		if(d == null)
			return null;
		return this.documentToArticle(d);
	}

	@Override
	public Article save(Article article) {
		if(article == null)
			throw new IllegalArgumentException("Cannot save null article!");
		if(article.getId() != null)
			throw new IllegalArgumentException("Article already has an id!");
		Document doc = extractArticleInfo(article);
		if(session != null)
			articleCollection.insertOne(session, doc);
		else
			articleCollection.insertOne(doc);
		return documentToArticle(doc);
	}
	
	@Override
	public void update(Article article) {
		if(article == null)
			throw new IllegalArgumentException("Cannot update null article!");
		if(article.getId() == null)
			throw new IllegalArgumentException("Article has null id!");
		Document doc = articleToDocument(article);
		doc.remove("_id");
		ObjectId articleId = new ObjectId(article.getId());
		if(session != null)
			articleCollection.replaceOne(
				session,
				Filters.eq("_id", articleId),
				doc,
				new ReplaceOptions().upsert(false)
			);
		else
			articleCollection.replaceOne(
				Filters.eq("_id", articleId),
				doc,
				new ReplaceOptions().upsert(false)
			);
	}
	
	private Document extractArticleInfo(Article article) {
		Document doc = articleToDocument(article);
		doc.remove("_id");
		return doc;
	}

	@Override
	public void delete(String id) {
		if(id == null)
			throw new IllegalArgumentException("Cannot delete: given id is null!");
		if(session != null)
			articleCollection.deleteOne(session, new Document("_id", new ObjectId(id)));
		else
			articleCollection.deleteOne(new Document("_id", new ObjectId(id)));
	}

	private Article documentToArticle(Document doc) {
		List<String> tagList = doc.getList("tags", String.class);
		Set<Tag> tags = tagList.stream()
			.map(Tag::new)
			.collect(Collectors.toSet());
		return new Article(
			doc.getObjectId("_id").toString(),
			doc.getString("title"),
			doc.getString("content"),
			tags
			);
	}

	private Document articleToDocument(Article article) {
		ObjectId id = null;
		if(article.getId() != null)
			id = new ObjectId(article.getId());
		List<String> tagLabels = article.getTags().stream()
			.map(Tag::getLabel)
			.toList();
		return new Document(new Document("_id", id)
			.append("title", article.getTitle())
			.append("content", article.getContent())
			.append("tags", tagLabels));
	}
}