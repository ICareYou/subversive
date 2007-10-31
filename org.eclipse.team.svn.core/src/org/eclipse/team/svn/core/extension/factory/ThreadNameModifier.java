/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.extension.factory;

import java.io.OutputStream;

import org.eclipse.team.svn.core.client.BlameCallback;
import org.eclipse.team.svn.core.client.ClientWrapperException;
import org.eclipse.team.svn.core.client.ConflictResolverCallback;
import org.eclipse.team.svn.core.client.CopySource;
import org.eclipse.team.svn.core.client.DirEntry;
import org.eclipse.team.svn.core.client.ICredentialsPrompt;
import org.eclipse.team.svn.core.client.ISVNClientWrapper;
import org.eclipse.team.svn.core.client.ISVNProgressMonitor;
import org.eclipse.team.svn.core.client.Info2;
import org.eclipse.team.svn.core.client.InfoCallback;
import org.eclipse.team.svn.core.client.LogMessage;
import org.eclipse.team.svn.core.client.LogMessageCallback;
import org.eclipse.team.svn.core.client.MergeInfo;
import org.eclipse.team.svn.core.client.Notify2;
import org.eclipse.team.svn.core.client.PropertyData;
import org.eclipse.team.svn.core.client.ProplistCallback;
import org.eclipse.team.svn.core.client.Revision;
import org.eclipse.team.svn.core.client.RevisionRange;
import org.eclipse.team.svn.core.client.Status;
import org.eclipse.team.svn.core.client.StatusCallback;
import org.eclipse.team.svn.core.utility.StringId;

/**
 * Each method call through this wrapper redefines thread name then restores it
 * 
 * @author Alexander Gurov
 */
public class ThreadNameModifier implements ISVNClientWrapper {
	protected ISVNClientWrapper client;

	public ThreadNameModifier(ISVNClientWrapper client) {
		this.client = client;
	}

