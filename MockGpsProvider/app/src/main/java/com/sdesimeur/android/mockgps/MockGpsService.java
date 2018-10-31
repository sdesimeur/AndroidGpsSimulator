package com.sdesimeur.android.mockgps;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MockGpsService extends Service implements LocationListener {
	private LocationManager locationManager;
	private Location newLocation = null;
	private static final int LONGTIMEOUT4SEND = 10000;
	private static final int SHORTTIMEOUT4HTMLREQUEST = 2000;
	//private static final int WAIT = 900;
	private String pattern = null;
	private double angle = 0;
	private Pattern compiledPattern = null;
	private double lat = 49.59266;
	private double lon = 3.65649;
	private double alt = 150;
	//private int nbretrywithcancel;
	private boolean mustBeAborted;
	private boolean allreadySend = false;
	//private boolean mustWait = true;
	//private static final String MOCKGPSTHREAD = "MockGpsThread";
	private String serverString = MockGpsProviderActivity.SERVERS.get(0);
	//private MockGpsThread mockGpsThread = null;
	private final IBinder mBinder = new LocalBinder();
	//private boolean isInterrupted = false;
	private Timer timer = null;
	private boolean checkBox = false;
	//private Timer timer1 = null;

	public class LocalBinder extends Binder {
		MockGpsService getService() {
			return MockGpsService.this;
		}
	}

	public MockGpsService() {
		this.pattern = "lat\\s*\\=\\s*(\\-?\\d*\\.?\\d*)\\s*,\\s*lon\\s*\\=\\s*(\\-?\\d*\\.?\\d*)\\s*,\\s*alt\\s*\\=\\s*(\\-?\\d*\\.?\\d*)";
		this.compiledPattern = Pattern.compile(this.pattern);
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
	}

	@TargetApi(17)
	private void sendNewLocation() {
		newLocation = new Location(LocationManager.GPS_PROVIDER);
		newLocation.setLatitude(lat);
		newLocation.setLongitude(lon);
		newLocation.setAltitude(alt);
		newLocation.setBearing((float) angle);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			newLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
		}
		newLocation.setSpeed(50);
		newLocation.setAccuracy(5);
		newLocation.setTime(System.currentTimeMillis());
		Method method;
		try {
			method = Location.class.getMethod("makeComplete", new Class[0]);
			if (method != null) {
				try {
					method.invoke(newLocation, new Object[0]);
				} catch (Exception exception) {
				}
			}
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, newLocation);
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
        /*timer1 = new Timer();
        TimerTask task1 = new TimerTask() {
            @Override
            public void run() {
                sendNewLocation();
            }
        };
        timer1.scheduleAtFixedRate(task1, 0, MockGpsService.LONGTIMEOUT4SEND);*/
		mustBeAborted = false;
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				String readString = "";
				URL url = null;
				InputStreamReader isr = null;
				BufferedReader in = null;
				try {
					if (serverString != "") {
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
						alt = Double.parseDouble(m.group(3));
						if ((newLocation == null) || (lat != newLocation.getLatitude()) || (lon != newLocation.getLongitude())) {
							if (newLocation != null) {
								double x = lat - newLocation.getLatitude();
								double y = lon - newLocation.getLongitude();
								angle = Math.toDegrees(Math.atan2(y, x));
							} else {
								angle = 0;
							}
						}
					}
				}
                sendNewLocation();
			}
		};
		timer = new Timer();
		timer.scheduleAtFixedRate(task, 0, MockGpsService.SHORTTIMEOUT4HTMLREQUEST);
	}

	private void stopProvider() {
		mustBeAborted = true;
		timer.cancel();
		timer = null;
        //timer1.cancel();
        //timer1=null;
		locationManager.removeTestProvider(LocationManager.GPS_PROVIDER);
	}

	@Override
	public void onCreate() {
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
			if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }
		}
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 2, this);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent.getAction().equals(Constants.ACTION.CHANGESERVER_ACTION)) {
			String server = intent.getStringExtra("ServerName");
			serverString = server;
			if (checkBox) Toast.makeText(this, "Change to: " + serverString, Toast.LENGTH_SHORT).show();
			//saveServersSettings();
		} else if (intent.getAction().equals(Constants.ACTION.CHANGECHK_ACTION)) {
			checkBox = (intent.getBooleanExtra("CheckBox",false));

		} else if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {
			if (checkBox) Toast.makeText(this, R.string.local_service_started, Toast.LENGTH_SHORT).show();
            Intent notificationIntent = new Intent(this, MockGpsProviderActivity.class);
            notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			loadServersSettings();
			if (checkBox) Toast.makeText(this, "Set to: " + serverString, Toast.LENGTH_SHORT).show();
            startProvider ();
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
			    String reallyStop = intent.getStringExtra("ReallyStop");
                if (reallyStop.equals(Constants.ACTION.REALLY_STOP)) {
                    if (checkBox) Toast.makeText(this, R.string.local_service_stopped, Toast.LENGTH_SHORT).show();
                    this.stopProvider();
                    PreferenceManager.getDefaultSharedPreferences(this);
                    this.stopForeground(true);
                    this.stopSelf();
                }
        }
//		return Service.START_STICKY;
		return Service.START_REDELIVER_INTENT;
	}

	private void saveServersSettings () {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = settings.edit();
		editor.clear();
		editor.putString("last", this.serverString);
		editor.commit();
	}
	private void loadServersSettings () {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		this.serverString=settings.getString("last",MockGpsProviderActivity.SERVERS.get(0));
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
		if (checkBox) Toast.makeText(this.getBaseContext(), location.getLatitude() + " " + location.getLongitude(), Toast.LENGTH_SHORT).show();
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
