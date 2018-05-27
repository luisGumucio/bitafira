package com.example.unknow.bitafira.global;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.unknow.bitafira.R;

public class CustomDialogClass extends Dialog implements
        android.view.View.OnClickListener {

    public Activity c;
    public Dialog d;
    public Button yes;
    private TextView txtDate, txtTime, txtEvent;
    public CustomDialogClass(Activity a) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_dialog);
        yes = (Button) findViewById(R.id.btn_exit_event);
        yes.setOnClickListener(this);
        txtDate =(TextView) findViewById(R.id.txt_date_info);
        txtTime =(TextView) findViewById(R.id.txt_time_info);
        txtEvent =(TextView) findViewById(R.id.txt_event_info);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_exit_event:
                dismiss();
                break;
            default:
                break;
        }
        dismiss();
    }

    public void fillField(String date, String time, String event) {


        txtDate.setText(date);
        txtTime.setText(time);
        txtEvent.setText(event);
    }
}
