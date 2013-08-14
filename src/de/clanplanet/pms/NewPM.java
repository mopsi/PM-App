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
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class NewPM extends Activity {


	// ProgressDialog
	ProgressDialog progress;
	
	// ActionBar fuer homebutton
	ActionBar actionBar;
	
	// Intent erstellen...
	Intent intent;
	
	// Handler variable erstellen
	Handler h;
	
	// Preferences erstellen...
	SharedPreferences pref1;
	SharedPreferences pref2;
	
	// String erstellen
	String betreff;
	String absender;
	String text;
	String username;
	String passwort;
	String data;
	String regex;
	String value;
	String userid;
	String url;
	String link;
	
	// Suchmuster Variablen erstellen...
	Pattern p;
	Matcher m;
	
	// Element Variablen erstellen...
	EditText betreff_id;
	EditText absender_id;
	EditText naricht_id;
	Button   antworten;
	
	// Httprequests req variable erstellen...
	Httprequests req;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reply);
		
		// Preferences initialisieren...
		pref1 = getSharedPreferences("username_clanplanet_pms", MODE_PRIVATE);
		pref2 = getSharedPreferences("passwort_clanplanet_pms", MODE_PRIVATE);
		
		// Intent initialisieren...
		intent = getIntent();
		
		// Httprequests initialisieren...
		req = new Httprequests("http://www.clanplanet.de");
		
		// Progress initialisieren...
		progress = new ProgressDialog(this);
		
		// Handler initialisieren
		h = new Handler();
		
		// String initialisieren
		betreff  = intent.getExtras().getString("betreff");
		absender = intent.getExtras().getString("absender");
		link     = intent.getExtras().getString("link");
		username = pref1.getString("username_clanplanet_pms", "");
		passwort = pref2.getString("passwort_clanplanet_pms", "");
		regex    = "Eingeloggt als " + username;
		
		// Elemente initialisieren...
		betreff_id  = (EditText) findViewById(R.id.betreff_reply_id);
		absender_id = (EditText) findViewById(R.id.to_reply_id);
		naricht_id  = (EditText) findViewById(R.id.text_reply_id);
		
		alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
	}
	
	public void setzeDialog(String message) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this)
							.setCancelable(false)
							.setMessage(message)
							.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
								
								public void onClick(DialogInterface dialog, int which) {
									dialog.cancel();
								}
							});
		AlertDialog dialog1 = dialog.create();
		dialog1.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu, menu);
		
		actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		return true;
	}
	
	PendingIntent service;
	AlarmManager alarm;
	
	@Override 
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch(item.getItemId()) {
		
			case android.R.id.home:
				
				onBackPressed();
				
				return true;
			case R.id.reply:
				betreff  = betreff_id.getText().toString();
				absender = absender_id.getText().toString();
				text     = naricht_id.getText().toString();
				
				// Progress message setzen
				progress.setMessage("Bitte warten...");
				
				if(absender.isEmpty()) {
					// Kein Betreff eingegeben...
					setzeDialog("Du musst einen Betreff eintragen...");
				}
				else if(betreff.isEmpty()) {
					// Kein Absender eingegeben...
					setzeDialog("Du musst einen Empfänger eintragen...");
				}
				else if(text.isEmpty()) {
					// Kein Text eingegeben...
					setzeDialog("Du musst einen Text eingeben...");
				}
				else {
					// Alles korrekt
					// Progress zeigen...
					progress.show();
				
					h.postDelayed(new Runnable() {
						public void run() {
							
							// Versuche Request durchzuführen...
							try {
								// Speichere Rückgabe in data String...
								data = req.postLoginClanplanet(username, passwort);
								
								if(data.indexOf(regex) > -1) {
									// Eingeloggt...
									
									ArrayList<NameValuePair>liste_der_daten = new ArrayList<NameValuePair>();
									liste_der_daten.add(new BasicNameValuePair("name", absender));
									liste_der_daten.add(new BasicNameValuePair("suchen", "2"));
									
									data = req.post(liste_der_daten, "http://www.clanplanet.de/personal/inbox_book.asp?rn=&action=search");
									
									p = Pattern.compile("<td class=\"lcell[ab]\" width=\"40\">(.*)&nbsp;&nbsp;</td>");
									
									m = p.matcher(data);
									if(m.find()) {
																		
										url = "http://www.clanplanet.de/personal/sendmail.asp?rn=&action=send";
										userid = m.group(1);
										data = req.refresh_page("http://www.clanplanet.de/personal/sendmail.asp?rn=&betreff=&text=&userid=" + userid);
										p = Pattern.compile("<input type=\"hidden\" name=\"receiver_list_number\" value=\"(.*)\">");
										
										text = text.replace("\n", "\r\n");
										
										m = p.matcher(data);
										
										if(m.find()) {
											value = m.group(1);
										}
										
										data = req.postPm(url, betreff, text, userid, value);
										if(data.indexOf("Nachricht versendet...") > -1) {
											// Naricht erfolgreich gesendet...
											Toast.makeText(getApplicationContext(), "Naricht erfolgreich versendet...", Toast.LENGTH_SHORT).show();
											progress.cancel();
											Intent intent = new Intent(NewPM.this, PMs.class);
											startActivity(intent);
										}
										else {
											// Ein fehler ist aufgetreten !
											Toast.makeText(getApplicationContext(), "Unbekannter Fehler...", Toast.LENGTH_LONG).show();
											progress.cancel();		
										}
									}
									else {
										Toast.makeText(getApplicationContext(), "Der eingegebene Clanplanet Benutzername exisitiert nicht...", Toast.LENGTH_LONG).show();
										progress.cancel();
									}
								}
								else {
									// Nicht eingeloggt...
									Toast.makeText(getApplicationContext(), "Unbekannter Fehler... , du wurdest ausgeloggt !", Toast.LENGTH_LONG).show();
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
					}, 2 * 1000);
				}
				
				return true;			
				case R.id.readed_pms_show: 
					// Gelesene PM's activity anzeigen !
					intent = new Intent(this, ReadedPMs.class);
					startActivity(intent);
				return true;
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
				return true;
			default:
				
				return super.onOptionsItemSelected(item);
		}
	}
}
