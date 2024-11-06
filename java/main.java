package com.emojify;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView myTextView = findViewById(R.id.myTextView);
        final SpannableString spannable = new SpannableString(myTextView.getText().toString());
        final float textSize = myTextView.getTextSize();

        Pattern pattern = Pattern.compile("\\[moji:([a-zA-Z0-9]+|http[s]?://[^\\s]+)]");
        Matcher matcher = pattern.matcher(spannable);

        while (matcher.find()) {
            final String emoji = matcher.group(1);
            final int start = matcher.start();
            final int end = matcher.end();

            if (emoji.startsWith("http")) {
                int placeholderResId = getResources().getIdentifier("placeholder", "drawable", getPackageName());
                if (placeholderResId != 0) {
                    Drawable placeholderDrawable = getResources().getDrawable(placeholderResId, getTheme());
                    int width = (int) textSize;
                    int height = (int) textSize;
                    placeholderDrawable.setBounds(0, 0, width, height);
                    ImageSpan placeholderSpan = new ImageSpan(placeholderDrawable, ImageSpan.ALIGN_BOTTOM);
                    spannable.setSpan(placeholderSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    myTextView.setText(spannable);
                }

                new AsyncTask<Void, Void, Bitmap>() {
                    @Override
                    protected Bitmap doInBackground(Void... params) {
                        try {
                            HttpURLConnection connection = (HttpURLConnection) new URL(emoji).openConnection();
                            connection.setDoInput(true);
                            connection.connect();
                            InputStream input = connection.getInputStream();
                            return BitmapFactory.decodeStream(input);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return null;
                        }
                    }

                    @Override
                    protected void onPostExecute(Bitmap bitmap) {
                        if (bitmap == null) {
                            int placeholderResId = getResources().getIdentifier("placeholder", "drawable", getPackageName());
                            if (placeholderResId != 0) {
                                Drawable placeholderDrawable = getResources().getDrawable(placeholderResId, getTheme());
                                int width = (int) textSize;
                                int height = (int) textSize;
                                placeholderDrawable.setBounds(0, 0, width, height);
                                ImageSpan placeholderSpan = new ImageSpan(placeholderDrawable, ImageSpan.ALIGN_BOTTOM);
                                spannable.setSpan(placeholderSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                myTextView.setText(spannable);
                            }
                            return;
                        }

                        Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                        int width = (int) textSize;
                        int height = (int) textSize;
                        drawable.setBounds(0, 0, width, height);
                        ImageSpan imageSpan = new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM);
                        spannable.setSpan(imageSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        myTextView.setText(spannable);
                    }
                }.execute();
            } else {
                int resourceId = getResources().getIdentifier(emoji, "drawable", getPackageName());

                Drawable emojiDrawable = null;
                if (resourceId == 0) {
                    resourceId = getResources().getIdentifier("placeholder", "drawable", getPackageName());
                }

                if (resourceId != 0) {
                    emojiDrawable = getResources().getDrawable(resourceId, getTheme());
                }

                if (emojiDrawable != null) {
                    int width = (int) textSize;
                    int height = (int) textSize;
                    emojiDrawable.setBounds(0, 0, width, height);
                    ImageSpan imageSpan = new ImageSpan(emojiDrawable, ImageSpan.ALIGN_BOTTOM);
                    spannable.setSpan(imageSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }

        myTextView.setText(spannable);
    }
}
