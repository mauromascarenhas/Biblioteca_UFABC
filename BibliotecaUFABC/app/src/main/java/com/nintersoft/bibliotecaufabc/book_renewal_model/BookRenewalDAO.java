package com.nintersoft.bibliotecaufabc.book_renewal_model;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface BookRenewalDAO {

    @Query("SELECT * FROM renewal_items")
    List<BookRenewalProperties> getAll();

    @Insert
    long insert(BookRenewalProperties bookRenewalProperties);

    @Query("DELETE FROM renewal_items")
    void removeAll();
}
