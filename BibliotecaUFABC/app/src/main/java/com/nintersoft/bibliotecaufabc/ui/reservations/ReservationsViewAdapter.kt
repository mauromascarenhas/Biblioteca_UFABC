package com.nintersoft.bibliotecaufabc.ui.reservations

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
import com.nintersoft.bibliotecaufabc.ui.search.SingleLiveEvent

class ReservationsViewAdapter(vModel : ReservationsViewModel) :
    RecyclerView.Adapter<ReservationsViewAdapter.ReservationsViewHolder>(){

    private val books = vModel.reservedBooks
    private val _cancellation = SingleLiveEvent<String>()

    init {
        books.observeForever {
            this.notifyDataSetChanged()
        }
    }

    @Suppress("unused")
    inner class ReservationsViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val card : CardView? = itemView.findViewById(R.id.cardReservationFrame)
        val bookQueue : TextView? = itemView.findViewById(R.id.cardReservationItemQueue)
        val bookTitle : TextView? = itemView.findViewById(R.id.cardReservationItemTitle)
        val bookCover : ImageView? = itemView.findViewById(R.id.cardReservationItemPhoto)
        val bookLibrary : TextView? = itemView.findViewById(R.id.cardReservationItemLibrary)
        val bookMaterial : TextView? = itemView.findViewById(R.id.cardReservationItemMaterial)
        val bookSituation : TextView? = itemView.findViewById(R.id.cardReservationItemSituation)
        val btnCancel : Button? = itemView.findViewById(R.id.cardReservationButtonCancel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservationsViewHolder {
        return ReservationsViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_book_reservation, parent, false))
    }

    override fun onBindViewHolder(holder: ReservationsViewHolder, position: Int) {
        books.value?.get(position).let { book ->
            holder.bookCover?.setImageResource(Constants.
                BOOK_COVER_PLACEHOLDERS[position % Constants.BOOK_COVER_PLACEHOLDERS.size])
            holder.bookQueue?.text = book?.queue
            holder.bookTitle?.text = book?.title
            holder.bookLibrary?.text = book?.library
            holder.bookMaterial?.text = book?.material
            holder.bookSituation?.text = book?.situation

            holder.btnCancel?.setOnClickListener { _cancellation.value = book?.linkCancel }
        }
    }

    override fun getItemCount(): Int {
        return books.value?.size ?: 0
    }

    fun cancellationRequest() : LiveData<String> { return _cancellation }
}