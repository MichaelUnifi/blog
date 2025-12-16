package com.michael.app.blog.repository.mongo;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoClient;
import com.mongodb.client.ClientSession;
import com.michael.app.blog.model.Article;
import com.michael.app.blog.model.Tag;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoClients;
import de.flapdoodle.embed.mongo.commands.ServerAddress;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.transitions.Mongod;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.TransitionWalker;

public class BlogMongoRepositoryTest {
	
	private static TransitionWalker.ReachedState<RunningMongodProcess> server;
	private static String connectionString;
	
	private static MongoClient client;
	private static ClientSession session;
	private BlogMongoRepository blogRepository;
	private MongoCollection<Document> articleCollection;
	private String id1;
	private String id2;
	private String blogDb = "blog";
	private String articleCollectionName = "blog";

	@BeforeClass
	public static void setUpServer() {
		Version.Main version = Version.Main.V8_1;
		server = Mongod.instance().transitions(version)
			.walker()
			.initState(StateID.of(RunningMongodProcess.class));
		ServerAddress addr = server.current().getServerAddress();
		connectionString = "mongodb://" + addr.getHost() + ":" + addr.getPort();
	}

	@AfterClass
	public static void shutDownServer() {
		server.close();
	}

	@Before
	public void setUp() {
		client = MongoClients.create(connectionString);
		session = client.startSession();
		blogRepository = new BlogMongoRepository(client, blogDb, articleCollectionName, session);
		MongoDatabase database = client.getDatabase(blogDb);
		database.drop();
		articleCollection = database.getCollection(articleCollectionName);
		id1 = "000000000000000000000000";
		id2 = "000000000000000000000001";
	}

	@After
	public void tearDown() {
		client.close();
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
	public void testUpdateForExistingArticle() {
		Article article = new Article(id1, "Parmesan eggplants", "I like them");
		Tag tag = new Tag("cooking");
		HashSet<Tag> tags = new HashSet<Tag>();
		tags.add(tag);
		addArticle(article);
		Article updatedArticle = new Article(id1, "Parmesan eggplants with extra cheese", "I like them a lot", tags);
		blogRepository.update(updatedArticle);
		Article foundArticle = readAllArticlesFromDatabase().get(0);
		assertThat(foundArticle.getId()).isEqualTo(updatedArticle.getId());
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