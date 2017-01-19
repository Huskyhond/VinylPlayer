package com.vinylplayer.niek.vinylspeler;

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
        implements SpotifyPlayer.NotificationCallback, ConnectionStateCallback {

    private static final String CLIENT_ID = "f93d2ff389604712ac75b53e739a1a02";
    private static final String REDIRECT_URI = "vinyl://spotify";
    private static final int REQUEST_CODE = 1337;
    private Player mPlayer;

    private TextView statusText;
    private Button songOne;
    private Button songTwo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);

        setContentView(R.layout.activity_main);

        setUI();
    }

    protected void setUI() {
        statusText = (TextView) findViewById(R.id.status_TextView);
        songOne = (Button) findViewById(R.id.song_one_button);
        songTwo = (Button) findViewById(R.id.song_two_button);

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
}