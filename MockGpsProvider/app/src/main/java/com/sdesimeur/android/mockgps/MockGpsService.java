package com.sdesimeur.android.mockgps;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.URL;
import java.net.URLConnection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MockGpsService extends Service implements LocationListener {
	private LocationManager locationManager;
	private Location newLocation = null;
	//private static final int LONGTIMEOUT4HTMLREQUEST = 10000;
	private static final int SHORTTIMEOUT4HTMLREQUEST = 2000;
	//private static final int WAIT = 900;
	private String pattern = null;
	private Pattern compiledPattern = null;
	private double lat=0;
	private double lon=0;
	//private int nbretrywithcancel;
	private boolean mustBeAborted;
	//private boolean mustWait = true;
	//private static final String MOCKGPSTHREAD = "MockGpsThread";
	private String serverString = MockGpsProviderActivity.SERVERS.get(0);
	//private MockGpsThread mockGpsThread = null;
	private final IBinder mBinder = new LocalBinder();
	//private boolean isInterrupted = false;
	private Timer timer = null;

	public class LocalBinder extends Binder {
		MockGpsService getService() {
			return MockGpsService.this;
		}
	}

	public MockGpsService() {
		this.pattern="lat\\s*\\=\\s*(\\-?\\d*\\.?\\d*)\\s*,\\s*lon\\s*\\=\\s*(\\-?\\d*\\.?\\d*)";
		this.compiledPattern = Pattern.compile(this.pattern);
		if (android.os.Build.VERSION.SDK_INT > 9) {
		    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    		StrictMode.setThreadPolicy(policy);
		}
	}

	private void startProvider() {
		if (timer != null) stopProvider();
		locationManager.addTestProvider(
				LocationManager.GPS_PROVIDER,
				"requiresNetwork" == "",
				"requiresSatellite" == "",
				"requiresCell" == "",
				"hasMonetaryCost" == "",
				"supportsAltitude" == "",
				"supportsSpeed" == "",
				"supportsBearing" == "",
				Criteria.POWER_HIGH,
				Criteria.ACCURACY_FINE
		);
		locationManager.setTestProviderStatus(LocationManager.GPS_PROVIDER, LocationProvider.AVAILABLE, null, System.currentTimeMillis());
		locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
			mustBeAborted = false;
			TimerTask task = new TimerTask() {
				@Override
				public void run() {
					String readString = "";
					URL url = null;
					InputStreamReader isr = null;
					BufferedReader in = null;
					try {
						if (serverString!="") {
							url = new URL(serverString + "/tracking/get.php");
							isr = new InputStreamReader(url.openStream());
							in = new BufferedReader(isr);
							String inputLine;
							while ((inputLine = in.readLine()) != null) {
								readString += inputLine;
								if (mustBeAborted) break;
							}
							in.close();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					if ("" != readString) {
						Matcher m = compiledPattern.matcher(readString);
						if (m.find()) {
							lat = Double.parseDouble(m.group(1));
							lon = Double.parseDouble(m.group(2));
							if ((newLocation == null) || (lat != newLocation.getLatitude()) || (lon != newLocation.getLongitude())) {
								double a;
								if (newLocation != null) {
									double x = lat - newLocation.getLatitude();
									double y = lon - newLocation.getLongitude();
									a = Math.toDegrees(Math.atan2(y, x));
								} else {
									a = 0;
								}
								newLocation = new Location(LocationManager.GPS_PROVIDER);
								newLocation.setLatitude(lat);
								newLocation.setLongitude(lon);
								newLocation.setAltitude(100);
								newLocation.setBearing((float) a);
								if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
									newLocation.setElapsedRealtimeNanos(0);
								}
								newLocation.setSpeed(50);
								newLocation.setAccuracy(5);
								//newLocation.setProvider(LocationManager.GPS_PROVIDER);
								newLocation.setTime(System.currentTimeMillis());
								//newLocation.setExtras(null);
								Method method;
								try {
									method = Location.class.getMethod("makeComplete", new Class[0]);
									if (method != null)
									{
										try
										{
											method.invoke(newLocation, new Object[0]);
										}
										catch (Exception exception) { }
									}
								} catch (NoSuchMethodException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, newLocation);
							}
						}
					}
				}
			};
			timer = new Timer();
			timer.scheduleAtFixedRate(task,0, MockGpsService.SHORTTIMEOUT4HTMLREQUEST);
	}

	private void stopProvider() {
		mustBeAborted = true;
		timer.cancel();
		timer = null;
		locationManager.removeTestProvider(LocationManager.GPS_PROVIDER);
	}
	@Override
    public void onCreate() {
		locationManager	= (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 2, this);
    }

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {
			Toast.makeText(this, R.string.local_service_started, Toast.LENGTH_SHORT).show();
            Intent notificationIntent = new Intent(this, MockGpsProviderActivity.class);
            notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            String server = intent.getStringExtra("ServerName");
            this.serverString = server;
            this.startProvider ();
			Notification notification = null;
			if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				notification = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.gpsweb)
                    .setContentTitle(getText(R.string.local_service_label))
                    .setContentText(getText(R.string.local_service_started))
                    .setWhen(System.currentTimeMillis())
                    .setContentIntent(pendingIntent)
                    .build();
			} else {
                notification = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.gpsweb)
                        .setContentTitle(getText(R.string.local_service_label))
                        .setContentText(getText(R.string.local_service_started))
                        .setWhen(System.currentTimeMillis())
                        .setContentIntent(pendingIntent)
                        .build();
            }
            this.startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification);
        } else if (intent.getAction().equals(Constants.ACTION.STOPFOREGROUND_ACTION)) {
                Toast.makeText(this, R.string.local_service_stopped, Toast.LENGTH_SHORT).show();
                this.stopProvider();
                this.stopForeground(true);
                this.stopSelf();
        }
		return Service.START_STICKY;
	}


	@Override
    public void onDestroy() {
		locationManager.removeUpdates(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
	@Override
	public void onLocationChanged(Location location) {
		Toast.makeText(this.getBaseContext(), location.getLatitude() + " " + location.getLongitude(), Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onStatusChanged(String s, int i, Bundle bundle) {

	}

	@Override
	public void onProviderEnabled(String s) {

	}

	@Override
	public void onProviderDisabled(String s) {

	}
}