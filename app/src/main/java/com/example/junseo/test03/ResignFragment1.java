package com.example.junseo.test03;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by Junseo on 2017-09-14.
 */

public class ResignFragment1 extends Fragment {

    public ResignFragment1() {
        // Required empty public constructor
    }

    @Nullable

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        View view = inflater.inflate(R.layout.fragment_resign, container, false);
        TextView myEmail = (TextView) view.findViewById(R.id.myEmail);
        EditText etName = (EditText) view.findViewById(R.id.etName);
        EditText reePawwrord = (EditText) view.findViewById(R.id.reetPassword);
        EditText reePasswordConfirm = (EditText) view.findViewById(R.id.etPasswordConfirm);

        // Inflate the layout for this fragment
        return view;
    }


}
