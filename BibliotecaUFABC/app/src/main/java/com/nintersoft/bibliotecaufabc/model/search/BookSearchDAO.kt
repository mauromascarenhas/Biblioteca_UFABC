package com.nintersoft.bibliotecaufabc.model.search

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface BookSearchDAO {

    @Query("SELECT * FROM ${BookSearchContract.TABLE_NAME}")
    fun getAll() : LiveData<List<BookSearch>>

    @Insert
    fun insert(book : BookSearch) : Long

    @Delete
    fun remove(book : BookSearch) : Int

    @Query("DELETE FROM ${BookSearchContract.TABLE_NAME}")
    fun removeAll()
}