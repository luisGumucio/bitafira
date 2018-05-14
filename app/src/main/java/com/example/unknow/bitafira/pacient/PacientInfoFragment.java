package com.example.unknow.bitafira.pacient;


import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
//import com.github.clans.fab.FloatingActionButton;
//import com.github.clans.fab.FloatingActionMenu;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class PacientInfoFragment extends Fragment {

//    FloatingActionMenu materialDesignFAM;
    FloatingActionButton floatingActionButton1, floatingActionButton2, floatingActionButton3;
    private Pacient pacient;
    private TextView  phoneRefe, full, address, phone, email, rol, historial ;
    DatabaseReference dbEvaluation;
    FragmentTransaction t;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pacient_info, parent, false);
    }

    // This event is triggered soon after onCreateView().
    // onViewCreated() is only called if the view returned from onCreateView() is non-null.
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        t = this.getFragmentManager().beginTransaction();
//        materialDesignFAM = (FloatingActionMenu) view.findViewById(R.id.material_design_android_floating_action_menu);
        floatingActionButton1 = (FloatingActionButton)  view.findViewById(R.id.material_design_floating_action_menu_item1);
        floatingActionButton2 = (FloatingActionButton)  view.findViewById(R.id.material_design_floating_action_menu_item2);
        floatingActionButton3 = (FloatingActionButton)  view.findViewById(R.id.material_design_floating_action_menu_item3);

        floatingActionButton1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //TODO something when floating action menu first item clicked

            }
        });
        floatingActionButton2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //TODO something when floating action menu second item clicked
                Fragment mFrag = new PacientHistoryFragment();
                Bundle bundle=new Bundle();
                bundle.putString("PACIENT_ID", pacient.getId());
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
                bundle.putString("PACIENT_ID", pacient.getId());
                bundle.putString("PACIENT_NAME", pacient.getName() );
                bundle.putString("PACIENT_LASTNAME", pacient.getLastName() );
                bundle.putInt("PACIENT_EDAD", pacient.getAge() );
                bundle.putInt("PACIENT_PHONE", pacient.getPhone());
                bundle.putInt("PACIENT_PHONE_REFE", pacient.getPhoneRefe());
                bundle.putString("PACIENT_EMAIL", pacient.getEmail() );
                bundle.putString("PACIENT_DIRECCION", pacient.getAddress() );
                bundle.putString("PACIENT_ROL", pacient.getRole());
                bundle.putInt("PACIENT_HISTORIAL", pacient.getNumberHistory());
                Fragment mFrag = new PacientEvaluationFragment();
                mFrag.setArguments(bundle);
                t.replace(R.id.main_fragment, mFrag).addToBackStack(null);
                t.commit();
    }

    private void init() {
        if(getArguments() !=null){
            pacient = new Pacient();
            pacient.setId(getArguments().getString("PACIENT_ID"));
            pacient.setName(getArguments().getString("PACIENT_NAME"));
            pacient.setLastName(getArguments().getString("PACIENT_LASTNAME"));
            pacient.setPhone(getArguments().getInt("PACIENT_PHONE", 0));
            pacient.setAddress(getArguments().getString("PACIENT_DIRECCION"));
            pacient.setRole(getArguments().getString("PACIENT_ROL"));
            pacient.setPhoneRefe(getArguments().getInt("PACIENT_PHONE_REFE", 0));
            pacient.setEmail(getArguments().getString("PACIENT_EMAIL"));
            pacient.setNumberHistory(getArguments().getInt("PACIENT_HISTORIAL",0));
        }
        full.setText(pacient.getName() + " "+pacient.getLastName());
        address.setText(pacient.getAddress());
        phone.setText(String.valueOf(pacient.getPhone()));
        rol.setText(pacient.getRole());
        historial.setText(String.valueOf(pacient.getNumberHistory()));
        email.setText(pacient.getEmail());
        phoneRefe.setText(String.valueOf(pacient.getPhoneRefe()));
        //dbEvaluation = FirebaseDatabase.getInstance().getReference("evaluations").child(pacient.getId());
    }
}
