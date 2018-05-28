package com.example.unknow.bitafira.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.bitalino.comm.BITalinoDevice;
import com.bitalino.comm.BITalinoException;
import com.bitalino.comm.BITalinoFrame;
import com.example.unknow.bitafira.R;
import com.example.unknow.bitafira.model.BITalinoReading;
import com.example.unknow.bitafira.pacient.PacientBitalinoFragment;
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
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    private Handler handlerFile;
    private CountDownTimer countDownTimer;
    private BluetoothDevice dev = null;
    private BluetoothSocket sock = null;
    private InputStream is = null;
    private OutputStream os = null;
    private BITalinoDevice bitalino;
    DatabaseReference dbBitalino;
    public String filename = null;
    public OutputStreamWriter fout = null;
    String id;
    String idPacient;
    Thread thread;
    String idEvaluation;

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
        id = UPDATE_LISTENER.getIdEvaluation();
        idPacient = UPDATE_LISTENER.getIdPacient();
        idEvaluation = UPDATE_LISTENER.getIdEvaluationActive();
        dbBitalino = FirebaseDatabase.getInstance().getReference("bitalino").child(idPacient);
        dev = btAdapter.getRemoteDevice(remoteDevice);
        try {
            sock = dev.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            e.printStackTrace();
        }
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                UPDATE_LISTENER.updateText(msg.obj.toString());
            }
        };
        handlerFile = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                UPDATE_LISTENER.updateTextFile(msg.obj.toString());
            }
        };
        try {
            initConnection();
        } catch (BITalinoException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Service.START_STICKY;
    }

    private void initConnection() throws BITalinoException, IOException {
        try {
            Log.d(TAG, "Stopping Bluetooth discovery.");
            btAdapter.cancelDiscovery();
            sock.connect();
            bitalino = new BITalinoDevice(1000, new int[]{1});
            bitalino.open(sock.getInputStream(), sock.getOutputStream());
            UPDATE_LISTENER.stopProgress();
            startTimeCodown();
        } catch (Exception e) {
            if(bitalino != null) {
                bitalino.stop();
                sock.close();
            }
            UPDATE_LISTENER.stopProgress(idEvaluation, id);

            new AlertDialog.Builder(UPDATE_LISTENER.getContext())
                    .setTitle("Error")
                    .setMessage("Fallo al conectar con el bitalino")
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        }
    }

    private void startTimeCodown() throws BITalinoException {
        long time = UPDATE_LISTENER.getTime();
        countDownTimer = new CountDownTimer(time, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Message message = new Message();
                message.obj = hmsTimeFormatter(millisUntilFinished);
                handler.sendMessage(message);
            }

            @Override
            public void onFinish() {
                try {
                    UPDATE_LISTENER.updateText("00:00:00");
                    closeFile();
                    thread.interrupt();
                    bitalino.stop();
                    sock.close();
                    UPDATE_LISTENER.clear();
                } catch (BITalinoException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        countDownTimer.start();
        createFile(id, idPacient);
        runBitalino();
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
        try {
            bitalino.stop();
        } catch (BITalinoException e) {
            e.printStackTrace();
        }

    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    private void createFile(String id, String idPacient) {
        Log.v(TAG, "UI BUtton pressed: Store Block - createFile()");
        if (fout == null) {

            Date cDate = new Date();
            String fDate = new SimpleDateFormat("yyyyMMddHHmmss").format(cDate);
            filename = id + ".txt";
            Message message = new Message();
            message.obj = filename;
            handlerFile.sendMessage(message);

            String line  = "";

            // Select between external or internal memory
            if (true) {
                try {
                    int currentapiVersion = android.os.Build.VERSION.SDK_INT;
                    if (currentapiVersion >= android.os.Build.VERSION_CODES.GINGERBREAD){
                        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                        StrictMode.setThreadPolicy(policy);
                    }
                    File sdPath = Environment.getExternalStorageDirectory();  //getExternalStorageDirectory();
                    String directory = "/Bitafira/" + idPacient;
                    File dir = new File(sdPath.getAbsolutePath() + directory);
//                    File folder = new File(sdPath.getAbsolutePath() + "/Bitafira");
//                    folder.mkdir();

                    dir.mkdirs();
                   // dir.createNewFile();
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

    private void runBitalino() throws BITalinoException {
        thread = new Thread() {
            @Override
            public void run() {
                try {
                    bitalino.start();
                    int counter = 0;
                    while (counter < 100) {
                        final int numberOfSamplesToRead = 1000;
                        BITalinoFrame[] frames = bitalino.read(numberOfSamplesToRead);
                        // present data in screen
                        for (BITalinoFrame myBitFrame : frames) {
                            if (fout != null) {
                                BITalinoReading reading = new BITalinoReading();
                                String idBitalino = dbBitalino.push().getKey();
                                reading.setId(idBitalino);
                                long current = System.currentTimeMillis();
                                reading.setTimestamp(current);
                                reading.setSequence(Integer.valueOf(myBitFrame.getSequence()));
                                reading.setData(myBitFrame.getAnalog(1));
                                //dbBitalino.child(idBitalino).setValue(reading);
                                String line = String.valueOf(getData(myBitFrame.getAnalog(1)));
                                line += "\t" + current;
                                line += "\n";
                                try {
                                    fout.write(line);
                                } catch (IOException e) {
                                    Log.v(TAG, "Error writing to the file " + line);
                                }
                            }
                        }
                        counter++;
                    }
                } catch (BITalinoException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    private BigDecimal getData(int analog) {

        double firstSolution = (double)analog/1024;
        BigDecimal decimal = BigDecimal.valueOf(((firstSolution - 1d/2) * 3.3)/100);
        return decimal;
    }

    private void closeFile() {
        Log.v(TAG, "UI BUtton pressed: Store Block - closeFile()");

        if (fout != null) {
            try {
                fout.flush();
                fout.close();
                fout = null;
                Log.v(TAG, "File closed " + filename);
            } catch (IOException e) {
                Log.e(TAG, "Error at writing in internal memory");
                e.printStackTrace();
            }
        }
    }

}
