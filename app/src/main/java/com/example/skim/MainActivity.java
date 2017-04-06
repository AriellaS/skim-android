package com.example.skim;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.view.*;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final FloatingActionButton cameraButton = (FloatingActionButton) findViewById(R.id.camera);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openCamera();
            }
        });
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MainActivity.this, CameraActivity.class);
        MainActivity.this.startActivityForResult(cameraIntent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (REQUEST_IMAGE_CAPTURE) : {
                if (resultCode == Activity.RESULT_OK) {
                    Bitmap photo = (Bitmap) data.getExtras().get("photo");
                    Intent searchIntent = new Intent(MainActivity.this, SearchActivity.class);
                    searchIntent.putExtra("photo", photo);
                    MainActivity.this.startActivity(searchIntent);
                }
                break;
            }
        }
    }
}