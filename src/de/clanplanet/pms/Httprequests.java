package de.clanplanet.pms;

/*
 * Noetige packages werden geladen...
 * 
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

/*
 * Klasse fuer die verschiedenen Abfragen an Clanplanet. 
 * 
 */

public class Httprequests {
	// URL einspeichern in ein String
	String url = ""; 
	
	// Der Client der auh Cookies speichert...
	DefaultHttpClient client = new DefaultHttpClient();
	
	// HttpPost fuer die Posts an Clanplanet
	HttpPost post;

	// Constructor (Einleiter Methode)
	public Httprequests(String url) {
		this.url = url;
	}
	
	// Methode zur Client uebergabe...
	public DefaultHttpClient getClient() {
		return this.client;
	}
	
	// Login Post Methode...
	public String postLoginClanplanet(String user, String pw) throws ClientProtocolException, IOException {
		
		// Post Variable wird initialisiert
		post = new HttpPost(this.url);
		
		// Neue Liste wird erstellt...
		List<NameValuePair> listederdaten = new ArrayList<NameValuePair>();
		listederdaten.add(new BasicNameValuePair("session", "start"));
		listederdaten.add(new BasicNameValuePair("kennung", user));
		listederdaten.add(new BasicNameValuePair("passwort", pw));
		
		// Uebergabe der input felder (der liste) as Clanplanet benoetigt...
		post.setEntity(new UrlEncodedFormEntity(listederdaten));
		
		// Die Rueckgabe vom Client...
		HttpResponse response = client.execute(post);
		
		// Buffer zum lesen von dem response
		BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		StringBuffer sb = new StringBuffer();
		String l = "";
		String nl = System.getProperty("line.separator");
		while((l = in.readLine()) != null) {
			sb.append(l + nl);
		}
		in.close();
		String data = sb.toString();
		
		// Cookies vom Client bekommen und wieder setzen.
		CookieStore cooks = client.getCookieStore();
		client.setCookieStore(cooks);
		
		// response zurueckgeben.
		return data;
	}
	
	// Refresh Page zur abfrage nach PM's
	public String refresh_page(String url) throws ClientProtocolException, IOException {
		
		// URLwird neu gesetzt.
		this.url = url;
		
		// Post wird initialisiert...
		post = new HttpPost(this.url);
		
		// response wird gesetzt.
		HttpResponse response = client.execute(post);
		
		// Buffer zum lesender seiten rueckgabe
		BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		StringBuffer sb = new StringBuffer();
		String l = "";
		String nl = System.getProperty("line.separator");
		while((l = in.readLine()) != null) {
			sb.append(l + nl);
		}
		in.close();
		String data = sb.toString();
		
		// response zurueckgeben.
		return data;
	}

	public String postPm(String url, String betreff, String text, String userid, String value) throws ClientProtocolException, IOException {
		
		// URL wird neu gesetzt.
		this.url = url;
		
		// Post wird initialisiert...
		post = new HttpPost(this.url);
		
		List<NameValuePair> list = new ArrayList<NameValuePair>();

		list.add(new BasicNameValuePair("userid", URLDecoder.decode(userid, "UTF-8")));

		list.add(new BasicNameValuePair("text", URLDecoder.decode(text, "UTF-8")));

		list.add(new BasicNameValuePair("betreff", URLDecoder.decode(betreff, "UTF-8")));

		list.add(new BasicNameValuePair("receiver_list_number", URLDecoder.decode(value, "UTF-8")));
		// Uebergabe der input felder (der liste) as Clanplanet benoetigt...
		post.setEntity(new UrlEncodedFormEntity(list));
		
		// response wird gesetzt.
		HttpResponse response = client.execute(post);

		// Buffer zum lesender seiten rueckgabe
		BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		StringBuffer sb = new StringBuffer();
		String l = "";
		String nl = System.getProperty("line.separator");
		while((l = in.readLine()) != null) {
			sb.append(l + nl);
		}
		in.close();
		String data = sb.toString();
		
		// response zurueckgeben.
		return data;
	}
	
	public String post(ArrayList<NameValuePair>listederdaten, String url) throws ClientProtocolException, IOException {
		
		// URL wird neu gesetzt.
		this.url = url;
		
		// Post wird initialisiert...
		post = new HttpPost(this.url);
		
		// Uebergabe der input felder (der liste) as Clanplanet benoetigt...
		post.setEntity(new UrlEncodedFormEntity(listederdaten));
		
		// response wird gesetzt.
		HttpResponse response = client.execute(post);

		// Buffer zum lesender seiten rueckgabe
		BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		StringBuffer sb = new StringBuffer();
		String l = "";
		String nl = System.getProperty("line.separator");
		while((l = in.readLine()) != null) {
			sb.append(l + nl);
		}
		in.close();
		String data = sb.toString();
		
		// response zurueckgeben.
		return data;
	}
}
