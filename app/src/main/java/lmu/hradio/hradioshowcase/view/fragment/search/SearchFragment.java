package lmu.hradio.hradioshowcase.view.fragment.search;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;

import org.omri.radio.Radio;
import org.omri.tuner.Tuner;
import org.omri.tuner.TunerType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import eu.hradio.httprequestwrapper.dtos.SearchNode;
import eu.hradio.httprequestwrapper.dtos.service_search.Genre;
import eu.hradio.httprequestwrapper.query.elastic.ESQuery;
import eu.hradio.httprequestwrapper.service.SearchNodeResolver;
import eu.hradio.httprequestwrapper.service.SearchNodeResolverImpl;
import lmu.hradio.hradioshowcase.BuildConfig;
import lmu.hradio.hradioshowcase.R;
import lmu.hradio.hradioshowcase.error.GeneralError;
import lmu.hradio.hradioshowcase.listener.OnManagerErrorListener;
import lmu.hradio.hradioshowcase.manager.LocationReader;
import lmu.hradio.hradioshowcase.model.view.CheckedValue;
import lmu.hradio.hradioshowcase.model.view.FederationState;
import lmu.hradio.hradioshowcase.util.ColorUtils;
import lmu.hradio.hradioshowcase.view.fragment.dialog.CheckedListDialogFragment;
import lmu.hradio.hradioshowcase.view.fragment.dialog.FederationDialogFragment;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnSearchRequestListener} interface
 * to handle interaction events.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends Fragment implements CheckedValue.OnCheckChangeListener, FederationDialogFragment.OnAcceptListener {

    private static final String SELECTED_TUNER_TYPES = "tuner-types";
    private static final String FEDERATION = "federation";

    private OnSearchRequestListener mListener;
    private DismissCallback callback;
    private FederationState state;


    @BindView(R.id.selected_tuner_types_text_view)
    TextView selectedTunerTypesTextView;
    //@BindView(R.id.recommendation_typ_spinner)Spinner recommendationTypSpinner;
    @BindView(R.id.name_edit_text)
    TextInputEditText nameEditText;
    @BindView(R.id.genre_edit_text)
    TextInputEditText genreEditText;
    @BindView(R.id.provider_edit_text)
    TextInputEditText providerEditText;

    @BindView(R.id.programme_edit_text)
    TextInputEditText programmeEditText;
    @BindView(R.id.distance_seek_bar)
    SeekBar distanceSeekBar;
    @BindView(R.id.selected_distance_text_view)
    TextView selectedDistanceTextView;
    @BindView(R.id.enable_distance_switch)
    Switch enableDistanceSwitch;

    @BindView(R.id.distance_container)
    View distanceContainer;

    private ArrayList<CheckedTunerTypeValue> selectedTunerTypes;


    public SearchFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SearchFragment.
     */
    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        ButterKnife.bind(this, view);
        enableDistanceSwitch.setChecked(false);
        distanceContainer.setVisibility(View.GONE);
        enableDistanceSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            distanceSeekBar.setEnabled(b);
            distanceContainer.setVisibility(b ? View.VISIBLE : View.GONE);

        });
        distanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                String distanceText = String.format(getResources().getString(R.string.distance_unit), i + 50);
                selectedDistanceTextView.setText(distanceText);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        distanceSeekBar.setProgress(150);
        distanceSeekBar.setEnabled(false);
        if (savedInstanceState != null) {
            Serializable serializable = savedInstanceState.getSerializable(SELECTED_TUNER_TYPES);
            if (serializable instanceof ArrayList<?>)
                selectedTunerTypes = (ArrayList<CheckedTunerTypeValue>) serializable;

            this.state = (FederationState) savedInstanceState.getSerializable(FEDERATION);

        }
        if (selectedTunerTypes == null) {
            selectedTunerTypes = new ArrayList<>();

            for (Tuner tuner : Radio.getInstance().getAvailableTuners()) {
                if (tuner.getTunerType() == TunerType.TUNER_TYPE_IP_SHOUTCAST)
                    selectedTunerTypes.add(new CheckedTunerTypeValue(tuner.getTunerType(), false, this));
                else
                    selectedTunerTypes.add(new CheckedTunerTypeValue(tuner.getTunerType(), true, this));
            }
        } else {
            for (CheckedValue checkedValue : selectedTunerTypes) {
                checkedValue.setListener(this);
            }
        }

        StringBuilder builder = new StringBuilder();
        for (CheckedValue g : selectedTunerTypes) {
            if (g.isChecked()) {
                if (!builder.toString().isEmpty())
                    builder.append(", ");
                builder.append(g.toString());
            }
        }
        selectedTunerTypesTextView.setText(builder.toString());


        return view;
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(SELECTED_TUNER_TYPES, selectedTunerTypes);
        outState.putSerializable(FEDERATION, state);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnSearchRequestListener) {
            mListener = (OnSearchRequestListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnSearchRequestListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        callback = null;
    }

    @OnClick(R.id.search_button)
    void onSearchClicked() {
        if(BuildConfig.DEBUG)Log.d(TAG, "searchFor onSearchClicked()");
        Map<String, String> searchParams = new HashMap<>();
        if (!TextUtils.isEmpty(nameEditText.getText()))
            searchParams.put(Keys.NAME, (nameEditText.getText().toString()).trim().replaceAll(" ", "*")+"*");
        if (!TextUtils.isEmpty(genreEditText.getText()))
            searchParams.put(Keys.GENRE, (genreEditText.getText().toString().trim()).toLowerCase().replaceAll(" ", ","));
        if (!TextUtils.isEmpty(providerEditText.getText())) {
            searchParams.put(Keys.PROVIDER, (providerEditText.getText().toString().trim()).toLowerCase());
        }
        if (!TextUtils.isEmpty(programmeEditText.getText())) {
            searchParams.put(Keys.PROGRAMME, programmeEditText.getText().toString().trim());
        }

        if (state != null) {
            searchParams.put(Keys.TLQ_SEARCH_FEDERATION_MODE, "FEDERATED");
            searchParams.put(Keys.TLQ_MAX_BREATH, state.getSearchWidth() + "");
            searchParams.put(Keys.TLQ_MAX_DEPTH, state.getSearchDepth() + "");
            if (!state.getGenres().isEmpty()) {
                List<String> checkedGenres = checkedToStringList(state.getGenres());
                if (checkedGenres.size() > 0)
                    searchParams.put(Keys.TLQ_GENRES, processInput(checkedGenres, "*"));// (Genre)state.getGenres().get(0).getValue()).getName());
            }
            if (!state.getKeywords().isEmpty()) {
                List<String> checkedKeywords = checkedToStringList(state.getKeywords());
                if (checkedKeywords.size() > 0)
                    searchParams.put(Keys.TLQ_KEYWORDS, processInput(checkedKeywords, "*"));//(String) state.getKeywords().get(0).getValue());
            }
        }

        if (enableDistanceSwitch.isChecked() && getActivity() != null) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                LocationReader.getInstance().readUserLocation(getActivity(), (location, error) -> {
                    if (location != null) {
                        searchParams.put(Keys.DISTANCE, distanceSeekBar.getProgress() + 50 + "km");
                        searchParams.put(Keys.LAT, location.getLatitude() + "");
                        searchParams.put(Keys.LON, location.getLongitude() + "");
                        sendRequest(searchParams);
                    } else {
                        enableDistanceSwitch.setChecked(false);
                        ((OnManagerErrorListener) getActivity()).onError(new GeneralError(GeneralError.LOCATION_ERROR_DISABLED));
                    }
                });
            }

        } else {
            sendRequest(searchParams);
        }
    }

    private void sendRequest(Map<String, String> requestParams) {
        if(BuildConfig.DEBUG)Log.d(TAG, "searchFor sendRequest()");
        List<TunerType> selectedTunerTypes = new ArrayList<>();
        for (CheckedTunerTypeValue value : this.selectedTunerTypes) {
            if (value.isChecked())
                selectedTunerTypes.add(value.getValue());
        }

        if (requestParams.isEmpty() && selectedTunerTypes.contains(TunerType.TUNER_TYPE_IP_SHOUTCAST) && getActivity() != null) {
            AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
            alertDialog.setTitle(R.string.empty_search_title);
            alertDialog.setMessage(getString(R.string.empty_search_descriptiom));
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.empty_search_accept),
                    (dialog, which) -> {
                        mListener.searchFor(requestParams, selectedTunerTypes);
                        if (callback != null) {
                            callback.dismiss();
                            callback = null;
                        }
                        dialog.dismiss();
                    });
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.empty_search_decline), (dialog, which) -> dialog.dismiss());
            alertDialog.show();
            int color = ColorUtils.resolveAttributeColor(R.attr.colorAccent, getContext());
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(color);
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(color);
        } else {
            mListener.searchFor(requestParams, selectedTunerTypes);
            if (callback != null) {
                callback.dismiss();
                callback = null;
            }
        }
    }

    private List<String> checkedToStringList(List<CheckedValue> checkedValueList) {
        List<String> l = new ArrayList<>();
        for (CheckedValue elem : checkedValueList) {
            if (elem.isChecked())
                l.add(elem.getValue().toString());
        }
        return l;
    }

    private String processInput(String input) {
        String[] splits = input.split(",");
        return processInput(Arrays.asList(splits), "*");
    }

    private String processInput(List<String> input, String separator) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < input.size(); i++) {
            builder.append(input.get(i).trim()).append(separator);
            if (i < input.size() - 1) {
                builder.append(",");
            }
        }
        return builder.toString();
    }

    @OnClick(R.id.tuner_type_button)
    void onSelectTunerClicked() {
        CheckedListDialogFragment dialogFragment = CheckedListDialogFragment.newInstance(selectedTunerTypes, R.string.tuner_type);
        dialogFragment.show(getChildFragmentManager(), SELECTED_TUNER_TYPES);
    }

    private final static String TAG = "SearchFragment";
    @OnClick(R.id.federation_button)
    void onFederationClicked() {
        if(BuildConfig.DEBUG)Log.d(TAG, "searchFor with federation");
        SearchNodeResolver nodeResolver = new SearchNodeResolverImpl();
        nodeResolver.getAllNodes("http://141.84.213.235:8080/api/v1", searchNodes -> {

            List<Genre> genres = new ArrayList<>();
            List<String> keywords = new ArrayList<>();

            for (SearchNode node : searchNodes) {
                Genre[] parsedGenres = SearchNode.parseGenres(node.getGenres());
                if (parsedGenres != null) {
                    genres.addAll(Arrays.asList(parsedGenres));
                    for (Genre g : genres)
                        System.out.println("GENRE: " + g.getName());
                }
                keywords.addAll(Arrays.asList(node.getKeywords()));
            }

            if (state==null) {
                state = new FederationState();
                state.setKeywords(keywords);
                state.setGenres(genres);
            }

            FederationDialogFragment dialogFragment = FederationDialogFragment.newInstance(state);
            dialogFragment.show(getChildFragmentManager(), FEDERATION);
        }, e -> {
            if (BuildConfig.DEBUG) Log.e(SearchFragment.class.getSimpleName(), e.toString());
        });

    }

    @Override
    public void onCheckChange() {
        StringBuilder anonymousBuilder = new StringBuilder();
        for (CheckedValue g : selectedTunerTypes) {
            if (g.isChecked()) {
                if (!anonymousBuilder.toString().isEmpty())
                    anonymousBuilder.append(", ");
                anonymousBuilder.append(g.toString());
            }
        }
        selectedTunerTypesTextView.setText(anonymousBuilder.toString());


    }

    public void setDismissCallback(DismissCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onAccept(FederationState state) {
        this.state = state;

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    @FunctionalInterface
    public interface OnSearchRequestListener {
        void searchFor(Map<String, String> params, List<TunerType> tunerTypes);
    }

    @FunctionalInterface
    public interface DismissCallback {
        void dismiss();
    }

    private interface Keys {
        String PROVIDER = "providerName";
        String NAME = "name";
        String GENRE = "genres";
        String DISTANCE = "distance";
        String LAT = ESQuery.Keys.LAT;
        String LON = ESQuery.Keys.LON;
        String KEYWORDS = "keywords";
        String PROGRAMME = "programme";
        String TLQ_MAX_BREATH = "maxBreadth";
        String TLQ_MAX_DEPTH = "maxDepth";
        String TLQ_TIMEOUT = "timeout";
        String TLQ_SEARCH_FEDERATION_MODE = "searchFederationMode";
        String TLQ_GENRES = "genres_tlq";
        String TLQ_KEYWORDS = "keywords_tlq";
    }

    static class CheckedTunerTypeValue extends CheckedValue<TunerType> {

        private static final long serialVersionUID = 9125108305926229767L;

        CheckedTunerTypeValue(TunerType value, boolean checked, OnCheckChangeListener listener) {
            super(value, checked, listener);
        }

        @Override
        public String toString() {
            switch (getValue()) {
                case TUNER_TYPE_FM:
                    return "FM";
                case TUNER_TYPE_DAB:
                    return "DAB";
                case TUNER_TYPE_IP_EDI:
                    return "EDI";
                case TUNER_TYPE_IP_SHOUTCAST:
                    return "Shoutcast";
                default:
                    return "IP";
            }
        }
    }

}
