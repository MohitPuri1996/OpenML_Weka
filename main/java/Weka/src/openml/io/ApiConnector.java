package openml.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import openml.algorithms.Hashing;
import openml.constants.Settings;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import weka.core.Instances;

public class ApiConnector {
	private static final String API_URL = Settings.BASE_URL + "api/";
	
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
	
	public static String openmlTasksSearch( int task_id ) throws IOException {
		return doApiRequest("openml.tasks.search", "&task_id=" + task_id );
	}
	
	public static String openmlDataUpload( File description, File dataset, String session_hash ) throws IOException {
		MultipartEntity params = new MultipartEntity();
		params.addPart("description", new FileBody(description));
		params.addPart("dataset", new FileBody(dataset));
		params.addPart("session_hash",new StringBody(session_hash));
        return doApiRequest("openml.data.upload", "", params);
	}
	
	public static String openmlImplementationUpload( File description, File binary, File source, String session_hash ) throws IOException {
		MultipartEntity params = new MultipartEntity();
		params.addPart("description", new FileBody(description));
		if(source != null)
			params.addPart("source", new FileBody(source));
		if(binary != null)
			params.addPart("binary", new FileBody(binary));
		params.addPart("session_hash",new StringBody(session_hash));
        return doApiRequest("openml.implementation.upload", "", params);
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
