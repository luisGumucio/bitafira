package com.example.unknow.bitafira.doctor;

import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidplot.util.Redrawer;
import com.androidplot.xy.AdvancedLineAndPointRenderer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.example.unknow.bitafira.R;
import com.example.unknow.bitafira.model.Pacient;
import com.example.unknow.bitafira.pacient.PacientEventFragment;
import android.support.design.widget.FloatingActionButton;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;

public class EvaluationTimeFragment extends Fragment {

    private XYPlot plot;

    /**
     * Uses a separate thread to modulate redraw frequency.
     */
    private Redrawer redrawer;
    private FloatingActionButton btEvaluation;
    private Pacient pacient;
    FragmentTransaction transact;
    private TextView phoneRefe, full, address, phone, email, rol, historial;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_evaluation_real_time_pacient, parent, false);
    }

    // This event is triggered soon after onCreateView().
    // onViewCreated() is only called if the view returned from onCreateView() is non-null.
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        transact = this.getFragmentManager().beginTransaction();//apuntamos con que nodo vamos a trabajar
        // initialize our XYPlot reference:
        plot = (XYPlot) view.findViewById(R.id.plot);

        ECGModel ecgSeries = new ECGModel(1000, 1000);

        // add a new series' to the xyplot:
        MyFadeFormatter formatter =new MyFadeFormatter(1000);
        formatter.setLegendIconEnabled(false);
        plot.addSeries(ecgSeries, formatter);
        plot.setRangeStep(StepMode.INCREMENT_BY_VAL, 0.5);
        plot.setRangeStepValue(0.5);
        plot.setRangeBoundaries(-1.5f, 1.5f, BoundaryMode.FIXED);
        plot.setDomainBoundaries(10, 1000, BoundaryMode.FIXED);

        // reduce the number of range labels
        plot.setLinesPerRangeLabel(3);

        // start generating ecg data in the background:
        //ecgSeries.start(new WeakReference<>(plot.getRenderer(AdvancedLineAndPointRenderer.class)));

        // set a redraw rate of 30hz and start immediately:
       // redrawer = new Redrawer(plot, 1000, true);

        btEvaluation = (FloatingActionButton) view.findViewById(R.id.btn_evaluations);
        btEvaluation.setOnClickListener(goToEvaluation);
        full = (TextView)  view.findViewById(R.id.txt_name_full_info_e);
        email = (TextView) view.findViewById(R.id.txt_email_info_e);
        rol = (TextView)view.findViewById(R.id.txt_role_info_e);
        historial = (TextView) view.findViewById(R.id.txtNroClinico);

        pacient = (Pacient) getArguments().getSerializable("PACIENT");
        init();
    }
    private void init() {
        full.setText(pacient.getName() + " "+pacient.getLastName());
        rol.setText(pacient.getRole());
        email.setText(pacient.getEmail());
    }

    private View.OnClickListener goToEvaluation = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Fragment mFrag = new DoctorHistorialFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable("PACIENT", pacient);
            mFrag.setArguments(bundle);
            transact.replace(R.id.main_fragment, mFrag).addToBackStack(null);
            transact.commit();
        }
    };

    /**
     * Special {@link AdvancedLineAndPointRenderer.Formatter} that draws a line
     * that fades over time.  Designed to be used in conjunction with a circular buffer model.
     */
    public static class MyFadeFormatter extends AdvancedLineAndPointRenderer.Formatter {

        private int trailSize;

        public MyFadeFormatter(int trailSize) {
            this.trailSize = trailSize;
        }

        @Override
        public Paint getLinePaint(int thisIndex, int latestIndex, int seriesSize) {
            // offset from the latest index:
            int offset;
            if(thisIndex > latestIndex) {
                offset = latestIndex + (seriesSize - thisIndex);
            } else {
                offset =  latestIndex - thisIndex;
            }

            float scale = 255f / trailSize;
            int alpha = (int) (255 - (offset * scale));
            getLinePaint().setAlpha(alpha > 0 ? alpha : 0);
            return getLinePaint();
        }
    }

    /**
     * Primitive simulation of some kind of signal.  For this example,
     * we'll pretend its an ecg.  This class represents the data as a circular buffer;
     * data is added sequentially from left to right.  When the end of the buffer is reached,
     * i is reset back to 0 and simulated sampling continues.
     */
    public static class ECGModel implements XYSeries {

        private final Number[] data;
        private final long delayMs;
        private final int blipInteral;
        private final Thread thread;
        private boolean keepRunning;
        private int latestIndex;

        private WeakReference<AdvancedLineAndPointRenderer> rendererRef;

        /**
         *
         * @param size Sample size contained within this model
         * @param updateFreqHz Frequency at which new samples are added to the model
         */
        public ECGModel(int size, int updateFreqHz) {
            data = new Number[size];
            for(int i = 0; i < data.length; i++) {
                data[i] = 0;
            }

            // translate hz into delay (ms):
            delayMs = 1000 / updateFreqHz;

            // add 7 "blips" into the signal:
            blipInteral = size / 7;

            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (keepRunning) {
                            if (latestIndex >= data.length) {
                                latestIndex = 0;
                            }

                            // generate some random data:
                            if (latestIndex % blipInteral == 0) {
                                // insert a "blip" to simulate a heartbeat:
                                data[latestIndex] = (Math.random() * 1000) + 1;
                            } else {
                                // insert a random sample:
                                data[latestIndex] = Math.random() * 100;
                            }

                            if(latestIndex < data.length - 1) {
                                // null out the point immediately following i, to disable
                                // connecting i and i+1 with a line:
                                data[latestIndex +1] = null;
                            }

                            if(rendererRef.get() != null) {
                                rendererRef.get().setLatestIndex(latestIndex);
                                Thread.sleep(delayMs);
                            } else {
                                keepRunning = false;
                            }
                            latestIndex++;
                        }
                    } catch (InterruptedException e) {
                        keepRunning = false;
                    }
                }
            });
        }

        public void start(final WeakReference<AdvancedLineAndPointRenderer> rendererRef) {
            this.rendererRef = rendererRef;
            keepRunning = true;
            thread.start();
        }

        @Override
        public int size() {
            return data.length;
        }

        @Override
        public Number getX(int index) {
            return index;
        }

        @Override
        public Number getY(int index) {
            return data[index];
        }

        @Override
        public String getTitle() {
            return "Signal";
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(redrawer != null) {
            redrawer.finish();
        }
        //redrawer.finish();
    }
}
