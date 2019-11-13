package com.spitschka.schuleintern.vplanupdater.untis;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Properties;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class VPlanUpdater {

	private static Properties settings;
	
	private static SystemTray tray;
	
	private static TrayIcon trayIcon;
	
	public static void init() {
		settings = new Properties();
		BufferedInputStream stream;
		try {
			stream = new BufferedInputStream(new FileInputStream("settings.properties"));
			settings.load(stream);
			stream.close();
		} catch (IOException e) {
			System.out.println("Leider ist ein Fehler aufgetreten. Die Einstellungsdatei settings.properties kann nicht geladen werden!");
		}
		
		if(SystemTray.isSupported()) {
			tray = SystemTray.getSystemTray();
			
			
			Image image = Toolkit.getDefaultToolkit().getImage("images/upload.png");

			trayIcon = new TrayIcon(image, "SchuleIntern Vplan");
			
			trayIcon.setImageAutoSize(true);
			
			PopupMenu popup = new PopupMenu();
	        // create menu item for the default action
	        MenuItem beenden = new MenuItem("Beenden");
	        
	        beenden.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					System.exit(0);
				}
			});
	        
	        popup.add(beenden);
	        
	        trayIcon.setPopupMenu(popup);
			
			try {
				tray.add(trayIcon);
			} catch (AWTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static Properties getSettings() {
		return settings;
	}
	
	public static void showMessage(String message) {
		if(trayIcon != null) {
	          trayIcon.displayMessage("SchuleIntern VPlan Updater", message, TrayIcon.MessageType.INFO);
		}
	}

}
