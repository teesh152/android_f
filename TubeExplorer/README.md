# TubeExplorer 📺 — MOB2 Final Project

Android app (Java) that integrates the **YouTube Data API v3** and displays videos using **RecyclerView inside Fragments**, with **TabLayout + ViewPager2**, an **Options Menu**, **AsyncTask** networking, and **Notifications**.

## How it maps to the project requirements

| Requirement | Where it is implemented |
|---|---|
| API Integration (YouTube API) | `network/FetchVideosTask.java` — YouTube Data API v3 `search` endpoint |
| AsyncTask for background fetching | `FetchVideosTask extends AsyncTask` |
| Loading indicator | `ProgressBar` in `fragment_videos.xml`, shown in `onPreExecute()` |
| TabLayout + ViewPager | `MainActivity.java` + `activity_main.xml` (ViewPager2 + TabLayoutMediator) |
| Fragments for both tabs | `fragments/VideosFragment.java` (Tab 1), `fragments/DetailsFragment.java` (Tab 2) |
| RecyclerView in Tab 1 | `adapter/VideoAdapter.java` + `fragment_videos.xml` |
| Tab 2 details, default = first item | `DetailsFragment` + `viewmodel/SharedViewModel.java` |
| Passing data between fragments | `SharedViewModel` (Activity-scoped, shared by both fragments) |
| Menu (Options Menu) | `res/menu/main_menu.xml` + `MainActivity.onCreateOptionsMenu()` |
| Notifications (success + error) | `util/NotificationHelper.java` |
| Material design + custom UI element | Material theme, CardView rows, custom gradient button `drawable/button_watch_bg.xml` |
| Pull-to-refresh (extra) | `SwipeRefreshLayout` in `VideosFragment` |
| Search (extra) | `SearchView` in the Options Menu → new API query |

## Run in Android Studio

1. Extract the ZIP.
2. Android Studio → **Open** → select the `TubeExplorer` folder.
3. Let Gradle sync (internet required the first time).
4. Run on an emulator or device (**minSdk 26** / Android 8.0+).

## Build the APK on GitHub (no PC needed)

1. Create a new GitHub repository.
2. Upload **all files/folders** of this project through the browser
   (including `.github/workflows/build.yml` — make sure the folder path is kept).
   No Gradle wrapper or any binary file is needed; the workflow installs Gradle itself.
3. The **Build TubeExplorer APK** action runs automatically on push
   (or run it manually from the **Actions** tab → *Run workflow*).
4. Get the APK either:
   - from the **`apk/TubeExplorer.apk`** file the workflow commits back to the repo, or
   - from the **Artifacts** section of the workflow run.

> Repo → Settings → Actions → General → **Workflow permissions** → select
> **Read and write permissions** (needed so the workflow can commit the APK).

## Notes

- The API key is in `FetchVideosTask.java` (constant `API_KEY`).
- On Android 13+ the app asks for the notification permission at first launch — tap **Allow** so the success/error notifications appear (needed for the demo video).
- `AsyncTask` is deprecated in modern Android but is used here because the project explicitly requires it.
