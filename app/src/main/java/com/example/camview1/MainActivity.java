package com.example.camview1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {
    TabLayout tabs;
    TabLayout.Tab tab1;
    TabLayout.Tab tab2;
    TabLayout.Tab tab3;
    NestedScrollView nested_view;
    Tab1_Fragment tab1_fragment;
    Tab2_Fragment tab2_fragment;
    Tab3_Fragment tab3_fragment;
    FragmentManager fragmentManager;
    Handler handler ;
    CameraService cameraService;
    AppBarLayout appBarLayout;
    TabLayout.Tab currentTab;
    FloatingActionButton floatbutton;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new MainHandler(new MainHandler.MsgReadyListener() {
            @Override
            public void onMsgReady(String msg) {
                append_to_tab3_fragment(msg);
            }
        });
        cameraService = new CameraService(handler);
        setContentView(R.layout.activity_main);
        nested_view=findViewById(R.id.nestedview);
        //TABS
        tabs = findViewById(R.id.tabs);
        tab1 = tabs.getTabAt(0);
        tab2 = tabs.getTabAt(1);
        tab3 = tabs.getTabAt(2);
        tabs.addOnTabSelectedListener(tabs_callback);
        //fragments
        fragmentManager = getSupportFragmentManager();
        tab1_fragment  = new Tab1_Fragment();
        tab2_fragment = new Tab2_Fragment();
        tab3_fragment = new Tab3_Fragment();
        appBarLayout= findViewById(R.id.appbar);
        floatbutton=findViewById(R.id.floatbutton);
        floatbutton.setOnTouchListener(floatbutton_listener);
    }
    protected  void onStart() {
        super.onStart();
        tabs_callback.onTabSelected(tab3);
        tabs_callback.onTabSelected(tab2);
        tabs_callback.onTabSelected(tab1);
        //Calling camera service here
        startCameraService_mThread();
    }
    TabLayout.OnTabSelectedListener tabs_callback = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {

            if(tab.equals(tab1)) {
                currentTab=tab;
                FragmentTransaction frag = fragmentManager.beginTransaction().replace(R.id.nestedview, tab1_fragment);
                frag.commit();
            }

            if(tab.equals(tab2)){
                currentTab=tab;
                FragmentTransaction frag = fragmentManager.beginTransaction().replace(R.id.nestedview, tab2_fragment);
                frag.commit();
            }
            if(tab.equals(tab3)){
                currentTab=tab;
                FragmentTransaction frag = fragmentManager.beginTransaction().replace(R.id.nestedview, tab3_fragment);
                frag.commit();
            }
        }
        @Override
        public void onTabUnselected(TabLayout.Tab tab) {

        }
        @Override
        public void onTabReselected(TabLayout.Tab tab) {
        }
    };
    private View.OnTouchListener floatbutton_listener=new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(currentTab==tab3){
                v.performClick();
                nested_view.fullScroll(NestedScrollView.FOCUS_DOWN);
                return true;
            }else{
                return false;
            }
        }
    };
    void setTab2Text(){
        tab2_fragment.setText();
    }
    private static class MainHandler extends Handler{
        MsgReadyListener lst;
        MainHandler(MainHandler.MsgReadyListener listener){
            lst=listener;
        }
        public interface MsgReadyListener{
            /**
             * @param msg incoming msg
             */
            void onMsgReady(String msg);
            }
        public void handleMessage(Message msg) {
            String message = (String) msg.obj; //Extract the string from the Message
            lst.onMsgReady(message);
        }

    }
    void startCameraService_mThread(){
        cameraService.start_mThread();
    }
    void append_to_tab3_fragment(String msg){
        tab3_fragment.appendMsg(msg);
    }
    void interrupt_cameraService_mThread(){
        cameraService.interrupt_mThread();

    }

}
