package com.example.gordee.isslocation;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String newString;
    private String[] info = {"", "", "", ""};
    double lat, lon, alt, vel;
    int count = 0;
    boolean centered = false;

    ListView attributes;

    ISS iss;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        attributes = (ListView)findViewById(R.id.item_list);

        AsyncTaskRunner runner = new AsyncTaskRunner();
        runner.execute();
        updateMap.run();
    }

    public void updateList(){
        info[0] = "Latitude: " + iss.getX();
        info[1] = "Longitude: " + iss.getY();
        info[2] = "Altitude: " + iss.getAltitude();
        info[3] = "Velocity: " + iss.getVelocity();
        ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1, info);
        attributes.setAdapter(adapter);
    }

    public void update(){
        AsyncTaskRunner runner = new AsyncTaskRunner();
        runner.execute();
    }


    final Handler h2 = new Handler();
    final Runnable updateMap = new Runnable() {
        @Override
        public void run() {
            update();
            h2.postDelayed(updateMap, 10000);
        }
    };



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        mMap.getUiSettings().setZoomControlsEnabled(true);

    }




    private class ISS {
        double x;
        double y;
        double velocity;
        double altitude;
        String[] astronauts;


        public ISS(double x, double y, double velocity, double altitude, String[] astronauts){
            this.x = x;
            this.y = y;
            this.velocity = velocity;
            this.altitude = altitude;
            this.astronauts = astronauts;
        }

        public double getX(){
            return x;
        }

        public double getY() {
            return y;
        }

        public String getVelocity(){return "" + velocity;}

        public double getAltitude(){return altitude;}

        public String[] getAstronauts() {
            return astronauts;
        }

        public String toString(){
            return getX() + ", " + getY() + ", " + getVelocity() + ", " + getAltitude();
        }
    }

    private class AsyncTaskRunner extends AsyncTask<String, String, String> {
        private String resp;
        ProgressDialog progressDialog;

        public ISS getLoc() throws IOException {

            URL url = new URL("https://api.wheretheiss.at/v1/satellites/25544");
            URLConnection yc = url.openConnection();
            //BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
            InputStream in = yc.getInputStream();
            JsonReader reader = new JsonReader(new InputStreamReader(yc.getInputStream(), "UTF-8"));

            lat = 0.;
            lon = 0.;
            alt = 0.;
            vel = 0.;

            reader.beginObject();
            while(reader.hasNext()){
                String loc = reader.nextName();
                if(loc.equals("latitude")){
                    lat = reader.nextDouble();
                }else if (loc.equals("longitude")){
                    lon = reader.nextDouble();
                }else if (loc.equals("velocity")) {
                    vel = reader.nextDouble();
                }else if(loc.equals("altitude")){
                    alt = reader.nextDouble();
                }else{
                    reader.skipValue();
                }
            }
            reader.endObject();
            in.close();
            return new ISS(lat, lon,vel, alt, null);
        }

        /*
        protected ISS readPos(JsonReader reader) throws IOException{
            double lat = 0, lon = 0;
            reader.beginObject();
            while (reader.hasNext()){
                String name = reader.nextName();
                if(name.equals("latitude")){
                    lat = Double.parseDouble(reader.nextString());
                }else if (name.equals("longitude")){
                    lon = Double.parseDouble(reader.nextString());
                }else{
                    reader.skipValue();
                }
            }
            reader.endObject();
            return new ISS(lat, lon);
        }*/


        @Override
        protected String doInBackground(String... params){

            String out = "";
            try {
                out = getLoc().toString();
                iss = getLoc();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return out;
        }

        @Override
        protected void onPostExecute(String result){
            SimpleDateFormat sdf = new SimpleDateFormat("YYYY/mm/dd, HH:mm:ss");
            MarkerOptions marker =new MarkerOptions().position(new LatLng(iss.getX(),
                    iss.getY())).title("ISS Location at " + sdf.format(new Date()));
            //mMap.clear();
            mMap.addMarker(marker);
            updateList();

            if(!centered) {
                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lon)));
                centered = true;
            }
        }
    }

}
