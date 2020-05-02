package com.nintersoft.bibliotecaufabc.ui.renewals

import android.app.NotificationManager
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.webkit.WebView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.nintersoft.bibliotecaufabc.R
import com.nintersoft.bibliotecaufabc.activities.LoginActivity
import com.nintersoft.bibliotecaufabc.global.Constants
import com.nintersoft.bibliotecaufabc.global.Functions
import com.nintersoft.bibliotecaufabc.model.AppContext
import com.nintersoft.bibliotecaufabc.model.AppDatabase
import com.nintersoft.bibliotecaufabc.synchronization.SyncService
import com.nintersoft.bibliotecaufabc.ui.home.HomeViewModel
import com.nintersoft.bibliotecaufabc.ui.snackbar.MessageSnackbar
import com.nintersoft.bibliotecaufabc.webclient.RenewalsWebClient
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_renewals.*
import org.json.JSONException
import org.json.JSONObject
import java.text.DateFormat
import java.util.*

class RenewalsFragment : Fragment() {

    private var dataSource : WebView? = null
    private lateinit var snackMsg : MessageSnackbar
    private lateinit var vAdapter : RenewalsViewAdapter
    private lateinit var renewalLoad : AlertDialog
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var renewalsViewModel: RenewalsViewModel

    private val prefListener = SharedPreferences.OnSharedPreferenceChangeListener { pref, key ->
        if(key == getString(R.string.key_synchronization_schedule))
            renewalsViewModel.setLastSync(DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
                DateFormat.MEDIUM, Locale.getDefault()).
                format(Date(pref.getLong(key, 0L))))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        homeViewModel = activity?.run {
            ViewModelProvider(this)[HomeViewModel::class.java]
        } ?: throw Exception("Invalid activity")
        renewalsViewModel =
            ViewModelProvider(this).get(RenewalsViewModel::class.java)
        renewalsViewModel.setLastSync(DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
            DateFormat.MEDIUM, Locale.getDefault()).
                format(Date(PreferenceManager.getDefaultSharedPreferences(context).
                getLong(getString(R.string.key_synchronization_schedule), 0L))))
        return inflater.inflate(R.layout.fragment_renewals, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onStart() {
        super.onStart()

        (activity?.getSystemService(NOTIFICATION_SERVICE) as? NotificationManager)?.cancelAll()

        createAlerts()
        configureRList()
        configureWebView()
        configureObservers()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (snackMsg.isShown) snackMsg.dismiss()
        PreferenceManager.getDefaultSharedPreferences(context).
            unregisterOnSharedPreferenceChangeListener(prefListener)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.refresh_frag_options_menu, menu)
        menu.findItem(R.id.action_refresh).isEnabled = !SyncService.isRunning.value!!
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.action_refresh -> {
                if (snackMsg.isShown) snackMsg.dismiss()
                ContextCompat.startForegroundService(activity!!,
                    Intent(activity!!, SyncService::class.java).
                        putExtra(Constants.SYNC_INTENT_SCHEDULED, false))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun createAlerts(){
        renewalLoad = AlertDialog.Builder(activity!!).apply {
            setView(R.layout.message_progress_dialog)
            setCancelable(false)
        }.create().also { dlg ->
            dlg.setOnShowListener {
                dlg.findViewById<TextView?>(R.id.label_message_loading)?.
                    setText(R.string.dialog_warning_message_loading_server_response)
            }
        }
        snackMsg = MessageSnackbar.make(renewalFragment,
            R.string.snack_message_no_renewal, Snackbar.LENGTH_INDEFINITE,
            MessageSnackbar.Type.INFO)?.setAction(R.string.dialog_button_ok, ({ snackMsg.dismiss() }))?.
            setAnchorView(activity!!.nav_view)!!
    }

    private fun configureRList(){
        renewalBookList.adapter = RenewalsViewAdapter().also {
            it.renewalRequest().observe(viewLifecycleOwner, Observer { url ->
                if (!homeViewModel.loginStatus.value!!){
                    AlertDialog.Builder(activity!!).apply {
                        setTitle(R.string.dialog_warning_title)
                        setMessage(R.string.dialog_warning_message_must_be_connected)
                        setPositiveButton(R.string.dialog_button_yes, ({ _, _ ->
                            activity?.startActivityForResult(
                                Intent(activity!!, LoginActivity::class.java),
                                Constants.ACTIVITY_LOGIN_REQUEST_CODE)
                        }))
                        setNegativeButton(R.string.dialog_button_no, null)
                    }.create().show()
                    it.notifyDataSetChanged()
                    return@Observer
                }
                dataSource?.loadUrl(url)
                renewalLoad.show()
            })
            vAdapter = it
        }
        renewalBookList.layoutManager = LinearLayoutManager(context)
    }

    private fun configureWebView(){
        dataSource = WebView(activity)
        Functions.configureWebView(dataSource!!, RenewalsWebClient(renewalsViewModel))
    }

    private fun configureObservers(){
        PreferenceManager.getDefaultSharedPreferences(context).
            registerOnSharedPreferenceChangeListener(prefListener)

        renewalsViewModel.lastSync.observe(viewLifecycleOwner, Observer {
            lblLastRenewal.text = getString(R.string.lbl_renewal_last_sync, it)
        })

        renewalsViewModel.navError.observe(viewLifecycleOwner, Observer {
            if (it.isNullOrEmpty()) return@Observer
            AlertDialog.Builder(activity!!).apply {
                setTitle(R.string.dialog_error_title)
                setMessage(it)
                setCancelable(false)
                setNeutralButton(R.string.dialog_button_ok, ({ _, _ ->
                    vAdapter.notifyDataSetChanged()
                }))
            }.create().show()
        })

        renewalsViewModel.renewalMessage.observe(viewLifecycleOwner, Observer {msg ->
            if (msg == null) return@Observer
            if (renewalLoad.isShowing) renewalLoad.dismiss()
            if (SyncService.isRunning.value == false)
                ContextCompat.startForegroundService(AppContext.context!!,
                    Intent(AppContext.context!!, SyncService::class.java).
                        putExtra(Constants.SYNC_INTENT_SCHEDULED, false))

            val sb = StringBuilder()
            try {
                with (JSONObject(msg)){
                    val feature : String? = getString("featured")
                    if (feature != null) sb.append(feature).append("\n\n")
                    getJSONArray("details").also {
                        for (i in 0 until it.length()) sb.append(it.getString(i)).append("\n")
                    }
                }
            } catch (_ : JSONException) {
                MessageSnackbar.make(renewalFragment,
                    R.string.snack_message_parse_fail,
                    Snackbar.LENGTH_LONG, MessageSnackbar.Type.ERROR)?.
                    setAnchorView(activity!!.nav_view)?.show()
            }

            AlertDialog.Builder(activity!!).apply {
                setTitle(R.string.dialog_server_response_title)
                setMessage(sb.toString())
                setPositiveButton(R.string.dialog_button_ok, null)
            }.create().show()

            renewalsViewModel.setRenewalMessage(null)
        })

        AppDatabase.getInstance()?.bookRenewalDAO()?.getAll()?.observe(viewLifecycleOwner,
            Observer {
                if (it.isEmpty() && !snackMsg.isShown) snackMsg.show()
                else if (snackMsg.isShownOrQueued) snackMsg.dismiss()
            }
        )

        SyncService.isRunning.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            activity?.invalidateOptionsMenu()
        })
    }
}