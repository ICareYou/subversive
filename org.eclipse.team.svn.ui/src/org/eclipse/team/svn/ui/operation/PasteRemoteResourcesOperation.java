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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.svn.core.operation.AbstractNonLockingOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.IRevisionProvider;
import org.eclipse.team.svn.core.operation.remote.AbstractCopyMoveResourcesOperation;
import org.eclipse.team.svn.core.operation.remote.CopyResourcesOperation;
import org.eclipse.team.svn.core.operation.remote.MoveResourcesOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryResourceProvider;
import org.eclipse.team.svn.ui.RemoteResourceTransfer;
import org.eclipse.team.svn.ui.RemoteResourceTransferrable;

/**
 * Paste remote resource from clipboard operation implementation
 * 
 * @author Alexander Gurov
 */
public class PasteRemoteResourcesOperation extends AbstractNonLockingOperation implements IRepositoryResourceProvider, IRevisionProvider {
	protected IRepositoryResource resource;
	protected Display display;
	protected String message;
	protected IRepositoryResource []pasted;
	protected int operationType;
	protected RevisionPair []revisionsPairs;

	public PasteRemoteResourcesOperation(IRepositoryResource resource, Display display, String message) {
		super("Operation.PasteResources");
		this.resource = resource;
		this.display = display;
		this.message = message;
		this.pasted = new IRepositoryResource[0];
	}
	
	public int getOperationType() {
		return this.operationType;
	}
	
	public IRepositoryResource []getRepositoryResources() {
		return this.pasted;
	}

	protected void runImpl(final IProgressMonitor monitor) throws Exception {
		this.revisionsPairs = new RevisionPair[0];
		final Exception []exs = new Exception[1];
		this.display.syncExec(new Runnable() {
			public void run() {
				try {
					PasteRemoteResourcesOperation.this.nonSyncImpl(monitor);
				}
				catch (Exception ex) {
					exs[0] = ex;
				}
			}
		});
		if (exs[0] != null) {
			throw exs[0];
		}
	}

	protected void nonSyncImpl(IProgressMonitor monitor) throws Exception {
		Clipboard clipboard = new Clipboard(this.display);
		try {
			final RemoteResourceTransferrable transferrable = (RemoteResourceTransferrable)clipboard.getContents(new RemoteResourceTransfer());
			if (transferrable == null || 
				(this.operationType = transferrable.getOperationType()) == RemoteResourceTransferrable.OP_NONE ||
				(this.pasted = transferrable.getResources()) == null || 
				this.pasted.length == 0) {
				return;
			}
			AbstractCopyMoveResourcesOperation copyMoveOp = 
				this.operationType == RemoteResourceTransferrable.OP_COPY ? 
				(AbstractCopyMoveResourcesOperation)new CopyResourcesOperation(this.resource, this.pasted, this.message) : 
				new MoveResourcesOperation(this.resource, this.pasted, this.message);
			copyMoveOp.setConsoleStream(this.getConsoleStream());
			copyMoveOp.run(monitor);
			this.reportStatus(copyMoveOp.getStatus());
			this.revisionsPairs = copyMoveOp.getRevisions();
			
			if (this.operationType == RemoteResourceTransferrable.OP_CUT && copyMoveOp.getExecutionState() == IActionOperation.OK) {
		        // Eclipse 3.1.0 API incompatibility fix instead of clipboard.setContents(new Object[0], new Transfer[0]);
		        //clipboard.clearContents(); - does not work for unknown reasons (when MS Office clipboard features are enabled)
		        //COM.OleSetClipboard(0); - incompatible with UNIX'like
		        clipboard.setContents(
		        	new Object[] {new RemoteResourceTransferrable(null, RemoteResourceTransferrable.OP_NONE)}, 
		        	new Transfer[] {new RemoteResourceTransfer()});
			}
		}
		finally {
			clipboard.dispose();
		}
	}

	public RevisionPair []getRevisions() {
		return this.revisionsPairs;
	}
	
}