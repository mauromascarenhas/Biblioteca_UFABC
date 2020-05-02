package com.nintersoft.bibliotecaufabc.model.search

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = BookSearchContract.TABLE_NAME)
class BookSearch {
    @PrimaryKey
    @ColumnInfo(name = BookSearchContract.ID)
    var id : Long? = null

    @ColumnInfo(name = BookSearchContract.COLUMN_NAME_TYPE)
    var type : String = ""

    @ColumnInfo(name = BookSearchContract.COLUMN_NAME_CODE)
    var code : String = ""

    @ColumnInfo(name = BookSearchContract.COLUMN_NAME_TITLE)
    var title : String = ""

    @ColumnInfo(name = BookSearchContract.COLUMN_NAME_AUTHOR)
    var author : String = ""

    @ColumnInfo(name = BookSearchContract.COLUMN_NAME_SECTION)
    var section : String = ""

    @ColumnInfo(name = BookSearchContract.COLUMN_NAME_FAVOURITE)
    var favourite : Boolean = false
}