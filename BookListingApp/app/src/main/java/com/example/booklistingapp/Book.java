package com.example.booklistingapp;

public class Book {

    private final String mTitle;
    private final String mAuthor;
    private final String mUrlImage;
    private final String mUrlBook;

    public Book(String title, String author, String urlImage, String urlBook)
    {
        mTitle = title;
        mAuthor = author;
        mUrlImage = urlImage;
        mUrlBook = urlBook;
    }

    public String getTitle()
    {
        return mTitle;
    }

    public String getAuthor()
    {
        return mAuthor;
    }

    public String getUrlImage()
    {
        return mUrlImage;
    }

    public String getUrlBook()
    {
        return mUrlBook;
    }

}
