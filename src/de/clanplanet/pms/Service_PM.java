/*
 * 
 * Service fuer die PM App 
 * Der Dienst ist dazu da um im Hintergrund 
 * das Programm auszufuehren und den User 
 * zu benarichtigen wenn der User eine PM erhalten hat.
 * 
 */

package de.clanplanet.pms;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.ClientProtocolException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;


public class Service_PM extends Service {

	// Http Request initialisieren
	Httprequests req;
	
	// Gespeicherter Username
	SharedPreferences pref1;
	
	// Gespeichertes Passwort
	SharedPreferences pref2;
	
	// Gespeicherte Anzahl an PM's
	SharedPreferences pref3;
	
	// Prueft ob Noti bei fehlender Interverbindung gesetzt oder nicht...
	SharedPreferences pref4;
	
	// Pattern
	Pattern p;
	
	//Matcher 
	Matcher matcher;
	
	// Zeichenkette fuer den Response von den Server Requests...
	String data;
	
	// Handler
	Handler h;
	
	// Anzahl der PM's
	int anzahl_pms = 0;
	 
	// Username
	String username;
	
	// Passwort
	String passwort;
	
	// MediaPlayer fuer den Sound der Notification...
	MediaPlayer mp = new MediaPlayer();
	
	// Notification Manager nMgr initialisieren
	NotificationManager nMgr;
	
	int NOTIFICATION_ID = 100;
	
