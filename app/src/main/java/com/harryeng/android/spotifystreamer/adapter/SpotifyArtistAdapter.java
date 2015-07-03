package com.harryeng.android.spotifystreamer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.harryeng.android.spotifystreamer.R;
import com.harryeng.android.spotifystreamer.dto.ArtistDTO;
import com.squareup.picasso.Picasso;

import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;

/**
 * {@link SpotifyArtistAdapter} exposes a list of artist or albums and tracks
 * from a {@link ArrayAdapter<Artist>} to a {@link android.widget.ListView}.
 */
public class SpotifyArtistAdapter extends ArrayAdapter<ArtistDTO> {

    public SpotifyArtistAdapter(Context context, int resource, List<ArtistDTO> objects) {
        super(context, resource, objects);
    }

    /**
     * Cache of the children views for a artists list item.
     */
    public static class ViewHolder {
        public ImageView artistImageView;
        public TextView artistView;

        public ViewHolder(View view) {
            artistImageView = (ImageView) view.findViewById(R.id.list_item_artist_imageview);
            artistView = (TextView) view.findViewById(R.id.list_item_artist_textview);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (null == convertView)
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.artist_list_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(convertView);

        // Get the artist data item for this position
        ArtistDTO artistDTO = getItem(position);

        // Get the first image if applicable and populate the artist image.
        if (null != artistDTO.getImageURL())
            Picasso.with(getContext()).load(artistDTO.getImageURL()).into(viewHolder.artistImageView);

        // Populate the artist name
        viewHolder.artistView.setText(artistDTO.getArtistName());

        // Return the completed view to render on screen
        return convertView;
    }
}