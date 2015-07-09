package com.harryeng.android.spotifystreamer.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.harryeng.android.spotifystreamer.R;
import com.harryeng.android.spotifystreamer.adapter.SpotifyTrackAdapter;
import com.harryeng.android.spotifystreamer.dto.TrackDTO;
import com.harryeng.android.spotifystreamer.utility.Utility;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * The tracks fragment containing a list view.
 */
public class TracksActivityFragment extends Fragment {
    private static final String LOG_TAG = TracksActivityFragment.class.getSimpleName();
    private static final String TRACK_LIST = "tracks.activity.fragment.track.list";
    private static final String RETRIEVE_DATA = "tracks.activity.fragment.retrieve.data";
    private static final String SELECTED_KEY = "selected_position";

    private SpotifyTrackAdapter mTrackAdapter;
    ListView mListView;
    private int mPosition = ListView.INVALID_POSITION;
    private boolean mRetrieveData = true; // Set the orientation flag to retrieve data initially

    public TracksActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (null != savedInstanceState) {
            mRetrieveData = savedInstanceState.getBoolean(RETRIEVE_DATA); // Set the flag
        }

        ArrayList<TrackDTO> trackDTOArrayList = new ArrayList<>();
        mTrackAdapter = new SpotifyTrackAdapter(getActivity(), R.layout.tracks_list_item, trackDTOArrayList);

        View rootView = inflater.inflate(R.layout.fragment_tracks, container, false);

        getTopTracks(); // Grab the top 10 tracks

