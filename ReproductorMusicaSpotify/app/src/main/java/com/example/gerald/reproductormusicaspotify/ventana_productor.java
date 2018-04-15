package com.example.gerald.reproductormusicaspotify;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ventana_productor extends AppCompatActivity {

    private String uri;
    private String nombre_cancion;
    private boolean playing_music;
    private MediaPlayer media;
    private Button button_play;
    private Button button_back;
    private Button button_next;
    private SeekBar progreso;
    private SeekBar volumen;
    private AudioManager audioManager;
    private Handler duracionHandler = new Handler();
    private int posicion_actual;

    private TextView mostrar_nombre;

    private int posicion_cancion;

    public int cancion_actual;

    private final Player.OperationCallback mOperationCallback = new Player.OperationCallback() {
        @Override
        public void onSuccess() {
        }

        @Override
        public void onError(Error error) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ventana_productor);

        mostrar_nombre =findViewById(R.id.textViewCancion);

        posicion_actual = 0;

        cancion_actual = 0;
        Intent intent = getIntent();

        uri = intent.getStringExtra(MainActivity.URI);
        nombre_cancion = intent.getStringExtra(MainActivity.CANCION);
        posicion_cancion = Integer.parseInt(intent.getStringExtra(MainActivity.POSICION));

        playing_music = false;

        button_play = findViewById(R.id.button);

        button_play.setBackgroundResource(R.drawable.play_icon);

        button_back = findViewById(R.id.button_back);

        button_back.setBackgroundResource(R.drawable.anterior_icon);

        button_next = findViewById(R.id.button_next);

        button_next.setBackgroundResource(R.drawable.siguiente_icon);

        progreso = findViewById(R.id.seekBarProgreso);
        volumen = findViewById(R.id.seekBarVolumen);

        try {
            inicializar_cancion();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        cambioVolumen();

        cambiarProgreso();
        //mostrar_nombre.setText((CharSequence) MainActivity.mPlayer.getMetadata().toString());

    }

    public void onDestroy() {
        super.onDestroy();
        MainActivity.mPlayer.pause(mOperationCallback);

    }

    public void onClickedPlay(View view){
        if(!playing_music){
            MainActivity.mPlayer.resume(mOperationCallback);
            playing_music = true;
            button_play.setBackgroundResource(R.drawable.pause_icon);
            duracionHandler.postDelayed(actualizarSeekBarProgreso, 100);
        }else{
            MainActivity.mPlayer.pause(mOperationCallback);
            playing_music = false;
            button_play.setBackgroundResource(R.drawable.play_icon);
        }

    }

    private void cambioVolumen()
    {
        try
        {
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            volumen.setMax(audioManager
                    .getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            volumen.setProgress(audioManager
                    .getStreamVolume(AudioManager.STREAM_MUSIC));

            volumen.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
            {
                @Override
                public void onStopTrackingTouch(SeekBar arg0)
                {
                }

                @Override
                public void onStartTrackingTouch(SeekBar arg0)
                {
                }

                @Override
                public void onProgressChanged(SeekBar arg0, int progress, boolean arg2)
                {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,progress, 0);
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void cambiarProgreso(){
        progreso.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onStopTrackingTouch(SeekBar arg0)
            {
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0)
            {
            }

            @Override
            public void onProgressChanged(SeekBar arg0, int progress, boolean arg2)
            {
                if(arg2){
                    MainActivity.mPlayer.seekToPosition(mOperationCallback,progress);
                }
            }
        });
    }

    private Runnable actualizarSeekBarProgreso = new Runnable() {
        public void run() {

            posicion_actual = (int) (MainActivity.mPlayer.getPlaybackState().positionMs);

            progreso.setProgress(posicion_actual);


            duracionHandler.postDelayed(this, 50);
        }
    };

    private void inicializar_cancion() throws InterruptedException {
        MainActivity.mPlayer.playUri(null, uri, cancion_actual, 0);
        //MainActivity.mPlayer.playUri();
        //Uri myUri = Uri.parse(path);
        //String nombre_cancion_actual = MainActivity.mPlayer.getMetadata().contextName;
        TimeUnit.MILLISECONDS.sleep(500);

        playing_music = true;
        button_play.setBackgroundResource(R.drawable.pause_icon);
        duracionHandler.postDelayed(actualizarSeekBarProgreso, 100);

        progreso.setMax((int)MainActivity.mPlayer.getMetadata().currentTrack.durationMs);

        mostrar_nombre.setText((CharSequence) MainActivity.mPlayer.getMetadata().currentTrack.name);

    }

    public void siguiente_cancion(View view) throws InterruptedException {

        if(cancion_actual+1 < MainActivity.lista_canciones.get(posicion_cancion).cantidadCanciones){
            //MainActivity.mPlayer.pause(mOperationCallback);
            cancion_actual++;
            //inicializar_cancion();
            MainActivity.mPlayer.skipToNext(mOperationCallback);
            TimeUnit.MILLISECONDS.sleep(500);
            progreso.setMax((int)MainActivity.mPlayer.getMetadata().currentTrack.durationMs);
            mostrar_nombre.setText((CharSequence) MainActivity.mPlayer.getMetadata().currentTrack.name);
        }

    }

    public void anterior_cancion(View view) throws InterruptedException {
        //MainActivity.mPlayer.skipToPrevious(mOperationCallback);
        if(cancion_actual > 0){
            //.mPlayer.pause(mOperationCallback);
            cancion_actual--;
            //inicializar_cancion();
            MainActivity.mPlayer.skipToPrevious(mOperationCallback);
            TimeUnit.MILLISECONDS.sleep(500);
            progreso.setMax((int)MainActivity.mPlayer.getMetadata().currentTrack.durationMs);
            mostrar_nombre.setText((CharSequence) MainActivity.mPlayer.getMetadata().currentTrack.name);
        }

    }
}
