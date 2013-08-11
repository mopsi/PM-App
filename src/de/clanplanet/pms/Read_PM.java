package de.clanplanet.pms;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.ClientProtocolException;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class Read_PM extends Activity {
	
	// Gespeicherter Benutzername...
	SharedPreferences pref1;
	
	// ActionBar zum "zurueckblaettern"
	ActionBar actionBar;
	
	// Gespeichertes Passwort...
	SharedPreferences pref2;
	
	// Elemente ...
	TextView from_id;
	TextView betreff_id;
	TextView date_view;
	ScrollView naricht_id;
	ProgressBar prog;
	
	// Httprequests erstellen
	Httprequests req;
	
	// Intent initialisieren...
	Intent intent;

	// Pattern erstellen und Matcher + Regex
	Pattern p;
	Matcher m;
	String regex;
	
	// Bundle fuer absender, betreff, link, datum...
	Bundle extras;	
	
	// Handler erstellen
	Handler h;
	
	// Strings initialisieren
	String absender;
	String betreff;
	String link;
	String date;
	
	// Response String variable...
	String data;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_read__pm);
		
		actionBar = getActionBar();
		
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		// Bekommener intent...
		intent = getIntent();
		
		regex = "<td class=\"lcell[ab]\"><div class=\"mark\" style=\"text-align: center\">(.*)</div>(.*)</td>";
		p = Pattern.compile(regex);
		
		// Handler initialisieren
		h = new Handler();
		
		// Httprequests req initialisieren...
		req = new Httprequests("http://www.clanplanet.de");
		
		// Benutzernamen und Passwort initialisieren...
		pref1 = getSharedPreferences("username_clanplanet_pms", MODE_PRIVATE);
		pref2 = getSharedPreferences("passwort_clanplanet_pms", MODE_PRIVATE);
		
		// Elemente initialisieren...
		from_id    = (TextView) findViewById(R.id.from_id);
		betreff_id = (TextView) findViewById(R.id.betreff_id);
		date_view  = (TextView) findViewById(R.id.datum);
		naricht_id = (ScrollView) findViewById(R.id.naricht_feld_id);
		prog       = new ProgressBar(this);
		
		// Extra Bundle wird initialisiert...
		extras = intent.getExtras();
		
		// Strings erstellen fuer absender, betreff, link, datum
		absender = extras.getString("absender");
		link     = extras.getString("link");
		betreff  = extras.getString("betreff");
		date     = extras.getString("datum");
		
		// Strings in die noetigen Felder eintragen ...
		from_id.setText("Von: " + absender);
		betreff_id.setText("Betreff: " + betreff);
		date_view.setText("Am: " + date);	
		
		// Progressbar setzen...
		naricht_id.addView(prog);
		
		// Versuche request durchzufuehren...
		try {
			data = req.postLoginClanplanet(pref1.getString("username_clanplanet_pms", ""), pref2.getString("passwort_clanplanet_pms", ""));
			
			if(data.indexOf("Eingeloggt als " + pref1.getString("username_clanplanet_pms", "")) > -1) {
				// Eingeloggt...
				h.postDelayed(new Runnable() {
					
					public void run() {
						
						// Versuche Request...
						try {
							data = req.refresh_page("http://www.clanplanet.de/personal/" + link);
						
							m = p.matcher(data);
							
							if(m.find()) {
								naricht_id.removeAllViews();
								TextView text = new TextView(getApplicationContext());
								text.setText(Html.fromHtml(m.group(2)));
								text.setMovementMethod(LinkMovementMethod.getInstance());
								text.setTextColor(getResources().getColor(R.color.black));
								text.setTextSize(20);
								naricht_id.addView(text);
							}
						} catch (ClientProtocolException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
				}, 3 * 1000);
			}
			else {
				// Nicht eingeloggt... sollte nicht passieren !!
				Toast.makeText(getApplicationContext(), "Ein Fehler ist aufgetreten. Du wurdest ausgeloggt...", Toast.LENGTH_LONG).show();
				finish();
			}
			
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.read__pm, menu);
		return true;
	}

}
