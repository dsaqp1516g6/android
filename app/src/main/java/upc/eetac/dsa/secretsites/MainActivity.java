package upc.eetac.dsa.secretsites;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.security.acl.Group;
import java.util.HashMap;
import java.util.List;

import upc.eetac.dsa.secretsites.client.SecretSitesClient;
import upc.eetac.dsa.secretsites.client.SecretSitesClientException;
import upc.eetac.dsa.secretsites.entity.AuthToken;
import upc.eetac.dsa.secretsites.entity.InterestPoint;
import upc.eetac.dsa.secretsites.entity.InterestPointCollection;
import upc.eetac.dsa.secretsites.entity.Link;
import upc.eetac.dsa.secretsites.entity.User;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private int LOGIN_ACTIVITY_CODE = 10;
    private int REGISTER_ACTIVITY_CODE = 20;

    private GoogleMap mMap;
    private HashMap<Marker, Integer> mapArray = new HashMap<Marker, Integer>();
    private InterestPointCollection pointCollection = null;
    private RootTask mRootTask = null;
    private GetPointsTask mGetPointsTask = null;
    private final static String TAG = SecretSitesClient.class.toString();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(), "ADD POINT !!",
                        Toast.LENGTH_SHORT).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        navigationView.getMenu().setGroupVisible(R.id.loginGroup, false);
        navigationView.getMenu().findItem(R.id.communicateItem).setVisible(false);

        mRootTask = new RootTask();
        mRootTask.execute();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final android.support.v7.widget.SearchView searchView = (android.support.v7.widget.SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setIconified(true);
        searchView.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {

            boolean firstTime = true;

            @Override
            public boolean onQueryTextSubmit(String query) {
                searchAction(query);
                firstTime = !firstTime;
                searchView.clearFocus();
                searchView.onActionViewCollapsed();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!firstTime) {
                    firstTime = !firstTime;
                    searchView.onActionViewCollapsed();
                }
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_gps:
                Toast.makeText(this, "GPS ON !!",
                        Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void searchAction(String query) {
        mGetPointsTask = new GetPointsTask(query);
        mGetPointsTask.execute();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_homeOut:
            case R.id.nav_homeIn:
                break;
            case R.id.nav_singIn:
                signIn();
                break;
            case R.id.nav_signUp:
                signUp();
                break;
            case R.id.nav_camara:
                break;
            case R.id.nav_gallery:
                break;
            case R.id.nav_slideshow:
                break;
            case  R.id.nav_manage:
                break;
            case R.id.nav_share:
                break;
            case  R.id.nav_send:
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void signIn() {
        Intent i = new Intent(MainActivity.this, LoginActivity.class);
        startActivityForResult(i, LOGIN_ACTIVITY_CODE);;
    }

    public void signUp() {
        Intent i = new Intent(MainActivity.this, RegisterActivity.class);
        startActivityForResult(i, REGISTER_ACTIVITY_CODE);;
    }

    public void saveDataStorage(String saveID, String saveData) {
        SharedPreferences sp = getSharedPreferences("user_data", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(saveID, saveData);
        editor.commit();
    }

    public String getDataStorage(String saveID) {
        String data = null;
        SharedPreferences sp = getSharedPreferences("user_data", Activity.MODE_PRIVATE);
        data = sp.getString(saveID, null);
        return data;
    }

    public void changeDrawerLayout(User user) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if(user != null) {
            String fullname = user.getFullname();
            String email = user.getEmail();
            navigationView.getMenu().setGroupVisible(R.id.registerGroup, false);
            navigationView.getMenu().setGroupVisible(R.id.loginGroup, true);
            ((TextView) drawer.findViewById(R.id.fullnameDrawer)).setText(fullname);
            ((TextView) drawer.findViewById(R.id.emailDrawer)).setText(email);
        }
    }

    public class RootTask extends AsyncTask<Void, Void, String> {

        RootTask() {

        }

        @Override
        protected String doInBackground(Void... params) {
            SecretSitesClient client = SecretSitesClient.getInstance();
            String token = getDataStorage("token");
            if(token != null) {
                try {
                    String json = client.loadRoot(token);
                    return json;
                }
                catch (SecretSitesClientException ex)  {
                    return ex.getReason();
                }
            }
            else {
                if(client.loadRoot()) return getString(R.string.authorized);
                else return getString(R.string.server_error);
            }
        }

        @Override
        protected void onPostExecute(final String result) {
            AuthToken auth = SecretSitesClient.getInstance().getAuthToken();
            if(auth != null) {                                              //Acces to root with TOKEN
                User user = new Gson().fromJson(result, User.class);
                auth.setUserid(user.getId());
                changeDrawerLayout(user);
            }
            else if(result.contains(getString(R.string.unauthorized))) {    //Acces to root with a unauthorized TOKEN (So no TOKEN)
                Toast.makeText(MainActivity.this, result,
                        Toast.LENGTH_SHORT).show();
                saveDataStorage("token", null);
            }
            else if(result.contains(getString(R.string.authorized))) {      //Acces to root without TOKEN
                Log.d(TAG, result);
            }
            else {                                                          //No acces to root (Result = Problem details)
                Toast.makeText(MainActivity.this, result,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOGIN_ACTIVITY_CODE && resultCode == RESULT_OK) {
            Bundle b = data.getExtras();
            User user = new Gson().fromJson(b.getString("user"), User.class);
            saveDataStorage("token", SecretSitesClient.getInstance().getAuthToken().getToken());
            changeDrawerLayout(user);
        }
        else if (requestCode == REGISTER_ACTIVITY_CODE && resultCode == RESULT_OK) {
            Bundle b = data.getExtras();
            User user = new Gson().fromJson(b.getString("user"), User.class);
            saveDataStorage("token", SecretSitesClient.getInstance().getAuthToken().getToken());
            changeDrawerLayout(user);
        }

        //For CAMERA ONLY
        if (requestCode == 69 && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
        }
    }


    //Google Maps

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                int pos = mapArray.get(marker);
                InterestPoint point = pointCollection.getInterestPoints().get(pos);
                String url = SecretSitesClient.getLink(point.getLinks(), "self-point").getUri().toString();
                Intent i = new Intent(MainActivity.this, DetailActivity.class);
                i.putExtra("pointName", point.getName());
                if(point.getBestPhoto() != null)
                    i.putExtra("photoId", point.getBestPhoto().getId());
                else
                    i.putExtra("photoId", "noBest");
                i.putExtra("urlPoint", url);
                startActivity(i);
            }
        });
    }

    public void putMarkers(InterestPointCollection pointsCollection) {
        int i = 0;
        this.pointCollection = pointsCollection;
        for (InterestPoint point : pointsCollection.getInterestPoints()) {
            LatLng latLng = new LatLng(point.getLatitude(), point.getLongitude());
            Marker marker = mMap.addMarker(new MarkerOptions().position(latLng).title(point.getName()));
            mapArray.put(marker, i);
            i++;
        }
    }

    public class GetPointsTask extends AsyncTask<Void, Void, String> {

        String searchName;

        GetPointsTask(String query) {
            this.searchName = query;
        }

        @Override
        protected String doInBackground(Void... params) {
            SecretSitesClient client = SecretSitesClient.getInstance();
            try {
                return client.getInterestPoints(this.searchName);
            }
            catch (SecretSitesClientException ex) {
                return ex.getReason();
            }
        }

        @Override
        protected void onPostExecute(final String response) {
            try{
                InterestPointCollection pointsCollection = new Gson().fromJson(response, InterestPointCollection.class);
                putMarkers(pointsCollection);
            }
            catch (JsonSyntaxException ex) {
                Toast.makeText(MainActivity.this, response,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
