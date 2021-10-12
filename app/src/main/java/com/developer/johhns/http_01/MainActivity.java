package com.developer.johhns.http_01;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText entrada;
    private TextView salida ;
    private RequestQueue  colaHttp ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
/*
        ImageView imagen = new ImageView(getApplicationContext());
        imagen.setImageBitmap();
        imagen.setImageResource();
        */
        colaHttp = Volley.newRequestQueue(getApplicationContext()) ;

        entrada = (EditText) findViewById(R.id.EditText01);
        salida = (TextView) findViewById(R.id.TextView01);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.
                Builder().permitNetwork().build());

    }

    public void buscar(View view){
        try {
            String palabras  = entrada.getText().toString();
            String resultado = resultadosGoogle(palabras);
            salida.append(palabras + "--" + resultado + "\n");
        } catch (Exception e) {
            salida.append("Error al conectar\n");
            Log.e("HTTP", e.getMessage(), e);
        }
    }

    public void buscar2(View view){
        String palabras = entrada.getText().toString();
        salida.append(palabras + "--");
        new BuscarGoogle().execute(palabras);
    }

    public void buscar4( View view ){
        String palabras = entrada.getText().toString() ;
        salida.setText("Resultato : \n");
        try {
            resultadosVolley();
        } catch ( Exception e ){
           e.printStackTrace();
        }
    }

    private void resultadosVolley(){
        StringRequest  peticion = new StringRequest(Request.Method.GET,
                "http://appserver.iea.com.sv/wsag/Sag_HH_Util.Sincronizar_Departamentos",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String resultado = response ;
                        salida.append( resultado );
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        salida.append("Error : " + error.getMessage() );
                    }
                }
        ){
            @Override
           public Map<String,String> getHeaders() throws AuthFailureError {
                Map<String,String> cabeceras = new HashMap<String,String>();
                cabeceras.put("User-Agent","Mozilla/5.0 (Window NT 6.1)") ;
                return cabeceras ;
            }
        } ;
        colaHttp.add( peticion ) ;
    }

    public String resultadosGoogle(String palabras) throws Exception {
        String pagina = "", devuelve = "";
        URL url = new URL("http://appserver.iea.com.sv/wsag/Sag_HH_Util.Sincronizar_Departamentos");
/*        URL url = new URL("http://appserver.iea.com.sv/wsag/Sag_HH_Util.Sincronizar_Departamentos"
                       + URLEncoder.encode(palabras, "UTF-8") + "\"");

* */
        HttpURLConnection conexion = (HttpURLConnection)  url.openConnection();
       // conexion.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1)");

        if ( conexion.getResponseCode() == HttpURLConnection.HTTP_OK ) {
            BufferedReader reader = new BufferedReader( new InputStreamReader( conexion.getInputStream() ) ) ;
            String linea = reader.readLine() ;
            while ( linea != null ) {
                pagina += linea ;
                linea  =  reader.readLine() ;
            }
            reader.close() ;
            devuelve = pagina ; //buscaAproximadamente(pagina);
        } else {
            devuelve = "ERROR: " + conexion.getResponseMessage();
        }
        conexion.disconnect();
        return devuelve;
    }

    String buscaAproximadamente(String pagina){
        int ini = pagina.indexOf("Aproximadamente");
        if ( ini != -1 ) {
            int fin = pagina.indexOf(" ", ini + 16);
            return pagina.substring(ini + 16, fin);
        } else {
            return "no encontrado";
        }
    }




    class BuscarGoogle extends AsyncTask<String, Void, String> {
        private ProgressDialog progreso;
        @Override protected void onPreExecute() {
            progreso = new ProgressDialog(MainActivity.this);
            progreso.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progreso.setMessage("Accediendo a pagina...");
            progreso.setCancelable(false); // false: no muestra botón cancelar
            progreso.show();
        }
        @Override protected String doInBackground(String... palabras) {
            try {
                return resultadosGoogle(palabras[0]);
            } catch (Exception e) {
                cancel(false); //true: interrumpimos hilo, false: dejamos termine
                Log.e("HTTP", e.getMessage(), e);
                return null;
            }
        }
        @Override protected void onPostExecute(String res) {
            progreso.dismiss();
            salida.append(res + "\n");
        }

        @Override protected void onCancelled() {
            progreso.dismiss();
            salida.append("Error al conectar\n");
        }
    }



}