 package com.example.trabajo1_ad_psp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private static final int CODIGO_SOLICITUD_PERMISO_RCL =1;
    private static final int CODIGO_SOLICITUD_PERMISO_RPS =2;
    private static final int CONTACTS_PERMISSION = 3;
    private static final int PHONE_PERMISSION = 4;
    private MainActivity activity;
    TextView textView;
    String RegistroLlamada;
    String auxContacto;
    String numero;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = this;
        init();
    }

    private void init() {

        Button button = (Button) findViewById(R.id.btVerContactos);
        textView = findViewById(R.id.tvContactos);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getContactsPermission();

            }
        });

    }

    /*
    * Comprobamos que tenemos acceso a los contactos del usuario
    * */

    private void getContactsPermission() {
        int result = PackageManager.PERMISSION_GRANTED;

        if(result == PackageManager.PERMISSION_GRANTED) {
            consultarCPLlamadas();
        } else {
            requestPermission();
        }
    }

    /*
    * Sacamos los nombres de mis contactos para despues comprobar si tenemos alguna llamada suya en el registro
    * */
    private void getContacts() {


        ContentResolver contentResolver = getContentResolver();

        Cursor contactCursor = contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                null, // *
                null, // where
                null, // parámetros
                null);

        while (contactCursor.moveToNext()) {
            //id, display_namem, has_phone_number,
            String id = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts._ID));
            String name = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            String cadenaNumerica = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

            Cursor phoneCursor = contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, //url
                    null, //*
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?", //where contact_id = :contact_id
                    new String[]{id}, //parámetro id como sustituto de ?
                    null);
            while (phoneCursor.moveToNext()) {
                int phoneType = phoneCursor.getInt(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                if (phoneNumber.equals(numero)) {
                    auxContacto = phoneNumber;
                }
            }
            phoneCursor.close();
            //}
        }
        //}
    }



    /*
    * Pedimos los permisos en caso de que no hayan sido otorgados
    * */

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
                explainReason();
            } else {
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, CONTACTS_PERMISSION);
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_CALL_LOG}, CODIGO_SOLICITUD_PERMISO_RCL);
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_PHONE_STATE}, CODIGO_SOLICITUD_PERMISO_RPS);
            }
        }
    }

    private void explainReason() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.titulo_permiso);
        builder.setMessage(R.string.mensaje_permiso);
        builder.setPositiveButton(R.string.aceptar, new DialogInterface.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(DialogInterface dialog, int which) {
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, CONTACTS_PERMISSION);
            }
        });
        builder.setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        builder.show();
    }

    /*public void mostrarLlamadas (){

        if (checarStatusPermiso()) {
            consultarCPLlamadas();
        }else {
            requestPermission();
        }

    }*/

    /*public void solicitarPermiso(){

        boolean solicitarPermisoRCL = ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_CALL_LOG);
        boolean solicitarPermisoRPS = ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_PHONE_STATE);

        if(solicitarPermisoRCL && solicitarPermisoRPS){
            Toast.makeText(activity, "Los permisos fueron otorgados", Toast.LENGTH_SHORT).show();
        }else {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_CALL_LOG}, CODIGO_SOLICITUD_PERMISO_RCL);
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_PHONE_STATE}, CODIGO_SOLICITUD_PERMISO_RPS);
        }
    }*/


    /*
    * Comprobamos que los permisos han sido concedidos
    * */

    public boolean checarStatusPermiso(){

         boolean permisoReadCallLog = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG)
                 == PackageManager.PERMISSION_GRANTED;
         boolean permisoReadPhoneState = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                 == PackageManager.PERMISSION_GRANTED;
        boolean permisoReadContacts = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED;


         if(permisoReadCallLog && permisoReadPhoneState && permisoReadContacts) {
             return true;
         } else {
             return false;
         }

    }

    /*
    * Metodo onRequest
    * */

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode) {
            case CONTACTS_PERMISSION:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContacts();
                } else {
                    finish();
                }
                break;
            case CODIGO_SOLICITUD_PERMISO_RCL:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    consultarCPLlamadas();
                } else {
                    finish();
                }
                break;
            case CODIGO_SOLICITUD_PERMISO_RPS:
                break;
            case PHONE_PERMISSION:
            break;
        }
    }


    /*
    * Aqui revisamos la BD con el registro de llamadas
    * */

    public void consultarCPLlamadas(){


        Uri direccionUriLlamadas = CallLog.Calls.CONTENT_URI;

        //numero, fecha, duracion
        String[] campos = {
                CallLog.Calls.NUMBER,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION
        };

        ContentResolver contentResolver = getContentResolver();
        Cursor registros = contentResolver.query(
                direccionUriLlamadas, campos, null, null, CallLog.Calls.DATE + "ASC");

        if (saveLlamadas(registros, campos)){
            readRegistros();
        }

    }

    /*
    * Comenzamos la escritura y lectura del documento CSV
    * */

    private void readRegistros() {
        File f = new File(getExternalFilesDir(null),"RegistroLlamadas.csv");
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String linea;
            StringBuilder texto = new StringBuilder();
            while ((linea = br.readLine()) != null) {
                texto.append(linea);
                texto.append('\n');
            }
            textView.setText(texto);
            br.close();
        } catch(IOException e) {
        }
    }

    private boolean saveLlamadas (Cursor registros, String [] campos){
        boolean result=true;
        File f = new File(getExternalFilesDir(null),"RegistroLlamadas.csv");
        FileWriter fw = null;
        try {
            fw = new FileWriter(f);
            while (registros.moveToNext()){
                numero = registros.getString(registros.getColumnIndex(campos[0]));
                Long fecha = registros.getLong(registros.getColumnIndex(campos[1]));
                String duracion = registros.getString(registros.getColumnIndex(campos[2]));

                getContacts();
                RegistroLlamada= (String) DateFormat.format(
                        "yyyy;mm;dd k;mm",fecha) + ";" + duracion + ";" + numero + ";" + auxContacto;
                fw.write(RegistroLlamada + "\n");
            }
            fw.flush();
            fw.close();
        } catch (IOException e) {
            result=false;
        }
        return result;
    }

}