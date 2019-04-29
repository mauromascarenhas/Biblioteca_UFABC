package com.nintersoft.bibliotecaufabc.book_search_model;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(version = BookSearchContract.DB_VERSION, entities = {BookSearchProperties.class}, exportSchema = false)
public abstract class BookSearchDatabase extends RoomDatabase {
    abstract public BookSearchDAO bookSearchDAO();
}
