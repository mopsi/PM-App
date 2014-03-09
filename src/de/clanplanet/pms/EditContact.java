package de.clanplanet.pms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class EditContact extends Activity {
	
	// Strings erstellen...
	String notiz;
	String name;
	String userid;
	String data;
	String user;
	String pw;
	String regex;
	
	// Matcher und Pattern erstellen
	Matcher m;
	Pattern p;
	
	// Preferences erstellen
	SharedPreferences pref1;
	SharedPreferences pref2;
	
	// Requests erstellen
	Httprequests req;
	
	// Elemente
	EditText name_edit;
	EditText notiz_edit;
	Button edit;
	CheckBox box;
	
	// Handler und Progress Dialog erstellen
	Handler h;
	ProgressDialog progress;

	// Intent...
	Intent intent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_contact);
		intent = getIntent();
		
		alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
		
		// Handler setzen und Progress Dialog setzen und bearbeiten !
		h = new Handler();
		progress = new ProgressDialog(this);
		progress.setTitle("Clanplanet PM's App");
		progress.setMessage("Die Änderungen werden übernommen !");
		progress.setCancelable(false);
		
		// Elemente setzen !
		name_edit = (EditText) findViewById(R.id.name_edit_contact);
		notiz_edit = (EditText) findViewById(R.id.notiz_edit_contact);
		edit = (Button) findViewById(R.id.edit_contacts_button);
		
		// Strings setzen !
		notiz  = intent.getExtras().getString("notiz");
		name   = intent.getExtras().getString("name");
		userid = intent.getExtras().getString("userid");
		
		notiz = notiz.replace("&#228;", "Ä");
		notiz = notiz.replace("&#196;", "ä");
		notiz = notiz.replace("&#246;", "Ö");
		notiz = notiz.replace("&#214;", "ö");
		notiz = notiz.replace("&#252;", "Ü");
		notiz = notiz.replace("&#220;", "ü");
		
		name_edit.setText(name);
		notiz_edit.setText(notiz);
		
		// Preferences initialisieren
		pref1 = getSharedPreferences("username_clanplanet_pms", MODE_PRIVATE);
		pref2 = getSharedPreferences("passwort_clanplanet_pms", MODE_PRIVATE);
		
		// Requests initialisieren
		req = new Httprequests("http://www.clanplanet.de");
		
		user = pref1.getString("username_clanplanet_pms", "");
		pw = pref2.getString("passwort_clanplanet_pms", "");
		
		// Regex initialisieren...
		regex = "Eingeloggt als <b>" + user + "</b>";
		
		edit.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				progress.show();
				h.postDelayed(new Runnable() {
					public void run() {
						try {
							data = req.postLoginClanplanet(user, pw);
							
							if(data.indexOf(regex) > -1) {
								// Eingeloggt
								
								ArrayList<NameValuePair> listederdaten = new ArrayList<NameValuePair>();
								
								data = req.refresh_page("http://www.clanplanet.de/personal/inbox_book.asp?rn=&betreff=&text=&action=edit&id=" + userid);
								p = Pattern.compile("<input type=\"hidden\" name=\"einstufung\" value=\"(.*)\">");
								m = p.matcher(data);
								
								if(m.find()) {
									
									String einstufung = m.group(1);
									
									listederdaten.add(new BasicNameValuePair("id", userid));
									listederdaten.add(new BasicNameValuePair("markname", name_edit.getText().toString()));
									listederdaten.add(new BasicNameValuePair("einstufung", einstufung));
									listederdaten.add(new BasicNameValuePair("marknotiz", notiz_edit.getText().toString()));
									listederdaten.add(new BasicNameValuePair("freundesliste", ""));
									
									data = req.post(listederdaten, "http://www.clanplanet.de/personal/inbox_book.asp?rn=&action=update");
									// Alles richtig !
									progress.cancel();
									Toast.makeText(getApplicationContext(), "Kontakt erfolgreich bearbeitet !", Toast.LENGTH_LONG).show();
									Intent intent = new Intent(EditContact.this, Kontakte.class);
									startActivity(intent);	
								}	
								else {
									Toast.makeText(getApplicationContext(), "Ein Fehler ist aufgetreten !", Toast.LENGTH_LONG).show();
								}
							}
							else {
								// Nicht eingeloggt ... sollte nicht passieren !
								Toast.makeText(getApplicationContext(), "Unbekannter Fehler, du wurdest ausgeloggt !", Toast.LENGTH_LONG).show();
								finish();
							}
						} catch (ClientProtocolException e) {
							Toast.makeText(getApplicationContext(), "Keine Internetverbindung vorhanden !", Toast.LENGTH_LONG).show();
						} catch (IOException e) {
							Toast.makeText(getApplicationContext(), "Keine Internetverbindung vorhanden !", Toast.LENGTH_LONG).show();
						}
					}
				}, 3 * 1000);
			}
			
		});
		
	}
	
	AlarmManager alarm;
	PendingIntent service;
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		
			case android.R.id.home:
				onBackPressed();		
			return true;			
			case R.id.readed_pms_show: 
				// Gelesene PM's activity anzeigen !
				intent = new Intent(this, ReadedPMs.class);
				startActivity(intent);
			return true;
			case R.id.contacts_menu:
				intent = new Intent(this, Kontakte.class);
				startActivity(intent);
			return true;
			case R.id.read_sended_pms:
				intent = new Intent(this, GesendeteNarichten.class);
				startActivity(intent);
			return true;
			case R.id.logout_item:
				DialogInterface.OnClickListener onClicklistener = new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch(which) {
							case DialogInterface.BUTTON_NEGATIVE :
								// Nein gedrueckt...
								// Dialog schlieÃŸen da Logout nicht beabsichtigt...
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
				return true;
			case R.id.new_pm_write:
				intent = new Intent(this, NewPM.class);
				intent.putExtra("absender", "");
				intent.putExtra("betreff", "");
				intent.putExtra("link", "");
				startActivity(intent);
			return true;			
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
			return true;
			default:
			
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		menu.getItem(0).setVisible(false);
		ActionBar bar = getActionBar();
		bar.setDisplayHomeAsUpEnabled(true);
		return true;
	}
}
