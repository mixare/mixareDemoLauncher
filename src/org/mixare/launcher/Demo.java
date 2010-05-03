/*
 * Copyright (C) 2010- Peer internet solutions
 * 
 * This program is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with 
 * this program. If not, see <http://www.gnu.org/licenses/>
 */

package org.mixare.launcher;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Demo extends Activity {

	//is set when mixare is installed
	boolean isMixareInstalled = false;
	
	//the test page that responds with the 4 cardinal points
	
	private static String mapServer = "http://www.mixare.org/geotest.php";
	
	private LocationManager lm;
	
	//These hold the current position of the user
	private Double lat;
	private Double lng;
	private Double alt;
	
	Location currentLocation;

	@Override
	public void onPause() {
		super.onPause();
		//We do not need updates while in pause
		getLocationManager().removeUpdates(locationListener);
	}

	@Override
	public void onResume() {

		super.onResume();

		//Opens the GPS settings so the user is able to enable it
		openGPSSettings();
		
		//Usual GPS initialization steps
		Criteria c = new Criteria();
		c.setAccuracy(Criteria.ACCURACY_FINE);
		String bestP = this.getLocationManager().getBestProvider(c, true);
		this.getLocationManager().requestLocationUpdates(bestP, 600, 5,	locationListener);
		Location lastP = this.getLocationManager().getLastKnownLocation(bestP);
		
		if(lastP != null) {
		lat = lastP.getLatitude();
		lng = lastP.getLongitude();
		alt = lastP.getAltitude();
		} 
		
		updateWithNewLocation(lastP);


		//Now we should have the position, let's proceed only if mixare is installed
		if (isMixareInstalled)
		{
			//show the main screen 
			setContentView(R.layout.home);
			
			
			//show the button that launch mixare
			final Button berge = (Button) findViewById(R.id.Button);
			berge.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					Intent i = new Intent();
					i.setAction(Intent.ACTION_VIEW);
					i.setDataAndType(Uri.parse(mapServer + "?"+ getGets(lat, lng, alt)), "application/mixare-json");
					startActivity(i); 
				}

				private String getGets(Double lat, Double lng, Double alt) {
					return "latitude=" + Double.toString(lat) + "&longitude=" + Double.toString(lng) + "&altitude=" + Double.toString(alt);
				}
				
			});
		       final TextView tv = (TextView) findViewById(R.id.urlText);
		       
		       tv.setText("This is the URL that will be called:\n" + mapServer + "?latitude=" + Double.toString(lat) + "&longitude=" + Double.toString(lng) + "&altitude=" + Double.toString(alt));

		} else {
		
			//Mixare is not installed, let's go to the market!
		try
		{
			Intent i = new Intent();
			i.setAction(Intent.ACTION_VIEW);
			i.setData(Uri.parse("market://search?q=pname:org.mixare"));
			startActivity(i); 
				
			finish();
        }
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		}

		
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//We try to locate mixare on the phone
		isMixareInstalled = false;
		try
		{
			PackageInfo pi = getPackageManager().getPackageInfo("org.mixare", 0);
			
			if (pi.versionCode >= 1)
			{
				isMixareInstalled = true;
			}
        }
		catch (PackageManager.NameNotFoundException  ex)
		{
			ex.printStackTrace();
		}


	}

	//usual Location services initialization
	private LocationManager getLocationManager() {
		if (this.lm == null)
			this.lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		return this.lm;
	}

	private final LocationListener locationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			updateWithNewLocation(location);
		}

		public void onProviderDisabled(String provider) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	};

	private void openGPSSettings()
	{
		//show GPS settings
		if( !getLocationManager().isProviderEnabled(android.location.LocationManager.GPS_PROVIDER ) )
		{
			Toast.makeText( this, "Please turn on GPS", Toast.LENGTH_LONG ).show();			
			//Output device name
			Log.d("device", Build.DEVICE);	
			//Output model name of device (in this case only the firs 3 letters, which are "HTC")
			Log.d("handy-model", Build.MODEL.substring(0, 3));
			
			//only for HTC HERO
			/*if(Build.DEVICE.equals("hero")){
				Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivity(myIntent);
			}*/
			//for HTC devices
			if(Build.MODEL.substring(0, 3).equals("HTC")){
				Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivity(myIntent);
			}
			//for other devices
			else{
				Intent myIntent = new Intent( Settings.ACTION_SECURITY_SETTINGS );
				startActivity(myIntent);
			}		
		}
	}
	
	private void updateWithNewLocation(Location location) {
		// Update your current location
		currentLocation = location;
		if (location != null) {
			lat = location.getLatitude();
			lng = location.getLongitude();
			alt = location.getAltitude();
		} else {
		}
	}
}