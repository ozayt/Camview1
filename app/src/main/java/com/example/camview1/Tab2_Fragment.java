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
    List<List<Object[]>> level_list;
     TextView text;
    private View view;
    private Boolean firstcreated = false;
    private NestedScrollView nestedview;
    LinearLayout linearLayout;
    private CameraCharacteristics cameraChar;
    private boolean cameraChar_is_inLayout =false;
    private List<CameraCharacteristics.Key> raw_char_keys;
    private List<String> avaible_types_string = new ArrayList<String>();
    private Object[][] dev_dir;
    int dir_level = 0 ;
    String most_recent_dir_id;

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
        if(cameraChar!=null & (!cameraChar_is_inLayout)&(level_list!=null)){
            linearLayout.removeAllViews();
            linearLayout.addView(text);
            //dir_level 0 root dir_level
            if(dir_level ==0){
            text.setText("CameraCharacteristics.Keys:");
            addto_linearLayout(level_list, dir_level,"");}
            cameraChar_is_inLayout =true;
        }
        return view;
    }

    void set_frag2_CameraCharacteristics(CameraCharacteristics cam){
        this.cameraChar = cam;
        raw_char_keys = new ArrayList<CameraCharacteristics.Key>();
        for (CameraCharacteristics.Key<?> key :cameraChar.getKeys()) {
            raw_char_keys.add(key);
        }
        //scan dir
//        scan_dir(raw_char_keys);
    }
    private void addto_linearLayout(List<CameraCharacteristics.Key<?>> keys){


        int j = 0;
        for (CameraCharacteristics.Key<?> key :keys) {
                if(((MainActivity)getContext())!=null) {
                    Button btnTag = new Button(((MainActivity) getContext()));
                    btnTag.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    btnTag.setText(key.toString());
                    btnTag.setId(j);
                    linearLayout.addView(btnTag);

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
    void scan_dir(List<CameraCharacteristics.Key> list){
        int i=0;
        for (CameraCharacteristics.Key element : list){
            String string = element.toString();
            make_dir(string,i);
            i++;
        }
        System.out.println(level_list);
    }
    void make_dir(String str0 , int key_index ){
        if(level_list==null){
            level_list= new ArrayList<List<Object[]>>();
        }
        int i =str0.indexOf("(");
        int j =str0.lastIndexOf(")");
        String sub_str0=str0.substring(i+1,j);
        System.out.println(sub_str0);

        int level_index = 0;
        int last_dot = 0;
        while (last_dot>=0){
            last_dot  =sub_str0.indexOf(".",last_dot+1);
            if(last_dot!=-1){//found next dot (meaning this is a dictionary)
                //First check if level_list contains dir_level
                if(level_list.size()<=level_index) {// if dir_level doesnt exist yet
                    level_list.add( new ArrayList<Object[]>());
                }
                String sbs = sub_str0.substring(0,last_dot);
                System.out.println(sbs);
                Boolean contains=false;
                for(Object[] pair : level_list.get(level_index)){
                    if(pair[0].toString().equals(sbs)){
                        contains=true;
                        break;
                    }
                }
                if(!contains){
                    Object[] newpair= {sbs,null};
                    level_list.get(level_index).add(newpair);
                }
                level_index++;
                //directory add with null
            }else{
                //element add with key index
                if(level_list.size()<=level_index) {// if dir_level doesnt exist yet
                    level_list.add( new ArrayList<Object[]>());
                }
                String sbs = sub_str0;
                System.out.println(sbs);
                Object[] newpair= {sbs,key_index};
                level_list.get(level_index).add(newpair);
                break;
            }
        }
    }
    List<CameraCharacteristics.Key> getRaw_char_keys(){return raw_char_keys;}
    void addto_linearLayout(List<List<Object[]>> lvl_list,int level,String root){
        if(((MainActivity)getContext())!=null) {
                int j = 0;
                for(Object[] pair : level_list.get(level)){
                    String strid = ((String)pair[0]);
                    if(strid.contains(root)||level==0){
                        Button btnTag = new Button(((MainActivity) getContext()));
                        btnTag.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        btnTag.setText(strid);
                        btnTag.setId(j);
                        btnTag.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                linearLayout.removeAllViews();
                                linearLayout.addView(text);
                                text.setText(strid);
                                most_recent_dir_id = strid;
                                if(pair[1]==null){
                                    text.append(" Opened directory, level:"+(level+1));
                                }else{
                                    text.append("Dir level: "+level+" Accesing key with index:"+pair[1].toString()+" -> "+raw_char_keys.get((int)pair[1]));
                                }
                                dir_level++;
                                try {
                                    addto_linearLayout(level_list,dir_level,strid);
                                }catch (IndexOutOfBoundsException e){
                                    ((MainActivity)getActivity()).append_to_tab3_fragment("Found last element: "+strid);
                                }

                            }
                        });
                        linearLayout.addView(btnTag); }
                }
        }
    }
    void get_root_mrm(String most_recent_direc_id){
        if(most_recent_direc_id.lastIndexOf(".")>=0){
        this.most_recent_dir_id= most_recent_direc_id.substring(0,most_recent_direc_id.lastIndexOf("."));}

    }
}
