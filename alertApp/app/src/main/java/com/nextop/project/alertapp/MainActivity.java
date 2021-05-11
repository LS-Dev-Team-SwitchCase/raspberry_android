package com.nextop.project.alertapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.BreakIterator;


public class MainActivity extends AppCompatActivity {

    MySoundPlayer ms;
    TextView tv;
    ImageView iv;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = (TextView) findViewById(R.id.status);
        iv = (ImageView) findViewById(R.id.pillImage);
        ms.initSounds(getApplicationContext());
    }

    public void getinfordata(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    new JsonTask(tv,iv,ms).execute("http://192.168.1.25:3000/curStat");

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}

class JsonTask extends AsyncTask<String, String, String> {

    URL sUrl;
    TextView tv;
    ImageView iv;
    MySoundPlayer ms;

    public JsonTask(TextView tv, ImageView iv, MySoundPlayer ms){
        this.tv = tv;
        this.iv = iv;
        this.ms = ms;
    }

    protected void onPreExecute() {
        super.onPreExecute();

        Log.e("JsonTask","Executing url");
    }

    protected String doInBackground(String... params) {


        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(params[0]);
            sUrl = url;
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();


            InputStream stream = connection.getInputStream();

            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuffer buffer = new StringBuffer();
            String line = "";

            while ((line = reader.readLine()) != null) {
                buffer.append(line+"\n");
                Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

            }

            return buffer.toString();


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        Log.e("JsonTask","Result : " + result);
        tv.setText(sUrl.toString() + "\n" + "Result : " + result);
        iv.setImageResource(R.drawable.nopill);

        if(result.contains("true")) {
            ms.play(MySoundPlayer.DING_DONG);
            tv.setText("약을 꺼내주세요!");
            iv.setImageResource(R.drawable.pill);
        }
    }
}
