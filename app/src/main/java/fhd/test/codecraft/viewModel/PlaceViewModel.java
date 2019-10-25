package fhd.test.codecraft.viewModel;

import android.app.Application;
import android.content.Context;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import fhd.test.codecraft.api.PlaceRepository;
import fhd.test.codecraft.model.Place;

public class PlaceViewModel extends AndroidViewModel {

    private final Context mContext;
    private MutableLiveData<ArrayList<Place>> mLocationLiveData;
    private PlaceRepository mPlaceRepository;

    public PlaceViewModel(@NonNull Application application) {
        super(application);
        this.mContext = application.getApplicationContext();
        mPlaceRepository = PlaceRepository.getInstance();
    }

    public void init() {

        mLocationLiveData = mPlaceRepository.getPlaces(mContext);

    }

    public LiveData<ArrayList<Place>> getPlaces() {
        return mLocationLiveData;
    }

    public LiveData<ArrayList<Place>> getMorePlaces() {
        mLocationLiveData = mPlaceRepository.getPlaces(mContext);
        return mLocationLiveData;
    }
}
