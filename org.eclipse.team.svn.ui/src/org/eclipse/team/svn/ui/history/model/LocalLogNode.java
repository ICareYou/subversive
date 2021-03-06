/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *    Thomas Champagne - Bug 217561 : additional date formats for label decorations
 *******************************************************************************/

package org.eclipse.team.svn.ui.history.model;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.history.data.SVNLocalFileRevision;
import org.eclipse.team.svn.ui.utility.DateFormatter;

/**
 * Local log UI node
 * 
 * @author Alexander Gurov
 */
public class LocalLogNode extends AbstractLogNode {
	protected SVNLocalFileRevision entry;
	
	public LocalLogNode(SVNLocalFileRevision entry, ILogNode parent) {
		super(parent);
		this.entry = entry;
	}
	
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IFileRevision.class)) {
			return this.entry;
		}
		return null;
	}
	
	public ILogNode[] getChildren() {
		return null;
	}

	public Object getEntity() {
		return this.entry;
	}

	public boolean requiresBoldFont(long currentRevision) {
		return this.entry.isCurrentState();
	}
	
	public ImageDescriptor getImageDescriptor() {
		return SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/local_rev.gif"); //$NON-NLS-1$
	}
	
	public String getLabel(int columnIndex, int labelType, long currentRevision) {
		switch (columnIndex) {
			case ILogNode.COLUMN_DATE: {
				return DateFormatter.formatDate(this.entry.getTimestamp());
			}
			case ILogNode.COLUMN_REVISION: {
				if (this.entry.isCurrentState()) {
					if (currentRevision != SVNRevision.INVALID_REVISION_NUMBER) {
						return SVNUIMessages.format(SVNUIMessages.LogMessagesComposite_CurrentRevision, new String [] {String.valueOf(currentRevision)});
					}
					return "*"; //$NON-NLS-1$
				}
				return ""; //$NON-NLS-1$
			}
			case ILogNode.COLUMN_COMMENT: {
				String retVal = this.entry.getComment();
				if (labelType == ILogNode.LABEL_TRIM) {
					return FileUtility.formatMultilineText(retVal);
				}
				else if (labelType == ILogNode.LABEL_FLAT) {
					return AbstractLogNode.flattenMultiLineText(retVal, " "); //$NON-NLS-1$
				}
				return retVal;
			}
		}
		return ""; //$NON-NLS-1$
	}
	
	public int getType() {
		return ILogNode.TYPE_LOCAL;
	}

	public boolean hasChildren() {
		return false;
	}
	
	public String getAuthor() {
		return ""; //$NON-NLS-1$
	}

	public int getChangesCount() {
		return 0;
	}

	public String getComment() {
		return this.entry.getComment();
	}

	public long getRevision() {
		return SVNRevision.INVALID_REVISION_NUMBER;
	}

	public long getTimeStamp() {
		return this.entry.getTimestamp();
	}
	
	public int hashCode() {
		return this.entry.hashCode();
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof LocalLogNode) {
			return this.entry.equals(((LocalLogNode)obj).entry);
		}
		return false;
	}
	
}
