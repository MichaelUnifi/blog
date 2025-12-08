package com.michael.app.blog.repository.mongo;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;
import static com.michael.app.blog.repository.mongo.BlogMongoRepository.BLOG_DB_NAME;
import static com.michael.app.blog.repository.mongo.BlogMongoRepository.ARTICLE_COLLECTION_NAME;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.assertj.core.util.Arrays;
import org.bson.Document;
import org.bson.types.ObjectId;

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
	private String id1;
	private String id2;

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
		id1 = "000000000000000000000000";
		id2 = "000000000000000000000001";
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
		Article article = new Article(id1, "Parmesan eggplants", "I like them");
		addArticle(article);
		assertThat(blogRepository.findAll()).contains(article);
	}
	
	@Test
	public void testFindAllWithTagWhenDataBaseIsEmpty() {
		Tag tag = new Tag("cooking");
		assertThat(blogRepository.findAllWithTag(tag)).isEmpty();
	}
	
	@Test
	public void testFindByIdWhenDatabaseDoesNotContainArticle() {
		assertThat(blogRepository.findById(id1)).isNull();
	}
	
	@Test
	public void testFindByIdWhenDatabaseContainsArticle() {
		Article article1 = new Article(id1, "Parmesan eggplants", "I like them");
		Article article2 = new Article(id2, "Parmesan eggplants", "I like them");
		addArticle(article1);
		addArticle(article2);
		Article foundArticle = blogRepository.findById(id1);
		assertThat(foundArticle).isEqualTo(article1);
	}
	
	@Test
	public void testFindAllWithTagWhenDataBaseElementsDoNotContainGivenTag() {
		Article article = new Article(id1, "Parmesan eggplants", "I like them");
		addArticle(article);
		Tag tag = new Tag("cooking");
		assertThat(blogRepository.findAllWithTag(tag)).isEmpty();
	}
	
	@Test
	public void testFindAllWithTagWhenDataBaseElementsContainGivenTag() {
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
	public void testSaveForNewArticle() {
		Article article = new Article(null, "Parmesan eggplants", "I like them");
		Article savedArticle = blogRepository.save(article);
		assertThat(savedArticle.getId()).isNotNull();
		assertThat(savedArticle.getTitle()).isEqualTo(article.getTitle());
		assertThat(savedArticle.getContent()).isEqualTo(article.getContent());
		assertThat(savedArticle.getTags()).isEqualTo(article.getTags());
	}
	
	@Test
	public void testShouldThrowExceptionWhenSavingNullArticle() {
		assertThatThrownBy(() -> blogRepository.save(null)).isInstanceOf(IllegalArgumentException.class).hasMessage("Cannot save null article!");
	}

	@Test
	public void testShouldThrowWhenSavingArticleWithId() {
		Article article = new Article(id1, "Parmesan eggplants", "I like them");
		addArticle(article);
		assertThatThrownBy(() -> blogRepository.save(article)).isInstanceOf(IllegalArgumentException.class).hasMessage("Article already has an id!");
	}
	
	@Test
	public void testUpdateForExistingArticle() {
		Article article = new Article(id1, "Parmesan eggplants", "I like them");
		Tag tag = new Tag("cooking");
		HashSet<Tag> tags = new HashSet<Tag>();
		tags.add(tag);
		addArticle(article);
		Article updatedArticle = new Article(id1, "Parmesan eggplants with extra cheese", "I like them a lot", tags);
		blogRepository.update(updatedArticle);
		Article foundArticle = readAllArticlesFromDatabase().get(0);
		assertThat(foundArticle.getTitle()).isEqualTo(updatedArticle.getTitle());
		assertThat(foundArticle.getContent()).isEqualTo(updatedArticle.getContent());
		assertThat(foundArticle.getTags()).isEqualTo(updatedArticle.getTags());
	}
	
	@Test
	public void testShouldThrowExceptionWhenUpdatingNullArticle() {
		assertThatThrownBy(() -> blogRepository.update(null)).isInstanceOf(IllegalArgumentException.class).hasMessage("Cannot update null article!");
	}

	@Test
	public void testShouldThrowExceptionWhenUpdatingArticleWithNullId() {
		Article article = new Article(null, "Parmesan eggplants", "I like them");
		assertThatThrownBy(() -> blogRepository.update(article)).isInstanceOf(IllegalArgumentException.class).hasMessage("Article has null id!");
	}

	@Test
	public void testDeleteForExistingArticle() {
		Article article = new Article(id1, "Parmesan eggplants", "I like them");
		Article articleToDelete = new Article(id2, "Fettuccine Alfredo", "America thinks it's something special");
		addArticle(article);
		addArticle(articleToDelete);
		blogRepository.delete(articleToDelete.getId());
		assertThat(readAllArticlesFromDatabase()).contains(article);
		assertThat(readAllArticlesFromDatabase()).doesNotContain(articleToDelete);
	}

	@Test
	public void testDeleteForNonExistingArticle() {
		Article article = new Article(id1, "Parmesan eggplants", "I like them");
		addArticle(article);
		blogRepository.delete(id2);
		assertThat(readAllArticlesFromDatabase()).contains(article);
	}
	
	@Test
	public void testShouldThrowWhenDeletingNullId() {
		assertThatThrownBy(() -> blogRepository.delete(null)).isInstanceOf(IllegalArgumentException.class).hasMessage("Cannot delete: given id is null!");
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