package com.michael.app.blog.guice;

import org.junit.Before;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.michael.app.blog.controller.BlogController;
import com.michael.app.blog.controller.BlogControllerFactory;
import com.michael.app.blog.repository.BlogRepository;
import com.michael.app.blog.repository.BlogRepositoryFactory;
import com.michael.app.blog.repository.mongo.BlogMongoRepository;
import com.michael.app.blog.service.BlogService;
import com.michael.app.blog.service.mongo.BlogMongoService;
import com.michael.app.blog.transaction.BlogMongoTransactionManager;
import com.michael.app.blog.transaction.TransactionManager;
import com.michael.app.blog.view.BlogView;
import com.michael.app.blog.view.swing.BlogSwingView;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;

public class BlogSwingMongoModuleTest {
	
	@Mock
	private BlogSwingView view;
	@Mock
	private ClientSession session;
	
	private Injector injector;
	private String mongoHost;
	private int mongoPort;
	private String databaseName;
	private String collectionName;


	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		mongoHost = "mongodb://";
		mongoPort = 12345;
		databaseName = "test-db)";
		collectionName = "blog-test";
		injector = Guice.createInjector(
			new BlogSwingMongoDefaultModule()
			.mongoHost(mongoHost)
			.mongoPort(mongoPort)
			.databaseName(databaseName)
			.collectionName(collectionName)
		);
	}

	@Test
	public void testConfigurationBindings() {
		assertThat(injector.getInstance(Key.get(String.class, MongoHost.class))).isEqualTo(mongoHost);
		assertThat(injector.getInstance(Key.get(Integer.class, MongoPort.class))).isEqualTo(mongoPort);
		assertThat(injector.getInstance(Key.get(String.class, MongoDbName.class))).isEqualTo(databaseName);
		assertThat(injector.getInstance(Key.get(String.class, MongoCollectionName.class))).isEqualTo(collectionName);
	}
	
	@Test
	public void testClassBindings() {
		assertThat(injector.getInstance(TransactionManager.class))
		.isInstanceOf(BlogMongoTransactionManager.class);
		assertThat(injector.getInstance(BlogRepositoryFactory.class))
			.isInstanceOf(BlogRepositoryFactory.class);
		assertThat(injector.getInstance(BlogService.class))
		.isInstanceOf(BlogMongoService.class);
		assertThat(injector.getInstance(BlogView.class))
		.isInstanceOf(BlogSwingView.class);
		assertThat(injector.getInstance(BlogControllerFactory.class))
		.isInstanceOf(BlogControllerFactory.class);
	}
	
	@Test
	public void testClientIsSingleton() {
		MongoClient client1 = injector.getInstance(MongoClient.class);
		assertThat(client1).isInstanceOf(MongoClient.class);
		MongoClient client2 = injector.getInstance(MongoClient.class);
		assertThat(client2).isInstanceOf(MongoClient.class);
		assertThat(client1).isSameAs(client2);
	}
	
	@Test
	public void testTransactionManagerIsSingleton() {
		TransactionManager transactionManager1 = injector.getInstance(TransactionManager.class);
		TransactionManager transactionManager2 = injector.getInstance(TransactionManager.class);
		assertThat(transactionManager1).isSameAs(transactionManager2);
	}
	
	@Test
	public void testServiceIsSingleton() {
		BlogService service1 = injector.getInstance(BlogService.class);
		BlogService service2 = injector.getInstance(BlogService.class);
		assertThat(service1).isSameAs(service2);
	}
	
	@Test
	public void testRepositoryFactoryCreatesCorrectImplementation() {
		BlogRepositoryFactory factory = injector.getInstance(BlogRepositoryFactory .class);
		BlogRepository repository = factory.createRepository(session);
		assertThat(repository)
			.isInstanceOf(BlogMongoRepository.class); 
	}
	
	@Test
	public void testControllerFactoryCreatesCorrectImplementation() {
		BlogControllerFactory factory = injector.getInstance(BlogControllerFactory.class);
		BlogController controller = factory.create(view);
		assertThat(controller)
			.isInstanceOf(BlogController.class); 
	}
	
	@Test
	public void testViewIsCreatedCorrectly() {
		view = injector.getInstance(BlogSwingView.class);
		assertThat(view.getBlogController()).isNotNull();
	}
}
