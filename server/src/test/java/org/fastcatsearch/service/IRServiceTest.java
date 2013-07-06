/*
 * Copyright (c) 2013 Websquared, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     swsong - initial API and implementation
 */

package org.fastcatsearch.service;

import junit.framework.TestCase;

import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.SettingException;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.settings.IRSettings;


public class IRServiceTest extends TestCase{
	public void testConstructor() throws IRException, SettingException{
		String irHome = "testHome/";
		IRSettings.setHome(irHome);
//		IRService service = IRService.getInstance();
	}
	
	public void testGetCollectionHandler() throws IRException, SettingException{
		String irHome = "testHome/";
		String collection = "test3";
		IRSettings.setHome(irHome);
		IRService service = null;//IRService.getInstance();
		CollectionHandler h = service.collectionHandler(collection);
		h.printSegmentStatus();
	}
}
