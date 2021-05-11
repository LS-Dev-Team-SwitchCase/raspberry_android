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
import android.widget.Button;
import android.widget.EditText;
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

    MyThread th;
    MySoundPlayer ms;
    TextView tv;
    ImageView iv;
    EditText et;
    Button bt;
    String url = "192.168.1.25";
    boolean condition = false;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = (TextView) findViewById(R.id.status);
        iv = (ImageView) findViewById(R.id.pillImage);
        et = (EditText) findViewById(R.id.url);
        bt = (Button) findViewById(R.id.button_info);
        ms.initSounds(getApplicationContext());
        et.setText("" + url);
        tv.setText("Initiate process at : " + url);
        bt.setText("Initiate");
    }

    public void getinfordata(View view) {
        if(condition == false) {
            condition = true;
            th = new MyThread();
            th.start();
            bt.setText("Stop");
        }
        else{
            condition = false;
            tv.setText("Initiate process at : " + url);
            bt.setText("Initiate");
        }
    }

    public void changeUrl(View view) {
        url = et.getText().toString();
        et.setText("" + url);
        tv.setText("Initiate process at : " + url);
    }

    class MyThread extends Thread {
        public void run() {
            while(condition) {
                new JsonTask(tv,iv,ms).execute("http://"+url+":3000/curStat");

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        condition = false;
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
        tv.setText(sUrl.toString() + "에서 현재 상태를 검사중입니다.");
        iv.setImageResource(R.drawable.nopill);

        if(result.contains("true")) {
            ms.play(MySoundPlayer.DING_DONG);
            tv.setText("약을 꺼내주세요!");
            iv.setImageResource(R.drawable.pill);
        }
    }
}
