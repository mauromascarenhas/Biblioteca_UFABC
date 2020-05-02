package com.nintersoft.bibliotecaufabc.ui.loginform

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import com.nintersoft.bibliotecaufabc.R
import com.nintersoft.bibliotecaufabc.global.Constants
import kotlinx.android.synthetic.main.fragment_login_form.*

class LoginForm : Fragment() {
    private var listener: OnFragmentInteractionListener? = null

    interface OnFragmentInteractionListener {
        fun onLoginRequest(username : String, password : String)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login_form, container, false)
    }

    override fun onStart() {
        super.onStart()
        setListeners()
        setValues()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) listener = context
        else throw RuntimeException("$context must implement OnFragmentInteractionListener")
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    private fun setListeners() {
        bt_login.setOnClickListener {
            listener?.onLoginRequest(edt_login.text.toString(), edt_password.text.toString())
        }
        bt_retrieve.setOnClickListener {
            with (Intent(Intent.ACTION_VIEW)) {
                data = Uri.parse("https://acesso.ufabc.edu.br/passwordRecovery/index")
                startActivity(this)
            }
        }
        edt_password.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL){
                listener?.onLoginRequest(edt_login.text.toString(), edt_password.text.toString())
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    private fun setValues(){
        edt_login.setText(arguments?.getString(Constants.LOGIN_TO_FORM_USERNAME))
        edt_login.error = arguments?.getString(Constants.LOGIN_TO_FORM_USERNAME_ERROR)
        edt_password.setText(arguments?.getString(Constants.LOGIN_TO_FORM_PASSWORD))
        edt_password.error = arguments?.getString(Constants.LOGIN_TO_FORM_PASSWORD_ERROR)
    }
}
