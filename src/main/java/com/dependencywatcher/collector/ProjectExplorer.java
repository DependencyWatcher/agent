package com.dependencywatcher.collector;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.regex.Pattern;

import net.lingala.zip4j.exception.ZipException;

/**
 * Goes over all project files, and collects needed information
 */
public class ProjectExplorer extends SimpleFileVisitor<Path> {

	private static final String[] files = new String[] { "pom.xml",
			"project.clj", "Gemfile", "build.gradle", "package.json",
			"setup.py", };

	private static final Pattern[] filePatterns = new Pattern[] {
			Pattern.compile(".*\\.html?"), Pattern.compile(".*\\.ftl"),
			Pattern.compile(".*\\.php[345s]?"), Pattern.compile(".*\\.phtml"),
			Pattern.compile(".*\\.jsp"), };

	private Path projectRoot;
	private DataCollector dataCollector;
	private StringBuilder fileList;

	public ProjectExplorer(Path projectRoot) throws IOException {
		this.projectRoot = projectRoot;
		this.dataCollector = new DataCollector(projectRoot);
		this.fileList = new StringBuilder();
	}

	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
			throws IOException {
		// Skip non-interesting directories:
		String dirName = dir.getFileName().toString();
		if (".git".equals(dirName) || ".svn".equals(dirName)) {
			return FileVisitResult.SKIP_SUBTREE;
		}
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attr)
			throws IOException {

		String fileName = file.getFileName().toString().toLowerCase();
		boolean collect = false;
		for (String f : files) {
			if (f.equals(fileName)) {
				collect = true;
				break;
			}
		}
		if (!collect) {
			for (Pattern p : filePatterns) {
				if (p.matcher(fileName).matches()) {
					collect = true;
					break;
				}
			}
		}
		if (collect) {
			dataCollector.collect(file);
		}

		fileList.append(projectRoot.relativize(file)).append('\n');

		return FileVisitResult.CONTINUE;
	}

	/**
	 * Iterates on all files in the project, and return archive containing
	 * collected data
	 * 
	 * @return archive containing the collected data
	 * @throws IOException
	 * @throws ZipException
	 */
	public File explore() throws IOException, ZipException {
		Files.walkFileTree(projectRoot, this);

		dataCollector.add("DWFileList.txt", fileList.toString());
		return dataCollector.createArchive();
	}
}
