package com.nintersoft.bibliotecaufabc.book_renewal_model;

import android.provider.BaseColumns;

public class BookRenewalContract {
    public static final String DB_NAME = "renewals.db";
    static final int DB_VERSION = 1;

    private BookRenewalContract(){}

    static class BookRenewalItems implements BaseColumns {
        static final String TABLE_NAME = "renewal_items";
        static final String _ID = "_id";
        static final String COLUMN_NAME_DATE = "date";
        static final String COLUMN_NAME_TITLE = "title";
        static final String COLUMN_NAME_LIBRARY = "library";
        static final String COLUMN_NAME_PATRIMONY = "patrimony";
        static final String COLUMN_NAME_RENEWAL_LINK = "renewal_link";
    }
}
