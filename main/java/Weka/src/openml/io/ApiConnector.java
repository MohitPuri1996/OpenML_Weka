package openml.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import openml.algorithms.Hashing;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import weka.core.Instances;

public class ApiConnector {
	private static final String API_URL = "http://www.openml.org/api/";
	
	private static HttpClient httpclient;
	
	public static String openmlAuthenticate( String username, String password ) throws IOException, NoSuchAlgorithmException {
		List<NameValuePair> params = new ArrayList<NameValuePair>(2);
		params.add(new BasicNameValuePair("username", username));
		params.add(new BasicNameValuePair("password", Hashing.md5( password )));
		return doApiRequest("openml.authenticate", "", new UrlEncodedFormEntity(params, "UTF-8"));
	}
	
	public static String openmlDataDescription( int did ) throws IOException {
		return doApiRequest("openml.data.description", "&data_id=" + did );
	}
	
	public static Instances getDatasetFromUrl( String url ) throws IOException {
		URL openml_url = new URL(url);
		URLConnection conn = openml_url.openConnection();
		BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		Instances dataset = new Instances(br);
		return dataset;
	}
	
	private static String doApiRequest(String function, String queryString) throws IOException {
		return doApiRequest(function, queryString, null);
	}
	
	private static String doApiRequest(String function, String queryString, HttpEntity entity) throws IOException {
		String result = "";
		httpclient = new DefaultHttpClient();
		String requestUri = API_URL + "?f=" + function + queryString;
		try {
            HttpPost httppost = new HttpPost( requestUri );
            
            if(entity != null) {
            	httppost.setEntity(entity);
            }
            
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity resEntity = response.getEntity();
            
            if (resEntity != null) {
            	result = httpEntitiToString(resEntity);
            } else {
            	// TODO: throw exception.  
            }
		} finally {
            try { httpclient.getConnectionManager().shutdown(); } catch (Exception ignore) {}
        }
		return result;
	}
	
	private static String httpEntitiToString(HttpEntity resEntity) throws IOException {
		String result = "";
		BufferedReader br = new BufferedReader( new InputStreamReader( resEntity.getContent() ) );
        while( br.ready() ) result += br.readLine();
        return result;
	}
}
