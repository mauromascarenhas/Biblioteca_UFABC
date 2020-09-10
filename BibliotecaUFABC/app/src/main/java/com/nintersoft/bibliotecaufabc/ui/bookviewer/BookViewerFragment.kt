package com.nintersoft.bibliotecaufabc.ui.bookviewer

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.nintersoft.bibliotecaufabc.R
import com.nintersoft.bibliotecaufabc.activities.LoginActivity
import com.nintersoft.bibliotecaufabc.global.Constants
import kotlinx.android.synthetic.main.fragment_book_viewer.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import java.util.regex.Pattern

class BookViewerFragment : Fragment() {

    private var listener: ReservationEvents? = null
    private val glideConf = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
        .placeholder(R.drawable.ic_default_book)
    private var btnReserve : Button? = null
    private var showWarning : Boolean = true
    private lateinit var loadingAlert : AlertDialog
    private lateinit var bookViewerViewModel: BookViewerViewModel

    interface ReservationEvents {
        fun reloadRequested()
        fun requestReservation(options : String)
        fun reservationRequested()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        bookViewerViewModel = activity?.run {
            ViewModelProvider(this)[BookViewerViewModel::class.java]
        } ?: throw Exception("Invalid activity!")
        return inflater.inflate(R.layout.fragment_book_viewer, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ReservationEvents) listener = context
        else throw RuntimeException("$context must implement OnFragmentInteractionListener")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        buildLate()
        setListeners()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_book_details, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            android.R.id.home -> {
                activity?.finish()
                return true
            }
            R.id.action_share_book -> {
                val shareMessage = getString(
                    R.string.share_book_structure,
                    lbl_book_title.text, lbl_book_author.text,
                    bookViewerViewModel.bookURL.value)

                with(Intent(Intent.ACTION_SEND)){
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareMessage)
                    if (resolveActivity(this@BookViewerFragment.requireActivity().packageManager) != null)
                        startActivity(Intent.createChooser(this,
                            getString(R.string.intent_share_book)))
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("InflateParams")
    private fun buildLate(){
        showWarning = PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(getString(R.string.key_general_leave_warning), true)
        loadingAlert = AlertDialog.Builder(activity)
            .apply {
                setView(View.inflate(activity, R.layout.message_progress_dialog, null))
                setCancelable(false)
            }.create()
    }

    @SuppressLint("CutPasteId")
    private fun setListeners(){
        bookViewerViewModel.bookProperties.observe(viewLifecycleOwner, Observer {props ->
            if (props == null) return@Observer
            if (bookViewerViewModel.loginRequest.value!!){
                bookViewerViewModel.setLoginRequest(false)
                bookViewerViewModel.setReservationRequest(false)
                requestReservation()
                return@Observer
            }

            Glide.with(requireContext().applicationContext).load(props.imgURL).apply(glideConf)
                .into(imgDetailsBookCover)

            lbl_book_title.text = props.title
            lbl_book_author.text = props.author

            val dMargin = resources.getDimension(R.dimen.form_items_default_margin).toInt()
            val dParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
            dParams.setMargins(0, dMargin, 0, 0)

            val hMargin = resources.getDimension(R.dimen.form_header_default_margin).toInt()
            val hParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
            hParams.setMargins(0, hMargin, 0, hMargin)
            hParams.gravity = Gravity.CENTER_HORIZONTAL

            val dSize = resources.getDimension(R.dimen.label_form_item_def_size)
            val hSize = resources.getDimension(R.dimen.label_form_title_def_size)

            props.properties.forEach {
                bookDetailsLayout.addView(TextView(activity).apply {
                    text = it.title
                    setTypeface(typeface, Typeface.BOLD)
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, hSize)
                    layoutParams = dParams
                })
                bookDetailsLayout.addView(TextView(activity).apply {
                    text = it.description
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, dSize)
                })
            }

            props.mediaContent.forEach {media ->
                bookDetailsLayout.addView(TextView(activity).apply {
                    text = media.type
                    gravity = Gravity.CENTER_HORIZONTAL
                    setTypeface(typeface, Typeface.BOLD)
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, hSize)
                    textAlignment = View.TEXT_ALIGNMENT_CENTER
                    layoutParams = hParams
                })

                media.data.forEach {
                    bookDetailsLayout.addView(TextView(activity).apply {
                        text = getString(R.string.lbl_book_viewer_media_action,
                            it.first.toUpperCase(Locale.getDefault()))
                        setTextSize(TypedValue.COMPLEX_UNIT_PX, dSize)
                        setTextColor(ContextCompat.getColor(context, R.color.colorLink))
                        if (it.second.isNotEmpty()){
                            setOnClickListener {_ ->
                                if (showWarning){
                                    AlertDialog.Builder(activity).apply {
                                        setTitle(R.string.dialog_warning_title)
                                        setMessage(
                                            getString(R.string.dialog_warning_message_external_link,
                                            it.second))
                                        setPositiveButton(R.string.dialog_button_ok,
                                            ({ _, _ ->
                                                with (Intent(Intent.ACTION_VIEW)){
                                                    data = Uri.parse(it.second)
                                                    if (resolveActivity(context.packageManager)
                                                        != null) startActivity(this)
                                                }
                                            }))
                                    }.create().show()
                                }
                                else{
                                    with (Intent(Intent.ACTION_VIEW)){
                                        data = Uri.parse(it.second)
                                        if (resolveActivity(context.packageManager) != null)
                                            startActivity(this)
                                    }
                                }
                            }
                        }
                    })
                }
            }

            if (props.copies.isNotEmpty()){
                bookDetailsLayout.addView(TextView(activity).apply {
                    setText(R.string.lbl_book_viewer_available_header)
                    gravity = Gravity.CENTER_HORIZONTAL
                    setTypeface(typeface, Typeface.BOLD)
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, hSize)
                    textAlignment = View.TEXT_ALIGNMENT_CENTER
                    layoutParams = hParams
                })

                props.copies.forEach { copy ->
                    bookDetailsLayout.addView(TextView(activity).apply {
                        text = getString(R.string.lbl_book_viewer_available_details,
                            copy.quantity.replace("de",
                                getString(R.string.lbl_book_viewer_available_counting_sep)),
                            copy.library)
                        setTextSize(TypedValue.COMPLEX_UNIT_PX, dSize)

                        val pattern = Pattern.compile("(\\d+).*?(\\d+)")
                        val matcher = pattern.matcher(copy.quantity)

                        if (matcher.find()) {
                            val relative =
                                ((Integer.parseInt(matcher.group(1) ?: "0").toFloat() /
                                Integer.parseInt(matcher.group(2) ?: "0").toFloat())
                                * 100).toInt()

                            setTextColor(ContextCompat.getColor(context, when(relative){
                                0 -> R.color.noBookAvailable
                                in 1 .. 75 -> R.color.fewBooksAvailable
                                else -> R.color.booksAvailable
                            }))
                        }
                    })
                }
            }

