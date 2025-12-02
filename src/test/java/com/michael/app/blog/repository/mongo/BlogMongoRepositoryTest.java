package com.michael.app.blog.repository.mongo;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;
import static com.michael.app.blog.repository.mongo.BlogMongoRepository.BLOG_DB_NAME;
import static com.michael.app.blog.repository.mongo.BlogMongoRepository.ARTICLE_COLLECTION_NAME;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.Document;
import com.mongodb.client.MongoClient;
import com.michael.app.blog.model.Article;
import com.michael.app.blog.model.Tag;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoClients;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
public class BlogMongoRepositoryTest {
	
	private static MongoServer server;
	private MongoClient client;
	private BlogMongoRepository blogRepository;
	private MongoCollection<Document> articleCollection;

	@BeforeClass
	public static void setUpServer() {
		server = new MongoServer(new MemoryBackend());
	}

	@AfterClass
	public static void shutDownServer() {
		server.shutdown();
	}

	@Before
	public void setUp() {
		String connectionString = server.bindAndGetConnectionString();
		client = MongoClients.create(connectionString);
		blogRepository = new BlogMongoRepository(client);
		MongoDatabase database = client.getDatabase(BLOG_DB_NAME);
		database.drop();
		articleCollection = database.getCollection(ARTICLE_COLLECTION_NAME);
	}

	@After
	public void tearDown() {
		client.close();
		server.shutdown();
	}

	@Test
	public void testFindAllWhenDataBaseIsEmpty() {
		assertThat(blogRepository.findAll()).isEmpty();
	}
	
	@Test
	public void testFindAllWhenDataBaseContainsAtLeastOneElement() {
		Article article = new Article(1, "Parmesan eggplants", "I like them");
		addArticle(article);
		assertThat(blogRepository.findAll()).contains(article);
	}
	
	@Test
	public void testFindAllWithTagWhenDataBaseIsEmpty() {
		Tag tag = new Tag("cooking");
		assertThat(blogRepository.findAllWithTag(tag)).isEmpty();
	}
	
	@Test
	public void testFindAllWithTagWhenDataBaseElementsDoNotContainGivenTag() {
		Article article = new Article(1, "Parmesan eggplants", "I like them");
		addArticle(article);
		Tag tag = new Tag("cooking");
		assertThat(blogRepository.findAllWithTag(tag)).isEmpty();
	}
	
	@Test
	public void testFindAllWithTagWhenDataBaseElementsContainGivenTag() {
		Article article1 = new Article(1, "Parmesan eggplants", "I like them");
		Tag tag = new Tag("cooking");
		Article article2 = new Article(2, "Parmesan eggplants", "I like them");
		article1.addTag(tag);
		addArticle(article1);
		addArticle(article2);
		assertThat(blogRepository.findAllWithTag(tag)).contains(article1);
		assertThat(blogRepository.findAllWithTag(tag)).doesNotContain(article2);
	}
	
	@Test
	public void testFindByIdWhenDatabaseDoesNotContainArticle() {
		assertThat(blogRepository.findById(1)).isNull();
	}
	
	@Test
	public void testFindByIdWhenDatabaseContainsArticle() {
		Article article1 = new Article(1, "Parmesan eggplants", "I like them");
		Article article2 = new Article(2, "Parmesan eggplants", "I like them");
		addArticle(article1);
		addArticle(article2);
		assertThat(blogRepository.findById(article1.getId())).isEqualTo(article1);
	}
	
	@Test
	public void testSaveForNewArticle() {
		Article article = new Article(1, "Parmesan eggplants", "I like them");
		blogRepository.save(article);
		assertThat(readAllArticlesFromDatabase()).contains(article);
	}
	
	@Test
	public void testShouldThrowExceptionWhenSavingNullArticle() {
		assertThatThrownBy(() -> blogRepository.save(null)).isInstanceOf(IllegalArgumentException.class).hasMessage("Cannot save null article!");
	}

	@Test
	public void testSaveForExistingArticleUpdate() {
		Article article = new Article(1, "Parmesan eggplants", "I like them");
		addArticle(article);
		Article updatedArticle = new Article(1, "Parmesan eggplants", "I like them a lot!");
		blogRepository.save(updatedArticle);
		assertThat(readAllArticlesFromDatabase()).contains(updatedArticle);
		assertThat(readAllArticlesFromDatabase()).doesNotContain(article);
	}

	@Test
	public void testDeleteForExistingArticle() {
		Article article = new Article(1, "Parmesan eggplants", "I like them");
		Article articleToDelete = new Article(2, "Fettuccine Alfredo", "America thinks it's something special");
		addArticle(article);
		addArticle(articleToDelete);
		blogRepository.delete(articleToDelete.getId());
		assertThat(readAllArticlesFromDatabase()).contains(article);
		assertThat(readAllArticlesFromDatabase()).doesNotContain(articleToDelete);
	}

	@Test
	public void testDeleteForNonExistingArticle() {
		Article article = new Article(1, "Parmesan eggplants", "I like them");
		addArticle(article);
		blogRepository.delete(2);
		assertThat(readAllArticlesFromDatabase()).contains(article);
	}

	private void addArticle(Article article) {
		List<String> tagLabels = article.getTags().stream()
			.map(Tag::getLabel)
			.toList();
		articleCollection.insertOne(new Document("_id", article.getId())
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
			doc.getInteger("_id"),
			doc.getString("title"),
			doc.getString("content"),
			tags
			);
	}
}