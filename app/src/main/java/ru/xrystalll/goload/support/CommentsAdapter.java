package ru.xrystalll.goload.support;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ru.xrystalll.goload.R;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {

    private final List<CommentModel> listItems;
    private final Context context;
    public CommentsAdapter(List<CommentModel> listItems, Context context) {
        this.listItems = listItems;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final CommentModel listItem = listItems.get(position);

        holder.textViewAuthor.setText(listItem.getAuthor());

        long timestamp = Long.parseLong(listItem.getTime()) * 1000L;
        holder.textViewTime.setText(getDate(timestamp));

        Picasso.get()
                .load(listItem.getUserPhoto())
                .noFade()
                .into(holder.imageViewUserPhoto);

        holder.textViewText.setText(listItem.getText());
        holder.textViewLikeCount.setText(counter(listItem.getLikeCount()));

    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        final TextView textViewAuthor;
        final ImageView imageViewUserPhoto;
        final TextView textViewText;
        final TextView textViewLikeCount;
        final TextView textViewTime;

        ViewHolder(View itemView) {
            super(itemView);

            textViewAuthor = itemView.findViewById(R.id.author);
            imageViewUserPhoto = itemView.findViewById(R.id.userPhoto);
            textViewText = itemView.findViewById(R.id.text);
            textViewLikeCount = itemView.findViewById(R.id.likeCount);
            textViewTime = itemView.findViewById(R.id.time);
        }
    }

    @NonNull
    private String getDate(long timestamp) {
        Calendar unix = Calendar.getInstance();
        unix.setTimeInMillis(timestamp);
        Calendar now = Calendar.getInstance();

        Date netDate = (new Date(timestamp));
        if (now.get(Calendar.DATE) == unix.get(Calendar.DATE)) {
            SimpleDateFormat sdt = new SimpleDateFormat("H:mm", Locale.getDefault());
            String format = sdt.format(netDate);
            return context.getString(R.string.today) + " " + format;
        } else if (now.get(Calendar.DATE) - unix.get(Calendar.DATE) == 1) {
            SimpleDateFormat sdt = new SimpleDateFormat("H:mm", Locale.US);
            String format = sdt.format(netDate);
            return context.getString(R.string.yesterday) + " " + format;
        } else if (now.get(Calendar.YEAR) == unix.get(Calendar.YEAR)) {
            SimpleDateFormat sdt = new SimpleDateFormat("dd MMM '" + context.getString(R.string.at) + "' H:mm", Locale.getDefault());
            return sdt.format(netDate);
        } else {
            SimpleDateFormat sdt = new SimpleDateFormat("dd MMM yyyy '" + context.getString(R.string.at) + "' H:mm", Locale.getDefault());
            return sdt.format(netDate);
        }
    }

    private String counter(String count) {
        int num = Integer.parseInt(count);
        if (num > 0) {
            return count;
        } else {
            return "";
        }
    }

}
