/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.dialog;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.svn.ui.SVNUIMessages;

/**
 * Unresolved conflict notification dialog
 * 
 * @author Alexander Gurov
 */
public class NotifyUnresolvedConflictDialog extends MessageDialog {

    public NotifyUnresolvedConflictDialog(Shell parentShell, String message) {
		super(parentShell, 
			SVNUIMessages.NotifyUnresolvedConflictDialog_Title, 
			null, 
			message != null ? message : SVNUIMessages.NotifyUnresolvedConflictDialog_Message,
			MessageDialog.WARNING, 
			new String[] {IDialogConstants.OK_LABEL}, 
			0);
    }

}
