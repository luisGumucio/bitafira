package com.example.unknow.bitafira.pacient;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.example.unknow.bitafira.R;
import com.example.unknow.bitafira.model.Evaluation;
import com.example.unknow.bitafira.model.Pacient;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.github.clans.fab.FloatingActionMenu;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class PacientInfoFragment extends Fragment {

    FloatingActionMenu materialDesignFAM;
    FloatingActionButton floatingActionButton1, floatingActionButton2, floatingActionButton3;
    private Pacient pacient;
    private TextView  phoneRefe, full, address, phone, email, rol, historial ;
    DatabaseReference dbEvaluation;
    FragmentTransaction t;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pacient_info, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        t = this.getFragmentManager().beginTransaction();
        materialDesignFAM = (FloatingActionMenu) view.findViewById(R.id.material_design_android_floating_action_menu);

        floatingActionButton2 = (FloatingActionButton)  view.findViewById(R.id.material_design_floating_action_menu_item2);
        floatingActionButton3 = (FloatingActionButton)  view.findViewById(R.id.material_design_floating_action_menu_item3);

        floatingActionButton2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //TODO something when floating action menu second item clicked
                Fragment mFrag = new PacientHistoryFragment();
                Bundle bundle=new Bundle();
                bundle.putSerializable("PACIENT", pacient);
                mFrag.setArguments(bundle);
                t.replace(R.id.main_fragment, mFrag).addToBackStack(null);
                t.commit();
            }
        });
        floatingActionButton3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //TODO something when floating action menu third item clicked
                goToEvaluation();
            }
        });
        full = (TextView)  view.findViewById(R.id.txtNameP);
        address = (TextView)  view.findViewById(R.id.txtAddress);
        phone = (TextView)  view.findViewById(R.id.txtPhone);
        phoneRefe = (TextView) view.findViewById(R.id.txtPhoneRefe);
        email = (TextView) view.findViewById(R.id.txtEmail);
        rol = (TextView)view.findViewById(R.id.txtRole);
        historial = (TextView) view.findViewById(R.id.txtNroClinico);
        init();
    }

    private void goToEvaluation() {
                Bundle bundle=new Bundle();
                bundle.putSerializable("PACIENT", pacient);
                Fragment mFrag = new PacientBitalinoFragment();
                mFrag.setArguments(bundle);
                t.replace(R.id.main_fragment, mFrag).addToBackStack(null);
                t.commit();
    }

    private void init() {
        if(getArguments() !=null){
            pacient = (Pacient) getArguments().getSerializable("PACIENT");
        }
        full.setText(pacient.getName() + " "+pacient.getLastName());
        address.setText(pacient.getAddress());
        phone.setText(String.valueOf(pacient.getPhone()));
        rol.setText(pacient.getRole());
        historial.setText(String.valueOf(pacient.getNumberHistory()));
        email.setText(pacient.getEmail());
        phoneRefe.setText(String.valueOf(pacient.getPhoneRefe()));
    }
}
