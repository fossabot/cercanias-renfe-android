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

package com.ricardogarfe.renfe.asynctasks;

import java.net.URL;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.json.JSONObject;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.ricardogarfe.renfe.handler.HorariosCercaniasHandler;
import com.ricardogarfe.renfe.model.DatosPeticionHorarioCercanias;
import com.ricardogarfe.renfe.model.HorarioCercanias;
import com.ricardogarfe.renfe.parser.JSONEstacionCercaniasParser;

/**
 * @author ricardo
 * 
 */
public class HorarioCercaniasTask
        extends
        AsyncTask<DatosPeticionHorarioCercanias, Integer, List<HorarioCercanias>> {

    private ProgressDialog progressDialog = null;
    private JSONEstacionCercaniasParser jsonEstacionCercaniasParser;

    private JSONObject mJSONObjectEstacionCercanias;

    private List<HorarioCercanias> mHorarioCercaniasList = null;

    private final String TAG = getClass().getSimpleName();

    private final String ERROR_MESSAGE = "No se han encontrado Horarios Cercanias";

    private Handler messageNucleoCercaniasHandler;
    private Message messageNucleoCercanias;

    private DatosPeticionHorarioCercanias datosPeticionHorarioCercanias;

    // URL to make requests.
    private final static String URL = "http://horarios.renfe.com/cer/horarios/horarios.jsp";

    protected void onPreExecute() {
        // Show progressDialog
        progressDialog.show();
    }

    /**
     * Retrieve train trip schedules from URL and selected parameters using XML
     * Parser {@link HorariosCercaniasHandler} to convert result to a Java
     * {@link HorarioCercanias} List and send correct or error message to
     * handler.
     */
    @Override
    protected List<HorarioCercanias> doInBackground(
            DatosPeticionHorarioCercanias... datosPeticionHorarioCercaniasGet) {

        datosPeticionHorarioCercanias = datosPeticionHorarioCercaniasGet[0];

        URL url;
        try {
            url = new URL(URL + "?nucleo="
                    + datosPeticionHorarioCercanias.getNucleo() + "&o="
                    + datosPeticionHorarioCercanias.getOrigen() + "&d="
                    + datosPeticionHorarioCercanias.getDestino() + "&df="
                    + datosPeticionHorarioCercanias.getFechaViaje() + "&hd="
                    + datosPeticionHorarioCercanias.getHoraFinal() + "&ho="
                    + datosPeticionHorarioCercanias.getHoraInicio());

            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();

            XMLReader xr = sp.getXMLReader();
            HorariosCercaniasHandler horariosCercaniasHandler = new HorariosCercaniasHandler();
            xr.setContentHandler(horariosCercaniasHandler);

            xr.parse(new InputSource(url.openStream()));

            mHorarioCercaniasList = horariosCercaniasHandler
                    .getHorarioCercaniasList();

        } catch (Exception e) {
            Log.e(TAG, "Error al obtener los Horarios de Cercanias from:\t"
                    + datosPeticionHorarioCercanias.toString() + "debido a:\n"
                    + e.getMessage());
        }

        return mHorarioCercaniasList;

    }

    @Override
    protected void onProgressUpdate(final Integer... progress) {
        progressDialog.setProgress(progress[0]);
    }

    @Override
    protected void onPostExecute(List<HorarioCercanias> horariosCercaniasList) {
        progressDialog.dismiss();

        messageNucleoCercanias = new Message();

        // Send result message
        if (horariosCercaniasList != null && !horariosCercaniasList.isEmpty()) {

            // Check error message from server
            HorarioCercanias horarioCercanias = horariosCercaniasList.get(0);
            if (horarioCercanias.getError() == null
                    || horarioCercanias.getError().isEmpty()) {
                messageNucleoCercanias.what = HorariosCercaniasHandler.TASK_COMPLETE;
                messageNucleoCercanias.obj = mHorarioCercaniasList;
                Log.d(TAG, "Retrieve Horarios Cercanias correctly from:\t"
                        + datosPeticionHorarioCercanias.toString());
            } else {
                messageNucleoCercanias.what = HorariosCercaniasHandler.TASK_ERROR;
                messageNucleoCercanias.obj = ERROR_MESSAGE
                        + horarioCercanias.getError();
                Log.e(TAG, "Error al obtener los Horarios de Cercanias:\t"
                        + horarioCercanias.getError());
            }

        } else {
            messageNucleoCercanias.what = HorariosCercaniasHandler.TASK_ERROR;
            messageNucleoCercanias.obj = ERROR_MESSAGE;
            Log.e(TAG, "Error al obtener los Horarios de Cercanias from:\t"
                    + datosPeticionHorarioCercanias.toString());
        }

        getMessageNucleoCercaniasHandler().sendMessage(messageNucleoCercanias);

    }

    public Handler getMessageNucleoCercaniasHandler() {
        return messageNucleoCercaniasHandler;
    }

    public void setMessageNucleoCercaniasHandler(
            Handler messageNucleoCercaniasHandler) {
        this.messageNucleoCercaniasHandler = messageNucleoCercaniasHandler;
    }

    public ProgressDialog getProgressDialog() {
        return progressDialog;
    }

    public void setProgressDialog(ProgressDialog progressDialog) {
        this.progressDialog = progressDialog;
    }
}
