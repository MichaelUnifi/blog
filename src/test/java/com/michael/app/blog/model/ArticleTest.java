package com.michael.app.blog.model;

import static org.assertj.core.api.Assertions.*;

import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

import com.michael.app.blog.model.Article;
import com.michael.app.blog.model.Tag;

public class ArticleTest {
	
	private int id;
	private String title;
	private String content;
	private String tagLabel;
	
	@Before
	public void setUp() {
		id = 1;
		title = "Parmesan eggplants";
		content = "I like them";
		tagLabel = "cooking";
	}
	
	@Test
	public void testSuccessfulCreation() {
		Article article = new Article(id, title, content);
		assertThat(article.getTitle()).isEqualTo(title);
		assertThat(article.getContent()).isEqualTo(content);
		assertThat(article.getTags()).isInstanceOf(HashSet.class);
	}

	@Test
	public void testNullTitleCreationFail() {
		assertThatThrownBy(() -> new Article(id, null, content)).isInstanceOf(IllegalArgumentException.class).hasMessage("Article title cannot be null!");
	}
	
	@Test
	public void testNullContentCreationFail() {
		assertThatThrownBy(() -> new Article(id, title, null)).isInstanceOf(IllegalArgumentException.class).hasMessage("Article content cannot be null!");
	}
	
	@Test
	public void testAddTagSuccess() {
		Article article = new Article(id, title, content);
		Tag tag = new Tag(tagLabel);
		article.addTag(tag);
		assertThat(article.getTags()).contains(tag);
	}
	
	@Test
	public void testAddNullTag() {
		Article article = new Article(id, title, content);
		assertThatThrownBy(() -> article.addTag(null)).isInstanceOf(IllegalArgumentException.class).hasMessage("Inserted tag cannot be null!");
	}
	
	@Test
	public void testAddDuplicateTagsDoesNotIncreaseSize() {
		Article article = new Article(id, title, content);
		Tag tag = new Tag(tagLabel);
		article.addTag(tag);
		Tag tag2 = new Tag(tagLabel);
		article.addTag(tag2);
		assertThat(article.getTags()).contains(tag2);
		assertThat(article.getTags()).hasSize(1);
	}
	
	@Test
	public void testAddTwoDifferentTags() {
		Article article = new Article(id, title, content);
		Tag tag1 = new Tag(tagLabel);
		String label2 = "tech";
		Tag tag2 = new Tag(label2);
		article.addTag(tag1);
		article.addTag(tag2);
		assertThat(article.getTags()).hasSize(2);
	}

}