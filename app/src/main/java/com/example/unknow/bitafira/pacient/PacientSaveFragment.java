package com.example.unknow.bitafira.pacient;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.example.unknow.bitafira.R;
import com.example.unknow.bitafira.global.Constante;
import com.example.unknow.bitafira.model.Pacient;
import com.example.unknow.bitafira.utils.BackButtonHandlerInterface;
import com.example.unknow.bitafira.utils.DatePickerFragment;
import com.example.unknow.bitafira.utils.OnBackClickListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PacientSaveFragment extends Fragment {
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRefPacient;
    private List<Pacient> pacientList;
    EditText txtName;
    EditText txtLastName;
    EditText txtNroHistory;
    EditText txtAge;
    EditText txtWeight;
    EditText txtHeight;
    EditText txtEmail;
    EditText txtAddres;
    EditText txtPhone;
    EditText txtNacimiento;
    EditText txtPassword;
    EditText txtRoles;
    EditText txtPhoneRefe;
    Button btnAgregar;
    FragmentTransaction t;
    FirebaseAuth mAuth;
    private BackButtonHandlerInterface backButtonHandler;
    CharSequence roles[] = new CharSequence[] {"Paciente", "Doctor", "Técnico"};
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pacient_save, parent, false);
    }

    // This event is triggered soon after onCreateView().
    // onViewCreated() is only called if the view returned from onCreateView() is non-null.
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        txtName = (EditText) view.findViewById(R.id.input_name);
        txtLastName = (EditText) view.findViewById(R.id.input_last_name);
        txtNroHistory = (EditText) view.findViewById(R.id.input_number_history);
        txtAge = (EditText) view.findViewById(R.id.input_age);
        txtWeight = (EditText) view.findViewById(R.id.input_weight);
        txtHeight = (EditText) view.findViewById(R.id.input_height);
        txtEmail = (EditText) view.findViewById(R.id.input_email);
        txtAddres = (EditText) view.findViewById(R.id.input_address);
        txtPhone = (EditText) view.findViewById(R.id.input_phone);
        txtPhoneRefe = (EditText) view.findViewById(R.id.input_phone1);
        txtPassword = (EditText) view.findViewById(R.id.input_password);
        txtRoles = (EditText) view.findViewById(R.id.input_roles);
        txtRoles.setOnClickListener(putRoles);
        txtNacimiento = (EditText) view.findViewById(R.id.input_nacimiento);
        txtNacimiento.setOnClickListener(dataNcimiento);
        btnAgregar = (Button) view.findViewById(R.id.btn_save);
        btnAgregar.setOnClickListener(agregarClickListener);
        //crear las instancias de Database de Firebase para poder consumir su servicio.
        mDatabase = FirebaseDatabase.getInstance();//Instanciamos el servicios
        mRefPacient = mDatabase.getReference(Constante.contacto);
        t = this.getFragmentManager().beginTransaction();//apuntamos con que nodo vamos a trabajar
        mAuth = FirebaseAuth.getInstance();
    }


    private View.OnClickListener putRoles = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Seleccione un rol por favor!");
            builder.setItems(roles, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // the user clicked on colors[which]
                    txtRoles.setText(roles[which]);
                }
            });
            builder.show();
        }
    };

    private View.OnClickListener dataNcimiento = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            DatePickerFragment newFragment = DatePickerFragment.newInstance(new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                    // +1 because january is zero
                    final String selectedDate = year + "-" + (month+1) + "-" + day;
                    txtNacimiento.setText(selectedDate);
                    Calendar cal = Calendar.getInstance();
                    Date fechaActual = cal.getTime();
                    DateFormat dateFormat = dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    try {
                        Date fechaNacimiento = dateFormat.parse(txtNacimiento.getText().toString());
                        txtAge.setText(getEdad(fechaNacimiento, fechaActual));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                }
            });
            newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
        }
    };
    public  String getEdad(Date fechaNacimiento, Date fechaActual) {
        DateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        int dIni = Integer.parseInt(formatter.format(fechaNacimiento));
        int dEnd = Integer.parseInt(formatter.format(fechaActual));
        int age = (dEnd-dIni)/10000;
        return String.valueOf(age);
    }
    private View.OnClickListener agregarClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final Pacient pacient = savePacient();
            mAuth.createUserWithEmailAndPassword(pacient.getEmail(), txtPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()) {
                        mRefPacient.child(pacient.getId()).setValue(pacient);
                        android.support.v4.app.Fragment mFrag = new PacientMainFragment();
                        t.replace(R.id.main_fragment, mFrag);
                        t.commit();
                    } else {
                        Toast.makeText(getContext().getApplicationContext(), "Fallo al registrar", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    };

    private Pacient savePacient() {
        Pacient pacient = new Pacient();
        String id = mRefPacient.push().getKey();
        pacient.setId(id);
        pacient.setName(txtName.getText().toString());
        pacient.setLastName(txtLastName.getText().toString());
        pacient.setNumberHistory(Integer.parseInt(txtNroHistory.getText().toString()));
        pacient.setAge(Integer.parseInt(txtAge.getText().toString()));
        pacient.setWeight(Double.parseDouble(txtWeight.getText().toString()));
        pacient.setHeight(Double.parseDouble(txtHeight.getText().toString()));
        pacient.setEmail(txtEmail.getText().toString());
        pacient.setAddress(txtAddres.getText().toString());
        pacient.setPhone(Integer.parseInt(txtPhone.getText().toString()));
        pacient.setUserId(id);
        pacient.setRole(txtRoles.getText().toString());
        pacient.setPhoneRefe(Integer.parseInt(txtPhoneRefe.getText().toString()));

        return pacient;
    }
}
