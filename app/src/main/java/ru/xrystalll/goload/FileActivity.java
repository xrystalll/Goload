package ru.xrystalll.goload;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ru.xrystalll.goload.fullviewer.FullscreenActivity;
import ru.xrystalll.goload.support.CommentsAdapter;
import ru.xrystalll.goload.support.CommentModel;
import ru.xrystalll.goload.support.SettingsUtils;
import ru.xrystalll.goload.support.VolleySingleton;

public class FileActivity extends AppCompatActivity {

    private final String BASE_API_URL = "https://goload.ru";
    private NestedScrollView fileView;
    private View loader;
    private TextView text_error;
    private Button downloadBtn;
    private ImageView shareButton;
    private String fileId;
    private LinearLayout likeBtn;
    private ImageView likeIcon;
    private TextView likeCount;
    private TextView commentsTitle;
    private LinearLayout comment_input_bar;
    private String author;
    private EditText input;
    private EditText user_input;
    private RelativeLayout commError;
    private SimpleExoPlayer exoPlayer;
    private SharedPreferences sharedPref;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private final List<CommentModel> listItems = new ArrayList<>();
    private LinearLayoutManager layoutManager;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SettingsUtils settingsUtils = new SettingsUtils(getBaseContext());
        setTheme(settingsUtils.loadThemeState() ? R.style.LightTheme : R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.file_title);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        requestQueue = VolleySingleton.getInstance(this).getRequestQueue();
        sharedPref = getSharedPreferences("SharedSettings", Context.MODE_PRIVATE);
        loader = findViewById(R.id.fileLoader);
        fileView = findViewById(R.id.fileView);
        likeCount = findViewById(R.id.likeCount);
        text_error = findViewById(R.id.text_error);
        commentsTitle = findViewById(R.id.commentsTitle);
        recyclerView = findViewById(R.id.recyclerView);
        comment_input_bar = findViewById(R.id.comment_input_bar);
        input = findViewById(R.id.comment_input);
        user_input = findViewById(R.id.user_input);
        commError = findViewById(R.id.commError);

        Intent intent = getIntent();
        Uri data = intent.getData();

        if (getIntent().hasExtra("id")) {
            fileId = getIntent().getStringExtra("id");
        } else if (data != null && data.toString().contains("file")) {
            List<String> params = data.getPathSegments();
            String id = params.get(params.size() - 1).replace("file", "");
            fileId = !id.isEmpty() ? id : "";
        } else {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
        }

        loadData(fileId);

        layoutManager = new LinearLayoutManager(getBaseContext());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new CommentsAdapter(listItems, getBaseContext());
        recyclerView.setAdapter(adapter);

