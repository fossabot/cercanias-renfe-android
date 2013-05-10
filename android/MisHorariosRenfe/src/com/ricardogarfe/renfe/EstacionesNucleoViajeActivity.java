/*
 * Copyright [2013] [Ricardo García Fernández] [ricardogarfe@gmail.com]
 * 
 * This file is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This file is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.ricardogarfe.renfe;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ricardogarfe.renfe.asynctasks.RetrieveEstacionesNucleoTask;
import com.ricardogarfe.renfe.model.EstacionCercanias;
import com.ricardogarfe.renfe.model.NucleoCercanias;

public class EstacionesNucleoViajeActivity extends Activity {

    private Button verHorariosButton;
    private Spinner origenSpinner;
    private Spinner destinoSpinner;

    private Spinner day;
    private Spinner month;
    private Spinner year;

    // Context
    public static Context mEstacionesNucleoViajeContext;

    private SharedPreferences mPreferences;

    private int estacionOrigenId = 0;
    private int estacionDestinoId = 0;

    private int estacionOrigenPosToSet = 0;
    private int estacionDestinoPosToSet = 0;

    boolean retrieveSharedPreferences = false;

    // AsyncTasks
    private RetrieveEstacionesNucleoTask retrieveEstacionesNucleoTask;

    private String TAG = getClass().getSimpleName();

    // Nucleo values
    private int codigoNucleo;
    private String descripcionNucleo;
    private String estacionesJSON;

    // Configuracion estaciones.
    private static List<EstacionCercanias> estacionCercaniasList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.estacion_nucleo_viaje);

        mPreferences = getSharedPreferences("Renfe", MODE_PRIVATE);

        mEstacionesNucleoViajeContext = this;

        configureWidgets();

        configureEstacionesPorNucleo();

        // Set listeners for each spinner.
        origenSpinner.setOnItemSelectedListener(estacionOrigenSelectedListener);
        destinoSpinner
                .setOnItemSelectedListener(estacionDestinoSelectedListener);

    }

    /**
     * Check if exist shared preferences to update spinners.
     * 
     * @return true if exist.
     */
    public boolean isNucleoChangedFromSharedPreferences() {
        // Comprobar si existen en sharedPreferences estaciones seleccionadas.
        final boolean codigoNucleoSet = mPreferences.getInt("codigoNucleo", 0) == codigoNucleo;
        final boolean estacionOrigenSet = mPreferences
                .contains("estacionOrigenPosToSet");
        final boolean estacioDestinoSet = mPreferences
                .contains("estacionDestinoPosToSet");

        return (codigoNucleoSet && estacionOrigenSet && estacioDestinoSet);
    }

    /**
     * Configure Widget values to initialize ui.
     */
    public void configureWidgets() {
        origenSpinner = (Spinner) this.findViewById(R.id.origenSpinner);
        destinoSpinner = (Spinner) this.findViewById(R.id.destinoSpinner);

        verHorariosButton = (Button) findViewById(R.id.verHorariosButton);
        verHorariosButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                // Mostrar error elegir una misma estacion de origen y destino.
                if (estacionOrigenId == estacionDestinoId) {
                    Toast.makeText(
                            getApplicationContext(),
                            "Las estaciones de origen y destino no pueden ser iguales",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(getApplicationContext(),
                            HorarioCercaniasActivity.class);
                    intent.putExtra("estacionOrigenId", estacionOrigenId);
                    intent.putExtra("estacionDestinoId", estacionDestinoId);

                    int origenSpinnerPosition = origenSpinner
                            .getSelectedItemPosition();
                    String estacionOrigenName = estacionCercaniasList.get(
                            origenSpinnerPosition).getDescripcion();

                    int destinoSpinnerPosition = destinoSpinner
                            .getSelectedItemPosition();
                    String estacionDestinoName = estacionCercaniasList.get(
                            destinoSpinnerPosition).getDescripcion();

                    // Configure day TODO: select day instance.
                    Calendar calendar = Calendar.getInstance();

                    DecimalFormat mFormat = new DecimalFormat("00");
                    mFormat.setRoundingMode(RoundingMode.DOWN);

                    int dayInt = calendar.get(Calendar.DATE);
                    String currentDay = mFormat.format(Double.valueOf(dayInt));

                    int monthInt = calendar.get(Calendar.MONTH) + 1;
                    String currentMonth = mFormat.format(Double
                            .valueOf(monthInt));

                    String currentYear = Integer.toString(calendar
                            .get(Calendar.YEAR));

                    intent.putExtra("day", currentDay);
                    intent.putExtra("month", currentMonth);
                    intent.putExtra("year", currentYear);

                    intent.putExtra("nucleoId", codigoNucleo);
                    intent.putExtra("nucleoName", descripcionNucleo);
                    intent.putExtra("estacionOrigenName", estacionOrigenName);
                    intent.putExtra("estacionDestinoName", estacionDestinoName);

                    // save preferences.
                    saveSharedPreferences();

                    startActivity(intent);

                }
            }
        });
    }

    /**
     * Configure {@link EstacionCercanias} using {@link NucleoCercanias} intent
     * values to retrieve each station and convert to spinner values.
     */
    public void configureEstacionesPorNucleo() {

        // Retrieve value from nucleos activity.
        Intent intentFromActivity = getIntent();

        TextView textViewNucleo = (TextView) this
                .findViewById(R.id.nucleo_text);

        if (intentFromActivity != null) {

            codigoNucleo = intentFromActivity.getIntExtra("codigo_nucleo", 0);
            descripcionNucleo = intentFromActivity
                    .getStringExtra("descripcion_nucleo");

            if (descripcionNucleo != null) {
                textViewNucleo.setText(descripcionNucleo);
            }
        }

        retrieveSharedPreferences = isNucleoChangedFromSharedPreferences();

        estacionesJSON = intentFromActivity.getStringExtra("estaciones_json");

        if (!retrieveSharedPreferences
                || (estacionCercaniasList == null || estacionCercaniasList
                        .isEmpty())) {
            retrieveEstacionesNucleoTask = new RetrieveEstacionesNucleoTask();
            retrieveEstacionesNucleoTask.execute(estacionesJSON,
                    descripcionNucleo, null, null);
            retrieveEstacionesNucleoTask
                    .setMessageEstacionesNucleoHandler(messageEstacionesNucleoHandler);
        } else {

            Message messageEstacionesNucleo = new Message();
            messageEstacionesNucleo.obj = estacionCercaniasList;
            messageEstacionesNucleoHandler.sendMessage(messageEstacionesNucleo);

        }
    }

    /**
     * Handler to retrieve values from {@link RetrieveEstacionesNucleoTask}
     * result.
     */
    private Handler messageEstacionesNucleoHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            estacionCercaniasList = (ArrayList<EstacionCercanias>) msg.obj;

            ArrayAdapter<CharSequence> spinnerEstacionAdapter = new ArrayAdapter<CharSequence>(
                    getApplicationContext(),
                    android.R.layout.simple_spinner_item);

            for (EstacionCercanias estacionCercanias : estacionCercaniasList) {
                spinnerEstacionAdapter.add(estacionCercanias.getDescripcion());
            }

            spinnerEstacionAdapter
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            origenSpinner.setAdapter(spinnerEstacionAdapter);

            destinoSpinner.setAdapter(spinnerEstacionAdapter);

            estacionOrigenId = estacionCercaniasList.get(0).getCodigo();
            estacionDestinoId = estacionCercaniasList.get(0).getCodigo();

        }
    };

    private OnItemSelectedListener estacionOrigenSelectedListener = new OnItemSelectedListener() {
        public void onItemSelected(AdapterView parent, View v, int position,
                long id) {

            if (retrieveSharedPreferences) {
                origenSpinner.setSelection(mPreferences.getInt(
                        "estacionOrigenPosToSet", 0));
                retrieveSharedPreferences = false;
            }

            if (origenSpinner.getSelectedItemPosition() >= 0) {
                int pos = origenSpinner.getSelectedItemPosition();
                estacionOrigenPosToSet = pos;
                estacionOrigenId = estacionCercaniasList.get(position)
                        .getCodigo();
            }
        }

        public void onNothingSelected(AdapterView arg0) {
        }
    };

    private OnItemSelectedListener estacionDestinoSelectedListener = new OnItemSelectedListener() {
        public void onItemSelected(AdapterView parent, View v, int position,
                long id) {

            if (retrieveSharedPreferences) {
                destinoSpinner.setSelection(mPreferences.getInt(
                        "estacionDestinoPosToSet", 0));
                retrieveSharedPreferences = false;
            }

            if (destinoSpinner.getSelectedItemPosition() >= 0) {
                int pos = destinoSpinner.getSelectedItemPosition();
                estacionDestinoPosToSet = pos;
                estacionDestinoId = estacionCercaniasList.get(position)
                        .getCodigo();
            }
        }

        public void onNothingSelected(AdapterView arg0) {
        }
    };

    protected void onSaveInstanceState(Bundle outState) {

        saveSharedPreferences();
    }

    /**
     * Save values from nucleos y estaciones in Shared Preferences.
     */
    public void saveSharedPreferences() {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt("codigoNucleo", codigoNucleo);
        editor.putInt("estacionOrigenPosToSet", estacionOrigenPosToSet);
        editor.putInt("estacionDestinoPosToSet", estacionDestinoPosToSet);
        editor.putBoolean("isNucleoRetrieved", estacionCercaniasList != null
                && !estacionCercaniasList.isEmpty());
        editor.commit();
    };
}
