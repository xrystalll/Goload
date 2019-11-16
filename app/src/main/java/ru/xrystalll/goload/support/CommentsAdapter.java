package ru.xrystalll.goload.support;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ru.xrystalll.goload.R;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {

    private final String BASE_API_URL = "https://goload.ru";
    private final List<CommentModel> listItems;
    private final Context context;
    private SharedPreferences sharedPref;
    private RequestQueue requestQueue;

    public CommentsAdapter(List<CommentModel> listItems, Context context) {
        this.listItems = listItems;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        ViewHolder holder = new ViewHolder(v);

        requestQueue = VolleySingleton.getInstance(context).getRequestQueue();
        sharedPref = context.getSharedPreferences("SharedSettings", Context.MODE_PRIVATE);

        return holder;
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

        if (loadLikeCommState(listItem.getId())) {
            holder.likeIcon.setColorFilter(context.getResources().getColor(R.color.colorAccent));
            holder.textViewLikeCount.setTextColor(context.getResources().getColor(R.color.colorAccent));
        } else {
            holder.likeIcon.setColorFilter(context.getResources().getColor(R.color.colorActionIcon));
            holder.textViewLikeCount.setTextColor(context.getResources().getColor(R.color.colorActionIcon));
        }

        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!loadLikeCommState(listItem.getId())) {

                    String URL_DATA = BASE_API_URL + "/api/likecomment.php?id=" + listItem.getId() + "&like";

                    StringRequest stringRequest = new StringRequest(Request.Method.GET, URL_DATA, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String s) {
                            try {
                                JSONObject o = new JSONObject(s);
                                String likes = o.getString("count");

                                holder.textViewLikeCount.setText(counter(likes));

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            Toast.makeText(context, volleyError.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

                    requestQueue.add(stringRequest);

                    holder.likeIcon.setColorFilter(context.getResources().getColor(R.color.colorAccent));
                    holder.textViewLikeCount.setTextColor(context.getResources().getColor(R.color.colorAccent));

                    setLikeCommState(true, listItem.getId());
                } else {

                    String URL_DATA = BASE_API_URL + "/api/likecomment.php?id=" + listItem.getId() + "&dislike";

                    StringRequest stringRequest = new StringRequest(Request.Method.GET, URL_DATA, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String s) {
                            try {
                                JSONObject o = new JSONObject(s);
                                String likes = o.getString("count");

                                holder.textViewLikeCount.setText(counter(likes));

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            Toast.makeText(context, volleyError.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

                    requestQueue.add(stringRequest);

                    holder.likeIcon.setColorFilter(context.getResources().getColor(R.color.colorActionIcon));
                    holder.textViewLikeCount.setTextColor(context.getResources().getColor(R.color.colorActionIcon));

                    setLikeCommState(false, listItem.getId());
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        final TextView textViewAuthor;
        final ImageView imageViewUserPhoto;
        final TextView textViewText;
        final TextView textViewLikeCount;
        final TextView textViewTime;
        final LinearLayout like;
        final ImageView likeIcon;

        ViewHolder(View itemView) {
            super(itemView);

            textViewAuthor = itemView.findViewById(R.id.author);
            imageViewUserPhoto = itemView.findViewById(R.id.userPhoto);
            textViewText = itemView.findViewById(R.id.text);
            textViewLikeCount = itemView.findViewById(R.id.likeCount);
            textViewTime = itemView.findViewById(R.id.time);
            like = itemView.findViewById(R.id.like);
            likeIcon = itemView.findViewById(R.id.likeIcon);
        }
    }

    @NonNull
    private String getDate(long timestamp) {
        SettingsUtils settingsUtils = new SettingsUtils(context);
        String localeState = settingsUtils.getLocaleString();
        String lang = localeState != null ? localeState : "en";

        Locale locale = new Locale(lang);

        Calendar unix = Calendar.getInstance();
        unix.setTimeInMillis(timestamp);
        Calendar now = Calendar.getInstance();

        Date netDate = (new Date(timestamp));
        if (now.get(Calendar.YEAR) == unix.get(Calendar.YEAR)) {
            if (now.get(Calendar.MONTH) == unix.get(Calendar.MONTH)) {
                if (now.get(Calendar.DATE) == unix.get(Calendar.DATE)) {
                    SimpleDateFormat sdt = new SimpleDateFormat("H:mm", locale);
                    String format = sdt.format(netDate);
                    return context.getString(R.string.today) + " " + format;
                } else if (now.get(Calendar.DATE) - unix.get(Calendar.DATE) == 1) {
                    SimpleDateFormat sdt = new SimpleDateFormat("H:mm", locale);
                    String format = sdt.format(netDate);
                    return context.getString(R.string.yesterday) + " " + format;
                } else {
                    SimpleDateFormat sdt = new SimpleDateFormat("dd MMM '" + context.getString(R.string.at) + "' H:mm", locale);
                    return sdt.format(netDate);
                }
            } else {
                SimpleDateFormat sdt = new SimpleDateFormat("dd MMM '" + context.getString(R.string.at) + "' H:mm", locale);
                return sdt.format(netDate);
            }
        } else {
            SimpleDateFormat sdt = new SimpleDateFormat("dd MMM yyyy '" + context.getString(R.string.at) + "' H:mm", locale);
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

    private void setLikeCommState(Boolean state, String id) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("LikeComm" + id, state);
        editor.apply();
    }

    @NonNull
    private Boolean loadLikeCommState(String id) {
        return sharedPref.getBoolean("LikeComm" + id, false);
    }

}
