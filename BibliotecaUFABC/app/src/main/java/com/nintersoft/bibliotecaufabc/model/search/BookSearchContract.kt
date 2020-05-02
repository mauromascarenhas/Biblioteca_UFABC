package com.nintersoft.bibliotecaufabc.model.search

import android.provider.BaseColumns

object BookSearchContract : BaseColumns {
    const val TABLE_NAME = "ent_BookSearch"

    const val ID = "_id"
    const val COLUMN_NAME_TYPE = "type"
    const val COLUMN_NAME_CODE = "code"
    const val COLUMN_NAME_TITLE = "title"
    const val COLUMN_NAME_AUTHOR = "author"
    const val COLUMN_NAME_SECTION = "section"
    const val COLUMN_NAME_FAVOURITE = "favourite"
}