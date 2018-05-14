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
import com.example.unknow.bitafira.global.Constante;
import com.example.unknow.bitafira.model.Evaluation;
import com.example.unknow.bitafira.model.Pacient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PacientHistoryFragment extends Fragment {

    ListView listEvaluations;
    EvaluationAdapter evaluationAdapter;
    List<Evaluation> evaluations;
    ValueEventListener mValueListeneroContactos;
    FirebaseDatabase mDatabase;
    DatabaseReference mEvaluation;
    FragmentTransaction t;
    Pacient pacient;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_evaluation_main, parent, false);
    }

    // This event is triggered soon after onCreateView().
    // onViewCreated() is only called if the view returned from onCreateView() is non-null.
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listEvaluations = (ListView) view.findViewById(R.id.lvHistory);
        listEvaluations.setOnItemClickListener(itemClickListenerAdmin);
        //init lista contactos
        evaluations = new ArrayList<Evaluation>();
        // Inicializar el adaptador con la fuente de datos.
        evaluationAdapter = new EvaluationAdapter(view.getContext().getApplicationContext(), evaluations);
        //Relacionando la lista con el adaptador
        listEvaluations.setAdapter(evaluationAdapter);
        pacient = new Pacient();
        pacient.setId(getArguments().getString("PACIENT_ID"));
        mDatabase = FirebaseDatabase.getInstance();
        mEvaluation = mDatabase.getReference("evaluations").child(pacient.getId());;
        loadDataAdmin();
    }
    private void loadDataAdmin() {
        mValueListeneroContactos = mEvaluation.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) { //si existen datos, hay que obtener la información
                    evaluations.clear();//limpiamos la data
                    for (DataSnapshot snapshotContacto : dataSnapshot.getChildren()) {
                        Evaluation evaluation = snapshotContacto.getValue(Evaluation.class);
                        //le decimos a Firebase que el q objeto en JSON lo parse a un objecto Contacto
                        //Log.i("INFO","pacient key "+ snapshotContacto.getKey());
                        evaluations.add(evaluation);

                    }
                    evaluationAdapter.notifyDataSetChanged();
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
