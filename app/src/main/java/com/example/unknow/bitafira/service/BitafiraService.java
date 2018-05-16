package com.example.unknow.bitafira.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.example.unknow.bitafira.R;
import com.example.unknow.bitafira.pacient.PacientBitalinoFragment;

import java.util.concurrent.TimeUnit;

/**
 * Created by luis_gumucio on 15-05-18.
 */

public class BitafiraService extends Service {

    private Handler myHandler;
    public static PacientBitalinoFragment UPDATE_LISTENER;
    private CountDownTimer countDownTimer;
    private long timeCountInMilliSeconds = 1 * 60000;

    public static void setUpdateListener(PacientBitalinoFragment poiService) {
        UPDATE_LISTENER = poiService;
    }

    @Override
    public void onCreate() {
        myHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                //UPDATE_LISTENER.updateTime(msg.obj.toString());
            }
        };
    }

    @Override
    public int onStartCommand(Intent intenc, int flags, int idArranque) {

        return START_STICKY;
    }

    @Override
    public void onDestroy() {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startTime() {
        countDownTimer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Message msg_out = new Message();
                msg_out.obj = hmsTimeFormatter(millisUntilFinished);
                myHandler.sendMessage(msg_out);
            }

            @Override
            public void onFinish() {
            }
        }.start();
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
}
