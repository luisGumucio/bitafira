package com.example.unknow.bitafira.pacient;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bitalino.comm.BITalinoFrame;
import com.example.unknow.bitafira.LoginActivity;
import com.example.unknow.bitafira.R;
import com.example.unknow.bitafira.model.Evaluation;
import com.example.unknow.bitafira.model.Pacient;
import com.example.unknow.bitafira.service.BitafiraService;
import com.example.unknow.bitafira.service.BitalinoService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by luis_gumucio on 15-05-18.
 */

public class PacientBitalinoFragment extends Fragment {

    private EditText txtTime;
    private Button btStart;
    private CountDownTimer countDownTimer;
    private Pacient pacient;
    private DatabaseReference dbEvaluation;
    private TextView full, email, rol;
    ProgressDialog progressDialog;
    private String id;

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
        full = (TextView) view.findViewById(R.id.txtNameP);
        email = (TextView) view.findViewById(R.id.txtEmail);
        rol = (TextView) view.findViewById(R.id.txtRole);
        txtTime = (EditText) view.findViewById(R.id.txt_time_bita);
        btStart = (Button) view.findViewById(R.id.btn_time_start);
        btStart.setOnClickListener(onStartTime);
        BitafiraService.setUpdateListener(this);
        pacient = (Pacient) getArguments().getSerializable("PACIENT");
        init();
    }

    private View.OnClickListener onStartTime = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Conectando...");
            progressDialog.show();
            Evaluation evaluation = new Evaluation();
            id = dbEvaluation.push().getKey();
            evaluation.setId(id);
            Calendar cal = Calendar.getInstance();
            Date fechaActual = cal.getTime();
            DateFormat dateFormat = dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            evaluation.setDateStart(dateFormat.format(fechaActual));
            evaluation.setTimeEvaluation(txtTime.getText().toString());
            dbEvaluation.child(id).setValue(evaluation).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        countDownTimer = new CountDownTimer(3000, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                            }

                            @Override
                            public void onFinish() {
                                getActivity().startService(new Intent(getActivity(), BitafiraService.class));
                            }
                        };
                        countDownTimer.start();
                       // btStart.setEnabled(false);
                    }
                }
            });
        }
    };

    public void stopProgress() {
        progressDialog.dismiss();
    }

    public long getTime() {
        String string = txtTime.getText().toString();
        String[] parts = string.split(":");
        String part1 = parts[0];
        String part2 = parts[1];
        long time = TimeUnit.MINUTES.toMillis(Long.parseLong(part2)) +
                TimeUnit.HOURS.toMillis(Long.parseLong(part1));
        return time;
    }

    public void updateText(String obj) {
        txtTime.setText(obj);
    }

    private void init() {
        if (getArguments() != null) {
            pacient = (Pacient) getArguments().getSerializable("PACIENT");
        }
        full.setText(pacient.getName() + " " + pacient.getLastName());

        rol.setText(pacient.getRole());
        email.setText(pacient.getEmail());
        dbEvaluation = FirebaseDatabase.getInstance().getReference("evaluations").child(pacient.getId());
    }

    public String getIdEvaluation() {
        return id;
    }

    public String getIdPacient() {
        return pacient.getId();
    }
}

