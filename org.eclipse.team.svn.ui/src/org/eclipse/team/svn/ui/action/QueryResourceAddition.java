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

package org.eclipse.team.svn.ui.action;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.panel.local.AddToSVNPanel;

/**
 * Generic "query addition" implementation
 * 
 * @author Alexander Gurov
 */
public class QueryResourceAddition {
    protected IResourceSelector selector;
    protected Shell shell;

    public QueryResourceAddition(IResourceSelector selector, Shell shell) {
		this.selector = selector;
		this.shell = shell;
    }
    
    public IResource []queryAddition() {
		IResource []tRes = FileUtility.addOperableParents(QueryResourceAddition.getSelectedForAddition(this.selector), IStateFilter.SF_NONVERSIONED);
		if (tRes.length > 0) {
			IResource []userSelectedResources = this.selector.getSelectedResources();
		    AddToSVNPanel panel = new AddToSVNPanel(tRes, userSelectedResources);
			DefaultDialog dialog = new DefaultDialog(this.shell, panel);
			tRes = dialog.open() != 0 ? null : panel.getSelectedResources();
			if (tRes != null && tRes.length == 0) {
			    tRes = null;
			}
		}
        return tRes == null ? null : FileUtility.addOperableParents(tRes, IStateFilter.SF_NONVERSIONED);
    }
    
    /**
     * The method returns non-recursive additions in first array, recursive in the second one and root nodes in third
     * @return recursive, non-recursive additions and root nodes
     */
    public IResource [][]queryAdditionsSeparated() {
    	HashSet nonRecursive = new HashSet(Arrays.asList(this.selector.getSelectedResources(IStateFilter.SF_IGNORED)));
    	HashSet recursive = new HashSet(Arrays.asList(this.selector.getSelectedResourcesRecursive(IStateFilter.SF_NEW)));
    	
		HashSet resources = new HashSet();
		resources.addAll(nonRecursive);
		resources.addAll(recursive);
		List parents = Arrays.asList(FileUtility.getOperableParents((IResource [])resources.toArray(new IResource[resources.size()]), IStateFilter.SF_NONVERSIONED));
		nonRecursive.addAll(parents);
		resources.addAll(parents);
		
		IResource []tRes = (IResource [])resources.toArray(new IResource[resources.size()]);
		if (tRes.length > 0) {
			IResource []userSelectedResources = this.selector.getSelectedResources();
		    AddToSVNPanel panel = new AddToSVNPanel(tRes, userSelectedResources);
			DefaultDialog dialog = new DefaultDialog(shell, panel);
			if (dialog.open() != 0) {
				tRes = null;
			}
			else {
				tRes = panel.getSelectedResources();
				if (tRes.length == 0) {
				    tRes = null;
				}
				else if (panel.getNotSelectedResources().length > 0) {
					nonRecursive = new HashSet(Arrays.asList(tRes));
					nonRecursive.addAll(Arrays.asList(FileUtility.addOperableParents(tRes, IStateFilter.SF_NONVERSIONED)));
					recursive.clear();
				}
			}
		}
		if (tRes != null) {
			IResource [][]retVal = new IResource[3][];
			retVal[0] = (IResource [])nonRecursive.toArray(new IResource[nonRecursive.size()]);
			retVal[1] = (IResource [])recursive.toArray(new IResource[recursive.size()]);
			retVal[2] = FileUtility.shrinkChildNodes(tRes);
	        return retVal;
		}
		else {
			return null;
		}
    }
    
	public static IResource []getSelectedForAddition(IResourceSelector selector) {
		Set resources = new HashSet();
		// non-recursive part (ignored)
		resources.addAll(Arrays.asList(selector.getSelectedResources(IStateFilter.SF_IGNORED)));
		// all new resources that can be fetched recursively
		resources.addAll(Arrays.asList(selector.getSelectedResourcesRecursive(IStateFilter.SF_NEW)));
		return (IResource [])resources.toArray(new IResource[resources.size()]);
	}
	
}