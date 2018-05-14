package com.example.unknow.bitafira.pacient;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.unknow.bitafira.R;
import com.example.unknow.bitafira.model.Evaluation;
import com.example.unknow.bitafira.model.Pacient;
import com.example.unknow.bitafira.service.BitalinoService;
import com.example.unknow.bitafira.service.SampleService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class PacientEvaluationFragment extends Fragment {

    private EditText txtTime;
    private Button btStartTime, btCancelar;
    ProgressDialog progressDialog;
    private Pacient pacient;
    DatabaseReference dbEvaluation;
    FragmentTransaction t;
    private TextView full, email, rol;
    String id;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pacient_evaluation, parent, false);
    }

    // This event is triggered soon after onCreateView().
    // onViewCreated() is only called if the view returned from onCreateView() is non-null.
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        full = (TextView) view.findViewById(R.id.txtNameP);
        email = (TextView) view.findViewById(R.id.txtEmail);
        rol = (TextView) view.findViewById(R.id.txtRole);
        init();
        txtTime = (EditText) view.findViewById(R.id.txt_time);
        btStartTime = (Button) view.findViewById(R.id.btn_time);
        btCancelar = (Button) view.findViewById(R.id.btn_cancelar);
        btStartTime.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                startService();
            }
        });
        btCancelar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Activity activity = getActivity();
                activity.stopService(new Intent(getActivity(), BitalinoService.class));
            }
        });
        BitalinoService.setUpdateListener(this);
    }

    private void init() {

        if (getArguments() != null) {
            pacient = new Pacient();
            pacient.setId(getArguments().getString("PACIENT_ID"));
            pacient.setName(getArguments().getString("PACIENT_NAME"));
            pacient.setLastName(getArguments().getString("PACIENT_LASTNAME"));
            pacient.setPhone(getArguments().getInt("PACIENT_PHONE", 0));
            pacient.setAddress(getArguments().getString("PACIENT_DIRECCION"));
            pacient.setRole(getArguments().getString("PACIENT_ROL"));
            pacient.setPhoneRefe(getArguments().getInt("PACIENT_PHONE_REFE", 0));
            pacient.setEmail(getArguments().getString("PACIENT_EMAIL"));
            pacient.setNumberHistory(getArguments().getInt("PACIENT_HISTORIAL", 0));
        }
        full.setText(pacient.getName() + " " + pacient.getLastName());

        rol.setText(pacient.getRole());
        email.setText(pacient.getEmail());
        dbEvaluation = FirebaseDatabase.getInstance().getReference("evaluations").child(pacient.getId());
    }


    /**
     * Inicia el servicio
     */
    private void startService() {
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

                    Activity activity = getActivity();
                    activity.startService(new Intent(getActivity(), BitalinoService.class)
                            .putExtra("ID_EVALUATION", id)
                            .putExtra("ID_PACIENT", pacient.getId())
                    );
                }
            }
        });
    }

    public String getTime() {
        return txtTime.getText().toString();
    }
    public void setTime(String time) {
        txtTime.setText(time);
    }

    public void cancelarProgress() {
        progressDialog.dismiss();
    }

    /**
     * Actualiza en la interfaz de usuario el tiempo cronometrado
     *
     * @param tiempo
     */
    public void actualizarCronometro(double tiempo) {
        txtTime.setText(String.format("%.2f", tiempo) + "s");
    }
}
