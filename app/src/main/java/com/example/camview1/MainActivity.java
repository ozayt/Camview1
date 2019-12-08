package com.example.camview1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    }
    protected  void onStart() {
        super.onStart();
        tabs_callback.onTabSelected(tab3);
        tabs_callback.onTabSelected(tab2);
        tabs_callback.onTabSelected(tab1);

    }
    TabLayout.OnTabSelectedListener tabs_callback = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            if(tab.equals(tab1)) {
                FragmentTransaction frag = fragmentManager.beginTransaction().replace(R.id.nestedview, tab1_fragment);
                frag.commit();
            }

            if(tab.equals(tab2)){
                FragmentTransaction frag = fragmentManager.beginTransaction().replace(R.id.nestedview, tab2_fragment);
                frag.commit();
            }
            if(tab.equals(tab3)){
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
    void setTab2Text(String str){
        tab2_fragment.setText(str);
    }
}
