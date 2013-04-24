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

package org.fastcatsearch.module;

public class ModuleException extends RuntimeException {
	public ModuleException(String message) {
		super(message);
	}

	public ModuleException(String message, Throwable cause) {
		super(message, cause);
	}

	public ModuleException(Throwable cause) {
		super(cause);
	}
}
