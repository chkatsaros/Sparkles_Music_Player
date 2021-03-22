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
import com.audioplayer.sparklesv10.adapter.ArtistAdapter;
import com.audioplayer.sparklesv10.dataloader.ArtistLoader;

import static com.audioplayer.sparklesv10.MainActivity.currFragment;

public class ArtistFragment extends Fragment {

    private ArtistAdapter artistAdapter;
    private RecyclerView artistRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        currFragment = 0;

        View view = inflater.inflate(R.layout.fragment_artist, container, false);

        artistRecyclerView = (RecyclerView) view.findViewById(R.id.artistRecyclerView);
        artistRecyclerView.setLayoutManager(new LinearLayoutManager(getContext().getApplicationContext()));
        artistRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        artistRecyclerView.setHasFixedSize(true);
        new loadData().execute("");

        return view;
    }

    public class loadData extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {

            if (getActivity() != null) {
                artistAdapter = new ArtistAdapter(getActivity(), new ArtistLoader().artistList(getActivity()));
            }
            return "Execute";
        }

        @Override
        protected void onPostExecute(String s){
            if(getActivity() != null) {
                artistRecyclerView.setAdapter(artistAdapter);
            }
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }
    }
}
