package com.example.camview1;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Objects;

public class Tab2_Fragment extends Fragment {
    private TextView text;
    private View view;
    private Boolean firstcreated = false ;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(!firstcreated){
            view = inflater.inflate(R.layout.tab2_fragment,container,false);
            text=view.findViewById(R.id.tab2_text);
            ((MainActivity) Objects.requireNonNull(getActivity())).append_to_tab3_fragment(Thread.currentThread().getName()+" :onCreateView Tab2_Fragment");
            firstcreated=true;
        }
        return view;
    }
    @SuppressLint("SetTextI18n")
    void setText(){
        text.setText("Tab 2 text is set now ");
    }
}
