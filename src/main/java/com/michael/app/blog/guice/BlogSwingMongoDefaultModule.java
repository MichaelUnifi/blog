package com.michael.app.blog.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.michael.app.blog.repository.BlogRepository;
import com.michael.app.blog.repository.BlogRepositoryFactory;
import com.michael.app.blog.repository.mongo.BlogMongoRepository;
import com.michael.app.blog.repository.mongo.BlogMongoRepositoryFactory;
import com.michael.app.blog.service.BlogMongoService;
import com.michael.app.blog.service.BlogService;
import com.michael.app.blog.transaction.MongoTransactionManager;
import com.michael.app.blog.transaction.TransactionManager;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

public class BlogSwingMongoDefaultModule  extends AbstractModule {
	
	private String mongoHost = "localhost";
	private int mongoPort = 27017;
	private String databaseName = "blog";
	private String collectionName = "blog";

	@Override
	protected void configure() {
		bind(String.class).annotatedWith(MongoHost.class).toInstance(mongoHost);
		bind(Integer.class).annotatedWith(MongoPort.class).toInstance(mongoPort);
		bind(String.class).annotatedWith(MongoDbName.class).toInstance(databaseName);
		bind(String.class).annotatedWith(MongoCollectionName.class).toInstance(collectionName);
		bind(BlogRepositoryFactory.class).to(BlogMongoRepositoryFactory.class);
		bind(TransactionManager.class).to(MongoTransactionManager.class);
		bind(BlogService.class).to(BlogMongoService.class);
	}
	
	public BlogSwingMongoDefaultModule mongoHost(String mongoHost) {
		this.mongoHost = mongoHost;
		return this;
	}
	
	public BlogSwingMongoDefaultModule mongoPort(int mongoPort) {
		this.mongoPort = mongoPort;
		return this;
	}
	
	public BlogSwingMongoDefaultModule databaseName(String databaseName) {
		this.databaseName = databaseName;
		return this;
	}
	
	public BlogSwingMongoDefaultModule collectionName(String collectionName) {
		this.collectionName = collectionName;
		return this;
	}
	
	@Provides
	@Singleton
	MongoClient mongoClient(@MongoHost String host, @MongoPort int port) {
		return MongoClients.create(host + ":" + port);
	}
}
