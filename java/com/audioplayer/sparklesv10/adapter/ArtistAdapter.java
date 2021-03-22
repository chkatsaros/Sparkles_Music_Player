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
import com.audioplayer.sparklesv10.fragments.ArtistDetailsFragment;
import com.audioplayer.sparklesv10.types.Artist;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ViewHolder> {

    private Context context;
    private List<Artist> artistList;

    public ArtistAdapter(Context context, List<Artist> artistList) {
        this.context = context;
        this.artistList = artistList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return (new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.artist_list, parent, false)));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Artist artist = artistList.get(position);

        if (artist != null) {
            holder.artistName.setText(artist.artistName);
            if (artist.songCount > 1) holder.numOfTracks.setText(artist.songCount + " Tracks");
            else holder.numOfTracks.setText(artist.songCount + " Track");
            ImageLoader.getInstance().displayImage(getImage(artist.id).toString(), holder.artistImage,
                    new DisplayImageOptions.Builder().cacheInMemory(true).showImageOnLoading(R.drawable.artist2)
                            .resetViewBeforeLoading(true).resetViewBeforeLoading(true).build()
            );
        }
    }

    private Uri getImage(long albumId) {
        return ContentUris.withAppendedId(Uri.parse("content://media/external/audio/artistart"), albumId);
    }

    @Override
    public int getItemCount() {
        if (artistList != null) return artistList.size();

        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView artistImage;
        private TextView artistName, numOfTracks;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            artistImage = (CircleImageView) itemView.findViewById(R.id.artistImage);
            artistName = (TextView) itemView.findViewById(R.id.artistName);
            numOfTracks = (TextView) itemView.findViewById(R.id.trackNumber);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            long artistId = artistList.get(getAdapterPosition()).id;

            FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);
            Fragment fragment = new ArtistDetailsFragment().newInstance(artistId);
            fragmentTransaction.hide(((AppCompatActivity) context).getSupportFragmentManager().findFragmentById(R.id.mainContainer));
            fragmentTransaction.add(R.id.mainContainer, fragment);
            fragmentTransaction.addToBackStack(null).commit();
        }
    }
}
