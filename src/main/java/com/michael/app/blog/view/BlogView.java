package com.michael.app.blog.view;

import java.util.List;

import com.michael.app.blog.model.Article;
import com.michael.app.blog.model.Tag;

public interface BlogView {

	void showAllArticles(List<Article> all);

	void articleAdded(Article article);

	void showError(String string, Article foundArticle);

	void articleDeleted(Article article);

	void addedTag(Article article, Tag tag);

	void removedTag(Article article, Tag tag);

}
