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

package org.eclipse.team.svn.core.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.team.svn.core.connector.ISVNNotificationCallback;
import org.eclipse.team.svn.core.connector.SVNNotification;

/**
 * SVN notification listener composite
 * 
 * @author Alexander Gurov
 */
public class SVNNotificationComposite implements ISVNNotificationCallback {
	protected ISVNNotificationCallback []listeners;

	public SVNNotificationComposite() {
		this.listeners = new ISVNNotificationCallback[0];
	}
	
	public void add(ISVNNotificationCallback listener) {
		List<ISVNNotificationCallback> tmp = new ArrayList<ISVNNotificationCallback>(Arrays.asList(this.listeners));
		if (!tmp.contains(listener)) {
			tmp.add(listener);
		}
		this.listeners = tmp.toArray(new ISVNNotificationCallback[tmp.size()]);
	}
	
	public void remove(ISVNNotificationCallback listener) {
		List<ISVNNotificationCallback> tmp = new ArrayList<ISVNNotificationCallback>(Arrays.asList(this.listeners));
		tmp.remove(listener);
		this.listeners = tmp.toArray(new ISVNNotificationCallback[tmp.size()]);
	}

	public void notify(SVNNotification info) {
		// thread safe...
	    ISVNNotificationCallback []tmp = this.listeners;
		for (int i = 0; i < tmp.length; i++) {
			tmp[i].notify(info);
		}
	}

}
