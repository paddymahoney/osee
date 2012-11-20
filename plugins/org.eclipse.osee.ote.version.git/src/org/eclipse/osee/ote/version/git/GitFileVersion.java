package org.eclipse.osee.ote.version.git;

import java.io.IOException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.osee.ote.version.FileVersion;

public class GitFileVersion implements FileVersion {

	private RevCommit commit;
	private boolean modified;

	GitFileVersion(GitVersion gitVersion) throws NoHeadException, IOException, GitAPIException{
		this.commit = gitVersion.getLastCommit();
		if(commit == null){
			throw new IOException("Not a git repo");
		}
	}
	
	GitFileVersion(RevCommit commit) {
		this.commit = commit;
	}
	
	@Override
	public String getVersion() {
		return commit.getId().getName();
	}

	@Override
	public String getURL() {
		return null;
	}

	@Override
	public String getVersionControlSystem() {
		return "GIT";
	}

	@Override
	public String getModifiedFlag() {
		return "N/A";
	}

	@Override
	public String getLastModificationDate() {
		return commit.getAuthorIdent().getWhen().toString();
	}

	@Override
	public String getLastAuthor() {
		return commit.getAuthorIdent().getName();
	}

}
