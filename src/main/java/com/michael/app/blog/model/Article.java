package com.michael.app.blog.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


public class Article {
	private String id;
	private String title;
	private String content;
	private Set<Tag> tags;
	
	public Article(String id, String title, String content, Set<Tag> tags) {
		super();
		this.id = id;
		if(title == null) throw new IllegalArgumentException("Article title cannot be null!");
		if(title.trim().equals("")) throw new IllegalArgumentException("Article title cannot be an empty string!");
		if(content == null) throw new IllegalArgumentException("Article content cannot be null!");
		if(content.trim().equals("")) throw new IllegalArgumentException("Article content cannot be an empty string!");
		this.setTitle(title);
		this.setContent(content);
		this.tags = tags;
	}
	
	public Article(String id, String title, String content) {
		this(id, title, content, new HashSet<>());
	}

	public String getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Set<Tag> getTags() {
		return tags;
	}
	
	public void addTag(Tag tag) {
		if(tag == null) throw new IllegalArgumentException("Inserted tag cannot be null!");
		tags.add(tag);
	}
	
	public void removeTag(Tag tag) {
		tags.remove(tag);
	}

	@Override
	public int hashCode() {
		return Objects.hash(content, id, tags, title);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Article other = (Article) obj;
		return Objects.equals(content, other.content) && id.equals(other.id) && Objects.equals(tags, other.tags)
				&& Objects.equals(title, other.title);
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "Article [title=" + title + ", content=" + content + "]";
	}
}
