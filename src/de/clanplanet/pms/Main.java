/*
 *
 * Clanplanet PM App
 * Copyright by mopsi.
 * Version 1.0 alpha
 * 
 * Open sourced auf github.com
 * 
 * 
 */

package de.clanplanet.pms;


/*
 * Importieren von noetigen Klassen ! und Packages....
 * 
 */

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/*
 * Einleiten der Hauptklasse (Main Klasse)
 * 
 */

public class Main extends Activity {

	// Error Textfeld (Error output)
	TextView error;
	
	// Button definition zum Klick abfang.
	Button button;
	
	// Username Textfeld.
	EditText user;
	
	// Passwort Textfeld.
	EditText pass;
	
	// Gespeicherter benutzername fuer die App
	SharedPreferences pref1; 
	
	// Gepspeichertes Passwort fuer die App
	SharedPreferences pref2;
	
	// Benutzername vom Textfeld
	String username;
	
	// Passwort vom Textfeld
	String passwort;
	
	// Httprequests Datentyp (eigene Klasse)
	Httprequests req;
	
	// Handler initialisieren fuer Zeitverzoegerungen
	Handler h;
	
	// String data fuer den Response der CP Seiten ...
	String data;
	
	// Intent i zum starten des neuen Intents.
	Intent i;
	
	// ProgressDialog zum Einloggen...
	ProgressDialog progress;
	
	// Suchmuster...
	String regex;
	
	/*
	 * 
	 * Initialisierung der onCreate Klasse.
	 * 
	 * Override rechte noetig !
	 */
	
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
		
		// Ansicht auf die Haupt XML Datei !
		setContentView(R.layout.activity_main);
		
		// Error Feld finden...
		error = (TextView) findViewById(R.id.error_view);
		
		// Button finden...
		button  = (Button) findViewById(R.id.login_button);
		
		// Username Textfeld finden...
		user  = (EditText) findViewById(R.id.username);
		
		// Passwort Feld finden...
		pass  = (EditText) findViewById(R.id.passwort);
		
		// gespeicherter Benutzername ...
		pref1 = getSharedPreferences("username_clanplanet_pms", MODE_PRIVATE);
		
		// gespeichertes Passwort... 
		pref2 = getSharedPreferences("passwort_clanplanet_pms", MODE_PRIVATE);
		
		// Httprequests wird initialisiert...
		req = new Httprequests("http://www.clanplanet.de");

		// Suchmuster...
		regex = "Eingeloggt als " + pref1.getString("username_clanplanet_pms", "");
		
		/*
		 * Abfragen ob schonmal eingeloggt...
		 * 
		 */
		if(pref1.getString("username_clanplanet_pms", "") != "" 
		   && pref2.getString("passwort_clanplanet_pms", "") != "") {
			try {
				data = req.postLoginClanplanet(pref1.getString("username_clanplanet_pms", ""), pref2.getString("passwort_clanplanet_pms", ""));				
				// Pruefen ob eingeloggt....
				if(data.indexOf(regex) > -1) {
					// Eingeloggt
					// Neuer Intent erzeugt.
					i = new Intent();
							
					// Klasse wird gesetzt...
					i.setClass(getApplicationContext(), PMs.class);
					
					// Neue Activity gestartet...
					startActivity(i);
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
			
			/*
			 * Bereits gespeichertes Passwort und den bereits
			 * gespeicherten Benutzernamen auf die
			 * Textfelder uebertragen !
			 * 
			 */
			user.setText(pref1.getString("username_clanplanet_pms", ""));
			pass.setText(pref2.getString("passwort_clanplanet_pms", ""));
			
			button.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					
					// Ausgabe...
					error.setText("Daten werden geprueft...");
					
					/*
					 * Benutzername nicht eingegeben
					 * 
					 */
					if(user.getText().toString().isEmpty()) {
						// Fehlermeldung ausgeben...
						error.setText("Du musst ein Usernamen eingeben !");
					}
					/*
					 * Passwort nicht eingegeben...
					 * 
					 */
					else if(pass.getText().toString().isEmpty()) {
						// Fehlermelung ausgeben.
						error.setText("Du musst ein Passwort eingeben !");
					}
					/*
					 * Beides eingegeben. Programm faerht fort.
					 * 
					 */
					else {	
						/*
						 * Daten werden geprueft ...
						 * 
						 */
						
						// Username wird gesetzt
						username = user.getText().toString();
						
						// Passwort wird gesetzt.
						passwort = pass.getText().toString();
						
						// Username wird gespeichert.
						pref1.edit().putString("username_clanplanet_pms", username).commit();
						
						// Passwort wird gespeichert.
						pref2.edit().putString("passwort_clanplanet_pms", passwort).commit();
					
						/* Versuche den Request...
						 * falls fehler tritt catch ein.
						 * 
						 */
						try {
							// Reponse speichern
							data = req.postLoginClanplanet(username, passwort);
							
							/*
							 * Pruefen ob Eingeloggt oder nicht.
							 * 
							 */
							if(data.indexOf(regex) > -1) {
								// Eingeloggt !	
								// Neuer Intent wird initialisiert...
								i = new Intent();
								
								// Klasse fuer den Intent setzen...
								i.setClass(getApplicationContext(), PMs.class);
								
								// Intent starten...
								startActivity(i);
								
								// ************************ //
								// Klasse endet hier... !!  //
								// ************************ //
							}
							else {
								// Nicht eingeloggt !
								// Fehlermeldung wird ausgegeben
								error.setText("Deine eingegebenen Login Daten sind nicht korrekt.");
							}
							
						} catch (ClientProtocolException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			});
		}
	}
}