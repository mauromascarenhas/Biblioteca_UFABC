package com.nintersoft.bibliotecaufabc.model.renewal

import android.provider.BaseColumns

object BookRenewalContract : BaseColumns {
    const val TABLE_NAME = "ent_BookRenewal"

    const val ID = "_id"
    const val COLUMN_NAME_DATE = "date"
    const val COLUMN_NAME_TITLE = "title"
    const val COLUMN_NAME_LIBRARY = "library"
    const val COLUMN_NAME_PATRIMONY = "patrimony"
    const val COLUMN_NAME_RENEWAL_LINK = "renewal_link"
}