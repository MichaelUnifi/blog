package com.michael.app.blog.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.michael.app.blog.model.Article;
import com.michael.app.blog.model.Tag;
import com.michael.app.blog.repository.BlogRepository;
import com.michael.app.blog.transaction.TransactionCode;
import com.michael.app.blog.transaction.TransactionManager;

public class BlogMongoServiceTest {

	@Mock
	private TransactionManager transactionManager;
	
	@Mock
	private BlogRepository repository;
	
	private BlogService service;
	
	private AutoCloseable closeable;

	private Article article;

	private String id1;
	private String id2;

	private Set<String> tagLabels;
	private Set<Tag> tags;
	
	@Before
	public void setup() {
		closeable = MockitoAnnotations.openMocks(this);
		when(transactionManager.doInTransaction(any())).thenAnswer(answer -> {
			TransactionCode<?> code = answer.getArgument(0);
			return code.apply(repository);
		});
		service = new BlogMongoService(transactionManager);
		id1 = "000000000000000000000000";
		id2 = "000000000000000000000001";
		tagLabels = new HashSet<String>();
		tags = new HashSet<Tag>();
		article = new Article(id1, "Parmesan eggplants", "I like them", tags);
	}
	
	@After
	public void tearDown() throws Exception {
		closeable.close();
	}
	
	@Test
	public void testGetAllArticles() {
		Article article2 = new Article(id2, "Fettuccine Alfredo", "America thinks it's something special");
		List<Article> articles = Arrays.asList(article, article2);
		when(repository.findAll()).thenReturn(articles);
		List<Article> foundArticles = service.getAllArticles();
		assertThat(foundArticles).isEqualTo(articles);
		verify(transactionManager).doInTransaction(any());
		verify(repository).findAll();
	}
	
	@Test
	public void testGetArticlesByTagWhenArticleWithTagExists() {
		Tag tag = new Tag("cooking");
		when(repository.findAllWithTag(tag)).thenReturn(Arrays.asList(article));
		List<Article> foundArticles = service.getArticlesByTag("cooking");
		assertThat(foundArticles).contains(article);
		verify(transactionManager).doInTransaction(any());
		verify(repository).findAllWithTag(tag);
	}
	
	@Test
	public void testSaveArticleWhenArticleDoesNotExist() {
		ArgumentCaptor<Article> articleCaptor = ArgumentCaptor.forClass(Article.class);
		tagLabels.add("cooking");
		tags.add(new Tag("cooking"));
		when(repository.save(any())).thenReturn(new Article(id1, "Parmesan eggplants", "I like them", tags));
		Article savedArticle = service.saveArticle("Parmesan eggplants", "I like them", tagLabels);
		verify(repository, times(1)).save(articleCaptor.capture());
		Article createdArticle = articleCaptor.getValue();
		verify(transactionManager).doInTransaction(any());
		assertThat(createdArticle.getTitle()).isEqualTo(savedArticle.getTitle());
		assertThat(createdArticle.getContent()).isEqualTo(savedArticle.getContent());
		assertThat(createdArticle.getTags()).isEqualTo(savedArticle.getTags());
	}
	
	@Test
	public void testShouldThrowExceptionWhenSavingArticleWithInvalidFormat() {
		assertThatThrownBy(() -> service.saveArticle("", "", tagLabels))
			.isInstanceOf(IllegalArgumentException.class);
		verifyNoMoreInteractions(ignoreStubs(repository));
	}
	
	@Test
	public void testShouldThrowExceptionWhenSavingArticleWithInvalidTagLabels() {
		tagLabels.add("");
		assertThatThrownBy(() -> service.saveArticle("Parmesan eggplants", "I like them", tagLabels))
			.isInstanceOf(IllegalArgumentException.class);
		verifyNoMoreInteractions(ignoreStubs(repository));
	}
	
	@Test
	public void testUpdateWhenArticleAlreadyExists() {
		when(repository.findById(id1)).thenReturn(article);
		ArgumentCaptor<Article> articleCaptor = ArgumentCaptor.forClass(Article.class);
		tagLabels.add("cooking");
		Article updatedArticle = service.updateArticle(id1, "Parmesan eggplants with extra cheese", "I like them a lot", tagLabels);
		verify(repository).findById(id1);
		verify(repository).update(articleCaptor.capture());
		assertThat(articleCaptor.getValue()).isEqualTo(updatedArticle);
	}
	
	@Test
	public void testShouldThrowExceptionWhenUpdatedArticleDoesNotExist() {
		when(repository.findById(id1)).thenReturn(null);
		assertThatThrownBy(() -> service.updateArticle(id1, "Parmesan eggplants", "I like them", tagLabels))
			.isInstanceOf(ArticleNotFoundException.class).hasMessage("Article not found with ID: " + id1);
	}
	
	@Test
	public void testShouldThrowExceptionWhenUpdatingArticleWithInvalidFormat() {
		when(repository.findById(id1)).thenReturn(article);
		assertThatThrownBy(() -> service.updateArticle(id1, "", "", tagLabels))
			.isInstanceOf(IllegalArgumentException.class);
		verify(repository).findById(id1);
		verifyNoMoreInteractions(ignoreStubs(repository));
	}
	
	@Test
	public void testShouldThrowExceptionWhenUpdatingArticleWithInvalidTagLabels() {
		tagLabels.add("");
		when(repository.findById(id1)).thenReturn(article);
		assertThatThrownBy(() -> service.updateArticle(id1, "Parmesan eggplants", "I like them", tagLabels))
			.isInstanceOf(IllegalArgumentException.class);
		verify(repository).findById(id1);
		verifyNoMoreInteractions(ignoreStubs(repository));
	}
	
	@Test
	public void testDeleteWhenArticleExists() {
		when(repository.findById(id1)).thenReturn(article);
		service.deleteArticle(id1);
		verify(repository).findById(id1);
		verify(repository).delete(id1);
		verifyNoMoreInteractions(ignoreStubs(repository));
	}
	
	@Test
	public void testShouldThrowExceptionWhenDeletingNonExistingArticle() {
		when(repository.findById(id1)).thenReturn(null);
		assertThatThrownBy(() -> service.deleteArticle(id1))
			.isInstanceOf(ArticleNotFoundException.class)
			.hasMessage("Article not found with ID: " + id1);
	}
}
