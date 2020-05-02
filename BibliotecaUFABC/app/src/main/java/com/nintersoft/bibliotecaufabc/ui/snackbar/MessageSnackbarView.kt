package com.nintersoft.bibliotecaufabc.ui.snackbar

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.snackbar.ContentViewCallback
import com.nintersoft.bibliotecaufabc.R
import kotlinx.android.synthetic.main.snackbar_message_layout.view.*

class MessageSnackbarView @JvmOverloads constructor(
    context : Context, attrs : AttributeSet? = null , defStyleAttr : Int = 0) :
    ConstraintLayout(context, attrs, defStyleAttr), ContentViewCallback {

    init {
        View.inflate(context, R.layout.snackbar_message_layout, this)
        clipToPadding = false
    }

    override fun animateContentIn(delay: Int, duration: Int) {
        val scaleX = ObjectAnimator.ofFloat(icon, View.SCALE_X, 0f, 1f)
        val scaleY = ObjectAnimator.ofFloat(icon, View.SCALE_Y, 0f, 1f)

        AnimatorSet().apply {
            interpolator = OvershootInterpolator()
            playTogether(scaleX, scaleY)
            setDuration(500)
        }.start()
    }

    override fun animateContentOut(delay: Int, duration: Int) {  }
}