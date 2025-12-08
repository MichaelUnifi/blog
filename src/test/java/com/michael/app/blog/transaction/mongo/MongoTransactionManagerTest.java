package com.michael.app.blog.transaction.mongo;

import static org.mockito.Mockito.*;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.InOrder;
import static org.assertj.core.api.Assertions.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.michael.app.blog.repository.BlogRepository;
import com.michael.app.blog.repository.BlogRepositoryFactory;
import com.michael.app.blog.transaction.MongoTransactionManager;
import com.michael.app.blog.transaction.TransactionCode;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;

public class MongoTransactionManagerTest {

	@Mock
	private MongoClient client;

	@Mock
	private ClientSession clientSession;

	@Mock
	private BlogRepository repository;

	@Mock
	private BlogRepositoryFactory factory;

	private MongoTransactionManager manager;

	private AutoCloseable closeable;

	@Before
	public void setup() {
		closeable = MockitoAnnotations.openMocks(this);
		when(client.startSession()).thenReturn(clientSession);
		when(factory.createRepository(clientSession)).thenReturn(repository);
		manager = new MongoTransactionManager(client, factory);
	}
	
	@After
	public void tearDown() throws Exception {
		closeable.close();
	}

	@Test
	public void testDoInTransactionSuccess() {
		String expectedResult = "success";
		@SuppressWarnings("unchecked")
		TransactionCode<String> code = mock(TransactionCode.class);
		when(code.apply(repository)).thenReturn(expectedResult);
		String result = manager.doInTransaction(code);
		InOrder inOrder = inOrder(client, clientSession);
		inOrder.verify(client).startSession();
		inOrder.verify(clientSession).startTransaction();
		inOrder.verify(clientSession).commitTransaction();
		inOrder.verify(clientSession).close();
		assertThat(expectedResult).isEqualTo(result);
	}
	
	@Test
	public void testTransactionAbortsOnException() {
		@SuppressWarnings("unchecked")
		TransactionCode<String> code = mock(TransactionCode.class);
		when(code.apply(repository)).thenThrow(new RuntimeException("Database error"));
		assertThatThrownBy(() -> manager.doInTransaction(code)).isInstanceOf(RuntimeException.class).hasMessage("Transaction failed: Database error");
		verify(clientSession).startTransaction();
		verify(clientSession).abortTransaction();
		verify(clientSession).close();
	}
}