        mListView = (ListView) rootView.findViewById(R.id.listview_tracks);
        mListView.setAdapter(mTrackAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                TrackDTO trackDTO = mTrackAdapter.getItem(position); // Eventually, this will do something.
                mPosition = position;
                highlightPosition(position);
            }
        });

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        if (null != mTrackAdapter) {
            // Store the list of tracks into the Bundle
            List<TrackDTO> trackList = new ArrayList<>();
            for (int i = 0; i < mTrackAdapter.getCount(); i++) {
                TrackDTO trackDTO = mTrackAdapter.getItem(i);
                trackList.add(trackDTO);
            }
            bundle.putParcelableArrayList(TRACK_LIST, (ArrayList<? extends Parcelable>) trackList);
        }
        bundle.putBoolean(RETRIEVE_DATA, mRetrieveData); // Store the flag value
        if (mPosition != ListView.INVALID_POSITION)
            bundle.putInt(SELECTED_KEY, mPosition);

    }

    @Override
    public void onViewStateRestored(Bundle bundle) {
        super.onViewStateRestored(bundle);
        if (null != bundle) {
            // Populate the SpotifyTrackAdapter with the list of tracks from the Bundle
            if (null != bundle.getParcelableArrayList(TRACK_LIST)) {
                List<TrackDTO> trackList = bundle.getParcelableArrayList(TRACK_LIST);
                for (TrackDTO trackDTO : trackList) {
                    mTrackAdapter.add(trackDTO);
                }
            }
            if (null != bundle.get(SELECTED_KEY)) {
                int position = (int) bundle.get(SELECTED_KEY);
                if (position != ListView.INVALID_POSITION) {
                    mPosition = position;
                    highlightPosition(position);
                }
            }
        }
    }

    /**
     * Highlights the selected item and refresh the adapter
     * @param position // Where to set the item position
     */
    private void highlightPosition(int position) {
        mTrackAdapter.setPosition(position); // Set the item to highlight the new Spotify green color (not a fan of new shade).
        mTrackAdapter.notifyDataSetChanged(); // Refreshes the adapter and does not reset the position to the top!
    }

    /**
     * Display the title and artist in the backwards compatible ActionBar.
     */
    private void getTopTracks() {
        Bundle extras = getActivity().getIntent().getExtras();
        if (null != extras) {
            String artist = extras.getString(Intent.EXTRA_TITLE);
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (null != actionBar)
                actionBar.setSubtitle(artist); // Artist name into ActionBar

            String spotifyId = extras.getString(Intent.EXTRA_UID);
            getTracks(spotifyId);
        }
    }

    /**
     * Set the criteria to query Spotify
     * @param spotifyId Spotify artist user id.
     */
    public void getTracks(String spotifyId) {
        if (mRetrieveData) {
            SpotifyFetchTask spotifyFetchTask = new SpotifyFetchTask();
            spotifyFetchTask.execute(spotifyId);
            mRetrieveData = false; // Set the orientation flag to not retrieve data
        }
    }

    /**
     * Fetch the top tracks from Spotify and fill the adapter full of 10 tracks or less.
     */
    public class SpotifyFetchTask extends AsyncTask<String, Void, List<TrackDTO>> {

        private final String LOG_TAG = SpotifyFetchTask.class.getSimpleName();

        @Override
        protected List<TrackDTO> doInBackground(String... params) {
            // If there's no artist, there's nothing to look up.  Verify size of params.
            if (params.length == 0) {
                return null;
            }

            /**
             * Searching for tracks requires the country code which is set in the settings preference.
             *
             * Currently, only US and Canada are on the list but any ISO 3166-1 alpha-2 codes would work.
             * They would need to be added to the arrays.xml resource.
             * */
            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setEndpoint(SpotifyApi.SPOTIFY_WEB_API_ENDPOINT)
                    .setRequestInterceptor(new RequestInterceptor() {
                        @Override
                        public void intercept(RequestFacade request) {
                            String locationSetting = Utility.getPreferredLocation(getActivity());
                            request.addQueryParam(SpotifyService.COUNTRY, locationSetting);
                        }
                    })
                    .build();

            SpotifyService spotify = restAdapter.create(SpotifyService.class);

            Tracks results;
            try {
                results = spotify.getArtistTopTrack(params[0]);
            } catch (RetrofitError error) {
                Log.e(LOG_TAG, error.toString());

                // The TrackDTO class has extra field which can store a message. Like an Exception for example.
                List<TrackDTO> trackDTOList = new ArrayList<>();
                trackDTOList.add(new TrackDTO(error.getMessage()));
                return trackDTOList; // Indicate to the user that the Spotify connection failed.
            }

            // If results, tracks, or track size is null, return null to display toast.
            if ((null == results) ||
                    (null == results.tracks) ||
                    (0 == results.tracks.size()))
                return null;

            List<TrackDTO> trackDTOList = new ArrayList<>();
            for (Track track : results.tracks) {
                TrackDTO trackDTO = new TrackDTO();
                trackDTO.setId(track.id); // Populate the track id

                List<Image> albumArt = track.album.images;
                if (!albumArt.isEmpty()) {
                    trackDTO.setSmallAlbumImageURL(albumArt.get(0).url); // Grab the first image (usually the largest)
                    trackDTO.setLargeAlbumImageURL(albumArt.get(0).url); // Grab the first image (usually the largest)

                    // Find the large (640px) and small (200px) images and overwrite
                    for (Image image : albumArt) {
                        if (640 == image.width)
                            trackDTO.setLargeAlbumImageURL(image.url);
                        if (200 == image.width)
                            trackDTO.setSmallAlbumImageURL(image.url);
                    }
                }
                trackDTO.setTrackName(track.name); // Populate the album name
                trackDTO.setAlbumName(track.album.name); // Populate the album name
                trackDTO.setPreviewURL(track.preview_url); // Populate the preview URL
                trackDTOList.add(trackDTO);
            }

            return trackDTOList;
        }

        @Override
        protected void onPostExecute(List<TrackDTO> results) {
            if (results != null) {
                // Clear the list if it's already populated.
                mTrackAdapter.clear();
                mTrackAdapter.notifyDataSetChanged();

                // Populate only the top 10 songs (if applicable).
                int trackCount = 0;
                Iterator iterator = results.iterator();
                while (iterator.hasNext() && trackCount < 10) {
                    TrackDTO trackDTO = (TrackDTO) iterator.next();
                    if (null == trackDTO.getExtra()) // If there's no issue, add; otherwise show toast.
                        mTrackAdapter.add(trackDTO);
                    else
                        Toast.makeText(getActivity(), trackDTO.getExtra(), Toast.LENGTH_SHORT).show();
                    trackCount++;
                }
            } else {
                clearAdapter();
                Toast.makeText(getActivity(), R.string.no_track_results, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Clear the adapter
     */
    private void clearAdapter() {
        mTrackAdapter.clear();
        mTrackAdapter.notifyDataSetChanged();
    }
}
