package org.fastcatsearch.cli.command;

import java.util.List;
import java.util.TreeSet;

import org.fastcatsearch.cli.Command;
import org.fastcatsearch.cli.ConsoleSessionContext;
import org.fastcatsearch.cli.command.exception.CollectionNotDefinedException;
import org.fastcatsearch.cli.command.exception.CollectionNotFoundException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionsConfig.Collection;
import org.fastcatsearch.service.ServiceManager;

public abstract class CollectionExtractCommand extends Command {
/*
 * do not use this class directly
 * */
	private TreeSet collectionSet = new TreeSet();
	protected String ALLCOLLECTION = "_ALL_";

	protected TreeSet getCollectionList() {
//		String collectionListStr = IRSettings.getConfig(true).getString("collection.list");
		IRService irService = ServiceManager.getInstance().getService(IRService.class);
		List<Collection> collectionList = irService.getCollectionList();//collectionListStr.split(",");

		collectionSet.clear();
		
		for (Collection collection : collectionList) {
			collectionSet.add(collection.getId().toLowerCase());
		}
		
		return collectionSet;
	}
	
	protected void extractCollectionList() {
//		String collectionListStr = IRSettings.getConfig(true).getString("collection.list");
//		String[] collectionList = collectionListStr.split(",");
		
		IRService irService = ServiceManager.getInstance().getService(IRService.class);
		List<Collection> collectionList = irService.getCollectionList();

		collectionSet.clear();
		
		for (Collection collection : collectionList) {
			if ( collection.getId().trim().length() != 0 )
				collectionSet.add(collection.getId().trim().toLowerCase());
		}
	}

	protected void checkCollectionExists(String collection) throws CollectionNotFoundException {
		//그때 그때 컬렉션 정보를 가져온다. 
		extractCollectionList();
		//가져온 컬렉션 리스트에서 있는지 확인한다.
		if ( collectionSet.contains(collection.toLowerCase()) == false )
				throw new CollectionNotFoundException();
	}
	
	protected String extractCollection (ConsoleSessionContext context) throws CollectionNotDefinedException
	{
		String collection = (String)context.getAttribute(SESSION_KEY_USING_COLLECTION);
		if ( collection == null || collection.trim().length() == 0 )
			throw new CollectionNotDefinedException();
		return collection;
	}

}

