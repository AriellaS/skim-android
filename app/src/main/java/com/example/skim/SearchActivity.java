package com.example.skim;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {

    ArrayList<Word> foundWords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        ImageView view = (ImageView) findViewById(R.id.image);
        view.setImageBitmap((Bitmap) getIntent().getExtras().get("photo"));
        Bitmap photo = ((BitmapDrawable) view.getDrawable()).getBitmap();
        new APIRequest().execute(photo);
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
                System.out.println(json);
                JSONObject regions = json.getJSONObject("regions");

                for (int region = 0; region < regions.length(); region++) {
                    JSONObject lines = regions.getJSONObject("lines");
                    for (int line = 0; line < lines.length(); line++) {
                        JSONArray words = lines.getJSONArray("words");
                        for (int word = 0; word < words.length(); word++) {
                            JSONObject foundWord = words.getJSONObject(word);
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
