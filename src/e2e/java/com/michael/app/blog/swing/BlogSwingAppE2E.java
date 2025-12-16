package com.michael.app.blog.swing;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;

import static org.assertj.core.api.Assertions.*;

import org.bson.Document;
import org.bson.types.ObjectId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.launcher.ApplicationLauncher.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import com.michael.app.blog.model.Article;
import com.michael.app.blog.model.Tag;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

@RunWith(GUITestRunner.class)
public class BlogSwingAppE2E extends AssertJSwingJUnitTestCase{ // NOSONAR
	
	@SuppressWarnings({"resource" })
	private static MongoDBContainer mongoContainer =
		new MongoDBContainer(DockerImageName.parse("mongo:5"))
		.withReplicaSet();
	
	private String dbName = "test-blog";
	private String collectionName = "test-blog";
	
	private FrameFixture window;
	
	private MongoClient mongoClient;

	private String id1 = "000000000000000000000000";
	private String id2 = "000000000000000000000001";
	
	@BeforeClass
	public static void setUpBeforeClass() {
		mongoContainer.start();
	}

	@AfterClass
	public static void tearDownAfterClass() {
		mongoContainer.stop();
	}
	
	@Override
	protected void onSetUp() {
		String containerIpAddress = mongoContainer.getHost();
		Integer mappedPort = mongoContainer.getFirstMappedPort();
		mongoClient = MongoClients.create("mongodb://" + containerIpAddress + ":" + mappedPort);
		mongoClient.getDatabase(dbName).drop();
		addTestArticleToDatabase(id1 , "Parmesan eggplants", "I like them", Set.of(new Tag("cooking")));
		addTestArticleToDatabase(id2, "Blogging is fun", "Try it now", Set.of(new Tag("blog")));
		application("com.michael.app.blog.swing.BlogSwingApp")
		.withArgs(
		"--mongo-host=" + "mongodb://" + containerIpAddress,
		"--mongo-port=" + mappedPort.toString(),
		"--db-name=" + dbName,
		"--db-collection=" + collectionName
		).start();
		window = WindowFinder.findFrame(new GenericTypeMatcher<JFrame>(JFrame.class) {
			@Override
			protected boolean isMatching(JFrame frame) {
				return "Blog View".equals(frame.getTitle()) && frame.isShowing();
			}
		}).using(robot());
	}
	
	@Test @GUITest
	public void testOnStartAllDatabaseArticlesAreShown() {
		assertThat(window.list("articleList").contents())
			.anySatisfy(e -> assertThat(e).contains("Parmesan eggplants", "I like them"))
			.anySatisfy(e -> assertThat(e).contains("Blogging is fun", "Try it now"));
	}
	
	@Test @GUITest
	public void testSaveButton() {
		window.textBox("TitleTextBox").enterText("test");
		window.textBox("ContentTextBox").enterText("test");
		window.button(JButtonMatcher.withText("Save")).click();
		assertThat(window.list("articleList").contents())
			.anySatisfy(e -> assertThat(e).contains("test"));
		assertThat(window.list("articleList").contents())
			.anySatisfy(e -> assertThat(e).contains("test"));
	}
	
	@Test @GUITest
	public void testUpdateButton() throws InterruptedException {
		window.list("articleList").selectItem(0);
		String title = window.textBox("TitleTextBox").text();
		window.textBox("TitleTextBox").setText("");
		window.textBox("TitleTextBox").enterText("test");
		window.button(JButtonMatcher.withText("Save")).click();
		assertThat(window.list("articleList").contents())
			.anySatisfy(e -> assertThat(e).contains("test"))
			.noneSatisfy(e -> assertThat(e).contains(title));
	}
	
	@Test @GUITest
	public void testUpdateButtonError() {
		window.list("articleList").selectItem(0);
		window.textBox("TitleTextBox").setText("");
		window.textBox("TitleTextBox").enterText("test");
		removeTestArticleToDatabase(id1);
		removeTestArticleToDatabase(id2);
		window.button(JButtonMatcher.withText("Save")).click();
		assertThat(window.label("errorMessageLabel").text())
			.contains("Error in article update", "Article not found with ID: ");
	}
	
	@Test @GUITest
	public void testDeleteArticleButton() {
		window.list("articleList").selectItem(0);
		String title = window.textBox("TitleTextBox").text();
		String content = window.textBox("ContentTextBox").text();
		window.button(JButtonMatcher.withText("Delete")).click();
		assertThat(window.list("articleList").contents())
			.noneSatisfy(e -> assertThat(e).contains(title, content));
	}
	
	@Test @GUITest
	public void testDeleteButtonError() {
		window.list("articleList").selectItem(0);
		removeTestArticleToDatabase(id1);
		removeTestArticleToDatabase(id2);
		window.button(JButtonMatcher.withText("Delete")).click();
		assertThat(window.label("errorMessageLabel").text())
			.contains("Error in article delete", "Article not found with ID: ");
	}
	
	@Test @GUITest
	public void testFilterButton() {
		window.textBox("FilterTextBox").enterText("cooking");
		window.button(JButtonMatcher.withText("Filter")).click();
		assertThat(window.list("articleList").contents())
			.anySatisfy(e -> assertThat(e).contains("Parmesan eggplants", "I like them"))
			.noneSatisfy(e -> assertThat(e).contains("Blogging is fun", "Try it now"));
	}
	
	@Test @GUITest
	public void testResetButton() {
		String newId = "000000000000000000000002";
		addTestArticleToDatabase(newId, "Steam engines", "Pretty cool stuff", Collections.emptySet());
		window.button(JButtonMatcher.withText("Reset")).click();
		assertThat(window.list("articleList").contents())
			.anySatisfy(e -> assertThat(e).contains("Parmesan eggplants", "I like them"))
			.anySatisfy(e -> assertThat(e).contains("Blogging is fun", "Try it now"))
			.anySatisfy(e -> assertThat(e).contains("Steam engines", "Pretty cool stuff"));
	}
	
	@Override
	protected void onTearDown() {
		mongoClient.close();
	}
	
	private Document articleToDocument(Article article) {
		List<String> tagLabels = article.getTags().stream()
			.map(Tag::getLabel)
			.toList();
		return new Document(new Document("_id", new ObjectId(article.getId()))
			.append("title", article.getTitle())
			.append("content", article.getContent())
			.append("tags", tagLabels));
	}
	
	private void addTestArticleToDatabase(String id, String title, String content, Set<Tag> tags) {
		mongoClient.getDatabase(dbName)
			.getCollection(collectionName)
			.insertOne(articleToDocument(new Article(id,title,content, tags))
		);
	}
	
	private void removeTestArticleToDatabase(String id) {
		mongoClient.getDatabase(dbName)
			.getCollection(collectionName)
			.deleteOne(new Document("_id", new ObjectId(id))
		);
	}
}
