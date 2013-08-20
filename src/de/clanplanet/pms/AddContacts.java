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
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AddContacts extends Activity {

	String username;
	String passwort;
	String regex;
	String data;
	
	Httprequests req;
	
	EditText name;
	RadioGroup teil_exact;
	Button search;
	TableLayout layout;
	
	SharedPreferences pref1;
	SharedPreferences pref2;
	
	ProgressDialog prog;
	
	Handler h;
	int selected_id;
	Matcher m;
	Pattern p;
	
	ArrayList<String> name_;
	ArrayList<String> userid;
	
	PendingIntent service;
	AlarmManager alarm;
	
	String search_id_value;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_contacts);
				
		name_ = new ArrayList<String>();
		userid = new ArrayList<String>();
		
		alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
		
		pref1 = getSharedPreferences("username_clanplanet_pms", MODE_PRIVATE);
		pref2 = getSharedPreferences("passwort_clanplanet_pms", MODE_PRIVATE);
		
		name = (EditText) findViewById(R.id.contact_name);
		teil_exact = (RadioGroup) findViewById(R.id.radio_group_search);
		search = (Button) findViewById(R.id.button_contacts);
		layout = (TableLayout) findViewById(R.id.scroll_table_layout);
		
		username = pref1.getString("username_clanplanet_pms", "");
		passwort = pref2.getString("passwort_clanplanet_pms", "");
		
		h = new Handler();
		
		prog = new ProgressDialog(this);
		prog.setMessage("Kontakt wird gesucht...");
		prog.setTitle("Clanplanet PM's App");
		prog.setCancelable(false);
		
		regex = "Eingeloggt als " + username;
		
		req = new Httprequests("http://www.clanplanet.de");
		
		try {
			data = req.postLoginClanplanet(username, passwort);
		
			if(data.indexOf(regex) > -1) {
				search.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						selected_id = teil_exact.getCheckedRadioButtonId();
						if(selected_id > -1) {
							if(name.getText().toString().isEmpty()) {
								AlertDialog.Builder dialog = new AlertDialog.Builder(AddContacts.this);
								dialog.setMessage("Du musst einen Namen eingeben !");
								dialog.setPositiveButton("Okay !", new OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
											dialog.cancel();								
									}
								});
							}
							else {
								
								if(selected_id == R.id.teilsuche) {
									search_id_value = "1";
								}
								else {
									search_id_value = "2";
								}
								
								ArrayList<NameValuePair> listederdaten = new ArrayList<NameValuePair>();
								listederdaten.add(new BasicNameValuePair("name", name.getText().toString()));
								listederdaten.add(new BasicNameValuePair("suchen", search_id_value));
								try {
									data = req.post(listederdaten, "http://www.clanplanet.de/personal/inbox_book.asp?rn=&action=search");
								
									regex = "<tr>\\s*<th nowrap>CP-Nr.</th>\\s*<th colspan=\"4\">Name</th>\\s*</tr>";
									p = Pattern.compile(regex);
									m = p.matcher(data);
									if(m.find()) {
										// Kontakte gefunden...
										regex = "<td class=\"lcell[ab]\" width=\"40\">(.*)&nbsp;&nbsp;</td>\\s*<td class=\"lcell[ab]\" nowrap width=\"100%\"><a href=\"(.*)\">(.*)</a></td>";
										p = Pattern.compile(regex);
										m = p.matcher(data);
										int j = 0;
										layout.removeAllViews();
										while(m.find()) {
											final TextView text = new TextView(getApplicationContext());
											userid.add(j, m.group(1));
											name_.add(j, m.group(3));
											text.setText(Html.fromHtml("Name: " + name_.get(j) + "<br>CP-Nr: " + userid.get(j) + "<br><br>"));
											text.setId(j);
											text.setTextSize(18);
											text.setTextColor(getResources().getColor(R.color.blue));
											text.setOnClickListener(new View.OnClickListener() {
												
												@Override
												public void onClick(View v) {
													addcontact(text.getId());												
												}
											});
											layout.addView(text);
											j++;
										}
									}
									else {
										// Keine Kontakte gefunden ...
										layout.removeAllViews();
										TextView text = new TextView(getApplicationContext());
										text.setText("Es wurden keine Benutzer gefunden");
										text.setTextColor(getResources().getColor(R.color.black));
										text.setTextSize(18);
										layout.addView(text);
									}
								} catch (ClientProtocolException e) {
									Toast.makeText(getApplicationContext(), "Keine Internetverbindung vorhanden !", Toast.LENGTH_LONG).show();
								} catch (IOException e) {
									Toast.makeText(getApplicationContext(), "Keine Internetverbindung vorhanden !", Toast.LENGTH_LONG).show();
								}
							}
						}
					}
				});
			}
		} catch (ClientProtocolException e1) {
			Toast.makeText(getApplicationContext(), "Keine Internetverbindung vorhanden !", Toast.LENGTH_LONG).show();
		} catch (IOException e1) {
			Toast.makeText(getApplicationContext(), "Keine Internetverbindung vorhanden !", Toast.LENGTH_LONG).show();
		}
	}
	
	public void addcontact(int ID) {
		final String userid_ = userid.get(ID);
		final String user    = name_.get(ID);
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setMessage(user + " wirklich zu den Kontakten hinzufügen ?");
		dialog.setPositiveButton("Ja", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				ArrayList<NameValuePair> listederdaten = new ArrayList<NameValuePair>();
				listederdaten.add(new BasicNameValuePair("id", userid_));
				listederdaten.add(new BasicNameValuePair("name", user));
				try {
					data = req.post(listederdaten, "http://www.clanplanet.de/personal/inbox_book.asp?rn=&action=addbook&id=" + userid_);
					regex = "<td class=\"lcell[ab]\">" + userid_ + "&nbsp;&nbsp;</td>";
				
					p = Pattern.compile(regex);
					m = p.matcher(data);
					if(m.find()) {
						// Alles OK
						Toast.makeText(getApplicationContext(), "Der Kontakt wurde erfolgreich hinzugefügt", Toast.LENGTH_LONG).show();
						Intent intent = new Intent(AddContacts.this, Kontakte.class);
						startActivity(intent);
					}
					else {
						Toast.makeText(getApplicationContext(), "Ein unbekannter Fehler ist aufgetreten !", Toast.LENGTH_LONG).show();
					}
				} catch (ClientProtocolException e) {
					Toast.makeText(getApplicationContext(), "Keine Internetverbindung vorhanden !", Toast.LENGTH_LONG).show();
				} catch (IOException e) {
					Toast.makeText(getApplicationContext(), "Keine Internetverbindung vorhanden !", Toast.LENGTH_LONG).show();
				}
			}
		});
		dialog.setNegativeButton("Nein", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		dialog.show();
	}
	
	Intent intent;
	
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
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu, menu);
		menu.getItem(0).setVisible(false);
		ActionBar bar = getActionBar();
		bar.setDisplayHomeAsUpEnabled(true);
		return true;
	}

}
