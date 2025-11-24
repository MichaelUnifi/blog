package com.michael.app.blog.model;

import java.util.Objects;

public class Tag {
	
	private String label;

	public Tag(String label) {
		if(label == null) throw new IllegalArgumentException("Tag label cannot be null!");
		if(label.equals("")) throw new IllegalArgumentException("Tag label cannot be an empty string!");
		label = label.toLowerCase();
		this.label = label;
	}

	public String getLabel() {
		return label;
	}


	@Override
	public int hashCode() {
		return Objects.hash(label);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tag other = (Tag) obj;
		return Objects.equals(label, other.label);
	}

}

