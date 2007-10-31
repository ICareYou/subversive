/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.operation.local.property;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.client.ISVNClientWrapper;
import org.eclipse.team.svn.core.client.PropertyData;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.operation.local.AbstractWorkingCopyOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IResourceProvider;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;

/**
 * Operation to set the properties on multiple resources
 * 
 * @author Sergiy Logvin
 */
public class SetMultiPropertiesOperation extends AbstractWorkingCopyOperation {
	protected IPropertyProvider propertyProvider;
	protected int depth;
	protected IStateFilter filter;
	
	public SetMultiPropertiesOperation(IResourceProvider resourceProvider, IPropertyProvider propertyProvider, IStateFilter filter, int depth) {
		super("Operation.SetMultiProperties", resourceProvider);
		this.propertyProvider = propertyProvider;
		this.depth = depth;
		this.filter = filter != null ? filter : IStateFilter.SF_VERSIONED;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IResource []resources = this.operableData();
		
		for (int i = 0; i < resources.length && !monitor.isCanceled(); i++) {
			final IResource current = resources[i];
			
			IRepositoryLocation location = SVNRemoteStorage.instance().getRepositoryLocation(current);
			final ISVNClientWrapper proxy = location.acquireSVNProxy();
			try {
				this.protectStep(new IUnprotectedOperation() {
					public void run(final IProgressMonitor monitor) throws Exception {
						FileUtility.visitNodes(current, new IResourceVisitor() {
							public boolean visit(IResource resource) throws CoreException {
								if (monitor.isCanceled()) {
									return false;
								}
								ILocalResource local = SVNRemoteStorage.instance().asLocalResource(resource);
								if (local == null) {
									return false;
								}
								if (SetMultiPropertiesOperation.this.filter.accept(resource, local.getStatus(), local.getChangeMask())) {
									PropertyData []properties = SetMultiPropertiesOperation.this.propertyProvider.getProperties(resource);
									if (properties != null) {
										SetMultiPropertiesOperation.this.processResource(proxy, resource, properties, monitor);
									}
								}
								return SetMultiPropertiesOperation.this.filter.allowsRecursion(resource, local.getStatus(), local.getChangeMask());
							}
						}, SetMultiPropertiesOperation.this.depth);
					}
				}, monitor, resources.length);
			}
			finally {
				location.releaseSVNProxy(proxy);
			}
		}
		
	}

	protected void processResource(final ISVNClientWrapper proxy, IResource current, PropertyData []properties, IProgressMonitor monitor) {
		ProgressMonitorUtility.setTaskInfo(monitor, this, current.getFullPath().toString());
		final String wcPath = FileUtility.getWorkingCopyPath(current);
		for (int i = 0; i < properties.length && !monitor.isCanceled(); i++) {
			final PropertyData property = properties[i];
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
                	proxy.propertySet(wcPath, property.name, property.data == null ? property.value.getBytes() : property.data, false, false, new SVNProgressMonitor(SetMultiPropertiesOperation.this, monitor, null));
				}
			}, monitor, properties.length);
		}
	}
	
}