package com.michael.app.blog.view.swing;

import static org.assertj.core.api.Assertions.*;

import java.util.Collections;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.michael.app.blog.controller.BlogController;
import com.michael.app.blog.guice.BlogSwingMongoDefaultModule;
import com.michael.app.blog.model.Article;
import com.michael.app.blog.model.Tag;
import com.michael.app.blog.service.mongo.BlogMongoService;

import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;

@RunWith(GUITestRunner.class)
public class BlogSwingViewIT extends AssertJSwingJUnitTestCase{
	
	@SuppressWarnings({"resource"})
	private static MongoDBContainer mongoContainer =
		new MongoDBContainer(DockerImageName.parse("mongo:5"))
		.withReplicaSet();
	
	private String databaseName = "test-blog";
	private String collectionName = "test-blog";
		
	private BlogSwingView view;
	private Injector injector;

	private BlogMongoService service;

	private FrameFixture window;

	private BlogController controller;

	private String id = "000000000000000000000000";
	
	@BeforeClass
	public static void setUpBeforeClass() {
		mongoContainer.start();
	}

	@AfterClass
	public static void tearDownAfterClass() {
		mongoContainer.stop();
	}

	@Override
	public void onSetUp() {
		
		GuiActionRunner.execute(() -> {
			injector = Guice.createInjector(
				new BlogSwingMongoDefaultModule()
				.mongoHost(mongoContainer.getReplicaSetUrl())
				.databaseName(databaseName)
				.collectionName(collectionName)
			);
			service = injector.getInstance(BlogMongoService.class);
			view = injector.getInstance(BlogSwingView.class);
			for(Article article : service.getAllArticles()) {
				service.deleteArticle(article.getId());
			}
			controller = view.getBlogController();
		});
		window = new FrameFixture(robot(), view);
		window.show();
	}
	
	@Test
	public void testShowAllArticles() {
		Article article1 = service.saveArticle("Parmesan eggplants", "I like them", Collections.emptySet());
		Article article2 = service.saveArticle("Steam engine", "What's this, 1900?", Collections.emptySet());
		GuiActionRunner.execute(
			() -> controller.allArticles());
		assertThat(window.list("articleList").contents()).containsExactly(article1.toString(), article2.toString());
	}
	
	@Test
	public void testSaveButtonSuccess() {
		window.textBox("TitleTextBox").enterText("test");
		window.textBox("ContentTextBox").enterText("test");
		window.button(JButtonMatcher.withText("Save")).click();
		Article article = service.getAllArticles().get(0);
		assertThat(window.list("articleList").contents()).containsExactly(article.toString());
	}
	
	@Test
	public void testUpdateButtonSuccess() {
		Article article = service.saveArticle("Parmesan eggplants", "I like them", Set.of("cooking"));
		GuiActionRunner.execute(
			() -> controller.allArticles()
		);
		window.list("articleList").selectItem(0);
		window.textBox("TitleTextBox").setText("");
		window.textBox("TitleTextBox").enterText("test");
		window.textBox("ContentTextBox").setText("");
		window.textBox("ContentTextBox").enterText("test");
		window.button(JButtonMatcher.withText("Save")).click();
		article.setTitle("test");
		article.setContent("test");
		assertThat(window.list("articleList").contents()).containsExactly(article.toString());
	}
	
	@Test
	public void testUpdateButtonError() {
		Article article = new Article(id , "Parmesan eggplants", "I like them", Set.of(new Tag("cooking")));
		GuiActionRunner.execute(
			() -> view.getListArticlesModel().addElement(article)
		);
		window.list("articleList").selectItem(0);
		window.textBox("TitleTextBox").setText("");
		window.textBox("TitleTextBox").enterText("test");
		window.button(JButtonMatcher.withText("Save")).click();
		assertThat(window.list("articleList").contents()).containsExactly(article.toString());
		window.label("errorMessageLabel").requireText("Error in article update - Transaction failed: Article not found with ID: " + id);
	}
	
	@Test
	public void testDeleteButtonSuccess() {
		service.saveArticle("Parmesan eggplants", "I like them", Set.of("cooking"));
		GuiActionRunner.execute(
			() -> controller.allArticles()
		);
		window.list("articleList").selectItem(0);
		window.button(JButtonMatcher.withText("Delete")).click();
		assertThat(window.list("articleList").contents()).isEmpty();
	}
	
	@Test
	public void testDeleteButtonError() {
		Article article = new Article(id , "Parmesan eggplants", "I like them", Set.of(new Tag("cooking")));
		GuiActionRunner.execute(
			() -> view.getListArticlesModel().addElement(article)
		);
		window.list("articleList").selectItem(0);
		window.button(JButtonMatcher.withText("Delete")).click();
		assertThat(window.list("articleList").contents()).containsExactly(article.toString());
		window.label("errorMessageLabel").requireText("Error in article delete - Transaction failed: Article not found with ID: " + id);
	}
	
	@Test
	public void testFilterButton() {
		service.saveArticle("Parmesan eggplants", "I like them", Collections.emptySet());
		Article article2 = service.saveArticle("Steam engine", "What's this, 1900?", Set.of("trains"));
		GuiActionRunner.execute(
			() -> controller.allArticles()
		);
		window.textBox("FilterTextBox").enterText("trains");
		window.button(JButtonMatcher.withText("Filter")).click();
		assertThat(window.list("articleList").contents()).containsExactly(article2.toString());
	}
	
	@Test
	public void testResetButton() {
		Article article1 = service.saveArticle("Parmesan eggplants", "I like them", Collections.emptySet());
		Article article2 = service.saveArticle("Steam engine", "What's this, 1900?", Set.of("trains"));
		GuiActionRunner.execute(
			() -> controller.allArticlesWithTag("trains")
		);
		window.button(JButtonMatcher.withText("Reset")).click();
		assertThat(window.list("articleList").contents()).containsExactly(article1.toString(), article2.toString());
	}
	
	@Test
	public void testAddButton() {
		window.textBox("TagTextBox").enterText("cooking");
		window.button(JButtonMatcher.withText("Add")).click();
		assertThat(window.list("tagList").contents()).containsExactly(new Tag("cooking").toString());
	}
	
	@Test
	public void testRemoveButton() {
		service.saveArticle("Parmesan eggplants", "I like them", Set.of("cooking"));
		GuiActionRunner.execute(
			() -> controller.allArticles()
		);
		window.list("articleList").selectItem(0);
		window.list("tagList").selectItem(0);
		window.button(JButtonMatcher.withText("Remove")).click();
		assertThat(window.list("tagList").contents()).isEmpty();
	}
}
