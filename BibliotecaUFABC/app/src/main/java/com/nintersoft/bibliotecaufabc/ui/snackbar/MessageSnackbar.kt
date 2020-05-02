package com.nintersoft.bibliotecaufabc.ui.snackbar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.nintersoft.bibliotecaufabc.R
import kotlinx.android.synthetic.main.snackbar_message_layout.view.*
import java.lang.Exception

@Suppress("MemberVisibilityCanBePrivate", "unused")
class MessageSnackbar (parent : ViewGroup, private val selfView : MessageSnackbarView) :
    BaseTransientBottomBar<MessageSnackbar>(parent, selfView, selfView){

    init {
        view.setBackgroundColor(ContextCompat.getColor(view.context, android.R.color.transparent))
        view.setPadding(0, 0, 0, 0)
    }

    enum class Type{
        DEFAULT,
        ERROR,
        INFO,
        SUCCESS,
        WARNING
    }

    companion object {
        fun make(view: View, msgId : Int, duration : Int,
                 type : Type = Type.DEFAULT, icon: Int? = null) : MessageSnackbar? {
            return make(view, view.context.getString(msgId), duration, type, icon)
        }

        fun make(view: View, message: String, duration : Int,
                    type : Type = Type.DEFAULT, icon: Int? = null) : MessageSnackbar?{
            return when (type){
                Type.ERROR -> makeCustom(view, message, duration,
                    icon, null, null, R.color.snack_background_red)
                Type.INFO -> makeCustom(view, message, duration,
                    icon, null, null, R.color.snack_background_blue)
                Type.SUCCESS -> makeCustom(view, message, duration,
                    icon, null, null, R.color.snack_background_green)
                Type.WARNING -> makeCustom(view, message, duration,
                    icon, null, null, R.color.snack_background_yellow)
                else -> makeCustom(view, message, duration, icon,
                    null, null, null)
            }
        }

        fun makeCustom(view : View, message : String, duration : Int, icon : Int? = null,
                       actionLabel : String? = null, listener : View.OnClickListener? = null,
                       colourId : Int? = null) : MessageSnackbar?{
            val parent = view.findSuitableParent() ?: throw IllegalArgumentException(
                "No suitable parent found from the given view. Please provide a valid view."
            )

            try {
                val customSnack = LayoutInflater.from(view.context).inflate(
                    R.layout.snackbar_message_view, parent, false
                ) as MessageSnackbarView

                customSnack.message.text = message
                if (icon != null) customSnack.icon.run {
                    setImageResource(icon)
                    visibility = View.VISIBLE
                }
                if (colourId != null) customSnack.snackRootLayout.
                    setBackgroundColor(ContextCompat.getColor(view.context, colourId))
                actionLabel?.let {
                    customSnack.actionLabel.text = actionLabel
                    customSnack.actionLabel.setOnClickListener(listener)
                }

                return MessageSnackbar(parent, customSnack).setDuration(duration)
            } catch (e : Exception) { e.printStackTrace() }

            return null
        }

        private fun View?.findSuitableParent(): ViewGroup? {
            var view = this
            var fallback: ViewGroup? = null
            do {
                if (view is CoordinatorLayout) return view
                else if (view is FrameLayout) {
                    if (view.id == android.R.id.content) return view
                    else fallback = view
                }

                if (view != null) {
                    val parent = view.parent
                    view = if (parent is View) parent else null
                }
            } while (view != null)
            return fallback
        }
    }

    fun setAction(resId : Int, listener : View.OnClickListener?) : MessageSnackbar{
        return setAction(context.getString(resId), listener)
    }

    fun setAction(resId : Int, function: (View) -> Unit) : MessageSnackbar{
        return setAction(context.getString(resId), function)
    }

    fun setAction(label : String, listener : View.OnClickListener?) : MessageSnackbar{
        selfView.actionLabel.text = label
        selfView.actionLabel.setOnClickListener(listener)
        return this
    }

    fun setAction(label : String, function: (View) -> Unit) : MessageSnackbar{
        selfView.actionLabel.text = label
        selfView.actionLabel.setOnClickListener(function)
        return this
    }

    fun setIconDrawable(drawableId : Int?) : MessageSnackbar{
        if (drawableId == null) selfView.icon.visibility = View.GONE
        else selfView.icon.run {
            setImageResource(drawableId)
            visibility = View.VISIBLE
        }
        return this
    }

    fun setBackgroundColour(colourId : Int) : MessageSnackbar{
        selfView.snackRootLayout.setBackgroundColor(ContextCompat.
            getColor(selfView.context, colourId))
        return this
    }
}