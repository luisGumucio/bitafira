package com.example.unknow.bitafira.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.bitalino.comm.BITalinoDevice;
import com.example.unknow.bitafira.R;
import com.example.unknow.bitafira.pacient.PacientBitalinoFragment;
import com.example.unknow.bitafira.pacient.PacientEvaluationFragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static android.content.ContentValues.TAG;

/**
 * Created by luis_gumucio on 15-05-18.
 */

public class BitafiraService extends Service {

    final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
    private static final UUID MY_UUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String remoteDevice = "20:17:09:18:58:37";

    private Timer temporizador = new Timer();
    private static final long INTERVALO_ACTUALIZACION = 10; // En ms
    public static PacientBitalinoFragment UPDATE_LISTENER;
    private double cronometro = 0;
    private Handler handler;
    private CountDownTimer countDownTimer;
    private BluetoothDevice dev = null;
    private BluetoothSocket sock = null;
    private InputStream is = null;
    private OutputStream os = null;
    private BITalinoDevice bitalino;
    DatabaseReference dbBitalino;

    /**
     * Establece quien va ha recibir las actualizaciones del cronometro
     *
     * @param poiService
     */
    public static void setUpdateListener(PacientBitalinoFragment poiService) {
        UPDATE_LISTENER = poiService;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String id = intent.getStringExtra("ID_EVALUATION");
        String idPacient = intent.getStringExtra("ID_PACIENT");
        dbBitalino = FirebaseDatabase.getInstance().getReference("bitalino").child(idPacient);
        UPDATE_LISTENER.startProgress();
        dev = btAdapter.getRemoteDevice(remoteDevice);
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                UPDATE_LISTENER.updateText(msg.obj.toString());
            }
        };
        //return super.onStartCommand(intent, flags, startId);

        initConnection();
        return Service.START_STICKY;
    }

    private void initConnection() {
        try {
            Log.d(TAG, "Stopping Bluetooth discovery.");
            sock = dev.createRfcommSocketToServiceRecord(MY_UUID);
            sock.connect();
            bitalino = new BITalinoDevice(1000, new int[]{1});
            bitalino.open(sock.getInputStream(), sock.getOutputStream());
            startTimeCodown();
        } catch (Exception e) {
            UPDATE_LISTENER.stopProgress();
            new AlertDialog.Builder(UPDATE_LISTENER.getContext())
                    .setTitle("Error")
                    .setMessage("Fallo al conectar con el bitalino")
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        }
    }

    private void startTimeCodown() {
        countDownTimer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Message message = new Message();
                message.obj = hmsTimeFormatter(millisUntilFinished);
                handler.sendMessage(message);
            }

            @Override
            public void onFinish() {

            }
        };
        countDownTimer.start();
    }

    /**
     * method to convert millisecond to time format
     *
     * @param milliSeconds
     * @return HH:mm:ss time formatted string
     */
    private String hmsTimeFormatter(long milliSeconds) {

        String hms = String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(milliSeconds),
                TimeUnit.MILLISECONDS.toMinutes(milliSeconds) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliSeconds)),
                TimeUnit.MILLISECONDS.toSeconds(milliSeconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliSeconds)));

        return hms;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}
