package com.example.myapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ImageSpan
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.regex.Pattern

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val myTextView = findViewById<TextView>(R.id.myTextView)
        val spannable = SpannableString(myTextView.text.toString())
        val textSize = myTextView.textSize

        val pattern = Pattern.compile("\\[moji:([a-zA-Z0-9]+|http[s]?://[^\\s]+)]")
        val matcher = pattern.matcher(spannable)

        while (matcher.find()) {
            val emoji = matcher.group(1) ?: ""
            val start = matcher.start()
            val end = matcher.end()

            if (emoji.startsWith("http")) {
                val placeholderResId = resources.getIdentifier("placeholder", "drawable", packageName)
                if (placeholderResId != 0) {
                    val placeholderDrawable = resources.getDrawable(placeholderResId, theme).apply {
                        setBounds(0, 0, textSize.toInt(), textSize.toInt())
                    }
                    spannable.setSpan(ImageSpan(placeholderDrawable, ImageSpan.ALIGN_BOTTOM), start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
                    myTextView.text = spannable
                }

                object : AsyncTask<Void, Void, Bitmap?>() {
                    override fun doInBackground(vararg params: Void?): Bitmap? {
                        return try {
                            val connection = URL(emoji).openConnection() as HttpURLConnection
                            connection.doInput = true
                            connection.connect()
                            val input = connection.inputStream
                            BitmapFactory.decodeStream(input)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            null
                        }
                    }

                    override fun onPostExecute(bitmap: Bitmap?) {
                        if (bitmap == null) {
                            val fallbackResId = resources.getIdentifier("placeholder", "drawable", packageName)
                            if (fallbackResId != 0) {
                                val fallbackDrawable = resources.getDrawable(fallbackResId, theme).apply {
                                    setBounds(0, 0, textSize.toInt(), textSize.toInt())
                                }
                                spannable.setSpan(ImageSpan(fallbackDrawable, ImageSpan.ALIGN_BOTTOM), start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
                                myTextView.text = spannable
                            }
                            return
                        }

                        val drawable = BitmapDrawable(resources, bitmap).apply {
                            setBounds(0, 0, textSize.toInt(), textSize.toInt())
                        }
                        spannable.setSpan(ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM), start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
                        myTextView.text = spannable
                    }
                }.execute()
            } else {
                var resourceId = resources.getIdentifier(emoji, "drawable", packageName)
                if (resourceId == 0) {
                    resourceId = resources.getIdentifier("placeholder", "drawable", packageName)
                }

                val emojiDrawable = resources.getDrawable(resourceId, theme)?.apply {
                    setBounds(0, 0, textSize.toInt(), textSize.toInt())
                }

                emojiDrawable?.let {
                    spannable.setSpan(ImageSpan(it, ImageSpan.ALIGN_BOTTOM), start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }

        myTextView.text = spannable
    }
}
