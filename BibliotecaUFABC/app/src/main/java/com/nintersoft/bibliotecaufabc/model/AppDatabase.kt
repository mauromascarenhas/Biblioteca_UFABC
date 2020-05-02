package com.nintersoft.bibliotecaufabc.model

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nintersoft.bibliotecaufabc.model.renewal.BookRenewal
import com.nintersoft.bibliotecaufabc.model.renewal.BookRenewalDAO
import com.nintersoft.bibliotecaufabc.model.search.BookSearch
import com.nintersoft.bibliotecaufabc.model.search.BookSearchDAO

@Database(version = DatabaseContract.DB_VERSION, entities = [BookSearch::class, BookRenewal::class])
@TypeConverters(BookRenewal.Converters::class)
abstract class AppDatabase : RoomDatabase(){
    abstract fun bookSearchDAO() : BookSearchDAO
    abstract fun bookRenewalDAO() : BookRenewalDAO

    companion object {
        private var instance : AppDatabase? = null

        fun getInstance() : AppDatabase? {
            if (instance == null) synchronized(AppDatabase::class){
                instance = Room.databaseBuilder(AppContext.context!!,
                    AppDatabase::class.java, DatabaseContract.DB_NAME).build()
            }
            return instance
        }
    }
}