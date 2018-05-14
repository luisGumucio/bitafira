package com.example.unknow.bitafira.pacient;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.unknow.bitafira.R;
import com.example.unknow.bitafira.model.Evaluation;
import com.example.unknow.bitafira.model.Event;

import java.util.List;

public class EventAdapter extends ArrayAdapter<Event> {

    public EventAdapter(Context context, List<Event> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Obtener inflater.
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // ¿Existe el view actual?
        if (null == convertView) {
            convertView = inflater.inflate(
                    R.layout.layout_adpater_event,
                    parent,
                    false);
        }

        // Referencias UI.
        TextView dateStart = (TextView) convertView.findViewById(R.id.txtDateEvent);
        TextView timeHour = (TextView) convertView.findViewById(R.id.txtHourEvent);

        // Lead actual.
        Event event = getItem(position);

        // Setup.
        dateStart.setText(event.getDate());
        timeHour.setText(String.valueOf(event.getHour()));

        return convertView;
    }
}