package com.example.unknow.bitafira.pacient;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.unknow.bitafira.R;
import com.example.unknow.bitafira.model.Evaluation;
import com.example.unknow.bitafira.model.Pacient;

import java.util.List;

public class EvaluationAdapter extends ArrayAdapter<Evaluation> {

    public EvaluationAdapter(Context context, List<Evaluation> objects) {
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
                    R.layout.layout_adpater_evaluation,
                    parent,
                    false);
        }

        // Referencias UI.
        TextView dateStart = (TextView) convertView.findViewById(R.id.txt_date_start);
        TextView timeEvaluation = (TextView) convertView.findViewById(R.id.txt_time_evaluation);

        // Lead actual.
        Evaluation evaluation = getItem(position);

        // Setup.
        dateStart.setText(evaluation.getDateStart());
        timeEvaluation.setText(String.valueOf(evaluation.getTimeEvaluation()));

        return convertView;
    }
}
