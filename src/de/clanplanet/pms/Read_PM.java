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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
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
		
		// Bekommener intent...
		intent = getIntent();
		
		alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
		
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
				
		betreff = betreff.replace("&#228;", "ä");
		betreff = betreff.replace("&#196;", "Ä");
		betreff = betreff.replace("&#246;", "ö");
		betreff = betreff.replace("&#214;", "Ö");
		betreff = betreff.replace("&#252;", "ü");
		betreff = betreff.replace("&#220;", "Ü");
		
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
								String html = m.group(2);
								String reg = "<script language=\"javascript\">buildemail\\('(.*)','(.*)'\\)</script>";
								ArrayList<String> name = new ArrayList<String>();
								Pattern p1 = Pattern.compile(reg);
								Matcher m1 = p1.matcher(html);
								int index = 0;
								while(m1.find()) {
									name.add(index, m1.group(1) + "@" + m1.group(2));
									html = html.replaceFirst(reg, name.get(index));
									index++;
								}
								html = replace_html_smileys(html);			
								
								html = html.replace("&#228;", "ä");
								html = html.replace("&#196;", "Ä");
								html = html.replace("&#246;", "ö");
								html = html.replace("&#214;", "Ö");
								html = html.replace("&#252;", "ü");
								html = html.replace("&#220;", "Ü");
								TextView text = new TextView(Read_PM.this);
								text.setText(Html.fromHtml(html));
								text.setLinksClickable(true);
								text.setMovementMethod(LinkMovementMethod.getInstance());
								text.setTextColor(getResources().getColor(R.color.black));
								text.setTextSize(18);
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
					
				}, 1 * 1000);
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
	
	public String replace_html_smileys(String input) {
		
		String baseurl = "http://gfx.clanplanet.de/emoticons/";
		String output = input;
		// String image = "smile.gif";
		ArrayList <String> smilelist = new ArrayList <String>();
		smilelist.add(0, "smile.gif");
		smilelist.add(1, "twinkle.gif");
		smilelist.add(2, "biglaugh.gif");
		smilelist.add(3, "schepp.gif");
		smilelist.add(4, "rofl.gif");
		smilelist.add(5, "wink.gif");
		smilelist.add(6, "knutsch.gif");
		smilelist.add(7, "happy.gif");
		smilelist.add(8, "punch.gif");
		smilelist.add(9, "evil.gif");
		smilelist.add(10, "kuss.gif");
		smilelist.add(11, "schnecke.gif");
		smilelist.add(12, "sad.gif");
		smilelist.add(13, "tongue.gif");
		smilelist.add(14, "grins.gif");
		smilelist.add(15, "oop.gif");
		smilelist.add(16, "grr.gif");
		smilelist.add(17, "flop.gif");
		smilelist.add(18, "cool.gif");
		smilelist.add(19, "knuddel.gif");
		smilelist.add(20, "snief.gif");
		smilelist.add(21, "drink.gif");
		smilelist.add(19, "confused.gif");
		smilelist.add(20, "herz.gif");
		smilelist.add(21, "kaffee.gif");
		
		ArrayList <String> replacement = new ArrayList<String>();
		replacement.add(0, ":)");
		replacement.add(1, ";)");
		replacement.add(2, ":D");
		replacement.add(3, ":/");
		replacement.add(4, "*rofl*");
		replacement.add(5, "*wink*");
		replacement.add(6, "*knutsch*");
		replacement.add(7, "*happy*");
		replacement.add(8, "*punch*");
		replacement.add(9, "*evil");
		replacement.add(10, "*kuss*");
		replacement.add(11, "*schnecke*");
		replacement.add(12, ":(");
		replacement.add(13, ":P");
		replacement.add(14, "*g*");
		replacement.add(15, ":o");
		replacement.add(16, "*grr*");
		replacement.add(17, "*flop*");
		replacement.add(18, "*cool*");
		replacement.add(19, "*knuddel*");
		replacement.add(20, "*snief*");
		replacement.add(21, "*drink*");
		replacement.add(22, "*confused*");
		replacement.add(23, "*herz*");
		replacement.add(24, "*kaffee*");
		
		for(int i = 0; i < smilelist.size(); i++) {
			output = output.replace("<img src=\"" + baseurl + smilelist.get(i) + "\" align=\"absmiddle\">", replacement.get(i));
		}		
		return output;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu, menu);
		
		if(getIntent().getExtras().getString("absender").indexOf("Clanplanet") > -1) {
			menu.getItem(0).setVisible(false);
		}
		
		actionBar = getActionBar();
		
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		return true;
	}
	
	PendingIntent service;
	AlarmManager alarm;
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch(item.getItemId()) {
			case android.R.id.home : 
				onBackPressed();
				return true;			
			case R.id.readed_pms_show: 
				// Gelesene PM's activity anzeigen !
				intent = new Intent(this, ReadedPMs.class);
				startActivity(intent);
				return true;
			case R.id.read_sended_pms:
				intent = new Intent(this, GesendeteNarichten.class);
				startActivity(intent);
				return true;
			case R.id.contacts_menu:
				intent = new Intent(this, Kontakte.class);
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
			case R.id.reply:
				Intent intent = new Intent(this, Reply.class);
				intent.putExtra("betreff", betreff);
				intent.putExtra("absender", absender);
				intent.putExtra("link", link);
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

}
