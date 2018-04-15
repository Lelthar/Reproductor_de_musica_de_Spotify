package com.example.gerald.reproductormusicaspotify;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Metadata;
import com.spotify.sdk.android.player.PlaybackState;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity implements
    SpotifyPlayer.NotificationCallback, ConnectionStateCallback{
    public static ArrayList<Cancion> lista_canciones = new ArrayList<>();
    public static ArrayList<String> lista_nombres = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private ListView listView;
    public static final String URI  = "uri";
    public static final String CANCION  = "nombreCancion";
    public static final String POSICION  = "posicion_cancion";
    public static final String MAXIMO  = "maximo_canciones";

    // TODO: Replace with your client ID
    private static final String CLIENT_ID = "1168c317cc684e97afdfc5eeb672a3d0";

    // TODO: Replace with your redirect URI
    private static final String REDIRECT_URI = "http://reproductor_musica_spotify_reque.com/callback/";

    public static Player mPlayer;

    private static final int REQUEST_CODE = 1337;

    public String token_acceso = "";

    public EditText editText;

    public static DownLoadTask downLoadTask;

    public String string_texto = "";

    public String consulta_parametros = "";

    public static PlaybackState mCurrentPlaybackState;

    public static Metadata mMetadata;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);

        editText = findViewById(R.id.editText);

        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);

        // The only thing that's different is we added the 5 lines below.

    }

    public class DownLoadTask extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... strings) {
            String xmlString;
            HttpURLConnection urlConnection = null;
            URL url = null;
            try {
                url = new URL("https://api.spotify.com/v1/search?q=%22"+consulta_parametros+"%22&type=playlist");

                urlConnection = (HttpURLConnection)url.openConnection();
                //urlConnection.setRequestProperty("q","leo jimenez");
                //urlConnection.setRequestProperty("type","playlist");
                urlConnection.setRequestProperty("Accept","application/json");
                urlConnection.setRequestProperty("Content-Type","application/json");
                urlConnection.setRequestProperty ("Authorization", "Bearer "+token_acceso);
                urlConnection.setRequestMethod("GET");

                if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    StringBuilder xmlResponse = new StringBuilder();
                    BufferedReader input = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String strLine = null;
                    while ((strLine = input.readLine()) != null) {
                        xmlResponse.append(strLine);
                    }
                    xmlString = xmlResponse.toString();
                    input.close();
                    return xmlString;

                }else{
                    return "Error1";
                }
            }
            catch (Exception e) {
                return "error2";
            }
            finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }
    }

    public void buscarOnclicked(View view) throws ExecutionException, InterruptedException, JSONException {
        if(!editText.getText().equals("")){
            lista_canciones.clear();
            lista_nombres.clear();
            consulta_parametros = convertir_busque_consulta(editText.getText().toString());
            //Log.d("Conv",consulta_parametros);
            downLoadTask = new DownLoadTask();
            string_texto = downLoadTask.execute().get();
            JSONObject reader = new JSONObject(string_texto);

            JSONObject playlist = reader.getJSONObject("playlists");

            JSONArray items = playlist.getJSONArray("items");

            //TextView textView2 = findViewById(R.id.textView2);


            for (int i = 0; i < items.length(); i++){
                JSONObject elemento = items.getJSONObject(i);
                //textView2.setText(elemento.getString("name"));
                JSONObject cantidad_item = elemento.getJSONObject("tracks");

                Cancion nueva_cancion = new Cancion(elemento.getString("name"),Integer.parseInt(cantidad_item.getString("total")),elemento.getString("uri"));
                lista_canciones.add(nueva_cancion);

                lista_nombres.add(elemento.getString("name"));
            }
            //Log.d("XML",string_texto);

            consulta_parametros = "";

            adapter = new ArrayAdapter<String>(this,R.layout.list_item,R.id.txtitem,lista_nombres);
            listView.setAdapter(adapter);



            listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    reproducirCancion(position);

                }

            });
        }

    }

    public String convertir_busque_consulta(String palabra){
        String resultado = "";
        for(int i = 0; i < palabra.length(); i++){
            if(palabra.charAt(i) == ' '){
                resultado += "%20";
            }else{
                resultado += palabra.charAt(i);
            }
        }
        return resultado;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);


        // Check if result comes from the correct activity
        // The next 19 lines of the code are what you need to copy & paste! :)
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
                token_acceso = response.getAccessToken();
                //textView.setText(token_acceso);


                Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
                    @Override
                    public void onInitialized(SpotifyPlayer spotifyPlayer) {
                        mPlayer = spotifyPlayer;
                        mPlayer.addConnectionStateCallback(MainActivity.this);
                        mPlayer.addNotificationCallback(MainActivity.this);

                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
                    }
                });
            }
        }
    }


    @Override
    protected void onDestroy() {
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        Log.d("MainActivity", "Playback event received: " + playerEvent.name());
        //mCurrentPlaybackState = mPlayer.getPlaybackState();
        //mMetadata = mPlayer.getMetadata();

    }

    @Override
    public void onPlaybackError(Error error) {
    Log.d("MainActivity", "Playback error received: " + error.name());
    switch (error) {
        // Handle error type as necessary
        default:
            break;
    }
    }

    @Override
    public void onLoggedIn() {
        Log.d("MainActivity", "User logged in");

        // This is the line that plays a song.
        //mPlayer.playUri(null, "spotify:user:12141322662:playlist:6PVeRJOHGP4HSEkve56hBC", 1, 0);
        //mPlayer.pl
        //mPlayer.pause(mOperationCallback);
    }


    @Override
        public void onLoggedOut() {
        Log.d("MainActivity", "User logged out");
    }

        @Override
        public void onLoginFailed(Error var1) {
        Log.d("MainActivity", "Login failed");
    }

        @Override
        public void onTemporaryError() {
        Log.d("MainActivity", "Temporary error occurred");
    }

        @Override
        public void onConnectionMessage(String message) {
        Log.d("MainActivity", "Received connection message: " + message);
    }

    public void logoutOnClicked(View view){
        mPlayer.logout();

    }

    public void reproducirCancion(int posicion){
        Cancion reproducto_cancion = lista_canciones.get(posicion);
        String texto1 = reproducto_cancion.uri;
        String texto2 = reproducto_cancion.nombre;
        Intent intent = new Intent(this,ventana_productor.class);
        intent.putExtra(URI,texto1);
        intent.putExtra(CANCION,texto2);
        intent.putExtra(POSICION,Integer.toString(posicion));
        startActivity(intent);

    }
}
