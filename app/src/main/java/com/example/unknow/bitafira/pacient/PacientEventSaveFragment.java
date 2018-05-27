package com.example.unknow.bitafira.pacient;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.unknow.bitafira.R;
import com.example.unknow.bitafira.model.EvaluationActive;
import com.example.unknow.bitafira.model.Event;
import com.example.unknow.bitafira.model.Pacient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PacientEventSaveFragment extends Fragment {

    FirebaseDatabase mDatabase;
    DatabaseReference mEvents;
    FragmentTransaction transact;
    Pacient pacient;
    Button save, cancelar;
    EditText txtEvent;
    EvaluationActive active;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pacient_event_save, parent, false);
    }

    // This event is triggered soon after onCreateView().
    // onViewCreated() is only called if the view returned from onCreateView() is non-null.
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        txtEvent = (EditText) view.findViewById(R.id.input_event);
        save = (Button) view.findViewById(R.id.btn_save_event);
        cancelar = (Button) view.findViewById(R.id.btn_cancelar_event);
        save.setOnClickListener(saveEvent);
        pacient = (Pacient) getArguments().getSerializable("PACIENT");
        active = (EvaluationActive) getArguments().getSerializable("EVALUATION");
        mDatabase = FirebaseDatabase.getInstance();
        transact = this.getFragmentManager().beginTransaction();//apuntamos con que nodo vamos a trabajar
        mEvents = mDatabase.getReference("events").child(pacient.getId());
        ;
    }

    private View.OnClickListener saveEvent = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Calendar calendar = Calendar.getInstance();
            Date fechaActual = calendar.getTime();
            DateFormat dateFormat = dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String hour = calendar.get(Calendar.HOUR) + ":" + calendar.get(Calendar.MINUTE) +
                    ":" + calendar.get(Calendar.SECOND);
            Event event = new Event();
            String id = mEvents.push().getKey();
            event.setDate(dateFormat.format(fechaActual));
            event.setHour(hour);
            event.setEvento(txtEvent.getText().toString());
            event.setIdEvaluation(active.getIdEvaluation());
            mEvents.child(id).setValue(event).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        android.support.v4.app.Fragment mFrag = new PacientEventFragment();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("PACIENT", pacient);
                        mFrag.setArguments(bundle);
                        transact.replace(R.id.main_fragment, mFrag);
                        transact.commit();
                    }
                }
            });
        }
    };
}
