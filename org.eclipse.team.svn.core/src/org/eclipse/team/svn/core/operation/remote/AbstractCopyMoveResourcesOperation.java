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

package org.eclipse.team.svn.core.operation.remote;

import java.text.MessageFormat;
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.client.ISVNClientWrapper;
import org.eclipse.team.svn.core.client.Notify2;
import org.eclipse.team.svn.core.client.NotifyInformation;
import org.eclipse.team.svn.core.operation.IConsoleStream;
import org.eclipse.team.svn.core.operation.IRevisionProvider;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.SVNUtility;

/**
 * Base implementation for copy and move repository resources operations
 * 
 * @author Alexander Gurov
 */
public abstract class AbstractCopyMoveResourcesOperation extends AbstractRepositoryOperation implements IRevisionProvider {
	protected IRepositoryResource destinationResource;
	protected String message;
	protected ArrayList revisionsPairs;
	
	public AbstractCopyMoveResourcesOperation(String operationName, IRepositoryResource destinationResource, IRepositoryResource []selectedResources, String message) {
		super(operationName, selectedResources);
		this.destinationResource = destinationResource;
		this.message = message;
	}
	
	public RevisionPair []getRevisions() {
		return this.revisionsPairs == null ? null : (RevisionPair [])this.revisionsPairs.toArray(new RevisionPair[this.revisionsPairs.size()]);
	}
	
	protected void runImpl(final IProgressMonitor monitor) throws Exception {
		this.revisionsPairs = new ArrayList();
		final String dstUrl = this.destinationResource.getUrl();
		IRepositoryResource []selectedResources = this.operableData();
		for (int i = 0; i < selectedResources.length && !monitor.isCanceled(); i++) {
			final IRepositoryResource current = selectedResources[i];
			final IRepositoryLocation location = current.getRepositoryLocation();
			final ISVNClientWrapper proxy = location.acquireSVNProxy();
			Notify2 notify = new Notify2() {
				public void onNotify(NotifyInformation info) {
					String []paths = AbstractCopyMoveResourcesOperation.this.getRevisionPaths(current.getUrl(), dstUrl + "/" + current.getName());
					AbstractCopyMoveResourcesOperation.this.revisionsPairs.add(new RevisionPair(info.revision, paths, location));
					String message = SVNTeamPlugin.instance().getResource("Console.CommittedRevision");
					AbstractCopyMoveResourcesOperation.this.writeToConsole(IConsoleStream.LEVEL_OK, MessageFormat.format(message, new String[] {String.valueOf(info.revision)}));
				}
			};
			SVNUtility.addSVNNotifyListener(proxy, notify);
			
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					AbstractCopyMoveResourcesOperation.this.processEntry(proxy, SVNUtility.encodeURL(current.getUrl()), SVNUtility.encodeURL(dstUrl + "/" + current.getName()), current, monitor);
				}
			}, monitor, selectedResources.length);
			
			SVNUtility.removeSVNNotifyListener(proxy, notify);
			location.releaseSVNProxy(proxy);
		}
	}

	protected abstract String []getRevisionPaths(String srcUrl, String dstUrl);
	protected abstract void processEntry(ISVNClientWrapper proxy, String sourceUrl, String destinationUrl, IRepositoryResource current, IProgressMonitor monitor) throws Exception;
}