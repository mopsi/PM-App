package de.clanplanet.pms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.ClientProtocolException;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ReadedPMs extends Activity {

	// Handler erstellen...
	Handler h;
	
	// Preferences erstellen
	SharedPreferences pref1;
	SharedPreferences pref2;
	
	// Httprequests erstellen
	Httprequests req;
	
	// Strings...
	String username;
	String passwort;
	String data;
	String regex;
	
	// Variablen fuer Suchmuster
	Matcher m;
	Pattern p;
	int index;
	
	// Elemente...
	LinearLayout view_scroll;
	
	// Arrays...
	ArrayList <String> name;
	ArrayList <String> datum;
	ArrayList <String> link;
	ArrayList <String> betreff;
	
	// ProgressDialog erstellen
	ProgressDialog progress;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_readed_pms);
		
		// Handler initialisieren
		h = new Handler();
		
		// ProgressDialog initialisieren...
		progress = new ProgressDialog(this);
		
		// Httprequests initialisieren...
		req = new Httprequests("http://www.clanplanet.de");
		
		alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
		
		// Array initialisieren...
		name = new ArrayList <String>();
		link = new ArrayList <String>();
		datum = new ArrayList <String>();
		betreff = new ArrayList <String>();
		
		// Progress Settings
		progress.setCancelable(false);
		progress.setMessage("Gelesene PM's werden geladen... Dies kann eine Weile dauern...");
		progress.setTitle("Clanplanet PM's App");
		
		// Preferences initialisieren...
		pref1 = getSharedPreferences("username_clanplanet_pms", MODE_PRIVATE);
		pref2 = getSharedPreferences("passwort_clanplanet_pms", MODE_PRIVATE);
		
		// Elemente initialisieren...
		view_scroll = (LinearLayout) findViewById(R.id.scrollViewPMsReaded);
		
		// Strings initialisieren
		username = pref1.getString("username_clanplanet_pms", "");
		passwort = pref2.getString("passwort_clanplanet_pms", "");
		
		// Requests und ProgressDialog zeigen
		progress.show();
		h.postDelayed(new Runnable() {
			public void run() {
				if(isOnline()) {
					// Versuche Request...
					try {
						data = req.postLoginClanplanet(username, passwort);
						
						if(data.indexOf("Eingeloggt als " + username) > -1) {
							// Eingeloggt...
							regex = "<tr>\\s*<th>\\s*Absender\\s*</th>\\s*<th>\\s*Betreff\\s*</th>\\s*<th>\\s*Datum\\s*</th>\\s*</tr>";
							data  = req.refresh_page("http://www.clanplanet.de/personal/inbox.asp?rn=&folder=read");
							p = Pattern.compile(regex);
							m = p.matcher(data);
							if(m.find()) {
								// Alte PM's gefunden...
								regex = "<tr>\\s*<td class=\"lcell[ab]\" nowrap><span class=\"small\"><span >(.*)</span></span></td>\\s*<td class=\"lcell[ab]\" width=\"100%\"><span class=\"small\">\\s*(?:<span class=\"(?:mark|unalert|alert)\">[!#]</span>\\s*)?<a href=\"(.*)\"><span >(.*)</span></a>\\s*</span></td>\\s*<td class=\"lcell[ab]\" width=\"120\" nowrap><span class=\"small\"><span >(.*)</span></span></td>\\s*</tr>";
								p = Pattern.compile(regex);
								m = p.matcher(data);
								index = 0;
								while(m.find()) {
									if(index == 100) {
										break;
									}
									name.add(index, m.group(1));
									link.add(index, m.group(2));
									betreff.add(index, m.group(3));
									datum.add(index, m.group(4));
									final TextView text = new TextView(getApplicationContext());
									text.setText(Html.fromHtml("<u>Von: " + name.get(index) + "<br>Betreff: " + betreff.get(index) + "<br>Datum: " + datum.get(index) + "</u><br><br>"));
									text.setTextSize(18);
									text.setId(index);
									text.setTextColor(getResources().getColor(R.color.blue));
									text.setOnClickListener(new View.OnClickListener() {
										
										@Override
										public void onClick(View v) {
											readNewPm(text.getId());
										}
									});
									view_scroll.addView(text);
									index++;
								}
								progress.cancel();
							}
							else {
								// Keine gelesenen PM's gefunden...
								TextView text = new TextView(ReadedPMs.this);
								text.setText("Keine Gelesenen PM's vorhanden...");
								text.setTextSize(18);
								view_scroll.addView(text);
								progress.cancel();
							}
						}
						else {
							// Nicht eingeloggt...sollte nicht vorkommen
							Toast.makeText(getApplicationContext(), "Unbekannter Fehler, du wurdest ausgeloggt...",  Toast.LENGTH_LONG).show();
							progress.cancel();
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
				else {
					Toast.makeText(getApplicationContext(), "Deine Interverbindung ist abgebrochen...", Toast.LENGTH_LONG).show();
				}
			}
		}, 5 * 1000);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu, menu);
		menu.getItem(0).setVisible(false);
		ActionBar bar = getActionBar();
		bar.setDisplayHomeAsUpEnabled(true);
		return true;
	}
	
	public void readNewPm(int i) {
		// Neue Activity muss sich oeffnen die PM zum lesen oeffnet...
		Intent intent_read = new Intent(this, Read_PM.class);
		intent_read.putExtra("absender", name.get(i));
		intent_read.putExtra("link", link.get(i));
		intent_read.putExtra("betreff", betreff.get(i));
		intent_read.putExtra("datum", datum.get(i));
		intent_read.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent_read);
	}
	
	public boolean isOnline() {
	    ConnectivityManager cm =
	        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isAvailable() && netInfo.isConnected()) {
	        return true;
	    }
	    return false;
	}
	
	Intent intent;
	PendingIntent service;
	AlarmManager alarm;
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.readed_pms_show: 
				// Gelesene PM's activity anzeigen !
				intent = new Intent(this, ReadedPMs.class);
				startActivity(intent);
			break;
			case R.id.logout_item:
				DialogInterface.OnClickListener onClicklistener = new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch(which) {
							case DialogInterface.BUTTON_NEGATIVE :
								// Nein gedrueckt...
								// Dialog schließen da Logout nicht beabsichtigt...
								dialog.dismiss();
								
							break;
							case DialogInterface.BUTTON_POSITIVE :
								// Ja gedrueckt...
								// Login Intent wird gestartet und sharedpreferences
								// (Benutzername und Passwort) werden zurueckgesetzt...
								pref1.edit().putString("username_clanplanet_pms", "").commit();
								pref2.edit().putString("passwort_clanplanet_pms", "").commit();
								SharedPreferences isNoti;
								isNoti = getSharedPreferences("isNoti_clanplanet_pms", MODE_PRIVATE);
								isNoti.edit().putBoolean("isNoti_clanplanet_pms", false).commit();
								
								// Service bekommen...
								service = PendingIntent.getService(getApplicationContext(), 0, new Intent(getApplicationContext(), Service_PM.class), 0);
								
								// Service stoppen...
								alarm.cancel(service);
								
								// Login Activity starten !
								startActivity(new Intent(getApplicationContext(), Main.class));
								
								// Dialog wird geschlossen...
								dialog.dismiss();
							break;
						}
					}
				};
				
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage("Willst du dich wirklich ausloggen ?")
					   .setNegativeButton("Nein", onClicklistener)
					   .setPositiveButton("Ja", onClicklistener)
					   .show();
			break;
			case android.R.id.home:
				onBackPressed();
			break;			
			case R.id.new_pm_write:
				intent = new Intent(this, NewPM.class);
				intent.putExtra("absender", "");
				intent.putExtra("betreff", "");
				intent.putExtra("link", "");
				startActivity(intent);
			break;
		}
		
		return true;
	}
}
