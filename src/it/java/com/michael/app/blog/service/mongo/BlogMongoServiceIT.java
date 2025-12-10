package com.michael.app.blog.service.mongo;

import static org.assertj.core.api.Assertions.*;

import java.util.Collections;
import java.util.List;
import java.util.Set;


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
import com.michael.app.blog.repository.BlogRepositoryFactory;
import com.mongodb.client.MongoClient;

public class BlogMongoServiceIT {
	
	@SuppressWarnings({"resource" })
	private static MongoDBContainer mongoContainer =
		new MongoDBContainer(DockerImageName.parse("mongo:5"))
		.withReplicaSet();
	
	private String databaseName = "test-blog";
	private String collectionName = "test-blog";
	private BlogMongoService service;
	private Injector injector;
	private MongoClient client;
	private String id = "000000000000000000000000";

	private Set<String> tagLabels;

	@BeforeClass
	public static void setUpBeforeClass() {
		mongoContainer.start();
	}

	@AfterClass
	public static void tearDownAfterClass() {
		mongoContainer.stop();
	}

	@Before
	public void setUp() {
		injector = Guice.createInjector(
			new BlogSwingMongoDefaultModule()
			.mongoHost(mongoContainer.getReplicaSetUrl())
			.databaseName(databaseName)
			.collectionName(collectionName)
		);
		injector.getInstance(BlogRepositoryFactory.class);
		client = injector.getInstance(MongoClient.class);
		service = injector.getInstance(BlogMongoService.class);
		clearDatabase();
	}

	@Test
	public void testGetAllArticles() {
		Article article = new Article(id , "Fettuccine Alfredo", "America thinks it's something special");
		addArticle(article);
		List<Article> retrievedArticles = service.getAllArticles();
		assertThat(retrievedArticles).containsExactly(article);
	}
	
	@Test
	public void testGetArticlesByTag() {
		Article article = new Article(id , "Fettuccine Alfredo", "America thinks it's something special");
		Tag tag = new Tag("cooking");
		article.addTag(tag);
		addArticle(article);
		List<Article> retrievedArticles = service.getArticlesByTag("cooking");
		assertThat(retrievedArticles).containsExactly(article);
	}
	
	@Test
	public void testSaveArticle() {
		tagLabels = Set.of("cooking");
		Article savedArticle = service.saveArticle("Fettuccine Alfredo", "America thinks it's something special", tagLabels);
		assertThat(savedArticle.getId()).isNotNull();
		List<Article> foundArticles = service.getAllArticles();
		assertThat(foundArticles).containsExactly(savedArticle);
	}
	
	@Test
	public void testDeleteArticle() {
		Article article = new Article(id , "Fettuccine Alfredo", "America thinks it's something special");
		addArticle(article);
		service.deleteArticle(id);
		List<Article> foundArticles = service.getAllArticles();
		assertThat(foundArticles).isEmpty();
	}
	
	@Test
	public void testUpdateArticle() {
		tagLabels = Set.of("cooking");
		Article article = service.saveArticle("Fettuccine Alfredo", "America thinks it's something special", Collections.emptySet());
		article.addTag(new Tag("cooking"));
		Article updatedArticle = service.updateArticle(article.getId(), "Fettuccine Alfredo", "America thinks it's something special", tagLabels);
		assertThat(article).isEqualTo(updatedArticle);
	}
	
	private void addArticle(Article article) {
		List<String> labels = article.getTags().stream()
			.map(Tag::getLabel)
			.toList();
		client.getDatabase(databaseName).getCollection(collectionName)
			.insertOne(new Document("_id", new ObjectId(article.getId()))
				.append("title", article.getTitle())
				.append("content", article.getContent())
				.append("tags", labels));
	}
	
	private void clearDatabase() {
		client.getDatabase(databaseName).drop();
	}
	
}
