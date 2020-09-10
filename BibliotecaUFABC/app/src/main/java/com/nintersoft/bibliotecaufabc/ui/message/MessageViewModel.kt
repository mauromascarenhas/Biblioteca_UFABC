package com.nintersoft.bibliotecaufabc.ui.message

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

@Suppress("unused")
class MessageViewModel : ViewModel() {
    private val _action = MutableLiveData<View.OnClickListener?>().apply { value = null }
    private val _message = MutableLiveData<String?>().apply { value = null }
    private val _drawable = MutableLiveData<Int?>().apply { value = null }
    private val _actLabel = MutableLiveData<String?>().apply { value = null }

    fun setAction(act : View.OnClickListener){ _action.value = act }
    fun setMessage(msg : String){ _message.value = msg }
    fun setDrawable(rid : Int) { _drawable.value = rid }
    fun setActLabel(actLbl : String){ _actLabel.value = actLbl }

    val action : LiveData<View.OnClickListener?> = _action
    val message : LiveData<String?> = _message
    val drawable : LiveData<Int?> = _drawable
    val actLabel : LiveData<String?> = _actLabel
}