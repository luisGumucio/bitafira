package com.example.unknow.bitafira.pacient;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.unknow.bitafira.R;
import com.example.unknow.bitafira.model.Evaluation;
import com.example.unknow.bitafira.model.Event;
import com.example.unknow.bitafira.model.Pacient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PacientEventFragment extends Fragment {

    ListView listEvents;
    EventAdapter eventAdapter;
    List<Event> events;
    ValueEventListener mValueListeneroContactos;
    FirebaseDatabase mDatabase;
    DatabaseReference mEvents;
    FragmentTransaction t;
    Pacient pacient;

    FloatingActionButton btnAgregar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pacient_event_main, parent, false);
    }

    // This event is triggered soon after onCreateView().
    // onViewCreated() is only called if the view returned from onCreateView() is non-null.
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listEvents = (ListView) view.findViewById(R.id.lvHistory);
        btnAgregar = (FloatingActionButton) view.findViewById(R.id.bt_event_save);
        listEvents.setOnItemClickListener(itemClickListenerAdmin);
        //init lista contactos
        events = new ArrayList<Event>();
        // Inicializar el adaptador con la fuente de datos.
        eventAdapter = new EventAdapter(view.getContext().getApplicationContext(), events);
        //Relacionando la lista con el adaptador
        listEvents.setAdapter(eventAdapter);
        pacient = (Pacient) getArguments().getSerializable("PACIENT");
        mDatabase = FirebaseDatabase.getInstance();
        mEvents = mDatabase.getReference("events").child(pacient.getId());;
        t = this.getFragmentManager().beginTransaction();//apuntamos con que nodo vamos a trabajar
        btnAgregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment mFrag = new PacientEventSaveFragment();
                Bundle bundle=new Bundle();
                bundle.putString("PACIENT_ID", pacient.getId());
                mFrag.setArguments(bundle);
                t.replace(R.id.main_fragment, mFrag).addToBackStack(null);
                t.commit();
            }
        });
        loadDataAdmin();
    }

    private void loadDataAdmin() {
        mValueListeneroContactos = mEvents.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) { //si existen datos, hay que obtener la información
                    events.clear();//limpiamos la data
                    for (DataSnapshot snapshotContacto : dataSnapshot.getChildren()) {
                        Event event = snapshotContacto.getValue(Event.class);
                        //le decimos a Firebase que el q objeto en JSON lo parse a un objecto Contacto
                        //Log.i("INFO","pacient key "+ snapshotContacto.getKey());
                        events.add(event);

                    }
                    eventAdapter.notifyDataSetChanged();
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
        }
    };
}
