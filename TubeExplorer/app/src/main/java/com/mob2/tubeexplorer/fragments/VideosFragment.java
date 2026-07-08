package com.mob2.tubeexplorer.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.mob2.tubeexplorer.R;
import com.mob2.tubeexplorer.adapter.VideoAdapter;
import com.mob2.tubeexplorer.model.VideoItem;
import com.mob2.tubeexplorer.network.FetchVideosTask;
import com.mob2.tubeexplorer.util.NotificationHelper;
import com.mob2.tubeexplorer.viewmodel.SharedViewModel;

import java.util.ArrayList;

/**
 * VideosFragment (TAB 1)
 * ----------------------
 * - Displays the API content in a RecyclerView (required)
 * - Uses AsyncTask (FetchVideosTask) to load data in the background (required)
 * - Shows a ProgressBar loading indicator while fetching (required)
 * - Fires success / error Notifications (required)
 * - Pull-to-refresh via SwipeRefreshLayout (optional extra marks)
 * - Search: observes the query typed in the Options Menu (optional extra marks)
 */
public class VideosFragment extends Fragment
        implements FetchVideosTask.VideosCallback, VideoAdapter.OnVideoClickListener {

    private static final String DEFAULT_QUERY = "android development tutorial";

    private SharedViewModel viewModel;
    private VideoAdapter adapter;

    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefresh;

    private String currentQuery = DEFAULT_QUERY;
    private boolean isRefreshing = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_videos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Shared with DetailsFragment through the Activity scope
        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        progressBar = view.findViewById(R.id.progressBar);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerVideos);

        adapter = new VideoAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Pull-to-refresh (optional feature)
        swipeRefresh.setColorSchemeResources(R.color.red_primary);
        swipeRefresh.setOnRefreshListener(() -> {
            isRefreshing = true;
            fetchVideos(currentQuery);
        });

        // Search typed in the Options Menu SearchView (optional feature)
        viewModel.getSearchQuery().observe(getViewLifecycleOwner(), query -> {
            if (query != null && !query.trim().isEmpty()) {
                currentQuery = query.trim();
                fetchVideos(currentQuery);
            }
        });

        // Refresh menu item (Options Menu in MainActivity)
        viewModel.getRefreshRequest().observe(getViewLifecycleOwner(), refresh -> {
            if (Boolean.TRUE.equals(refresh)) {
                viewModel.refreshHandled();
                fetchVideos(currentQuery);
            }
        });

        // First load (only if we don't already have data, e.g. after rotation)
        if (viewModel.getVideoList().getValue() == null) {
            fetchVideos(currentQuery);
        } else {
            adapter.setVideos(viewModel.getVideoList().getValue());
        }
    }

    /** Start the AsyncTask that fetches videos in the background. */
    private void fetchVideos(String query) {
        new FetchVideosTask(this).execute(query);
    }

    // ---------------- FetchVideosTask.VideosCallback ----------------

    @Override
    public void onPreLoad() {
        // Loading indicator while fetching data (required)
        if (!isRefreshing) progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onVideosFetched(ArrayList<VideoItem> videos) {
        if (!isAdded()) return;

        progressBar.setVisibility(View.GONE);
        swipeRefresh.setRefreshing(false);
        isRefreshing = false;

        adapter.setVideos(videos);
        viewModel.setVideoList(videos); // pass data to DetailsFragment

        // Success notification (required)
        NotificationHelper.showSuccess(requireContext(), videos.size());
    }

    @Override
    public void onError(String message) {
        if (!isAdded()) return;

        progressBar.setVisibility(View.GONE);
        swipeRefresh.setRefreshing(false);
        isRefreshing = false;

        Toast.makeText(requireContext(),
                getString(R.string.toast_error, message), Toast.LENGTH_LONG).show();

        // Error notification (required)
        NotificationHelper.showError(requireContext(), message);
    }

    // ---------------- VideoAdapter.OnVideoClickListener ----------------

    @Override
    public void onVideoClick(VideoItem item) {
        // Pass the selected item to Tab 2 through the SharedViewModel
        viewModel.selectVideo(item);
    }
}
