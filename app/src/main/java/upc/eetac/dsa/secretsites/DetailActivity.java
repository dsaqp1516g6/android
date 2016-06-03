package upc.eetac.dsa.secretsites;

import android.app.ActionBar;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import upc.eetac.dsa.secretsites.client.SecretSitesClient;
import upc.eetac.dsa.secretsites.client.SecretSitesClientException;
import upc.eetac.dsa.secretsites.entity.InterestPoint;

public class DetailActivity extends AppCompatActivity {

    private static final int MAX_SIZE = 4096;

    private GetPointTask mGetPointTask = null;
    private ImageLoadTask mImageLoadTask = null;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;


    // Progress Dialog
    private ProgressDialog pDialog;
    // Progress dialog type (0 - for Horizontal progress bar)
    public static final int progress_bar_type = 0;


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

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mGetPointTask = new GetPointTask(url);
        mImageLoadTask = new ImageLoadTask(onImageUrlCreate(photoid), (ImageView) findViewById(R.id.point_image));
        mGetPointTask.execute();
        mImageLoadTask.execute();
    }

    public String onImageUrlCreate(String photoid) {
        return getString(R.string.serverIP) + photoid.toLowerCase() + ".png";
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

    /**
     * Showing Dialog
     * */
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case progress_bar_type:
                pDialog = new ProgressDialog(this);
                pDialog.setMessage("Downloading file. Please wait...");
                pDialog.setIndeterminate(false);
                pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pDialog.setCancelable(true);
                pDialog.show();
                return pDialog;
            default:
                return null;
        }
    }

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
                Toast.makeText(DetailActivity.this, ex.getReason(),
                        Toast.LENGTH_SHORT).show();
                return null;
            }
        }

        @Override
        protected void onPostExecute(final String response) {
            if(response != null) {
                InterestPoint point = new Gson().fromJson(response, InterestPoint.class);
            }
        }
    }

    public class ImageLoadTask extends AsyncTask<Void, String, Bitmap> {

        private String url;
        private ImageView imageView;
        private ProgressBar progressBar;

        public ImageLoadTask(String url, ImageView imageView) {
            this.url = url;
            this.imageView = imageView;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar = (ProgressBar) findViewById(R.id.progressBar);
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
                File file = new File("/sdcard/downloadedfile.png");
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
            switch(getArguments().getInt(ARG_SECTION_NUMBER)) {
                case 0:         //COMMENTS LAYOUT
                    return inflater.inflate(R.layout.activity_comment, container, false);
                case 1:         //PHOTOS LAYOUT
                    return inflater.inflate(R.layout.activity_photo, container, false);
            }
            return null;
        }
    }
}


