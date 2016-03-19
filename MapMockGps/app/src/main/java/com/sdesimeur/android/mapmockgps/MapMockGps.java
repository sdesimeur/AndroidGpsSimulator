package com.sdesimeur.android.mapmockgps;

import android.app.ActionBar;
import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.overlay.Polyline;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.reader.MapDataStore;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.InternalRenderTheme;

import android.app.Activity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Environment;

import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;


public class MapMockGps extends Activity implements OnItemSelectedListener {
    public static final List <String> SERVERS;
    static {
        SERVERS = new ArrayList<String>() {{
            add("http://127.0.0.1");
            add("http://192.168.43.1");
            add("http://192.168.3.150/~sam/AndroidDevel");
            add("http://192.168.3.195");
            add("");
        }};
    }
    public CrossView crossImg;
    private ArrayAdapter serversListAdapter;
    private static final String MAPFILE = "GpsFictionProjects/mapsforge/local.map";
    private static final double LATITUDE = 49.59266;
    private static final double LONGITUDE = 3.65649;
    //protected ViewGroup rootView;
    private static final int SELECTEDBUTTON = 255;
    private static final int UNSELECTEDBUTTON = 100;
    private TileRendererLayer tileRendererLayer = null;
    private MyMapView mapView = null;
    //private RelativeLayout myMapLayout = null;
    //private EditText newServer;
    private boolean isOnStart = true;
    private ArrayList <String> serversList;
    protected TileCache tileCache;
    //private ViewGroup crossView = null;
    private EditText newServer;
    private SharedPreferences settings;
    private Spinner serversSpinner;
    private static final int TIME2WAIT = 500;
    private String serverString = MapMockGps.SERVERS.get(0);
    protected boolean moreThanTime = true;
    private LatLong oldCenter = new LatLong(MapMockGps.LATITUDE, MapMockGps.LONGITUDE);
    private Polyline line = null;

