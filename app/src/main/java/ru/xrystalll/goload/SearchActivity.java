package ru.xrystalll.goload;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
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

    private RecyclerView.Adapter adapter;
    private List<ItemModel> listItems = new ArrayList<>();
    private View loader;
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

        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new FilesAdapter(listItems, this);
        recyclerView.setAdapter(adapter);

        input.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent keyevent) {
                if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    if (query != null) {
                        listItems.clear();
                        adapter.notifyDataSetChanged();
                        showLoader();
                        query = input.getText().toString();
                        loadData(query, 0);
                    }
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

                            adapter.notifyDataSetChanged();
                            hideLoader();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        hideLoader();
                        Toast.makeText(SearchActivity.this, volleyError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(SearchActivity.this);
        requestQueue.getCache().clear();
        stringRequest.setShouldCache(false);
        requestQueue.add(stringRequest);
    }

    private void showLoader() {
        loader.setVisibility(View.VISIBLE);
    }

    private void hideLoader() {
        loader.setVisibility(View.GONE);
    }

}
