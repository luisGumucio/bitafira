package com.example.unknow.bitafira.doctor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.unknow.bitafira.R;
import com.example.unknow.bitafira.model.Evaluation;
import com.example.unknow.bitafira.model.Pacient;
import com.example.unknow.bitafira.pacient.EvaluationAdapter;
import com.example.unknow.bitafira.pacient.PacientEventFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DoctorHistorialFragment extends Fragment {

    ListView listEvaluations;
    EvaluationAdapter evaluationAdapter;
    List<Evaluation> evaluations;
    ValueEventListener mValueListeneroContactos;
    FirebaseDatabase mDatabase;
    DatabaseReference mEvaluation;
    FragmentTransaction transact;
    Pacient pacient;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_evaluation_doctor_main, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        transact = this.getFragmentManager().beginTransaction();//apuntamos con que nodo vamos a trabajar
        listEvaluations = (ListView) view.findViewById(R.id.lvHistory);
        listEvaluations.setOnItemClickListener(itemClickListenerAdmin);
        evaluations = new ArrayList<Evaluation>();
        evaluationAdapter = new EvaluationAdapter(view.getContext().getApplicationContext(), evaluations);
        listEvaluations.setAdapter(evaluationAdapter);
        pacient = (Pacient) getArguments().getSerializable("PACIENT");
        mDatabase = FirebaseDatabase.getInstance();
        mEvaluation = mDatabase.getReference("evaluations").child(pacient.getId());
        loadDataAdmin();
    }

    private void loadDataAdmin() {
        mValueListeneroContactos = mEvaluation.orderByChild("dateStart").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    evaluations.clear();
                    for (DataSnapshot snapshotContacto : dataSnapshot.getChildren()) {
                        Evaluation evaluation = snapshotContacto.getValue(Evaluation.class);
                        evaluations.add(evaluation);
                    }
                    evaluationAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("ERROR", databaseError.toString());
            }
        });
    }

    private AdapterView.OnItemClickListener itemClickListenerAdmin = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Evaluation evaluation = evaluations.get(position);
            Fragment mFrag = new DoctorEventsFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable("PACIENT", pacient);
            bundle.putSerializable("EVALUATION", evaluation);
            mFrag.setArguments(bundle);
            transact.replace(R.id.main_fragment, mFrag).addToBackStack(null);
            transact.commit();


        }
    };
}
