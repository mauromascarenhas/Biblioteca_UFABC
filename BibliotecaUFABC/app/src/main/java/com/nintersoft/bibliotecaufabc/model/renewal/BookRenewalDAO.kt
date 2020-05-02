package com.nintersoft.bibliotecaufabc.model.renewal

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface BookRenewalDAO {
    @Query("SELECT * FROM ${BookRenewalContract.TABLE_NAME}")
    fun getAll() : LiveData<List<BookRenewal>>

    @Insert
    fun insert(bookRenewal: BookRenewal) : Long

    @Delete
    fun remove(bookRenewal: BookRenewal) : Int

    @Query("DELETE FROM ${BookRenewalContract.TABLE_NAME}")
    fun removeAll()
}