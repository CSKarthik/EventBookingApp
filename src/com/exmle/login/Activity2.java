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
import org.json.JSONArray;
import  org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

@SuppressLint("NewApi")
public class Activity2 extends Activity {

	NfcAdapter mAdapter;
	PendingIntent pendingIntent;;
	IntentFilter ndef;
	IntentFilter[] intentFiltersArray;
	String[][] techListsArray;
	String ip,event;	//Passed Via Intent
	RelativeLayout rl1,rl2;
	String tag_str;
	String name,email,usn,mob;
	boolean delete=false;
	

	String url1;
	String search_url="/MHS/retrieve.php?method=retrieve&TAG=";
	String url2="/MHS/entry.php";


	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity2);

		rl1 = (RelativeLayout)findViewById(R.id.rl1);
		rl2 = (RelativeLayout)findViewById(R.id.rl2);

		rl2.setVisibility(View.GONE);
		rl1.setVisibility(View.VISIBLE);

		Bundle extras = getIntent().getExtras();
		ip = extras.getString("IP_ADDR");
		event = extras.getString("event");
		delete=extras.getBoolean("delete");

		mAdapter = NfcAdapter.getDefaultAdapter(this);

		pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

		ndef = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
		intentFiltersArray = new IntentFilter[] {ndef};

		techListsArray = new String[][] { new String[] {MifareClassic.class.getName()} };
	}

	public void onPause() {
		super.onPause();
		mAdapter.disableForegroundDispatch(this);
	}

	public void onResume() {
		super.onResume();
		mAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray);
		rl2.setVisibility(View.GONE);
		rl1.setVisibility(View.VISIBLE);
	}

	private long getDec(byte[] bytes) {
		long result = 0;
		long factor = 1;
		for (int i = 0; i < bytes.length; ++i) {
			long value = bytes[i] & 0xffl;
			result += value * factor;
			factor *= 256l;
		}
		return result;
	}

	public void onNewIntent(Intent intent) {
		Tag t = (Tag)intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		long tag_id = getDec(t.getId());
		tag_str = String.valueOf(tag_id);
		url1 = search_url+String.valueOf(tag_id);
		
//		Earlier Bug had
//		url1=url1+String.valueOf(tag_id) and hence the tag_id was appended for every event and made the url obselete.
		
		search();
	}

	private void search() {
		//Use the Below Async Task with the url to retrieve.php file.
		FindTag ft = new FindTag();
		ft.execute();

	}

	//This class is responsible for looking up the detials of the tag.
	class FindTag extends AsyncTask<String,Void,JSONObject>
	{

		ProgressDialog dialog;
		String ExpTest = "No";

		protected void onPreExecute()
		{
			super.onPreExecute();
			dialog = new ProgressDialog(Activity2.this);
			dialog.setCancelable(false);
			//dialog.setMessage("Fetching Records..");
			dialog.show();

		}

		@Override
		protected JSONObject doInBackground(String... params) {
			int responseCode=0;
			HttpClient client = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(ip+url1);
			//			List<NameValuePair> nameValue = new ArrayList<NameValuePair>();
			//			nameValue.add(new BasicNameValuePair("tag_id",params[0]));
			//			httppost.setEntity(new UrlEncodedFormEntity(nameValue));


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


				StringBuilder sb = new StringBuilder();
				String line;

				while ((line = rd.readLine()) != null)
				{
					sb.append(line + "\n");
				}


				//dialog.setMessage("After Json Read");
				String json = sb.toString();
				//if String is not NULL then it return json object.
				if(!json.equals("NULL")){
					JSONObject jObj = null;
					jObj = new JSONObject(json);
					//dialog.setMessage("After Json Create");
					JSONArray arr = null;
					arr = jObj.getJSONArray("Record_Data");
					return arr.getJSONObject(0);

					//dialog.setMessage("After Json Return");
				}


			}catch(Exception e)
			{
				ExpTest="Yes";
			}
			return null;
		}

		@Override
		protected void onPostExecute(JSONObject json)
		{

			//Toast.makeText(getBaseContext(),ExpTest,Toast.LENGTH_LONG).show();
			dialog.dismiss();
			if(json==null)
				changeLayout();
			else
				displayInfo(json);
		}
	}


	private void changeLayout() {
		rl1.setVisibility(View.GONE);
		rl2.setVisibility(View.VISIBLE);
	}

	public  void displayInfo(JSONObject json) {
		Intent 	i = new Intent(getBaseContext(),Activity3.class);
		i.putExtra("INFO",json.toString());
		i.putExtra("IP_ADDR",ip);
		i.putExtra("event",event);
		i.putExtra("delete", delete);
		startActivity(i);
		//finish();
	}

	//After This It reacts to different Relative Layout ie rl2

	public void btnSubmit(View v)
	{
		EditText et = (EditText)findViewById(R.id.email2);
		email = et.getText().toString();

		et = (EditText)findViewById(R.id.name2);
		name = et.getText().toString();

		et = (EditText)findViewById(R.id.usn2);
		usn = et.getText().toString();

		et = (EditText)findViewById(R.id.mob2);
		mob = et.getText().toString();

		if(email.equals("") || name.equals("") || usn.equals("") || mob.equals(""))
			displayAlert();
		else
		{
			//			Parameter Order: 1. tag_str, 2. name, 3. usn, 4. email, 5. mob;
			RecordEntry re = new RecordEntry();
			re.execute(tag_str,name,usn,email,mob);
			
		}

		et.setText("");
		et = (EditText)findViewById(R.id.email2);
		et.setText("");
		et = (EditText)findViewById(R.id.usn2);
		et.setText("");
		et = (EditText)findViewById(R.id.name2);
		et.setText("");
		
		rl1.setVisibility(View.VISIBLE);
		rl2.setVisibility(View.GONE);

	}

	public void displayAlert()
	{
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle("Alert");
		alertDialog.setMessage("All Fields are Mandatory!");
		alertDialog.setCancelable(true);
		alertDialog.show();
	}

	class RecordEntry extends AsyncTask<String,Void,JSONObject>
	{

		ProgressDialog dialog;
		String ExpTest = "No";

		protected void onPreExecute()
		{
			super.onPreExecute();
			dialog = new ProgressDialog(Activity2.this);
			dialog.setCancelable(false);
			//dialog.setMessage("Fetching Records..");
			dialog.show();

		}

		@Override
		protected JSONObject doInBackground(String... params) {
			int responseCode=0;
			HttpClient client = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(ip+url2);

			List<NameValuePair> nameValue = new ArrayList<NameValuePair>();
			nameValue.add(new BasicNameValuePair("tag_id",params[0]));
			nameValue.add(new BasicNameValuePair("name",params[1]));
			nameValue.add(new BasicNameValuePair("usn",params[2]));
			nameValue.add(new BasicNameValuePair("email",params[3]));
			nameValue.add(new BasicNameValuePair("mob",params[4]));

			try {
				httppost.setEntity(new UrlEncodedFormEntity(nameValue));
			} catch (UnsupportedEncodingException e1) {

				e1.printStackTrace();
			}


			HttpResponse response = null;
			int executeCount = 0;
			try{
				do
				{
					executeCount++;
					response = client.execute(httppost);
					responseCode = response.getStatusLine().getStatusCode();						
				} while (executeCount < 5 && responseCode == 408);

				BufferedReader rd = null;

				rd = new BufferedReader(new InputStreamReader(
						response.getEntity().getContent(), "iso-8859-1"), 8);


				StringBuilder sb = new StringBuilder();
				String line;

				while ((line = rd.readLine()) != null)
				{
					sb.append(line + "\n");
				}


				String json = sb.toString();
				if(!json.equals("NULL")){
					JSONObject jObj = null;
					jObj = new JSONObject(json);
					JSONArray arr = null;
					arr = jObj.getJSONArray("Entry_Result");
					return arr.getJSONObject(0);
				}


			}catch(Exception e)
			{
				ExpTest="Yes";
			}
			return null;
		}

		@Override
		protected void onPostExecute(JSONObject json)
		{

			//Toast.makeText(getBaseContext(),ExpTest,Toast.LENGTH_LONG).show();
			dialog.dismiss();
			if(json==null)
				Toast.makeText(getBaseContext(),"Entry Failed! Retry Later!",Toast.LENGTH_LONG).show();
			else
				Toast.makeText(getBaseContext(), "Entry Successful!", Toast.LENGTH_LONG).show();
		}
	}




	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
