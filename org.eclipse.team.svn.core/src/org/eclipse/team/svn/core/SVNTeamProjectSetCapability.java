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

package org.eclipse.team.svn.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.team.core.ProjectSetCapability;
import org.eclipse.team.core.ProjectSetSerializationContext;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.operation.remote.CheckoutAsOperation;
import org.eclipse.team.svn.core.operation.remote.management.SaveRepositoryLocationsOperation;
import org.eclipse.team.svn.core.resource.IRemoteStorage;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;

/**
 * Class that implements serializing and deserializing of references to the SVN based
 * projects
 * 
 * @author Alexander Gurov
 */
public class SVNTeamProjectSetCapability extends ProjectSetCapability {
	protected static final String OLD_PLUGIN_INFORMATION = "InClipse_0.2.7";
	protected static final String PLUGIN_INFORMATION = "1.0.0";

	public SVNTeamProjectSetCapability() {
		super();
	}

	public String[] asReference(IProject []projects, ProjectSetSerializationContext context, IProgressMonitor monitor) throws TeamException {
		monitor.beginTask(SVNTeamPlugin.instance().getResource("Operation.ExportProjectSet"), projects.length);
		try {
			String []result = new String[projects.length];
			for (int i = 0; i < projects.length; i++) {
				result[i] = this.asReference(projects[i]);
				monitor.worked(1);
			}
			return result;
		}
		finally {
			monitor.done();
		}
	}
	
	public IProject[] addToWorkspace(String []referenceStrings, ProjectSetSerializationContext context, IProgressMonitor monitor) throws TeamException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		Map project2reference = new HashMap();
		for (int i = 0; i < referenceStrings.length; i++) {
			String name = this.getNameForReference(referenceStrings[i]);
			if (name != null) {
				project2reference.put(root.getProject(name), referenceStrings[i]);
			}
		}
		Set allProjects = project2reference.keySet();
		IProject []projects = this.confirmOverwrite(context, (IProject [])allProjects.toArray(new IProject[allProjects.size()]));

		if (projects != null && projects.length > 0) {
			final CompositeOperation op = new CompositeOperation("Operation.ImportProjectSet");
			
			op.add(new SaveRepositoryLocationsOperation());
			
			ArrayList retVal = new ArrayList();
			for (int i = 0; i < projects.length; i++) {
				String fullReference = (String)project2reference.get(projects[i]);
				IProject project = this.configureCheckoutOperation(op, projects[i], fullReference);
				if (project != null) {
					retVal.add(project);
				}
			}
			projects = (IProject [])retVal.toArray(new IProject[retVal.size()]);
			
			op.add(new RefreshResourcesOperation(projects));
			SVNTeamPlugin.instance().getOptionProvider().addProjectSetCapabilityProcessing(op);

			// already in WorkspaceModifyOperation context
			ProgressMonitorUtility.doTaskExternal(op, monitor);
		}
		
		return projects;
	}
	
	protected IProject configureCheckoutOperation(CompositeOperation op, IProject project, String fullReference) throws TeamException {
		IRemoteStorage storage = SVNRemoteStorage.instance();

		String []parts = fullReference.split(",");
		
		IRepositoryLocation location = this.getLocationForReference(storage, parts);
		IRepositoryResource resource = location.asRepositoryContainer(parts[1], true);

		if (resource != null) {
			String projectLocation = 
				project.exists() ? 
				FileUtility.getResourcePath(project).removeLastSegments(1).toString() : 
				Platform.getLocation().toString();
			CheckoutAsOperation mainOp = new CheckoutAsOperation(project.getName(), resource, projectLocation, true);
			op.add(mainOp);
			return mainOp.getProject();
		}
		else {
			return null;
		}
	}
	
	protected IRepositoryLocation getLocationForReference(IRemoteStorage storage, String []parts) {
		IRepositoryLocation location = null;
		if (parts.length > 3) {
			location = storage.newRepositoryLocation(parts[3]);
			if (storage.getRepositoryLocation(location.getId()) != null) {
				return location;
			}
		}
		else {
			IRepositoryLocation []locations = storage.getRepositoryLocations();
			Path awaitingFor = new Path(parts[1]);
			for (int i = 0; i < locations.length; i++) {
				if (new Path(locations[i].getUrl()).isPrefixOf(awaitingFor)) {
					return locations[i];
				}
			}
			location = storage.newRepositoryLocation();
		}
		if (location.getUrl() == null || location.getUrl().length() == 0) {
			location.setUrl(parts[1]);
		}
		storage.addRepositoryLocation(location);
		return location;
	}
	
	protected String getNameForReference(String fullReference) {
		String []parts = fullReference.split(",");
		if (parts.length < 3 || 
			!(parts[0].equals(SVNTeamProjectSetCapability.PLUGIN_INFORMATION) || 
			parts[0].equals(SVNTeamProjectSetCapability.OLD_PLUGIN_INFORMATION))) {
			return null;
		}
		return parts[2];
	}
	
	protected String asReference(IProject project) throws TeamException {
		IRemoteStorage storage = SVNRemoteStorage.instance();
		IRepositoryResource resource = storage.asRepositoryResource(project);
		IRepositoryLocation location = resource.getRepositoryLocation();
		
		// 1) save plugin information
		// 2) save URL
		// 3) save project name
		// non-mandatory part
		// 4) save repository location
		String fullReference = PLUGIN_INFORMATION;
		fullReference += "," + resource.getUrl();
		fullReference += "," + project.getName();
		
		fullReference += "," + storage.repositoryLocationAsReference(location);
		
		return fullReference;
	}
	
}