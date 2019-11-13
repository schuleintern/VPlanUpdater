package com.spitschka.schuleintern.vplanupdater.untis;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Map;
import java.util.Queue;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthOption;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.MalformedChallengeException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthenticationStrategy;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.HttpContext;

public class UpdateThread extends Thread {

	private boolean isRunning = true;
	
	private File dirSchuelerHeute;
	private File dirSchuelerMorgen;
	private File dirLehrerHeute;
	private File dirLehrerMorgen;
	
	
	private String fileType;
	
	private FileFilter fileFilterSchuelerHeute;
	private FileFilter fileFilterSchuelerMorgen;
	private FileFilter fileFilterLehrerHeute;
	private FileFilter fileFilterLehrerMorgen;
	
	private String prefixSchuelerHeute;
	private String prefixSchuelerMorgen;
	private String prefixLehrerHeute;
	private String prefixLehrerMorgen;
	
	private long lastChangeSchuelerHeute = 0;
	private long lastChangeSchuelerMorgen = 0;
	private long lastChangeLehrerHeute = 0;
	private long lastChangeLehrerMorgen = 0;
	
	
	public UpdateThread() {
		this.dirLehrerHeute = new File(VPlanUpdater.getSettings().get("dirLehrerHeute").toString());
		this.dirLehrerMorgen = new File(VPlanUpdater.getSettings().get("dirLehrerMorgen").toString());
		this.dirSchuelerHeute = new File(VPlanUpdater.getSettings().get("dirSchuelerHeute").toString());
		this.dirSchuelerMorgen = new File(VPlanUpdater.getSettings().get("dirSchuelerMorgen").toString());
		this.prefixSchuelerHeute = VPlanUpdater.getSettings().get("schuelerHeutePrefix").toString();
		this.prefixSchuelerMorgen = VPlanUpdater.getSettings().get("schuelerMorgenPrefix").toString();
		this.prefixLehrerHeute = VPlanUpdater.getSettings().get("lehrerHeutePrefix").toString();
		this.prefixLehrerMorgen = VPlanUpdater.getSettings().get("lehrerMorgenPrefix").toString();
		
		this.fileType = VPlanUpdater.getSettings().getProperty("filetype");
		
		fileFilterSchuelerHeute = new FileFilter() {
			public boolean accept(File pathname) {
				if(!prefixSchuelerHeute.equals("")) {
					return pathname.getName().toLowerCase().startsWith(prefixSchuelerHeute) && pathname.getName().toLowerCase().endsWith("." + fileType);
				}else {
					return pathname.getName().toLowerCase().endsWith("." + fileType);
				}
			}
		};
		
		fileFilterSchuelerMorgen = new FileFilter() {
			public boolean accept(File pathname) {
				if(!prefixSchuelerHeute.equals("")) {
					return pathname.getName().toLowerCase().startsWith(prefixSchuelerMorgen) && pathname.getName().toLowerCase().endsWith("." + fileType);
				}else {
					return pathname.getName().toLowerCase().endsWith("." + fileType);
				}
			}
		};
		
		fileFilterLehrerMorgen = new FileFilter() {
			public boolean accept(File pathname) {
				if(!prefixSchuelerHeute.equals("")) {
					return pathname.getName().toLowerCase().startsWith(prefixLehrerHeute) && pathname.getName().toLowerCase().endsWith("." + fileType);
				}else {
					return pathname.getName().toLowerCase().endsWith("." + fileType);
				}
			}
		};
		
		fileFilterLehrerMorgen = new FileFilter() {
			public boolean accept(File pathname) {
				if(!prefixSchuelerHeute.equals("")) {
					return pathname.getName().toLowerCase().startsWith(prefixLehrerMorgen) && pathname.getName().toLowerCase().endsWith("." + fileType);
				}else {
					return pathname.getName().toLowerCase().endsWith("." + fileType);
				}
			}
		};
	}

	public void run() {
		while(this.isRunning) {
			try {
				boolean update = false;
				
				System.out.println("check for update");
				
				File[] filesLH = this.dirLehrerHeute.listFiles(fileFilterLehrerHeute);
				if(filesLH.length > 0 && this.lastChangeLehrerHeute != filesLH[0].lastModified()) {
					this.updateDir(filesLH, "lehrerheute");
					this.lastChangeLehrerHeute = filesLH[0].lastModified();
				}
				
				File[] filesLM = this.dirLehrerMorgen.listFiles(fileFilterLehrerMorgen);
				if(filesLM.length > 0 && this.lastChangeLehrerMorgen != filesLM[0].lastModified()) {
					this.updateDir(filesLM, "lehrermorgen");
					this.lastChangeLehrerMorgen = filesLM[0].lastModified();
				}
				
				File[] filesSH = this.dirSchuelerHeute.listFiles(fileFilterSchuelerHeute);
				if(filesSH.length > 0 && this.lastChangeSchuelerHeute != filesSH[0].lastModified()) {
					this.updateDir(filesSH, "schuelerheute");
					this.lastChangeSchuelerHeute = filesSH[0].lastModified();
				}
				
				File[] filesSM = this.dirSchuelerMorgen.listFiles(fileFilterSchuelerMorgen);
				if(filesSM.length > 0 && this.lastChangeSchuelerMorgen != filesSM[0].lastModified()) {
					this.updateDir(filesSM, "schuelermorgen");
					this.lastChangeSchuelerMorgen = filesSM[0].lastModified();
				}
				
				Thread.sleep(Long.parseLong(VPlanUpdater.getSettings().get("updateTimerInMilliSeconds").toString()));
				
			
				
			} catch (NumberFormatException e) {
				System.out.println("Angegebene Zeit updateTimerInMilliSeconds ungültig!");
				System.exit(0);
			} catch (InterruptedException e) {
			}
		}
	}
	
