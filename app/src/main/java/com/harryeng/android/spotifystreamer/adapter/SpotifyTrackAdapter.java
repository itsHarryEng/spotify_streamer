package com.harryeng.android.spotifystreamer.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.harryeng.android.spotifystreamer.R;
import com.harryeng.android.spotifystreamer.dto.TrackDTO;
import com.squareup.picasso.Picasso;
import kaaes.spotify.webapi.android.models.Tracks;

import java.util.List;

/**
 * {@link SpotifyTrackAdapter} exposes a list of artist or albums and tracks
 * from a {@link ArrayAdapter<Tracks>} to a {@link android.widget.ListView}.
 */
public class SpotifyTrackAdapter extends ArrayAdapter<TrackDTO> {

    private int mPosition = ListView.INVALID_POSITION;

    public SpotifyTrackAdapter(Context context, int resource, List<TrackDTO> objects) {
        super(context, resource, objects);
    }

    /**
     * Cache of the children views for a album/tracks list item.
     */
    public static class ViewHolder {
        public ImageView listArtView;
        public TextView albumView;
        public TextView trackView;

        public ViewHolder(View view) {
            listArtView = (ImageView) view.findViewById(R.id.list_item_art_imageview);
            albumView = (TextView) view.findViewById(R.id.list_item_album_textview);
            trackView = (TextView) view.findViewById(R.id.list_item_track_textview);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (null == convertView)
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.tracks_list_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(convertView);

        if (position == mPosition)
            convertView.setBackgroundColor(getContext().getResources().getColor(R.color.spotify_green));
        else
            convertView.setBackgroundColor(Color.TRANSPARENT);

        // Get the track data item for this position
        TrackDTO trackDTO = getItem(position);

        // Populate the list album image
        String listArtUrl = trackDTO.getSmallAlbumImageURL();
        if (null != listArtUrl && !listArtUrl.isEmpty())
            Picasso.with(getContext()).load(listArtUrl).into(viewHolder.listArtView);

        // Populate the track name
        viewHolder.trackView.setText(trackDTO.getTrackName());

        // Populate the album name
        viewHolder.albumView.setText(trackDTO.getAlbumName());

        // Return the completed view to render on screen
        return convertView;
    }

    public void setPosition(int position) {
        this.mPosition = position;
    }
}