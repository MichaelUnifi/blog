package com.michael.app.blog.repository.mongo;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Inject;
import com.michael.app.blog.model.Article;
import com.michael.app.blog.model.Tag;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.testcontainers.mongodb.MongoDBContainer;

public class BlogMongoRepositoryIT {

	private MongoClient client;
	private ClientSession session;
	@Inject
	private BlogMongoRepository blogRepository;
	private MongoCollection<Document> articleCollection;
	private String id1;
	private String id2;
	private static MongoDBContainer mongoContainer;
	private String collectionName = "blog";
	private String databaseName = "blog";
	
	@BeforeClass
	public static void setUpContainer() {
		mongoContainer = new MongoDBContainer("mongo:5");
		mongoContainer.start();
	}

	@AfterClass
	public static void tearDownContainer() {
		mongoContainer.stop();
	}

	@Before
	public void setUp() {
		client = MongoClients.create(mongoContainer.getConnectionString());
		session = client.startSession();
		MongoDatabase database = client.getDatabase(databaseName);
		blogRepository = new BlogMongoRepository(client, databaseName, collectionName, session);
		database.drop();
		articleCollection = database.getCollection(collectionName);
		id1 = "000000000000000000000000";
		id2 = "000000000000000000000001";
	}

	@After
	public void tearDown() {
		client.close();
		session.close();
	}

	@Test
	public void testFindAll() {
		Article article = new Article(id1, "Parmesan eggplants", "I like them");
		addArticle(article);
		assertThat(blogRepository.findAll()).contains(article);
	}
	
	@Test
	public void testFindAllWithTag() {
		Article article1 = new Article(id1, "Parmesan eggplants", "I like them");
		Tag tag = new Tag("cooking");
		Article article2 = new Article(id2, "Parmesan eggplants", "I like them");
		article1.addTag(tag);
		addArticle(article1);
		addArticle(article2);
		assertThat(blogRepository.findAllWithTag(tag)).contains(article1);
		assertThat(blogRepository.findAllWithTag(tag)).doesNotContain(article2);
	}
	
	@Test
	public void testFindById() {
		Article article1 = new Article(id1, "Parmesan eggplants", "I like them");
		Article article2 = new Article(id2, "Parmesan eggplants", "I like them");
		addArticle(article1);
		addArticle(article2);
		assertThat(blogRepository.findById(article1.getId())).isEqualTo(article1);
	}
	
	@Test
	public void testSave() {
		Article article = new Article(id1, "Parmesan eggplants", "I like them");
		Article savedArticle = blogRepository.save(article);
		assertThat(savedArticle.getId()).isNotNull();
		assertThat(savedArticle.getTitle()).isEqualTo(article.getTitle());
		assertThat(savedArticle.getContent()).isEqualTo(article.getContent());
		assertThat(savedArticle.getTags()).isEqualTo(article.getTags());
	}
	
	@Test
	public void testDelete() {
		Article article = new Article(id1, "Parmesan eggplants", "I like them");
		Article articleToDelete = new Article(id2, "Fettuccine Alfredo", "America thinks it's something special");
		addArticle(article);
		addArticle(articleToDelete);
		blogRepository.delete(articleToDelete.getId());
		assertThat(readAllArticlesFromDatabase()).contains(article);
		assertThat(readAllArticlesFromDatabase()).doesNotContain(articleToDelete);
	}
	
	private void addArticle(Article article) {
		List<String> tagLabels = article.getTags().stream()
			.map(Tag::getLabel)
			.toList();
		articleCollection.insertOne(new Document("_id", new ObjectId(article.getId()))
			.append("title", article.getTitle())
			.append("content", article.getContent())
			.append("tags", tagLabels));
	}
	
	private List<Article> readAllArticlesFromDatabase() {
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