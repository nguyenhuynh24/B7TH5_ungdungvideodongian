package com.example.b7th5;



import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "VideoPlayer";
    private VideoView videoView;
    private EditText urlEditText;
    private Button pickVideoButton, playUrlButton;
    private static final int REQUEST_PERMISSION = 1;
    private static final int REQUEST_PICK_VIDEO = 2;
    private android.widget.MediaController mediaController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        videoView = findViewById(R.id.videoView);
        urlEditText = findViewById(R.id.urlEditText);
        pickVideoButton = findViewById(R.id.pickVideoButton);
        playUrlButton = findViewById(R.id.playUrlButton);

        // Khởi tạo MediaController
        mediaController = new android.widget.MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        if (!checkPermissions()) {
            requestPermissions();
        }

        pickVideoButton.setOnClickListener(v -> pickVideoFromStorage());
        playUrlButton.setOnClickListener(v -> playVideoFromUrl());
    }

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET
        }, REQUEST_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show();
        }
    }

    private void pickVideoFromStorage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_PICK_VIDEO);
    }

    private void playVideoFromUrl() {
        String url = urlEditText.getText().toString().trim();
        if (!url.isEmpty()) {
            try {
                Uri videoUri = Uri.parse(url);
                playVideo(videoUri);
            } catch (Exception e) {
                Log.e(TAG, "Invalid URL: " + url, e);
                Toast.makeText(this, "Invalid URL", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Please enter a URL", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_VIDEO && resultCode == RESULT_OK && data != null) {
            Uri videoUri = data.getData();
            if (videoUri != null) {
                playVideo(videoUri);
            } else {
                Toast.makeText(this, "Failed to get video URI", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void playVideo(Uri videoUri) {
        try {
            Log.d(TAG, "Playing video from URI: " + videoUri);
            videoView.setVideoURI(videoUri);
            videoView.setOnPreparedListener(mp -> {
                Log.d(TAG, "Video prepared, starting playback");
                videoView.start();
                Toast.makeText(this, "Playing video", Toast.LENGTH_SHORT).show();
            });
            videoView.setOnErrorListener((mp, what, extra) -> {
                String errorMsg;
                switch (what) {
                    case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                        errorMsg = "Unknown error";
                        break;
                    case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                        errorMsg = "Media server died";
                        break;
                    default:
                        errorMsg = "Error code: " + what;
                }
                switch (extra) {
                    case MediaPlayer.MEDIA_ERROR_IO:
                        errorMsg += " - I/O error";
                        break;
                    case MediaPlayer.MEDIA_ERROR_MALFORMED:
                        errorMsg += " - Malformed data";
                        break;
                    case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                        errorMsg += " - Unsupported format";
                        break;
                    case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                        errorMsg += " - Timed out";
                        break;
                    default:
                        errorMsg += " - Extra code: " + extra;
                }
                Log.e(TAG, "MediaPlayer error: " + errorMsg + " (what=" + what + ", extra=" + extra + ")");
                Toast.makeText(this, "Error playing video: " + errorMsg, Toast.LENGTH_LONG).show();
                return true;
            });
        } catch (Exception e) {
            Log.e(TAG, "Failed to set video URI: " + videoUri, e);
            Toast.makeText(this, "Failed to load video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView.isPlaying()) {
            videoView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoView.stopPlayback();
    }
}