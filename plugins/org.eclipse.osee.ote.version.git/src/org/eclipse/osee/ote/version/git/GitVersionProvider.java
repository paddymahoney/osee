package org.eclipse.osee.ote.version.git;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.osee.ote.version.FileVersion;
import org.eclipse.osee.ote.version.FileVersionInformationProvider;

public class GitVersionProvider implements FileVersionInformationProvider {


	@Override
	public FileVersion getFileVersion(File file) {
		try {
			return new GitFileVersion(new GitVersion(file));
		} catch (NoHeadException e) {
		} catch (IOException e) {
		} catch (GitAPIException e) {
		}
		return null;
	}

	@Override
	public void getFileVersions(List<File> files, Map<File, FileVersion> versions) {
		GitVersions gitVersions = new GitVersions(files);
		Map<File, RevCommit> commits = gitVersions.getLastCommits();
		for(Entry<File, RevCommit> entry:commits.entrySet()){
			versions.put(entry.getKey(), new GitFileVersion(entry.getValue()));
		}
	}

}
