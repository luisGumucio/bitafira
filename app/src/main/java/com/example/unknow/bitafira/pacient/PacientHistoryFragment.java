package com.example.unknow.bitafira.pacient;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
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
import com.example.unknow.bitafira.global.Constante;
import com.example.unknow.bitafira.model.Evaluation;
import com.example.unknow.bitafira.model.Pacient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
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

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
        mValueListeneroContactos = mEvaluation.addValueEventListener(new ValueEventListener() {
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

            File sdPath = Environment.getExternalStorageDirectory();
            String path = sdPath.getParent() + "/0/Bitafira/" + pacient.getId() + "/" + evaluations.get(position).getId() + ".txt";
            File f = new File(path);

            Intent intentShareFile = new Intent(Intent.ACTION_SEND_MULTIPLE);
            File fileWithinMyDir = new File(path);

            if (fileWithinMyDir.exists()) {
                intentShareFile.setType("text/plain");
                intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(fileWithinMyDir));
                intentShareFile.putExtra(Intent.EXTRA_SUBJECT, "MyApp File Share: " + f.getName());
                intentShareFile.setPackage("com.whatsapp");
                getActivity().startActivity(Intent.createChooser(intentShareFile, f.getName()));
            } else {
                Toast.makeText(getContext(), "El archivo no existe en tu dispositivo", Toast.LENGTH_LONG).show();
            }
        }
    };
}
