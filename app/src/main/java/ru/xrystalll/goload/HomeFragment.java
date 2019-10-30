package ru.xrystalll.goload;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import ru.xrystalll.goload.support.SettingsUtils;

public class HomeFragment extends Fragment {

    private final String BASE_API_URL = "https://goload.ru";
    private RecyclerView.Adapter adapter;
    private final List<ItemModel> listItems = new ArrayList<>();
    private RecyclerView recyclerView;
    private View loader;
    private View itemLoader;
    private RelativeLayout connError;
    private LinearLayoutManager layoutManager;
    private SwipeRefreshLayout swipeLayout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        loader = view.findViewById(R.id.recyclerLoader);
        itemLoader = view.findViewById(R.id.itemLoader);
        connError = view.findViewById(R.id.connError);

        loadData(0);

        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new FilesAdapter(listItems, getContext());
        recyclerView.setAdapter(adapter);

        swipeLayout = view.findViewById(R.id.swipeLayout);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                listItems.clear();
                adapter.notifyDataSetChanged();

                loadData(0);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeLayout.setRefreshing(false);
                    }
                }, 1000);
            }
        });
        SettingsUtils settingsUtils = new SettingsUtils(getActivity());
        int progressBackground = settingsUtils.loadThemeState() ? R.color.colorCardLight : R.color.colorCard;

        swipeLayout.setProgressBackgroundColorSchemeColor(getResources().getColor(progressBackground));
        swipeLayout.setColorSchemeColors(getResources().getColor(R.color.colorAccent));

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerview, int dx, int dy) {
                if (hasNetwork()) {
                    connError.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
                if (layoutManager.findLastCompletelyVisibleItemPosition() == listItems.size()-1) {
                    showItemLoader();
                    loadData(layoutManager.getItemCount());
                }
            }
        });

        return view;
    }

    private void loadData(int offset) {
        String URL_DATA = BASE_API_URL + "/api/all.php?limit=10&offset=" + offset;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL_DATA,
                new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                hideLoader();

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
                                o.getString("format"),
                                o.getString("thumbnail")
                        );
                        listItems.add(item);
                    }

                    adapter.notifyDataSetChanged();
                    hideItemLoader();

                } catch (JSONException e) {
                    hideItemLoader();
                    e.printStackTrace();
                }
            }
        },
        new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                hideLoader();
                hideItemLoader();
                if (hasNetwork()) {
                    Toast.makeText(getActivity(), volleyError.getMessage(), Toast.LENGTH_LONG).show();
                } else {
                    showError();
                }
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());
        requestQueue.getCache().clear();
        stringRequest.setShouldCache(false);
        requestQueue.add(stringRequest);
    }

    private void hideLoader() {
        loader.setVisibility(View.GONE);
    }

    private void showItemLoader() {
        itemLoader.setVisibility(View.VISIBLE);
    }

    private void hideItemLoader() {
        itemLoader.setVisibility(View.GONE);
    }

    private void showError() {
        recyclerView.setVisibility(View.GONE);
        connError.setVisibility(View.VISIBLE);
    }

    private boolean hasNetwork() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

}