	@Override 
	public void onCreate() {
		super.onCreate();
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build();	
		StrictMode.setThreadPolicy(policy);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		super.onStartCommand(intent, flags, startId);
		
		nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
		// Handler initialisieren mit Klasse Handler...
		h = new Handler();
		
		// Request initialisieren...
		req = new Httprequests("http://www.clanplanet.de");
		
		// Suchmuster erstellen...
		p = Pattern.compile("<tr>\\s*<th>\\s*Absender\\s*</th>\\s*<th>Betreff</th>\\s*<th>Datum</th>\\s*</tr>");
		
		// Pref1 initialisieren
		pref1 = getSharedPreferences("username_clanplanet_pms", MODE_PRIVATE);
		
		// Pref2 initialisieren
		pref2 = getSharedPreferences("passwort_clanplanet_pms", MODE_PRIVATE);
		
		// Pref3 initialisieren
		pref3 = getSharedPreferences("anzahl_der_cp_pms", MODE_PRIVATE);
		
		// Pref4 initialisieren
		pref4 = getSharedPreferences("is_internet_noti_on", MODE_PRIVATE);
		
		// Den gespeicherten Benutzernamen in die Variable username speichern...
		username = pref1.getString("username_clanplanet_pms", "");
		
		// Das gespeicherte Passwort in die Variable passwort speichern 
		passwort = pref2.getString("passwort_clanplanet_pms", "");
		// Versuche den Request durchzufuehren...
		try {
			
			// Login Posten und in Data Variable speichern...
			data = req.postLoginClanplanet(username, passwort);
			
			/*
			 * Pruefen ob eingeloggt oder nicht...
			 * 
			 */
			if(data.indexOf("Eingeloggt als " + username) > -1) {
				// Eingeloggt...
				// Wird auf neue PM geprueft...
				h.postDelayed(new Runnable() {
					public void run() {
							// Pref4 auf false setzen...
							pref4.edit().putBoolean("is_internet_noti_on", false);
							
							// Versuche Request, sonst Fehlermeldung im Logcat
							try {
								// Rueckgabe wird in data variable gespeichert...
								data = req.refresh_page("http://www.clanplanet.de/personal/inbox.asp");
								
								matcher = p.matcher(data);
								
								// Pruefen ob neue PM gefunden...
								if(matcher.find()) {
									anzahl_pms = 0;
									// Neue PM gefunden...
									
									// Neues suchmuster fuer die PM's
									Pattern p1 = Pattern.compile("<tr>\\s*<td class=\"lcell[ab]\" nowrap><span class=\"small\"><span >(.*)</span></span></td>\\s*<td class=\"lcell[ab]\" width=\"100%\"><span class=\"small\">\\s*(?:<span class=\"(?:mark|unalert|alert)\">[!#]</span>\\s*)?<a href=\"(.*)\"><span >(.*)</span></a>\\s*</span></td>\\s*<td class=\"lcell[ab]\" width=\"120\" nowrap><span class=\"small\"><span >(.*)</span></span></td>\\s*</tr>");
									
									// Suchmuster matchen mit data
									Matcher m1 = p1.matcher(data);
									
									// Solange er PM's findet immer Pro PM die variable eins hochzaehlen
									while(m1.find()) {
										anzahl_pms++;
									}
									
									if(pref3.getInt("anzahl_der_cp_pms", 0) < anzahl_pms) {
										// Noti noch nicht gesetzt....
										Intent intent = new Intent(getApplicationContext(), PMs.class);
										PendingIntent pintent = PendingIntent.getActivity(getApplicationContext(), NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

										if(anzahl_pms == 1) {
											createNot(pintent, "Du hast 1 neue Clanplanet PM erhalten !", nMgr, NOTIFICATION_ID);
										}
										else {
											createNot(pintent, "Du hast mehrere neue Clanplanet PMs erhalten !", nMgr, NOTIFICATION_ID);
										}
										// Da Notification gesetzt wird boolean jetzt true...
										pref3.edit().putInt("anzahl_der_cp_pms", anzahl_pms).commit();
										h.postDelayed(this, 60 * 1000);
									}
									else {
										pref3.edit().putInt("anzahl_der_cp_pms", anzahl_pms).commit();
										h.postDelayed(this, 60 * 1000);
									}
								}
								else {
									// Keine neue PM gefunden...
									nMgr.cancel(NOTIFICATION_ID);
									pref3.edit().putInt("anzahl_der_cp_pms", 0).commit();
									h.postDelayed(this, 60 * 1000);
								}
								
							} catch (ClientProtocolException e) {							
								pref3.edit().putInt("anzahl_der_cp_pms", 0).commit();
								
								h.postDelayed(this, 60 * 1000);
							} catch (IOException e) {							
								pref3.edit().putInt("anzahl_der_cp_pms", 0).commit();
								
								h.postDelayed(this, 2 * 60 * 1000);
							}
					}
				}, 3000);
			}
			else {
				// Nicht eingeloggt...
				// Sollte nicht passieren...
				h.postDelayed(new Runnable() {
					public void run() {
						
						/*
						 * Dienst wird beendet...
						 * 
						 */
						stopSelf();
						
					}
				}, 500);
			}
		} catch (ClientProtocolException e) {	
			// Internetverbindung nicht mehr Moeglich
			// deshalb Noti ausgeben und in 60 sekunden wieder versuchen !...
			
			Intent intent1 = new Intent(getApplicationContext(), Main.class);
			PendingIntent pintent = PendingIntent.getActivity(getApplicationContext(), NOTIFICATION_ID, intent1, PendingIntent.FLAG_UPDATE_CURRENT);

			if(pref4.getBoolean("is_internet_noti_on", false) == false) {
				createNot(pintent, "Deine Internetverbindung ist abgebrochen...", nMgr, NOTIFICATION_ID);
				pref4.edit().putBoolean("is_internet_noti_on", true);
			}
			pref3.edit().putInt("anzahl_der_cp_pms", 0).commit();
		} catch (IOException e) {		
			// Internetverbindung nicht mehr Moeglich
			// deshalb Noti ausgeben und in 60 sekunden wieder versuchen !...
			
			Intent intent1 = new Intent(getApplicationContext(), Main.class);
			PendingIntent pintent = PendingIntent.getActivity(getApplicationContext(), NOTIFICATION_ID, intent1, PendingIntent.FLAG_UPDATE_CURRENT);

			if(pref4.getBoolean("is_internet_noti_on", false) == false) {
				createNot(pintent, "Deine Internetverbindung ist abgebrochen...", nMgr, NOTIFICATION_ID);
				pref4.edit().putBoolean("is_internet_noti_on", true);
			}
			pref3.edit().putInt("anzahl_der_cp_pms", 0).commit();
		}
		
		return START_STICKY;
	}
	
	public void createNot(PendingIntent pIntent, String text, NotificationManager nMgr, int NOTIFICATION_ID) {
		Notification noti = new Notification.Builder(this)
							.setContentIntent(pIntent)
							.setContentText(text)
							.setContentTitle("Clanplanet PM App")
							.setSmallIcon(R.drawable.ic_launcher)
							.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
							.build();
		
		nMgr.notify(NOTIFICATION_ID, noti);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}