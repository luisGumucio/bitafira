package com.example.unknow.bitafira;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.unknow.bitafira.doctor.DoctorFragmentView;
import com.example.unknow.bitafira.global.ConfigurationFragment;
import com.example.unknow.bitafira.global.Constante;
import com.example.unknow.bitafira.global.InfoPersonalFragment;
import com.example.unknow.bitafira.model.Pacient;
import com.example.unknow.bitafira.pacient.PacientBitalinoFragment;
import com.example.unknow.bitafira.pacient.PacientDoctorSaveFragment;
import com.example.unknow.bitafira.pacient.PacientEventFragment;
import com.example.unknow.bitafira.pacient.PacientInfoFragment;
import com.example.unknow.bitafira.pacient.PacientMainDoctorFragment;
import com.example.unknow.bitafira.pacient.PacientMainFragment;
import com.example.unknow.bitafira.utils.BackButtonHandlerInterface;
import com.example.unknow.bitafira.utils.OnBackClickListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int MENU_ADMIN_CONFIGURATION = 2;
    private static final int MENU_ADMIN_SHOW_EVALUACION = 3;
    private static final int MENU_ADMIN_CERRAR_SESSION = 4;
    private static final int MENU_ADMIN_SHOW_PACIENT = 5;
    private ArrayList<WeakReference<OnBackClickListener>> backClickListenersList = new ArrayList<>();
    NavigationView navigationView;
    FirebaseDatabase mDatabase;
    DatabaseReference mPacients;
    Pacient pacient;
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        init();
    }

    private void init() {
         mAuth = FirebaseAuth.getInstance();
         if(mAuth != null) {

         }
        mDatabase = FirebaseDatabase.getInstance();
        mPacients = mDatabase.getReference(Constante.contacto);
        Query query = mPacients.orderByChild("email").equalTo(mAuth.getCurrentUser().getEmail());

        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Cargando espere por favor...");
        progressDialog.show();
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    pacient = data.getValue(Pacient.class);
                    if (pacient.getRole().equals("Técnico")) {
                        loadMenuAdmin();
                        initAdmin();
                        progressDialog.dismiss();
                    } else  if( pacient.getRole().equals("Doctor")) {
                        loadMenuPacient();
                        initAdmin();
                        progressDialog.dismiss();
                    } else {
                        loadMenuPacient();
                        initPacient();
                        progressDialog.dismiss();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });
    }

    private void loadMenuPacient() {
        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);

        Menu menu = navView.getMenu();
//        menu.add(Menu.NONE, MENU_ADMIN_CONFIGURATION, Menu.NONE, "Configuración Bitalino").setIcon(R.drawable.tools);
//        menu.add(Menu.NONE, MENU_ADMIN_SHOW_EVALUACION, Menu.NONE, "Mostrar evaluación").setIcon(R.drawable.evaluation);
        menu.add(Menu.NONE, MENU_ADMIN_CERRAR_SESSION, Menu.NONE, "Cerrar sesión").setIcon(R.drawable.exit);
        navView.invalidate();
    }

    private void loadMenuAdmin() {
        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);

        Menu menu = navView.getMenu();
        menu.add(Menu.NONE, MENU_ADMIN_CONFIGURATION, Menu.NONE, "Configuración Bitalino").setIcon(R.drawable.tools);
        menu.add(Menu.NONE, MENU_ADMIN_SHOW_PACIENT, Menu.NONE, "Mostrar Pacientes").setIcon(R.drawable.tools);
        menu.add(Menu.NONE, MENU_ADMIN_SHOW_EVALUACION, Menu.NONE, "Mostrar Técnicos y Doctores").setIcon(R.drawable.evaluation);
        menu.add(Menu.NONE, MENU_ADMIN_CERRAR_SESSION, Menu.NONE, "Cerrar sesión").setIcon(R.drawable.exit);
        navView.invalidate();
    }

    private void initPacient() {
        TextView user = (TextView) navigationView.findViewById(R.id.txtUser);
        TextView role = (TextView) navigationView.findViewById(R.id.txtRol);
        user.setText(pacient.getName() + pacient.getLastName());
        role.setText(pacient.getRole());

        Bundle bundle=new Bundle();
        bundle.putSerializable("PACIENT", pacient);
        Fragment mFrag = new PacientEventFragment();
        mFrag.setArguments(bundle);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        ft.replace(R.id.main_fragment, mFrag);
        ft.commit();
    }

    private void initAdmin() {
        TextView user = (TextView) navigationView.findViewById(R.id.txtUser);
        TextView role = (TextView) navigationView.findViewById(R.id.txtRol);
        user.setText(pacient.getName() + pacient.getLastName());
        role.setText(pacient.getRole());

        Bundle bundle=new Bundle();
        bundle.putSerializable("PACIENT", pacient);
        Fragment mFrag = new PacientMainFragment();
        mFrag.setArguments(bundle);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        ft.replace(R.id.main_fragment, mFrag);
        ft.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_info:
                Bundle bundle=new Bundle();
                bundle.putSerializable("PACIENT", pacient);
                Fragment mFrag = new InfoPersonalFragment();
                mFrag.setArguments(bundle);
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.main_fragment, mFrag);
                ft.commit();
                break;
            case MENU_ADMIN_CONFIGURATION:
                Fragment mFrag1 = new ConfigurationFragment();
                FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                ft1.replace(R.id.main_fragment, mFrag1);
                ft1.commit();
                break;
            case MENU_ADMIN_SHOW_EVALUACION:
                Fragment mFrag12 = new PacientMainDoctorFragment();
                Bundle bundle1=new Bundle();
                bundle1.putSerializable("PACIENT", pacient);
                mFrag12.setArguments(bundle1);
                FragmentTransaction ft12 = getSupportFragmentManager().beginTransaction();
                ft12.replace(R.id.main_fragment, mFrag12);
                ft12.commit();
                break;
            case MENU_ADMIN_SHOW_PACIENT:
                Fragment mfrag3 = new PacientMainFragment();
                Bundle bundle12=new Bundle();
                bundle12.putSerializable("PACIENT", pacient);
                mfrag3.setArguments(bundle12);
                FragmentTransaction ft123 = getSupportFragmentManager().beginTransaction();
                ft123.replace(R.id.main_fragment, mfrag3);
                ft123.commit();
                break;
            case MENU_ADMIN_CERRAR_SESSION:
                mAuth.signOut();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
