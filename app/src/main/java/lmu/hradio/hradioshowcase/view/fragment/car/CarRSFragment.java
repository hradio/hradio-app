package lmu.hradio.hradioshowcase.view.fragment.car;

import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.omri.radioservice.RadioService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import eu.hradio.httprequestwrapper.util.TimeUtils;
import lmu.hradio.hradioshowcase.R;
import lmu.hradio.hradioshowcase.database.Database;
import lmu.hradio.hradioshowcase.listener.OnRadioServiceUpdateListener;
import lmu.hradio.hradioshowcase.listener.OnServiceUpdateTunerScanListener;
import lmu.hradio.hradioshowcase.listener.PlayRadioServiceListener;
import lmu.hradio.hradioshowcase.listener.State;
import lmu.hradio.hradioshowcase.model.state.PlayBackState;
import lmu.hradio.hradioshowcase.model.view.RadioServiceViewModel;
import lmu.hradio.hradioshowcase.model.view.UserActionEnum;
import lmu.hradio.hradioshowcase.view.adapter.RadioServiceRecyclerViewAdapter;


public class CarRSFragment extends Fragment implements OnRadioServiceUpdateListener, Database.OnFavoritesChangeListener, PlayBackState.OnChangeServiceListener  {

    private static final String CAR_VIEW_COLUMN_COUNT = "column-count";
    private int mColumnCount;
    private int orientation;
    private PlayRadioServiceListener prsListener;
    private RadioServiceRecyclerViewAdapter adapterRS;
    private RadioServiceRecyclerViewAdapter adapterFav;
    private RadioServiceRecyclerViewAdapter adapterRec;

    private State.SearchStateHolder searchStateHolder;
    private State.PlayBackStateHolder playbackStateHolder;

    final int layoutID = R.layout.fragment_radioservice_grid_item;

    boolean favoritesSelected= true;

    @BindView(R.id.car_fav_text)
    TextView favTV;

    @BindView(R.id.car_recom_text)
    TextView recTV;

    @BindView(R.id.car_rs_button)
    ImageButton closeRSButton;

    @BindView(R.id.car_favorites_list)
    RecyclerView favRecListRV;

    @BindView(R.id.car_rs_list)
    RecyclerView radioServiceListRV;

    @BindView(R.id.car_fav_card)
    CardView favCard;

    @BindView(R.id.car_recom_card)
    CardView recomCard;

    @BindView(R.id.car_rs_divider)
    View divider;



    public CarRSFragment() {
    }

    public static CarRSFragment newInstance() {
        return newInstance(0);
    }

    public static CarRSFragment newInstance(int columnCount) {
        CarRSFragment fragment = new CarRSFragment();
        Bundle args = new Bundle();
        args.putInt(CAR_VIEW_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        orientation = RecyclerView.HORIZONTAL;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_car_radioservices_list, container, false);
        ButterKnife.bind(this,view);

        Bundle args = this.getArguments();
        if (args != null) {
            mColumnCount= args.getInt(CAR_VIEW_COLUMN_COUNT);
        }
        else mColumnCount=2;

        orientation = RecyclerView.HORIZONTAL;
        Context context = view.getContext();

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        mColumnCount = mColumnCount != 0 ? mColumnCount: calculateNoOfColumns(200, displayMetrics.heightPixels / displayMetrics.density);

        radioServiceListRV.setLayoutManager(new GridLayoutManager(context, mColumnCount, orientation, false));
        searchStateHolder.getLastSearchResultState(res -> adapterRS = new RadioServiceRecyclerViewAdapter(res, prsListener ,layoutID));
        radioServiceListRV.setAdapter(adapterRS);

        int orientationRec = RecyclerView.HORIZONTAL;
        favRecListRV.setLayoutManager(new LinearLayoutManager(getContext(), orientationRec, false));
        setFavorites(view);
        onChangeService();

        return view;
    }


    private static int calculateNoOfColumns(float columnWidthDp, float screenMetricDP) { // For example columnWidthdp=180
        return  (int) ((screenMetricDP-100) / columnWidthDp + 0.5); // +0.5 for correct rounding to int. -200 for toolbar et al
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof State.PlayBackStateHolder){
            searchStateHolder = (State.SearchStateHolder) context;
            playbackStateHolder = (State.PlayBackStateHolder) context;
            playbackStateHolder.getState().registerOnChangeServiceListener(this);
        }
        Database.getInstance().registerFavoritesChangeListener(this);


    }

    @Override
    public void onDetach() {
        super.onDetach();
        prsListener = null;
        Database.getInstance().unregisterFavoritesChangeListener(this);
        playbackStateHolder.getState().unregisterOnChangeServiceListener(this);
    }


    @OnClick(R.id.car_fav_card)
    void showFavorites() {
        if (!favoritesSelected) {
            setFavorites(getView());
            favCard.setBackgroundColor(getResources().getColor(R.color.opaque_20_white));
            recomCard.setBackgroundColor(getResources().getColor(R.color.colorSecondaryDark));
        }
        favoritesSelected=true;
    }

    @OnClick(R.id.car_recom_card)
    void showRecommendations() {
        if (favoritesSelected) {
            setRecommendations();
            recomCard.setBackgroundColor(getResources().getColor(R.color.opaque_20_white));
            favCard.setBackgroundColor(getResources().getColor(R.color.colorSecondaryDark));
        }
        favoritesSelected=false;
    }

    private void setFavorites(View view) {
        Database.getInstance().readFavorites(favorite -> {
            if(getContext() == null)
                return;
            adapterFav = new RadioServiceRecyclerViewAdapter(favorite, prsListener, layoutID);
            this.favRecListRV.setAdapter(adapterFav);
        }, view.getContext());
    }


    @Override
    public void onServiceUpdate(List<RadioServiceViewModel> newServices) {
        if(this.getActivity() != null)
            this.getActivity().runOnUiThread(() ->{
                if(adapterRS != null)
                    adapterRS.addContent(newServices);
            });
    }


    public void setPlayRadioServiceListener(PlayRadioServiceListener listener) {
        this.prsListener = listener;
    }

    @Override
    public void onChangeService() {

        PlayBackState state = playbackStateHolder.getState();
        if (state != null) {
            adapterRec = new RadioServiceRecyclerViewAdapter(new ArrayList<>(), prsListener, layoutID);
            playbackStateHolder.getRecommendations(new OnServiceUpdateTunerScanListener() {
                @Override
                public void onResult(List<RadioServiceViewModel> services) {
                    if(services == null || services.isEmpty()) return;
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            adapterRec = new RadioServiceRecyclerViewAdapter(services, prsListener, layoutID);
                        });
                    }
                }

                @Override
                public void onServiceFound(RadioServiceViewModel service, RadioService type) { }
            });
        }
    }

    private void setRecommendations(){
        PlayBackState state = playbackStateHolder.getState();
        if (state != null) {
            if (state.getRelateRecommendedServices() != null && !state.getRelateRecommendedServices().isEmpty())
                adapterRec = new RadioServiceRecyclerViewAdapter(state.getRelateRecommendedServices(), prsListener, layoutID);
            this.favRecListRV.setAdapter(adapterRec);
        }
    }


    @Override
    public void onAdded(RadioServiceViewModel service) {
        adapterFav.addContent(service, null);
    }

    @Override
    public void onRemoved(RadioServiceViewModel service) {
        adapterFav.remove(service);

    }




}
