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
import android.widget.Button;

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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String newString;
    private Double lat = 0.0, lon = 0.0;
    int count = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //newString= (String) savedInstanceState.getSerializable("location");
       // Intent in = getIntent();

        AsyncTaskRunner runner = new AsyncTaskRunner();
        runner.execute();
        updateMap.run();
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
            h2.postDelayed(updateMap, 5000);
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




    private class Point2D {
        double x;
        double y;

        public Point2D(double x, double y){
            this.x = x;
            this.y = y;
        }

        public double getX(){
            return x;
        }

        public double getY() {
            return y;
        }
    }

    private class AsyncTaskRunner extends AsyncTask<String, String, String> {
        private String resp;
        ProgressDialog progressDialog;

        public String getLoc() throws IOException {

            URL url = new URL("http://api.open-notify.org/iss-now.json");
            URLConnection yc = url.openConnection();
            //BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
            InputStream in = yc.getInputStream();
            JsonReader reader = new JsonReader(new InputStreamReader(yc.getInputStream(), "UTF-8"));

            Point2D p = null;
            reader.beginObject();
            while(reader.hasNext()){
                String loc = reader.nextName();
                if(loc.equals("iss_position")){
                    p = readPos(reader);
                }else{
                    reader.skipValue();
                }

            }
            reader.endObject();
            in.close();
            Location loc = new Location("ISS");

            return p.x + " " + p.y;
        }

        protected Point2D readPos(JsonReader reader) throws IOException{
            Log.e("yay", "supposed to be here");
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
            return new Point2D(lat, lon);
        }

        @Override
        protected String doInBackground(String... params){

            String out = "";
            try {
                out = getLoc();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return out;
        }

        @Override
        protected void onPostExecute(String result){
            newString = result;
            lat = Double.parseDouble(newString.substring(0, newString.indexOf(" ")));
            lon = Double.parseDouble(newString.substring(newString.indexOf(" ")+1, newString.length()));

            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).title("ISS Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lon)));
        }
    }

}
