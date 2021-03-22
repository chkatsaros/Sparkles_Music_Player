package com.audioplayer.sparklesv10.fragments;

import android.content.ContentUris;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.audioplayer.sparklesv10.R;
import com.audioplayer.sparklesv10.adapter.AlbumSonglistAdapter;
import com.audioplayer.sparklesv10.adapter.ArtistSonglistAdapter;
import com.audioplayer.sparklesv10.dataloader.ArtistLoader;
import com.audioplayer.sparklesv10.dataloader.ArtistSonglistLoader;
import com.audioplayer.sparklesv10.types.Artist;
import com.audioplayer.sparklesv10.types.Song;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import static com.audioplayer.sparklesv10.MainActivity.currFragment;

public class ArtistDetailsFragment extends Fragment {

    private long artist_id;

    private Artist artist;
    private List<Song> artistDetailsSongList = new ArrayList<>();

    private ImageView imageView;
    private TextView artistDetailsName, artistDetailsAlbums, artistDetailsTracks;
    private RecyclerView artistDetailsRecyclerView;
    private ArtistSonglistAdapter artistSonglistAdapter;

    public static ArtistDetailsFragment newInstance(long id) {

        Bundle args = new Bundle();

        ArtistDetailsFragment fragment = new ArtistDetailsFragment();
        args.putLong("_ID", id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        artist_id = getArguments().getLong("_ID");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        currFragment = 2;

        View myView = inflater.inflate(R.layout.fragment_artist_details, container, false);

        artist = new ArtistLoader().getArtist(getActivity(), artist_id);

        imageView = myView.findViewById(R.id.artistDetailsImage);
        ImageLoader.getInstance().displayImage(getImage(artist.id).toString(), imageView,
                new DisplayImageOptions.Builder().cacheInMemory(true).showImageOnLoading(R.drawable.record3)
                        .resetViewBeforeLoading(true).resetViewBeforeLoading(true).build()
        );

        artistDetailsName = (TextView) myView.findViewById(R.id.artistDetailsName);
        artistDetailsName.setSelected(true);
        artistDetailsName.setText(artist.artistName);
        artistDetailsAlbums = (TextView) myView.findViewById(R.id.artistDetailsAlbums);
        artistDetailsAlbums.setSelected(true);

        if (artist.albumCount > 1) artistDetailsAlbums.setText(artist.albumCount + " Albums");
        else artistDetailsAlbums.setText(artist.albumCount + " Album");

        artistDetailsTracks = (TextView) myView.findViewById(R.id.artistDetailsTracks);
        artistDetailsTracks.setSelected(true);

        if (artist.songCount > 1) artistDetailsTracks.setText(artist.songCount + " Tracks");
        else artistDetailsTracks.setText(artist.songCount + " Track");

        artistDetailsRecyclerView = (RecyclerView) myView.findViewById(R.id.artistDetailsRecyclerView);
        artistDetailsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        artistDetailsSongList = ArtistSonglistLoader.getAllArtistSongs(getActivity(), artist_id);
        artistDetailsRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        artistDetailsRecyclerView.setHasFixedSize(true);
        new loadData().execute("");

        return myView;
    }

    private Uri getImage(long albumId) {
        return ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"),albumId);
    }

    private class loadData extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {

            if (getActivity() != null) {
                artistSonglistAdapter = new ArtistSonglistAdapter(getActivity(), artistDetailsSongList);
            }
            return "Execute";
        }

        @Override
        protected void onPostExecute(String s){

            if(getActivity() != null) {
                artistDetailsRecyclerView.setAdapter(artistSonglistAdapter);
            }
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }
    }
}