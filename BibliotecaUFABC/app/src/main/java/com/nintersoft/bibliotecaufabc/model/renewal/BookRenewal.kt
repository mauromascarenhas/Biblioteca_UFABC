package com.nintersoft.bibliotecaufabc.model.renewal

import androidx.room.*
import java.util.*

@Entity(tableName = BookRenewalContract.TABLE_NAME)
class BookRenewal {

    class Converters{
        @TypeConverter
        fun fromTimeStamp(value : Long?) : Date? {
            return if (value == null) null else Date(value)
        }

        @TypeConverter
        fun toTimeStamp(date : Date?) : Long? {
            return date?.time
        }
    }

    @PrimaryKey
    @ColumnInfo(name = BookRenewalContract.ID)
    var id : Long? = null

    @TypeConverters(Converters::class)
    @ColumnInfo(name = BookRenewalContract.COLUMN_NAME_DATE)
    var date : Date? = null

    @ColumnInfo(name = BookRenewalContract.COLUMN_NAME_TITLE)
    var title : String = ""

    @ColumnInfo(name = BookRenewalContract.COLUMN_NAME_LIBRARY)
    var library : String = ""

    @ColumnInfo(name = BookRenewalContract.COLUMN_NAME_PATRIMONY)
    var patrimony : String = ""

    @ColumnInfo(name = BookRenewalContract.COLUMN_NAME_RENEWAL_LINK)
    var renewalLink : String = ""
}