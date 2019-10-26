package fhd.test.codecraft.view.adpter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;
import fhd.test.codecraft.R;
import fhd.test.codecraft.model.Place;
import fhd.test.codecraft.model.PlaceItem;
import fhd.test.codecraft.model.ProgressModel;
import fhd.test.codecraft.utils.ImageLoader;
import fhd.test.codecraft.view.activity.MainActivity;
import fhd.test.codecraft.view.activity.PlaceDetailsActivty;

public class PlaceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_LOADING = 1;
    private static final int VIEW_TYPE_ITEM = 0;
    private final ImageLoader imageLoader;
    private ArrayList<Place> places;
    private MainActivity mContext;
    private String oldRef;

    public PlaceAdapter(MainActivity context, ArrayList<Place> places) {
        this.mContext = context;
        this.places = places;
        imageLoader= new ImageLoader(context);
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.place_item, parent, false);
            ViewHolder holder = new ViewHolder(view);
            return holder;
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.loading_progress_item, parent, false);
            ProgressHolder progressHolder = new ProgressHolder(view);
            return progressHolder;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        final Place place = places.get(position);
        if (place instanceof ProgressModel || viewHolder instanceof ProgressHolder)
            return;
        PlaceItem placeItem = (PlaceItem) place;

        final ViewHolder holder = ((ViewHolder) viewHolder);

        imageLoader.DisplayImage(placeItem.getIcon(), holder.ivIcon);

        holder.tvName.setText(placeItem.getName());
        holder.tvDistance.setText(placeItem.getDistance() + "");
        holder.tvVicinity.setText(placeItem.getVicinity());

        holder.item.setOnClickListener(view -> {
            if(placeItem.getPhotoReference()==null) {
                Toast.makeText(mContext, "No reference Value received", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(mContext, PlaceDetailsActivty.class);
            intent.putExtra(PlaceDetailsActivty.PLACE_PHOTO_REFERENCE,placeItem.getPhotoReference());
            intent.putExtra(PlaceDetailsActivty.PLACE_NAME,placeItem.getName());
            mContext.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return places == null ? 0 : places.size();
    }

    @Override
    public int getItemViewType(int position) {
        return (position == places.size() - 1 && getItem(position) instanceof ProgressModel) ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private View item;
        protected TextView tvName, tvDistance, tvVicinity;
        ImageView ivIcon;

        public ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            tvVicinity = itemView.findViewById(R.id.tvVicinity);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            item = itemView.findViewById(R.id.item);
        }
    }

    public class ProgressHolder extends RecyclerView.ViewHolder {
        protected View progressBar;

        public ProgressHolder(View itemView) {
            super(itemView);
            this.progressBar = itemView.findViewById(R.id.progressBar);

        }
    }

    //    public void addLoadingFooter() {
//        isLoadingAdded = true;
//
//        PlaceItem placeItem = new PlaceItem();
//        placeItem.setLoader(true);
//        add(placeItem);
//    }
//
//    public void removeLoadingFooter() {
//        isLoadingAdded = false;
//
//        int position = places.size() - 1;
//        Place place = getItem(position);
//
//        if (place != null) {
//            places.remove(position);
//            notifyItemRemoved(position);
//        }
//    }
//
    public Place getItem(int position) {
        return places.get(position);
    }
//
//    public void add(PlaceItem placeItem) {
//        if( oldRef != placeItem.getPhotoReference()) {
//            oldRef = placeItem.getPhotoReference();
//            places.add(placeItem);
//
//            Handler handler = new Handler();
//
//            final Runnable r = new Runnable() {
//                public void run() {
//                    notifyItemInserted(places.size() - 1);
//                }
//            };
//
//            handler.post(r);
//        }
//    }
}
