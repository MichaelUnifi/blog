package com.michael.app.blog;

import java.awt.EventQueue;

import org.apache.log4j.Logger;

import com.michael.app.blog.view.swing.BlogSwingView;

public class BlogSwingApp {
	
	private static final Logger logger = Logger.getLogger(BlogSwingApp.class);
	
	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			try {
				BlogSwingView frame = new BlogSwingView();
				frame.setVisible(true);
			} catch (Exception e) {
				logger.error("error: ", e);
			}
		});
	}
}
