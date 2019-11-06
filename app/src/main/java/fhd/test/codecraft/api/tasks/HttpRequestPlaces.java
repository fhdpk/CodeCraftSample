package fhd.test.codecraft.api.tasks;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import androidx.lifecycle.MutableLiveData;
import fhd.test.codecraft.CodeCraftApp;
import fhd.test.codecraft.model.Place;
import fhd.test.codecraft.model.PlaceItem;
import fhd.test.codecraft.model.ProgressModel;


public class HttpRequestPlaces extends AsyncTask<String, Void, String> {

    private final double latitude;
    private final double longitude;
    private String nextPageToken;

    public interface OnLocationListener {
        void onLocationsReceived(ArrayList<Place> placesList, String responseData);

        void onLocationsError(String msg);
    }

    private OnLocationListener onLocationListener;
    private String urlStr;

    public HttpRequestPlaces(double latitude, double longitude,
                             MutableLiveData<ArrayList<Place>> location,
                             OnLocationListener onLocationListener, String nextPageToken) {

        this.latitude = latitude;
        this.longitude = longitude;
        this.nextPageToken = nextPageToken;
        this.onLocationListener = onLocationListener;
        urlStr = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "type=restaurant&key=" + CodeCraftApp.GOOGLE_API_KEY +
                "&rankby=distance&location=" + latitude + "," + longitude + "&pagetoken="
                + nextPageToken;
        Log.d("urlStr ::", urlStr + "");

    }

    @Override
    protected String doInBackground(String... params) {
        try {

            URL mUrl = new URL(urlStr);
            HttpURLConnection httpConnection = (HttpURLConnection) mUrl.openConnection();
            httpConnection.setRequestMethod("GET");
            httpConnection.setRequestProperty("Content-length", "0");
            httpConnection.setUseCaches(false);
            httpConnection.setAllowUserInteraction(false);
            httpConnection.setConnectTimeout(100000);
            httpConnection.setReadTimeout(100000);

            httpConnection.connect();

            int responseCode = httpConnection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();
                return sb.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String response) {
        super.onPostExecute(response);
        String errorMessage = null;
        ArrayList<Place> placesList = new ArrayList<>();
        Log.d("Result ::", response + "");
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray array = jsonObject.getJSONArray("results");

            if(jsonObject.has("error_message")) {
                errorMessage = jsonObject.getString("error_message");
                onLocationListener.onLocationsError(errorMessage);
                return;
            }

            for (int i = 0; i < array.length(); i++) {
                JSONObject item = (JSONObject) array.get(i);
                PlaceItem place = new PlaceItem();
                place.setLat(item.getJSONObject("geometry").getJSONObject("location").getDouble("lat"));
                place.setLng(item.getJSONObject("geometry").getJSONObject("location").getDouble("lng"));
                place.setName(item.getString("name"));
                place.setIcon(item.getString("icon"));
                place.setVicinity(item.getString("vicinity"));
                place.setPhotoReference(item.has("photos")? ((JSONObject)item.getJSONArray("photos").get(0)).getString("photo_reference") : "");
                place.setDistance(distance(latitude, longitude, place.getLat(), place.getLng()));
                placesList.add(place);
            }
            if(jsonObject.has("next_page_token")){
                nextPageToken = jsonObject.getString("next_page_token");
                placesList.add(new ProgressModel());
            } else {
                if(!(jsonObject.getString("status").equals("INVALID_REQUEST"))) {
                    nextPageToken = null;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (onLocationListener != null && response != null) {
            onLocationListener.onLocationsReceived(placesList, nextPageToken);
        } else
            onLocationListener.onLocationsError("Something went wrong, Try again later");
    }

    private String distance(double lat1, double lon1, double lat2, double lon2) {
        double p = 0.017453292519943295;    // Math.PI / 180
        double a = 0.5 - Math.cos((lat2 - lat1) * p) / 2 +
                Math.cos(lat1 * p) * Math.cos(lat2 * p) *
                        (1 - Math.cos((lon2 - lon1) * p)) / 2;

        double kmDistance = 12742 * Math.asin(Math.sqrt(a)); // 2 * R; R = 6371 km
        if (kmDistance * 1000 <= 999) {
            return ((int) (kmDistance * 1000)) + "m";
        } else {
            return (String.format("%.2f", kmDistance)) + "km";
        }

    }
}
