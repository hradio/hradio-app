package lmu.hradio.hradioshowcase.view.fragment.dialog;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.config.IConfigurationProvider;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import eu.hradio.httprequestwrapper.dtos.SearchNode;
import eu.hradio.httprequestwrapper.dtos.service_search.RankedStandaloneService;
import eu.hradio.httprequestwrapper.dtos.service_search.ServiceList;
import eu.hradio.httprequestwrapper.service.SearchNodeResolver;
import eu.hradio.httprequestwrapper.service.SearchNodeResolverImpl;
import lmu.hradio.hradioshowcase.BuildConfig;
import lmu.hradio.hradioshowcase.R;
import lmu.hradio.hradioshowcase.manager.ImageDownloadTask;
import lmu.hradio.hradioshowcase.manager.LocationReader;
import lmu.hradio.hradioshowcase.model.view.ImageData;
import lmu.hradio.hradioshowcase.model.view.RadioServiceViewModel;
import lmu.hradio.hradioshowcase.spotify.web.model.Image;
import lmu.hradio.hradioshowcase.util.ImageDataHelper;


public class MapDialogFragment extends DialogFragment {

    private static final String TAG = MapDialogFragment.class.getSimpleName();
    private static final String RESULT_TAG = "results";

    @BindView(R.id.map_view)
    MapView mMapView;

    private MyLocationNewOverlay mLocationOverlay;
    private ScaleBarOverlay mScaleBarOverlay;
    private RotationGestureOverlay mRotationGestureOverlay;
    private SearchNodeResolver searchNodeResolver;

    private Map<String, Marker> markers;

    public MapDialogFragment() {
        // Required empty public constructor
    }


    public static MapDialogFragment newInstance(List<RadioServiceViewModel> searchResult) {
        MapDialogFragment fragment = new MapDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(RESULT_TAG, new ArrayList<>(searchResult));
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Context ctx =context.getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        IConfigurationProvider osmConf = Configuration.getInstance();
        File basePath = new File(context.getCacheDir().getAbsolutePath(), "osmdroid");
        File tileCache = new File(osmConf.getOsmdroidBasePath().getAbsolutePath(), "tile");
        osmConf.setOsmdroidTileCache(tileCache);
        osmConf.setOsmdroidBasePath(basePath);
        osmConf.setUserAgentValue(context.getPackageName());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        ButterKnife.bind(this, view);
        markers = new HashMap<>();

        searchNodeResolver = new SearchNodeResolverImpl();
        Bundle args = getArguments();
        if(args != null && args.getSerializable(RESULT_TAG) != null){
            List<RadioServiceViewModel> searchResult = (List<RadioServiceViewModel>) args.getSerializable(RESULT_TAG);
            for (RadioServiceViewModel service : searchResult){
                resolveService(service);
            }
        }


        initMapView();

        return view;
    }

    private void initMapView() {
        mMapView.setMultiTouchControls(true);
        mMapView.setTileSource(TileSourceFactory.ChartbundleWAC);
        mMapView.setTilesScaledToDpi(true);

        LocationReader.getInstance().readUserLocation(getActivity(), (location, error) -> {
            if(location != null){
                mMapView.setExpectedCenter(new GeoPoint(location.getLatitude(), location.getLongitude()));
            }
        });


        mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(Objects.requireNonNull(getContext())), mMapView);
        mLocationOverlay.enableMyLocation();
        mMapView.getOverlays().add(this.mLocationOverlay);

        mRotationGestureOverlay = new RotationGestureOverlay(mMapView);
        mRotationGestureOverlay.setEnabled(true);
        mMapView.getOverlays().add(mRotationGestureOverlay);

        final DisplayMetrics dm = Objects.requireNonNull(getContext()).getResources().getDisplayMetrics();
        mScaleBarOverlay = new ScaleBarOverlay(mMapView);
        mScaleBarOverlay.setCentred(true);
        mScaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10);
        mMapView.getOverlays().add(this.mScaleBarOverlay);

        IMapController mapController = mMapView.getController();
        mapController.setZoom(6.0);
    }

    private void resolveService(RadioServiceViewModel service) {
        if(service.getSource() != null && !service.getSource().isEmpty())
        searchNodeResolver.resolveNodeLocation(service.getSource(), nodes ->{
            for(SearchNode node : nodes){
                markOnMap(service, node);
            }
        }, error -> {
            if(BuildConfig.DEBUG)Log.e(TAG, "Node resolving failed");
        });
    }

    private void markOnMap(RadioServiceViewModel service, SearchNode node){
       if(getActivity() != null && mMapView != null){
           getActivity().runOnUiThread(() -> {

               Marker marker;
               if(!markers.containsKey(service.getSource())) {
                   marker = new Marker(mMapView);
                   GeoPoint geoPoint = new GeoPoint(node.getLocation().getGeoPoint().getLat(), node.getLocation().getGeoPoint().getLon());
                   marker.setPosition(geoPoint);
                   markers.put(service.getSource(), marker);
                   marker.setTitle(service.getSource() + ":");
                   IMapController mapController = mMapView.getController();
                   mapController.setCenter(geoPoint);

               } else {
                   marker = markers.get(service.getSource());
               }
               String title = marker.getTitle()+  "\n" + service.getServiceLabel();
               marker.setTitle(title);


               mMapView.getOverlays().add(marker);

           });
       }
    }

    @Override
    public void show(@NonNull FragmentManager manager, @Nullable String tag) {
        if(manager.isStateSaved())
            return;
        super.show(manager, tag);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();

    }

    @Override
    public void onPause(){
        super.onPause();
        mMapView.onPause();
    }

}
