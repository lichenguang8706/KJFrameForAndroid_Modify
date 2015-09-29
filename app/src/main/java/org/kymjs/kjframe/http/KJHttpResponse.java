package org.kymjs.kjframe.http;

import java.io.InputStream;
import java.util.Map;

/**
 * 此类用于替换HttpClient中的HttpResponse，最大程度减少HTTP框架与HttpClient的黏连程度，从而达到适配Android6.0的目的
 * @author 我是无名氏
 */
public class KJHttpResponse {
	
	/**
	 * 消息头
	 */
	private Map<String, String> headers;
	
	/**
	 * 状态码
	 */
	private int statusCode;
	
	/**
	 * 响应消息
	 */
	private String responseMessage;
	
	/**
	 * 报文流
	 */
	private InputStream content;
	
	/**
	 * 报文编码
	 */
	private String contentEncoding;
	
	/**
	 * 报文类型
	 */
	private String contentType;
	
	/**
	 * 报文长度
	 */
	private long contentLength;

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public String getResponseMessage() {
		return responseMessage;
	}

	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}

	public InputStream getContent() {
		return content;
	}

	public void setContent(InputStream content) {
		this.content = content;
	}

	public String getContentEncoding() {
		return contentEncoding;
	}

	public void setContentEncoding(String contentEncoding) {
		this.contentEncoding = contentEncoding;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public long getContentLength() {
		return contentLength;
	}

	public void setContentLength(long contentLength) {
		this.contentLength = contentLength;
	}
}
