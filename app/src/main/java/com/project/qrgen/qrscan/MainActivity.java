package com.project.qrgen.qrscan;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.dlazaro66.qrcodereaderview.QRCodeReaderView;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class MainActivity extends AppCompatActivity implements QRCodeReaderView.OnQRCodeReadListener {


    String scanningURL;
    AlertDialog dialog2;
    SimpleDateFormat simpleDateFormat;
    String s;
    Snackbar snackbar;
    String IP;
    String temp;
    String tempstate;
    FTPClient ftpClient;
    String filename;
    EditText editText;
    TextView textView;
    Calendar calendar;
    ToggleButton toggleButton;
    String state;
    private QRCodeReaderView mydecoderview;
    AlertDialog.Builder builder;
    AlertDialog dialog;
    ConnectivityManager connectivityManager;
    NetworkInfo networkInfo;
    AlertDialog.Builder builder2;

    class MessageSender extends AsyncTask<String,Void,Void> {
        @Override
        protected Void doInBackground(String... params) {
            FTPClient ftpClient = new FTPClient();
            String datetime;
            datetime = simpleDateFormat.format(calendar.getTime());
            try{
                ftpClient.connect(IP, 21);
                ftpClient.login("user", "pass");
                ftpClient.enterLocalPassiveMode();
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                FileOutputStream fileOutputStream = openFileOutput(filename,Context.MODE_PRIVATE);
                    fileOutputStream.write((params[0] + " " + " \r\n" + datetime + " \r\n" + state + " \r\n").getBytes());
                    fileOutputStream.close();
                    final FileInputStream fileInputStream = openFileInput(filename);

                    ftpClient.storeFile(filename, fileInputStream);
                    fileInputStream.close();
                    ftpClient.logout();
                    ftpClient.disconnect();

            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

    }

    public void functionthatdoesit(android.view.View view){
        IP = editText.getText().toString();
        textView.setText("Current IP : " + IP);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       /* */
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET},1);

        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);

        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_WIFI_STATE)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_WIFI_STATE},1);

        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_NETWORK_STATE)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_NETWORK_STATE},1);

        }


        builder2 = new AlertDialog.Builder(this);
        mydecoderview = (QRCodeReaderView) findViewById(R.id.qrdecoderview);
        mydecoderview.setBackCamera();
        mydecoderview.setOnQRCodeReadListener(this);
        mydecoderview.setAutofocusInterval(1000);
        IP = "192.168.1.33";
        connectivityManager = (ConnectivityManager) MainActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connectivityManager.getActiveNetworkInfo();
        if(!(networkInfo.getType() == ConnectivityManager.TYPE_WIFI && networkInfo.isConnectedOrConnecting()))
        {
            builder2.setMessage("Not Connected to Wifi!").setTitle("Error");
            builder2.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                }
            });
            dialog2 = builder2.create();
            dialog2.show();
        }
        else{
            builder2.setMessage("Connected to Wifi! Please Enter Server IP").setTitle("Set IP");
            builder2.setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(MainActivity.this,"Enter Server IP",Toast.LENGTH_LONG).show();
                }
            });
            dialog2 = builder2.create();
            dialog2.show();
        }
        builder= new AlertDialog.Builder(this);
        builder.setMessage("Already Punched! Try Again?").setTitle("Already Punched");
        builder.setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mydecoderview.setQRDecodingEnabled(true);
            }
        });
        dialog = builder.create();
        temp = " ";
        calendar = Calendar.getInstance();
        textView = (TextView) findViewById(R.id.txt);
        editText = (EditText) findViewById(R.id.editxt);

        filename = "MyFile.txt";
        simpleDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
        tempstate = " ";
        toggleButton = (ToggleButton) findViewById(R.id.radiob);
        toggleButton.setText("Punch Out");
        toggleButton.setTextOff("Punch Out");
        toggleButton.setTextOn("Punch In");
        state = "0";
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    state = "1";
                }
                else{
                    state = "0";
                }
            }
        });
    }

    @Override
    public void onQRCodeRead(String text, PointF[] points) {
        scanningURL=text;
        if(!(temp.equals(scanningURL + state))) {
            //Toast.makeText(getApplicationContext(), " Punched " + " " + scanningURL, Toast.LENGTH_SHORT).show();
            AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
            builder1.setTitle("PUNCHED!").setMessage(scanningURL + " :" + (state.equals("1")?"Punch In":"Punch Out"));
            builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mydecoderview.setQRDecodingEnabled(true);
                }
            });
            AlertDialog dialog1 = builder1.create();
            MessageSender messageSender = new MessageSender();
            messageSender.execute(scanningURL);
            MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.sound);
            mediaPlayer.start();
            dialog1.show();
            //
        }
        else{
            MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.error);
            mediaPlayer.start();
            dialog.show();
            //Toast.makeText(getApplicationContext(),"Already Entered!",Toast.LENGTH_SHORT).show();

        }
        temp = scanningURL + state;
        mydecoderview.setQRDecodingEnabled(false);

        /*        listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            mydecoderview.setQRDecodingEnabled(true);
            }
        };*/
    }


    public void cameraNotFound() {

    }


    public void QRCodeNotFoundOnCamImage() {

    }


    @Override
    protected void onResume() {
        super.onResume();
        mydecoderview.startCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mydecoderview.stopCamera();

    }


}