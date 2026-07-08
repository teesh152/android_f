package com.mob2.tubeexplorer.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mob2.tubeexplorer.model.VideoItem;

import java.util.List;

/**
 * SharedViewModel is scoped to the hosting Activity, which means BOTH
 * fragments (VideosFragment in Tab 1 and DetailsFragment in Tab 2) get
 * the SAME instance. This is the mechanism used to PASS DATA BETWEEN
 * FRAGMENTS in this project:
 *
 *  - Tab 1 posts the full video list and the item the user taps.
 *  - Tab 2 observes those LiveData objects and updates its UI.
 *  - If nothing was selected yet, Tab 2 defaults to the FIRST item.
 */
public class SharedViewModel extends ViewModel {

    /** Complete list fetched from the YouTube API. */
    private final MutableLiveData<List<VideoItem>> videoList = new MutableLiveData<>();

    /** The item the user tapped in the RecyclerView (Tab 1). */
    private final MutableLiveData<VideoItem> selectedVideo = new MutableLiveData<>();

    /** Search query typed in the Options Menu SearchView. */
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>();

    /** One-shot event asking MainActivity to switch to the Details tab. */
    private final MutableLiveData<Boolean> navigateToDetails = new MutableLiveData<>(false);

    /** One-shot event fired by the Refresh menu item. */
    private final MutableLiveData<Boolean> refreshRequest = new MutableLiveData<>(false);

    public LiveData<Boolean> getRefreshRequest() { return refreshRequest; }
    public void requestRefresh() { refreshRequest.setValue(true); }
    public void refreshHandled() { refreshRequest.setValue(false); }

    // ---------- Video list ----------
    public LiveData<List<VideoItem>> getVideoList() { return videoList; }

    public void setVideoList(List<VideoItem> list) {
        videoList.setValue(list);
        // Default behaviour required by the project:
        // if no item is selected yet, Tab 2 shows the FIRST item.
        if (selectedVideo.getValue() == null && list != null && !list.isEmpty()) {
            selectedVideo.setValue(list.get(0));
        }
    }

    // ---------- Selected item ----------
    public LiveData<VideoItem> getSelectedVideo() { return selectedVideo; }

    public void selectVideo(VideoItem item) {
        selectedVideo.setValue(item);
        navigateToDetails.setValue(true);
    }

    // ---------- Search ----------
    public LiveData<String> getSearchQuery() { return searchQuery; }

    public void setSearchQuery(String query) {
        // Reset selection so the first result of the new search is shown by default
        selectedVideo.setValue(null);
        searchQuery.setValue(query);
    }

    // ---------- Navigation event ----------
    public LiveData<Boolean> getNavigateToDetails() { return navigateToDetails; }

    public void doneNavigating() { navigateToDetails.setValue(false); }
}
