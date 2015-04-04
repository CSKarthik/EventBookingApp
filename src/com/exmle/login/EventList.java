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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class EventList extends Activity{
	
	LinearLayout ll;
	FrameLayout fl;
	int color;
	String ip,org;
	String url1="/MHS/retrieve_events.php"; //retrieve list of events of org
	String url2 = "/MHS/createtable.php"; //register a new event under org.
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_list);
		
		Bundle extras = getIntent().getExtras();
		ip = extras.getString("IP_ADDR");
		org = extras.getString("org");	
		//Call AsyncTask
		EList el = new EList();
		el.execute(org);
		
	}
	
	
	public void setUI(JSONObject json) throws JSONException
	{
		
		ListView lv = (ListView)findViewById(R.id.linear1);
		
		int count = json.getInt("count");
		 List<String> list = new ArrayList<String>();
			for(int i=0;i<count;i++)
			{
				list.add(json.getString(String.valueOf(i)));
			}
			
						
		BinderData bindingData = new BinderData(this,list);
			lv.setAdapter(bindingData);
		
	}
	
	class EList extends AsyncTask<String,Void,JSONObject>
	{
		ProgressDialog dialog;
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			dialog = new ProgressDialog(EventList.this);
			dialog.setCancelable(false);
			dialog.setMessage("Processing your Events.");
			dialog.show();
			
		}
		
		@Override
		protected JSONObject doInBackground(String... params) {
			int responseCode=0;
			JSONObject finalJson = null;
			try{
				
				HttpParams httpParameters = new BasicHttpParams();
				int timeoutConnection = 5000;
				HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
				HttpClient client = new DefaultHttpClient();
				
				
				HttpPost httppost = new HttpPost(ip+url1);
				List<NameValuePair> nameValue = new ArrayList<NameValuePair>();
				nameValue.add(new BasicNameValuePair("org",params[0]));
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
				
				StringBuilder sb = new StringBuilder();
				String line;
				while ((line = rd.readLine()) != null)
				{
					sb.append(line + "\n");
				}
				
				String json = sb.toString();
				JSONObject jObj = new JSONObject(json);
				JSONArray arr = jObj.getJSONArray("Org_Data");
				finalJson = arr.getJSONObject(0);
			}
			catch(Exception e)
			{
				
			}
			return finalJson;
		}
		
		
		//Json Login Format
		
		@Override
		protected void onPostExecute(JSONObject json)
		{
			
			dialog.dismiss();
			String name=null;
			try {
				if(json!=null){
				 name = json.getString("success");
				name.trim();
				}
				else
					name="NULL";
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//Toast.makeText(MainActivity.this, name, Toast.LENGTH_SHORT).show();
			if(name.equals("1"))
			{
				try {
					setUI(json);
				} catch (JSONException e) {
					Toast.makeText(getApplicationContext(),"Json Exception.",Toast.LENGTH_LONG).show();
					e.printStackTrace();
				}
				
			}
			else
			{
				Toast.makeText(getApplicationContext(),"NO EVENTS",Toast.LENGTH_LONG).show();
			}
			
		}
		
	}
	
	public void Clicked(View v)
	{
		Intent i = new Intent(this,EventOptions.class);
		i.putExtra("org", org);
		i.putExtra("IP_ADDR", ip);
		TextView tv = (TextView)findViewById(v.getId()+100);
		i.putExtra("event",tv.getText());
		startActivity(i);
		finish();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.event, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.add_event:
	    	findViewById(R.id.linear1).setVisibility(View.GONE);
	    	findViewById(R.id.linear2).setVisibility(View.VISIBLE);
	    	break;
	    	
	    case R.id.show_events:
	    	findViewById(R.id.linear1).setVisibility(View.VISIBLE);
	    	findViewById(R.id.linear2).setVisibility(View.GONE);
	    	
	        default:
	            return super.onOptionsItemSelected(item);
	    }
		return super.onOptionsItemSelected(item);
	}
	
	public void createEvent(View v)
	{
		EditText et = (EditText) findViewById(R.id.ename);
		String ename = et.getText().toString();
		if(ename.equals(""))
			Toast.makeText(this, "Invalid Name",Toast.LENGTH_SHORT).show();
		else{
		findViewById(R.id.linear1).setVisibility(View.VISIBLE);
		findViewById(R.id.linear2).setVisibility(View.GONE);
		//Toast.makeText(this, ename, Toast.LENGTH_SHORT).show();
		createEventTask cet = new createEventTask();
		cet.execute(org,ename);
		}
		//Pass ip, org, ename,
	}
	
	class createEventTask extends AsyncTask<String,Void,String>
	{
		ProgressDialog dialog;
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			dialog = new ProgressDialog(EventList.this);
			dialog.setCancelable(false);
			dialog.setMessage("Creating a New Event");
			dialog.show();
			
		}
		
		@Override
		protected String doInBackground(String... params) {
			int responseCode=0;
			String line = "NULL";
			try{
				
				HttpParams httpParameters = new BasicHttpParams();
				int timeoutConnection = 5000;
				HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
				HttpClient client = new DefaultHttpClient();
				
				
				HttpPost httppost = new HttpPost(ip+url2);
				List<NameValuePair> nameValue = new ArrayList<NameValuePair>();
				nameValue.add(new BasicNameValuePair("org",params[0]));
				nameValue.add(new BasicNameValuePair("table",params[1]));
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
		protected void onPostExecute(String msg)
		{
			
			dialog.dismiss();
			if(msg.equals("Success"))
			{
				msg = "Event Successfully Created.";
			}
			else
			{
				msg="Creating Event Failed";
			}
			displayAlert(msg);
			
		}
		
	}
	

	public void displayAlert(String msg)
	{
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();

// Setting Dialog Title
alertDialog.setTitle("Warning!");

// Setting Dialog Message
alertDialog.setMessage(msg);

// Setting Icon to Dialog
//alertDialog.setIcon(R.drawable.tick);

alertDialog.setCancelable(true);

// Setting OK Button
//alertDialog.setButton2("OK", new DialogInterface.OnClickListener() {
//
//	@Override
//	public void onClick(DialogInterface dialog, int which) {
//		
//		
//	}
//});

// Showing Alert Message
alertDialog.show();

// Intent to the Same Activity to Refresh the UI

Intent i = new Intent(this,EventList.class);
i.putExtra("IP_ADDR",ip);
i.putExtra("org",org);
startActivity(i);
finish();
	
	}

}
