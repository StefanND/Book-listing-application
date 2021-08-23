package com.example.booklistingapp;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public final class QueryUtils {

    public static final String LOG_TAG = QueryUtils.class.getSimpleName();

    private QueryUtils()
    {

    }

    private static List<Book> extractBookFromJson(String bookJson)
    {
        //If the JSON string is empty or null, then return null
        if(TextUtils.isEmpty(bookJson))
        {
            return null;
        }

        // Create an empty ArrayList that we can start adding books to
        List<Book> books = new ArrayList<Book>();

        // Try to parse the JSON response string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try
        {
            // Create a JSONObject from the JSON response string
            JSONObject baseJsonResponse = new JSONObject(bookJson);
            Log.println(Log.INFO, LOG_TAG, bookJson);

            // Extract the JSONArray associated with the key called "items",
            // which represents a list of books.
            JSONArray booksArray = baseJsonResponse.getJSONArray("items");
            Log.println(Log.INFO, LOG_TAG, String.valueOf(booksArray));

            // For each book in the booksArray, create an {@link Book} object

            for(int i = 0; i< booksArray.length(); i++)
            {
                // Get a single book at position i within the list of items (books)
                JSONObject currentBook = booksArray.getJSONObject(i);
                Log.println(Log.INFO, LOG_TAG, String.valueOf(currentBook));

                // For a given book, extract the JSONObject associated with the
                // key called "volumeInfo", which represents a list of all properties
                // for that book. + [authors] list

                JSONObject volumeInfo = currentBook.getJSONObject("volumeInfo");

                // Extract the value for the key called "title"
                String title = volumeInfo.getString("title");

                //Extract the value for the key called "author"
                String author;

                // Check if JSONArray exist
                if(volumeInfo.has("author"))
                {
                    JSONArray authors = volumeInfo.getJSONArray("authors");
                    Log.println(Log.INFO, LOG_TAG, String.valueOf(authors));
                    // Check JSONArray Returns true if this object has no mapping for name or if it has a mapping whose value is NULL
                    if(!volumeInfo.isNull("authors"))
                    {
                        // Get 1st element
                        author = (String) authors.get(0);
                    }
                    else
                    {
                        author = "No author";
                    }
                }else
                {
                    author = "No author";
                }

                // For a given book, extract the JSONObject associated with the
                // key called "imageLinks", which represents a list of all cover
                // images in a different size
                JSONObject imageLink = volumeInfo.getJSONObject("imageLinks");
                Log.println(Log.INFO, LOG_TAG, String.valueOf(imageLink));
                // Extract String URL of specific image
                String imageURL = imageLink.getString("smallThumbnail");

                // Extract the value for the key called "smallThumbnail"
                // Using REGEX and StringBuilder
                StringBuilder stringBuilder = new StringBuilder();

                stringBuilder.append("https://books.google.com/books/content/images/frontcover/").append(imageURL.substring(imageURL.indexOf("id=")+3,imageURL.indexOf("id=")+15)).append("?fife=w300");
                Log.i(LOG_TAG,imageURL.substring(imageURL.indexOf("id=")+3,imageURL.indexOf("id=")+15));
                imageURL = String.valueOf(stringBuilder);

                // For a given book, extract the JSONObject associated with the
                // key called "saleInfo", which represents a list of region and object RetailPrice{amount, currency}
                JSONObject saleInfo = currentBook.getJSONObject("saleInfo");

                // Extract the value for the key called "buyLink"
                String buyLink = (String) saleInfo.get("buyLink");

                // Create a new {@link Book} object with the title, author, coverImageUrl, price, currency and language
                // and url from the JSON response.
                Book bookItem = new Book(title, author, imageURL, buyLink);

                // Add the new {@link Book} to the list of booksList.
                books.add(bookItem);
            }
        }catch(JSONException e)
        {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("QueryUtils", "Problem parsing the earthquake JSON results", e);
        }

        // Return the list of books (booksList)
        return books;
    }

    /**
     * Query the USGS dataset and return a list of {@link Book} objects.
     */
    static List<Book> fetchBookData(String requestUrl) {

        final int SLEEP_TIME_MILLIS = 2000;

        // This action with sleeping is required for displaying circle progress bar
        try {
            Thread.sleep(SLEEP_TIME_MILLIS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Create URL object
        URL url = createUrl(requestUrl);
        Log.i(LOG_TAG, url.toString());
        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
            Log.i(LOG_TAG, "HTTP request: OK");
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        // Extract relevant fields from the JSON response and create a list of {@link Book}s
        List<Book> listBooks = extractBookFromJson(jsonResponse);

        // Return the list of {@link Book}s
        return listBooks;
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl)
    {
        URL url = null;
        try
        {
            url = new URL(stringUrl);
        }catch(MalformedURLException e)
        {
            Log.e(LOG_TAG,"Problem building the URL", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the earthquake JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return jsonResponse;
    }
    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }
}
