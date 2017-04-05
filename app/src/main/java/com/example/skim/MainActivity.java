package com.example.skim;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;
import android.content.Intent;
import android.provider.MediaStore;
import android.widget.*;
import android.view.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    ArrayList<Word> foundWords;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_camera:
                    return true;
                case R.id.navigation_gallery:
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        final Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openCamera();
            }
        });

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        EditText text = new EditText(getApplicationContext());

    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MainActivity.this, CameraActivity.class);
        MainActivity.this.startActivityForResult(cameraIntent, 1);

        /*File file = null;
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            file = createImageFile();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (file != null) {
            Uri uri = Uri.fromFile(file);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            startActivity(intent);
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            new Requests().execute(file);

            ImageView imageView = (ImageView) findViewById(R.id.imageDisplay);
            System.out.println(filePath);
            Bitmap bitmap = BitmapFactory.decodeFile(filePath);
            imageView.setImageBitmap(bitmap);

            Paint paint = new Paint();
            Bitmap mutable = bitmap.copy(Bitmap.Config.ARGB_8888, true);
//            Canvas canvas = new Canvas(mutable);
            System.out.println(matched);

        } */
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    String filePath;

    private File createImageFile() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "image" + timestamp;
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(fileName, ".jpg", dir);
        filePath = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (1) : {
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

    private class APIRequest extends AsyncTask<Bitmap, ArrayList<Word>, ArrayList<Word>> {

        @Override
        protected ArrayList<Word> doInBackground(Bitmap... params) {
            ArrayList<Word> result = new ArrayList<Word>();
            try {
                URL url = new URL("https://westus.api.cognitive.microsoft.com/vision/v1.0/ocr?&detectOrientation=true");
                HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
                httpCon.setRequestProperty("Ocp-Apim-Subscription-Key", "c0081ca3a4c64ea99cfd392ddd0f25d6");
                httpCon.setRequestProperty("Content-Type", "application/octet-stream");
                httpCon.setDoOutput(true);
                httpCon.setRequestMethod("POST");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                //baos.write(new FileInputStream(params[0]).read());
                params[0].compress(Bitmap.CompressFormat.JPEG, 60, baos);
                baos.writeTo(httpCon.getOutputStream());
                System.out.println(httpCon.getResponseCode());
                System.out.println(httpCon.getResponseMessage());
                //System.out.println(httpCon.getInputStream().read());
                InputStreamReader reader = new InputStreamReader(httpCon.getInputStream(), "UTF-8");
                String str = "";
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();

                int thing = 0;
                while (thing != -1) {
                    //System.out.println(reader.read());
                    thing = reader.read();
                    buffer.write(thing);
                }
                baos.close();

                    JSONObject json = new JSONObject(buffer.toString());
                    JSONObject regions  = ((JSONObject) json.get("regions"));

                    for (int region = 0; region < regions.length(); region++) {
                        JSONObject lines = (JSONObject) regions.get("lines");
                        for (int line = 0; line < lines.length(); line++) {
                            JSONArray words = (JSONArray) lines.get("words");
                            for (int word = 0; word < words.length(); word++) {
                                JSONObject foundWord = (JSONObject) words.get(word);
                                result.add(new Word(foundWord.get("text").toString(), foundWord.get("boundingBox").toString()));
                            }
                        }
                    }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(ArrayList<Word> result) {
            foundWords = result;
        }

    }
}