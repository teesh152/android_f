package com.mob2.tubeexplorer.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.mob2.tubeexplorer.R;
import com.mob2.tubeexplorer.model.VideoItem;
import com.mob2.tubeexplorer.viewmodel.SharedViewModel;

/**
 * DetailsFragment (TAB 2)
 * -----------------------
 * Shows the details of the item selected in Tab 1.
 * If NO item has been selected yet, it defaults to the FIRST item of the
 * list (required by the project). Data arrives through the SharedViewModel,
 * which demonstrates PASSING DATA BETWEEN FRAGMENTS.
 */
public class DetailsFragment extends Fragment {

    private SharedViewModel viewModel;

    private ImageView imageThumbnail;
    private TextView textTitle;
    private TextView textChannel;
    private TextView textDate;
    private TextView textDescription;
    private TextView textEmpty;
    private View contentGroup;
    private Button buttonWatch;

    private VideoItem currentItem;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        imageThumbnail  = view.findViewById(R.id.detailThumbnail);
        textTitle       = view.findViewById(R.id.detailTitle);
        textChannel     = view.findViewById(R.id.detailChannel);
        textDate        = view.findViewById(R.id.detailDate);
        textDescription = view.findViewById(R.id.detailDescription);
        textEmpty       = view.findViewById(R.id.detailEmpty);
        contentGroup    = view.findViewById(R.id.detailContent);
        buttonWatch     = view.findViewById(R.id.buttonWatch);

        // Custom-designed UI element: styled "Watch on YouTube" button
        buttonWatch.setOnClickListener(v -> openOnYouTube());

        // Observe the selected item (or the default first item)
        viewModel.getSelectedVideo().observe(getViewLifecycleOwner(), this::bindVideo);

        // If nothing selected yet, fall back to the first item of the list
        viewModel.getVideoList().observe(getViewLifecycleOwner(), list -> {
            if (viewModel.getSelectedVideo().getValue() == null
                    && list != null && !list.isEmpty()) {
                bindVideo(list.get(0));
            }
        });
    }

    /** Bind a VideoItem to the detail views. */
    private void bindVideo(@Nullable VideoItem item) {
        if (item == null) {
            contentGroup.setVisibility(View.GONE);
            textEmpty.setVisibility(View.VISIBLE);
            return;
        }

        currentItem = item;
        contentGroup.setVisibility(View.VISIBLE);
        textEmpty.setVisibility(View.GONE);

        textTitle.setText(item.getTitle());
        textChannel.setText(item.getChannelTitle());
        textDate.setText(getString(R.string.published_on, item.getPublishedAt()));
        textDescription.setText(item.getDescription().isEmpty()
                ? getString(R.string.no_description)
                : item.getDescription());

        Glide.with(this)
                .load(item.getThumbnailUrl())
                .placeholder(R.drawable.thumbnail_placeholder)
                .centerCrop()
                .into(imageThumbnail);
    }

    /** Open the selected video in the YouTube app / browser. */
    private void openOnYouTube() {
        if (currentItem == null) return;
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.youtube.com/watch?v=" + currentItem.getVideoId()));
        startActivity(intent);
    }
}
