package org.fastcatsearch.cli.command;

import java.io.IOException;
import java.util.TreeSet;

import org.fastcatsearch.cli.Command;
import org.fastcatsearch.cli.CommandException;
import org.fastcatsearch.cli.CommandResult;
import org.fastcatsearch.cli.ConsoleSessionContext;
import org.fastcatsearch.ir.config.IRSettings;

public abstract class CollectionExtractCommand extends Command {
/*
 * do not use this class directly
 * */
	private TreeSet collectionSet = new TreeSet();
	protected String ALLCOLLECTION = "_ALL_";

	protected TreeSet getCollectionList() {
		String collectionListStr = IRSettings.getConfig(true).getString("collection.list");
		String[] collectionList = collectionListStr.split(",");

		collectionSet.clear();
		
		for (String strCollection : collectionList) {
			collectionSet.add(strCollection.toLowerCase());
		}
		
		return collectionSet;
	}
	
	protected void extractCollectionList() {
		String collectionListStr = IRSettings.getConfig(true).getString("collection.list");
		String[] collectionList = collectionListStr.split(",");

		collectionSet.clear();
		
		for (String strCollection : collectionList) {
			if ( strCollection.trim().length() != 0 )
				collectionSet.add(strCollection.trim().toLowerCase());
		}
	}

	protected boolean isCollectionExists(String collection) {
		//그때 그때 컬렉션 정보를 가져온다. 
		extractCollectionList();
		//가져온 컬렉션 리스트에서 있는지 확인한다.
		return collectionSet.contains(collection.toLowerCase());
	}
	
	protected String extractCollection (ConsoleSessionContext context)
	{
		return ((String) context.getAttribute(SESSION_KEY_USING_COLLECTION));
	}
}

