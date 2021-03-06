/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.operation;

/**
 * The exception should be ignored by mail reporter
 * 
 * @author Alexander Gurov
 */
public class UnreportableException extends RuntimeException {
	private static final long serialVersionUID = 1428755738425428674L;
	
	public UnreportableException() {
		super();
	}

	public UnreportableException(String message) {
		super(message);
	}

	public UnreportableException(Throwable cause) {
		super(cause);
	}

	public UnreportableException(String message, Throwable cause) {
		super(message, cause);
	}

}
