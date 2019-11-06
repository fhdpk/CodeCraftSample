package fhd.test.codecraft.view.activity;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import fhd.test.codecraft.R;
import fhd.test.codecraft.model.Place;
import fhd.test.codecraft.model.PlaceItem;
import fhd.test.codecraft.utils.SharedPreferencesUtil;
import fhd.test.codecraft.viewModel.PlaceViewModel;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    public static final String PLACES_LIST = "PLACE_LIST";
    private GoogleMap mMap;
    private ArrayList<Place> placeList;
    private LatLng currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        placeList= (ArrayList<Place>) getIntent().getSerializableExtra(PLACES_LIST);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        // Add a marker in Sydney and move the camera
        currentLocation = new LatLng(SharedPreferencesUtil.getLat(this),SharedPreferencesUtil.getLng(this) );
        Marker currentLocationMarker = mMap.addMarker(new MarkerOptions()
                .position(currentLocation)
                .title("Current Location"));
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(currentLocationMarker.getPosition());

        for(int i=0;i<placeList.size();i++) {
            if(!(placeList.get(i) instanceof PlaceItem)) continue;
            PlaceItem place = (PlaceItem) placeList.get(i);
            LatLng latLng = new LatLng(place.getLat(),place.getLng());
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(place.getName())
                    .snippet("Distance :"+place.getDistance())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            marker.setTag(place);
            builder.include(marker.getPosition());

        }

        LatLngBounds bounds = builder.build();
        int padding = 100; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        googleMap.moveCamera(cu);

        mMap.setOnMarkerClickListener(this);

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if(marker.getTag()==null) return false;
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?saddr="+currentLocation.latitude+","+currentLocation.longitude+"&daddr="+marker.getPosition().latitude+","+marker.getPosition().longitude));
        startActivity(intent);
        return false;
    }
}
