package com.nintersoft.bibliotecaufabc.ui.search

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

class SingleLiveEvent<T> : MutableLiveData<T>() {
    override fun setValue(value: T?) {
        super.setValue(value)
        super.setValue(null)
    }

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        super.observe(owner, { t -> if (t != null) observer.onChanged(t) })
    }
}