package com.example.camview1;

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
    private boolean cameraChar_is_inLayout =false;
    private List<CameraCharacteristics.Key> raw_char_keys;
    private List<String> avaible_types_string = new ArrayList<String>();

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
        //Load the desired linearlayout ocntent
        if(cameraChar!=null & (!cameraChar_is_inLayout)){
            addto_linearLayout(cameraChar.getKeys());
            cameraChar_is_inLayout =true;
        }
        return view;
    }

    void set_frag2_CameraCharacteristics(CameraCharacteristics cam){
        this.cameraChar = cam;
    }
    private void addto_linearLayout(List<CameraCharacteristics.Key<?>> keys){

        raw_char_keys = new ArrayList<CameraCharacteristics.Key>();
        int j = 0;
        for (CameraCharacteristics.Key<?> key :keys) {
                if(((MainActivity)getContext())!=null) {
                    Button btnTag = new Button(((MainActivity) getContext()));
                    btnTag.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    btnTag.setText(key.toString());
                    btnTag.setId(j);
                    linearLayout.addView(btnTag);
                    raw_char_keys.add(key);
                    final int finalJ = j;
                    Class<?> c =cameraChar.get((raw_char_keys.get(finalJ))).getClass();
                    if(!avaible_types_string.contains(c.toString())){
                        avaible_types_string.add(c.toString());
                    }

                    btnTag.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Class<?> c =cameraChar.get((raw_char_keys.get(finalJ))).getClass();
                            if(c.isArray()){
                                text.setText(Integer.toString(v.getId())+ " is an ARRAY of ");
                                if(c.getComponentType() ==int.class){
                                    text.append("int[]");
                                    int len =((int[])cameraChar.get(raw_char_keys.get(finalJ))).length;
                                    text.append(" with lenght "+len);
                                }else if(c.getComponentType() ==byte.class){
                                    text.append("byte[]");
                                    int len =((byte[])cameraChar.get(raw_char_keys.get(finalJ))).length;
                                    text.append(" with lenght "+len);
                                }else{
                                    text.append(c.getComponentType().toString()+" not supported by this app");
                                }

//                                ((Button) v).setText(cameraChar.get(raw_char_keys.get(finalJ)).getClass().toString());
                            }else {
                                text.setText(Integer.toString(v.getId()));
                                text.setText(v.getId()+ " is a "+ c.toString());
                                if(Number.class.isAssignableFrom(c)){
                                    text.append("Number:"+((Number)cameraChar.get(raw_char_keys.get(finalJ))).toString());
                                }else{
                                    text.append(":"+(cameraChar.get(raw_char_keys.get(finalJ))).toString());
                                }
//                                ((Button) v).setText(cameraChar.get(raw_char_keys.get(finalJ)).getClass().toString());
                            }
                            if(cameraChar_is_inLayout){
                                cameraChar_is_inLayout =false;}


                        }
                    });
                    j++;
                }

        }
        for(String str: avaible_types_string){
            text.append(" "+str);
        }

    }
}
