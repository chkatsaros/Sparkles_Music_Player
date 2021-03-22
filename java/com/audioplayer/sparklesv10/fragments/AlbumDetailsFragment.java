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
import com.audioplayer.sparklesv10.dataloader.AlbumLoader;
import com.audioplayer.sparklesv10.dataloader.AlbumSonglistLoader;
import com.audioplayer.sparklesv10.types.Album;
import com.audioplayer.sparklesv10.types.Song;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import static com.audioplayer.sparklesv10.MainActivity.currFragment;

public class AlbumDetailsFragment extends Fragment {

    private long album_id;

    private Album album;
    private List<Song> albumDetailsSongList = new ArrayList<>();

    private ImageView imageView;
    private TextView albumDetailsTitle, albumDetailsArtist, albumDetailsTracks;
    private RecyclerView albumDetailsRecyclerView;
    private AlbumSonglistAdapter albumSonglistAdapter;

    public static AlbumDetailsFragment newInstance(long id) {
        
        Bundle args = new Bundle();
        
        AlbumDetailsFragment fragment = new AlbumDetailsFragment();
        args.putLong("_ID", id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        album_id = getArguments().getLong("_ID");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        currFragment = 1;

        View myView = inflater.inflate(R.layout.fragment_album_details, container, false);

        album = new AlbumLoader().getAlbum(getActivity(), album_id);

        imageView = myView.findViewById(R.id.albumDetailsImage);
        ImageLoader.getInstance().displayImage(getImage(album.id).toString(), imageView,
                new DisplayImageOptions.Builder().cacheInMemory(true).showImageOnLoading(R.drawable.record3)
                        .resetViewBeforeLoading(true).resetViewBeforeLoading(true).build()
        );

        albumDetailsTitle = (TextView) myView.findViewById(R.id.albumDetailsName);
        albumDetailsTitle.setSelected(true);
        albumDetailsTitle.setText(album.albumName);
        albumDetailsArtist = (TextView) myView.findViewById(R.id.albumDetailsArtist);
        albumDetailsArtist.setSelected(true);
        albumDetailsArtist.setText(album.artistName);
        albumDetailsTracks = (TextView) myView.findViewById(R.id.albumDetailsTracks);
        albumDetailsTracks.setSelected(true);

        if (album.numOfSongs > 1) albumDetailsTracks.setText(album.numOfSongs + " Tracks");
        else albumDetailsTracks.setText(album.numOfSongs + " Track");

        albumDetailsRecyclerView = (RecyclerView) myView.findViewById(R.id.albumDetailsRecyclerView);
        albumDetailsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        albumDetailsSongList = AlbumSonglistLoader.getAllAlbumSongs(getActivity(), album_id);
        albumDetailsRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        albumDetailsRecyclerView.setHasFixedSize(true);
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
                albumSonglistAdapter = new AlbumSonglistAdapter(getActivity(), albumDetailsSongList);
            }
            return "Execute";
        }

        @Override
        protected void onPostExecute(String s){

            if(getActivity() != null) {
                albumDetailsRecyclerView.setAdapter(albumSonglistAdapter);
            }
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }
    }

}