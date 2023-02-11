package tech.socidia.proman.Utils;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.view.GestureDetectorCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;

public class RecyclerItemLongClickListener implements RecyclerView.OnLongClickListener {

    RecyclerItemLongClickListener.OnItemLongClickListener longClickListener;
    GestureDetectorCompat gestureDetector;
    public RecyclerItemLongClickListener(RecyclerView recyclerViewer, RecyclerItemLongClickListener.OnItemLongClickListener listener){
        this.longClickListener = listener;
        gestureDetector = new GestureDetectorCompat(recyclerViewer.getContext(), new GestureDetector.SimpleOnGestureListener(){
            @Override
            public void onLongPress(@NonNull MotionEvent e) {
                super.onLongPress(e);
            }
        });
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }
    public interface OnItemLongClickListener{
        void onItemLongClick(View inView, int inPosition) throws IOException;
    }
}
