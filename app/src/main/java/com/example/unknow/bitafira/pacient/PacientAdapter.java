package com.example.unknow.bitafira.pacient;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.unknow.bitafira.R;
import com.example.unknow.bitafira.model.Pacient;

import java.util.List;

/**
 * Created by luis_gumucio on 12-04-18.
 */

public class PacientAdapter extends ArrayAdapter<Pacient> {

    public PacientAdapter(Context context, List<Pacient> objects) {
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
                    R.layout.layout_adpater_pacients,
                    parent,
                    false);
        }

        // Referencias UI.
        TextView namePacient = (TextView) convertView.findViewById(R.id.nombreContacto);
        TextView phonePacient = (TextView) convertView.findViewById(R.id.telefonoContacto);

        // Lead actual.
        Pacient pacient = getItem(position);

        // Setup.
        namePacient.setText(pacient.getName());
        phonePacient.setText(String.valueOf(pacient.getPhone()));

        return convertView;
    }
}

