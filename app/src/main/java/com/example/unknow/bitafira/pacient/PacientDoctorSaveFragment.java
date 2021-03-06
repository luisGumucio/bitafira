package com.example.unknow.bitafira.pacient;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PacientDoctorSaveFragment extends Fragment {
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRefPacient;
    private List<Pacient> pacientList;
    EditText txtName;
    EditText txtLastName;
    EditText txtAge;
    EditText txtEmail;
    EditText txtAddres;
    EditText txtPhone;
    EditText txtNacimiento;
    EditText txtPassword;
    EditText txtRoles;
    EditText txtPhoneRefe, txtSexo;
    Button btnAgregar;
    FragmentTransaction t;
    FirebaseAuth mAuth;
    Pacient pacient;
    private BackButtonHandlerInterface backButtonHandler;
    CharSequence roles[] = new CharSequence[] {"Doctor", "Técnico"};
    CharSequence sexo[] = new CharSequence[] {"Masculino", "Femenino"};
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pacient_doctor_save, parent, false);
    }

    // This event is triggered soon after onCreateView().
    // onViewCreated() is only called if the view returned from onCreateView() is non-null.
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        txtName = (EditText) view.findViewById(R.id.input_name);
        txtLastName = (EditText) view.findViewById(R.id.input_last_name);
        txtAge = (EditText) view.findViewById(R.id.input_age);
        txtEmail = (EditText) view.findViewById(R.id.input_email);
        txtAddres = (EditText) view.findViewById(R.id.input_address);
        txtPhone = (EditText) view.findViewById(R.id.input_phone);
        txtPhoneRefe = (EditText) view.findViewById(R.id.input_phone1);
        txtPassword = (EditText) view.findViewById(R.id.input_password);
        txtSexo = (EditText) view.findViewById(R.id.input_sexo);
        txtSexo.setOnClickListener(putSexo);
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
        pacient = (Pacient) getArguments().getSerializable("PACIENT");
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

    private View.OnClickListener putSexo = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Seleccione un sexo por favor!");
            builder.setItems(sexo, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // the user clicked on colors[which]
                    txtSexo.setText(sexo[which]);
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
            final ProgressDialog progressDialog = new ProgressDialog(getContext(),
                    R.style.AppTheme_Dark_Dialog);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Guardando espere por favor...");
            progressDialog.show();
            final Pacient pacient = savePacient();
            mAuth.createUserWithEmailAndPassword(pacient.getEmail(), txtPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()) {
                        mRefPacient.child(pacient.getId()).setValue(pacient);
                        progressDialog.dismiss();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("PACIENT", pacient);
                        android.support.v4.app.Fragment mFrag = new PacientMainFragment();
                        mFrag.setArguments(bundle);
                        t.replace(R.id.main_fragment, mFrag);
                        t.commit();
                    } else {
                        progressDialog.dismiss();
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
        pacient.setAge(Integer.parseInt(txtAge.getText().toString()));
        pacient.setEmail(txtEmail.getText().toString());
        pacient.setAddress(txtAddres.getText().toString());
        pacient.setPhone(Integer.parseInt(txtPhone.getText().toString()));
        pacient.setUserId(id);
        pacient.setRole(txtRoles.getText().toString());
        pacient.setSexo(txtSexo.getText().toString());
        pacient.setPhoneRefe(Integer.parseInt(txtPhoneRefe.getText().toString()));
        pacient.setNacimiento(txtNacimiento.getText().toString());
        return pacient;
    }
}

