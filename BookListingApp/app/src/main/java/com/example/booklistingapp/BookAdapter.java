package com.example.booklistingapp;

import android.app.Activity;
import android.app.DownloadManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextClock;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class BookAdapter extends ArrayAdapter<Book> {

    public BookAdapter(Activity context, ArrayList<Book> books)
    {
        super(context, 0,books);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View listItemView = convertView;

        if(listItemView == null)
        {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.book_list_item,parent,false);
        }

        //get current position of the book
        Book currentBook = getItem(position);

        //set book title
        TextView bookTitle = (TextView) listItemView.findViewById(R.id.book_title);
        bookTitle.setText(currentBook.getTitle());

        //set book author
        TextView bookAuthor = (TextView) listItemView.findViewById(R.id.book_author);
        bookAuthor.setText(currentBook.getAuthor());

        ImageView bookImage = (ImageView) listItemView.findViewById(R.id.book_image);
        //using picasso library we load the image from Url
        Picasso.with(getContext()).load(currentBook.getUrlImage()).into(bookImage);

        return listItemView;
    }
}
