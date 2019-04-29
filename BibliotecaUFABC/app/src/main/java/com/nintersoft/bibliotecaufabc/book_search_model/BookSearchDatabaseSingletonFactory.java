package com.nintersoft.bibliotecaufabc.book_search_model;

import androidx.room.Room;

import com.nintersoft.bibliotecaufabc.appcontext.ContextApp;

public class BookSearchDatabaseSingletonFactory {
    private static BookSearchDatabase instance;

    private BookSearchDatabaseSingletonFactory(){}

    public static BookSearchDatabase getInstance(){
        if (instance == null){
            instance = Room.databaseBuilder(ContextApp.getContext(),
                    BookSearchDatabase.class, BookSearchContract.DB_NAME).allowMainThreadQueries().build();
        }
        return instance;
    }
}
