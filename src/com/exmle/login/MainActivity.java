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

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

	EditText uname,pass;
	Button submit;
	String ip = "http://10.0.0.3:8080";
	String url="/MHS/login.php";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		uname = (EditText)findViewById(R.id.loginEmail);
		pass = (EditText)findViewById(R.id.password);
		submit = (Button)findViewById(R.id.btnLogin);
		
		
		
		submit.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) {
				
				Login login = new Login();
				String name = uname.getText().toString();
				String pwd = pass.getText().toString();
				uname.setText("");
				pass.setText("");
				if(name.equals("") || pwd.equals(""))
				{
					displayAlert();
				}
				else
				login.execute(ip,name,pwd);
			}
			
		});
	}
	
	public void displayAlert()
	{
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();

// Setting Dialog Title
alertDialog.setTitle("Warning!");

// Setting Dialog Message
alertDialog.setMessage("Enter Login Credentials");

// Setting Icon to Dialog
//alertDialog.setIcon(R.drawable.tick);

alertDialog.setCancelable(true);

// Setting OK Button
//alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
//
//	@Override
//	public void onClick(DialogInterface dialog, int which) {
//		// TODO Auto-generated method stub
//		
//	}
//});

// Showing Alert Message
alertDialog.show();
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.action_settings:
	    	findViewById(R.id.linear1).setVisibility(View.INVISIBLE);
	    	findViewById(R.id.linear2).setVisibility(View.VISIBLE);
	    	break;
	    	
	        default:
	            return super.onOptionsItemSelected(item);
	    }
		return super.onOptionsItemSelected(item);
	}
	
	
	public void ipReset(View v)
	{
		EditText et = (EditText)findViewById(R.id.ip);
		String str = et.getText().toString();
		if(!str.equals(""))
			ip = "http://"+str;
		findViewById(R.id.linear2).setVisibility(View.INVISIBLE);
		findViewById(R.id.linear1).setVisibility(View.VISIBLE);
	}
	

 class Login extends AsyncTask<String,Void,JSONObject>
	{
		ProgressDialog dialog;
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			dialog = new ProgressDialog(MainActivity.this);
			dialog.setCancelable(false);
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
				
				
				HttpPost httppost = new HttpPost(ip+url);
				List<NameValuePair> nameValue = new ArrayList<NameValuePair>();
				nameValue.add(new BasicNameValuePair("email",params[1]));
				nameValue.add(new BasicNameValuePair("password",params[2]));
				httppost.setEntity(new UrlEncodedFormEntity(nameValue));
				
				HttpResponse response;
				int executeCount = 0;
				do
				{
					dialog.setMessage("Logging in..");
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
				JSONArray arr = jObj.getJSONArray("Login_Data");
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
					startNextActivity(json.getString("org"));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			else
			{
				Toast.makeText(MainActivity.this,"Invalid Credentials or Server Address",Toast.LENGTH_LONG).show();
			}
		}
		
	}
 
	public void startNextActivity(String org)
	{
		
		Intent i = new Intent(this,EventList.class);
		i.putExtra("IP_ADDR",ip);
		i.putExtra("org",org);
	
		startActivity(i);
		finish();
	}
}
