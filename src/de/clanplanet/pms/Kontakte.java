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
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Kontakte extends Activity {
	
	// Elemente erstellen
	TableLayout scroll_contacts;
	
	// prefences erstellen
	SharedPreferences pref1;
	SharedPreferences pref2;
	
	// Strings ...
	String username;
	String passwort;
	String data;
	String regex;
	
	// Suchermuster variablen
	Pattern p;
	Matcher m;
	int i;
	ArrayList <String> userid;
	ArrayList <String> name;
	ArrayList <String> notiz;
	
	// Httprequests erstellen
	Httprequests req;
	
	// ProgressDialog und Handler erstellen
	ProgressDialog progress;
	Handler h;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_kontakte);
		
		alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
		
		// Alle Variablen initialisieren die gebraucht werden... !
		scroll_contacts = (TableLayout) findViewById(R.id.scroll_contacts);
		pref1 = getSharedPreferences("username_clanplanet_pms", MODE_PRIVATE);
		pref2 = getSharedPreferences("passwort_clanplanet_pms", MODE_PRIVATE);
		req   = new Httprequests("http://www.clanplanet.de");
		progress = new ProgressDialog(this);
		h = new Handler();
		username = pref1.getString("username_clanplanet_pms", "");
		passwort = pref2.getString("passwort_clanplanet_pms", "");
		name = new ArrayList <String>();
		userid = new ArrayList <String>();
		notiz = new ArrayList <String>();
		
		// ProgressDialog bearbeiten und Zeigen...
		progress.setCancelable(false);
		progress.setTitle("Clanplanet PM's App");
		progress.setMessage("Die Kontakte werden geladen !");
		progress.show();
		
		h.postDelayed(new Runnable() {
			public void run() {
				try {
					// Login posten !
					data = req.postLoginClanplanet(username, passwort);
					
					// Regex muster setzen...
					regex = "Eingeloggt als <b>" + username + "</b>";
					
					if(data.indexOf(regex) > -1) {
						// Eingeloggt !
						data = req.refresh_page("http://www.clanplanet.de/personal/inbox_book.asp?rn=");
					
						// Neues Suchmuster erstellen...
						regex = "<tr>\\s*<th nowrap>Nr.</th>\\s*<th></th>\\s*<th>Name</th>\\s*<th width=\"100%\">Notiz</th>\\s*<th colspan=\"4\"></td>\\s*</tr>";
						
						// Suchmuster einleiten ...
						p = Pattern.compile(regex);
						m = p.matcher(data);
						if(m.find()) {
							// Kontakte gefunden...
							// Suchermuster erneuern und nochmals suchen ...
							regex = "<td class=\"lcell[ab]\">(.*)&nbsp;&nbsp;</td>\\s*<td class=\"lcell[ab]\">(.*)</td>\\s*<td class=\"lcell[ab]\" nowrap><a href=\"(.*)\">(.*)</a></td>\\s*<td class=\"lcell[ab]\">(.*)</td>";						
							p = Pattern.compile(regex);
							m = p.matcher(data);
							i = 0;
							while(m.find()) {
								userid.add(i, m.group(1));
								name.add(i, m.group(4));
								notiz.add(i, m.group(5));
								final TextView text = new TextView(getApplicationContext());
								text.setTextSize(18);
								text.setTextColor(getResources().getColor(R.color.blue));
								text.setText(Html.fromHtml("Kontakt Name: " + name.get(i) + "<br>CP-Nummer: " + userid.get(i) + "<br>Notiz: " + (notiz.get(i).isEmpty() ? "keine" : notiz.get(i)) + "<br><br>"));
								text.setId(i);
								text.setOnClickListener(new View.OnClickListener() {
									
									@Override
									public void onClick(View v) {
										// Beim Klick auf den TextView
										showOptions(text.getId());
									}
								});
								scroll_contacts.addView(text);
								i++;
							}
							progress.cancel();
						}
						else {
							// Keine Kontakte vorhanden...
							TextView text = new TextView(getApplicationContext());
							text.setText(Html.fromHtml("Du hast noch keine Kontakte.<br>Du kannst einen Kontakt über das Symbol in der oberen Leiste hinzufügen !"));
							text.setTextColor(getResources().getColor(R.color.black));
							text.setTextSize(18);
							scroll_contacts.addView(text);
						}
					}
					else {
						// Nicht eingeloggt... sollte nicht passieren !
						progress.cancel();
						Toast.makeText(getApplicationContext(), "Ein Unbekannter Fehler ist aufgetreten... du wurdest ausgeloggt...", Toast.LENGTH_LONG).show();
						finish();
					}
					
				} catch (ClientProtocolException e) {
					progress.cancel();
					Toast.makeText(getApplicationContext(), "Keine Internetverbindung gefunden !", Toast.LENGTH_LONG).show();
					e.printStackTrace();
				} catch (IOException e) {
					progress.cancel();
					Toast.makeText(getApplicationContext(), "Keine Internetverbindung gefunden !", Toast.LENGTH_LONG).show();
					e.printStackTrace();
				}
			}
		}, 2 * 1000);
	}
	
	AlertDialog dialog_;
	
	public void showOptions(final int ID) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle("Kontakt Optionen");
		Button button_new_pm = new Button(this);
		button_new_pm.setText("Neue PM an " + name.get(ID));
		button_new_pm.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Kontakte.this, NewPM.class);
				intent.putExtra("link", "");
				intent.putExtra("betreff", "");
				intent.putExtra("absender", name.get(ID));
				startActivity(intent);
			}
			
		});
		Button button_bearbeiten = new Button(this);
		button_bearbeiten.setText("Kontakt bearbeiten");
		button_bearbeiten.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Kontakte.this, EditContact.class);
				intent.putExtra("name", name.get(ID));
				intent.putExtra("notiz", notiz.get(ID));
				intent.putExtra("userid", userid.get(ID));
				startActivity(intent);
			}
		});
		Button button_loeschen = new Button(this);
		button_loeschen.setText("Kontakt löschen");
		button_loeschen.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AlertDialog.Builder dialogdelete = new AlertDialog.Builder(Kontakte.this);
				dialogdelete.setMessage(name.get(ID) + " wirklich löschen ?");
				dialogdelete.setPositiveButton("Ja", new OnClickListener() {
					
					@Override
					public void onClick(final DialogInterface dialog, int which) {
						dialog.cancel();
						h.postDelayed(new Runnable() {
							public void run() {
								dialog.cancel();
								ArrayList<NameValuePair>listederdaten = new ArrayList<NameValuePair>();
								listederdaten.add(new BasicNameValuePair("id", userid.get(ID)));
								listederdaten.add(new BasicNameValuePair("confirm", "true"));
								try {
									String data = req.post(listederdaten, "http://www.clanplanet.de/personal/inbox_book.asp?rn=&action=del");
									Matcher m;
									Pattern p = Pattern.compile("<td class=\"lcell[ab]\">" + userid.get(ID) + "&nbsp;&nbsp;</td>\\s*<td class=\"lcell[ab]\">(.*)</td>\\s*<td class=\"lcell[ab]\" nowrap><a href=\"(.*)\">" + name.get(ID) + "</a></td>\\s*<td class=\"lcell[ab]\">" + notiz.get(ID) + "</td>");
									m = p.matcher(data);
									if(m.find()) {
										Toast.makeText(getApplicationContext(), "Kontakt konnte nicht gelöscht werden ! Ein Fehler ist aufgetreten !", Toast.LENGTH_LONG).show();
									}
									else {
										Toast.makeText(getApplicationContext(), "Kontakt wurde erfolgreich gelöscht !", Toast.LENGTH_LONG).show();
										Intent intent = new Intent(Kontakte.this, Kontakte.class);
										startActivity(intent);
									}
								} catch (ClientProtocolException e) {
									Toast.makeText(getApplicationContext(), "Keine Internetverbindung vorhanden !", Toast.LENGTH_LONG).show();
								} catch (IOException e) {
									Toast.makeText(getApplicationContext(), "Keine Internetverbindung vorhanden !", Toast.LENGTH_LONG).show();
								}
							}
						}, 2 * 1000);
					}
					
				});
				dialogdelete.setNegativeButton("Nein", new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
				dialog_ = dialogdelete.create();
				dialog_.show();
			}
		});
		TableLayout layout = new TableLayout(this);
		layout.addView(button_new_pm);
		layout.addView(button_bearbeiten);
		layout.addView(button_loeschen);
		dialog.setView(layout);
		dialog.show();
	}
	
	Intent intent;
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
			case R.id.add_contact:
				intent = new Intent(this, AddContacts.class);
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
				case R.id.show_help:
					AlertDialog.Builder builder_ = new AlertDialog.Builder(this);
					builder_.setTitle("Ãœber Clanplanet PM's App");
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
			case R.id.new_pm_write:
				intent = new Intent(this, NewPM.class);
				intent.putExtra("absender", "");
				intent.putExtra("betreff", "");
				intent.putExtra("link", "");
				startActivity(intent);
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
		menu.getItem(1).setVisible(true);
		ActionBar bar = getActionBar();
		bar.setDisplayHomeAsUpEnabled(true);
		return true;
	}

}