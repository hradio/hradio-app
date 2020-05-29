package lmu.hradio.hradioshowcase.view.fragment;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.hradio.radiowebview.RadioWebView;
import eu.hradio.timeshiftplayer.TimeshiftPlayer;
import lmu.hradio.hradioshowcase.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class WebViewFragment extends Fragment {

    private static final String urlTag = "url";

    @BindView(R.id.radio_web_view)
    RadioWebView radioWebView;

    private TimeShiftHolder holder;

    public WebViewFragment() {
        // Required empty public constructor
    }
    
    public static WebViewFragment newInstance(String url) {
        Bundle args = new Bundle();
        args.putString(urlTag, url);
        WebViewFragment fragment = new WebViewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof TimeShiftHolder){
            holder = (TimeShiftHolder) context;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_web_view, container, false);
        ButterKnife.bind(this, view);
        if(savedInstanceState == null) {
            radioWebView.reset();
            if (getArguments() != null) {
                radioWebView.loadUrl(getArguments().getString(urlTag));
                if (holder.getTimeShiftPlayer() != null)
                    radioWebView.setTimeshiftPlayer(holder.getTimeShiftPlayer());
            }
        }
        return view;
    }

    public interface TimeShiftHolder{
        TimeshiftPlayer getTimeShiftPlayer();
    }

}
