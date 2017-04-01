package com.vinylplayer.niek.vinylspeler;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity
        implements SpotifyPlayer.NotificationCallback, ConnectionStateCallback, BluetoothLeUart.Callback {

    private static final String CLIENT_ID = "f93d2ff389604712ac75b53e739a1a02";
    private static final String REDIRECT_URI = "vinyl://spotify";
    private static final int REQUEST_CODE = 1337;
    private Player mPlayer;

    private TextView statusText;
    private Button songOne;
    private Button songTwo;
    private Button spotifyButton;
    private Button uartButton;
    private BluetoothLeUart uart;
    private AuthenticationRequest request;
    private MainActivity that;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        request = builder.build();

        setContentView(R.layout.activity_main);
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
        requestPermissions(permissions, 1);
        uart = new BluetoothLeUart(getApplicationContext());
        that = this;

        setUI();

    }

    protected void setUI() {
        statusText = (TextView) findViewById(R.id.status_TextView);
        songOne = (Button) findViewById(R.id.song_one_button);
        songTwo = (Button) findViewById(R.id.song_two_button);
        spotifyButton = (Button) findViewById(R.id.spotify_button);
        uartButton = (Button) findViewById(R.id.uartbutton);

        songOne.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mPlayer.playUri(null, "spotify:track:5C0LFQARavkPpn7JgA4sLk", 0, 0);
            }
        });

        songTwo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mPlayer.playUri(null, "spotify:track:3FmAUR4SPWa3P1KyDf21Fu", 0, 0);
            }
        });

        spotifyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AuthenticationClient.openLoginActivity(that, REQUEST_CODE, request);
            }
        });

        uartButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("MainActivity", "Scanning for devices ...");
                uart.registerCallback(that);
                uart.connectFirstAvailable();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("MainActivity", "Disconnecting ...");
        uart.unregisterCallback(this);
        uart.disconnect();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
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
    public void onLoggedIn() {
        Log.d("MainActivity", "User logged in");
        songOne.setEnabled(true);
        songTwo.setEnabled(true);
        statusText.setText("Logged in!");
    }

    @Override
    public void onLoggedOut() {
        Log.d("MainActivity", "User logged out");
        songOne.setEnabled(false);
        songTwo.setEnabled(false);
        statusText.setText("Logged out!");
    }

    @Override
    public void onLoginFailed(Error error) {
        Log.d("MainActivity", "Login failed");
    }

    @Override
    public void onTemporaryError() {
        Log.d("MainActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String s) {
        Log.d("MainActivity", "Received connection message: " + s);
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        Log.d("MainActivity", "Playback event received: " + playerEvent.name());
        switch (playerEvent) {
            case kSpPlaybackEventAudioFlush:
                break;
            case kSpPlaybackNotifyBecameActive:
                statusText.setText("Initialized!");
                break;
            case kSpPlaybackNotifyTrackChanged:
                statusText.setText("Playing new track!");
                break;
            // Handle event type as necessary
            default:
                break;
        }
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
    public void onConnected(BluetoothLeUart uart) {
        Log.d("MainActivity", "Connected to Uart device");
    }

    @Override
    public void onConnectFailed(BluetoothLeUart uart) {
        Log.d("MainActivity", "Connection failed to Uart device");
    }

    @Override
    public void onDisconnected(BluetoothLeUart uart) {
        Log.d("MainActivity", "Disconnected from Uart device");
    }

    @Override
    public void onReceive(BluetoothLeUart uart, BluetoothGattCharacteristic rx) {
        Log.d("MainActivity", rx.getStringValue(0));
        switch(rx.getStringValue(0)){
            case "1":
                mPlayer.playUri(null, "spotify:track:5C0LFQARavkPpn7JgA4sLk", 0, 0);
                break;
            case "2":
                mPlayer.playUri(null, "spotify:track:3FmAUR4SPWa3P1KyDf21Fu", 0, 0);
                break;
            default:
                break;
        }
    }

    @Override
    public void onDeviceFound(BluetoothDevice device) {
        Log.d("MainActivity", device.getAddress());
    }

    @Override
    public void onDeviceInfoAvailable() {
        Log.d("MainActivity", uart.getDeviceInfo());
    }
}