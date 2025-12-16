package com.michael.app.blog.controller;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;
import java.util.Set;


import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.michael.app.blog.guice.BlogSwingMongoDefaultModule;
import com.michael.app.blog.model.Article;
import com.michael.app.blog.model.Tag;
import com.michael.app.blog.service.mongo.BlogMongoService;
import com.michael.app.blog.view.BlogView;

public class BlogControllerIT {

	@SuppressWarnings({"resource"})
	private static MongoDBContainer mongoContainer =
		new MongoDBContainer(DockerImageName.parse("mongo:5"))
		.withReplicaSet();
	
	private String databaseName = "test-blog";
	private String collectionName = "test-blog";
	
	private BlogController controller;
	
	@Mock
	private BlogView view;
	private Injector injector;

	private AutoCloseable closeable;
	private BlogMongoService service;
	String id = "000000000000000000000000";
	
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
		closeable = MockitoAnnotations.openMocks(this);
		AbstractModule overrideModule = new AbstractModule() {
			@Override
			protected void configure() {
				bind(BlogView.class).toInstance(view);
			}
		};
		injector = Guice.createInjector(
			Modules.override(new BlogSwingMongoDefaultModule()
				.mongoHost(mongoContainer.getReplicaSetUrl())
				.databaseName(databaseName)
				.collectionName(collectionName)
			).with(overrideModule)
		);
		controller = injector.getInstance(BlogControllerFactory.class).create(view);
		service = injector.getInstance(BlogMongoService.class);
		for(Article article : service.getAllArticles()) {
			service.deleteArticle(article.getId());
		}
	}
	
	@After
	public void releaseMocks() throws Exception {
		closeable.close();
	}

	@Test
	public void testAllArticles() {
		Article article = service.saveArticle("Parmesan eggplants", "I like them", Collections.emptySet());
		controller.allArticles();
		verify(view).showAllArticles(List.of(article));
	}
	
	@Test
	public void testAllArticlesWithTag() {
		String tagLabel = "cooking";
		service.saveArticle("Steam engine", "What's this, 1900?", Collections.emptySet());
		Article taggedArticle = service.saveArticle("Parmesan eggplants", "I like them", Set.of(tagLabel));
		controller.allArticlesWithTag(tagLabel);
		verify(view).showAllArticles(List.of(taggedArticle));
	}
	
	@Test
	public void testSaveArticleSuccess() {
		controller.saveArticle("Steam engine", "What's this, 1900?", Collections.emptySet());
		Article savedArticle = service.getAllArticles().get(0);
		assertThat(savedArticle.getId()).isNotNull();
		verify(view).articleAdded(savedArticle);
	}
	
	@Test
	public void testUpdateArticleSuccess() {
		String tagLabel = "trains";
		Article savedArticle = service.saveArticle("Steam engine", "What's this, 1900?", Collections.emptySet());
		controller.updateArticle(savedArticle.getId(), "Steam engine", "What's this, 1900?", Set.of(tagLabel));
		Article updatedArticle = service.getAllArticles().get(0);
		assertThat(updatedArticle.getId()).isNotNull();
		verify(view).articleUpdated(updatedArticle);
	}
	
	@Test
	public void testUpdateArticleError() {
		controller.updateArticle(id, "Steam engine", "What's this, 1900?", Collections.emptySet());
		verify(view).showError("Error in article update - Transaction failed: Article not found with ID: " + id);
	}
	
	@Test
	public void testDeleteArticleSuccess() {
		Article savedArticle = service.saveArticle("Steam engine", "What's this, 1900?", Collections.emptySet());
		controller.deleteArticle(savedArticle.getId());
		List<Article> retrievedArticles = service.getAllArticles();
		assertThat(retrievedArticles).isEmpty();
		verify(view).articleDeleted();
	}
	
	@Test
	public void testDeleteArticleError() {
		controller.deleteArticle(id);
		verify(view).showError("Error in article delete - Transaction failed: Article not found with ID: " + id);
	}
	
	@Test
	public void testTag() {
		controller.tag("cooking");
		verify(view).addedTag(new Tag("cooking"));
	}
}
