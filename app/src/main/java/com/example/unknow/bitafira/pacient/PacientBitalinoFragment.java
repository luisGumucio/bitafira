package com.example.unknow.bitafira.pacient;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.bitalino.comm.BITalinoFrame;
import com.example.unknow.bitafira.LoginActivity;
import com.example.unknow.bitafira.R;
import com.example.unknow.bitafira.service.BitafiraService;
import com.example.unknow.bitafira.service.BitalinoService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by luis_gumucio on 15-05-18.
 */

public class PacientBitalinoFragment extends Fragment {

    private EditText txtTime;
    private Button btStart;
    private CountDownTimer countDownTimer;
    ProgressDialog progressDialog;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pacient_bitalino_evaluation, parent, false);
    }

    // This event is triggered soon after onCreateView().
    // onViewCreated() is only called if the view returned from onCreateView() is non-null.
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressDialog = new ProgressDialog(getContext(),
                R.style.AppTheme_Dark_Dialog);
        txtTime = (EditText) view.findViewById(R.id.txt_time_bita);
        btStart = (Button) view.findViewById(R.id.btn_time_start);
        btStart.setOnClickListener(onStartTime);
        BitafiraService.setUpdateListener(this);
    }

    private View.OnClickListener onStartTime = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            btStart.setEnabled(false);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Conectando...");
            getActivity().startService(new Intent(getActivity(),BitafiraService.class));
        }
    };

    public void startProgress() {
        progressDialog.show();
    }
    public void stopProgress() {
        progressDialog.dismiss();
    }

    public void updateText(String obj) {
        txtTime.setText(obj);
    }
}

