package com.audioplayer.sparklesv10.adapter;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.audioplayer.sparklesv10.R;
import com.audioplayer.sparklesv10.fragments.AlbumDetailsFragment;
import com.audioplayer.sparklesv10.types.Album;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder> {

    private Context context;
    private List<Album> albumList;

    public AlbumAdapter(Context context, List<Album> albumList) {
        this.context = context;
        this.albumList = albumList;
    }

    @NonNull
    @Override
    public AlbumAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return (new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.album_list, parent, false)));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Album album = albumList.get(position);

        if (album != null) {
            holder.albumName.setText(album.albumName);
            holder.artistName.setText(album.artistName);
            ImageLoader.getInstance().displayImage(getImage(album.id).toString(), holder.albumImage,
                    new DisplayImageOptions.Builder().cacheInMemory(true).showImageOnLoading(R.drawable.record2)
                            .resetViewBeforeLoading(true).resetViewBeforeLoading(true).build()
            );
        }
    }

    private Uri getImage(long albumId) {
        return ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"),albumId);
    }
    @Override
    public int getItemCount() {
        if (albumList != null) return albumList.size();

        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView albumImage;
        private TextView albumName;
        private TextView artistName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            albumImage = (ImageView) itemView.findViewById(R.id.albumImage);
            albumName = (TextView) itemView.findViewById(R.id.albumName);
            artistName = (TextView) itemView.findViewById(R.id.artistName);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            long albumId = albumList.get(getAdapterPosition()).id;

            FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);

            Fragment fragment = new AlbumDetailsFragment().newInstance(albumId);

            fragmentTransaction.hide(((AppCompatActivity) context).getSupportFragmentManager().findFragmentById(R.id.mainContainer));
            fragmentTransaction.add(R.id.mainContainer, fragment);
            fragmentTransaction.addToBackStack(null).commit();
        }
    }
}
