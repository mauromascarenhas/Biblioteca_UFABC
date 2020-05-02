
package com.nintersoft.bibliotecaufabc.ui.reservations

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.nintersoft.bibliotecaufabc.R
import com.nintersoft.bibliotecaufabc.ui.loading.LoadingFragment
import com.nintersoft.bibliotecaufabc.ui.message.MessageFragment
import com.nintersoft.bibliotecaufabc.ui.message.MessageViewModel

class ReservationsFragment : Fragment() {
    private lateinit var messageViewModel: MessageViewModel
    private lateinit var reservationsViewModel: ReservationsViewModel

    private var listener: ReservationsRecyclerFragment.ReservationEvents? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        messageViewModel = activity?.run {
            ViewModelProvider(this)[MessageViewModel::class.java]
        } ?: throw Exception("Invalid activity!")
        reservationsViewModel = activity?.run {
            ViewModelProvider(this)[ReservationsViewModel::class.java]
        } ?: throw Exception("Invalid activity!")
        return inflater.inflate(R.layout.fragment_reservations, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ReservationsRecyclerFragment.ReservationEvents) listener = context
        else throw RuntimeException("$context must implement OnFragmentInteractionListener")
    }

    override fun onStart() {
        super.onStart()
        val fragTransaction = activity?.supportFragmentManager?.beginTransaction()
        fragTransaction?.add(R.id.reservation_frag, LoadingFragment())
        fragTransaction?.commitNowAllowingStateLoss()
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onResume() {
        super.onResume()
        configureView()
    }

    private fun configureView(){
        reservationsViewModel.reservedBooks.observe(viewLifecycleOwner, Observer {
            if (it.isEmpty()) return@Observer
            val fragTransaction = activity?.supportFragmentManager?.beginTransaction()
            fragTransaction?.replace(R.id.reservation_frag,
                ReservationsRecyclerFragment()
            )
            fragTransaction?.commitAllowingStateLoss()
        })

        reservationsViewModel.userName.observe(viewLifecycleOwner, Observer {
            if (it.isNullOrEmpty()) return@Observer
            messageViewModel.setMessage(getString(R.string.lbl_no_reservation_username,
                reservationsViewModel.userName.value))
            messageViewModel.setActLabel(getString(R.string.menu_nav_refresh))
            messageViewModel.setAction(View.OnClickListener {
                reservationsViewModel.requestReload(true)
            })

            val fragTransaction = activity?.supportFragmentManager?.beginTransaction()
            MessageFragment().also {frag ->
                fragTransaction?.replace(R.id.reservation_frag, frag)
                fragTransaction?.commitAllowingStateLoss()
            }
        })

        reservationsViewModel.reloadRequest.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                listener?.refreshReservations()
                val fragTransaction = activity?.supportFragmentManager?.beginTransaction()
                fragTransaction?.replace(R.id.reservation_frag, LoadingFragment())
                fragTransaction?.commitNowAllowingStateLoss()
            }
        })
    }
}