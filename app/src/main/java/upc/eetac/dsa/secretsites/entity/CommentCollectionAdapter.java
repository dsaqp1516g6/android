package upc.eetac.dsa.secretsites.entity;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import upc.eetac.dsa.secretsites.DetailActivity;
import upc.eetac.dsa.secretsites.R;

/**
 * Created by Marti on 03/06/2016.
 */
public class CommentCollectionAdapter extends RecyclerView.Adapter<CommentCollectionAdapter.ViewHolder> implements ItemClickListener {

    private CommentCollection commentCollection;
    private View lastView = null;
    private DetailActivity detailActivity;

    public CommentCollectionAdapter(DetailActivity detailActivity, CommentCollection commentCollection) {
        super();
        this.detailActivity = detailActivity;
        this.commentCollection = commentCollection;
    }

    @Override
    public int getItemCount() {
        return this.commentCollection.getComments().size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.template_comment_list, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(v, this);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        Comment currentItem = this.commentCollection.getComments().get(i);
        final String username = currentItem.getUsername();
        final String commentID = currentItem.getId();
        String content = currentItem.getText();
        Long lastModified = currentItem.getLastModified();

        android.text.format.DateFormat df = new android.text.format.DateFormat();
        viewHolder.textViewUsername.setText(username);
        viewHolder.textViewDescription.setText(content);
        viewHolder.textViewDataTime.setText(df.format("dd/MM/yyyy hh:mm:ss", lastModified));
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
        TextView textViewUsername;
        TextView textViewDescription;
        TextView textViewDataTime;
        ItemClickListener listener;

        public ViewHolder(View row, ItemClickListener listener){
            super(row);
            this.listener = listener;
            row.setOnClickListener(this);
            this.textViewUsername = (TextView) row
                    .findViewById(R.id.textViewUsername);
            this.textViewDescription = (TextView) row
                    .findViewById(R.id.textViewCommentText);
            this.textViewDataTime = (TextView) row
                    .findViewById(R.id.textViewDataTime);
        }

        @Override
        public void onClick(View v) {
            listener.onItemClick(v, getAdapterPosition());
        }
    }
}