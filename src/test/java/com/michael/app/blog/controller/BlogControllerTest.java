package com.michael.app.blog.controller;

import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.List;
import static java.util.Arrays.asList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.michael.app.blog.model.Article;
import com.michael.app.blog.model.Tag;
import com.michael.app.blog.repository.BlogRepository;
import com.michael.app.blog.view.BlogView;

public class BlogControllerTest {
	
	@Mock
	private BlogRepository blogRepository;
	
	@Mock
	private BlogView blogView;
	
	@InjectMocks
	BlogController controller;
	
	private AutoCloseable closeable;
	
	private int id;
	private String title;
	private String content;
	@Spy
	private Article article;
	
	
	@Before
	public void setup() {
		id = 1;
		title = "Parmesan eggplants";
		content = "I like them";
		article = new Article(id, title, "I like them");
		closeable = MockitoAnnotations.openMocks(this);
	}
	
	@After
	public void releaseMocks() throws Exception {
		closeable.close();
	}
	
	@Test
	public void testAllArticles() {
		List<Article> articles = asList(article);
		when(blogRepository.findAll()).thenReturn(articles);
		controller.allArticles();
		verify(blogRepository).findAll();
		verify(blogView).showAllArticles(articles);
	}
	
	@Test
	public void testAllArticlesWithTag() {
		Tag tag = new Tag("cooking");
		article.addTag(new Tag("cooking"));
		List<Article> articles = asList(article);
		when(blogRepository.findAllWithTag(tag)).thenReturn(articles);
		controller.allArticlesWithTag(tag);
		verify(blogRepository).findAllWithTag(tag);
		verify(blogView).showAllArticles(articles);
		article.removeTag(tag);
	}
	
	@Test
	public void testAddArticleWhenNotPresent() {
		when(blogRepository.findById(id)).thenReturn(null);
		controller.addArticle(article);
		InOrder inOrder = inOrder(blogRepository, blogView);
		inOrder.verify(blogRepository).save(article);
		inOrder.verify(blogView).articleAdded(article);
	}
	
	@Test
	public void testAddArticleWhenAlreadyPresent() {
		Article alreadyPresentArticle = new Article(id, "Parmesan eggplants", "I hate them");
		when(blogRepository.findById(id)).thenReturn(alreadyPresentArticle);
		controller.addArticle(article);
		verify(blogView).showError("An article with title Parmesan eggplants is already present!", alreadyPresentArticle);
		verifyNoMoreInteractions(ignoreStubs(blogRepository));
	}
	
	@Test
	public void testDeleteArticleWhenArticleExists() {
		when(blogRepository.findById(id)).thenReturn(article);
		controller.deleteArticle(article);
		InOrder inOrder = inOrder(blogRepository, blogView);
		inOrder.verify(blogRepository).delete(id);
		inOrder.verify(blogView).articleDeleted(article);
	}
	
	@Test
	public void testDeleteArticleWhenArticleDoesNotExists() {
		when(blogRepository.findById(id)).thenReturn(null);
		controller.deleteArticle(article);
		verify(blogView).showError("No article with title Parmesan eggplants", article);
		verifyNoMoreInteractions(ignoreStubs(blogRepository));
	}
	
	@Test
	public void testTagWhenArticleDoesNotContainTag() {
		Tag tag = new Tag("cooking");
		when(blogRepository.findById(id)).thenReturn(article);
		controller.tag(article, tag);
		InOrder inOrder = inOrder(article, blogRepository, blogView);
		inOrder.verify(article).addTag(tag);
		inOrder.verify(blogRepository).save(article);
		inOrder.verify(blogView).addedTag(article, tag);
	}
	
	@Test
	public void testTagWhenArticleDoesNotExist() { 
		Tag tag = new Tag("cooking");
		when(blogRepository.findById(id)).thenReturn(null);
		controller.tag(article, tag);
		verify(blogView).showError("Unable to tag: article does not exist", article);
		verifyNoMoreInteractions(ignoreStubs(blogRepository));
	}
	
	@Test
	public void testTagWhenArticleAlreadyContainsTag() { 
		Tag tag = new Tag("cooking");
		HashSet<Tag> tags = new HashSet<Tag>();
		tags.add(tag);
		Article alreadyTaggedArticle = new Article(id, title, content, tags);
		when(blogRepository.findById(id)).thenReturn(alreadyTaggedArticle);
		controller.tag(article, tag);
		verify(blogView).showError("Article already has tag cooking", article);
		verifyNoMoreInteractions(ignoreStubs(blogRepository));
	}
	
	@Test
	public void testUntagWhenArticleContainsTag() { 
		Tag tag = new Tag("cooking");
		HashSet<Tag> tags = new HashSet<Tag>();
		tags.add(tag);
		Article alreadyTaggedArticle = new Article(id, title, content, tags);
		when(blogRepository.findById(id)).thenReturn(alreadyTaggedArticle);
		controller.untag(article, tag);
		InOrder inOrder = inOrder(article, blogRepository, blogView);
		inOrder.verify(article).removeTag(tag);
		inOrder.verify(blogRepository).save(alreadyTaggedArticle);
		inOrder.verify(blogView).removedTag(alreadyTaggedArticle, tag);
		verifyNoMoreInteractions(ignoreStubs(blogRepository));
	}
	
	@Test
	public void testUntagWhenArticleDoesNotContainTag() { 
		Tag tag = new Tag("cooking");
		when(blogRepository.findById(id)).thenReturn(article);
		controller.untag(article, tag);
		verify(blogView).showError("Article is not tagged with cooking", article);
		verifyNoMoreInteractions(ignoreStubs(blogRepository));
	}
	
	@Test
	public void testUntagWhenArticleDoesNotExist() { 
		Tag tag = new Tag("cooking");
		when(blogRepository.findById(id)).thenReturn(null);
		controller.untag(article, tag);
		verify(blogView).showError("Unable to untag: article does not exist", article);
		verifyNoMoreInteractions(ignoreStubs(blogRepository));
	}
}
