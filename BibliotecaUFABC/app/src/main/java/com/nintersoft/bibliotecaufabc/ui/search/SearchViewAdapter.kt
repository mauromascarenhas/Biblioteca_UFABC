package com.nintersoft.bibliotecaufabc.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.nintersoft.bibliotecaufabc.R
import com.nintersoft.bibliotecaufabc.global.Constants
import com.nintersoft.bibliotecaufabc.model.AppContext
import com.nintersoft.bibliotecaufabc.model.search.BookSearch

class SearchViewAdapter (viewModel : SearchViewModel):
    RecyclerView.Adapter<SearchViewAdapter.HomeBookViewHolder>(){

    private val books = viewModel.searchResults
    private val glideConf = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
    private val _clickedBook = SingleLiveEvent<BookSearch>()
    private var _selectedBook = SingleLiveEvent<BookSearch>()

    init {
        books.observeForever{
            this.notifyDataSetChanged()
        }
    }

    inner class HomeBookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: CardView? = itemView.findViewById(R.id.card_search_frame)
        val bookType: TextView? = itemView.findViewById(R.id.card_item_type)
        val bookTitle: TextView? = itemView.findViewById(R.id.card_item_title)
        val bookCover: ImageView? = itemView.findViewById(R.id.card_item_photo)
        val bookAuthor : TextView? = itemView.findViewById(R.id.card_item_author)
        val bookSection : TextView? = itemView.findViewById(R.id.card_item_section)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HomeBookViewHolder {
        return HomeBookViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_book_search, parent, false))
    }

    override fun onBindViewHolder(holder: HomeBookViewHolder, position: Int) {
        val book = books.value?.get(position)

        holder.bookType?.text = book?.type
        holder.bookTitle?.text = book?.title
        holder.bookAuthor?.text = book?.author
        holder.bookSection?.text = book?.section
        Glide.with(AppContext.context!!)
            .load(Constants.URL_LIBRARY_BOOK_COVER +
                    "${Constants.MANDATORY_APPEND_URL_LIBRARY_BOOK_COVER}${book?.code}")
            .apply(glideConf.placeholder(Constants.
                BOOK_COVER_PLACEHOLDERS[position % Constants.BOOK_COVER_PLACEHOLDERS.size])).
                into(holder.bookCover!!)

        holder.card?.setOnClickListener { _clickedBook.value = book }

        holder.card?.setOnLongClickListener {
            _selectedBook.value = book
            true
        }

    }

    override fun getItemCount(): Int { return books.value?.size ?: 0 }

    fun clickedBook() : LiveData<BookSearch> { return _clickedBook }

    fun selectedBook() : LiveData<BookSearch> { return _selectedBook }
}