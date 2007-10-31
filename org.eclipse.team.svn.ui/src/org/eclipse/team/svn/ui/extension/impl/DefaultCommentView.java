/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrej Zachar - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.extension.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.extension.factory.ICommentView;
import org.eclipse.team.svn.ui.panel.local.CommitPanel;
import org.eclipse.team.svn.ui.panel.local.CommitPanel.GetBugTraqPropertiesModelOperation;
import org.eclipse.team.svn.ui.properties.bugtraq.BugtraqModel;
import org.eclipse.team.svn.ui.properties.bugtraq.IssueList;
import org.eclipse.team.svn.ui.properties.bugtraq.IssueList.Issue;

/**
 * Default implementation of history comment
 * 
 * @author Andrej Zachar
 */
public class DefaultCommentView implements ICommentView {
	protected StyledText multilineComment;
	protected Cursor handCursor;
	protected Cursor busyCursor;
	
	protected Color black;
	protected Color blue;
	
	protected boolean mouseDown;
	protected boolean dragEvent;
	
	protected BugtraqModel model;
	protected IssueList linkList = new IssueList();

	public void createCommentView(Composite parent) {
		this.createCommentView(parent, SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.WRAP);
	}
	
	public void createCommentView(Composite parent, int style) {
		this.multilineComment = new StyledText(parent, style);
		this.multilineComment.setEditable(false);
		// set system color
		this.multilineComment.setBackground(this.multilineComment.getBackground());
		
		this.handCursor = new Cursor(parent.getDisplay(), SWT.CURSOR_HAND);
		this.busyCursor = new Cursor(parent.getDisplay(), SWT.CURSOR_WAIT);
		
		this.blue = new Color(parent.getDisplay(), 0, 0, 192);
		this.black = new Color(parent.getDisplay(), 2, 200, 30);
		
		parent.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				DefaultCommentView.this.blue.dispose();
				DefaultCommentView.this.black.dispose();			
			}
		});
		
		this.multilineComment.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				if (e.button != 1) {
					return;
				}
				DefaultCommentView.this.mouseDown = true;
			}
			public void mouseUp(MouseEvent e) {
				DefaultCommentView.this.mouseDown = false;
				StyledText text = (StyledText)e.widget;
				int offset = text.getCaretOffset();
				if (DefaultCommentView.this.dragEvent) {
					DefaultCommentView.this.dragEvent = false;
					if (DefaultCommentView.this.linkList.isIssueAt(offset)) {
						text.setCursor(DefaultCommentView.this.handCursor);
						text.getStyleRangeAtOffset(offset).background = DefaultCommentView.this.blue;
					}
				}
				else {
					if (DefaultCommentView.this.linkList.isIssueAt(offset)) {
						text.setCursor(DefaultCommentView.this.busyCursor);
						String url = DefaultCommentView.this.model.getResultingURL(DefaultCommentView.this.linkList.getIssueAt(offset));
						if (url != null) {
							Program.launch(url);
						}
						text.setCursor(null);
						text.getStyleRangeAtOffset(offset).background = DefaultCommentView.this.black;
					}
				}
			}
		});
		this.multilineComment.addMouseMoveListener(new MouseMoveListener() {

			public void mouseMove(MouseEvent e) {
				// Do not change cursor on drag events
				if (DefaultCommentView.this.mouseDown) {
					if (!DefaultCommentView.this.dragEvent) {
						StyledText text = (StyledText) e.widget;
						text.setCursor(null);
					}
					DefaultCommentView.this.dragEvent = true;
					return;
				}
				StyledText text = (StyledText) e.widget;
				int offset = -1;
				try {
					offset = text.getOffsetAtLocation(new Point(e.x, e.y));
				}
				catch (IllegalArgumentException ex) {
					// ok
				}
				if (offset == -1) text.setCursor(null);
				else if (DefaultCommentView.this.linkList.isIssueAt(offset)) {
					text.setCursor(DefaultCommentView.this.handCursor);
					text.getStyleRangeAtOffset(offset).background = DefaultCommentView.this.blue;
					DefaultCommentView.this.multilineComment.redraw();
				}
				else text.setCursor(null);
			}
		});

		this.multilineComment.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				DefaultCommentView.this.linkList.getIssues().clear();
				StyledText text = (StyledText)e.getSource();
				if (DefaultCommentView.this.getModel().getMessage() != null) {
					DefaultCommentView.this.linkList.parseMessage((text).getText(), DefaultCommentView.this.getModel());
					List styledRanges = new ArrayList();
					for (Iterator iter = DefaultCommentView.this.linkList.getIssues().iterator(); iter.hasNext();) {
						Issue issue = (Issue)iter.next();
						StyleRange range = new StyleRange();
						range.start  = issue.getStart();
						range.length = issue.getEnd() - issue.getStart();
						range.foreground = DefaultCommentView.this.blue;
						range.underline = true;
						styledRanges.add(range);
					}
					text.setStyleRanges((StyleRange[]) styledRanges.toArray(new StyleRange[styledRanges.size()]));
				}
			}
		});
        
        this.multilineComment.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                dispose();
            }           
        });
	}

	public void usedFor(IResource resource) {
		CommitPanel.GetBugTraqPropertiesModelOperation bugtraqOp = new GetBugTraqPropertiesModelOperation(new IResource[] {resource});
		bugtraqOp.run(new NullProgressMonitor());
		this.model = bugtraqOp.getBugtraqModel();
	}

	public void usedFor(IRepositoryResource resource) {

	}

	public void setComment(String comment) {
		this.multilineComment.setText(comment);
	}
	
	protected void dispose(){
		if (this.busyCursor != null) {
			this.busyCursor.dispose();
		}
		if (this.handCursor != null) {
			this.handCursor.dispose();
		}
	}
	
	protected BugtraqModel getModel() {
		if (this.model != null) {
			return this.model;
		}
		else {
			return new BugtraqModel();
		}
	}
	
}