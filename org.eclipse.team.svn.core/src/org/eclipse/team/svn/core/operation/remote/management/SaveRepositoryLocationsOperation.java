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

package org.eclipse.team.svn.core.operation.remote.management;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.file.SVNFileStorage;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;

/**
 * Save repository location changes operation
 * 
 * @author Alexander Gurov
 */
public class SaveRepositoryLocationsOperation extends AbstractActionOperation {
	public SaveRepositoryLocationsOperation() {
		super("Operation_SaveRepositoryLocations", SVNMessages.class); //$NON-NLS-1$
	}
	
	public int getOperationWeight() {
		return 0;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		SVNRemoteStorage.instance().saveConfiguration();
		if (SVNTeamPlugin.instance().isLocationsDirty()) {
			SVNFileStorage.instance().saveConfiguration();
			SVNTeamPlugin.instance().setLocationsDirty(false);
		}
	}
	
}
