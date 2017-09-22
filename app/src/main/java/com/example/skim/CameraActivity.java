package com.example.skim;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraActivity extends Activity {
    private static final int REQUEST_TAKE_PHOTO = 5;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            dispatchTakePhotoIntent();
        } catch (IOException e) {};
    }

    Uri uri;
    private void dispatchTakePhotoIntent() throws IOException {
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePhotoIntent.resolveActivity(getPackageManager()) != null) {
            File file = File.createTempFile(
                    new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + "_",
                    ".jpg",
                    getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            );
            if (file != null) {
                uri = Uri.fromFile(file);
                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                startActivityForResult(takePhotoIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case (REQUEST_TAKE_PHOTO) : {
                if (resultCode == Activity.RESULT_OK) {
                    setResult(Activity.RESULT_OK, new Intent().putExtra("imageUri", uri));
                    finish();
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    setResult(Activity.RESULT_CANCELED, new Intent());
                    finish();
                }
                break;
            }
        }
    }
}
