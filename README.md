# Mojify

### Step-by-Step Explanation

This guide will walk you through adding custom emojis to a `TextView` in your Android project, either from local resources (drawables) or by downloading from a URL. Emojis are specified in the text as `[moji:emoji_id]` for drawable resources or `[moji:url]` for images from URLs.

#### Step 1: Define the SpannableString and Regex Pattern

1. Retrieve the original text from the `TextView` as a `SpannableString` to enable inline formatting.
2. Store the `TextView`’s text size to set consistent emoji dimensions.
3. Define a regex pattern to recognize the `[moji:...]` format, capturing the emoji ID or URL.

   ```java
   SpannableString spannable = new SpannableString(myTextView.getText().toString());
   float textSize = myTextView.getTextSize();
   Pattern pattern = Pattern.compile("\\[moji:([a-zA-Z0-9]+|http[s]?://[^\\s]+)]");
   Matcher matcher = pattern.matcher(spannable);
   ```

#### Step 2: Process Each Match

Loop through each matched `[moji:...]` to replace it with the respective emoji.

```java
while (matcher.find()) {
    String emoji = matcher.group(1);
    int start = matcher.start();
    int end = matcher.end();
```

1. **Check if the match is a URL**:
   - Use a placeholder image while the URL image is being downloaded.
   - Use `AsyncTask` to download the image from the URL and replace the placeholder.

   ```java
   if (emoji.startsWith("http")) {
       // Set placeholder initially
       int placeholderResId = getResources().getIdentifier("placeholder", "drawable", getPackageName());
       if (placeholderResId != 0) {
           Drawable placeholderDrawable = getResources().getDrawable(placeholderResId, getTheme());
           placeholderDrawable.setBounds(0, 0, (int) textSize, (int) textSize);
           spannable.setSpan(new ImageSpan(placeholderDrawable, ImageSpan.ALIGN_BOTTOM), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
           myTextView.setText(spannable);
       }

       // Download and replace image from URL
       new AsyncTask<Void, Void, Bitmap>() {
           @Override
           protected Bitmap doInBackground(Void... params) {
               try {
                   HttpURLConnection connection = (HttpURLConnection) new URL(emoji).openConnection();
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
               Drawable drawable = bitmap == null ? getResources().getDrawable(placeholderResId, getTheme()) : new BitmapDrawable(getResources(), bitmap);
               drawable.setBounds(0, 0, (int) textSize, (int) textSize);
               spannable.setSpan(new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
               myTextView.setText(spannable);
           }
       }.execute();
   }
```

2. **Handle Local Emoji Resources**:
   - If the match is not a URL, assume it’s an ID corresponding to a drawable resource.
   - Attempt to load the drawable from the resources. If it doesn’t exist, default to a placeholder.

   ```java
   else {
       int resourceId = getResources().getIdentifier(emoji, "drawable", getPackageName());
       if (resourceId == 0) {
           resourceId = getResources().getIdentifier("placeholder", "drawable", getPackageName());
       }

       Drawable emojiDrawable = resourceId != 0 ? getResources().getDrawable(resourceId, getTheme()) : null;
       if (emojiDrawable != null) {
           emojiDrawable.setBounds(0, 0, (int) textSize, (int) textSize);
           spannable.setSpan(new ImageSpan(emojiDrawable, ImageSpan.ALIGN_BOTTOM), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
       }
   }
```

#### Step 3: Apply the Modified Text

Once all `[moji:...]` placeholders have been replaced with the respective images, set the modified `spannable` back to the `TextView`:

```java
myTextView.setText(spannable);
```

### Features Added

1. **Inline Emoji Rendering**: The code identifies `[moji:emoji_id]` or `[moji:url]` formats within the text and replaces them with drawable images, allowing seamless integration of emojis in `TextView`.
2. **URL-Based Emoji Support**: Emojis from URLs are supported with placeholder handling and asynchronous loading.
3. **Placeholder Fallback**: If the image fails to load or the drawable resource is missing, the code falls back to a placeholder image, ensuring visual continuity.
4. **Scalable Emoji Sizing**: Each emoji dynamically adjusts to match the `TextView`’s text size, maintaining consistency across different device resolutions.

This setup allows you to easily add emojis either from local resources or from URLs, with robust error handling and efficient resource management.