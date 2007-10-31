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

package org.eclipse.team.svn.core.operation.local;

import java.util.StringTokenizer;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.client.Depth;
import org.eclipse.team.svn.core.client.ISVNClientWrapper;
import org.eclipse.team.svn.core.client.PropertyData;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNNullProgressMonitor;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRemoteStorage;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IResourceProvider;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;

/**
 * Add to version control operation
 * 
 * @author Alexander Gurov
 */
public class AddToSVNOperation extends AbstractWorkingCopyOperation {
	protected boolean isRecursive;
	
	public AddToSVNOperation(IResource[] resources) {
		this(resources, false);
	}
	
	public AddToSVNOperation(IResource[] resources, boolean isRecursive) {
		super("Operation.AddToSVN", resources);
		this.isRecursive = isRecursive;
	}

	public AddToSVNOperation(IResourceProvider provider) {
		this(provider, false);
	}

	public AddToSVNOperation(IResourceProvider provider, boolean isRecursive) {
		super("Operation.AddToSVN", provider);
		this.isRecursive = isRecursive;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IResource []resources = this.operableData();
		
		if (this.isRecursive) {
			this.isRecursive = !FileUtility.checkForResourcesPresenceRecursive(resources, IStateFilter.SF_IGNORED);
		}
		if (this.isRecursive) {
			resources = FileUtility.shrinkChildNodes(resources);
		}
		else {
			FileUtility.reorder(resources, true);
		}
		
		final IRemoteStorage storage = SVNRemoteStorage.instance();
		for (int i = 0; i < resources.length && !monitor.isCanceled(); i++) {
		    final IResource current = resources[i];
			IRepositoryLocation location = storage.getRepositoryLocation(current);
			final ISVNClientWrapper proxy = location.acquireSVNProxy();
			
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					AddToSVNOperation.this.doAdd(current, proxy, monitor);
				}
			}, monitor, resources.length);
			location.releaseSVNProxy(proxy);
		}
	}
	
	public static void removeFromParentIgnore(ISVNClientWrapper proxy, String parentPath, String name) throws Exception {
		PropertyData data = proxy.propertyGet(parentPath, PropertyData.IGNORE, null, null, new SVNNullProgressMonitor());
		String ignoreValue = data == null ? "" : data.value;
		
		StringTokenizer tok = new StringTokenizer(ignoreValue, "\n", true);
		ignoreValue = "";
		boolean skipToken = false;
		while (tok.hasMoreTokens()) {
		    String oneOf = tok.nextToken();
		    
			if (!oneOf.equals(name) && !skipToken) {
			    ignoreValue += oneOf;
			}
			else {
			    skipToken = !skipToken;
			}
		}
		
		if (ignoreValue.length() > 0)
		{
			proxy.propertySet(parentPath, PropertyData.IGNORE, ignoreValue, Depth.empty, false, new SVNNullProgressMonitor());
		}
		else
		{
			proxy.propertyRemove(parentPath, PropertyData.IGNORE, Depth.empty, new SVNNullProgressMonitor());
		}
	}
	
	protected void doAdd(IResource current, ISVNClientWrapper proxy, IProgressMonitor monitor) throws Exception {
		String wcPath = FileUtility.getWorkingCopyPath(current);

		AddToSVNOperation.this.writeToConsole(IConsoleStream.LEVEL_CMD, "svn add \"" + FileUtility.normalizePath(wcPath) + "\"" + (AddToSVNOperation.this.isRecursive ? "" : " -N") + "\n");
		
		IResource parent = current.getParent();
		if (parent != null) {
			AddToSVNOperation.removeFromParentIgnore(proxy, FileUtility.getWorkingCopyPath(parent), current.getName());
		}
		
		proxy.add(wcPath, Depth.infinityOrEmpty(AddToSVNOperation.this.isRecursive), false, false, new SVNProgressMonitor(AddToSVNOperation.this, monitor, null));
	}
	
}