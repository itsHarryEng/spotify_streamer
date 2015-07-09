package com.harryeng.android.spotifystreamer.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import com.harryeng.android.spotifystreamer.R;
import com.harryeng.android.spotifystreamer.activity.TracksActivity;
import com.harryeng.android.spotifystreamer.adapter.SpotifyArtistAdapter;
import com.harryeng.android.spotifystreamer.dto.ArtistDTO;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.RetrofitError;

import java.util.ArrayList;
import java.util.List;


/**
 * The artist fragment containing a list view.
 */
public class ArtistsActivityFragment extends Fragment {
    private static final String LOG_TAG = ArtistsActivityFragment.class.getSimpleName();
    private static final String ARTIST_LIST = "artists.activity.fragment.artist.list";
    private static final String SEARCH_ARTIST = "artists.activity.fragment.search.artist";

    private SpotifyArtistAdapter mArtistAdapter;
    private EditText mEditText;

    public ArtistsActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ArrayList<ArtistDTO> artistDTOArrayList = new ArrayList<>();
        mArtistAdapter = new SpotifyArtistAdapter(getActivity(), R.layout.artist_list_item, artistDTOArrayList);

        View rootView = inflater.inflate(R.layout.fragment_artists, container, false);

        mEditText = (EditText) rootView.findViewById(R.id.artist_text);
        mEditText.requestFocus();

        /*
            If you enable this, add android:imeOptions="actionDone"
            to the EditText in fragment_artists.xml. It clears the keyboard from your view.
         */
        //searchOnTextChange(editText); // Search on text change.
        /*
            If you enable this, remove android:imeOptions="actionDone"
            from the EditText in fragment_artists.xml. It was acting weird with it enabled.
         */
        searchOnTextEnter(mEditText); // Search on button enter.

        ListView listView = (ListView) rootView.findViewById(R.id.listview_artist);
        listView.setAdapter(mArtistAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                ArtistDTO artistDTO = mArtistAdapter.getItem(position);

                Bundle extras = new Bundle();
                extras.putString(Intent.EXTRA_TITLE, artistDTO.getArtistName()); // The artist name for ActionBar
                extras.putString(Intent.EXTRA_UID, artistDTO.getId()); // The artist id to query tracks
                Intent intent = new Intent(getActivity(), TracksActivity.class)
                        .putExtras(extras);
                startActivity(intent);
            }
        });

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        if (null != mArtistAdapter) {
            List<ArtistDTO> artistList = new ArrayList<>();
            for (int i = 0; i < mArtistAdapter.getCount(); i++) {
                ArtistDTO artistDTO = mArtistAdapter.getItem(i);
                artistList.add(artistDTO);
            }
            bundle.putParcelableArrayList(ARTIST_LIST, (ArrayList<? extends Parcelable>) artistList);
        }
        if (null != mEditText)
            bundle.putString(SEARCH_ARTIST, mEditText.getText().toString());
    }

    @Override
    public void onViewStateRestored(Bundle bundle) {
        super.onViewStateRestored(bundle);
        if (null != bundle) {
            if (null != bundle.getParcelableArrayList(ARTIST_LIST)) {
                List<ArtistDTO> artistList = bundle.getParcelableArrayList(ARTIST_LIST);
                for (ArtistDTO artistDTO : artistList) {
                    mArtistAdapter.add(artistDTO);
                }
            }
            if (null != bundle.get(SEARCH_ARTIST))
                mEditText.setText(bundle.get(SEARCH_ARTIST).toString());
        }
    }

    /**
     * Queries Spotify when text is inputed. Will send multiple request to Spotify.
     * Hopefully Spotify will not think I'm spamming the system. This mimics the way Pandora searches.
     * @param editText search EditText View
     */
    private void searchOnTextChange(final EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                String artist = editText.getText().toString();
                if (artist.length() > 0)
                    onChange(artist);
                else
                    clearAdapter();
            }
        });
    }

    /**
     * Similar to searchOnTextChange except it will not query Spotify till the return key is pressed.
     * The toast message per requirements about no results works better in this scenario.
     * @param editText search EditText View
     */
    private void searchOnTextEnter(final EditText editText) {
        editText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    String artist = editText.getText().toString();
                    if (artist.length() > 0)
                        onChange(artist);
                    else
                        clearAdapter();
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * Set the criteria to query Spotify
     * @param artist the name of the artist
     */
    public void onChange(String artist) {
        SpotifyFetchTask spotifyFetchTask = new SpotifyFetchTask();
        spotifyFetchTask.execute(artist);
    }

    /**
     * Fetch the artist from Spotify and fill the adapter full of artist.
     */
    public class SpotifyFetchTask extends AsyncTask<String, Void, List<ArtistDTO>> {

        private final String LOG_TAG = SpotifyFetchTask.class.getSimpleName();

        @Override
        protected List<ArtistDTO> doInBackground(String... params) {
            // If there's no artist, there's nothing to look up.  Verify size of params.
            if (params.length == 0) {
                return null;
            }

            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            ArtistsPager artistsPager;
            try {
                artistsPager = spotify.searchArtists(params[0]);
            } catch (RetrofitError error) {
                Log.e(LOG_TAG, error.toString());

                // The ArtistDTO class has extra field which can store a message. Like an Exception for example.
                List<ArtistDTO> artistDTOList = new ArrayList<>();
                artistDTOList.add(new ArtistDTO(error.getMessage()));
                return artistDTOList; // Indicate to the user that the Spotify connection failed.
            }

            // Return null if there are no results
            if (null == artistsPager || artistsPager.artists.items.isEmpty())
                return null;

            // Populate the data transfer object
            List<ArtistDTO> artistDTOList = new ArrayList<>();
            for (Artist artist : artistsPager.artists.items) {
                ArtistDTO artistDTO = new ArtistDTO();
                artistDTO.setId(artist.id);
                artistDTO.setArtistName(artist.name);
                if (!artist.images.isEmpty())
                    artistDTO.setImageURL(artist.images.get(0).url); // Get the largest
                artistDTOList.add(artistDTO);
            }

            return artistDTOList;
        }

        @Override
        protected void onPostExecute(List<ArtistDTO> results) {
            if (results != null) {
                clearAdapter();
                for (ArtistDTO artistDTO : results) {
                    if (null == artistDTO.getExtra()) // If there's no issue, add; otherwise show toast.
                        mArtistAdapter.add(artistDTO);
                    else
                        Toast.makeText(getActivity(), artistDTO.getExtra(), Toast.LENGTH_SHORT).show();
                }
            } else {
                clearAdapter();
                Toast.makeText(getActivity(), R.string.no_artist_results, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Clear the adapter
     */
    private void clearAdapter() {
        mArtistAdapter.clear();
        mArtistAdapter.notifyDataSetChanged();
    }
}
