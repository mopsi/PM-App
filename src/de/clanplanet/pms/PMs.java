package de.clanplanet.pms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.ClientProtocolException;

import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.text.Html;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class PMs extends Activity {	
	// Handler fuer zeitverzoegerung erstellen
	Handler h;
	
	// Matcher fuer die Liste der PM's
	Matcher m1;
	
	// Der Username
	SharedPreferences pref1;
	
	// Das Passwort...
	SharedPreferences pref2;
	
	// Pattern
	Pattern p;
	
	// PendingIntent fuer den Service...
	PendingIntent pIntent;
	
	//Matcher 
	Matcher matcher;
	
	// Username
	String username;
	
	// Strings fuer PM's
	String writer;
	String betreffPM;
	String linkInbox;
	String datum;
	
	// Passwort
	String passwort;
	
	// Listen fuer die PM's
	ArrayList <String> name;
	ArrayList <String> link;
	ArrayList <String> betreff;
	ArrayList <String> date;
	
	// Zeichenkette fuer den Response von den Server Requests...
	String data;
	
	// Fuer Schleife...
	int index;
	
	// Requests Klasse...
	Httprequests req;
	
	// Service...
	PendingIntent pint;
	
	// Alarmmanager zum starten des Service erstellen...
	AlarmManager alarm;	
	
	// Notification Manager
	NotificationManager nMgr;
	
	// PendingIntent fuer den Logout
	PendingIntent service;
	
	// ID der Notification
	int NOTIFICATION_ID = 100;
	
	// Menue fuer Logout, usw...
	Menu men;
	
	// ProgressDialog erstellen
	ProgressDialog prog;
	
	// ScrollView setzen...
	LinearLayout view_scroll;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		/*
		 * Strict Mode aktivierung.
		 * Dient zur Fehlerbehandlung damit die PM nicht direkt
		 * wieder stoppt sobald sie aufgerufen wird.
		 * 
		 */
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build();	
		StrictMode.setThreadPolicy(policy);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pms);
		
		view_scroll = (LinearLayout) findViewById(R.id.linlayout);
		
		// Notification Manager initialisieren...
		nMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		
		// Notification schließen...
		nMgr.cancel(NOTIFICATION_ID);
		
		// Benutzernamen Preference initialisieren.
		pref1 = getSharedPreferences("username_clanplanet_pms", MODE_PRIVATE);
		
		// Passwort Preference initialisieren.
		pref2 = getSharedPreferences("passwort_clanplanet_pms", MODE_PRIVATE);
		
		// Gespeicherten Benutzernamen bekommen.
		username = pref1.getString("username_clanplanet_pms", "");
		
		// Gespeichertes Passwort bekommen.
		passwort = pref2.getString("passwort_clanplanet_pms", "");
		
		// Httprequests einleiten...
		req = new Httprequests("http://www.clanplanet.de");
		
		// Handler erstellen...
		h = new Handler();
		
		// AlarmManager initialisieren
		alarm = (AlarmManager) getSystemService(ALARM_SERVICE);

		// Pattern wird gesetzt
		p = Pattern.compile("<tr>\\s*<th>\\s*Absender\\s*</th>\\s*<th>Betreff</th>\\s*<th>Datum</th>\\s*</tr>");
		
		// Progress initialisieren
		// pms.setText("Clanplanet PM's werden geprueft...");
		prog = new ProgressDialog(this);
		prog.setCancelable(false);
		prog.getWindow().setGravity(Gravity.CENTER);
		prog.setTitle("Clanplanet PM's werden geprüft...");
		prog.setMessage("PM's werden geladen...");
		prog.show();
		
		// Intent fuer Service
		Intent intent = new Intent(getApplicationContext(), Service_PM.class);
		
		// Pending Intent erstellen fuer Service stopp...
		pint = PendingIntent.getService(getApplicationContext(), 0, intent, 0);
		
		// Service stoppen...
		alarm.cancel(pint);
		
		/*
		 * Versuche einen Request zu erstellen
		 * ansonsten beende mit Fehlermeldung...
		 * 
		 */
		try {
			
			// Rueckgabe wird gespeichert in data String...
			data = req.postLoginClanplanet(username, passwort);
			
			/*
			 * Pruefen ob eingeloggt...
			 * 
			 */
			if(data.indexOf("Eingeloggt als " + username) > -1) {
				// Eingeloggt...
				
				/*
				 * Alle 30 Sekunden abfragen ob PM erhalten !...
				 * hier 2 Sekunden Zeitverzoegerung.
				 * 
				 */
				h.postDelayed(new Runnable() {
				
					public void run() {
					
						// Abfragen nach der Inbox von CP
						try {
							data = req.refresh_page("http://www.clanplanet.de/personal/inbox.asp?rn=");							
							
							matcher = p.matcher(data);
							
							if(matcher.find()) {
								// PM erhalten !	
								// Ausgabe das neue PM erhalten wurde 
								// und verlinkung per HTML 
								// auf die Clanpanet Hauptseite !
								String reg = "<tr>\\s*<td class=\"lcell[ab]\" nowrap><span class=\"small\"><span >(.*)</span></span></td>\\s*<td class=\"lcell[ab]\" width=\"100%\"><span class=\"small\">\\s*(?:<span class=\"(?:mark|unalert|alert)\">[!#]</span>\\s*)?<a href=\"(.*)\"><span >(.*)</span></a>\\s*</span></td>\\s*<td class=\"lcell[ab]\" width=\"120\" nowrap><span class=\"small\"><span >(.*)</span></span></td>\\s*</tr>";
								
								Pattern p1 = Pattern.compile(reg);
								m1 = p1.matcher(data);
								name = new ArrayList<String>();
								link = new ArrayList<String>();
								betreff = new ArrayList<String>();
								date = new ArrayList<String>();
								index = 0;
								view_scroll.removeAllViews();
								while(m1.find()) {
									// 1 = Name
									// 2 = Link
									// 3 = Betreff
									// 4 = Datum
									name.add(index, m1.group(1));
									link.add(index, m1.group(2));
									betreff.add(index, m1.group(3));
									date.add(index, m1.group(4));
									final TextView text = new TextView(getApplicationContext());
									writer = name.get(index);
									linkInbox = link.get(index);
									betreffPM = betreff.get(index);
									datum     = date.get(index);
									text.setId(index);
									text.setText(Html.fromHtml("<u>Von: " + writer + "<br>Betreff: " + betreffPM + "<br>Datum: " + datum + "</u><br><br>"));
									text.setTextSize(18);
									text.setTextColor(getResources().getColor(R.color.blue));
									text.setOnClickListener(new View.OnClickListener() {
										
										@Override
										public void onClick(View v) {
											readNewPm(text.getId());
										}
									});
									view_scroll.addView(text, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
									index++;
								}
								prog.cancel();
							}
							else {
								// Keine neue PM erhalten...
								view_scroll.removeAllViews();
								TextView textView = new TextView(getApplicationContext());
								textView.setText("Keine neue PM erhalten !");
								textView.setTextSize(18);
								textView.setTextColor(getResources().getColor(R.color.black));
								view_scroll.addView(textView);
								prog.cancel();
							}
							
							// Alle 30 Sekunden abfragen...
							h.postDelayed(this, 30 * 1000);
							
						} catch (ClientProtocolException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
					
				}, 2000);			
			}
			else {
				// Nicht eingeloggt... (Sollte niemals passieren ... !)
				// Fehlermeldung wird ausgegegeben... 
				Toast.makeText(getApplicationContext(), "Unbekannter Fehler...", Toast.LENGTH_LONG).show();
				// Zeit verzoegerung von 4s
				h.postDelayed(new Runnable() {
					public void run() {
						
						/*
						 * Programm wird beendet...
						 * 
						 */
						finish();
						
					}
				}, 4 * 1000);
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
	public void onPause() {
		super.onPause();
		
		// Neuen Intent erstellen
		Intent intent = new Intent();
		
		// Intent den Service mitgeben...
		intent.setClass(getApplicationContext(), Service_PM.class);
		
		// Pending Intent erstellen zum starten des Service
		pIntent = PendingIntent.getService(getApplicationContext(), 0, intent, 0);
		
		// Service ueber Alarmmanager starten...
		alarm.set(AlarmManager.RTC_WAKEUP, 0, pIntent);
	}
	
	public void readNewPm(int i) {
		// Neue Activity muss sich oeffnen die PM zum lesen oeffnet...
		Intent intent_read = new Intent(this, Read_PM.class);
		intent_read.putExtra("absender", name.get(i));
		intent_read.putExtra("link", link.get(i));
		intent_read.putExtra("betreff", betreff.get(i));
		intent_read.putExtra("datum", date.get(i));
		intent_read.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent_read);
	}
	
	Intent intent;
	
	@Override
	public boolean onOptionsItemSelected(MenuItem inputs) {
		
		switch(inputs.getItemId()) {
			case R.id.readed_pms_show: 
				// Gelesene PM's activity anzeigen !
				intent = new Intent(this, ReadedPMs.class);
				startActivity(intent);
			break;
			case R.id.new_pm_write:
				intent = new Intent(this, NewPM.class);
				intent.putExtra("absender", "");
				intent.putExtra("betreff", "");
				intent.putExtra("link", "");
				startActivity(intent);
			break;	
			case R.id.contacts_menu:
				intent = new Intent(this, Kontakte.class);
				startActivity(intent);
			break;			
			case R.id.read_sended_pms:
				intent = new Intent(this, GesendeteNarichten.class);
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
								service = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(getApplicationContext(), Service_PM.class), 0);
								
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
			case R.id.show_help:
				AlertDialog.Builder builder_ = new AlertDialog.Builder(this);
				builder_.setTitle("Über Clanplanet PM's App");
				TextView text = new TextView(this);
				text.setText(Html.fromHtml("Die Clanplanet PM's App ist eine \"Open Source App\" für die Plattform Clanplanet." +
							  "<br>" +
							  "<br>" +
							  "Die App wurde entwickelt um Android Nutzern das PM Center von www.clanplanet.de zu erleichtern. Sie erfüllt die hauptsächlichen Funktionen des Clanplanet PM Centers." +
							  "<br>" +
							  "Was enthält die App für Funktionen (was kann sie mir bieten) ?" +
							  "<br>" +
							  "Die App erfüllt die folgenden Funktionen:" +
							  "<br>" +
							  	"- Benarichtigung bei neuer PM<br>" +
							    "- Lesen der neuen PM's<br>" +
							    "- Direktes Antworten auf PM's<br>" +
							    "- Gelesene PM's anzeigen<br>" +
							    "- Gesendete PM's anzeigen<br>" +
							    "- Kontakten PM's schreiben, editieren und löschen<br>" +
							    "- Schreiben neuer PM's<br>"));
				text.setTextColor(getResources().getColor(R.color.black));
				ScrollView view_scroll_ = new ScrollView(this);
				view_scroll_.addView(text);
				builder_.setView(view_scroll_);
				builder_.setPositiveButton("Okay", new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int arg1) {
						dialog.cancel();
					}
				});
				builder_.show();
			break;
		}
		
		return true;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu, menu);
		
		ActionBar bar = getActionBar();
		bar.setDisplayHomeAsUpEnabled(false);
		menu.getItem(0).setVisible(false); 
		
		return true;
	}
	
}