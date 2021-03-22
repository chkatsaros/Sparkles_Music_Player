package com.audioplayer.sparklesv10.adapter;

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

import java.util.List;

import static com.audioplayer.sparklesv10.audioplayer.AudioService.typePlaying;
import static com.audioplayer.sparklesv10.audioplayer.AudioPlayerService.playAll;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {

    public static List<Song> songList;
    private long[] mIds;

    public SongAdapter(List<Song> songList) {
        this.songList = songList;
        mIds = getIds();
    }

    private long[] getIds() {
        long[] res = new long[getItemCount()];
        for (int i=0; i<getItemCount(); i++) {
            res[i] = songList.get(i).id;
        }
        return res;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return (new SongAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.song_list, parent, false)));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Song song = songList.get(position);

        if (song != null) {
            holder.title.setText(song.title);
            holder.artist.setText(song.artistName);
            ImageLoader.getInstance().displayImage(getImage(song.albumId).toString(), holder.imageView,
                    new DisplayImageOptions.Builder().cacheInMemory(true).showImageOnLoading(R.drawable.note2)
                            .resetViewBeforeLoading(true).resetViewBeforeLoading(true).build()
            );
        }
    }

    private Uri getImage(long albumId) {
        return ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId);
    }

    @Override
    public int getItemCount() {
        if (songList != null) return songList.size();

        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView imageView;
        private TextView title, artist;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = (ImageView) itemView.findViewById(R.id.songImage);
            title = (TextView) itemView.findViewById(R.id.songTitle);
            artist = (TextView) itemView.findViewById(R.id.songArtist);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        typePlaying = 0;
                        playAll(mIds, getAdapterPosition(), songList.get(getAdapterPosition()).id, SparklesUtil.IdType.NA);
                    }
                    catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }, 100);
        }
    }
}
