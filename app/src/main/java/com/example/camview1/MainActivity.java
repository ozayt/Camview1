package com.example.camview1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.hardware.camera2.CameraCharacteristics;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.view.KeyEvent;
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
    private int pos=1;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new MainHandler(new MainHandler.MsgReadyListener() {
            @Override
            public void onMsgReady(Message msg) {
                if(msg.what==MainHandler.STRING) {
                    String message = (String) msg.obj; //Extract the string from the Message
                    append_to_tab3_fragment(message);
                }else if(msg.what==MainHandler.CameraCharacteristics){
                    tab2_fragment.set_frag2_CameraCharacteristics((CameraCharacteristics)msg.obj); // Extract CameraCharacteristics from msg
                }
            }
        });
//        cameraService = new CameraService(handler,this);
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
        floatbutton.setOnClickListener(floatbutton_listener);
        floatbutton.setClickable(true);
    }
    protected  void onStart() {
        super.onStart();
        if(currentTab==null) {
            tabs_callback.onTabSelected(tab3);
            tabs_callback.onTabSelected(tab2);
            tabs_callback.onTabSelected(tab1);
        }
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
    private View.OnClickListener floatbutton_listener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(currentTab==tab3){
                nested_view.fullScroll(NestedScrollView.FOCUS_DOWN);
            }
            if(currentTab==tab2){
                nested_view.fullScroll(NestedScrollView.FOCUS_UP);
            }
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK & currentTab==tab2 ) {

            if(tab2_fragment.dir_level==0)super.onKeyDown(keyCode,event);
            else {
                if(tab2_fragment.in_directory) {
                    tab2_fragment.linearLayout.removeAllViews();
                    tab2_fragment.linearLayout.addView(tab2_fragment.text);
                    tab2_fragment.dir_level--;
                    tab2_fragment.get_root_mrm(tab2_fragment.most_recent_dir_id);
                    tab2_fragment.text.setText(tab2_fragment.most_recent_dir_id + " dir level " + tab2_fragment.dir_level);
                    tab2_fragment.addto_linearLayout(tab2_fragment.level_list, tab2_fragment.dir_level, tab2_fragment.most_recent_dir_id);
                }else{//leave file content
                    tab2_fragment.linearLayout.removeAllViews();
                    tab2_fragment.linearLayout.addView(tab2_fragment.text);
                    tab2_fragment.get_root_mrm(tab2_fragment.most_recent_dir_id);
                    tab2_fragment.text.setText(tab2_fragment.most_recent_dir_id + " dir level " + tab2_fragment.dir_level);
                    tab2_fragment.addto_linearLayout(tab2_fragment.level_list, tab2_fragment.dir_level, tab2_fragment.most_recent_dir_id);
                    tab2_fragment.in_directory=true;
                }
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public static class MainHandler extends Handler{
        MsgReadyListener lst;
        MainHandler(MainHandler.MsgReadyListener listener){
            lst=listener;
        }
        public static final int STRING = 0;
        public static final int CameraCharacteristics = 1 ;
        public interface MsgReadyListener{
            /**
             * @param msg incoming msg
             */
            void onMsgReady(Message msg);
            }
        public void handleMessage(Message msg) {
            lst.onMsgReady(msg);
        }

    }
    void startCameraService_mThread(){
        cameraService.start_mThread();
        cameraService.open_Camera(pos);
    }
    void append_to_tab3_fragment(String msg){
        tab3_fragment.appendMsg(msg);
    }
    void interrupt_cameraService_mThread(){
        cameraService.interrupt_mThread();

    }
    void reset_cameraService(){
        cameraService=null;
        cameraService = new CameraService(handler,this);
        if(pos==1){pos=0;}
        else{pos=1;}
    }
    void frag2_scan_cameraChar_keys(){
        tab2_fragment.scan_CameraChar_keys(tab2_fragment.getRaw_camera_char_keys());
    }
    void frag2_scan_cap_request_keys(){
//        tab2_fragment.scan_capres_keys(tab2_fragment.get
    }
}
