package com.michael.app.blog.transaction;

public interface TransactionManager {
	<T> T doInTransaction(TransactionCode<T> code);
}