            if (props.reservable){
                bookDetailsLayout.addView(Button(activity).apply {
                    text = getString(R.string.btn_book_viewer_reserve)
                    layoutParams = hParams

                    if (bookViewerViewModel.loggedIn.value!!){
                        setOnClickListener { requestReservation() }
                    }
                    else {
                        setOnClickListener {
                            AlertDialog.Builder(activity).apply {
                                setTitle(R.string.dialog_warning_title)
                                setMessage(R.string.dialog_warning_message_user_disconnected)
                                setPositiveButton(R.string.dialog_button_yes, ({_ ,_ ->
                                    showLoadingAlert()
                                    activity?.startActivityForResult(Intent(activity,
                                        LoginActivity::class.java),
                                        Constants.ACTIVITY_LOGIN_REQUEST_CODE)
                                }))
                                setNegativeButton(R.string.dialog_button_cancel, null)
                            }.create().show()
                        }
                    }
                    btnReserve = this
                })

                if (bookViewerViewModel.reservationRequest.value!!){
                    bookViewerViewModel.setLoginRequest(false)
                    requestReservation()
                }
            }
        })

        bookViewerViewModel.reservationResult.observe(viewLifecycleOwner, Observer {
            if (it.isNullOrEmpty()) return@Observer

            if (loadingAlert.isShowing) loadingAlert.dismiss()
            AlertDialog.Builder(activity).apply {
                setTitle(R.string.dialog_server_response_title)
                setMessage(it)
                setPositiveButton(R.string.dialog_button_ok, null)
            }.create().show()
        })

        bookViewerViewModel.reservationAvailability.observe(viewLifecycleOwner, Observer {details ->
            if (details == null) return@Observer

            if(loadingAlert.isShowing) loadingAlert.dismiss()
            val dialog = AlertDialog.Builder(activity).apply {
                setTitle(R.string.dialog_reservation_options_title)
                setView(View.inflate(activity, R.layout.dialog_reservation_options_layout,
                    null))
                setPositiveButton(R.string.dialog_button_submit, null)
                setNegativeButton(R.string.dialog_button_cancel, null)
                setCancelable(false)
            }.create()
            dialog.setOnShowListener {dlg ->
                var cText: TextView?
                var cOptions : List<String>?
                var cSpinner : Spinner?

                for (i in 0..4){
                    when (i){
                        0 -> {
                            cText = dialog.findViewById(R.id.dialogReservationLabelYear)
                            cSpinner = dialog.findViewById(R.id.dialogReservationSpinnerYear)
                            cOptions = details.years
                        }
                        1 -> {
                            cText = dialog.findViewById(R.id.dialogReservationLabelSupport)
                            cSpinner = dialog.findViewById(R.id.dialogReservationSpinnerSupport)
                            cOptions = details.support
                        }
                        2 -> {
                            cText = dialog.findViewById(R.id.dialogReservationLabelVolume)
                            cSpinner = dialog.findViewById(R.id.dialogReservationSpinnerVolume)
                            cOptions = details.volumes
                        }
                        3 -> {
                            cText = dialog.findViewById(R.id.dialogReservationLabelEdition)
                            cSpinner = dialog.findViewById(R.id.dialogReservationSpinnerEdition)
                            cOptions = details.editions
                        }
                        else -> {
                            cText = dialog.findViewById(R.id.dialogReservationLabelLibrary)
                            cSpinner = dialog.findViewById(R.id.dialogReservationSpinnerLibrary)
                            cOptions = details.libraries
                        }
                    }

                    if (cOptions.isEmpty()){
                        cText?.visibility = View.GONE
                        cSpinner?.visibility = View.GONE
                        continue
                    }

                    cSpinner?.adapter = object : ArrayAdapter<String>(requireContext(),
                        android.R.layout.simple_spinner_item, cOptions){
                        override fun getDropDownView(position: Int, convertView: View?,
                                                     parent: ViewGroup): View {
                            val v = super.getDropDownView(position, convertView, parent)
                            with (v.findViewById<TextView>(android.R.id.text1)){
                                setTextSize(TypedValue.COMPLEX_UNIT_PX, resources
                                    .getDimensionPixelSize(R.dimen.label_form_item_def_size)
                                    .toFloat())
                                textAlignment = View.TEXT_ALIGNMENT_CENTER
                                gravity = Gravity.CENTER
                            }
                            v.minimumHeight = resources
                                .getDimensionPixelSize(R.dimen.spinner_item_size)
                            return v
                        }
                    }
                }

                (dlg as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    try {
                        var hasErrors = false
                        val options = JSONObject()

                        for (i in 0..4){
                            lateinit var index : String
                            lateinit var spinner : Spinner

                            when(i){
                                0 -> {
                                    index = "year"
                                    spinner = dialog.
                                        findViewById(R.id.dialogReservationSpinnerYear)
                                }
                                1 -> {
                                    index = "volume"
                                    spinner = dialog.
                                        findViewById(R.id.dialogReservationSpinnerVolume)
                                }
                                2 -> {
                                    index = "edition"
                                    spinner = dialog.
                                        findViewById(R.id.dialogReservationSpinnerEdition)
                                }
                                3 -> {
                                    index = "support"
                                    spinner = dialog.
                                        findViewById(R.id.dialogReservationSpinnerSupport)
                                }
                                else -> {
                                    index = "library"
                                    spinner = dialog.
                                        findViewById(R.id.dialogReservationSpinnerLibrary)
                                }
                            }

                            if (spinner.selectedItemPosition < 1
                                && spinner.visibility != View.GONE) hasErrors = true
                            else options.put(index, spinner.selectedItemPosition)
                        }

                        if (hasErrors){
                            dialog.findViewById<TextView>(R.id.dialogReservationLabelError)
                                .visibility = View.VISIBLE
                            return@setOnClickListener
                        }

                        listener?.requestReservation(options.toString())
                        dialog.dismiss()

                        loadingAlert.setOnShowListener {
                            loadingAlert.findViewById<TextView>(R.id.label_message_loading)
                                .setText(R.string.dialog_warning_message_loading_server_response)
                        }
                        loadingAlert.show()
                    } catch (_ : JSONException) { }
                }
            }
            dialog.setOnDismissListener { bookViewerViewModel.setReservationAvailability(null) }
            dialog.show()
        })

        bookViewerViewModel.reservationError.observe(viewLifecycleOwner, Observer {
            if (it.isNullOrEmpty()) return@Observer
            AlertDialog.Builder(activity).apply {
                setTitle(R.string.dialog_error_title)
                setMessage(it)
                setPositiveButton(R.string.dialog_button_ok, null)
            }.create().show()
        })
    }

    private fun requestReservation(){
        showLoadingAlert()
        btnReserve?.setOnClickListener {
            bookViewerViewModel.setReservationRequest(true)
            listener?.reloadRequested()
        }
        listener?.reservationRequested()
    }

    private fun showLoadingAlert(){
        if (!loadingAlert.isShowing){
            loadingAlert.setOnShowListener {
                loadingAlert.findViewById<TextView>(R.id.label_message_loading)
                    .setText(R.string.dialog_warning_message_loading_reservation)
            }
            loadingAlert.show()
        }
    }
}
