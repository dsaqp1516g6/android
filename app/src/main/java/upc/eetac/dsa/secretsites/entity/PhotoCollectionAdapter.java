package upc.eetac.dsa.secretsites.entity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;


import upc.eetac.dsa.secretsites.DetailActivity;
import upc.eetac.dsa.secretsites.R;

/**
 * Created by Marti on 05/06/2016.
 */
public class PhotoCollectionAdapter extends RecyclerView.Adapter<PhotoCollectionAdapter.ViewHolder> implements ItemClickListener {

    private PhotoCollection photoCollection;
    private View lastView = null;
    private DetailActivity detailActivity;

    public PhotoCollectionAdapter(DetailActivity detailActivity, PhotoCollection photoCollection) {
        super();
        this.detailActivity = detailActivity;
        this.photoCollection = photoCollection;
    }


    @Override
    public int getItemCount() {
        return this.photoCollection.getPhotos().size();
    }

    public Object getItem(int position) {
        return this.photoCollection.getPhotos().get(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.template_photo_list, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(v, this);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        Photo currentItem = this.photoCollection.getPhotos().get(i);
        String username = currentItem.getUsername();
        String photoid = currentItem.getId();
        float myRating = currentItem.getMyRating();
        float totalRating = currentItem.getTotalRating();
        Long lastModified = currentItem.getUploadTimestamp();

        if(Float.isNaN(myRating))
            myRating = 0.0f;

        android.text.format.DateFormat df = new android.text.format.DateFormat();
        viewHolder.textViewUsername.setText(username);
        viewHolder.textViewRating.setText(totalRating + "☆");
        viewHolder.textViewDataTime.setText(df.format("dd/MM/yyyy hh:mm:ss", lastModified));
        viewHolder.ratingBar.setRating(myRating);
        viewHolder.textViewMyRating.setText(myRating + "");
        final TextView textMyRating = viewHolder.textViewMyRating;
        viewHolder.ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                textMyRating.setText(rating + "");
            }
        });

        DetailActivity.ImageLoadTask mImageLoadTask = new DetailActivity.ImageLoadTask(DetailActivity.onImageUrlCreate(photoid), viewHolder.imageViewPhoto, viewHolder.progressBarPhoto);
        mImageLoadTask.execute();
        Log.d("***********" + i, DetailActivity.onImageUrlCreate(photoid));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void onItemClick(View view, int position) {
        if(detailActivity.lastPosition == -1) {
            view.setBackgroundColor(Color.LTGRAY);
            this.lastView = view;
            detailActivity.lastPosition = position;
            detailActivity.selectedItemEvent();
        }
        else if(detailActivity.lastPosition == position) {
            view.setBackgroundColor(Color.TRANSPARENT);
            this.lastView = null;
            detailActivity.unselectedItemEvent();
        }
        else if (lastView != null) {
            lastView.setBackgroundColor(Color.TRANSPARENT);
            view.setBackgroundColor(Color.LTGRAY);
            this.lastView = view;
            detailActivity.lastPosition = position;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imageViewPhoto;
        TextView textViewUsername;
        TextView textViewRating;
        TextView textViewDataTime;
        TextView textViewMyRating;
        ProgressBar progressBarPhoto;
        RatingBar ratingBar;
        ItemClickListener listener;

        public ViewHolder(View row, ItemClickListener listener){
            super(row);
            this.listener = listener;
            row.setOnClickListener(this);
            this.imageViewPhoto = (ImageView) row
                    .findViewById(R.id.imageViewPhoto);
            this.textViewUsername = (TextView) row
                    .findViewById(R.id.textViewUsername);
            this.textViewRating = (TextView) row
                    .findViewById(R.id.textViewRating);
            this.textViewDataTime = (TextView) row
                    .findViewById(R.id.textViewDataTime);
            textViewMyRating = (TextView) row
                    .findViewById(R.id.textViewMyRating);
            progressBarPhoto = (ProgressBar) row
                    .findViewById(R.id.progressBarPhotos);
            this.ratingBar = (RatingBar) row
                    .findViewById(R.id.ratingBar);
        }

        @Override
        public void onClick(View v) {
            listener.onItemClick(v, getAdapterPosition());
        }
    }
}

interface ItemClickListener {
    void onItemClick(View view, int position);
}
