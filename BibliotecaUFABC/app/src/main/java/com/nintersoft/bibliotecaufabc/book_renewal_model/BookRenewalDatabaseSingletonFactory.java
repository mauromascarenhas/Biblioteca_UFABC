package com.nintersoft.bibliotecaufabc.book_renewal_model;

import com.nintersoft.bibliotecaufabc.appcontext.ContextApp;

import androidx.room.Room;

public class BookRenewalDatabaseSingletonFactory {
    private static BookRenewalDatabase instance;

    private BookRenewalDatabaseSingletonFactory(){}

    public static BookRenewalDatabase getInstance(){
        if (instance == null){
            instance = Room.databaseBuilder(ContextApp.getContext(),
                    BookRenewalDatabase.class, BookRenewalContract.DB_NAME).allowMainThreadQueries().build();
        }
        return instance;
    }
}
