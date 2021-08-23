package com.example.booklistingapp;


import androidx.appcompat.app.AppCompatActivity;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Book>>
{

    //Tag for the log message
    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    //Id for initLoader
    private static final int BOOK_LOAD_ID = 1;

    private String mUrlRequestGoogle = "";
    private ListView mBookListView;
    private ProgressBar mLoadingBar;
    private TextView mEmptyTextView;

    private BookAdapter mAdapter;

    private Button mSearchButton;
    private SearchView mSearchBar;
    private boolean isConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEmptyTextView = (TextView) findViewById(R.id.empty_view);
        mLoadingBar = (ProgressBar) findViewById(R.id.loading_spinner);
        mSearchButton = (Button) findViewById(R.id.search_button);
        mSearchBar = (SearchView) findViewById(R.id.search_bar);

        mBookListView = (ListView) findViewById(R.id.book_list);
        mBookListView.setEmptyView(mEmptyTextView);

        //
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        isConnected = checkInternetConnection(connectivityManager);
        Log.i(LOG_TAG, "INTERNET connection status: " + String.valueOf(isConnected) + ". It's time to play with LoaderManager :)");
        // Create a new adapter that takes an empty list of books as input
        mAdapter = new BookAdapter(this,new ArrayList<Book>());
        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        mBookListView.setAdapter(mAdapter);

        if(isConnected)
        {
            mSearchBar.setVisibility(View.VISIBLE);
            mSearchButton.setVisibility(View.VISIBLE);

            // Get a reference to the LoaderManager, in order to interact with loaders.

            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            getLoaderManager().initLoader(BOOK_LOAD_ID, null, this);
        }
        else
        {

            Log.i(LOG_TAG, "INTERNET connection status: " + String.valueOf(isConnected) + ". Sorry dude, no internet - no data :(");
            //hide loading bar
            mLoadingBar.setVisibility(View.GONE);
            mSearchBar.setVisibility(View.INVISIBLE);
            mSearchButton.setVisibility(View.INVISIBLE);
            //set empty view "No internet connection." text
            mEmptyTextView.setText(R.string.no_internet_connection);
        }

        // Set an item click listener on the Search Button, which sends a request to
        // Google Books API based on value from Search View

        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                 //Check connection status
                isConnected = checkInternetConnection(connectivityManager);

                if(isConnected)
                {
                    // Update URL and restart loader to displaying new result of searching
                    updateQueryUrl(mSearchBar.getQuery().toString());
                    resetQueryLoader();
                    Log.i(LOG_TAG, "Search value: " + mSearchBar.getQuery().toString());
                }else
                {
                    // Clear the adapter of previous book data
                    mAdapter.clear();
                    // Set mEmpty visible
                    mEmptyTextView.setVisibility(View.VISIBLE);
                    // ...and display message: "No internet connection."
                    mEmptyTextView.setText(R.string.no_internet_connection);
                }
            }
        });

        // Set an item click listener on the ListView, which sends an intent to a web browser
        // to open a website with more information about the selected book.
        mBookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Find the current book that was clicked on
                Book currentBook = mAdapter.getItem(position);

                // Convert the String URL into a URI object (to pass into the Intent constructor)
                assert currentBook != null;
                Uri buyBookUri = Uri.parse(currentBook.getUrlBook());

                // Create a new intent to view buy the book URI
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, buyBookUri);

                // Send the intent to launch a new activity
                startActivity(websiteIntent);
            }
        });
    }

    private String updateQueryUrl(String searchValue)
    {
        if(searchValue.contains(" "))
        {
            searchValue = searchValue.replace(" ", "+");
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("https://www.googleapis.com/books/v1/volumes?q=").append(searchValue).append("&filter=paid-ebooks&maxResults=40");
        mUrlRequestGoogle = stringBuilder.toString();
        return mUrlRequestGoogle;
    }

    //the return status of internet connection
    private boolean checkInternetConnection(ConnectivityManager connectivityManager)
    {

        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @Override
    public Loader<List<Book>> onCreateLoader(int id, Bundle args) {
        Log.i("There is no instance", ": Created new one loader at the beginning!");
        // Create a new loader for the given URL
        updateQueryUrl(mSearchBar.getQuery().toString());
        return new BookLoader(this, mUrlRequestGoogle);
    }

    @Override
    public void onLoadFinished(android.content.Loader<List<Book>> loader, List<Book> books) {
        // Progress bar mapping
        View circleProgressBar = findViewById(R.id.loading_spinner);
        circleProgressBar.setVisibility(View.GONE);

        // Set empty state text to display "No books found."
        mEmptyTextView.setText(R.string.no_books);
        Log.i(LOG_TAG, ": Books has been moved to adapter's data set. This will trigger the ListView to update!");

        // Clear the adapter of previous book data
        mAdapter.clear();

        // If there is a valid list of {@link Book}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (books != null && !books.isEmpty()) {
            mAdapter.addAll(books);
        }
    }

    @Override
    public void onLoaderReset(android.content.Loader<List<Book>> loader) {
        Log.i(LOG_TAG, ": Loader reset, so we can clear out our existing data!");
        // Loader reset, so we can clear out our existing data.
        mAdapter.clear();
    }

    private void resetQueryLoader()
    {
        mEmptyTextView.setVisibility(View.GONE);
        mLoadingBar.setVisibility(View.VISIBLE);
        getLoaderManager().restartLoader(BOOK_LOAD_ID, null, MainActivity.this);
    }
}