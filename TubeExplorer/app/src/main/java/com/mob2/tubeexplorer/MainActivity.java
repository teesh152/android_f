package com.mob2.tubeexplorer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.mob2.tubeexplorer.fragments.DetailsFragment;
import com.mob2.tubeexplorer.fragments.VideosFragment;
import com.mob2.tubeexplorer.util.NotificationHelper;
import com.mob2.tubeexplorer.viewmodel.SharedViewModel;

/**
 * MainActivity
 * ------------
 * Hosts the TabLayout + ViewPager2 (required) with TWO Fragments:
 *   Tab 1 -> VideosFragment  (RecyclerView list of YouTube videos)
 *   Tab 2 -> DetailsFragment (details of the selected / first item)
 *
 * Also provides the OPTIONS MENU (required) containing:
 *   - a SearchView to filter API results (optional extra marks)
 *   - a Refresh action to reload the data
 */
public class MainActivity extends AppCompatActivity {

    private SharedViewModel viewModel;
    private ViewPager2 viewPager;

    /** Runtime permission launcher for POST_NOTIFICATIONS (Android 13+). */
    private final androidx.activity.result.ActivityResultLauncher<String> notifPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                // Nothing special to do; notifications simply won't show if denied.
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create the notification channel once at startup
        NotificationHelper.createChannel(this);
        requestNotificationPermissionIfNeeded();

        viewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        viewPager.setAdapter(new TabsAdapter(this));

        // Attach the TabLayout to the ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText(R.string.tab_videos);
                tab.setIcon(R.drawable.ic_list);
            } else {
                tab.setText(R.string.tab_details);
                tab.setIcon(R.drawable.ic_info);
            }
        }).attach();

        // When an item is tapped in Tab 1, automatically switch to Tab 2
        viewModel.getNavigateToDetails().observe(this, navigate -> {
            if (Boolean.TRUE.equals(navigate)) {
                viewPager.setCurrentItem(1, true);
                viewModel.doneNavigating();
            }
        });
    }

    /** Android 13+ requires the POST_NOTIFICATIONS runtime permission. */
    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    // ---------------- OPTIONS MENU (required) ----------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        // SearchView inside the Options Menu -> search functionality
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        if (searchView != null) {
            searchView.setQueryHint(getString(R.string.search_hint));
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    viewModel.setSearchQuery(query);   // triggers a new API fetch
                    viewPager.setCurrentItem(0, true); // show results in Tab 1
                    searchView.clearFocus();
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            viewModel.requestRefresh();        // observed by VideosFragment
            viewPager.setCurrentItem(0, true); // show the list while reloading
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ---------------- ViewPager2 adapter ----------------

    /** FragmentStateAdapter supplying the two tab fragments. */
    private class TabsAdapter extends FragmentStateAdapter {

        TabsAdapter(@NonNull AppCompatActivity activity) {
            super(activity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return new VideosFragment();
            }
            return new DetailsFragment();
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}
