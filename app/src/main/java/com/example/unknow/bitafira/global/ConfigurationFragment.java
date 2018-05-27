package com.example.unknow.bitafira.global;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.bitalino.comm.BITalinoException;
import com.example.unknow.bitafira.R;
import com.example.unknow.bitafira.model.Pacient;
import com.example.unknow.bitafira.pacient.PacientMainFragment;
import com.example.unknow.bitafira.utils.BITlog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

public class ConfigurationFragment extends Fragment {
    public Spinner spinnerBluetooth;
    String[] myDeviceList;
    private CountDownTimer countDownTimer;
    ArrayList<String> devices1;
    private Button scan, save;
    ArrayAdapter<String> spinnerArrayAdapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_configuration, parent, false);
    }

    // This event is triggered soon after onCreateView().
    // onViewCreated() is only called if the view returned from onCreateView() is non-null.
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        save = (Button)view.findViewById(R.id.bt_save_bluetooth);
        scan = (Button)view.findViewById(R.id.button_scan);
        scan.setOnClickListener(scanBluetooth);
        save.setOnClickListener(saveBluetooth);
        spinnerBluetooth = (Spinner) view.findViewById(R.id.spinnerBluetooth);
        myDeviceList = this.getBluetoothDevices();


        spinnerArrayAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, myDeviceList);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        spinnerBluetooth.setAdapter(spinnerArrayAdapter);
        spinnerBluetooth.setSelection(getIndex(spinnerBluetooth, "bitalino"));

        spinnerBluetooth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {

                String myMAC = spinnerBluetooth.getSelectedItem().toString();
                myMAC = myMAC.substring(myMAC.length()-17);
                BITlog.MAC = myMAC;

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }

        });
    }
    private static final int REQUEST_ENABLE_BT = 12;
    private String[] getBluetoothDevices(){
        String[] result = null;
        ArrayList<String> devices = new ArrayList<String>();
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()){
            Log.e("Dialog", "Couldn't find enabled the mBluetoothAdapter");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }else{
            Set<BluetoothDevice> devList = mBluetoothAdapter.getBondedDevices();

            for( BluetoothDevice device : devList)
                devices.add(device.getName() + "-"+ device.getAddress());

            String[] aux_items = new String[devices.size()];
            final String[] items = devices.toArray(aux_items);
            result = items;
        }
        return result;
    }

    private int getIndex(Spinner spinner, String myString){

        int index = 0;

        for (int i=0;i<spinner.getCount();i++){
            String aux = (String) spinner.getItemAtPosition(i);
            if (aux.contains(myString)){
                index = i;
                continue;
            }
        }
        return index;
    }

    void bluetoothScanning(){
        final ProgressDialog progressDialog = new ProgressDialog(getContext(),

                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Buscando espere por favor...");
        progressDialog.show();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        getContext().registerReceiver(mReceiver, filter);
        final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.startDiscovery();
        countDownTimer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                progressDialog.dismiss();
                mBluetoothAdapter.cancelDiscovery();
                String[] aux_items = new String[devices1.size()];
                final String[] items = devices1.toArray(aux_items);
                myDeviceList = items;
                spinnerArrayAdapter.notifyDataSetChanged();
            }
        };
        countDownTimer.start();
    }


    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            devices1 = new ArrayList<String>();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                devices1.add(device.getName() + "-"+ device.getAddress());
                Log.i("Device Name: " , "device " + deviceName);
                Log.i("deviceHardwareAddress " , "hard"  + deviceHardwareAddress);
            }
        }
    };

    private View.OnClickListener scanBluetooth = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          bluetoothScanning();
        }
    };

    private View.OnClickListener saveBluetooth = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast toast = Toast.makeText(getContext(),"Guardado con exito.",
                    Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    };
}
