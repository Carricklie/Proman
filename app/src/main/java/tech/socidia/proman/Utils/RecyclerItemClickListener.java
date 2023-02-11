package tech.socidia.proman.Utils;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.view.GestureDetectorCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;

public class RecyclerItemClickListener extends RecyclerView.SimpleOnItemTouchListener{

    OnItemClickListener clickListener;
    GestureDetectorCompat gestureDetector;
    public RecyclerItemClickListener(RecyclerView recyclerViewer, OnItemClickListener listener){
        this.clickListener = listener;
        gestureDetector = new GestureDetectorCompat(recyclerViewer.getContext(), new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

        View childView = rv.findChildViewUnder(e.getX(), e.getY());
        if(childView!= null && clickListener!= null && gestureDetector.onTouchEvent(e)){
            try {
                clickListener.onItemClick(childView, rv.getChildAdapterPosition(childView));
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            return true;
        }

        return false;

    }

    public interface OnItemClickListener{
        void onItemClick(View inView, int inPosition) throws IOException;
    }
}

