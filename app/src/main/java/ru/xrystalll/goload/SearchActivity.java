package ru.xrystalll.goload;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ru.xrystalll.goload.support.FilesAdapter;
import ru.xrystalll.goload.support.ItemModel;
import ru.xrystalll.goload.support.ThemePreference;

public class SearchActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private final List<ItemModel> listItems = new ArrayList<>();
    private View loader;
    private View error;
    private TextView text_error;
    private LinearLayoutManager layoutManager;
    private String query = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ThemePreference themePref = new ThemePreference(getBaseContext());
        setTheme(themePref.loadNightModeState() ? R.style.LightTheme : R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final EditText input = toolbar.findViewById(R.id.search_input);
        input.setVisibility(View.VISIBLE);
        input.requestFocus();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        loader = findViewById(R.id.recyclerLoader);
        error = findViewById(R.id.searchError);
        recyclerView = findViewById(R.id.recyclerView);
        text_error = findViewById(R.id.text_error);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new FilesAdapter(listItems, this);
        recyclerView.setAdapter(adapter);

        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEARCH) {
                    hideKeyboard(SearchActivity.this);
                    listItems.clear();
                    adapter.notifyDataSetChanged();
                    showLoader();
                    query = input.getText().toString();
                    loadData(query, 0);
                    return true;
                }
                return false;
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (layoutManager.findLastCompletelyVisibleItemPosition() == listItems.size()-1) {
                    if (query != null) {
                        loadData(query, layoutManager.getItemCount());
                    }
                }
            }
        });

    }

    private void loadData(String query, int offset) {
        String URL_DATA = "https://goload.ru/api/search.php?q=" + query + "&limit=10&offset=" + offset;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL_DATA,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {

                        if (s.contains("nulltag")) {
                            text_error.setText(R.string.nothing_found);
                            showError();
                        } else {
                            try {
                                JSONObject jsonObject = new JSONObject(s);
                                JSONArray array = jsonObject.getJSONArray("data");

                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject o = array.getJSONObject(i);
                                    ItemModel item = new ItemModel(
                                            o.getString("id"),
                                            o.getString("author"),
                                            o.getString("time"),
                                            o.getString("name"),
                                            o.getString("file"),
                                            o.getString("like"),
                                            o.getString("comments"),
                                            o.getString("load"),
                                            o.getString("views"),
                                            o.getString("format")
                                    );
                                    listItems.add(item);
                                }

                                hideLoader();
                                adapter.notifyDataSetChanged();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        if (hasNetwork()) {
                            text_error.setText(R.string.nothing_found);
                            Toast.makeText(SearchActivity.this, volleyError.getMessage(), Toast.LENGTH_LONG).show();
                        } else {
                            text_error.setText(R.string.check_connection_error);
                        }
                        showError();
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(SearchActivity.this);
        requestQueue.getCache().clear();
        stringRequest.setShouldCache(false);
        requestQueue.add(stringRequest);
    }

    private void showLoader() {
        recyclerView.setVisibility(View.GONE);
        error.setVisibility(View.GONE);
        loader.setVisibility(View.VISIBLE);
    }

    private void hideLoader() {
        loader.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void showError() {
        recyclerView.setVisibility(View.GONE);
        loader.setVisibility(View.GONE);
        error.setVisibility(View.VISIBLE);
    }

    private static void hideKeyboard(@NonNull Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        assert inputMethodManager != null;
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    private boolean hasNetwork() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();

    }

}
