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

package org.eclipse.team.svn.ui.synchronize.merge.action;

import java.text.MessageFormat;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.MergeOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.operation.local.RestoreProjectMetaOperation;
import org.eclipse.team.svn.core.operation.local.SaveProjectMetaOperation;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.operation.ClearMergeStatusesOperation;
import org.eclipse.team.svn.ui.operation.NotifyUnresolvedConflictOperation;
import org.eclipse.team.svn.ui.synchronize.action.AbstractSynchronizeModelAction;
import org.eclipse.team.svn.ui.synchronize.action.ISyncStateFilter;
import org.eclipse.team.svn.ui.synchronize.merge.MergeSubscriber;
import org.eclipse.team.svn.ui.synchronize.merge.MergeSyncInfo;

/**
 * Merge update action implementation
 * 
 * @author Alexander Gurov
 */
public class UpdateAction extends AbstractSynchronizeModelAction {
	protected boolean advancedMode;

	public UpdateAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
		this.advancedMode = false;
	}

	public UpdateAction(String text, ISynchronizePageConfiguration configuration, ISelectionProvider selectionProvider) {
		super(text, configuration, selectionProvider);
		this.advancedMode = true;
	}

	protected FastSyncInfoFilter getSyncInfoFilter() {
		return new FastSyncInfoFilter.SyncInfoDirectionFilter(new int[] {SyncInfo.INCOMING, SyncInfo.CONFLICTING}) {
            public boolean select(SyncInfo info) {
                if (super.select(info)) {
                    MergeSyncInfo sync = (MergeSyncInfo)info;
                    return !IStateFilter.SF_OBSTRUCTED.accept(sync.getLocal(), sync.getLocalResource().getStatus(), sync.getLocalResource().getChangeMask());
                }
                return false;
            }
        };
	}

	protected IActionOperation execute(FilteredSynchronizeModelOperation operation) {
		IResource []resources = operation.getSelectedResourcesRecursive(ISyncStateFilter.SF_ONREPOSITORY);
		// IStateFilter.SF_NONVERSIONED not versioned locally
		resources = FileUtility.addOperableParents(resources, IStateFilter.SF_NONVERSIONED);
		
		if (this.advancedMode) {
			String message;
			if (resources.length == 1) {
				message = SVNTeamUIPlugin.instance().getResource("UpdateAll.Message.Single");
			}
			else {
				message = SVNTeamUIPlugin.instance().getResource("UpdateAll.Message.Multi");
				message = MessageFormat.format(message, new String[] {String.valueOf(resources.length)});
			}
			final MessageDialog dlg = new MessageDialog(operation.getShell(), SVNTeamUIPlugin.instance().getResource("UpdateAll.Title"), null, message, MessageDialog.QUESTION, new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL}, 0);
			operation.getShell().getDisplay().syncExec(new Runnable() {
				public void run() {
					dlg.open();
				}
			});
			if (dlg.getReturnCode() != 0) {
				return null;
			}
		}

		MergeOperation mainOp = new MergeOperation(resources, MergeSubscriber.instance().getMergeScope().getMergeSet(), false);
		
		CompositeOperation op = new CompositeOperation(mainOp.getId());
		
		SaveProjectMetaOperation saveOp = new SaveProjectMetaOperation(resources);
		op.add(saveOp);
		op.add(mainOp);
		op.add(new RestoreProjectMetaOperation(saveOp));
		op.add(new ClearMergeStatusesOperation(mainOp));
		op.add(new RefreshResourcesOperation(mainOp));
		op.add(new NotifyUnresolvedConflictOperation(mainOp));

		return op;
	}

}