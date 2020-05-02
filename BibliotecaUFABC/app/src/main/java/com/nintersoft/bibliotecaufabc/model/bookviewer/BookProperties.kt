package com.nintersoft.bibliotecaufabc.model.bookviewer

data class BookProperties(var title : String,
                          var author : String,
                          var imgURL : String = "",
                          var copies : ArrayList<Copy> = arrayListOf(),
                          var reservable : Boolean = false,
                          var properties : ArrayList<Property> = arrayListOf(),
                          var mediaContent : ArrayList<MediaData> = arrayListOf()) {

    data class Copy(var quantity : String, var library : String)
    data class Property(var title: String, var description: String)
    data class MediaData(var type : String,
                         var data : ArrayList<Pair<String, String>> = arrayListOf())
}