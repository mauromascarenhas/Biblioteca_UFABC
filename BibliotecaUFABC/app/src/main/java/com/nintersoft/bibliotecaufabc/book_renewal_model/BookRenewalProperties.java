package com.nintersoft.bibliotecaufabc.book_renewal_model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = BookRenewalContract.BookRenewalItems.TABLE_NAME)
public class BookRenewalProperties {
    @PrimaryKey()
    @ColumnInfo(name = BookRenewalContract.BookRenewalItems._ID)
    private long id;

    @ColumnInfo(name = BookRenewalContract.BookRenewalItems.COLUMN_NAME_DATE)
    private String date;

    @ColumnInfo(name = BookRenewalContract.BookRenewalItems.COLUMN_NAME_TITLE)
    private String title;

    @ColumnInfo(name = BookRenewalContract.BookRenewalItems.COLUMN_NAME_LIBRARY)
    private String library;

    @ColumnInfo(name = BookRenewalContract.BookRenewalItems.COLUMN_NAME_PATRIMONY)
    private String patrimony;

    @ColumnInfo(name = BookRenewalContract.BookRenewalItems.COLUMN_NAME_RENEWAL_LINK)
    private String renewalLink;

    public BookRenewalProperties(){
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId(){return id;}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPatrimony() {
        return patrimony;
    }

    public void setPatrimony(String patrimony) {
        this.patrimony = patrimony;
    }

    public String getLibrary() {
        return library;
    }

    public void setLibrary(String library) {
        this.library = library;
    }

    public String getRenewalLink() {
        return renewalLink;
    }

    public void setRenewalLink(String renewalLink) {
        this.renewalLink = renewalLink;
    }
}
