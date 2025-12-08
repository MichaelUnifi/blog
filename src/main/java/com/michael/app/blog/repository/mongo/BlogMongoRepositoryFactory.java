package com.michael.app.blog.repository.mongo;

import com.michael.app.blog.repository.BlogRepository;
import com.michael.app.blog.repository.BlogRepositoryFactory;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;

public class BlogMongoRepositoryFactory implements BlogRepositoryFactory {
	private MongoClient client;

	public BlogMongoRepositoryFactory(MongoClient client) {
		this.client = client;
	}
	
	@Override
	public BlogRepository createRepository(ClientSession session) {
		return new BlogMongoRepository(client, session);
	}

}