    private void sendCenter () {
        if ( "" != this.serverString ) {
            LatLong newCenter = this.mapView.getModel().mapViewPosition.getCenter();
            if (!(newCenter.equals(oldCenter))) {
                    if (oldCenter != null) {
						double dx = (newCenter.longitude - oldCenter.longitude);
						double dy = (newCenter.latitude - oldCenter.latitude);
						double angle = (Math.toDegrees(Math.atan2(dx,dy)));
						crossImg.setAngle(angle);
					}
                    String urlString = this.serverString + "/tracking/put.php?lat=" + newCenter.latitude + "&lon=" + newCenter.longitude;
                    try {
                        URL url = new URL(urlString);
                        InputStreamReader isr = new InputStreamReader(url.openStream());
                        isr.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(this, newCenter.latitude + "" + newCenter.longitude, Toast.LENGTH_SHORT).show();
                    this.oldCenter = newCenter;
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        AndroidGraphicFactory.createInstance(this.getApplication());
        this.setContentView(R.layout.activity_map_mock_gps);
        this.crossImg = (CrossView)this.findViewById(R.id.crossview);
        this.mapView = new MyMapView(this);
        this.mapView.init(this);
        RelativeLayout myMapLayout = (RelativeLayout) this.findViewById(R.id.mymaplayout);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        this.mapView.setLayoutParams(lp);
        myMapLayout.addView(this.mapView);
        this.mapView.setClickable(true);
        this.mapView.getMapScaleBar().setVisible(true);
        this.mapView.setBuiltInZoomControls(true);
        this.mapView.getMapZoomControls().setZoomLevelMin((byte) 10);
        this.mapView.getMapZoomControls().setZoomLevelMax((byte) 20);
        // create a tile cache of suitable size
        this.tileCache = AndroidUtil.createTileCache(this, "mapcache",
                this.mapView.getModel().displayModel.getTileSize(), 1f,
                this.mapView.getModel().frameBufferModel.getOverdrawFactor());


        Paint paintStroke = AndroidGraphicFactory.INSTANCE.createPaint();
        paintStroke.setStyle(Style.STROKE);
        paintStroke.setDashPathEffect(new float[] { 25, 15 });
        paintStroke.setStrokeWidth(getResources().getDimension(R.dimen.linewidth));
        //paintStroke.setStrokeWidth(8);
        // TODO: new mapsforge version wants an mapsforge-paint, not an android paint.
        // This doesn't seem to support transparceny
        //paintStroke.setAlpha(128);
        line = new Polyline(paintStroke, AndroidGraphicFactory.INSTANCE);
        List<LatLong> geoPoints = line.getLatLongs();
        geoPoints.add(oldCenter);
        line.setVisible(true);


        this.settings= this.getPreferences(Context.MODE_PRIVATE);
        this.loadServersSettings();

        this.newServer = (EditText) this.findViewById(R.id.new_server);
		/*Le Spinner a besoin d'un adapter pour sa presentation alors on lui passe le context(this) et
                un fichier de presentation par défaut( android.R.layout.simple_spinner_item)
		Avec la liste des elements (exemple) */
        this.initServersListAdapter();
               /* On definit une présentation du spinner quand il est déroulé         (android.R.layout.simple_spinner_dropdown_item) */
		this.serversListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
               //Enfin on passe l'adapter au Spinner et c'est tout
        this.serversSpinner = (Spinner) findViewById(R.id.servers);
		this.serversSpinner.setAdapter(this.serversListAdapter);
    }
    public void initServersListAdapter () {
        this.serversListAdapter = new ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                this.serversList
        );
    }
    protected boolean onMapTap( LatLong tapLatLong, Point layerXY, Point tapXY ){
        line.getLatLongs().add(tapLatLong);
        mapView.getModel().mapViewPosition.setCenter(tapLatLong);
        sendCenter();
        return true;
    }
    @Override
    public void onStart() {
        super.onStart();
        this.mapView.getModel().mapViewPosition.setCenter(oldCenter);
        this.mapView.getModel().mapViewPosition.setZoomLevel((byte) 15);
        //int lastServerIndex = ((this.serverString.isEmpty())?0:this.serversListAdapter.getPosition(this.serverString));
        //this.setNewServer(this.serverString);
        // tile renderer layer using internal render theme
        MapDataStore mapDataStore = new MapFile(this.getMapFile());
        this.tileRendererLayer = new TileRendererLayer(tileCache, mapDataStore,
                this.mapView.getModel().mapViewPosition, false, true, AndroidGraphicFactory.INSTANCE){
            @Override
            public boolean onTap( LatLong tapLatLong, Point layerXY, Point tapXY ) {
                return onMapTap(tapLatLong, layerXY, tapXY);
            }
        };
        this.tileRendererLayer.setXmlRenderTheme(InternalRenderTheme.OSMARENDER);

        // only once a layer is associated with a mapView the rendering starts
        this.mapView.getLayerManager().getLayers().add(this.tileRendererLayer);
        this.mapView.getLayerManager().getLayers().add(line);
        this.serversSpinner.setSelection(this.serversListAdapter.getPosition(this.serverString));
        this.serversSpinner.setOnItemSelectedListener(this);
    }

	@Override
	protected void onPause() {
		super.onPause();
	}
    @Override
    protected void onStop() {
        super.onStop();
        this.mapView.getLayerManager().getLayers().remove(this.tileRendererLayer);
        this.tileRendererLayer.onDestroy();
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
        this.tileCache.destroy();
        this.mapView.getModel().mapViewPosition.destroy();
        this.mapView.destroy();
        AndroidGraphicFactory.clearResourceMemoryCache();
	}
    private File getMapFile() {
        File file = new File(Environment.getExternalStorageDirectory(), MapMockGps.MAPFILE);
        boolean greaterOrEqKitkat = Build.VERSION.SDK_INT >= 19;
        if (greaterOrEqKitkat) {
        		file = new File("/mnt/media_rw/sdcard1/", MapMockGps.MAPFILE);
                //file = new File(Environment.getExternalStoragePublicDirectory(""), MapMockGps.MAPFILE);
        	} else {
        		file = new File(Environment.getExternalStorageDirectory(), MapMockGps.MAPFILE);
        	}
        return file;
    }

	protected void redrawLayers() {
		this.mapView.getLayerManager().redrawLayers();
	}
    private void setServerString (String server) {
        this.serverString=server;
        //this.mapView.setServerString(server);
    }
    private void setNewServer (String server) {
        this.newServer.setText(server, TextView.BufferType.EDITABLE);
        this.setServerString(server);
        //this.serversSpinner.setSelection(this.serversListAdapter.getPosition(server));
        this.mapView.requestFocus();
        InputMethodManager imm = (InputMethodManager)this.getSystemService(Service.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.newServer.getWindowToken(), 0);
        this.saveServersSettings();
    }
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String s = this.serversListAdapter.getItem(position).toString();
        this.setNewServer(s);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        String s = this.serversListAdapter.getItem(0).toString();
        this.setNewServer(s);
    }

    public void addServer (View view) {
        String newServerString = this.newServer.getText().toString();
        if (!(newServerString.isEmpty())) {
            this.serversListAdapter.add(newServerString);
            //this.initServersListAdapter();
            //this.setNewServer(newServerString);
            this.serversSpinner.setSelection(this.serversListAdapter.getPosition(newServerString));
        }
        //this.newServer.setText("",null);
        //this.newServer.clearComposingText();
    }
    public void delServer (View view) {
        this.serversListAdapter.remove(this.serversSpinner.getSelectedItem());
        //this.initServersListAdapter();
        //this.setNewServer(this.serversListAdapter.getItem(0).toString());
        this.serversSpinner.setSelection(0);
    }
    public void resetPosition (View view) {
        oldCenter = new LatLong(MapMockGps.LATITUDE,MapMockGps.LONGITUDE);
        if (this.mapView != null)
            this.mapView.getModel().mapViewPosition.setCenter(oldCenter);
    }
    private void saveServersSettings () {
        SharedPreferences.Editor editor = this.settings.edit();
        editor.clear();
        for (int i = 0; i < (this.serversListAdapter.getCount()); i++) {
            editor.putString("server" + i, this.serversListAdapter.getItem(i).toString());
        }
        editor.putString("last",this.serverString);
        editor.putString("lastlat",Double.toString(this.mapView.getModel().mapViewPosition.getCenter().latitude));
        editor.putString("lastlon",Double.toString(this.mapView.getModel().mapViewPosition.getCenter().longitude));
        editor.commit();
    }
    private void loadServersSettings () {
        this.serversList = new ArrayList<String>();
        String lastServer = this.settings.getString("last",MapMockGps.SERVERS.get(0));
        this.setServerString(lastServer);
        int i = 0;
        String tmp = this.settings.getString ("server"+i,"####");
        while ("####" != tmp) {
            this.serversList.add(tmp);
            i++;
            tmp = this.settings.getString ("server"+i,"####");
        }
        if (this.serversList.size()==0)
            this.serversList.addAll(MapMockGps.SERVERS);
        String txt = null;
        txt = this.settings.getString("lastlat","#");
        double latitude = ((txt == "#") ? MapMockGps.LATITUDE : Double.parseDouble(txt) );
        txt = this.settings.getString("lastlon","#");
        double longitude = ((txt == "#") ? MapMockGps.LONGITUDE : Double.parseDouble(txt) );
        oldCenter = new LatLong(latitude, longitude);
    }

}
