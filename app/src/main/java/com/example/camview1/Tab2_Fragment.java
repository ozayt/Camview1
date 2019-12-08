package com.example.camview1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
            firstcreated=true;
        }
        return view;
    }
    void setText(String str){
        text.setText(str);
    }


//    @Override
//    public void onSaveInstanceState(@NonNull Bundle outState) {
//        super.onSaveInstanceState(outState);
//        outState.putString("TEXT",text.getText().toString());
//    }
}
