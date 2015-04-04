package com.exmle.login;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class EventOptions extends Activity {
	
	String ip,event,org;
	String url1;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_options);
		
		Bundle extras = getIntent().getExtras();
		ip = extras.getString("IP_ADDR");
		event = extras.getString("event");
		org = extras.getString("org");
		//Toast.makeText(this,event,Toast.LENGTH_LONG).show();
		


	}

	
	public void register(View v){
		Intent i = new Intent(this,Activity2.class);
		i.putExtra("IP_ADDR", ip);
		i.putExtra("event",org+"_"+event);
		startActivity(i);
		
	}
	public void show_records(View v){
		Intent i = new Intent(this,Participants.class);
		i.putExtra("IP_ADDR", ip);
		i.putExtra("org", org);
		i.putExtra("event",event);
		startActivity(i);
		//finish();
		
	}
	public void delete_entry(View v){
		
		Intent i = new Intent(this,Activity2.class);
		i.putExtra("IP_ADDR", ip);
		i.putExtra("event",org+"_"+event);
		i.putExtra("delete", true);
		startActivity(i);
		
	}
	public void export_as_csv(View v){
		goToUrl(ip+"/MHS/CSV.php?table="+org+"_"+event);
		
	}
	public void delete_event(View v){
		url1="/MHS/deletetable.php";
		Del_Eve de = new Del_Eve();
		de.execute(url1,org,event);
		
		
	}
	
	
	
	private void goToUrl (String url) {
		Intent myWebLink = new Intent(android.content.Intent.ACTION_VIEW);
        myWebLink.setData(Uri.parse(url));
            startActivity(myWebLink);
    }

	class Del_Eve extends AsyncTask<String,Void,String>
	{
		ProgressDialog dialog;
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			dialog = new ProgressDialog(EventOptions.this);
			dialog.setCancelable(false);
			dialog.setMessage("Processing your Events.");
			dialog.show();
			
		}
	
	@Override
	protected String doInBackground(String... params) {
		int responseCode=0;
		String line="NULL";
		try{
			
			HttpParams httpParameters = new BasicHttpParams();
			int timeoutConnection = 5000;
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			HttpClient client = new DefaultHttpClient();
			
			
			HttpPost httppost = new HttpPost(ip+params[0]);
			List<NameValuePair> nameValue = new ArrayList<NameValuePair>();
			nameValue.add(new BasicNameValuePair("org",params[1]));
			nameValue.add(new BasicNameValuePair("table",params[2]));
			httppost.setEntity(new UrlEncodedFormEntity(nameValue));
			
			HttpResponse response;
			int executeCount = 0;
			do
			{
				// Execute HTTP Post Request
				executeCount++;
				response = client.execute(httppost);
				responseCode = response.getStatusLine().getStatusCode();						
				// If you want to see the response code, you can Log it
				// out here by calling:
				// Log.d("256 Design", "statusCode: " + responseCode)
			} while (executeCount < 5 && responseCode == 408);
			
			//dialog.setMessage("Out Of While");
			
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent(), "iso-8859-1"), 8);
			
			
			
			line = rd.readLine();
		}
		catch(Exception e)
		{
			
		}
		return line;
	}
	
	
	//Json Login Format
	
	@Override
	protected void onPostExecute(String line)
	{
		
		if(!line.equals("NULL")){
			//Toast.makeText(getApplicationContext(),line,Toast.LENGTH_LONG).show();
			
		}
		else
		{
			Toast.makeText(getApplicationContext(),"Delete Failed!",Toast.LENGTH_LONG).show();
		}
		goBack();
	}

}
	
	public void goBack(){
		Intent i = new Intent(this,EventList.class);
		i.putExtra("IP_ADDR", ip);
		i.putExtra("org",org);
		startActivity(i);
		finish();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.back, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.action_settings:
	    	
	    	Intent i = new Intent(this,EventList.class);
	    	i.putExtra("IP_ADDR",ip);
	    	i.putExtra("org",org);
	    	startActivity(i);
	    	finish();
	    	break;
	    	
	        default:
	            return super.onOptionsItemSelected(item);
	    }
		return super.onOptionsItemSelected(item);
	}
	
//	public void startIntent()
//	{
//		Intent i = new Intent(EventOptions.this,EventList.class);
//		i.putExtra("IP_ADDR",ip);
//		i.putExtra("org",org);
//		startActivity(i);
//		finish();
//	}

}
