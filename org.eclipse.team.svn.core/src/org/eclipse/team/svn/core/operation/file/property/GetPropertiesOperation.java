/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.operation.file.property;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.client.ISVNClientWrapper;
import org.eclipse.team.svn.core.client.PropertyData;
import org.eclipse.team.svn.core.client.Revision;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.operation.file.AbstractFileOperation;
import org.eclipse.team.svn.core.operation.file.SVNFileStorage;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;

/**
 * Get resource properties operation
 * 
 * @author Alexander Gurov
 */
public class GetPropertiesOperation extends AbstractFileOperation {
	protected PropertyData []properties;
	protected Revision revision;
	
	public GetPropertiesOperation(File file) {
		this(file, Revision.WORKING);
	}

	public GetPropertiesOperation(File file, Revision revision) {
		super("Operation.GetPropertiesFile", new File[] {file});
		this.revision = revision;
	}

	public PropertyData []getProperties() {
		return this.properties;
	}
	
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		File file = this.operableData()[0];
		IRepositoryResource remote = SVNFileStorage.instance().asRepositoryResource(file, false);
		IRepositoryLocation location = remote.getRepositoryLocation();
		ISVNClientWrapper proxy = location.acquireSVNProxy();
		try {
			this.properties = proxy.properties(file.getAbsolutePath(), this.revision, null, new SVNProgressMonitor(this, monitor, null));
		}
		finally {
			location.releaseSVNProxy(proxy);
		}
	}

}