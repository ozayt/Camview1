package com.example.clint_test0;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import java.io.ByteArrayInputStream;

public class MainActivity extends AppCompatActivity {
    private TextView textView;
    private EditText editText;
    private byte[] buffer = new byte[20];
    Handler handler;
    public static int MSG_READ0 = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Handler.Callback hnadlercb = new Handler.Callback(){

            @Override
            public boolean handleMessage(@NonNull Message msg) {
                if(msg.what == MSG_READ0) {
                    textView.setText(Integer.toString(msg.arg1));
                }
                return true ;
            }
        };
        handler= new Handler(hnadlercb);


        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        textView= findViewById(R.id.textView);
        editText=findViewById(R.id.plain_text_input);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Send to server ? ", Snackbar.LENGTH_LONG)
                        .setAction("SEND", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                textView.setText(editText.getText());
                                byte [] bytes = editText.getText().toString().getBytes();
                                for(int i =0 ; i<buffer.length ; i++){
                                    try{
                                        buffer[i] = bytes[i];
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }

                                }
                                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer);
                                Client clnt = new Client("192.168.13.85",5556,byteArrayInputStream,handler);

                            }
                        }).show();

            }
        });
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

    @Override
    protected void onStart() {
        super.onStart();

    }

}
