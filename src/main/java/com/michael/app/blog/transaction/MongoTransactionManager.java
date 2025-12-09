package com.michael.app.blog.transaction;

import com.google.inject.Inject;
import com.michael.app.blog.repository.BlogRepository;
import com.michael.app.blog.repository.BlogRepositoryFactory;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;

public class MongoTransactionManager implements TransactionManager {

	private MongoClient client;
	private BlogRepositoryFactory repositoryFactory;
	
	@Inject
	public MongoTransactionManager(MongoClient client, BlogRepositoryFactory repositoryFactory) {
		this.client = client;
		this.repositoryFactory = repositoryFactory;
	}

	@Override
	public <T> T doInTransaction(TransactionCode<T> code) throws TransactionException{
		try (ClientSession session = client.startSession()) {
			session.startTransaction();
			try {
				BlogRepository repository = repositoryFactory.createRepository(session);
				T result = code.apply(repository);
				session.commitTransaction();
				return result;
			} catch (Exception e) {
				session.abortTransaction();
				throw new TransactionException("Transaction failed: " + e.getMessage(), e);
			}
		}
	}
}
