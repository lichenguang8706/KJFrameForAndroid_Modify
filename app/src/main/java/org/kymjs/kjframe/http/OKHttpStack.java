package org.kymjs.kjframe.http;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 如果想要集成OKHttp，这里提供一个入口，集成的代码自己写，不会写到网上搜，一搜一大推
 * 然后找到HttpConfig中的httpStackFactory()方法，将new HttpConnectStack()替换成new OKHttpStack()
 * @author 我是无名氏
 *
 */
public class OKHttpStack extends HttpConnectStack {
	
	public OKHttpStack() {
		
	}

	@Override
	public HttpURLConnection openConnection(URL url, Request<?> request)
			throws IOException {
		
		return super.openConnection(url, request);
	}
}
