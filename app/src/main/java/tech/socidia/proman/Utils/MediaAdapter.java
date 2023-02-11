package tech.socidia.proman.Utils;

import static java.security.AccessController.getContext;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.PlayerView;

import java.util.List;

import tech.socidia.proman.R;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaViewHolder>{
    List<Uri> mediaList;
    String type;
    Context context;
    public MediaAdapter(Context context,List<Uri> mediaList,String type){
        this.mediaList = mediaList;
        this.type=type;
        this.context = context;
    }

    @NonNull
    @Override
    public MediaAdapter.MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.media_item, parent, false);
        return new MediaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaAdapter.MediaViewHolder holder, int position) {
        holder.bind(context,mediaList.get(position),type);
    }

    @Override
    public int getItemCount() {
        return mediaList.size();
    }

    public static class MediaViewHolder extends RecyclerView.ViewHolder {

        PlayerView itemVideoView;
        ImageView itemImageView;
        TextView uploadImageText;
        ExoPlayer exoPlayer;
        public MediaViewHolder(@NonNull View itemView) {
            super(itemView);

            itemImageView = itemView.findViewById(R.id.itemsImageView);
            itemVideoView = itemView.findViewById(R.id.itemsVideoView);
            uploadImageText = itemView.findViewById(R.id.uploadText);
            exoPlayer = new ExoPlayer.Builder(itemView.getContext()).build();
            itemVideoView.setPlayer(exoPlayer);
        }

        public void bind(Context context,Uri uri,String type) {
            if(uri!=null){
                if(type.equals("videos")){
                    itemVideoView.setVisibility(View.VISIBLE);
                    exoPlayer.setVolume(0f);
                    itemImageView.setVisibility(View.GONE);
                    exoPlayer.setMediaItem(MediaItem.fromUri(uri));
                    exoPlayer.prepare();
                    exoPlayer.play();
                }else if(type.equals("images")){
                    itemVideoView.setVisibility(View.GONE);
                    itemImageView.setVisibility(View.VISIBLE);
                    Glide
                            .with(context)
                            .load(uri) // the uri you got from Firebase
                            .centerCrop()
                            .into(itemImageView);
                }
                uploadImageText.setVisibility(View.GONE);
            }else{
                if(type.equals("image")){
                    uploadImageText.setText("Klik untuk \nmemilih faktur");
                }
            }
        }
    }

}
