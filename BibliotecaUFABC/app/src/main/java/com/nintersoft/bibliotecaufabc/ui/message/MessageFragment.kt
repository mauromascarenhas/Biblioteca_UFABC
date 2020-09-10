package com.nintersoft.bibliotecaufabc.ui.message

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.nintersoft.bibliotecaufabc.R
import kotlinx.android.synthetic.main.fragment_message.*

class MessageFragment : Fragment() {

    private lateinit var messageViewModel: MessageViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        messageViewModel = activity?.run {
            ViewModelProvider(this)[MessageViewModel::class.java]
        } ?: throw Exception("Invalid activity!")
        return inflater.inflate(R.layout.fragment_message, container, false)
    }

    override fun onStart() {
        super.onStart()

        messageViewModel.message.observe(viewLifecycleOwner, {
            lblMessage.text = it
        })

        messageViewModel.drawable.observe(viewLifecycleOwner, {
            val image = ContextCompat.getDrawable(requireContext(),
                it ?: R.drawable.ic_info_image)?.apply { setBounds(0, 0,
                    intrinsicWidth, intrinsicHeight) }
            lblMessage.setCompoundDrawables(null, image,
                null, null)
        })

        messageViewModel.actLabel.observe(viewLifecycleOwner, {
            btnAction.text = it
            btnAction.visibility = if (it == null) View.GONE else View.VISIBLE
        })

        messageViewModel.action.observe(viewLifecycleOwner, {
            btnAction.setOnClickListener(it)
        })
    }
}
