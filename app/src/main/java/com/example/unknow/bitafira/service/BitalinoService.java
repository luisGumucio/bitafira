package com.example.unknow.bitafira.service;

import android.app.ProgressDialog;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.bitalino.comm.BITalinoDevice;
import com.bitalino.comm.BITalinoException;
import com.bitalino.comm.BITalinoFrame;
import com.example.unknow.bitafira.model.BITalinoReading;
import com.example.unknow.bitafira.pacient.PacientEvaluationFragment;
import com.example.unknow.bitafira.utils.BITlog;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static android.content.ContentValues.TAG;

public class BitalinoService extends Service {

    private static final UUID MY_UUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothDevice dev = null;
    private BluetoothSocket sock = null;
    private InputStream is = null;
    private OutputStream os = null;
    private BITalinoDevice bitalino;
    private static final String remoteDevice = "20:17:09:18:58:37";
    final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

    private Timer temporizador = new Timer();
    private static final long INTERVALO_ACTUALIZACION = 10; // En ms
    public static PacientEvaluationFragment UPDATE_LISTENER;
    private double cronometro = 0;
    private Handler handler;
    private CountDownTimer countDownTimer;
    private long timeCountInMilliSeconds = 1 * 60000;
    public String filename = null;
    public OutputStreamWriter fout = null;
    DatabaseReference dbBitalino;

    /**
     * Establece quien va ha recibir las actualizaciones del cronometro
     *
     * @param poiService
     */
    public static void setUpdateListener(PacientEvaluationFragment poiService) {
        UPDATE_LISTENER = poiService;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String id = intent.getStringExtra("ID_EVALUATION");
        String idPacient = intent.getStringExtra("ID_PACIENT");
        dbBitalino = FirebaseDatabase.getInstance().getReference("bitalino").child(idPacient);
        initConnection(id, idPacient);
        return Service.START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        dev = btAdapter.getRemoteDevice(remoteDevice);
    }

    private void initConnection(String id, String idPacient) {
        try {
            Log.d(TAG, "Stopping Bluetooth discovery.");
            btAdapter.cancelDiscovery();
            sock = dev.createRfcommSocketToServiceRecord(MY_UUID);
            sock.connect();
            bitalino = new BITalinoDevice(1000, new int[]{1});
            bitalino.open(sock.getInputStream(), sock.getOutputStream());
            createFile(id, idPacient);
            startCronometer(idPacient);
        } catch (Exception e) {
            new AlertDialog.Builder(UPDATE_LISTENER.getContext())
                    .setTitle("Error")
                    .setMessage("Fallo al conectar con el bitalino")
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        }
    }

    private void startCronometer(String idPacient) {
        long temporal;
        int hours = 0;
        String timeEvaluation = UPDATE_LISTENER.getTime();
        String[] output = timeEvaluation.split(":");
        int hoursToMinute = Integer.parseInt(output[0]) * 60;
        int minute = Integer.parseInt(output[1]);
        timeCountInMilliSeconds = hoursToMinute * 60 * 1000;
        temporal = minute * 60 * 1000;
        timeCountInMilliSeconds = timeCountInMilliSeconds + temporal;
        startCountDownTimer(idPacient);
    }

    /**
     * method to start count down timer
     */
    private void startCountDownTimer(String idPacient) {
        try {
            bitalino.start();
//            new Handler().postDelayed(new Runnable() {
//                public void run() {
//                    countDownTimer = new CountDownTimer(timeCountInMilliSeconds, 1000) {
//                        @Override
//                        public void onTick(long millisUntilFinished) {
//                            UPDATE_LISTENER.setTime(hmsTimeFormatter(millisUntilFinished));
//                        }
//
//                        @Override
//                        public void onFinish() {
//                            try {
//                                bitalino.stop();
//                                UPDATE_LISTENER.setTime(hmsTimeFormatter(timeCountInMilliSeconds));
//                                UPDATE_LISTENER.setTime("00:00");
//                            } catch (BITalinoException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    };
//                    countDownTimer.start();
//                }
//            }, 1000);
            runBitalino(idPacient);
        } catch (BITalinoException e) {
            e.printStackTrace();
        }
    }

