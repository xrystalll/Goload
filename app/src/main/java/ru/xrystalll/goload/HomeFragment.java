package ru.xrystalll.goload;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import ru.xrystalll.goload.support.ThemePreference;

public class HomeFragment extends Fragment {

    private RecyclerView.Adapter adapter;
    private List<ItemModel> listItems = new ArrayList<>();
    private View loader;
    private View itemLoader;
    private LinearLayoutManager layoutManager;
    private SwipeRefreshLayout swipeLayout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        loader = view.findViewById(R.id.recyclerLoader);
        itemLoader = view.findViewById(R.id.itemLoader);

        loadData(0);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);

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
        ThemePreference themePref = new ThemePreference(getActivity());
        int progressBackground = themePref.loadNightModeState() ? R.color.colorCardLight : R.color.colorCard;

        swipeLayout.setProgressBackgroundColorSchemeColor(getResources().getColor(progressBackground));
        swipeLayout.setColorSchemeColors(getResources().getColor(R.color.colorAccent));

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (layoutManager.findLastCompletelyVisibleItemPosition() == listItems.size()-1) {
                    showItemLoader();
                    loadData(layoutManager.getItemCount());
                }
            }
        });

        return view;
    }

    private void loadData(int offset) {
        String URL_DATA = "https://goload.ru/api/all.php?limit=10&offset=" + offset;

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
                                        o.getString("format")
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
                        Toast.makeText(getActivity(), volleyError.getMessage(), Toast.LENGTH_LONG).show();
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

}
