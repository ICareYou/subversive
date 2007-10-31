/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Vladimir Bykov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.composite;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.svn.core.client.Revision;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.remote.GetLogMessagesOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.panel.common.SelectRevisionPanel;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.team.svn.ui.verifier.AbstractVerifierProxy;
import org.eclipse.team.svn.ui.verifier.CompositeVerifier;
import org.eclipse.team.svn.ui.verifier.IValidationManager;
import org.eclipse.team.svn.ui.verifier.IntegerFieldVerifier;
import org.eclipse.team.svn.ui.verifier.NonEmptyFieldVerifier;

/**
 * Select revision panel
 * 
 * @author Vladimir Bykov
 */
public class RevisionComposite extends Composite {
	protected IRepositoryResource selectedResource;
	protected Revision defaultRevision;
	protected long currentRevision;
	protected long lastSelectedRevision;
	protected boolean stopOnCopy;
	protected String []captions;

	protected Revision selectedRevision;
	
	protected Text revisionField;
	protected Button headRevisionRadioButton;
	protected Button changeRevisionRadioButton;
	protected Button changeRevisionButton;
	
	protected IValidationManager validationManager;
	
	public RevisionComposite(Composite parent, IValidationManager validationManager, boolean stopOnCopy, String []captions, Revision defaultRevision) {
		super(parent, SWT.NONE);
		this.stopOnCopy = stopOnCopy;
		this.validationManager = validationManager;
		this.lastSelectedRevision = Revision.SVN_INVALID_REVNUM;
		this.captions = captions;
		this.defaultRevision = defaultRevision;
		this.createControls();
	}
	
	public Revision getSelectedRevision() {
		return this.selectedRevision;
	}
	
	public IRepositoryResource getSelectedResource() {
		return this.selectedResource;
	}
	
	public void addChangeRevisionListener(SelectionListener listener) {
		this.changeRevisionButton.addSelectionListener(listener);
	}
	
	public void setSelectedResource(IRepositoryResource resource) {
		this.selectedResource = resource;
		Revision rev = this.selectedResource.getSelectedRevision();
		if (rev.getKind() == Revision.Kind.number) {
			this.selectedRevision = rev;
			this.lastSelectedRevision = ((Revision.Number)this.selectedRevision).getNumber();
			
			if (this.changeRevisionRadioButton != null) {
				this.revisionField.setText(this.selectedRevision.toString());
				this.headRevisionRadioButton.setSelection(false);
				this.changeRevisionRadioButton.setSelection(true);
				this.changeRevisionButton.setEnabled(true);
				this.revisionField.setEditable(true);
			}
		}
		else {
			this.selectedRevision = this.defaultRevision;
			this.lastSelectedRevision = -1;
			
			if (this.changeRevisionRadioButton != null) {
				this.revisionField.setText("");
				this.headRevisionRadioButton.setSelection(true);
				this.changeRevisionRadioButton.setSelection(false);
				this.changeRevisionButton.setEnabled(false);
				this.revisionField.setEditable(false);
			}
		}
	}
	
	public long getCurrentRevision() {
		return this.currentRevision;
	}
	
	public void setCurrentRevision(long currentRevision) {
		this.currentRevision = currentRevision;
	}
	
