package com.nintersoft.bibliotecaufabc.book_search_model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = BookSearchContract.BookRenewalItems.TABLE_NAME)
public class BookSearchProperties {
    @PrimaryKey()
    @ColumnInfo(name = BookSearchContract.BookRenewalItems._ID)
    private long id;

    @ColumnInfo(name = BookSearchContract.BookRenewalItems.COLUMN_NAME_TYPE)
    private String type;

    @ColumnInfo(name = BookSearchContract.BookRenewalItems.COLUMN_NAME_CODE)
    private String code;

    @ColumnInfo(name = BookSearchContract.BookRenewalItems.COLUMN_NAME_TITLE)
    private String title;

    @ColumnInfo(name = BookSearchContract.BookRenewalItems.COLUMN_NAME_AUTHOR)
    private String author;

    @ColumnInfo(name = BookSearchContract.BookRenewalItems.COLUMN_NAME_SECTION)
    private String section;

    public BookSearchProperties(){
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
