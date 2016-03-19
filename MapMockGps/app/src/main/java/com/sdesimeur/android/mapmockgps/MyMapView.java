package com.sdesimeur.android.mapmockgps;

import android.content.Context;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.util.Log;
import android.view.DragEvent;
import android.widget.Toast;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.view.MapView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Timer;
import java.util.TimerTask;

public class MyMapView extends MapView {

	//private static final String SERVER = "http://192.168.3.195";
	private static final int TIME2WAIT = 900;
	private String serverString = MapMockGps.SERVERS.get(0);
	protected boolean moreThanTime = true;
	private LatLong oldCenter = null;
	private MapMockGps mapMockGps;
	private Context context;
	public MyMapView(Context context) {
		super(context);
		this.context = context;
		// TODO Auto-generated constructor stub
	}
	public void init(MapMockGps mapMockGps) {
		this.mapMockGps=mapMockGps;
	}
	/*
	private void sendCenter () {
		if ( "" != this.serverString ) {
			LatLong newCenter = this.getModel().mapViewPosition.getCenter();
			if (!(newCenter.equals(oldCenter))) {
				if (moreThanTime) {
					this.moreThanTime = false;
					if (oldCenter != null) {
						float dx = (float)(newCenter.longitude - oldCenter.longitude);
						float dy = (float)(newCenter.latitude - oldCenter.latitude);
						float angle = (float) (Math.toDegrees(Math.atan(dx/dy))+((dy<0)?180.0:0.0));
						mapMockGps.crossImg.setAngle(angle);
					}
					TimerTask myTimerTask = new TimerTask() {
						@Override
						public void run() {
							moreThanTime = true;
						}
					};
					Timer timer = new Timer();
					timer.schedule(myTimerTask, MyMapView.TIME2WAIT);
					String urlString = this.serverString + "/tracking/put.php?lat=" + newCenter.latitude + "&lon=" + newCenter.longitude;
					try {
						URL url = new URL(urlString);
						InputStreamReader isr = new InputStreamReader(url.openStream());
						isr.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					Toast.makeText(this.context, newCenter.latitude + "" + newCenter.longitude, Toast.LENGTH_SHORT).show();
					this.oldCenter = newCenter;
				}
			}
		}
	}
	*/
/*
	@Override
	protected void onDraw(Canvas androidCanvas) {
		super.onDraw(androidCanvas);
		this.sendCenter();
	}
	public void setServerString(String serverString) {
		this.serverString = serverString;
	}
	*/
}
