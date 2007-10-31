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

package org.eclipse.team.svn.ui.operation;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.client.ISVNClientWrapper;
import org.eclipse.team.svn.core.client.PropertyData;
import org.eclipse.team.svn.core.client.Revision;
import org.eclipse.team.svn.core.operation.AbstractNonLockingOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.operation.UnreportableException;
import org.eclipse.team.svn.core.resource.IRepositoryContainer;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryRoot;
import org.eclipse.team.svn.core.resource.IRepositoryResource.Info;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;

/**
 * Load folder children. Used in asynchronous repository view refresh.
 * 
 * @author Alexander Gurov
 */
public class GetRemoteFolderChildrenOperation extends AbstractNonLockingOperation {
	protected IRepositoryContainer parent;
	protected IRepositoryResource []children;
	protected boolean sortChildren;
	protected Map externalsNames;

	public GetRemoteFolderChildrenOperation(IRepositoryContainer parent) {
		this(parent, true);
	}

	public GetRemoteFolderChildrenOperation(IRepositoryContainer parent, boolean sortChildren) {
		super("Operation.GetRemoteChildren");
		this.parent = parent;
		this.sortChildren = sortChildren;
		this.externalsNames = new HashMap();
	}

	public IRepositoryResource[] getChildren() {
		return this.children;
	}
	
	public String getExternalsName(IRepositoryResource resource) {
		return (String)this.externalsNames.get(resource);
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IRepositoryResource []tmp = this.parent.getChildren();
		
		// handle svn:externals, if present:
		Info info = this.parent.getInfo();
		if (info != null && info.hasProperties && SVNTeamPreferences.getRepositoryBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.REPOSITORY_SHOW_EXTERNALS_NAME)) {
			IRepositoryLocation location = this.parent.getRepositoryLocation();
			ISVNClientWrapper proxy = location.acquireSVNProxy();
			try {
				String remotePath = this.parent.getUrl();
				PropertyData data = proxy.propertyGet(remotePath, PropertyData.EXTERNALS, this.parent.getSelectedRevision(), this.parent.getPegRevision(), new SVNProgressMonitor(this, monitor, null));
				if (data != null) {
					String []externals = data.value.trim().split("[\\n]+"); // it seems different clients have different behaviours wrt trailing whitespace.. so trim() to be safe
					if (externals.length > 0) {
						IRepositoryResource []newTmp = new IRepositoryResource[tmp.length + externals.length];
						System.arraycopy(tmp, 0, newTmp, 0, tmp.length);
						
						for (int i = 0; i < externals.length; i++) {
							String []parts = externals[i].split("[\\t ]+");
							// 2 - name + URL
							// 3 - name + -rRevision + URL
							// 4 - name + -r Revision + URL
							if (parts.length < 2 || parts.length > 4) {
								throw new UnreportableException("Malformed external, " + parts.length + ", " + externals[i]);
							}
							String name = parts[0];  // hmm, we aren't handle the case were the name does not match the remote name, ie.   "foo  http://server/trunk/bar"..
							String url = (parts.length == 2 ? parts[1] : (parts.length == 4 ? parts[3] : parts[2])).trim(); // trim() to deal with windoze CR characters..
							
							try {
								url = SVNUtility.decodeURL(url);
							}
							catch (IllegalArgumentException ex) {
								// the URL is not encoded
							}
						    url = SVNUtility.normalizeURL(url);
							// see if we can find a matching repository location:
							newTmp[tmp.length + i] = SVNRemoteStorage.instance().asRepositoryResource(location, url, false);
							int revision = Revision.SVN_INVALID_REVNUM;
							try {
								if (parts.length == 4) {
									revision = Integer.parseInt(parts[2]);
								}
								else if (parts.length == 3) {
									revision = Integer.parseInt(parts[1].substring(2));
								}
							}
							catch (Exception ex) {
								throw new UnreportableException("Malformed external, " + parts.length + ", " + externals[i]);
							}
							if (revision != Revision.SVN_INVALID_REVNUM) {
								newTmp[tmp.length + i].setSelectedRevision(Revision.getInstance(revision));
							}
							this.externalsNames.put(newTmp[tmp.length + i], name);
						}
						
						tmp = newTmp;
					}
				}
			} finally {
				location.releaseSVNProxy(proxy);
			}
		}
		
		if (this.sortChildren) {
			FileUtility.sort(tmp, new Comparator() {
				public int compare(Object o1, Object o2) {
					IRepositoryResource first = (IRepositoryResource)o1;
					IRepositoryResource second = (IRepositoryResource)o2;
					boolean firstContainer = first instanceof IRepositoryContainer;
					boolean secondContainer = second instanceof IRepositoryContainer;
					if (firstContainer && secondContainer) {
						boolean firstRoot = first instanceof IRepositoryRoot;
						boolean secondRoot = second instanceof IRepositoryRoot;
						return firstRoot == secondRoot ? (firstRoot ? this.compareRoots(((IRepositoryRoot)first).getKind(), ((IRepositoryRoot)second).getKind()) : first.getUrl().compareTo(second.getUrl())) : (firstRoot ? -1 : 1);
					}
					return firstContainer == secondContainer ? first.getUrl().compareTo(second.getUrl()) : (firstContainer ? -1 : 1);
				}
				
				public int compareRoots(int firstKind, int secondKind) {
					return firstKind < secondKind ? -1 : 1;
				}
			});
		}
		this.children = tmp;
	}

}