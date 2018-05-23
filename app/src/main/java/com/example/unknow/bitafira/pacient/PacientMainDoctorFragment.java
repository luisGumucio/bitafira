package com.example.unknow.bitafira.pacient;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.unknow.bitafira.R;
import com.example.unknow.bitafira.global.Constante;
import com.example.unknow.bitafira.model.Pacient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class PacientMainDoctorFragment extends Fragment {

    private static String POPUP_CONSTANT = "mPopup";
    private static String POPUP_FORCE_SHOW_ICON = "setForceShowIcon";
    Pacient pacient;
    ListView listPacients;
    FloatingActionButton btnAgregar;

    PacientAdapter pacientAdapter;
    List<Pacient> pacients;
    ValueEventListener mValueListeneroContactos;
    FirebaseDatabase mDatabase;
    DatabaseReference mPacients;
    FragmentTransaction t;
    int itemPosition;
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
        listPacients.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        mDatabase = FirebaseDatabase.getInstance();
        mPacients = mDatabase.getReference(Constante.contacto);
        t = this.getFragmentManager().beginTransaction();
        btnAgregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment mFrag = new PacientDoctorSaveFragment();
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
        return inflater.inflate(R.layout.fragment_pacient_doctor_main, container, false);
    }

    private void loadDataAdmin() {
        pacient = (Pacient) getArguments().getSerializable("PACIENT");
        if(pacient.getRole().equals("Doctor")) {
            btnAgregar.setVisibility(View.INVISIBLE);
        } else {
            listPacients.setOnItemClickListener(itemClickListenerAdmin);
            listPacients.setOnItemLongClickListener(itemClickLongListener);

        }

        mValueListeneroContactos = mPacients.orderByChild("lastName").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    pacients.clear();
                    for (DataSnapshot snapshotContacto : dataSnapshot.getChildren()) {
                        Pacient pacient1 = snapshotContacto.getValue(Pacient.class);
                        if(!pacient1.getRole().equals("Paciente")) {
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
            Bundle bundle=new Bundle();
            bundle.putSerializable("PACIENT", pacients.get(position));
            Fragment mFrag = new PacientInfoDoctorFragment();
            mFrag.setArguments(bundle);
            t.replace(R.id.main_fragment, mFrag).addToBackStack(null);
            t.commit();
        }
    };


    private AdapterView.OnItemLongClickListener itemClickLongListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view,
                                       int position, long id) {
            // TODO Auto-generated method stub
            itemPosition = position;
            showPopup(view);
            return true;
        }
    };

    public void showPopup(View view) {
        PopupMenu popup = new PopupMenu(getContext(), view);
        try {
            // Reflection apis to enforce show icon
            Field[] fields = popup.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.getName().equals(POPUP_CONSTANT)) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popup);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod(POPUP_FORCE_SHOW_ICON, boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Pacient pacient2 = pacientAdapter.getItem(itemPosition);
                switch (item.getItemId()) {
                    case R.id.pmnuDelete:
                        mPacients.child(pacient2.getId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()) {
                                    Toast.makeText(getContext(), "Eliminado con exito", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(getContext(), "Fallo al eliminar intente de nuevo por favor.", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                        break;
                    case R.id.pmnuEdit:
                        Toast.makeText(getContext(), "Falta implementar", Toast.LENGTH_LONG).show();
                        break;
                }

                return false;
            }
        });
        popup.show();
    }
}
