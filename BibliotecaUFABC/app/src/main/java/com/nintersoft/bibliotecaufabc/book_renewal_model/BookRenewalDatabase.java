package com.nintersoft.bibliotecaufabc.book_renewal_model;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(version = BookRenewalContract.DB_VERSION, entities = {BookRenewalProperties.class}, exportSchema = false)
public abstract class BookRenewalDatabase extends RoomDatabase {
    abstract public BookRenewalDAO bookRenewalDAO();
}
