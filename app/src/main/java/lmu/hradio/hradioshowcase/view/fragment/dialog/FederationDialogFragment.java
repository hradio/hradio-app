package lmu.hradio.hradioshowcase.view.fragment.dialog;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import lmu.hradio.hradioshowcase.R;
import lmu.hradio.hradioshowcase.model.view.CheckedValue;
import lmu.hradio.hradioshowcase.model.view.FederationState;


public class FederationDialogFragment extends DialogFragment {

    private static final String FEDERATION_STATE = "state";

    @BindView(R.id.genre_spinner)
    ConstraintLayout genresSpinner;

    @BindView(R.id.keywords_spinner)
    ConstraintLayout keywordSpinner;
    @BindView(R.id.search_depth_text_input)
    TextInputEditText searchDepthTextInput;

    @BindView(R.id.search_width_text_input)
    TextInputEditText searchWidthTextInput;

    @BindView(R.id.selected_genres_text_view)
    TextView selectedGenresTextView;

    @BindView(R.id.selected_keywords_text_view)
    TextView selectedKeywordsTextView;

    private FederationState state;

    public FederationDialogFragment() {
        // Required empty public constructor
    }


    public static FederationDialogFragment newInstance(FederationState state) {
        FederationDialogFragment fragment = new FederationDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(FEDERATION_STATE, state);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_federation_dialog, container, false);
        ButterKnife.bind(this, view);

        Bundle args = this.getArguments();
        if(args != null) {
        state = (FederationState) args.getSerializable(FEDERATION_STATE);
        }

        fillWithLastState();
        initViewChangeListener();
        return view;
    }

    @OnClick(R.id.genre_spinner)
    void onGenresClicked(){
        state.setListener( () ->{
            StringBuilder builder = new StringBuilder();
            for(CheckedValue g : state.getGenres()){
                if(g.isChecked()) {
                    if(!builder.toString().isEmpty())
                        builder.append(", ");
                    builder.append(g.toString());
                }
            }
            selectedGenresTextView.setText(builder.toString());
        });
        CheckedListDialogFragment listDialog = CheckedListDialogFragment.newInstance(state.getGenres(), R.string.genre_name);
        listDialog.show(getChildFragmentManager(), CheckedListDialogFragment.class.getSimpleName());
    }

    @OnClick(R.id.keywords_spinner)
    void onKeywordsClicked(){
        state.setListener( () ->{
            StringBuilder builder = new StringBuilder();
            for(CheckedValue g : state.getKeywords()){
                if(g.isChecked()) {
                    if(!builder.toString().isEmpty())
                        builder.append(", ");
                    builder.append(g.toString());
                }
            }
            selectedKeywordsTextView.setText(builder.toString());
        });
        CheckedListDialogFragment listDialog = CheckedListDialogFragment.newInstance(state.getKeywords(), R.string.keywords_name);
        listDialog.show(getChildFragmentManager(), CheckedListDialogFragment.class.getSimpleName());
    }

    @Override
    public void show(@NonNull FragmentManager manager, @Nullable String tag) {
        if(manager.isStateSaved())
            return;
        super.show(manager, tag);
    }

    @OnClick(R.id.accept_button)
    void onAcceptClicked(){
        if(acceptListener != null)
            acceptListener.onAccept(this.state);
        dismiss();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(FEDERATION_STATE, state);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(getParentFragment() instanceof OnAcceptListener)
            acceptListener = (OnAcceptListener) getParentFragment();
    }



    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void fillWithLastState(){
        if(getArguments() != null) {
            state = (FederationState) getArguments().getSerializable(FEDERATION_STATE);
            if (state != null){
                searchDepthTextInput.setText(String.valueOf(state.getSearchDepth()));
                searchWidthTextInput.setText(String.valueOf(state.getSearchWidth()));
            }
        }


        StringBuilder builder = new StringBuilder();
        for(CheckedValue g : state.getGenres()){
            if(g.isChecked()) {
                if(!builder.toString().isEmpty())
                    builder.append(", ");
                builder.append(g.toString());
            }
        }
        selectedGenresTextView.setText(builder.toString());


        builder = new StringBuilder();
        for(CheckedValue g : state.getKeywords()){
            if(g.isChecked()) {
                if(!builder.toString().isEmpty())
                    builder.append(", ");
                builder.append(g.toString());
            }
        }
        selectedKeywordsTextView.setText(builder.toString());
    }


    private void initViewChangeListener(){
        searchWidthTextInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(!editable.toString().isEmpty())
                    state.setSearchWidth(Integer.valueOf(editable.toString()));
            }
        });

        searchDepthTextInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(!editable.toString().isEmpty())
                    state.setSearchDepth(Integer.valueOf(editable.toString()));
            }
        });

    }

    private OnAcceptListener acceptListener;


    @FunctionalInterface
    public interface OnAcceptListener {
        void onAccept(FederationState state);
    }
}
