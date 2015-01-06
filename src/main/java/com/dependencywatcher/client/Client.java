package com.dependencywatcher.client;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;

/**
 * DependencyWatcher client
 */
public class Client {

	private static final String BASE_URI = "https://dependencywatcher.com/api/v1/";
	private CloseableHttpClient httpClient;

	public Client(String apiKey) {
		httpClient = HttpClientBuilder
				.create()
				.setDefaultHeaders(
						Arrays.asList(new BasicHeader(
								HttpHeaders.AUTHORIZATION, "apikey=" + apiKey)))
				.build();
	}

	private static String encodeURIComponent(String s) {
		try {
			return URLEncoder.encode(s, "UTF-8").replaceAll("\\+", "%20")
					.replaceAll("\\%21", "!").replaceAll("\\%27", "'")
					.replaceAll("\\%28", "(").replaceAll("\\%29", ")")
					.replaceAll("\\%7E", "~");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Creates or updates repository by the given name
	 * 
	 * @param name
	 *            Repository name
	 * @param archive
	 *            Archive containing repository data
	 * @throws ClientException
	 */
	public void uploadRepository(String name, File archive)
			throws ClientException {

		HttpPut putMethod = new HttpPut(BASE_URI + "repository/"
				+ encodeURIComponent(name));

		HttpEntity httpEntity = MultipartEntityBuilder
				.create()
				.addBinaryBody("file", archive,
						ContentType.create("application/zip"),
						archive.getName()).build();

		putMethod.setEntity(httpEntity);

		try {
			httpClient.execute(putMethod).close();

		} catch (HttpResponseException e) {
			throw new APICallException(e.getMessage());

		} catch (IOException e) {
			throw new NotAvailableException(e);
		}
	}

	public static abstract class ClientException extends Exception {
		private static final long serialVersionUID = 1L;

		public ClientException(Throwable cause) {
			super(cause);
		}

		public ClientException(String message) {
			super(message);
		}
	}

	/**
	 * This exception is thrown when dependencywatcher.com is not available
	 */
	public static class NotAvailableException extends ClientException {
		private static final long serialVersionUID = 1L;

		public NotAvailableException(Throwable cause) {
			super(cause);
		}
	}

	/**
	 * This exception is thrown when an API callback returns error
	 */
	public static class APICallException extends ClientException {
		private static final long serialVersionUID = 1L;

		public APICallException(String message) {
			super(message);
		}
	}
}
