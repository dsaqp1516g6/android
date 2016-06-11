package upc.eetac.dsa.secretsites;

import android.animation.Animator;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import upc.eetac.dsa.secretsites.client.SecretSitesClient;
import upc.eetac.dsa.secretsites.client.SecretSitesClientException;
import upc.eetac.dsa.secretsites.client.SecretSitesResources;
import upc.eetac.dsa.secretsites.entity.Comment;
import upc.eetac.dsa.secretsites.entity.CommentCollection;
import upc.eetac.dsa.secretsites.entity.CommentCollectionAdapter;
import upc.eetac.dsa.secretsites.entity.InterestPoint;
import upc.eetac.dsa.secretsites.entity.PhotoCollection;
import upc.eetac.dsa.secretsites.entity.PhotoCollectionAdapter;

public class DetailActivity extends AppCompatActivity {

    private static final int MAX_SIZE = 4096;
    private CommentCollectionAdapter adapterComments = null;
    private PhotoCollectionAdapter adapterPhotos = null;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private RecyclerView commentListView = null;
    private RecyclerView photoListView = null;
    public int lastPosition = -1;
    private InterestPoint point = null;
    private boolean isCommentPage = true;

    private GetPointTask mGetPointTask = null;
    private ImageLoadTask mImageLoadTask = null;
    private CreateCommentTask mCreateCommentTask = null;
    private EditCommentTask mEditCommentTask = null;
    private DeleteCommentTask mDeleteCommentTask = null;

    FloatingActionButton fabAddItem = null;
    FloatingActionButton fabEditItem = null;
    FloatingActionButton fabDeleteComment = null;
    FloatingActionButton fabDeletePhoto = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Intent i = this.getIntent();
        String name = i.getExtras().getString("pointName");
        String photoid = i.getExtras().getString("photoId");
        String url = i.getExtras().getString("urlPoint");
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        toolbar.setTitle(name);
        setSupportActionBar(toolbar);

