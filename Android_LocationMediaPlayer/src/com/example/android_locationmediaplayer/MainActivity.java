package com.example.android_locationmediaplayer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements LocationListener {
	
	private Button playButton;
	private TextView coordinatesTextView;
	private TextView songTitleTextView;
	private LocationManager locationManager = null;
	private Location location = null;
	private boolean isNetworkEnabled = false;
	private boolean isGPSEnabled = false;
	private MediaPlayer mediaPlayer = null;
	// The minimum distance to change Updates in meters
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
	// The minimum time between updates in milliseconds
	private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute
	private HashMap<Integer, Integer> songsMap = new HashMap<>();
	private int songId = 0;
	
	private void initSongsList() {
		songsMap.put(0, R.raw.i_giardini_di_marzo);
		songsMap.put(1, R.raw.innocenti_evasioni);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		playButton = (Button) findViewById(R.id.playButton);
		coordinatesTextView = (TextView) findViewById(R.id.coordinatesTextView);
		songTitleTextView = (TextView) findViewById(R.id.songTitleTextView);
		
		initSongsList();
		
		// Acquire a reference to the system Location Manager
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

		// Getting GPS status
		isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

		// Getting network status
		isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		
		if (!isGPSEnabled && !isNetworkEnabled) 
			Toast.makeText(getApplicationContext(), "Enable Network/GPS provider", Toast.LENGTH_SHORT).show();
		else {
			if (isGPSEnabled)
				getGPSLocation();
			else if (isNetworkEnabled)
				getNetworkLocation();
		}
		if(location != null)
			coordinatesTextView.setText("Latitude:" + location.getLatitude() + ", Longitude:" + location.getLongitude());
		
		mediaPlayer = new MediaPlayer();
		playSong();
		
		playButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// Check for already playing
				if(mediaPlayer.isPlaying()) {
					if(mediaPlayer != null)
						mediaPlayer.pause();
				}
				else {
					// Resume song
					if(mediaPlayer != null)
						mediaPlayer.start();
				}
			}
		});
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mediaPlayer.release();
	}
	
	private void playSong() {
		mediaPlayer = MediaPlayer.create(getApplicationContext(), songsMap.get(songId));	
		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				mp.reset();			
				playSong();
			}
		});
		try {
			mediaPlayer.prepare();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mediaPlayer.start();
		songTitleTextView.setText(songsMap.get(songId));
	}
	
	private void getNetworkLocation() {
		//Toast.makeText(getApplicationContext(), "Network provider enabled", Toast.LENGTH_SHORT).show();
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, 
				MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
		if(locationManager != null)
			location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	}

	private void getGPSLocation() {
		//Toast.makeText(getApplicationContext(), "GPS provider enabled", Toast.LENGTH_SHORT).show();
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, 
				MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
		if(locationManager != null)
			location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);		
	} 

	@Override
	public void onLocationChanged(Location location) {
		mediaPlayer.reset();
		this.location= location;
    	Toast.makeText(getApplicationContext(), "Location is changed - change song", Toast.LENGTH_LONG).show();	
		coordinatesTextView.setText("Latitude:" + this.location.getLatitude() + ", Longitude:" + this.location.getLongitude());
		if(songId == 0) 
			songId = 1;
		else
			songId = 0;
		playSong();
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		if(provider.equals(LocationManager.GPS_PROVIDER) && !isNetworkEnabled) {
			Toast.makeText(getApplicationContext(), "GPS provider enabled", Toast.LENGTH_SHORT).show();
			this.isGPSEnabled = true;
			getGPSLocation();
		}
		else if(provider.equals(LocationManager.NETWORK_PROVIDER) && !isGPSEnabled) {
			Toast.makeText(getApplicationContext(), "Network provider enabled", Toast.LENGTH_SHORT).show();
			this.isNetworkEnabled = true;
			getNetworkLocation();
		}			
	}

	@Override
	public void onProviderDisabled(String provider) {
		if(provider.equals(LocationManager.GPS_PROVIDER)) {
			Toast.makeText(getApplicationContext(), "GPS provider disabled", Toast.LENGTH_SHORT).show();
			if (isNetworkEnabled) {
				getNetworkLocation();
			}
		}
		else if(provider.equals(LocationManager.NETWORK_PROVIDER)) {
			Toast.makeText(getApplicationContext(), "Network provider disabled", Toast.LENGTH_SHORT).show();
			if (isGPSEnabled) {
				getGPSLocation();
			}
		}	
	}
}
