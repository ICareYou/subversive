/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.synchronize.action;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.resource.ILocalFile;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.ui.operation.ShowHistoryViewOperation;
import org.eclipse.team.ui.synchronize.ISynchronizeModelElement;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Synchronize view "show resource history" action implementation
 * 
 * @author Alexander Gurov
 */
public class ShowOutgoingHistoryAction extends AbstractSynchronizeModelAction {

	public ShowOutgoingHistoryAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
	}

	protected boolean updateSelection(IStructuredSelection selection) {
		super.updateSelection(selection);
		if (selection.size() == 1) {
			ISynchronizeModelElement element = (ISynchronizeModelElement)selection.getFirstElement();
			ILocalResource local = SVNRemoteStorage.instance().asLocalResource(element.getResource());
			// null for change set nodes
			return local != null && (IStateFilter.SF_ONREPOSITORY.accept(local) || local instanceof ILocalFile);
		}
	    return false;
	}
	
	protected IActionOperation execute(FilteredSynchronizeModelOperation operation) {
		IResource resource = this.getSelectedResource();
		return new ShowHistoryViewOperation(resource, 0, 0);
	}

}
