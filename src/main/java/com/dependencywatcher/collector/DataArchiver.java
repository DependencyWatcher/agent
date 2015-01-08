package com.dependencywatcher.collector;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import org.apache.commons.io.FileUtils;

/**
 * Collects files into temporary directory, then archives it.
 */
public class DataArchiver {

	private Path projectRoot;
	private Path tempDir;

	public DataArchiver(Path projectRoot) throws IOException {
		this.projectRoot = projectRoot;
		this.tempDir = Files.createTempDirectory("dw-collector");
	}

	public void dispose() throws IOException {
		if (tempDir != null) {
			FileUtils.deleteDirectory(tempDir.toFile());
			tempDir = null;
		}
	}

	/**
	 * Add the given file to the data archive
	 * 
	 * @param file
	 *            File to add
	 * @throws IOException
	 */
	public void collect(Path file) throws IOException {
		Path targetPath = tempDir.resolve(projectRoot.relativize(file));
		Files.createDirectories(targetPath.getParent());
		Files.copy(file, targetPath);
	}

	/**
	 * Create new file in the data archive
	 * 
	 * @param String
	 *            file File name
	 * @param String
	 *            contents File contents
	 * 
	 * @throws IOException
	 */
	public void add(String file, String contents) throws IOException {
		FileUtils.writeStringToFile(tempDir.resolve(file).toFile(), contents);
	}

	/**
	 * Creates archive containing collected data
	 * 
	 * @return Zip file
	 * @throws IOException
	 * @throws ZipException
	 */
	public File createArchive() throws IOException, ZipException {
		File tmpFile = File.createTempFile("dw-data", ".zip");
		try {
			tmpFile.delete();

			ZipFile zipFile = new ZipFile(tmpFile.getAbsoluteFile());
			ZipParameters parameters = new ZipParameters();
			parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_ULTRA);
			parameters.setIncludeRootFolder(false);
			zipFile.addFolder(tempDir.toFile(), parameters);

		} finally {
			dispose();
		}
		return tmpFile;
	}
}
