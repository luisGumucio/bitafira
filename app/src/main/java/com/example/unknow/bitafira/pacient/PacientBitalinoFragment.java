package com.example.unknow.bitafira.pacient;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bitalino.comm.BITalinoFrame;
import com.example.unknow.bitafira.LoginActivity;
import com.example.unknow.bitafira.R;
import com.example.unknow.bitafira.model.Evaluation;
import com.example.unknow.bitafira.model.EvaluationActive;
import com.example.unknow.bitafira.model.Pacient;
import com.example.unknow.bitafira.service.BitafiraService;
import com.example.unknow.bitafira.service.BitalinoService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
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

    private EditText txtHour, txtMinute, txtSecond;
    private Button btStart, btCancelar;
    private CountDownTimer countDownTimer;
    private Pacient pacient;
    private DatabaseReference dbEvaluation, dbEvaluationActive;
    private TextView full, email, rol, txtTime, txtFile;
    ProgressDialog progressDialog;
    private String id;
    private String idEvaluationActive;

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
        txtTime = (TextView) view.findViewById(R.id.txt_time_bita);
        txtFile = (TextView)view.findViewById(R.id.txt_file);
        txtHour = (EditText) view.findViewById(R.id.txt_hour);
        txtMinute = (EditText) view.findViewById(R.id.txt_minute);
        txtSecond = (EditText) view.findViewById(R.id.txt_second);
        btCancelar = (Button) view.findViewById(R.id.btn_cancelar);
        btStart = (Button) view.findViewById(R.id.btn_time_start);
        btStart.setOnClickListener(onStartTime);
        btCancelar.setOnClickListener(onCancelar);
        BitafiraService.setUpdateListener(this);
        pacient = (Pacient) getArguments().getSerializable("PACIENT");
        init();
    }

    private View.OnClickListener onStartTime = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Query query = dbEvaluationActive.orderByChild("evaluation").equalTo(true);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {

                            Toast toast = Toast.makeText(getContext(),"El paciente esta en evaluacion.",
                                    Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();

                    } else {
                        evaluate();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    };
    private void evaluate() {
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Conectando...");
        progressDialog.show();
        final Evaluation evaluation = new Evaluation();
        id = dbEvaluation.push().getKey();
        evaluation.setId(id);
        Calendar cal = Calendar.getInstance();
        Date fechaActual = cal.getTime();
        DateFormat dateFormat = dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        evaluation.setDateStart(dateFormat.format(fechaActual));
        evaluation.setTimeEvaluation(getTimeEvaluation());
        dbEvaluation.child(id).setValue(evaluation).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    EvaluationActive evaluationActive = new EvaluationActive();
                    String idActive = dbEvaluationActive.push().getKey();
                    evaluationActive.setId(idActive);
                    evaluationActive.setStart_time(evaluation.getDateStart());
                    evaluationActive.setIdEvaluation(evaluation.getId());
                    evaluationActive.setIdPacient(pacient.getId());
                    evaluationActive.setEvaluation(true);
                    dbEvaluationActive.child(idActive).setValue(evaluationActive);
                    idEvaluationActive = evaluationActive.getId();
                    countDownTimer = new CountDownTimer(3000, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                        }

                        @Override
                        public void onFinish() {
                            getActivity().startService(new Intent(getActivity(), BitafiraService.class)
                                    .putExtra("Evaluation_id", evaluation.getId()));
                        }
                    };
                    countDownTimer.start();
                    // btStart.setEnabled(false);
                }
            }
        });
    }

    private View.OnClickListener onCancelar = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            getActivity().stopService(new Intent(getActivity(), BitafiraService.class));
        }
    };

    public void stopProgress(String evaluation, String idEvaluation) {
        dbEvaluation.child(idEvaluation).removeValue();
        dbEvaluationActive.child(evaluation).removeValue();
        progressDialog.dismiss();
    }
    public void stopProgress() {
        progressDialog.dismiss();
    }

    public long getTime() {
        String hour = txtHour.getText().toString();
        String minute = txtMinute.getText().toString();
        String Second = txtSecond.getText().toString();
        if(TextUtils.isEmpty(hour)) {
            hour = "00";
        }
        if(TextUtils.isEmpty(minute)) {
            minute = "00";
        }
        if(TextUtils.isEmpty(Second)) {
            Second = "00";
        }
        long time = TimeUnit.HOURS.toMillis(Long.parseLong(hour)) +
                TimeUnit.MINUTES.toMillis(Long.parseLong(minute)) +
                TimeUnit.SECONDS.toMillis(Long.parseLong(Second));
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
        dbEvaluationActive = FirebaseDatabase.getInstance().getReference("evaluation_active").child(pacient.getId());
    }

    public String getIdEvaluation() {
        return id;
    }

    public String getIdPacient() {
        return pacient.getId();
    }

    public String getIdEvaluationActive() {
        return idEvaluationActive;
    }

    public String getTimeEvaluation() {
        String hour = txtHour.getText().toString();
        String minute = txtMinute.getText().toString();
        String Second = txtSecond.getText().toString();
        if(TextUtils.isEmpty(hour)) {
            hour = "00";
        }
        if(TextUtils.isEmpty(minute)) {
            minute = "00";
        }
        if(TextUtils.isEmpty(Second)) {
            Second = "00";
        }
        return hour + ":" + minute +
                ":" + Second;
    }

    public void updateTextFile(String s) {
        txtFile.setText(s);
    }

    public void clear() {
        txtFile.setText("sample.txt");
        Query query = dbEvaluationActive.orderByChild("evaluation").equalTo(true);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "issue" node with all children with id 0
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                        EvaluationActive active = issue.getValue(EvaluationActive.class);
                        active.setEvaluation(false);
                        dbEvaluationActive.child(active.getId()).setValue(active);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}

