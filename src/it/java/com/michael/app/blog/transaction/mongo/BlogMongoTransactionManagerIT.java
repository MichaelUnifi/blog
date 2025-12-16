package com.michael.app.blog.transaction.mongo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.michael.app.blog.guice.BlogSwingMongoDefaultModule;
import com.michael.app.blog.model.Article;
import com.michael.app.blog.model.Tag;
import com.michael.app.blog.transaction.BlogMongoTransactionManager;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class BlogMongoTransactionManagerIT {
	
	@SuppressWarnings({"resource"})
	private static MongoDBContainer mongoContainer =
		new MongoDBContainer(DockerImageName.parse("mongo:5"))
		.withReplicaSet();
	
	private String databaseName = "test-blog";
	private String collectionName = "test-blog";
	
	private BlogMongoTransactionManager transactionManager;
	private String id = "000000000000000000000000";
	
	@BeforeClass
	public static void setUpContainer() {
		mongoContainer.start();
	}
	
	@AfterClass
	public static void tearDownContainer() {
		mongoContainer.stop();
	}
	
	@Before
	public void setUp() {
		Injector injector = Guice.createInjector(
			new BlogSwingMongoDefaultModule()
			.mongoHost(mongoContainer.getReplicaSetUrl())
			.databaseName(databaseName)
			.collectionName(collectionName)
		);
		
		transactionManager = injector.getInstance(BlogMongoTransactionManager.class);
	}
	
	@Test
	public void testDoInTransactionCommit() {
		Article article = new Article(id, "Parmesan eggplants", "I like them");
		setupDatabaseWithOneArticle(article);
		transactionManager.doInTransaction(repository -> {
			repository.delete(id);
			return null;
		});
		List<Article> databaseList = readAllArticlesFromDatabase();
		assertThat(databaseList).isEmpty();
	}
	
	@Test
	public void testDoInTransactionRollback() {
		Article article = new Article(id, "Parmesan eggplants", "I like them");
		setupDatabaseWithOneArticle(article);
		try {
			transactionManager.doInTransaction(repository -> {
				repository.delete(id);
				throw new RuntimeException("DATABASE ERROR");
			});
		} catch (RuntimeException e) {
			//just to pass to the assertions
		}
		List<Article> databaseList = readAllArticlesFromDatabase();
		assertThat(databaseList.get(0)).isEqualTo(article);
	}
	
	private void setupDatabaseWithOneArticle(Article article) {
		MongoClient client = MongoClients.create(mongoContainer.getConnectionString());
		MongoDatabase database = client.getDatabase(databaseName);
		MongoCollection<Document> articleCollection = database.getCollection(collectionName);
		List<String> tagLabels = article.getTags().stream()
			.map(Tag::getLabel)
			.toList();
		articleCollection.insertOne(new Document("_id", new ObjectId(article.getId()))
			.append("title", article.getTitle())
			.append("content", article.getContent())
			.append("tags", tagLabels));
	}
	
	private List<Article> readAllArticlesFromDatabase() {
		MongoClient client = MongoClients.create(mongoContainer.getConnectionString());
		MongoDatabase database = client.getDatabase(databaseName);
		MongoCollection<Document> articleCollection = database.getCollection(collectionName);
		return StreamSupport.stream(articleCollection.find().spliterator(), false)
			.map(this::documentToArticle)
			.toList();
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
}