        loadComments(fileId, 0);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerview, int dx, int dy) {
                if (layoutManager.findLastCompletelyVisibleItemPosition() == listItems.size()-1) {
                    loadComments(fileId, layoutManager.getItemCount());
                }
            }
        });

        final String user = sharedPref.getString("UserName", "");
        if (!user.equalsIgnoreCase("")) {
            comment_input_bar.setWeightSum(2f);
            user_input.setVisibility(View.GONE);
            author = user;
        }

        user_input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_NEXT) {
                    if (user_input.getText().toString().trim().length() > 0) {
                        author = user_input.getText().toString().trim();
                        setUsername(author);
                        comment_input_bar.setWeightSum(2f);
                        user_input.setVisibility(View.GONE);
                    }
                    return true;
                }
                return false;
            }
        });
        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEND) {
                    String text = input.getText().toString().trim();
                    if (author != null) {
                        if (text.length() > 1) {
                            hideKeyboard(FileActivity.this);
                            writeComment(fileId, author, text);
                            input.getText().clear();
                        }
                    } else {
                        if (user_input.getText().toString().trim().length() > 0) {
                            author = user_input.getText().toString().trim();
                            setUsername(author);
                            comment_input_bar.setWeightSum(2f);
                            user_input.setVisibility(View.GONE);

                            if (text.length() > 1) {
                                hideKeyboard(FileActivity.this);
                                writeComment(fileId, author, text);
                                input.getText().clear();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_name_error), Toast.LENGTH_SHORT).show();
                        }
                    }
                    return true;
                }
                return false;
            }
        });
    }

    private void loadData(String fileId) {
        String URL_DATA = BASE_API_URL + "/api/file.php?id=" + fileId;
        showLoader();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL_DATA, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                hideLoader();

                try {
                    JSONObject jsonObject = new JSONObject(s);
                    JSONObject o = jsonObject.getJSONObject("data");

                    final String id = o.getString("id");
                    String author = o.getString("author");
                    String time = o.getString("time");
                    final String name = o.getString("name");
                    String text = o.getString("opis");
                    final String file = o.getString("file");
                    final String like = o.getString("like");
                    String comments = o.getString("comments");
                    String load = o.getString("load");
                    String views = o.getString("views");
                    String format = o.getString("format");
                    String password = o.getString("password");

                    fillCard(author, time, name, text, file, like, comments, load, views, format);

                    downloadBtn = findViewById(R.id.downloadBtn);
                    if (password.equals("false")) {
                        downloadBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Toast.makeText(getApplicationContext(),
                                        getString(R.string.downloading_notification), Toast.LENGTH_SHORT).show();
                                String filename = file.substring(file.lastIndexOf("/")+1);
                                download(BASE_API_URL + "/up" + id, name, filename);
                            }
                        });
                    } else {
                        downloadBtn.setVisibility(View.GONE);
                    }

                    shareButton = findViewById(R.id.share);
                    shareButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            share(BASE_API_URL + "/file" + id);
                        }
                    });

                    likeBtn = findViewById(R.id.like);
                    likeIcon = findViewById(R.id.likeIcon);

                    if (loadLikeState(id)) {
                        likeIcon.setColorFilter(getResources().getColor(R.color.colorAccent));
                        likeCount.setTextColor(getResources().getColor(R.color.colorAccent));
                    }

                    likeBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (!loadLikeState(id)) {
                                getLike(id, "like");

                                likeIcon.setColorFilter(getResources().getColor(R.color.colorAccent));
                                likeCount.setTextColor(getResources().getColor(R.color.colorAccent));

                                setLikeState(true, id);
                            } else {
                                getLike(id, "dislike");

                                likeIcon.setColorFilter(getResources().getColor(R.color.colorActionIcon));
                                likeCount.setTextColor(getResources().getColor(R.color.colorActionIcon));

                                setLikeState(false, id);
                            }
                        }
                    });

                } catch (JSONException e) {
                    text_error.setText(R.string.file_not_found);
                    showError();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (hasNetwork()) {
                    text_error.setText(R.string.file_not_found);
                    Toast.makeText(getApplicationContext(), volleyError.getMessage(), Toast.LENGTH_LONG).show();
                } else {
                    text_error.setText(R.string.check_connection_error);
                }
                showError();
            }
        });

        requestQueue.add(stringRequest);
    }

    private void fillCard(
            String author, String time, String fileName, @NonNull String description, final String file,
            String likeCount, String commentsCount, String downloadCount, String viewsCount, String format) {

        TextView textViewAuthor = findViewById(R.id.author);
        TextView textViewTime = findViewById(R.id.time);
        TextView textViewFileName = findViewById(R.id.fileName);
        TextView textViewDescription = findViewById(R.id.description);
        final ImageView imageViewImagePreview = findViewById(R.id.imagePreview);
        SimpleExoPlayerView exoPlayerView = findViewById(R.id.videoPreview);
        TextView textViewLikeCount = findViewById(R.id.likeCount);
        TextView textViewCommentsCount = findViewById(R.id.commentsCount);
        TextView textViewDownloadCount = findViewById(R.id.downloadCount);
        TextView textViewViewsCount = findViewById(R.id.viewsCount);
        RelativeLayout videoBlock = findViewById(R.id.videoBlock);

        if (description.length() != 0) {
            textViewDescription.setVisibility(View.VISIBLE);
        }

        textViewAuthor.setText(author);

        long timestamp = Long.parseLong(time) * 1000L;
        textViewTime.setText(getDate(timestamp));

        textViewFileName.setText(fileName);
        textViewDescription.setText(description);

        if (format.equals("png") || format.equals("jpg") || format.equals("jpeg") || format.equals("gif") || format.equals("ico")) {
            videoBlock.setVisibility(View.GONE);

            Picasso.get()
                    .load(file)
                    .into(imageViewImagePreview);

            imageViewImagePreview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(FileActivity.this, FullscreenActivity.class);
                    i.putExtra("passingImage", file);

                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            FileActivity.this, findViewById(R.id.imagePreview), "image");
                    startActivity(i, options.toBundle());
                }
            });

        } else if (format.equals("mp4") || format.equals("3gp") || format.equals("avi") || format.equals("webm")) {
            videoBlock.setVisibility(View.VISIBLE);
            imageViewImagePreview.setVisibility(View.GONE);

            try {
                BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
                TrackSelector trackSelector = new DefaultTrackSelector(new AdaptiveTrackSelection.Factory(bandwidthMeter));
                exoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
                Uri uri = Uri.parse(file);
                DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory("goload_video");
                ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
                MediaSource mediaSource = new ExtractorMediaSource(uri, dataSourceFactory, extractorsFactory, null,
                        null);
                exoPlayerView.setPlayer(exoPlayer);
                exoPlayer.prepare(mediaSource);

                View controlView = exoPlayerView.findViewById(R.id.exo_controller);
                controlView.findViewById(R.id.exo_fullscreen_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        exoPlayer.stop();
                        Intent i = new Intent(FileActivity.this, FullscreenActivity.class);
                        i.putExtra("passingVideo", file);

                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                                FileActivity.this, findViewById(R.id.videoPreview), "video");
                        startActivity(i, options.toBundle());
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            imageViewImagePreview.setVisibility(View.GONE);
            videoBlock.setVisibility(View.GONE);
        }

        textViewLikeCount.setText(counter(likeCount));
        textViewCommentsCount.setText(counter(commentsCount));
        textViewDownloadCount.setText(counter(downloadCount));
        textViewViewsCount.setText(counter(viewsCount));
    }

    private void download(String link, String name, String filename) {
        if (getPermission()) {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(link));
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                    DownloadManager.Request.NETWORK_MOBILE);
            request.setTitle(name);
            request.setDescription(getString(R.string.downloading_notification));

            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);

            DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            manager.enqueue(request);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    private void share(String text) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, "Share file");
        startActivity(shareIntent);
    }

    @NonNull
    private String getDate(long timestamp) {
        SettingsUtils settingsUtils = new SettingsUtils(getBaseContext());
        String localeState = settingsUtils.getLocaleString();
        String lang = localeState != null ? localeState : "en";

        Locale locale = new Locale(lang);

        Calendar unix = Calendar.getInstance();
        unix.setTimeInMillis(timestamp);
        Calendar now = Calendar.getInstance();

        Date netDate = (new Date(timestamp));
        if (now.get(Calendar.DATE) == unix.get(Calendar.DATE)) {
            SimpleDateFormat sdt = new SimpleDateFormat("H:mm", locale);
            String format = sdt.format(netDate);
            return getString(R.string.today) + " " + format;
        } else if (now.get(Calendar.DATE) - unix.get(Calendar.DATE) == 1) {
            SimpleDateFormat sdt = new SimpleDateFormat("H:mm", locale);
            String format = sdt.format(netDate);
            return getString(R.string.yesterday) + " " + format;
        } else if (now.get(Calendar.YEAR) == unix.get(Calendar.YEAR)) {
            SimpleDateFormat sdt = new SimpleDateFormat("dd MMM '" + getString(R.string.at) + "' H:mm", locale);
            return sdt.format(netDate);
        } else {
            SimpleDateFormat sdt = new SimpleDateFormat("dd MMM yyyy '" + getString(R.string.at) + "' H:mm", locale);
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

    private void getLike(String id, String action) {
        String URL_DATA = BASE_API_URL + "/api/like.php?id=" + id + "&" + action;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL_DATA, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                try {
                    JSONObject o = new JSONObject(s);
                    String likes = o.getString("count");

                    likeCount.setText(counter(likes));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(getApplicationContext(), volleyError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        requestQueue.add(stringRequest);
    }

    private void setLikeState(Boolean state, String id) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("Like" + id, state);
        editor.apply();
    }

    @NonNull
    private Boolean loadLikeState(String id) {
        return sharedPref.getBoolean("Like" + id, false);
    }

    private void loadComments(String fileId, int offset) {
        String URL_DATA = BASE_API_URL + "/api/comments.php?id=" + fileId + "&limit=10&offset=" + offset;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL_DATA, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {

                if (s.contains("No comments yet")) {
                    showCommentsError();
                } else {
                    try {
                        JSONObject jsonObject = new JSONObject(s);
                        JSONArray array = jsonObject.getJSONArray("comments");

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject o = array.getJSONObject(i);
                            CommentModel item = new CommentModel(
                                    o.getString("id"),
                                    o.getString("file"),
                                    o.getString("author"),
                                    o.getString("userPhoto"),
                                    o.getString("text"),
                                    o.getString("like"),
                                    o.getString("time")
                            );
                            listItems.add(item);
                        }

                        commentsTitle.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                showCommentsError();
                if (hasNetwork()) {
                    Toast.makeText(getApplicationContext(), volleyError.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        stringRequest.setShouldCache(false);
        requestQueue.add(stringRequest);
    }

    private void writeComment(final String id, final String author, final String text) {
        String URL_DATA = BASE_API_URL + "/api/writecomment.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_DATA, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                if (!s.contains("error")) {
                    try {
                        JSONObject jsonObject = new JSONObject(s);
                        JSONObject o = jsonObject.getJSONObject("data");

                        CommentModel item = new CommentModel(
                                o.getString("id"),
                                o.getString("file"),
                                o.getString("author"),
                                o.getString("userPhoto"),
                                o.getString("text"),
                                o.getString("like"),
                                o.getString("time")
                        );
                        listItems.add(0, item);

                        commentsTitle.setVisibility(View.VISIBLE);
                        hideCommentsError();
                        adapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(getApplicationContext(), volleyError.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("id", id);
                params.put("author", author);
                params.put("text", text);
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }

    private void setUsername(String name) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("UserName", name);
        editor.apply();
    }

    private void showLoader() {
        fileView.setVisibility(View.GONE);
        comment_input_bar.setVisibility(View.GONE);
        loader.setVisibility(View.VISIBLE);
    }

    private void hideLoader() {
        fileView.setVisibility(View.VISIBLE);
        comment_input_bar.setVisibility(View.VISIBLE);
        loader.setVisibility(View.GONE);
    }

    private void showError() {
        fileView.setVisibility(View.GONE);
        comment_input_bar.setVisibility(View.GONE);
        loader.setVisibility(View.GONE);
        RelativeLayout fileError = findViewById(R.id.fileError);
        fileError.setVisibility(View.VISIBLE);
    }

    private void showCommentsError() {
        recyclerView.setVisibility(View.GONE);
        commError.setVisibility(View.VISIBLE);
    }

    private void hideCommentsError() {
        recyclerView.setVisibility(View.VISIBLE);
        commError.setVisibility(View.GONE);
    }

    private boolean getPermission() {
        String permission = "android.permission.WRITE_EXTERNAL_STORAGE";
        return checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onDestroy() {
        if (exoPlayer != null) {
            exoPlayer.stop();
            exoPlayer.release();
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        } else if (item.getItemId() == R.id.action_search) {
            Intent i = new Intent(this, SearchActivity.class);
            startActivity(i);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean hasNetwork() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private static void hideKeyboard(@NonNull Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        assert inputMethodManager != null;
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

}