	public void add(String path, int depth, boolean skipIgnores, boolean addParents, ISVNProgressMonitor monitor) throws ClientWrapperException {
		String oldName = this.overrideThreadName();
		try {
			this.client.add(path, depth, skipIgnores, addParents, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void blame(String path, Revision pegRevision, Revision revisionStart, Revision revisionEnd, boolean ignoreMimeType, boolean includeMergedRevisions, BlameCallback callback, ISVNProgressMonitor monitor) throws ClientWrapperException {
		String oldName = this.overrideThreadName();
		try {
			this.client.blame(path, pegRevision, revisionStart, revisionEnd, ignoreMimeType, includeMergedRevisions, callback, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public long checkout(String moduleName, String destPath, Revision revision, Revision pegRevision, int depth, boolean ignoreExternals, boolean allowUnverObstructions, ISVNProgressMonitor monitor) throws ClientWrapperException {
		String oldName = this.overrideThreadName();
		try {
			return this.client.checkout(moduleName, destPath, revision, pegRevision, depth, ignoreExternals, allowUnverObstructions, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void cleanup(String path, ISVNProgressMonitor monitor) throws ClientWrapperException {
		String oldName = this.overrideThreadName();
		try {
			this.client.cleanup(path, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public long []commit(String []path, String message, int depth, boolean noUnlock, boolean keepChangelist, String changelistName, ISVNProgressMonitor monitor) throws ClientWrapperException {
		String oldName = this.overrideThreadName();
		try {
			return this.client.commit(path, message, depth, noUnlock, keepChangelist, changelistName, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void copy(String srcPath, String destPath, Revision revision, ISVNProgressMonitor monitor) throws ClientWrapperException {
		String oldName = this.overrideThreadName();
		try {
			this.client.copy(srcPath, destPath, revision, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void copy(CopySource []srcPath, String destPath, String message, boolean copyAsChild, boolean makeParents, boolean withMergeHistory, ISVNProgressMonitor monitor) throws ClientWrapperException {
		String oldName = this.overrideThreadName();
		try {
			this.client.copy(srcPath, destPath, message, copyAsChild, makeParents, withMergeHistory, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void diff(String target1, Revision revision1, Revision peg1, String target2, Revision revision2, Revision peg2, String outFileName, int depth, boolean ignoreAncestry, boolean noDiffDeleted, boolean force, boolean diffUnversioned, boolean relativePath, ISVNProgressMonitor monitor) throws ClientWrapperException {
		String oldName = this.overrideThreadName();
		try {
			this.client.diff(target1, revision1, peg1, target2, revision2, peg2, outFileName, depth, ignoreAncestry, noDiffDeleted, force, diffUnversioned, relativePath, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public Status []diffStatus(String url1, Revision pegRevision1, Revision revision1, String url2, Revision pegRevision2, Revision revision2, int depth, boolean ignoreAncestry, ISVNProgressMonitor monitor) throws ClientWrapperException {
		String oldName = this.overrideThreadName();
		try {
			return this.client.diffStatus(url1, pegRevision1, revision1, url2, pegRevision2, revision2, depth, ignoreAncestry, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void diffStatus(String url1, Revision pegRevision1, Revision revision1, String url2, Revision pegRevision2, Revision revision2, int depth, boolean ignoreAncestry, StatusCallback cb, ISVNProgressMonitor monitor) throws ClientWrapperException {
		String oldName = this.overrideThreadName();
		try {
			this.client.diffStatus(url1, pegRevision1, revision1, url2, pegRevision2, revision2, depth, ignoreAncestry, cb, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void dispose() {
		String oldName = this.overrideThreadName();
		try {
			this.client.dispose();
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public long doExport(String srcPath, String destPath, Revision revision, Revision pegRevision, boolean force, boolean ignoreExternals, int depth, String nativeEOL, ISVNProgressMonitor monitor) throws ClientWrapperException {
		String oldName = this.overrideThreadName();
		try {
			return this.client.doExport(srcPath, destPath, revision, pegRevision, force, ignoreExternals, depth, nativeEOL, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void doImport(String path, String url, String message, int depth, boolean noIgnore, boolean ignoreUnknownNodeTypes, ISVNProgressMonitor monitor) throws ClientWrapperException {
		String oldName = this.overrideThreadName();
		try {
			this.client.doImport(path, url, message, depth, noIgnore, ignoreUnknownNodeTypes, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public long doSwitch(String path, String url, Revision revision, int depth, boolean ignoreExternals, boolean allowUnverObstructions, ISVNProgressMonitor monitor) throws ClientWrapperException {
		String oldName = this.overrideThreadName();
		try {
			return this.client.doSwitch(path, url, revision, depth, ignoreExternals, allowUnverObstructions, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public String getConfigDirectory() throws ClientWrapperException {
		String oldName = this.overrideThreadName();
		try {
			return this.client.getConfigDirectory();
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public Notify2 getNotification2() {
		String oldName = this.overrideThreadName();
		try {
			return this.client.getNotification2();
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public ICredentialsPrompt getPrompt() {
		String oldName = this.overrideThreadName();
		try {
			return this.client.getPrompt();
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public Info2[] info2(String pathOrUrl, Revision revision, Revision pegRevision, boolean recurse, ISVNProgressMonitor monitor) throws ClientWrapperException {
		String oldName = this.overrideThreadName();
		try {
			return this.client.info2(pathOrUrl, revision, pegRevision, recurse, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public boolean isCommitMissingFiles() {
		String oldName = this.overrideThreadName();
		try {
			return this.client.isCommitMissingFiles();
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public boolean isCredentialsCacheEnabled() {
		String oldName = this.overrideThreadName();
		try {
			return this.client.isCredentialsCacheEnabled();
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public boolean isReportRevisionChange() {
		String oldName = this.overrideThreadName();
		try {
			return this.client.isReportRevisionChange();
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public boolean isSSLCertificateCacheEnabled() {
		String oldName = this.overrideThreadName();
		try {
			return this.client.isSSLCertificateCacheEnabled();
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public boolean isTouchUnresolved() {
		String oldName = this.overrideThreadName();
		try {
			return this.client.isTouchUnresolved();
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public DirEntry []list(String url, Revision revision, Revision pegRevision, int depth, int direntFields, boolean fetchLocks, ISVNProgressMonitor monitor) throws ClientWrapperException {
		String oldName = this.overrideThreadName();
		try {
			return this.client.list(url, revision, pegRevision, depth, direntFields, fetchLocks, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void lock(String[] path, String comment, boolean force, ISVNProgressMonitor monitor) throws ClientWrapperException {
		String oldName = this.overrideThreadName();
		try {
			this.client.lock(path, comment, force, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public LogMessage []logMessages(String path, Revision pegRevision, Revision revisionStart, Revision revisionEnd, boolean stopOnCopy, boolean discoverPath, boolean omitLogText, long limit, ISVNProgressMonitor monitor) throws ClientWrapperException {
		String oldName = this.overrideThreadName();
		try {
			return this.client.logMessages(path, pegRevision, revisionStart, revisionEnd, stopOnCopy, discoverPath, omitLogText, limit, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void logMessages(String path, Revision pegRevision, Revision revisionStart, Revision revisionEnd, boolean stopOnCopy, boolean discoverPath, boolean includeMergedRevisions, boolean omitLogText, long limit, LogMessageCallback cb, ISVNProgressMonitor monitor) throws ClientWrapperException {
		String oldName = this.overrideThreadName();
		try {
			this.client.logMessages(path, pegRevision, revisionStart, revisionEnd, stopOnCopy, discoverPath, includeMergedRevisions, omitLogText, limit, cb, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void merge(String path1, Revision revision1, String path2, Revision revision2, String localPath, boolean force, int depth, boolean ignoreAncestry, boolean dryRun, ISVNProgressMonitor monitor) throws ClientWrapperException {
		String oldName = this.overrideThreadName();
		try {
			this.client.merge(path1, revision1, path2, revision2, localPath, force, depth, ignoreAncestry, dryRun, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void merge(String path, Revision pegRevision, RevisionRange []revisions, String localPath, boolean force, int depth, boolean ignoreAncestry, boolean dryRun, ISVNProgressMonitor monitor) throws ClientWrapperException {
		String oldName = this.overrideThreadName();
		try {
			this.client.merge(path, pegRevision, revisions, localPath, force, depth, ignoreAncestry, dryRun, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public Status[] merge(String url, Revision peg, Revision from, Revision to, String mergePath, Status[] mergeStatus, boolean force, ISVNProgressMonitor monitor) throws ClientWrapperException {
		String oldName = this.overrideThreadName();
		try {
			return this.client.merge(url, peg, from, to, mergePath, mergeStatus, force, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public Status[] mergeStatus(String url, Revision peg, Revision from, Revision to, String path, Revision lastMerged, boolean recurse, boolean ignoreAncestry, ISVNProgressMonitor monitor) throws ClientWrapperException {
		String oldName = this.overrideThreadName();
		try {
			return this.client.mergeStatus(url, peg, from, to, path, lastMerged, recurse, ignoreAncestry, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void mkdir(String []path, String message, boolean makeParents, ISVNProgressMonitor monitor) throws ClientWrapperException {
		String oldName = this.overrideThreadName();
		try {
			this.client.mkdir(path, message, makeParents, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void move(String srcPath, String dstPath, boolean force, ISVNProgressMonitor monitor) throws ClientWrapperException {
		String oldName = this.overrideThreadName();
		try {
			this.client.move(srcPath, dstPath, force, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void move(String []srcPath, String dstPath, String message, boolean force, boolean moveAsChild, boolean makeParents, boolean withMergeHistory, ISVNProgressMonitor monitor) throws ClientWrapperException {
		String oldName = this.overrideThreadName();
		try {
			this.client.move(srcPath, dstPath, message, force, moveAsChild, makeParents, withMergeHistory, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void notification2(Notify2 notify) {
		String oldName = this.overrideThreadName();
		try {
			this.client.notification2(notify);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void password(String password) {
		String oldName = this.overrideThreadName();
		try {
			this.client.password(password);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public PropertyData[] properties(String path, Revision revision, Revision peg, ISVNProgressMonitor monitor) throws ClientWrapperException {
		String oldName = this.overrideThreadName();
		try {
			return this.client.properties(path, revision, peg, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public PropertyData propertyGet(String path, String name, Revision revision, Revision pegRevision, ISVNProgressMonitor monitor) throws ClientWrapperException {
		String oldName = this.overrideThreadName();
		try {
			return this.client.propertyGet(path, name, revision, pegRevision, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void propertyRemove(String path, String name, int depth, ISVNProgressMonitor monitor) throws ClientWrapperException {
		String oldName = this.overrideThreadName();
		try {
			this.client.propertyRemove(path, name, depth, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void propertySet(String path, String name, byte []value, boolean recurse, boolean force, ISVNProgressMonitor monitor) throws ClientWrapperException {
		String oldName = this.overrideThreadName();
		try {
			this.client.propertySet(path, name, value, recurse, force, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void propertySet(String path, String name, String value, int depth, boolean force, ISVNProgressMonitor monitor) throws ClientWrapperException {
		String oldName = this.overrideThreadName();
		try {
			this.client.propertySet(path, name, value, depth, force, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void relocate(String from, String to, String path, boolean recurse, ISVNProgressMonitor monitor) throws ClientWrapperException {
		String oldName = this.overrideThreadName();
		try {
			this.client.relocate(from, to, path, recurse, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void remove(String []path, String message, boolean force, boolean keepLocal, ISVNProgressMonitor monitor) throws ClientWrapperException {
		String oldName = this.overrideThreadName();
		try {
			this.client.remove(path, message, force, keepLocal, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void resolved(String path, int depth, int conflictResult, ISVNProgressMonitor monitor) throws ClientWrapperException {
		String oldName = this.overrideThreadName();
		try {
			this.client.resolved(path, depth, conflictResult, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void revert(String path, int depth, ISVNProgressMonitor monitor) throws ClientWrapperException {
		String oldName = this.overrideThreadName();
		try {
			this.client.revert(path, depth, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void setClientSSLCertificate(String certPath, String passphrase) {
		String oldName = this.overrideThreadName();
		try {
			this.client.setClientSSLCertificate(certPath, passphrase);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void setCommitMissingFiles(boolean commitMissingFiles) {
		String oldName = this.overrideThreadName();
		try {
			this.client.setCommitMissingFiles(commitMissingFiles);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void setCredentialsCacheEnabled(boolean cacheCredentials) {
		String oldName = this.overrideThreadName();
		try {
			this.client.setCredentialsCacheEnabled(cacheCredentials);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void setPrompt(ICredentialsPrompt prompt) {
		String oldName = this.overrideThreadName();
		try {
			this.client.setPrompt(prompt);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void setProxy(String host, int port, String userName, String password) {
		String oldName = this.overrideThreadName();
		try {
			this.client.setProxy(host, port, userName, password);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void setReportRevisionChange(boolean report) {
		String oldName = this.overrideThreadName();
		try {
			this.client.setReportRevisionChange(report);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void setSSHCredentials(String userName, String privateKeyPath, String passphrase, int port) {
		String oldName = this.overrideThreadName();
		try {
			this.client.setSSHCredentials(userName, privateKeyPath, passphrase, port);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void setSSHCredentials(String userName, String password, int port) {
		String oldName = this.overrideThreadName();
		try {
			this.client.setSSHCredentials(userName, password, port);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void setSSLCertificateCacheEnabled(boolean enabled) {
		String oldName = this.overrideThreadName();
		try {
			this.client.setSSLCertificateCacheEnabled(enabled);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void setTouchUnresolved(boolean touchUnresolved) {
		String oldName = this.overrideThreadName();
		try {
			this.client.setTouchUnresolved(touchUnresolved);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public Status []status(String path, boolean descend, boolean onServer, boolean getAll, boolean noIgnore, boolean ignoreExternals, ISVNProgressMonitor monitor) throws ClientWrapperException {
		String oldName = this.overrideThreadName();
		try {
			return this.client.status(path, descend, onServer, getAll, noIgnore, ignoreExternals, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void status(String path, int depth, boolean onServer, boolean getAll, boolean noIgnore, boolean ignoreExternals, StatusCallback callback, ISVNProgressMonitor monitor) throws ClientWrapperException {
		String oldName = this.overrideThreadName();
		try {
			this.client.status(path, depth, onServer, getAll, noIgnore, ignoreExternals, callback, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void streamFileContent(String path, Revision revision, Revision pegRevision, int bufferSize, OutputStream stream, ISVNProgressMonitor monitor) throws ClientWrapperException {
		String oldName = this.overrideThreadName();
		try {
			this.client.streamFileContent(path, revision, pegRevision, bufferSize, stream, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void unlock(String[] path, boolean force, ISVNProgressMonitor monitor) throws ClientWrapperException {
		String oldName = this.overrideThreadName();
		try {
			this.client.unlock(path, force, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public long []update(String []path, Revision revision, int depth, boolean ignoreExternals, boolean allowUnverObstructions, ISVNProgressMonitor monitor) throws ClientWrapperException {
		String oldName = this.overrideThreadName();
		try {
			return this.client.update(path, revision, depth, ignoreExternals, allowUnverObstructions, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void username(String username) {
		String oldName = this.overrideThreadName();
		try {
			this.client.username(username);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void addToChangelist(String[] paths, String changelist, ISVNProgressMonitor monitor) throws ClientWrapperException {
		String oldName = this.overrideThreadName();
		try {
			this.client.addToChangelist(paths, changelist, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public RevisionRange[] getAvailableMerges(String path, Revision pegRevision, String mergeSource, ISVNProgressMonitor monitor) throws ClientWrapperException {
		String oldName = this.overrideThreadName();
		try {
			return this.client.getAvailableMerges(path, pegRevision, mergeSource, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public String[] getChangelist(String changelist, String rootPath, ISVNProgressMonitor monitor) throws ClientWrapperException {
		String oldName = this.overrideThreadName();
		try {
			return this.client.getChangelist(changelist, rootPath, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public MergeInfo getMergeInfo(String path, Revision pegRevision, ISVNProgressMonitor monitor) throws ClientWrapperException {
		String oldName = this.overrideThreadName();
		try {
			return this.client.getMergeInfo(path, pegRevision, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void info2(String pathOrUrl, Revision revision, Revision pegRevision, int depth, InfoCallback cb, ISVNProgressMonitor monitor) throws ClientWrapperException {
		String oldName = this.overrideThreadName();
		try {
			this.client.info2(pathOrUrl, revision, pegRevision, depth, cb, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void properties(String path, Revision revision, Revision peg, int depth, ProplistCallback callback, ISVNProgressMonitor monitor) throws ClientWrapperException {
		String oldName = this.overrideThreadName();
		try {
			this.client.properties(path, revision, peg, depth, callback, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void removeFromChangelist(String[] paths, String changelist, ISVNProgressMonitor monitor) throws ClientWrapperException {
		String oldName = this.overrideThreadName();
		try {
			this.client.removeFromChangelist(paths, changelist, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void setConfigDirectory(String configDir) throws ClientWrapperException {
		String oldName = this.overrideThreadName();
		try {
			this.client.setConfigDirectory(configDir);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public void setConflictResolver(ConflictResolverCallback listener) {
		String oldName = this.overrideThreadName();
		try {
			this.client.setConflictResolver(listener);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	public String[] suggestMergeSources(String path, Revision pegRevision, ISVNProgressMonitor monitor) throws ClientWrapperException {
		String oldName = this.overrideThreadName();
		try {
			return this.client.suggestMergeSources(path, pegRevision, monitor);
		}
		finally {
			this.restoreThreadName(oldName);
		}
	}

	protected String overrideThreadName() {
		Thread current = Thread.currentThread();
		String oldName = current.getName();
		current.setName(StringId.generateRandom("SVN", 5));
		return oldName;
	}

	protected void restoreThreadName(String oldName) {
		Thread.currentThread().setName(oldName);
	}

}