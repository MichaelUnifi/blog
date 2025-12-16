package com.michael.app.blog.transaction.mongo;

import static org.mockito.Mockito.*;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.michael.app.blog.model.Article;
import com.michael.app.blog.repository.BlogRepositoryFactory;
import com.michael.app.blog.repository.mongo.BlogMongoRepository;
import com.michael.app.blog.transaction.BlogMongoTransactionManager;
import com.michael.app.blog.transaction.TransactionCode;
import com.michael.app.blog.transaction.TransactionException;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

public class BlogMongoTransactionManagerTest {
	
	private MongoClient client;

	@Mock
	private BlogRepositoryFactory factory;
	@Mock
	private BlogMongoRepository blogRepository;
	private BlogMongoTransactionManager manager;
	private AutoCloseable closeable;
	private String id = "000000000000000000000000";

	@Before
	public void setup() {
		closeable = MockitoAnnotations.openMocks(this);
		client = MongoClients.create("mongodb://test");
		when(factory.createRepository(any(ClientSession.class))).thenReturn(blogRepository);
		manager = new BlogMongoTransactionManager(client, factory);
	}
	
	@After
	public void tearDown() throws Exception {
		closeable.close();
	}

	@Test
	public void testDoInTransactionCommitsOnSuccess() {
		String expectedResult = "success";
		TransactionCode<String> code = repository -> {
			repository.save(new Article(id, "title", "content"));
			return expectedResult;
		};
		String result = manager.doInTransaction(code);
		assertThat(result).isEqualTo(expectedResult);
		ArgumentCaptor<ClientSession> sessionCaptor = ArgumentCaptor.forClass(ClientSession.class);
		verify(factory).createRepository(sessionCaptor.capture());
		ClientSession capturedSession = sessionCaptor.getValue();
		assertThat(capturedSession).isNotNull();
		verify(blogRepository).save(any(Article.class));
	}
	
	@Test
	public void testTransactionRollbackWhenRepositoryThrowsException() {
		doThrow(new RuntimeException("Repository error"))
			.when(blogRepository).save(any(Article.class));
		TransactionCode<Void> code = repository -> {
			repository.save(new Article("id", "title", "content"));
			return null;
		};
		assertThatThrownBy(() -> manager.doInTransaction(code))
			.isInstanceOf(TransactionException.class)
			.hasMessageContaining("Repository error");
		verify(blogRepository).save(any(Article.class));
	}
}