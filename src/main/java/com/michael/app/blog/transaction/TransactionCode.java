package com.michael.app.blog.transaction;
import java.util.function.Function;

import com.michael.app.blog.repository.BlogRepository;

@FunctionalInterface
public interface TransactionCode<T> extends Function<BlogRepository, T>{

}
