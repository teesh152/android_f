package com.mob2.tubeexplorer.model;

import java.io.Serializable;

/**
 * Model class representing a single YouTube video item
 * returned by the YouTube Data API v3 (search endpoint).
 */
public class VideoItem implements Serializable {

    private final String videoId;
    private final String title;
    private final String description;
    private final String channelTitle;
    private final String publishedAt;
    private final String thumbnailUrl;

    public VideoItem(String videoId, String title, String description,
                     String channelTitle, String publishedAt, String thumbnailUrl) {
        this.videoId = videoId;
        this.title = title;
        this.description = description;
        this.channelTitle = channelTitle;
        this.publishedAt = publishedAt;
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getVideoId()      { return videoId; }
    public String getTitle()        { return title; }
    public String getDescription()  { return description; }
    public String getChannelTitle() { return channelTitle; }
    public String getPublishedAt()  { return publishedAt; }
    public String getThumbnailUrl() { return thumbnailUrl; }
}
