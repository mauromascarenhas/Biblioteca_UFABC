package com.nintersoft.bibliotecaufabc.ui.reservations


import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager

import com.nintersoft.bibliotecaufabc.R
import kotlinx.android.synthetic.main.fragment_renewals_recycler.*

class ReservationsRecyclerFragment : Fragment() {

    private var listener: ReservationEvents? = null
    private lateinit var cancelLoad : AlertDialog
    private lateinit var reservationsViewModel: ReservationsViewModel

    interface ReservationEvents{
        fun loadCancelLink(link : String)
        fun refreshReservations()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        reservationsViewModel = activity?.run {
            ViewModelProvider(this)[ReservationsViewModel::class.java]
        } ?: throw Exception("Invalid activity")
        return inflater.inflate(R.layout.fragment_renewals_recycler, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ReservationEvents) listener = context
        else throw RuntimeException("$context must implement OnFragmentInteractionListener")

        cancelLoad = AlertDialog.Builder(activity!!).apply {
            setView(R.layout.message_progress_dialog)
            setCancelable(false)
        }.create().also {
            it.setOnShowListener { _ ->
                it.findViewById<TextView>(R.id.label_message_loading)?.
                    setText(R.string.dialog_warning_message_loading_server_response)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.refresh_frag_options_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.action_refresh -> reservationsViewModel.requestReload(true)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onStart() {
        super.onStart()

        ReservationsViewAdapter(reservationsViewModel).also {
            renewalsRecycler.adapter = it
            it.cancellationRequest().observe(viewLifecycleOwner, Observer {link ->
                listener?.loadCancelLink(link)
                cancelLoad.show()
            })
        }
        renewalsRecycler.layoutManager = LinearLayoutManager(context)

        reservationsViewModel.cancellationMsg.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            if (cancelLoad.isShowing) cancelLoad.dismiss()

            AlertDialog.Builder(activity!!).apply {
                setTitle(R.string.dialog_server_response_title)
                setMessage(it)
                setPositiveButton(R.string.dialog_button_ok, null)
            }.create().show()
        })
    }
}
