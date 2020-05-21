package com.vertin_go.myapplication;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.telephony.SmsMessage;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class MonService extends Service implements OnInitListener
{

    Receiver receiver = new Receiver();
    private TextToSpeech mTTS;
    private String strPhrase1;
    private String strPhrase2 = "Lecture du message";
    private String strNomContact;
    private ArrayList<Appelant> liste;

    public class Receiver extends BroadcastReceiver
    {

        @Override
        public void onReceive(Context context, Intent intent)
        {

			/*Initialisation broadcastReceiver
			 *
			 * C'est au moment ou listener d'android recoit un SMS que l'action s'effectue
			*/

//			Je crée un intent qui va récupérer le fournisseur de service qui gère la réception des SMS

            if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
                Bundle bundle = intent.getExtras();										// Je récupère les données de l'intent qui vient de d'arriver avec le SMS
                if(bundle != null){
                    Object[] pdus = (Object[]) bundle.get("pdus");						// Je récupère tous les messages bruts de la collection dans un tableau
                    SmsMessage[] messages = new SmsMessage[pdus.length];             // çà me donne un tableau en deux dimensions

                    for(int i=0;i<pdus.length;i++)
                    {
                        if(Build.VERSION.SDK_INT >= 19)
                        { //KITKAT
                            SmsMessage[] msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent);
                            messages[i] = msgs[0];
                        }
                        else
                        {
                            messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                        }

                        for(SmsMessage message : messages)
                        {
                            chercherContacts(message.getOriginatingAddress());			/* Pour chaque message que je lis, je cherche les infos du sender et je les
																							place dans la liste et je teste
																						*/
                            if(liste.size() > 0)
                            {
                                for(Appelant ap : liste)
                                {								// si le sender du message existe dans le phone alors je récupère son nom
                                    strNomContact = ap.getoNom();
                                }
                                liste.clear();
                            }
                            else
                            {
                                strNomContact = message.getOriginatingAddress();		// sinon, je lis le numéro de téléphone simplement
                            }

                            mTTS.setLanguage(Locale.FRENCH);							// je choisis la langue de lecture du message et je lis le message
                            final String strPhrase = strPhrase1 + " de " + MonService.this.getStrNomContact() + "..." +
                                    strPhrase2 + "... " + message.getMessageBody() + "... fin du message";


                            mTTS=new TextToSpeech(MonService.this, new TextToSpeech.OnInitListener()
                            {
                                @Override
                                public void onInit(int status)
                                {
                                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                                    {
                                        mTTS.speak(strPhrase,TextToSpeech.QUEUE_FLUSH,null,null);
                                    }
                                    else
                                    {
                                        mTTS.speak(strPhrase, TextToSpeech.QUEUE_FLUSH, null);
                                    }

                                }
                            });

                          /*  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                mTTS.speak(strPhrase,TextToSpeech.QUEUE_FLUSH,null,null);
                            } else {
                                mTTS.speak(strPhrase, TextToSpeech.QUEUE_FLUSH, null);
                            }*/
                        }
                    }
                }
            }
        }
    }


    public void onCreate()
    {
        mTTS = new TextToSpeech(this, this);
    }

    @Override
    public void onStart(Intent intent, int startId)
    {

    }

    private IBinder mBinder = new MonServiceBinder();

    @Override
    public IBinder onBind(Intent intent)
    {
        return mBinder;
    }

    public void lancerService()
    {
        Toast.makeText(MonService.this, "Activation du service", Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "Service activé", Toast.LENGTH_SHORT).show();
        registerReceiver(receiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));

        mTTS=new TextToSpeech(MonService.this, new TextToSpeech.OnInitListener()
        {
            @Override
            public void onInit(int status)
            {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                {
                    mTTS.speak("Activation du service",TextToSpeech.QUEUE_FLUSH,null,null);
                }
                else
                {
                    mTTS.speak("Activation du service", TextToSpeech.QUEUE_FLUSH, null);
                }

            }
        });
    }

    public void arreterService()
    {
        Intent intent = new Intent(this, MonService.class);
        stopService(intent);
        this.mBinder = null;
        mTTS.stop();
        mTTS.shutdown();
        unregisterReceiver(receiver);
        Toast.makeText(this, "Le service est arrêté", Toast.LENGTH_LONG).show();
    }

    public class MonServiceBinder extends Binder
    {
        MonService getService(){
            return MonService.this;
        }
    }

    @Override
    public void onInit(int status)
    {
        if(status == TextToSpeech.SUCCESS)
        {
            Toast.makeText(this, "Initialisation du service", Toast.LENGTH_SHORT);
            Toast.makeText(this, "Service initialisé", Toast.LENGTH_LONG).show();
        }
        else
        {
            Toast.makeText(this, "Une erreur est survenue !", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroy()
    {
        Toast.makeText(this, "Le service est arrêté", Toast.LENGTH_LONG).show();
        mTTS.stop();
        mTTS.shutdown();
        if(receiver != null)
            unregisterReceiver(receiver);
    }

    //  Accesseurs pour l'attribut strPhrase
    public String getStrPhrase() {
        return strPhrase1;
    }

    public void setStrPhrase(String strPhrase) {
        this.strPhrase1 = strPhrase;
    }

//	Accesseurs pour l'attribut nom qui me servira à récupérer le cas échéant le nom
//	du contact

    public String getStrNomContact() {
        return strNomContact;
    }

    public void setStrNomContact(String strNomContact) {
        this.strNomContact = strNomContact;
    }
//	*************Fin des accesseurs**********

    private void chercherContacts(String pNumero)
    {

//		Méthode servant à trouver le nom du contact qui envoie le message s'il existe dans notre répertoire

//		Liste servant à stoquer la liste des contacts du phone
        liste = new ArrayList<Appelant>();
        Cursor cInfosContacts = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

//		Je boucle sur la collection de contacts et je récupère l'id du contact, son nom et son numéro (s'ils existent)
//		pour faire la correspondance.

        while (cInfosContacts.moveToNext())
        {
            String contactId = cInfosContacts.getString(cInfosContacts.getColumnIndex(ContactsContract.Contacts._ID));
            String nomContact = cInfosContacts.getString(cInfosContacts.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
            String phoneExist = cInfosContacts.getString(cInfosContacts.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

            if((contactId != null) && (nomContact != null) && (phoneExist != null))
            {
                if(phoneExist.equalsIgnoreCase("1"))
                {
                    phoneExist = "true";
                }
                else
                    phoneExist = "false" ;

                if(Boolean.parseBoolean(phoneExist))
                {
                    Cursor numContacts = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ contactId,null, null);

                    while (numContacts.moveToNext())
                    {
                        String lNumero = numContacts.getString(numContacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                        if(lNumero.equals(pNumero))
                        {
                            Appelant ap = new Appelant();
                            ap.setoNom(nomContact);
                            ap.setoNum(pNumero);
                            liste.add(ap);
                        }
                    }
                    numContacts.close();
                }
            }
        }
        cInfosContacts.close();
    }

}
