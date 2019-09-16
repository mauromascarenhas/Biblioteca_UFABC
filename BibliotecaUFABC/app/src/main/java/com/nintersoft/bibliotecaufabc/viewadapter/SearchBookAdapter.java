package com.nintersoft.bibliotecaufabc.viewadapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.nintersoft.bibliotecaufabc.activities.BookViewerActivity;
import com.nintersoft.bibliotecaufabc.R;
import com.nintersoft.bibliotecaufabc.book_search_model.BookSearchProperties;
import com.nintersoft.bibliotecaufabc.utilities.GlobalConstants;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class SearchBookAdapter extends RecyclerView.Adapter<SearchBookAdapter.SearchBooksViewHolder> {

    @SuppressWarnings("WeakerAccess")
    public static class SearchBooksViewHolder extends RecyclerView.ViewHolder{
        TextView type;
        TextView title;
        TextView author;
        TextView section;
        CardView cardView;
        ImageView bookCover;

        public SearchBooksViewHolder(View v){
            super(v);

            type = v.findViewById(R.id.card_item_type);
            title = v.findViewById(R.id.card_item_title);
            author = v.findViewById(R.id.card_item_author);
            section = v.findViewById(R.id.card_item_section);
            cardView = v.findViewById(R.id.card_frame);
            bookCover = v.findViewById(R.id.card_item_photo);
        }
    }

    private Context mContext;
    private ArrayList<BookSearchProperties> properties;

    public SearchBookAdapter(@NonNull Context context,@NonNull ArrayList<BookSearchProperties> properties){
        this.properties = properties;
        mContext = context;
    }

    @NonNull
    @Override
    public SearchBooksViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SearchBooksViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_book_properties, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SearchBooksViewHolder holder, int position) {
        final BookSearchProperties cProperties = properties.get(position);

        holder.type.setText(cProperties.getType());
        holder.title.setText(cProperties.getTitle());
        holder.author.setText(cProperties.getAuthor());
        holder.section.setText(cProperties.getSection());
        Glide.with(mContext)
                .load(GlobalConstants.URL_LIBRARY_BOOK_COVER +
                                            GlobalConstants.MANDATORY_APPEND_URL_LIBRARY_BOOK_COVER + cProperties.getCode())
                .placeholder(R.drawable.ic_default_book)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.bookCover);

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadBookDetails(cProperties.getCode());
            }
        });

        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setItems(R.array.item_context_options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case 0: loadBookDetails(cProperties.getCode()); break;
                            case 1: shareBookDetails(cProperties); break;
                        }
                    }
                });
                builder.create().show();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return properties.size();
    }

    private void loadBookDetails(String code){
        Intent viewer = new Intent(mContext, BookViewerActivity.class);
        viewer.putExtra("code", code);
        mContext.startActivity(viewer);
    }

    private void shareBookDetails(BookSearchProperties book){
        String bookShare = mContext.getString(R.string.share_book_structure,
                book.getTitle(), book.getAuthor(),
                GlobalConstants.URL_LIBRARY_DETAILS + "?codigo=" + book.getCode()
                        + GlobalConstants.MANDATORY_APPEND_URL_LIBRARY_DETAILS);

        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, bookShare);
        if (share.resolveActivity(mContext.getPackageManager()) != null)
            mContext.startActivity(Intent.createChooser(share, mContext.getString(R.string.intent_share_book)));
    }

}
