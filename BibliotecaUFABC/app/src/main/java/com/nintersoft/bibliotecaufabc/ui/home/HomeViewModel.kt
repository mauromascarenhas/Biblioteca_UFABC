package com.nintersoft.bibliotecaufabc.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nintersoft.bibliotecaufabc.model.AppDatabase
import com.nintersoft.bibliotecaufabc.model.search.BookSearch
import com.nintersoft.bibliotecaufabc.ui.search.SingleLiveEvent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException

class HomeViewModel : ViewModel() {

    val askedLogout =  MutableLiveData<Boolean>().apply { value = false }
    val hasRequestedSync = MutableLiveData<Boolean>().apply { value = false }
    val hasRequestedLogin = MutableLiveData<Boolean>().apply { value = false }
    val hasCheckedPermission = MutableLiveData<Boolean>().apply { value = false }

    private val _loginStatus = MutableLiveData<Boolean?>().apply { value = null }
    private val _dataLoadError = SingleLiveEvent<Boolean?>().apply { value = null }
    private val _connectedUserName = MutableLiveData<String?>().apply { value = null }

    fun defineLoadError(error : Boolean){ _dataLoadError.value = error }
    fun defineLoginStatus(connected : Boolean){ _loginStatus.value = connected }
    fun defineConnectedUserName(uName : String?) { _connectedUserName.value = uName }
    fun defineBookSearchResults(jsonResult : String?){
        if (jsonResult.isNullOrEmpty()) return
        GlobalScope.launch {
            try {
                val dao = AppDatabase.getInstance()?.bookSearchDAO()
                dao?.removeAll()
                val jsBooksData = JSONArray(jsonResult)
                for (i in 0 until jsBooksData.length()){
                    val jsBook = jsBooksData.getJSONObject(i)
                    dao?.insert(BookSearch().apply {
                        id = i.toLong()
                        code = jsBook.getString("code")
                        type = jsBook.getString("type")
                        title = jsBook.getString("title")
                        author = jsBook.getString("author")
                        section = jsBook.getString("section")
                    })
                }
            } catch (_ : JSONException) {  }
        }
    }

    val loginStatus : LiveData<Boolean?> = _loginStatus
    val dataLoadError : LiveData<Boolean?> = _dataLoadError
    val connectedUserName : LiveData<String?> = _connectedUserName
}