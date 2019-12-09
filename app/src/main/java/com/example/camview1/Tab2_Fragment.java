package com.example.camview1;

import android.annotation.SuppressLint;
import android.hardware.camera2.CameraCharacteristics;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Tab2_Fragment extends Fragment {
    private TextView text;
    private View view;
    private Boolean firstcreated = false;
    private NestedScrollView nestedview;
    private LinearLayout linearLayout;
    private CameraCharacteristics cameraChar;
    private boolean added=false;
    private List<CameraCharacteristics.Key> char_list;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (!firstcreated) {
            view = inflater.inflate(R.layout.tab2_fragment, container, false);
            nestedview = (NestedScrollView) container;
            text = view.findViewById(R.id.tab2_text);
            linearLayout = view.findViewById(R.id.LinearLayout);

            ((MainActivity) Objects.requireNonNull(getActivity())).append_to_tab3_fragment(Thread.currentThread().getName() + " :onCreateView Tab2_Fragment");
            firstcreated = true;
        }
        if(cameraChar!=null & (!added)){
            addto_linearLayout(cameraChar.getKeys());
            added=true;
        }
        return view;
    }

    @SuppressLint("SetTextI18n")
    void setText() {
        text.setText("Tab 2 text is set now ");
    }
    void prepare_frag2(CameraCharacteristics cam){
        this.cameraChar = cam;

    }
    void addto_linearLayout(List<CameraCharacteristics.Key<?>> keys ){
//
//        List<CameraCharacteristics.Key<?>> malist = camc.getKeys();
//        for(CameraCharacteristics.Key<?> key :malist ){
//            key.toString(
//        }
        char_list = new ArrayList<CameraCharacteristics.Key>();
        int j = 0;
        for (CameraCharacteristics.Key<?> key :keys) {
                if(((MainActivity)getContext())!=null) {
                    Button btnTag = new Button(((MainActivity) getContext()));
                    btnTag.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    btnTag.setText(key.toString());
                    btnTag.setId(j);
                    linearLayout.addView(btnTag);
                    char_list.add(key);
                    final int finalJ = j;
                    btnTag.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            text.setText(Integer.toString(v.getId()));
                            ((Button)v).setText(cameraChar.get(char_list.get(finalJ)).toString());
                            if(added){added=false;}

                        }
                    });
                    j++;
                }
        }
//        for (int i=0; i <10 ; i++){
//            Button btnTag = new Button(getActivity().getApplicationContext());
//            btnTag.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
//            btnTag.setText(i);
//            btnTag.setId(i);
//            linearLayout.addView(btnTag);
//        }
    }
}
