package com.nintersoft.bibliotecaufabc.book_search_model;

import android.provider.BaseColumns;

public class BookSearchContract {
    public static final String DB_NAME = "search.db";
    static final int DB_VERSION = 1;

    private BookSearchContract(){}

    static class BookRenewalItems implements BaseColumns {
        static final String TABLE_NAME = "search_items";
        static final String _ID = "_id";
        static final String COLUMN_NAME_TYPE = "type";
        static final String COLUMN_NAME_CODE = "code";
        static final String COLUMN_NAME_TITLE = "title";
        static final String COLUMN_NAME_AUTHOR = "author";
        static final String COLUMN_NAME_SECTION = "section";
    }
}
