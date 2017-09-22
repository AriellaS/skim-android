package com.example.skim;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {

    ArrayList<Word> foundWords;
    EditText text;
    Bitmap photo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        final FloatingActionButton cancelButton = (FloatingActionButton) findViewById(R.id.cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        ImageView view = (ImageView) findViewById(R.id.image);
        
        text = (EditText) findViewById(R.id.text);
        text.setFocusable(false);
        text.setFocusableInTouchMode(false);
        text.setTextColor(Color.LTGRAY);
        text.setText("Loading...");

        Uri imageUri = (Uri) getIntent().getExtras().get("imageUri");
        try {
            photo = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri).copy(Bitmap.Config.ARGB_8888, true);
        } catch (IOException e) {};
        view.setImageBitmap(photo);

        new APIRequest().execute(photo);
    }

    private class APIRequest extends AsyncTask<Bitmap, ArrayList<Word>, ArrayList<Word>> {

        @Override
        protected ArrayList<Word> doInBackground(Bitmap... params) {
            ArrayList<Word> result = new ArrayList<Word>();
            try {
                URL url = new URL("https://westus.api.cognitive.microsoft.com/vision/v1.0/ocr?&detectOrientation=true");
                HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
                httpCon.setRequestProperty("Ocp-Apim-Subscription-Key", "6d0796ff5ad645e19d7d94f1e0833c35");
                httpCon.setRequestProperty("Content-Type", "application/octet-stream");
                httpCon.setDoOutput(true);
                httpCon.setRequestMethod("POST");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                params[0].compress(Bitmap.CompressFormat.JPEG, 60, baos);
                baos.writeTo(httpCon.getOutputStream());

                System.out.println(httpCon.getResponseCode());
                System.out.println(httpCon.getResponseMessage());

                ByteArrayOutputStream output = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length;
                while ((length = httpCon.getInputStream().read(buffer)) != -1) {
                    output.write(buffer, 0, length);
                }
                String str = output.toString("UTF-8");

                JSONObject json = new JSONObject(str);
                if (((JSONArray) json.get("regions")).length() <= 0) {
                    return null;
                }

                JSONArray regions = (JSONArray) json.get("regions");
                for (int region = 0; region < regions.length(); region++) {
                    JSONObject regionObject = (JSONObject) regions.get(region);
                    JSONArray lines = (JSONArray) regionObject.get("lines");
                    for (int line = 0; line < lines.length(); line++) {
                        JSONObject lineObject = (JSONObject) lines.get(line);
                        JSONArray words = (JSONArray) lineObject.get("words");
                        for (int word = 0; word < words.length(); word++) {
                            JSONObject wordObject =(JSONObject) words.get(word);
                            result.add(new Word(wordObject.get("text").toString(), wordObject.get("boundingBox").toString()));
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
            if (result == null) {
                showAlert();
                text.setText("No text found.");
            } else {
                foundWords = result;

                text.setFocusable(true);
                text.setFocusableInTouchMode(true   );
                text.setTextColor(Color.DKGRAY);
                text.setText("");

                text.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void afterTextChanged(Editable s) {
                        findWords();
                    }
                });
            }
        }

        private void findWords() {
            for (int word = 0; word < foundWords.size(); word++) {
                if (foundWords.get(word).text.equals(text.getText().toString())) {
                    System.out.println(foundWords.get(word).text);
                    drawBox(foundWords.get(word));
                }
            }
        }

        private void drawBox(Word word) {
            Paint paint = new Paint();
            paint.setColor(Color.rgb(0, 0, 0));
            paint.setStrokeWidth(10);
            paint.setStyle(Paint.Style.STROKE);
            new Canvas(photo).drawRect(word.x, word.y, word.x+word.width, word.y+word.height, paint);
        }

    }

    private void showAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(SearchActivity.this).create();
        alertDialog.setTitle("Oh no!");
        alertDialog.setMessage("Skim was unable to find any text in this image.");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }
}
