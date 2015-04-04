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
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class Participants extends Activity{
	String ip,url1,org,event;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.participants);
		
		Bundle extras = getIntent().getExtras();
		ip = extras.getString("IP_ADDR");
		org = extras.getString("org");
		event=extras.getString("event");
		url1="/MHS/retrive_entries.php";
		
		PList pl = new PList();
		pl.execute(org,event);

}

class PList extends AsyncTask<String,Void,JSONArray>
{
	public String s="NULL";
	
	ProgressDialog dialog;
	@Override
	protected void onPreExecute()
	{
		super.onPreExecute();
		dialog = new ProgressDialog(Participants.this);
		dialog.setCancelable(false);
		dialog.setMessage(" Retrieving Participents");
		dialog.show();
		
	}
	
	@Override
	protected JSONArray doInBackground(String... params) {
		int responseCode=0;
		JSONArray arr = null;
		try{
			
			HttpParams httpParameters = new BasicHttpParams();
			int timeoutConnection = 5000;
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			HttpClient client = new DefaultHttpClient();
			
			
			HttpPost httppost = new HttpPost(ip+url1);
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
			
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = rd.readLine()) != null)
			{
				sb.append(line + "\n");
			}
			
			String json = sb.toString();
			JSONObject jObj = new JSONObject(json);
			arr = jObj.getJSONArray("Part_Data");
		}
		catch(Exception e)
		{
			s="exception";
		}
		return arr;
	}
	
	
	//Json Login Format
	
	@Override
	protected void onPostExecute(JSONArray json)
	{
		dialog.dismiss();
		try {
			if(json!=null){
			 showTable(json);
			}
			else
				Toast.makeText(getApplicationContext(),"NO ENTRIES",Toast.LENGTH_LONG).show();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
}

public void showTable(JSONArray json) throws JSONException{
	int count = json.length();
	TableLayout tl = (TableLayout)findViewById(R.id.table);
	LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	for(int i=0;i<count;i++)
	{
		JSONObject jo = json.getJSONObject(i);
		TableRow tr = (TableRow) inflater.inflate(R.layout.record, null);
		
		TextView tv = (TextView)tr.getChildAt(0);
		tv.setText(jo.getString("name"));
		
		 tv = (TextView)tr.getChildAt(1);
		tv.setText(jo.getString("usn"));
		
		 tv = (TextView)tr.getChildAt(2);
		tv.setText(jo.getString("mob"));
		
		tv = (TextView)tr.getChildAt(3);
		tv.setText(jo.getString("email"));
		
		tl.addView(tr);
	}
}
}