	private void updateDir(File[] files, String name) {
		System.out.println("Update " + name);
		// Post all Files to SchuleIntern
		String url = VPlanUpdater.getSettings().getProperty("urlToSchuleIntern");
		
		{
			HttpClientBuilder cb = HttpClientBuilder.create();
			
			if(VPlanUpdater.getSettings().getProperty("useProxy").equals("1")) {
				HttpHost proxy = new HttpHost(VPlanUpdater.getSettings().getProperty("proxyHost"), Integer.parseInt(VPlanUpdater.getSettings().getProperty("proxyPort")));
				
				cb.setProxy(proxy);
				CredentialsProvider credsProvider = new BasicCredentialsProvider();
				credsProvider.setCredentials(
						new AuthScope(proxy.getHostName(), proxy.getPort()),
						new UsernamePasswordCredentials(VPlanUpdater.getSettings().get("proxyUsername").toString(), VPlanUpdater.getSettings().get("proxyPassword").toString()));
				cb = cb.setDefaultCredentialsProvider(credsProvider)
						.setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy());
			}
			
			HttpClient client = cb.build();
		
			
			HttpPost post = new HttpPost(url);
			
			StringBody mode = new StringBody("startUpload", ContentType.MULTIPART_FORM_DATA);
			StringBody page = new StringBody("updatevplan", ContentType.MULTIPART_FORM_DATA);
			StringBody plan = new StringBody(name, ContentType.MULTIPART_FORM_DATA);
			StringBody key = new StringBody(VPlanUpdater.getSettings().getProperty("updateKey"), ContentType.MULTIPART_FORM_DATA);
			
			// 
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			builder.addPart("mode", mode);
			builder.addPart("key",key);
			builder.addPart("plan",plan);
			builder.addPart("page",page);
			
			HttpEntity entity = builder.build();
			//
			post.setEntity(entity);
			
			try {
				HttpResponse response = client.execute(post);
			} catch (ClientProtocolException e) {
				VPlanUpdater.showMessage("Update nicht möglich! " + e.getMessage());
			} catch (IOException e) {
				VPlanUpdater.showMessage("Update nicht möglich! " + e.getMessage());
				e.printStackTrace();
			}
		}
		

		for(int i = 0; i < files.length; i++) {
			System.out.println("Upload Seite " + i);

			HttpClient client = HttpClientBuilder.create().build();

			File file = files[i];
			HttpPost post = new HttpPost(url);
			FileBody fileBody = new FileBody(file, ContentType.TEXT_HTML);
			
			StringBody mode = new StringBody("uploadFile", ContentType.MULTIPART_FORM_DATA);
			StringBody pageNumber = new StringBody((i+1) + "", ContentType.MULTIPART_FORM_DATA);
			StringBody key = new StringBody(VPlanUpdater.getSettings().getProperty("updateKey"), ContentType.MULTIPART_FORM_DATA);
			StringBody plan = new StringBody(name, ContentType.MULTIPART_FORM_DATA);
			StringBody page = new StringBody("updatevplan", ContentType.MULTIPART_FORM_DATA);
			
			// 
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			builder.addPart("pageFile", fileBody);
			builder.addPart("mode", mode);
			builder.addPart("pagenr", pageNumber);
			builder.addPart("key",key);
			builder.addPart("plan",plan);
			builder.addPart("page",page);

			
			HttpEntity entity = builder.build();
			//
			post.setEntity(entity);
			try {
				HttpResponse response = client.execute(post);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				VPlanUpdater.showMessage("Update nicht möglich! " + e.getMessage());
			} catch (IOException e) {
				e.printStackTrace();
				VPlanUpdater.showMessage("Update nicht möglich! " + e.getMessage());
			}
		}
		
		{
			HttpClient client = HttpClientBuilder.create().build();

			HttpPost post = new HttpPost(url);
			
			StringBody mode = new StringBody("finishUpload", ContentType.MULTIPART_FORM_DATA);
			StringBody plan = new StringBody(name, ContentType.MULTIPART_FORM_DATA);
			StringBody key = new StringBody(VPlanUpdater.getSettings().getProperty("updateKey"), ContentType.MULTIPART_FORM_DATA);
			StringBody page = new StringBody("updatevplan", ContentType.MULTIPART_FORM_DATA);
			// 
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			builder.addPart("mode", mode);
			builder.addPart("key",key);
			builder.addPart("plan",plan);
			builder.addPart("page",page);
			
			HttpEntity entity = builder.build();
			//
			post.setEntity(entity);
			try {
				HttpResponse response = client.execute(post);
				VPlanUpdater.showMessage("Update erfolgreich!");
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				VPlanUpdater.showMessage("Update nicht möglich! " + e.getMessage());
			} catch (IOException e) {
				e.printStackTrace();
				VPlanUpdater.showMessage("Update nicht möglich! " + e.getMessage());
			}
		}
		
	}
}
