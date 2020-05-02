package com.nintersoft.bibliotecaufabc.ui.reservations

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nintersoft.bibliotecaufabc.model.reservation.BookReservation
import com.nintersoft.bibliotecaufabc.ui.search.SingleLiveEvent
import org.json.JSONArray
import org.json.JSONException

class ReservationsViewModel : ViewModel() {

    private val _userName = MutableLiveData<String?>().apply { value = null }
    private val _loadError = SingleLiveEvent<Boolean?>().apply { value = null }
    private val _reloadRequest = SingleLiveEvent<Boolean?>().apply { value = null }
    private val _reservedBooks = MutableLiveData<ArrayList<BookReservation>>().apply {
        value = arrayListOf()
    }
    private val _cancellationMsg = SingleLiveEvent<String?>().apply { value = null }

    fun setCancellationMessage(msg : String?){
        _cancellationMsg.value = msg
    }

    fun setUserName(uName : String?){
        _userName.value = uName
        _reservedBooks.value = arrayListOf()
    }

    fun setLoadError(error : Boolean?){ _loadError.value = error }

    fun requestReload(req : Boolean?) { _reloadRequest.value = req }

    fun setReservedBooks(books : JSONArray?){
        if (books == null) return
        try{
            if (books.length() == 0){
                _userName.value = "???"
                return
            }
            _userName.value = null

            val newBooks = arrayListOf<BookReservation>()
            for (i in 0 until books.length()){
                newBooks.add(BookReservation().apply {
                        with(books.getJSONObject(i)){
                            title = getString("title")
                            queue = getString("queue")
                            library = getString("library")
                            material = getString("material")
                            situation = getString("situation")
                            linkCancel = getString("cancel_link")
                        }
                    })
            }
            _reservedBooks.value = newBooks
        }
        catch (_ : JSONException){  }
    }

    val userName : LiveData<String?> = _userName
    val loadError : LiveData<Boolean?> = _loadError
    val reloadRequest : LiveData<Boolean?> = _reloadRequest
    val reservedBooks : LiveData<ArrayList<BookReservation>> = _reservedBooks
    val cancellationMsg : LiveData<String?> = _cancellationMsg
}