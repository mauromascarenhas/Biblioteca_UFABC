package com.nintersoft.bibliotecaufabc.ui.bookviewer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nintersoft.bibliotecaufabc.global.Constants
import com.nintersoft.bibliotecaufabc.model.bookviewer.BookProperties
import com.nintersoft.bibliotecaufabc.model.bookviewer.ReservationDetails
import com.nintersoft.bibliotecaufabc.ui.search.SingleLiveEvent
import org.json.JSONException
import org.json.JSONObject

class BookViewerViewModel : ViewModel(){

    private val _bookURL = MutableLiveData<String>().apply { value = "" }
    private val _loggedIn = MutableLiveData<Boolean>().apply { value = false }
    private val _loginRequest = MutableLiveData<Boolean>().apply { value = false }
    private val _loginCancelled = MutableLiveData<Boolean>().apply { value = false }
    private val _bookProperties = MutableLiveData<BookProperties?>().apply { value = null }
    private val _reservationError = MutableLiveData<String?>().apply { value = null }
    private val _reservationResult = SingleLiveEvent<String?>().apply { value = null }
    private val _reservationRequest = MutableLiveData<Boolean>().apply { value = false }
    private val _dataSourceDetailsError = SingleLiveEvent<Boolean>().apply { value = null }
    private val _reservationAvailability = MutableLiveData<ReservationDetails?>()
        .apply { value = null }

    fun setBookURL(url : String){ _bookURL.value = url }
    fun setBookDetails(rawData : JSONObject){
        try{
            val jsBook = rawData.getJSONObject("details")
            if (!jsBook.getBoolean("exists")) return

            val bookProps = BookProperties(jsBook.getString("title"),
                jsBook.getString("author"))
            bookProps.imgURL = Constants.URL_LIBRARY_BOOK_COVER +
                    Constants.MANDATORY_APPEND_URL_LIBRARY_BOOK_COVER +
                    jsBook.getString("code")

            val props = jsBook.getJSONArray("properties")
            for(i in 0 until props.length()){
                with (props.getJSONObject(i)){
                    bookProps.properties.add(BookProperties.Property(getString("title"),
                        getString("description")))
                }
            }

            val media = jsBook.getJSONArray("media")
            for (i in 0 until media.length()){
                val jsContent = media.getJSONObject(i)
                val mContent = BookProperties.MediaData(jsContent.getString("type"))
                val detailsContent = jsContent.getJSONArray("values")
                for (j in 0 until detailsContent.length()){
                    with (detailsContent.getJSONObject(j)){
                        mContent.data.add(Pair(getString("text"),
                            getString("link")))
                    }
                }
            }

            val jsCopies = jsBook.getJSONArray("copies")
            for (i in 0 until jsCopies.length()){
                with (jsCopies.getJSONObject(i)){
                    bookProps.copies.add(
                        BookProperties.Copy(getString("copies"),
                            getString("library")))
                }
            }

            _loggedIn.value = rawData.getBoolean("login")
            bookProps.reservable = jsBook.getBoolean("reservable")

            _bookProperties.value = bookProps
        }
        catch (_ : JSONException) { }
    }
    fun setLoginRequest(request : Boolean) { _loginRequest.value = request }
    fun setLoginCancelled(cancelled : Boolean){ _loginCancelled.value = cancelled }
    fun setReservationError(error : String?) { _reservationError.value = error }
    fun setReservationRequest(request : Boolean){ _reservationRequest.value = request }
    fun setReservationResult(result : String?) { _reservationResult.value = result }
    fun setDataSourceDetailsError(error : Boolean){ _dataSourceDetailsError.value = error }
    fun setReservationAvailability(jsOptions : JSONObject?){
        try{
            if (jsOptions == null) {
                _reservationAvailability.value = null
                return
            }
            _reservationAvailability.value = ReservationDetails().apply {
                for (i in 0..4){
                    when(i){
                        0 -> libraries = arrayListOf<String>().apply {
                            val cOption = jsOptions.getJSONArray("library")
                            for (j in 0 until cOption.length()) add(cOption.getString(j))
                        }
                        1 -> volumes = arrayListOf<String>().apply {
                            val cOption = jsOptions.getJSONArray("volume")
                            for (j in 0 until cOption.length()) add(cOption.getString(j))
                        }
                        2 -> years = arrayListOf<String>().apply {
                            val cOption = jsOptions.getJSONArray("year")
                            for (j in 0 until cOption.length()) add(cOption.getString(j))
                        }
                        3 -> editions = arrayListOf<String>().apply {
                            val cOption = jsOptions.getJSONArray("edition")
                            for (j in 0 until cOption.length()) add(cOption.getString(j))
                        }
                        else -> support = arrayListOf<String>().apply {
                            val cOption = jsOptions.getJSONArray("support")
                            for (j in 0 until cOption.length()) add(cOption.getString(j))
                        }
                    }
                }
            }
        } catch (_: JSONException){ }
    }

    val bookURL : LiveData<String> = _bookURL
    val loggedIn : LiveData<Boolean> = _loggedIn
    val loginRequest : LiveData<Boolean> = _loginRequest
    val loginCancelled : LiveData<Boolean> = _loginCancelled
    val bookProperties : LiveData<BookProperties?> = _bookProperties
    val reservationError : LiveData<String?> = _reservationError
    val reservationResult : LiveData<String?> = _reservationResult
    val reservationRequest : LiveData<Boolean> = _reservationRequest
    val dataSourceDetailsError : LiveData<Boolean?> = _dataSourceDetailsError
    val reservationAvailability : LiveData<ReservationDetails?> = _reservationAvailability
}