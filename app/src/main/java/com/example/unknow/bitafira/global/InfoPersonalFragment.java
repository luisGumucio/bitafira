package com.example.unknow.bitafira.global;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.unknow.bitafira.R;
import com.example.unknow.bitafira.model.Pacient;

public class InfoPersonalFragment extends Fragment {

    private Pacient pacient;
    private TextView full, email, rol, addreess, age, nacimiento, phone;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_info_personal, parent, false);
    }

    // This event is triggered soon after onCreateView().
    // onViewCreated() is only called if the view returned from onCreateView() is non-null.
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        full = (TextView) view.findViewById(R.id.txt_name_full_info);
        email = (TextView) view.findViewById(R.id.txt_email_info);
        rol = (TextView) view.findViewById(R.id.txt_role_info);
        age = (TextView) view.findViewById(R.id.txt_age_info);
        nacimiento = (TextView) view.findViewById(R.id.txt_date_info);
        phone = (TextView) view.findViewById(R.id.txt_phone_info);
        addreess = (TextView) view.findViewById(R.id.txt_address_info);
        init();
    }

    private void init() {
        if(getArguments() !=null){
            pacient = (Pacient) getArguments().getSerializable("PACIENT");
        }
        full.setText(pacient.getName() + " "+pacient.getLastName());
        addreess.setText(pacient.getAddress());
        phone.setText(String.valueOf(pacient.getPhone()));
        rol.setText(pacient.getRole());
        nacimiento.setText(String.valueOf(pacient.getNumberHistory()));
        email.setText(pacient.getEmail());
        age.setText(String.valueOf(pacient.getAge()));
        nacimiento.setText(String.valueOf(pacient.getNacimiento()));
    }
}
