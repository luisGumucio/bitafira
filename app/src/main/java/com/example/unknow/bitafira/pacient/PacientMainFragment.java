package com.example.unknow.bitafira.pacient;

import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.os.Bundle;

import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.unknow.bitafira.R;
import com.example.unknow.bitafira.global.Constante;
import com.example.unknow.bitafira.model.Pacient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class PacientMainFragment extends Fragment {

    Pacient pacient;
    ListView listPacients;
    FloatingActionButton btnAgregar;

    PacientAdapter pacientAdapter;
    List<Pacient> pacients;
    ValueEventListener mValueListeneroContactos;
    FirebaseDatabase mDatabase;
    DatabaseReference mPacients;
    FragmentTransaction t;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listPacients = (ListView) view.findViewById(R.id.lvListaContactos);
        btnAgregar = (FloatingActionButton) view.findViewById(R.id.btnAgregar);
        //init lista contactos
        pacients = new ArrayList<Pacient>();
        // Inicializar el adaptador con la fuente de datos.
        pacientAdapter = new PacientAdapter(view.getContext().getApplicationContext(), pacients);
        //Relacionando la lista con el adaptador
        listPacients.setAdapter(pacientAdapter);
        mDatabase = FirebaseDatabase.getInstance();
        mPacients = mDatabase.getReference(Constante.contacto);
        t = this.getFragmentManager().beginTransaction();
        btnAgregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment mFrag = new PacientSaveFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("PACIENT", pacient);
                mFrag.setArguments(bundle);
                t.replace(R.id.main_fragment, mFrag).addToBackStack(null);
                t.commit();
            }
        });
        loadDataAdmin();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pacient_main, container, false);
    }

    private void loadDataAdmin() {
        pacient = (Pacient) getArguments().getSerializable("PACIENT");
        if(pacient.getRole().equals("Doctor")) {
            btnAgregar.setVisibility(View.INVISIBLE);
            listPacients.setOnItemClickListener(itemClickListenerAdminDoctor);
        } else {
            listPacients.setOnItemClickListener(itemClickListenerAdmin);
        }

        mValueListeneroContactos = mPacients.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) { //si existen datos, hay que obtener la información
                    pacients.clear();//limpiamos la data
                    for (DataSnapshot snapshotContacto : dataSnapshot.getChildren()) {
                        Pacient pacient1 = snapshotContacto.getValue(Pacient.class);
                        //le decimos a Firebase que el q objeto en JSON lo parse a un objecto Contacto
                        //Log.i("INFO","pacient key "+ snapshotContacto.getKey());
                        if(pacient1.getRole().equals("Paciente")) {
                            pacients.add(pacient1);
                        }
                    }
                    pacientAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Error de tipo conexión, errores no existe, etc.
                Log.e("ERROR", databaseError.toString());
            }
        });
    }

    private AdapterView.OnItemClickListener itemClickListenerAdmin = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//            Intent intent = new Intent(MainActivity.this, PacientInfoActivity.class);
            Bundle bundle=new Bundle();
            Pacient pacient2 = pacients.get(position);
            bundle.putString("PACIENT_ID", pacient2.getId());
            bundle.putInt("EXTRA_POSITION", position);
            bundle.putString("PACIENT_NAME", pacient2.getName() );
            bundle.putString("PACIENT_LASTNAME", pacient2.getLastName() );
            bundle.putInt("PACIENT_EDAD", pacient2.getAge() );
            bundle.putInt("PACIENT_PHONE", pacient2.getPhone());
            bundle.putInt("PACIENT_PHONE_REFE", pacient2.getPhoneRefe());
            bundle.putString("PACIENT_EMAIL", pacient2.getEmail() );
            bundle.putString("PACIENT_DIRECCION", pacient2.getAddress() );
            bundle.putString("PACIENT_ROL", pacient2.getRole());
            bundle.putInt("PACIENT_HISTORIAL", pacient2.getNumberHistory());
            Fragment mFrag = new PacientInfoFragment();
            mFrag.setArguments(bundle);
            t.replace(R.id.main_fragment, mFrag).addToBackStack(null);
            t.commit();
        }
    };

    private AdapterView.OnItemClickListener itemClickListenerAdminDoctor = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Bundle bundle=new Bundle();
            Pacient pacient2 = pacients.get(position);
            bundle.putString("PACIENT_ID", pacient2.getId());
            bundle.putInt("EXTRA_POSITION", position);
            bundle.putString("PACIENT_NAME", pacient2.getName() );
            bundle.putString("PACIENT_LASTNAME", pacient2.getLastName() );
            bundle.putInt("PACIENT_EDAD", pacient2.getAge() );
            bundle.putInt("PACIENT_PHONE", pacient2.getPhone());
            bundle.putInt("PACIENT_PHONE_REFE", pacient2.getPhoneRefe());
            bundle.putString("PACIENT_EMAIL", pacient2.getEmail() );
            bundle.putString("PACIENT_DIRECCION", pacient2.getAddress() );
            bundle.putString("PACIENT_ROL", pacient2.getRole());
            bundle.putInt("PACIENT_HISTORIAL", pacient2.getNumberHistory());
            Fragment mFrag = new PacientEventFragment();
            mFrag.setArguments(bundle);
            t.replace(R.id.main_fragment, mFrag).addToBackStack(null);
            t.commit();
        }
    };
}
