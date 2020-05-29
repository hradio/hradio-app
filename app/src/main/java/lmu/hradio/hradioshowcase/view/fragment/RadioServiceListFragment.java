package lmu.hradio.hradioshowcase.view.fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.omri.radioservice.RadioService;
import org.omri.radioservice.RadioServiceType;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import lmu.hradio.hradioshowcase.R;
import lmu.hradio.hradioshowcase.listener.OnRadioServiceUpdateListener;
import lmu.hradio.hradioshowcase.listener.PlayRadioServiceListener;
import lmu.hradio.hradioshowcase.listener.State;
import lmu.hradio.hradioshowcase.util.DeviceUtils;
import lmu.hradio.hradioshowcase.view.adapter.RadioServiceRecyclerViewAdapter;
import lmu.hradio.hradioshowcase.model.view.RadioServiceViewModel;

public class RadioServiceListFragment extends Fragment implements OnRadioServiceUpdateListener{

    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String ARG_SCROLL_ORIENTATION = "scroll-orientation";
    private int mColumnCount;
    private int orientation;
    private PlayRadioServiceListener listener;
    private RadioServiceRecyclerViewAdapter adapter;

    private State.SearchStateHolder stateHolder;

    @BindView(R.id.list)
    RecyclerView radioServiceList;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RadioServiceListFragment() {
    }

    public static RadioServiceListFragment newInstance() {
        RadioServiceListFragment fragment = new RadioServiceListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public static RadioServiceListFragment newInstance(int columnCount, int orientation) {
        RadioServiceListFragment fragment = new RadioServiceListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        args.putInt(ARG_SCROLL_ORIENTATION, orientation);
        fragment.setArguments(args);
        return fragment;
    }


    public static RadioServiceListFragment newInstance(int columnCount) {
        RadioServiceListFragment fragment = new RadioServiceListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        orientation = (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) ? RecyclerView.VERTICAL : RecyclerView.HORIZONTAL;
        mColumnCount = (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) ? 2 : 1;

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT, mColumnCount);
            orientation = getArguments().getInt(ARG_SCROLL_ORIENTATION, orientation);
        }

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_radioservice_list, container, false);
        ButterKnife.bind(this,view);

        refresh( view.getContext());
        return view;
    }

    public void refresh(Context context) {
        int layoutID;
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (orientation == RecyclerView.VERTICAL && mColumnCount == 1|| DeviceUtils.isTablet(context)) {
                layoutID = R.layout.fragment_radioservice_list_item;
            } else {
                layoutID = R.layout.fragment_radioservice_grid_item;
            }
        } else {
            if(orientation == RecyclerView.VERTICAL && mColumnCount <= 2 || DeviceUtils.isTablet(context))
                layoutID = R.layout.fragment_radioservice_list_item;
            else
                layoutID = R.layout.fragment_radioservice_grid_item;
        }

        radioServiceList.setLayoutManager(new GridLayoutManager(context, mColumnCount, orientation, false));
        stateHolder.getLastSearchResultState(resList -> {
            adapter = new RadioServiceRecyclerViewAdapter(resList, listener,layoutID);
            if(getActivity() != null) {
             getActivity().runOnUiThread(new Runnable() {
                 @Override
                 public void run() {
                     radioServiceList.setAdapter(adapter);
                 }
             });
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof PlayRadioServiceListener)
            listener = (PlayRadioServiceListener) context;
        else throw new RuntimeException("Context must implement PlayRadioServiceListener");
        if(context instanceof State.SearchStateHolder){
            stateHolder = (State.SearchStateHolder) context;
        }

    }

    public void onServiceFound(RadioServiceViewModel service, RadioService type){
        if(adapter != null)
            adapter.addContent(service, type);
    }

    public void clearServiceList(){
        if(adapter != null)
            adapter.clear();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }


    @Override
    public void onServiceUpdate(List<RadioServiceViewModel> newServices) {
        if(this.getActivity() != null)
                if(adapter != null)
                    adapter.addContent(newServices);
    }


}
