package com.nintersoft.bibliotecaufabc.book_search_model;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface BookSearchDAO {

    @Query("SELECT * FROM search_items")
    List<BookSearchProperties> getAll();

    @Insert
    long insert(BookSearchProperties bookRenewalProperties);

    @Query("DELETE FROM search_items")
    void removeAll();
}
