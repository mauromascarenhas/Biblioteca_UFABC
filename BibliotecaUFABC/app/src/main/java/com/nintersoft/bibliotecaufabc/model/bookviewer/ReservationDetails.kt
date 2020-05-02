package com.nintersoft.bibliotecaufabc.model.bookviewer

data class ReservationDetails(var years : List<String> = arrayListOf(),
                              var support : List<String> = arrayListOf(),
                              var volumes : List<String> = arrayListOf(),
                              var editions : List<String> = arrayListOf(),
                              var libraries : List<String> = arrayListOf())