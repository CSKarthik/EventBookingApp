package com.exmle.login;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


//The purpose of this activity is that it recievies 2 types of intents one from Activity2 and other from intent_filter
//Activity2 passes json string and on intent_filter it makes call to Async task.
//The key for json string of intent is "INFO


public class Activity3 extends Activity {
	public String ip,url2,event;
	TextView tv;
	JSONObject jobj = null;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity3);
		
		Bundle exts = getIntent().getExtras();
		String json =  exts.getString("INFO");
		boolean delete=exts.getBoolean("delete");
		
		Bundle extras = getIntent().getExtras();
		ip = extras.getString("IP_ADDR");
		url2=ip+"/MHS/arrlist.php";
		event = extras.getString("event");
		
		
		if(delete){
			Button b = (Button)findViewById(R.id.button1);
			b.setText("delete_entry");
			url2=ip+"/MHS/delete.php";
		}
		try {
			jobj = new JSONObject(json);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		try {
			tv= (TextView)findViewById(R.id.name3);
			tv.setText(jobj.getString("name"));
			
			tv= (TextView)findViewById(R.id.usn3);
			tv.setText(jobj.getString("usn"));
			
			tv= (TextView)findViewById(R.id.email3);
			tv.setText(jobj.getString("email"));
			
			
			tv= (TextView)findViewById(R.id.mob3);
			tv.setText(jobj.getString("mob"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	
	//DIRTY CODE FOR WORKOUT 
	
	public void submit(View V)
	{
		SubmitArray sa = new SubmitArray();
		String n = null,u=null,e=null,m=null;
		try{
		 n = jobj.getString("name");
		 u = jobj.getString("usn");
		 e = jobj.getString("email");
		 m = jobj.getString("mob");
		}
		catch(Exception e1){
			Toast.makeText(this, "Exp", Toast.LENGTH_SHORT).show();
		}
		sa.execute(url2,n,u,e,m,event);
	}
	
	class SubmitArray extends AsyncTask<String,Void,String>
	{

		ProgressDialog dialog;

		protected void onPreExecute()
		{
			super.onPreExecute();
			dialog = new ProgressDialog(Activity3.this);
			dialog.setCancelable(false);
			//dialog.setMessage("Fetching Records..");
			dialog.show();

		}

		@Override
		protected String doInBackground(String... params) {
			int responseCode=0;
			HttpClient client = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(params[0]);
						List<NameValuePair> nameValue = new ArrayList<NameValuePair>();
						nameValue.add(new BasicNameValuePair("table",params[5]));
						nameValue.add(new BasicNameValuePair("name",params[1]));
						nameValue.add(new BasicNameValuePair("usn",params[2]));
						nameValue.add(new BasicNameValuePair("email",params[3]));
						nameValue.add(new BasicNameValuePair("mob",params[4]));
						
						try {
							httppost.setEntity(new UrlEncodedFormEntity(nameValue));
						} catch (UnsupportedEncodingException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}


			HttpResponse response = null;
			int executeCount = 0;
			try{
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

				BufferedReader rd = null;

				rd = new BufferedReader(new InputStreamReader(
						response.getEntity().getContent(), "iso-8859-1"), 8);

				String line;
					line = rd.readLine();
					line = line.trim();
					return line;

				//dialog.setMessage("After Json Read");


			}
			catch(Exception e)
			{
				
			}
			return "IVAL";
		}

		@Override
		protected void onPostExecute(String msg)
		{
			dialog.dismiss();
				Toast.makeText(getBaseContext(),msg, Toast.LENGTH_LONG).show();
				finish();
//			else 
//				Toast.makeText(getBaseContext(), "Already Exists", Toast.LENGTH_LONG).show();
		}
	}

}