        mGetPointTask = new GetPointTask(url);
        if (photoid.compareTo("noBest") != 0) {
            mImageLoadTask = new ImageLoadTask(onImageUrlCreate(photoid), (ImageView) findViewById(R.id.point_image), (ProgressBar) findViewById(R.id.progressBar));
            mImageLoadTask.execute();
        }

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (lastPosition == -1) {
                    fabAddItem.animate().translationY(fabAddItem.getHeight() + 50)
                            .setInterpolator(new AccelerateInterpolator(2))
                            .setListener(createAddAnimationListener(position))
                            .start();
                } else {
                    if (position == 0) { //COMMENTS
                        fabAddItem.setImageResource(R.mipmap.add_comment_icon);
                        photoListView.getChildAt(lastPosition).setBackgroundColor(Color.TRANSPARENT);
                        fabDeletePhoto.animate().translationY(fabDeleteComment.getHeight() + 50).setInterpolator(new AccelerateInterpolator(2)).start();
                        unselectedItemEvent();
                        isCommentPage = true;
                    } else if (position == 1) {//PHOTOS
                        fabAddItem.setImageResource(R.mipmap.add_photo_icon);
                        commentListView.getChildAt(lastPosition).setBackgroundColor(Color.TRANSPARENT);
                        fabDeleteComment.animate().translationY(fabDeleteComment.getHeight() + 50).setInterpolator(new AccelerateInterpolator(2)).start();
                        unselectedItemEvent();
                        isCommentPage = false;
                    }

                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        fabAddItem = (FloatingActionButton) findViewById(R.id.addItem);
        fabAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAddCommentDialog();
            }
        });

        fabEditItem = (FloatingActionButton) findViewById(R.id.editItem);
        fabEditItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createEditCommentDialog();
            }
        });

        fabDeleteComment = (FloatingActionButton) findViewById(R.id.deleteComment);
        fabDeleteComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDeleteCommentTask = new DeleteCommentTask();
                mDeleteCommentTask.execute();
            }
        });

        fabDeletePhoto = (FloatingActionButton) findViewById(R.id.deletePhoto);
        fabDeletePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        fabEditItem.animate().translationY(fabEditItem.getHeight() + 300).setInterpolator(new AccelerateInterpolator(2)).start();
        fabDeleteComment.animate().translationY(fabDeleteComment.getHeight() + 300).setInterpolator(new AccelerateInterpolator(2)).start();
        fabDeletePhoto.animate().translationY(fabDeletePhoto.getHeight() + 300).setInterpolator(new AccelerateInterpolator(2)).start();
    }

    public void getCommentListView(RecyclerView commentList) {
        this.commentListView = commentList;
        this.commentListView.setLayoutManager(new LinearLayoutManager(DetailActivity.this));
        if(this.photoListView != null) {
            mGetPointTask.execute();
        }
    }

    public void getPhotoListView(RecyclerView photoListView) {
        this.photoListView = photoListView;
        this.photoListView.setLayoutManager(new LinearLayoutManager(DetailActivity.this));
        if(this.commentListView != null) {
            mGetPointTask.execute();
        }
    }

    public static String onImageUrlCreate(String photoid) {
        return SecretSitesResources.serverIP + photoid.toLowerCase() + ".jpg";
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        item.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "COMMENTS";
                case 1:
                    return "PHOTOS";
            }
            return null;
        }
    }

    public static class PlaceholderFragment extends Fragment {

        private static final String ARG_SECTION_NUMBER = "section_number";

        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            DetailActivity detailAct = (DetailActivity) getActivity();
            switch(getArguments().getInt(ARG_SECTION_NUMBER)) {
                case 0:         //COMMENTS LAYOUT
                    View commentV = inflater.inflate(R.layout.fragment_activity_comment, container, false);
                    detailAct.getCommentListView((RecyclerView) commentV.findViewById(R.id.commentList));
                    return commentV;
                case 1:         //PHOTOS LAYOUT
                    View photoV = inflater.inflate(R.layout.fragment_activity_photo, container, false);
                    detailAct.getPhotoListView((RecyclerView) photoV.findViewById(R.id.photoList));
                    return photoV;
            }
            return null;
        }

    }

    public void selectedItemEvent() {
        if (isCommentPage) {
            fabEditItem.setVisibility(View.VISIBLE);
            fabEditItem.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
            fabDeleteComment.setVisibility(View.VISIBLE);
            fabDeleteComment.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
        }
        else {
            fabDeletePhoto.setVisibility(View.VISIBLE);
            fabDeletePhoto.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
        }
        fabAddItem.animate().translationY(fabAddItem.getHeight() + 50).setInterpolator(new AccelerateInterpolator(2)).start();


    }

    public void unselectedItemEvent() {
        lastPosition = -1;
        if (isCommentPage) {
            fabEditItem.animate().translationY(fabEditItem.getHeight() + 50).setInterpolator(new AccelerateInterpolator(2)).start();
            fabDeleteComment.animate().translationY(fabDeleteComment.getHeight() + 50).setInterpolator(new AccelerateInterpolator(2)).start();
        }
        else {
            fabDeletePhoto.animate().translationY(fabDeleteComment.getHeight() + 50).setInterpolator(new AccelerateInterpolator(2)).start();
        }
        fabAddItem.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
    }

    public void createAddCommentDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_add_comment, null);
        dialogBuilder.setView(dialogView);

        final TextView namePoint = (TextView) dialogView.findViewById(R.id.pointNameAdd);
        final TextView commentText = (TextView) dialogView.findViewById(R.id.commentTextAdd);
        namePoint.setText(point.getName());

        //dialogBuilder.setTitle("Custom dialog");
        dialogBuilder.setPositiveButton("Comment", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                mCreateCommentTask = new CreateCommentTask(commentText.getText().toString());
                mCreateCommentTask.execute();
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    public void createEditCommentDialog() {
        Comment comment = point.getComments().getComments().get(lastPosition);
        android.text.format.DateFormat df = new android.text.format.DateFormat();
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_edit_comment, null);
        dialogBuilder.setView(dialogView);

        final TextView txtPoint = (TextView) dialogView.findViewById(R.id.pointNameEdit);
        final TextView txtUser = (TextView) dialogView.findViewById(R.id.createUser);
        final TextView txtDate = (TextView) dialogView.findViewById(R.id.dateComment);
        final TextView txtComment = (TextView) dialogView.findViewById(R.id.commentTextEdit);
        txtPoint.setText(point.getName());
        txtUser.setText(comment.getUsername());
        txtComment.setText(comment.getText());
        txtDate.setText(df.format("dd/MM/yyyy hh:mm:ss", comment.getLastModified()));

        //dialogBuilder.setTitle("Custom dialog");
        dialogBuilder.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                mEditCommentTask = new EditCommentTask();
                mEditCommentTask.execute();
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    @Override
    public void onBackPressed() {
        if(lastPosition == -1) {
            super.onBackPressed();
        }
        else {
            unselectedItemEvent();
        }
    }

    public Animator.AnimatorListener createAddAnimationListener(final int position) {
        return (new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (position == 0) { //COMMENTS
                    isCommentPage = true;
                    fabAddItem.setImageResource(R.mipmap.add_comment_icon);
                    fabEditItem.setVisibility(View.VISIBLE);
                    fabDeleteComment.setVisibility(View.VISIBLE);
                    fabDeletePhoto.setVisibility(View.GONE);
                    fabAddItem.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
                } else if (position == 1) {//PHOTOS
                    isCommentPage = false;
                    fabAddItem.setImageResource(R.mipmap.add_photo_icon);
                    fabEditItem.setVisibility(View.GONE);
                    fabDeleteComment.setVisibility(View.GONE);
                    fabDeletePhoto.setVisibility(View.VISIBLE);
                    fabAddItem.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
                }
                fabAddItem.animate().setListener(null);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }



    //Async TASKS

    public class GetPointTask extends AsyncTask<Void, Void, String> {

        private String url = null;

        GetPointTask(String url) {
            this.url = url;
        }

        @Override
        protected String doInBackground(Void... params) {
            SecretSitesClient client = SecretSitesClient.getInstance();
            try {
                return client.getDetailInterestPoint(this.url);
            }
            catch (SecretSitesClientException ex) {
                return ex.getReason();
            }
        }

        @Override
        protected void onPostExecute(final String response) {
            try{
                point = new Gson().fromJson(response, InterestPoint.class);
                adapterComments = new CommentCollectionAdapter(DetailActivity.this, point.getComments());
                adapterPhotos = new PhotoCollectionAdapter(DetailActivity.this, point.getPhotos());
                commentListView.setAdapter(adapterComments);
                photoListView.setAdapter(adapterPhotos);
                adapterComments.notifyDataSetChanged();
                adapterPhotos.notifyDataSetChanged();
            }
            catch (JsonSyntaxException ex) {
                Toast.makeText(DetailActivity.this, response,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static class ImageLoadTask extends AsyncTask<Void, String, Bitmap> {

        private String url;
        private ImageView imageView;
        private ProgressBar progressBar;

        public ImageLoadTask(String url, ImageView imageView, ProgressBar progressBar) {
            this.url = url;
            this.imageView = imageView;
            this.progressBar = progressBar;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setMax(100);
            progressBar.setIndeterminate(false);
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            progressBar.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            int count;
            try {
                URL urlConnection = new URL(this.url);
                HttpURLConnection connection = (HttpURLConnection) urlConnection
                        .openConnection();
                int lenghtOfFile = connection.getContentLength();
                //connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                File file = new File("/sdcard/secretsitestmpphoto.png");
                OutputStream output = new FileOutputStream(file);
                byte data[] = new byte[1024];
                long total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));
                    output.write(data, 0, count);
                }
                Bitmap myBitmap = BitmapFactory.decodeFile(file.getPath());
                file.delete();
                return myBitmap;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            progressBar.setVisibility(View.GONE);
            if(result != null) {
                if(result.getWidth() > MAX_SIZE) {
                    int toRest = result.getWidth() - MAX_SIZE;
                    result = Bitmap.createScaledBitmap(result, MAX_SIZE, result.getHeight() - toRest, true);
                }
                if(result.getHeight() > MAX_SIZE) {
                    int toRest = result.getHeight() - MAX_SIZE;
                    result = Bitmap.createScaledBitmap(result, result.getWidth() - toRest, MAX_SIZE, true);
                }
                super.onPostExecute(result);
                imageView.setImageBitmap(result);
            }
        }
    }

    public class CreateCommentTask extends AsyncTask<Void, Void, String> {

        String text;
        CreateCommentTask(String text) {
            this.text = text;
        }

        @Override
        protected String doInBackground(Void... params) {
            SecretSitesClient client = SecretSitesClient.getInstance();
            try {
                return client.createComment(client.getLink(point.getLinks(), "create-comment").getUri().toString(), point.getId(), this.text);
            }
            catch (SecretSitesClientException ex) {
                return ex.getReason();
            }
        }

        @Override
        protected void onPostExecute(final String response) {
            try{
                Comment comment = new Gson().fromJson(response, Comment.class);
                point.getComments().getComments().add(0, comment);
                adapterComments = new CommentCollectionAdapter(DetailActivity.this, point.getComments());
                commentListView.setAdapter(adapterComments);
                adapterComments.notifyDataSetChanged();
            }
            catch (JsonSyntaxException ex) {
                Toast.makeText(DetailActivity.this, response,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class EditCommentTask extends AsyncTask<Void, Void, String> {

        EditCommentTask() {
        }

        @Override
        protected String doInBackground(Void... params) {
            SecretSitesClient client = SecretSitesClient.getInstance();
            Comment comment = point.getComments().getComments().get(lastPosition);
            try {
                return client.editComment(client.getLink(comment.getLinks(),"self-comment").getUri().toString(), comment);
            }
            catch (SecretSitesClientException ex) {
                return ex.getReason();
            }
        }

        @Override
        protected void onPostExecute(final String response) {
            try{
                Comment comment = new Gson().fromJson(response, Comment.class);
                point.getComments().getComments().set(lastPosition, comment);
                adapterComments = new CommentCollectionAdapter(DetailActivity.this, point.getComments());
                commentListView.setAdapter(adapterComments);
                adapterComments.notifyDataSetChanged();
                unselectedItemEvent();
            }
            catch (JsonSyntaxException ex) {
                Toast.makeText(DetailActivity.this, response,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class DeleteCommentTask extends AsyncTask<Void, Void, String> {

        DeleteCommentTask() {
        }

        @Override
        protected String doInBackground(Void... params) {
            SecretSitesClient client = SecretSitesClient.getInstance();
            Comment comment = point.getComments().getComments().get(lastPosition);
            try {
                return client.deleteComment(client.getLink(comment.getLinks(), "self-comment").getUri().toString());
            }
            catch (SecretSitesClientException ex) {
                return ex.getReason();
            }
        }

        @Override
        protected void onPostExecute(final String response) {
            if (response.compareTo("OK") == 0) {
                point.getComments().getComments().remove(lastPosition);
                unselectedItemEvent();
                adapterComments = new CommentCollectionAdapter(DetailActivity.this, point.getComments());
                commentListView.setAdapter(adapterComments);
                adapterComments.notifyDataSetChanged();
            } else {
                Toast.makeText(DetailActivity.this, response,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }



}


