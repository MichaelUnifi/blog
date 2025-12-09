package com.michael.app.blog.controller;

import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.assertj.core.api.Assertions.*;
import org.mockito.Spy;

import com.michael.app.blog.model.Article;
import com.michael.app.blog.model.Tag;
import com.michael.app.blog.service.BlogService;
import com.michael.app.blog.view.BlogView;

public class BlogControllerTest {

	@Mock
	private BlogService blogService;
	
	@Mock
	private BlogView blogView;
	
	@InjectMocks
	BlogController controller;
	
	private AutoCloseable closeable;
	
	private String id;
	private String title;
	private String content;
	
	@Spy
	private Article article;

	private Set<String>  tagLabels;
	
	@Before
	public void setup() {
		id = "000000000000000000000000";
		title = "Parmesan eggplants";
		content = "I like them";
		tagLabels = new HashSet<String>();
		article = new Article(id, title, content);
		closeable = MockitoAnnotations.openMocks(this);
	}
	
	@After
	public void releaseMocks() throws Exception {
		closeable.close();
	}
	
	@Test
	public void testAllArticles() {
		List<Article> articles = asList(article);
		when(blogService.getAllArticles()).thenReturn(articles);
		controller.allArticles();
		verify(blogService).getAllArticles();
		verify(blogView).showAllArticles(articles);
	}
	
	@Test
	public void testAllArticlesWithTag() {
		String tagLabel = "cooking";
		article.addTag(new Tag("cooking"));
		List<Article> articles = asList(article);
		when(blogService.getArticlesByTag(tagLabel)).thenReturn(articles);
		controller.allArticlesWithTag(tagLabel);
		verify(blogService).getArticlesByTag(tagLabel);
		verify(blogView).showAllArticles(articles);
	}
	
	@Test
	public void testSaveArticleSuccess() {
		when(blogService.saveArticle(title, content, tagLabels)).thenReturn(article);
		controller.saveArticle(title, content, tagLabels);
		InOrder inOrder = inOrder(blogService, blogView);
		inOrder.verify(blogService).saveArticle(title, content, tagLabels);
		inOrder.verify(blogView).articleAdded(article);
	}
	
	@Test
	public void testSaveArticleShowsErrorWhenCatchingException() {
		when(blogService.saveArticle("", content, tagLabels)).thenThrow(new IllegalArgumentException("Article title cannot be empty!"));
		controller.saveArticle("", content, tagLabels);
		verify(blogView).showError("Error in article save - Article title cannot be empty!");
		verifyNoMoreInteractions(ignoreStubs(blogService, blogView));
	}
	
	@Test
	public void testUpdateArticleSuccess() {
		Article updatedArticle = new Article(id, "Parmesan eggplants", "I like them now!");
		when(blogService.updateArticle(id, "Parmesan eggplants", "I like them now!", tagLabels)).thenReturn(updatedArticle);
		controller.updateArticle(id, "Parmesan eggplants", "I like them now!", tagLabels);
		InOrder inOrder = inOrder(blogService, blogView);
		inOrder.verify(blogService).updateArticle(id, "Parmesan eggplants", "I like them now!", tagLabels);
		inOrder.verify(blogView).articleUpdated(updatedArticle);
	}
	
	@Test
	public void testUpdateArticleShowsErrorWhenCatchingException() {
		doThrow(new IllegalArgumentException("Article title cannot be null!")).when(blogService).updateArticle(id, "", content, tagLabels);
		controller.updateArticle(id, "", content, tagLabels);
		verify(blogView).showError("Error in article update - Article title cannot be null!");
		verifyNoMoreInteractions(ignoreStubs(blogService));
	}
	
	@Test
	public void testDeleteArticleWhenArticleExists() {
		controller.deleteArticle(id);
		InOrder inOrder = inOrder(blogService, blogView);
		inOrder.verify(blogService).deleteArticle(id);
		inOrder.verify(blogView).articleDeleted();
		verifyNoMoreInteractions(blogService);
		verifyNoMoreInteractions(blogView);
	}
	
	@Test
	public void testDeleteArticleShowsErrorWhenCatchingException() {
		doThrow(new RuntimeException("Article does not exist!")).when(blogService).deleteArticle(id);
		controller.deleteArticle(id);
		verify(blogView).showError("Error in article delete - Article does not exist!");
		verifyNoMoreInteractions(ignoreStubs(blogService));
	}
	
	@Test
	public void testTagSuccess() {
		ArgumentCaptor<Tag> tagCaptor = ArgumentCaptor.forClass(Tag.class);
		controller.tag("COoking");
		verify(blogView).addedTag(tagCaptor.capture());
		assertThat(tagCaptor.getValue()).isEqualTo(new Tag("cooking"));
	}
	
	@Test
	public void testShouldShowErrorWhenTaggingWithInvalidFormat() {
		controller.tag("");
		verify(blogView).showError("Tag label cannot be blank!");
	}
}
