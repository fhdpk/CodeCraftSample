package fhd.test.codecraft.api;


import android.content.Context;

import java.util.ArrayList;

import androidx.lifecycle.MutableLiveData;
import fhd.test.codecraft.api.tasks.HttpRequestPlaces;
import fhd.test.codecraft.model.Place;
import fhd.test.codecraft.model.ProgressModel;
import fhd.test.codecraft.utils.SharedPreferencesUtil;

public class PlaceRepository implements HttpRequestPlaces.OnLocationListener {

    private static PlaceRepository mPlaceRepository;

    private MutableLiveData<ArrayList<Place>> places;
    private String nextPageToken;

    public static PlaceRepository getInstance() {
        if (mPlaceRepository == null) {
            mPlaceRepository = new PlaceRepository();
        }
        return mPlaceRepository;
    }

    public MutableLiveData<ArrayList<Place>> getPlaces(Context mContext) {
        if (places == null)
            places = new MutableLiveData<>();
        if (places.getValue() == null)
            places.postValue(new ArrayList<Place>());


        new HttpRequestPlaces(SharedPreferencesUtil.getLat(mContext),
                SharedPreferencesUtil.getLng(mContext), places, this,
                (places.getValue() == null || places.getValue().size() == 0 ? "" : nextPageToken)).execute();
        return places;
    }

    @Override
    public void onLocationsReceived(ArrayList<Place> newList, String nextPageToken) {

        ArrayList<Place> currentList = places.getValue();

        if (currentList.size() > 0) {
            if (currentList.get(currentList.size() - 1) instanceof ProgressModel) {
                currentList.remove(currentList.size() - 1);
            }
        }
        currentList.addAll(newList);
        places.postValue(currentList);
        this.nextPageToken = nextPageToken;
    }

    @Override
    public void onLocationsError(String msg) {

    }
}
