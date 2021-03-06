/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.tests.core.workflow;

import java.util.StringTokenizer;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNProperty;
import org.eclipse.team.svn.core.connector.SVNProperty.BuiltIn;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.operation.local.AddToSVNIgnoreOperation;
import org.eclipse.team.svn.core.operation.local.AddToSVNOperation;
import org.eclipse.team.svn.core.resource.IRemoteStorage;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.tests.core.AbstractOperationTestCase;
import org.eclipse.team.svn.tests.core.AddOperationTest;
import org.eclipse.team.svn.tests.core.CommitOperationTest;
import org.eclipse.team.svn.tests.core.ShareNewProjectOperationTest;
import org.eclipse.team.svn.tests.core.TestWorkflow;

/**
 * Reproducing steps, which are described in PLC-314 defect (Add to SVN incorrectly 
 * interact with svn:ignore) 
 *
 * @author Sergiy Logvin
 */
public class PLC314Test extends TestWorkflow {
    public void testPLC366() {
        new ShareNewProjectOperationTest() {}.testOperation();
        new AddOperationTest() {}.testOperation();
        new CommitOperationTest() {}.testOperation();
        new AbstractOperationTestCase() {
            protected IActionOperation getOperation() {
                return new AbstractLockingTestOperation("PLC314Test") {
                    protected void runImpl(IProgressMonitor monitor) throws Exception {                        
                        FileUtility.copyAll(getFirstProject().getFolder("src").getLocation().toFile(), getSecondProject().getFolder("web").getLocation().toFile(), monitor);
                        IResource[] ignoreResource = new IResource[] {getFirstProject().getFile("src/web"), getFirstProject().getFile("src/web/site.css"), getFirstProject().getFile("src/web/site.xsl")};
                        new AddToSVNIgnoreOperation(ignoreResource, IRemoteStorage.IGNORE_NAME, "").run(monitor);
                        new AddToSVNOperation(new IResource[] {getFirstProject().getFile("src/web/site.css")}).run(monitor);
                        SVNRemoteStorage storage = SVNRemoteStorage.instance(); 
                        IResource current = getFirstProject().getFile("src/web/site.css");                        
                        IResource parent = current.getParent();
                		String name = current.getName();
                		IRepositoryLocation location = storage.getRepositoryLocation(parent);
                		ISVNConnector proxy = location.acquireSVNProxy();

                		SVNProperty data = null;
                		try {
                    		data = proxy.getProperty(new SVNEntryRevisionReference(FileUtility.getWorkingCopyPath(parent)), BuiltIn.IGNORE, null, new SVNProgressMonitor(this, monitor, null));
                		}
                		finally {
                		    location.releaseSVNProxy(proxy);
                		}
                		
                		String ignoreValue = data == null ? "" : data.value;
                		StringTokenizer tok = new StringTokenizer(ignoreValue, "\n", true);
                		while (tok.hasMoreTokens()) {
                		    String oneOf = tok.nextToken();                		    
                			if (oneOf.equals(name)) {
                			    assertTrue("Name of added to SVN resource was not deleted (PLC314Test)", false);              			    
                			}                			
                		}
                    };
                };
            }            
        }.testOperation();
    }
}