    private void runBitalino(String idPacient) throws BITalinoException {
        // read until task is stopped
        int counter = 0;
        while (counter < 100) {
            final int numberOfSamplesToRead = 1000;
            BITalinoFrame[] frames = bitalino.read(numberOfSamplesToRead);
            // prepare reading for upload
            for (BITalinoFrame myBitFrame : frames) {
                if (fout != null) {
                    BITalinoReading reading = new BITalinoReading();
                    String idBitalino = dbBitalino.push().getKey();
                    reading.setId(idBitalino);
                    reading.setTimestamp(System.currentTimeMillis());
                    reading.setSequence(Integer.valueOf(myBitFrame.getSequence()));

                   // dbBitalino.child(idBitalino).setValue(reading);
                    String line = Integer.valueOf(myBitFrame.getSequence()).toString();
                    line += "\t" + myBitFrame.getAnalog(1);
                    line += "\n";
                    try {
                        fout.write(line);
                        //  Log.v(TAG, "Write: \n\t"+line);
                    } catch (IOException e) {
                        Log.v(TAG, "Error writing to the file " + line);
                        //e.printStackTrace();
                    }
                }
            }
            counter++;
        }

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
        try {
            if(bitalino != null) {
                bitalino.stop();
            }
            stopSelf();
        } catch (BITalinoException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    private void iniciarCronometro() {
        temporizador.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                cronometro += 0.01;
                handler.sendEmptyMessage(0);
            }
        }, 0, INTERVALO_ACTUALIZACION);
    }

    private void pararCronometro() {
        if (temporizador != null)
            temporizador.cancel();
    }

    private void createFile(String id, String idPacient) {
        Log.v(TAG, "UI BUtton pressed: Store Block - createFile()");
        if (fout == null) {

            Date cDate = new Date();
            String fDate = new SimpleDateFormat("yyyyMMddHHmmss").format(cDate);
            filename = id + ".txt";

            ArrayList<Integer> channelList = new ArrayList<Integer>();
            for (int i = 0; i < 2; i++) {
                boolean find = false;
                for (int j = 0; j < BITlog.channels.length; j++) {
                    if (i == BITlog.channels[j]) {
                        find = true;
                        channelList.add(i, Integer.valueOf(i));
                    }
                }
                if (!find) channelList.add(i, null);
            }

            String line = "#{" + "\"date\": \"" + new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS").format(cDate) + "\", "
                    + "\"MAC\": \"" + BITlog.MAC + "\", "
                    + "\"ChannelsOrder\": [\"SeqN\"";

            for (int i = 0; i < 2; i++) {
                if (channelList.get(i) != null) {
                    line += ", " + "\t" + "\"Analog" + i + "\"";
                }
            }

            line += " ]}\n";

            // Select between external or internal memory
            if (true) {
                try {
                    File sdPath = Environment.getExternalStorageDirectory();  //getExternalStorageDirectory();
                    String directory = "/Bitafira/" + idPacient;
                    File dir = new File(sdPath.getAbsolutePath() + directory);
                    dir.mkdirs();

                    File f = new File(dir, filename);
                    fout = new OutputStreamWriter(new FileOutputStream(f));

                    Log.v(TAG, "Log file: " + filename + " created in the SD card");
                    fout.write(line);
                    fout.flush();
                } catch (Exception ex) {
                    stopSelf();
                    Log.e("Ficheros", "Error writing the file in SD card");
                }
            } else {
                try {
                    fout = new OutputStreamWriter(openFileOutput(filename, MODE_WORLD_WRITEABLE));
                    Log.v(TAG, "Log file: " + filename + " created in the internal memory");
                    fout.write(line);
                    //Log.v(TAG, "Write: \n\t"+line);
                } catch (Exception e) {
                    Log.e(TAG, "Error at writing in internal memory");
                    e.printStackTrace();

                }
            }
        }
    }
}
