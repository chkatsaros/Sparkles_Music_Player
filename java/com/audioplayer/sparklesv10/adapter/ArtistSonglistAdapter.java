package com.audioplayer.sparklesv10.adapter;

import android.app.Activity;
import android.content.ContentUris;
import android.net.Uri;
import android.os.Handler;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.audioplayer.sparklesv10.R;
import com.audioplayer.sparklesv10.types.Song;
import com.audioplayer.sparklesv10.utility.SparklesUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import static com.audioplayer.sparklesv10.audioplayer.AudioPlayerService.playAll;
import static com.audioplayer.sparklesv10.audioplayer.AudioService.typePlaying;

public class ArtistSonglistAdapter extends  RecyclerView.Adapter<ArtistSonglistAdapter.ViewHolder> {

    public static List<Song> artistSongList;

    private long[] mIds;
    private Activity context;

    public ArtistSonglistAdapter(Activity context, List<Song> artistSongList) {
        this.context = context;
        this.artistSongList = artistSongList;
        mIds = getIds();
    }

    private long[] getIds() {
        long[] res = new long[getItemCount()];
        for (int i=0; i<getItemCount(); i++) {
            res[i] = artistSongList.get(i).id;
        }
        return res;
    }

    @NonNull
    @Override
    public ArtistSonglistAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ArtistSonglistAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.song_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ArtistSonglistAdapter.ViewHolder holder, int position) {

        Song song = artistSongList.get(position);

        if (song != null) {
            holder.title.setText(song.title);
            holder.albumName.setText(song.albumTitle);
            ImageLoader.getInstance().displayImage(getImage(song.albumId).toString(), holder.albumArt,
                    new DisplayImageOptions.Builder().cacheInMemory(true).showImageOnLoading(R.drawable.note2)
                            .resetViewBeforeLoading(true).resetViewBeforeLoading(true).build()
            );
        }
    }

    private Uri getImage(long albumId) {
        return ContentUris.withAppendedId(Uri.parse("content://media/external/audio/artistart"), albumId);
    }

    @Override
    public int getItemCount() {
        if (artistSongList != null) return artistSongList.size();

        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ImageView albumArt;
        private TextView title, albumName;

        public ViewHolder(View inflate) {
            super(inflate);

            albumArt = (ImageView) itemView.findViewById(R.id.songImage);
            title = (TextView) itemView.findViewById(R.id.songTitle);
            albumName = (TextView) itemView.findViewById(R.id.songArtist);
            inflate.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        typePlaying = 2;
                        playAll(mIds, getAdapterPosition(), artistSongList.get(getAdapterPosition()).id, SparklesUtil.IdType.NA);
                    }
                    catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }, 100);
        }
    }
}
