package com.nintersoft.bibliotecaufabc.ui.renewals

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.nintersoft.bibliotecaufabc.R
import com.nintersoft.bibliotecaufabc.global.Constants
import com.nintersoft.bibliotecaufabc.model.AppContext
import com.nintersoft.bibliotecaufabc.model.AppDatabase
import com.nintersoft.bibliotecaufabc.ui.search.SingleLiveEvent
import java.text.DateFormat
import java.util.*

class RenewalsViewAdapter : RecyclerView.Adapter<RenewalsViewAdapter.RenewalBookViewHolder>() {

    private val books = AppDatabase.getInstance()?.bookRenewalDAO()?.getAll()
    private val _renewal = SingleLiveEvent<String>()

    init {
        books?.observeForever {
            this.notifyDataSetChanged()
        }
    }

    @Suppress("unused")
    inner class RenewalBookViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val card : CardView? = itemView.findViewById(R.id.card_renewal_frame)
        val date : TextView? = itemView.findViewById(R.id.card_renewal_item_date)
        val title : TextView? = itemView.findViewById(R.id.card_renewal_item_title)
        val library : TextView? = itemView.findViewById(R.id.card_renewal_item_library)
        val bookCover : ImageView? = itemView.findViewById(R.id.card_renewal_item_photo)
        val patrimony : TextView? = itemView.findViewById(R.id.card_renewal_item_patrimony)
        val btnRenew : Button? = itemView.findViewById(R.id.card_renewal_item_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RenewalBookViewHolder {
        return RenewalBookViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_book_renewal, parent, false))
    }

    @ExperimentalStdlibApi
    override fun onBindViewHolder(holder: RenewalBookViewHolder, position: Int) {
        val book = books?.value?.get(position)

        holder.bookCover?.setImageResource(Constants.
            BOOK_COVER_PLACEHOLDERS[position % Constants.BOOK_COVER_PLACEHOLDERS.size])
        holder.date?.text = AppContext.context?.getString(R.string.card_renewal_limit_date_text,
            DateFormat.getDateInstance(DateFormat.FULL, Locale.getDefault()).format(book?.date!!)
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() })
        holder.title?.text = book?.title
        holder.library?.text = book?.library
        holder.patrimony?.text = book?.patrimony

        val hasRenewal = book?.renewalLink != "${Constants.URL_LIBRARY_RENEWAL}#"
        holder.btnRenew?.isEnabled = hasRenewal
        holder.btnRenew?.text = AppContext.context?.getString(R.string.btn_card_renewal_renew)
        holder.btnRenew?.setOnClickListener {
            _renewal.value = book?.renewalLink
            holder.btnRenew.text = AppContext.context?.getString(R.string.btn_card_renewal_renewing)
            holder.btnRenew.isEnabled = false
        }
    }

    override fun getItemCount(): Int {
        return books?.value?.size ?: 0
    }

    fun renewalRequest() : LiveData<String> { return _renewal }
}