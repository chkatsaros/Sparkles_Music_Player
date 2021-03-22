package com.audioplayer.sparklesv10.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.audioplayer.sparklesv10.R;
import com.audioplayer.sparklesv10.adapter.SongAdapter;
import com.audioplayer.sparklesv10.dataloader.SongLoader;

import static com.audioplayer.sparklesv10.MainActivity.currFragment;

public class SongFragment extends Fragment {

    private SongAdapter songAdapter;
    private RecyclerView songRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        currFragment = 0;

        View view = inflater.inflate(R.layout.fragment_song, container, false);

        songRecyclerView = (RecyclerView) view.findViewById(R.id.songRecyclerView);
        songRecyclerView.setLayoutManager(new LinearLayoutManager(getContext().getApplicationContext()));
        songRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        songRecyclerView.setHasFixedSize(true);
        new loadData().execute("");

        return view;
    }

    private class loadData extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {

            if (getActivity() != null) {
                songAdapter = new SongAdapter(new SongLoader().getAllSongs(getActivity()));
            }
            return "Execute";
        }

        @Override
        protected void onPostExecute(String s){

            if(getActivity() != null) {
                songRecyclerView.setAdapter(songAdapter);
            }
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }
    }
}
