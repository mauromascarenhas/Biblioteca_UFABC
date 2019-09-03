package com.nintersoft.bibliotecaufabc.viewadapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nintersoft.bibliotecaufabc.R;
import com.nintersoft.bibliotecaufabc.book_renewal_model.BookRenewalProperties;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class BookLoansAdapter extends RecyclerView.Adapter<BookLoansAdapter.SearchBooksViewHolder> {

    @SuppressWarnings("WeakerAccess")
    public static class SearchBooksViewHolder extends RecyclerView.ViewHolder{
        TextView title;
        TextView library;
        TextView patrimony;
        TextView date;
        CardView cardView;
        ImageView bookCover;

        public SearchBooksViewHolder(View v){
            super(v);

            date = v.findViewById(R.id.card_renewal_item_date);
            title = v.findViewById(R.id.card_renewal_item_title);
            library = v.findViewById(R.id.card_renewal_item_library);
            bookCover = v.findViewById(R.id.card_renewal_item_photo);
            patrimony = v.findViewById(R.id.card_renewal_item_patrimony);

            cardView = v.findViewById(R.id.card_renewal_frame);

            v.findViewById(R.id.card_renewal_button).setVisibility(View.GONE);
        }
    }

    private ArrayList<BookRenewalProperties> properties;

    public BookLoansAdapter(@NonNull ArrayList<BookRenewalProperties> properties){
        this.properties = properties;
    }

    @NonNull
    @Override
    public SearchBooksViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SearchBooksViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_renewal_book_properties, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SearchBooksViewHolder holder, int position) {
        final BookRenewalProperties cProperties = properties.get(position);

        holder.date.setText(cProperties.getDate());
        holder.title.setText(cProperties.getTitle());
        holder.library.setText(cProperties.getLibrary());
        holder.patrimony.setText(cProperties.getPatrimony());
    }

    @Override
    public int getItemCount() {
        return properties.size();
    }
}
