package com.michael.app.blog.repository;

import com.mongodb.client.ClientSession;

public interface BlogRepositoryFactory {
	BlogRepository createRepository(ClientSession session);
}