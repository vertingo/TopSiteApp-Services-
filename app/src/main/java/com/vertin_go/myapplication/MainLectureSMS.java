package com.vertin_go.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class MainLectureSMS extends Activity implements OnClickListener {

    private MonService mMonService ;
    private EditText edtPhrase ;
    private Button btnDemarrer ;
    private Button btnStoper ;
    private String strTexte ;
    private TextToSpeech ts;

    /** Called when the activity is first created. */

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_lecture_sms);

//        Au démarrage, je charge la vue principale avec ses éléments

        Intent intentAssoc = new Intent(this, MonService.class);

        if(bindService(intentAssoc, mConnexion, Context.BIND_AUTO_CREATE))
        {
            btnDemarrer = (Button)findViewById(R.id.btnActiver);
            btnStoper = (Button)findViewById(R.id.btnArreter);
            edtPhrase = (EditText)findViewById(R.id.edtPhrase);
            btnDemarrer.setOnClickListener(this);
            btnStoper.setOnClickListener(this);
            edtPhrase.setEnabled(false);
        }
    }


    @Override
    public void onClick(View v)
    {

//		En fonction du bouton sur lequel on a cliqué on choisit un ensemble d'actions

        switch (v.getId())
        {
            case R.id.btnActiver:				// Si le user clique sur le bouton "activer" sans avoir saisi une phrase une erreur est signalée
                edtPhrase.setEnabled(false);
                strTexte = edtPhrase.getText().toString().trim();

                if(strTexte.equals(""))
                {
                    new AlertDialog.Builder(this).setTitle("Erreur")
                            .setMessage("Ce champ ne doit pas être vide!")
                            .setPositiveButton("OK", null).show();
                    edtPhrase.setEnabled(true);
                    edtPhrase.requestFocus();
                }
                else
                {										// Sinon, je ne laisse au user que la possibilité de désactiver le service
                    mMonService.setStrPhrase(strTexte);
                    mMonService.lancerService();			// Et je lance le service.
                    btnStoper.setEnabled(true);
                    btnDemarrer.setEnabled(false);
                }
                break;
            case R.id.btnArreter:							// S'il clique sur "arreter" j'arrete le service, je vide le champ
                mMonService.arreterService();
                edtPhrase.setEnabled(true);
                edtPhrase.setText("");
                strTexte = "";
                edtPhrase.requestFocus();
                btnDemarrer.setEnabled(true);
                btnStoper.setEnabled(false);
            default:
                break;
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        if(strTexte != null)
        {
            if(strTexte.equals(""))
            {
                edtPhrase.setEnabled(true);
                btnDemarrer.setEnabled(true);
                btnStoper.setEnabled(false);
            }
            else
            {
                edtPhrase.setEnabled(false);
                btnDemarrer.setEnabled(false);
                btnStoper.setEnabled(true);
            }
        }
        else
        {
            edtPhrase.setEnabled(true);
            btnDemarrer.setEnabled(true);
            btnStoper.setEnabled(false);
        }
    }

    @Override
    public void onBackPressed(){
        moveTaskToBack(true);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

//    	A l'arret du service je romp la connexion entre l'activité et le service

        unbindService(mConnexion);
    }

//    Initialisation de la variable de connexion entre l'activité et le service

    private ServiceConnection mConnexion = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service)
        {
            mMonService = ((MonService.MonServiceBinder)service).getService();
        }
        public void onServiceDisconnected(ComponentName className){
            mMonService = null;
        }
    };
}