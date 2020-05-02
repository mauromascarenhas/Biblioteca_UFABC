package com.nintersoft.bibliotecaufabc.ui.renewals

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nintersoft.bibliotecaufabc.ui.search.SingleLiveEvent

class RenewalsViewModel : ViewModel() {
    private val _navError = SingleLiveEvent<String>().apply { value = "" }
    private val _lastSync = MutableLiveData<String>().apply { value = "-" }
    private val _renewalMessage = MutableLiveData<String?>().apply { value = null }

    fun setNavError(error : String){ _navError.value = error }
    fun setLastSync(value : String){ _lastSync.value = value }
    fun setRenewalMessage(msg : String?){ _renewalMessage.value = msg }

    val navError : LiveData<String> = _navError
    val lastSync : LiveData<String> = _lastSync
    val renewalMessage : LiveData<String?> = _renewalMessage
}