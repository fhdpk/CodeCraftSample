package fhd.test.codecraft.view.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import fhd.test.codecraft.R;
import fhd.test.codecraft.model.Place;
import fhd.test.codecraft.utils.SharedPreferencesUtil;
import fhd.test.codecraft.view.adpter.PlaceAdapter;
import fhd.test.codecraft.view.listeners.PaginationScrollListener;
import fhd.test.codecraft.viewModel.PlaceViewModel;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    private static final long LOCATION_REFRESH_TIME = 10000;
    private static final float LOCATION_REFRESH_DISTANCE = 100;
    private static final int PERMISSIONS_REQUEST_ACCESS_LOC = 100;

    private LocationManager mLocationManager;

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            placeViewModel = ViewModelProviders.of(MainActivity.this).get(PlaceViewModel.class);
            SharedPreferencesUtil.setLat(MainActivity.this, location.getLatitude());
            SharedPreferencesUtil.setLng(MainActivity.this, location.getLongitude());
            placeViewModel.init();
            getPlaces();

            Toast.makeText(MainActivity.this, location.getLatitude() + "," + location.getLongitude(), Toast.LENGTH_LONG).show();
            Log.d("Loc: Latitude", location.getLatitude() + "," + location.getLongitude());
        }

        @Override
        public void onProviderDisabled(String provider) {
//            Log.d("Loc: provider", "disable");
        }

        @Override
        public void onProviderEnabled(String provider) {
//            Log.d("Loc: provider", "enable");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
//            Log.d("Loc: status", status + "");
        }
    };
    private PlaceViewModel placeViewModel;
    private RecyclerView placeRecyclerView;
    private View layoutLoading;
    private PlaceAdapter placeAdapter;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferencesUtil.setLat(this, 0.0f);
        SharedPreferencesUtil.setLng(this, 0.0f);

        placeRecyclerView = findViewById(R.id.placeRecyclerView);
        layoutLoading = findViewById(R.id.layoutLoading);


        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermission();
        } else {
            if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                buildAlertMessageNoGps();
            }
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                    LOCATION_REFRESH_DISTANCE, mLocationListener);
        }

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_red_dark,
                android.R.color.holo_orange_dark, android.R.color.holo_blue_dark,
                android.R.color.holo_green_dark);
        mSwipeRefreshLayout.setOnRefreshListener(() -> placeViewModel.clearPlaces());

    }

    private void getPlaces() {
        isLoading = true;
        placeViewModel.getPlaces().observe(this, (ArrayList<Place> places) -> {
            isLoading = false;
            if(places.size()==0) {
                layoutLoading.setVisibility(View.VISIBLE);
                placeRecyclerView.setVisibility(View.GONE);
            }else {
                layoutLoading.setVisibility(View.GONE);
                placeRecyclerView.setVisibility(View.VISIBLE);
            }
            setupRecyclerView(places);
            Log.e("liveData::", "success");
            mSwipeRefreshLayout.setRefreshing(false);
        });
    }

    private void setupRecyclerView(ArrayList<Place> places) {
        if (placeAdapter == null) {
            placeAdapter = new PlaceAdapter(MainActivity.this, places);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            placeRecyclerView.setLayoutManager(layoutManager);
            placeRecyclerView.setItemAnimator(new DefaultItemAnimator());
            placeRecyclerView.setNestedScrollingEnabled(true);
            placeRecyclerView.addOnScrollListener(new PaginationScrollListener(layoutManager) {
                @Override
                protected void loadMoreItems() {
                    if (isLoading) return;
                    isLoading = true;
                    placeRecyclerView.post(() -> placeViewModel.getMorePlaces());
                }

                @Override
                public boolean isLastPage() {
                    return isLastPage;
                }

                @Override
                public boolean isLoading() {
                    return isLoading;
                }
            });
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this,
                    layoutManager.getOrientation());
            placeRecyclerView.addItemDecoration(dividerItemDecoration);
            placeRecyclerView.setAdapter(placeAdapter);
        } else {
            placeAdapter.notifyDataSetChanged();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void buildAlertMessageNoGps() {
        showMessageOKCancel(
                "Your GPS seems to be disabled, For this app to function properly you have to enable GPS.",
                (dialog, which) -> {
                    startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                },
                (dialog, which) -> {
                    finish();
                });

    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE},
                PERMISSIONS_REQUEST_ACCESS_LOC);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_LOC:
                if (grantResults.length > 0) {

                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[2] == PackageManager.PERMISSION_GRANTED;

                    if (locationAccepted && storageAccepted) {
                        Toast.makeText(MainActivity.this, "Permission Granted.", Toast.LENGTH_LONG).show();
                        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            buildAlertMessageNoGps();
                        }
                    } else {

                        Toast.makeText(MainActivity.this, "Permission Denied.", Toast.LENGTH_LONG).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            showMessageOKCancel(
                                    "You need to allow both access permissions",
                                    (dialog, which) -> {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            requestPermission();
                                        }
                                    },
                                    (dialog, which) -> {
                                        finish();
                                    });
                            return;
                        }

                    }
                }

                break;
        }
    }


    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener,
                                     DialogInterface.OnClickListener cancelListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Exit", cancelListener)
                .create()
                .show();
    }

}
