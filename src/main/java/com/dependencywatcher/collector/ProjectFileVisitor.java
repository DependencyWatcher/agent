package com.dependencywatcher.collector;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.regex.Pattern;

/**
 * Visits project files, and collects interesting information
 */
public class ProjectFileVisitor extends SimpleFileVisitor<Path> {

	private static final String[] files = new String[] { "pom.xml",
			"project.clj", "Gemfile", "build.gradle", "package.json",
			"setup.py", };

	private static final Pattern[] filePatterns = new Pattern[] {
			Pattern.compile(".*\\.html?"), Pattern.compile(".*\\.ftl"),
			Pattern.compile(".*\\.php[345s]?"), Pattern.compile(".*\\.phtml"),
			Pattern.compile(".*\\.jsp"), };

	private DataArchiver dataArchiver;
	private StringBuilder fileList;
	private Path projectRoot;

	public ProjectFileVisitor(DataArchiver dataArchiver, Path projectRoot) {
		this.dataArchiver = dataArchiver;
		this.fileList = new StringBuilder();
		this.projectRoot = projectRoot;
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
			dataArchiver.collect(file);
		}

		fileList.append(projectRoot.relativize(file)).append('\n');

		return FileVisitResult.CONTINUE;
	}

	/**
	 * @return list of all files in a project
	 */
	public String getFileList() {
		return fileList.toString();
	}
}