	private void createControls() {
		GridLayout layout = null;
		GridData data = null;
		
		layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        this.setLayout(layout);
		
		Group group = new Group(this, SWT.NONE);
		group.setText(this.captions == null ? SVNTeamUIPlugin.instance().getResource("RevisionComposite.Revision") : this.captions[0]);
		layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		group.setLayoutData(data);

		this.headRevisionRadioButton = new Button(group, SWT.RADIO);
		this.headRevisionRadioButton.setText(captions == null ? SVNTeamUIPlugin.instance().getResource("RevisionComposite.HeadRevision") : captions[1]);
		this.headRevisionRadioButton.setLayoutData(new GridData());
		this.headRevisionRadioButton.setSelection(true);

		this.headRevisionRadioButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				RevisionComposite.this.validationManager.validateContent();
				Button button = (Button)e.widget;
				if (button.getSelection()) {
					RevisionComposite.this.selectedRevision = RevisionComposite.this.defaultRevision;
					RevisionComposite.this.changeRevisionButton.setEnabled(false);
					RevisionComposite.this.revisionField.setEditable(false);
					RevisionComposite.this.revisionField.setText("");	
				}
				else {
					RevisionComposite.this.changeRevisionButton.setEnabled(true);
					RevisionComposite.this.revisionField.setEditable(true);
					if (RevisionComposite.this.lastSelectedRevision != Revision.SVN_INVALID_REVNUM) {
						RevisionComposite.this.revisionField.setText(String.valueOf(RevisionComposite.this.lastSelectedRevision));
						RevisionComposite.this.selectedRevision = Revision.getInstance(RevisionComposite.this.lastSelectedRevision);
					}
				}
				RevisionComposite.this.additionalValidation();
			}
		});
		
		Label emptyControl = new Label(group, SWT.NONE);
		emptyControl.setLayoutData(new GridData());
		emptyControl.setText("");
		
		this.changeRevisionRadioButton = new Button(group, SWT.RADIO);
		this.changeRevisionRadioButton.setText(SVNTeamUIPlugin.instance().getResource("RevisionComposite.Revision"));
		
		final Composite revisionSelection = new Composite(group, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		revisionSelection.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		revisionSelection.setLayoutData(data);
		
		data = new GridData();
		data.widthHint = 60;
		this.revisionField = new Text(revisionSelection, SWT.SINGLE | SWT.BORDER);	
		this.revisionField.setLayoutData(data);
		this.revisionField.setEditable(false);
		CompositeVerifier verifier = new CompositeVerifier();
		String name = SVNTeamUIPlugin.instance().getResource("RevisionComposite.Revision.Verifier");
		verifier.add(new NonEmptyFieldVerifier(name));
		verifier.add(new IntegerFieldVerifier(name, true));
		this.validationManager.attachTo(this.revisionField, new AbstractVerifierProxy(verifier) {
			protected boolean isVerificationEnabled(Control input) {
				return RevisionComposite.this.changeRevisionRadioButton.getSelection();
			}
		});
		this.revisionField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String input = ((Text)e.widget).getText();
				try {
					long selectedRevisionNum = Long.parseLong(input);
					if (selectedRevisionNum >= 0) {
					    RevisionComposite.this.lastSelectedRevision = selectedRevisionNum;
					    RevisionComposite.this.selectedRevision = Revision.getInstance(selectedRevisionNum);
					}
				}
				catch (NumberFormatException ex) {
					//don't handle this exception - already handled by the verifier
				}
			}
		});
		
		this.changeRevisionButton = new Button(revisionSelection, SWT.PUSH);
		this.changeRevisionButton.setText(SVNTeamUIPlugin.instance().getResource("RevisionComposite.Select"));
		data = new GridData();
		data.widthHint = DefaultDialog.computeButtonWidth(this.changeRevisionButton);
		this.changeRevisionButton.setLayoutData(data);
		this.changeRevisionButton.setEnabled(false);
			
		this.changeRevisionButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
		    	GetLogMessagesOperation msgsOp = new GetLogMessagesOperation(RevisionComposite.this.selectedResource, RevisionComposite.this.stopOnCopy);
				IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
				if (SVNTeamPreferences.getHistoryBoolean(store, SVNTeamPreferences.HISTORY_PAGING_ENABLE_NAME)) {
					msgsOp.setLimit(SVNTeamPreferences.getHistoryInt(store, SVNTeamPreferences.HISTORY_PAGE_SIZE_NAME));
				}
				if (!UIMonitorUtility.doTaskNowDefault(RevisionComposite.this.getShell(), msgsOp, true).isCancelled() && msgsOp.getExecutionState() == IActionOperation.OK) {
				    SelectRevisionPanel panel = new SelectRevisionPanel(msgsOp, SWT.SINGLE, RevisionComposite.this.currentRevision);
					DefaultDialog dialog = new DefaultDialog(RevisionComposite.this.getShell(), panel);
					if (dialog.open() == 0) {
					    long selectedRevisionNum = panel.getSelectedRevision();
					    RevisionComposite.this.lastSelectedRevision = selectedRevisionNum;
					    RevisionComposite.this.selectedRevision = Revision.getInstance(selectedRevisionNum);
					    RevisionComposite.this.revisionField.setText(String.valueOf(selectedRevisionNum));
					}
				}
				RevisionComposite.this.additionalValidation();
			}
		});		
	}
	
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		this.changeRevisionButton.setEnabled(enabled && this.changeRevisionRadioButton.getSelection());
		this.changeRevisionRadioButton.setEnabled(enabled);
		this.headRevisionRadioButton.setEnabled(enabled);
	}
	
	public void additionalValidation() {
		//override this if there is a need to perform additional validation
	}
	
}