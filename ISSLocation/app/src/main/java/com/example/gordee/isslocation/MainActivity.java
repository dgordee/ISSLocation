package com.example.gordee.isslocation;

import android.app.ProgressDialog;
import android.graphics.Point;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.net.*;
import android.widget.Button;
import android.widget.TextView;
import java.io.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import org.json.*;


public class MainActivity extends AppCompatActivity {

    TextView locString;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

         locString = (TextView)findViewById(R.id.loc_string);
        Button showLoc = (Button)findViewById(R.id.show_button);

        showLoc.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){

                AsyncTaskRunner runner = new AsyncTaskRunner();
                runner.execute();

            }

        });



        /*
        URL url;
        try {
            url = new URL("http://api.open-notify.org/iss-now.json");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        try {
            con.setRequestMethod("GET");
        } catch (ProtocolException e) {
            e.printStackTrace();
        }
        */


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
            locString.setText(result);
        }
    }

}


