package com.example.unknow.bitafira.pacient;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.bitalino.comm.BITalinoFrame;
import com.example.unknow.bitafira.LoginActivity;
import com.example.unknow.bitafira.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by luis_gumucio on 15-05-18.
 */

public class PacientBitalinoFragment extends Fragment {

    public StoreLooperThread looperThread;
    private EditText txtTime;
    private Button btStart;
    private CountDownTimer countDownTimer;
    ProgressDialog progressDialog;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pacient_bitalino_evaluation, parent, false);
    }

    // This event is triggered soon after onCreateView().
    // onViewCreated() is only called if the view returned from onCreateView() is non-null.
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressDialog = new ProgressDialog(getContext(),
                R.style.AppTheme_Dark_Dialog);
        txtTime = (EditText) view.findViewById(R.id.txt_time_bita);
        btStart = (Button) view.findViewById(R.id.btn_time_start);
        btStart.setOnClickListener(onStartTime);
        looperThread= new StoreLooperThread();
    }

    private View.OnClickListener onStartTime = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Authenticating...");
            progressDialog.show();
            looperThread.start();

        }
    };

    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //UPDATE_LISTENER.updateTime(msg.obj.toString());
            txtTime.setText(msg.obj.toString());
        }
    };

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
                progressDialog.dismiss();
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


    public class StoreLooperThread  extends Thread {
        public Handler mHandler;
        public void run() {

            Looper.prepare();
            startTime();
            mHandler = new Handler() {

                public void handleMessage(Message msg_in) {

                }

            };

            Looper.loop();
        }
    }
}

