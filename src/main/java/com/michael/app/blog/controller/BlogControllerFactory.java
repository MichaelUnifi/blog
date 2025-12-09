package com.michael.app.blog.controller;

import com.michael.app.blog.view.BlogView;

public interface BlogControllerFactory {
	BlogController create(BlogView view);
}
