package com.example.camview1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

public class Tab3_Fragment extends Fragment {
    private Boolean firstcreated = false ;
    private View view;
    private TextView text;
    private NestedScrollView nestedview;
    private boolean on_frag3_view=false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(!firstcreated){
            view = inflater.inflate(R.layout.tab3_fragment,container,false);
            nestedview =(NestedScrollView)container;
            text=view.findViewById(R.id.tab3_text);
            appendMsg(Thread.currentThread().getName()+" :onCreateView Tab3_Fragment");
            firstcreated=true;
        }else{
            on_frag3_view=true;
        }

        return view;
    }

    @Override
    public void onPause() {
        on_frag3_view=false;
        super.onPause();

    }

    void appendMsg(String msg){
        if(nestedview!=null) {
//            ((MainActivity) getActivity()).scrollY(nestedview.getScrollY());
            nestedview.fullScroll(NestedScrollView.FOCUS_DOWN);
        }
        text.append(System.getProperty("line.separator"));
        text.append(msg);

    }
}
