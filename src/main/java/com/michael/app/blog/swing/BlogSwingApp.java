package com.michael.app.blog.swing;

import java.awt.EventQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Guice;
import com.michael.app.blog.guice.BlogSwingMongoDefaultModule;
import com.michael.app.blog.view.swing.BlogSwingView;

import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(mixinStandardHelpOptions = true)
public class BlogSwingApp implements Callable<Void>{
	
	@Option(names = { "--mongo-host" }, description = "MongoDB host address")
	private String mongoHost = "mongodb://localhost";
	
	@Option(names = { "--mongo-port" }, description = "MongoDB host port")
	private int mongoPort = 27017;
	
	@Option(names = { "--db-name" }, description = "Database name")
	private String databaseName = "blog";
	
	@Option(names = { "--db-collection" }, description = "Collection name")
	private String collectionName = "articles";
		
	public static void main(String[] args) {
		new CommandLine(new BlogSwingApp()).execute(args);
	}
	
	@Override
	public Void call() throws Exception{
		EventQueue.invokeLater(() -> {
			try {
				Guice.createInjector(new BlogSwingMongoDefaultModule()
						.mongoHost(mongoHost)
						.mongoPort(mongoPort)
						.databaseName(databaseName)
						.collectionName(collectionName)
					).getInstance(BlogSwingView.class)
					.start();
			} catch (Exception e) {
				Logger.getLogger(getClass().getName())
				.log(Level.SEVERE, "Exception", e);
			}
		});
		return null;
	}
}