/*
 Copyright (C)2011 Paul Houx
 All rights reserved.
 
 Based on code written by Pedro Assuncao, see:
 http://pedroassuncao.com/2009/11/android-location-provider-mock/

 Redistribution and use in source and binary forms, with or without modification, 
 are permitted without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
*/

package com.sdesimeur.android.mockgps;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MockGpsProviderActivity extends Activity implements AdapterView.OnItemSelectedListener {
	//private Intent myIntent= null;
    private LocationManager locationManager = null;
    private boolean isOnStartNewServer = true;
    private Button buttonStart = null;
    public static final List<String> SERVERS;
    static {
        SERVERS = new ArrayList<String>() {{
            add("http://127.0.0.1/");
            add("http://192.168.43.1/");
            add("http://server.sdesimeur.org/~sam/MapMockGpsPHP/");
            add("http://192.168.4.150/~sam/MapMockGpsPHP/");
            add("");
        }};
    }
    //private MockGpsService mockGpsService = null;
    private int lastServerIndex = 0 ;
    private ArrayAdapter serversListAdapter;
    private EditText newServer;
    private Button addButton, delButton;

    private ArrayList <String> serversList = new ArrayList<String>();
    private Spinner serversSpinner;
    //private ViewGroup crossView = null;
    private String serverString = "127.0.0.1";
    private String serverSelected = "";
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        this.setContentView(R.layout.main);
        this.buttonStart = (Button) this.findViewById(R.id.button_start);
        this.serversSpinner = (Spinner) findViewById(R.id.servers);
        //Création d'une liste d'élément à mettre dans le Spinner(pour l'exemple)
        //this.settings = this.getPreferences(Context.MODE_PRIVATE);
        this.loadServersSettings();
		/*Le Spinner a besoin d'un adapter pour sa presentation alors on lui passe le context(this) et
                un fichier de presentation par défaut( android.R.layout.simple_spinner_item)
		Avec la liste des elements (exemple) */
        this.initServersListAdapter();
               /* On definit une présentation du spinner quand il est déroulé         (android.R.layout.simple_spinner_dropdown_item) */
        this.serversListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Enfin on passe l'adapter au Spinner et c'est tout
        this.serversSpinner.setAdapter(this.serversListAdapter);
        this.newServer = (EditText) this.findViewById(R.id.new_server);
    }
    public void onStart() {
        super.onStart();
        this.startServer(null);
        this.isOnStartNewServer = true;
        this.serversSpinner.setSelection(this.serversListAdapter.getPosition(this.serverString));
        this.serversSpinner.setOnItemSelectedListener(this);
    //    this.setNewServer(this.serverString);
    //    this.isOnStart =false;
    }
    public void initServersListAdapter () {
        this.serversListAdapter = new ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                this.serversList
        );
    }

    public void onDestroy() {
        if (this.locationManager != null) {
            this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			this.locationManager = null;
		}
        super.onDestroy();

    }
    
    public void onBackPressed() {
    	super.onBackPressed();
    }

    public void changeServer(View view) {
        Intent myIntent = new Intent(this, MockGpsService.class);
        myIntent.putExtra("ServerName",this.serverString);
        myIntent.setAction(Constants.ACTION.CHANGESERVER_ACTION);
        this.startService(myIntent);
    }

    public void onCheck(View view) {
        Intent myIntent = new Intent(this, MockGpsService.class);
        myIntent.putExtra("CheckBox",((CheckBox)findViewById(R.id.checkBox)).isChecked());
        myIntent.setAction(Constants.ACTION.CHANGECHK_ACTION);
        this.startService(myIntent);
    }

    public void startServer(View view) {
        Intent myIntent = new Intent(this, MockGpsService.class);
        //myIntent.putExtra("ServerName",this.serverString);
        myIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
        this.startService(myIntent);
    }
    public void stopServer(View view) {
        Intent myIntent = new Intent(this, MockGpsService.class);
        myIntent.putExtra("ReallyStop",Constants.ACTION.REALLY_STOP);
        myIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
        this.startService(myIntent);
    }

    private void setNewServer (String server) {
        this.newServer.setText(server, TextView.BufferType.EDITABLE);
        //if (! (this.isOnStartNewServer)) {
        //    this.stopServer(null);
        //    this.isOnStartNewServer = false;
        //}
        this.serverString=server;
        changeServer(null);
        //this.serversSpinner.setSelection(this.serversListAdapter.getPosition(server));
        this.buttonStart.requestFocus();
        InputMethodManager imm = (InputMethodManager)this.getSystemService(Service.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.newServer.getWindowToken(), 0);
        saveServersSettings();
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
            this.setNewServer(newServerString);
            this.serversSpinner.setSelection(this.serversListAdapter.getPosition(newServerString));
        }
        //this.newServer.setText("",null);
        //this.newServer.clearComposingText();
    }
    public void delServer (View view) {
        serversListAdapter.remove(this.serversSpinner.getSelectedItem());
        //this.initServersListAdapter();
        //this.setNewServer(this.serversListAdapter.getItem(0).toString());
        serversSpinner.setSelection(0);
        this.setNewServer(this.serversListAdapter.getItem(0).toString());
        saveServersSettings();
    }
    private void saveServersSettings () {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        for (int i = 0; i < (this.serversListAdapter.getCount()); i++) {
            editor.putString("server" + i, this.serversListAdapter.getItem(i).toString());
        }
        editor.putString("last", this.serverString);
        editor.commit();
    }
    private void loadServersSettings () {
        this.serversList = new ArrayList<String>();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String lastServer = settings.getString("last",MockGpsProviderActivity.SERVERS.get(0));
        this.serverString=lastServer;
        int i = 0;
        String tmp = settings.getString ("server"+i,"####");
        while ("####" != tmp) {
            this.serversList.add(tmp);
            i++;
            tmp = settings.getString ("server"+i,"####");
        }
        if (this.serversList.size()==0)
            this.serversList.addAll(MockGpsProviderActivity.SERVERS);
    }
}
