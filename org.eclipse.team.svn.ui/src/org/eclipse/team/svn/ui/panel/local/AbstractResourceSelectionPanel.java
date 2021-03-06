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

package org.eclipse.team.svn.ui.panel.local;

import org.eclipse.core.resources.IResource;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.composite.ResourceSelectionComposite;
import org.eclipse.team.svn.ui.event.IResourceSelectionChangeListener;
import org.eclipse.team.svn.ui.event.ResourceSelectionChangedEvent;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;
import org.eclipse.team.svn.ui.panel.participant.BasePaneParticipant;
import org.eclipse.team.svn.ui.panel.participant.PaneParticipantHelper;
import org.eclipse.team.svn.ui.panel.participant.PaneParticipantHelper.PaneVerifier;
import org.eclipse.team.svn.ui.verifier.AbstractVerifier;

/**
 * Abstract resource selection panel implementation
 * 
 * @author Alexander Gurov
 */
public abstract class AbstractResourceSelectionPanel extends AbstractDialogPanel {
	protected IResource []resources;
	protected ResourceSelectionComposite selectionComposite;
//	protected int subPathStart;	// common root length, unfortunately doesn't work with more than one repository location
	protected IResource[] userSelectedResources;

	protected PaneParticipantHelper paneParticipantHelper;
	
    public AbstractResourceSelectionPanel(IResource []resources, IResource[] userSelectedResources, String []buttonNames) {
        super(buttonNames);
		this.resources = resources;
		this.userSelectedResources = userSelectedResources;		
		
		this.paneParticipantHelper = new PaneParticipantHelper();
    }

	public IResource []getSelectedResources() {
		if (this.paneParticipantHelper.isParticipantPane()) {
			return this.paneParticipantHelper.getSelectedResources();
		}
		return this.selectionComposite.getSelectedResources();							
	}

	public IResource []getNotSelectedResources() {
    	if (this.paneParticipantHelper.isParticipantPane()) {    		
    		return this.paneParticipantHelper.getNotSelectedResources(); 		
    	}
    	return this.selectionComposite.getNotSelectedResources();    	
	}

	public IResource[] getTreatAsEdits() {
		return this.paneParticipantHelper.isParticipantPane() ? new IResource[0] : this.selectionComposite.getTreatAsEdits();
	}

    public Point getPrefferedSizeImpl() {
        return new Point(600, SWT.DEFAULT);
    }
    
    public void createControlsImpl(Composite parent) {
    	if (this.paneParticipantHelper.isParticipantPane()) {    		
    		this.paneParticipantHelper.init(this.createPaneParticipant());
    		this.createPaneControls(parent);
    	} else {
        	this.selectionComposite = new ResourceSelectionComposite(parent, SWT.NONE, this.resources, false, this.userSelectedResources, false);
    		GridData data = new GridData(GridData.FILL_BOTH);
    		data.heightHint = 210;
    		this.selectionComposite.setLayoutData(data);
    		this.selectionComposite.addResourcesSelectionChangedListener(new IResourceSelectionChangeListener() {
    			public void resourcesSelectionChanged(ResourceSelectionChangedEvent event) {
    				AbstractResourceSelectionPanel.this.validateContent();
    			}
    		});
    		this.attachTo(this.selectionComposite, new AbstractVerifier() {
    			protected String getErrorMessage(Control input) {
    				IResource []selection = AbstractResourceSelectionPanel.this.getSelectedResources();
    				if (selection == null || selection.length == 0) {
    					return SVNUIMessages.ResourceSelectionComposite_Verifier_Error;
    				}
    				return null;
    			}
    			protected String getWarningMessage(Control input) {
    				return null;
    			}
    		});
    		this.addContextMenu();	
    	}
    }
    
    protected void createPaneControls(Composite parent) {    			
		Control paneControl = this.paneParticipantHelper.createChangesPage(parent);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 210;
        paneControl.setLayoutData(data);                        
        
        this.paneParticipantHelper.initListeners();	        
                   
        //add validator to pane
        this.attachTo(paneControl, new PaneVerifier(this.paneParticipantHelper));                        
    }
    
    public void postInit() {
    	super.postInit();
    	if (this.paneParticipantHelper.isParticipantPane()) {
    		this.paneParticipantHelper.expandPaneTree();
    	}
    }      
    
    public void dispose() {
    	super.dispose();
    	if (this.paneParticipantHelper.isParticipantPane()) {
    		this.paneParticipantHelper.dispose();
    	}  	
    }
	
	protected void saveChangesImpl() {
    }

    protected void cancelChangesImpl() {
    }
    
    protected void addContextMenu() {
    }
    
    protected abstract BasePaneParticipant createPaneParticipant();
}
