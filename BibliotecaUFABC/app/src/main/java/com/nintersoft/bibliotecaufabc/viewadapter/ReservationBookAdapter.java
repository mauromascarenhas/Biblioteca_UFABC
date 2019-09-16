package com.nintersoft.bibliotecaufabc.viewadapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nintersoft.bibliotecaufabc.R;
import com.nintersoft.bibliotecaufabc.activities.ReservationActivity;
import com.nintersoft.bibliotecaufabc.bookreservationproperties.BookReservationProperties;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class ReservationBookAdapter extends RecyclerView.Adapter<ReservationBookAdapter.SearchBooksViewHolder> {

    @SuppressWarnings("WeakerAccess")
    public static class SearchBooksViewHolder extends RecyclerView.ViewHolder{
        Button cancelButton;
        TextView queue;
        TextView title;
        TextView library;
        TextView material;
        TextView situation;
        CardView cardView;
        ImageView bookCover;

        public SearchBooksViewHolder(View v){
            super(v);

            queue = v.findViewById(R.id.card_reservation_item_queue);
            title = v.findViewById(R.id.card_reservation_item_title);
            library = v.findViewById(R.id.card_reservation_item_library);
            material = v.findViewById(R.id.card_reservation_item_material);
            situation = v.findViewById(R.id.card_reservation_item_situation);
            bookCover = v.findViewById(R.id.card_reservation_item_photo);
            cancelButton = v.findViewById(R.id.card_reservation_button_cancel);

            cardView = v.findViewById(R.id.card_reservation_frame);
        }
    }

    private Context mContext;
    private ArrayList<BookReservationProperties> properties;

    public ReservationBookAdapter(@NonNull Context context, @NonNull ArrayList<BookReservationProperties> properties){
        this.properties = properties;
        mContext = context;
    }

    @NonNull
    @Override
    public SearchBooksViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SearchBooksViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_reservation_book_properties, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SearchBooksViewHolder holder, int position) {
        final BookReservationProperties cProperties = properties.get(position);

        holder.queue.setText(cProperties.getQueue());
        holder.title.setText(cProperties.getTitle());
        holder.library.setText(cProperties.getLibrary());
        holder.material.setText(cProperties.getMaterial());
        holder.situation.setText(cProperties.getSituation());

        holder.cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ReservationActivity)mContext).loadCancellationLink(cProperties.getCancelLink());
            }
        });
    }

    @Override
    public int getItemCount() {
        return properties.size();
    }
}
