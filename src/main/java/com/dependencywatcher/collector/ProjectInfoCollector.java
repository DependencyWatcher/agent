package com.dependencywatcher.collector;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import net.lingala.zip4j.exception.ZipException;

/**
 * Goes over all project files, and collects needed information
 */
public class ProjectInfoCollector {

	/**
	 * Gathers needed information about project
	 * 
	 * @param projectRoot
	 *            Project root path
	 * @return archive containing the collected data
	 * @throws IOException
	 * @throws ZipException
	 */
	public File collect(Path projectRoot) throws IOException, ZipException {

		DataArchiver dataArchiver = new DataArchiver(projectRoot);
		try {
			ProjectFileVisitor fileVisitor = new ProjectFileVisitor(
					dataArchiver, projectRoot);

			Files.walkFileTree(projectRoot, fileVisitor);

			dataArchiver.add("DWFileList.txt", fileVisitor.getFileList());
			return dataArchiver.createArchive();
		} finally {
			dataArchiver.dispose();
		}
	}
}
