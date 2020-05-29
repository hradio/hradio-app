package lmu.hradio.hradioshowcase.view.fragment;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.text.InputType;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import eu.hradio.httprequestwrapper.dtos.recommendation.Recommender;
import eu.hradio.httprequestwrapper.listener.OnSearchResultListener;
import lmu.hradio.hradioshowcase.BuildConfig;
import lmu.hradio.hradioshowcase.R;
import lmu.hradio.hradioshowcase.listener.TrackLikeService;
import lmu.hradio.hradioshowcase.manager.substitution.SubstitutionProviderType;
import lmu.hradio.hradioshowcase.util.DataCollectionHelper;
import lmu.hradio.hradioshowcase.util.SharedPreferencesHelper;
import lmu.hradio.hradioshowcase.view.component.PreferenceView;
import lmu.hradio.hradioshowcase.view.fragment.dialog.RecommenderPreferencesDialogFragment;
import lmu.hradio.hradioshowcase.view.fragment.dialog.SelectPlaylistDialogFragment;
import lmu.hradio.hradioshowcase.view.fragment.dialog.SelectSubstitutionDialogFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {

    private final static String TAG = "SettingsFragment";

    @BindView(R.id.cdts_username_preference)
    PreferenceView cdtsUsernamePref;
    @BindView(R.id.data_usage_preference)
    PreferenceView dataUsagePref;
    @BindView(R.id.recommender_preference)
    PreferenceView recommnederPref;
    @BindView(R.id.dark_mode_preference)
    Switch darkModeSwitch;
    @BindView(R.id.substitution_playlist_preference)
    PreferenceView substitutionPlaylist;
    @BindView(R.id.substitution_provider_preference)
    PreferenceView substitutionProvider;
    @BindView(R.id.web_view_pref)
    PreferenceView radioWebPreference;
    @BindView(R.id.pref_about)
    PreferenceView aboutPreference;
    @BindView(R.id.del_cdts_pref)
    Button delCdtsPref;

    private SettingsActivity activity;

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance() {
        Bundle args = new Bundle();
        SettingsFragment fragment = new SettingsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof SettingsActivity) {
            activity = (SettingsActivity) context;
        } else {
            throw new RuntimeException(context.getClass().getSimpleName() + " no instance of SettingsActivity");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this, view);
        boolean darkMode = SharedPreferencesHelper.getBoolean(view.getContext(), "dark_mode", true);
        darkModeSwitch.setChecked(darkMode);
        darkModeSwitch.setOnCheckedChangeListener((v, b) -> {
            toggleDarkMode(b);
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fillPreferenceContent(view.getContext());
    }


    private void fillPreferenceContent(@Nullable Context context){
        if(context != null) {
            cdtsUsernamePref.setTitle("Crosss Device Timeshift Username");
            String cdtsUsername = SharedPreferencesHelper.getString(context, SharedPreferencesHelper.CDTS_USERNAME);
            if(!cdtsUsername.isEmpty()) {
                cdtsUsernamePref.setContentText(cdtsUsername);
            } else {
                cdtsUsernamePref.setContentText("Set your CDTS username");
            }

            dataUsagePref.setTitle(context.getString(R.string.data_usage_title));
            dataUsagePref.setContentText(context.getString(R.string.data_usage_summary));

            recommnederPref.setTitle(context.getString(R.string.recommender_title));
            recommnederPref.setContentText(context.getString(R.string.recommender_summary));

            substitutionPlaylist.setTitle(context.getString(R.string.substitution_playlist_title));

            String substitutionContentText = SharedPreferencesHelper.getString(context, SharedPreferencesHelper.SUBSTITUTION_PLAYLIST_NAME_KEY);
            substitutionContentText = substitutionContentText.isEmpty() ? context.getString(R.string.substitution_playlist_summary) : substitutionContentText;
            substitutionPlaylist.setContentText(substitutionContentText);

            int provider = SharedPreferencesHelper.getInt(context, SharedPreferencesHelper.SUBSTITUTION_PROVIDER_TYPE, 0);
            if(provider == 0) {
                substitutionProvider.setContentText(context.getString(R.string.substitution_provider_summary));
            }else{
                substitutionProvider.setContentText(getString(SubstitutionProviderType.values()[provider].getDisplayResource()));
            }
            substitutionProvider.setTitle(context.getString(R.string.substitution_provider_title));

            radioWebPreference.setTitle(context.getString(R.string.web_config_location_title));
            radioWebPreference.setContentText(context.getString(R.string.web_config_location_storage));

            aboutPreference.setTitle(context.getString(R.string.pref_about));
            aboutPreference.setContentText(context.getString(R.string.pref_about_text));

        }
    }

    @OnClick(R.id.cdts_username_preference)
    void setCdtsUsername() {
        if(getActivity() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DialogTheme);
            builder.setTitle("CDTS Name");
            final EditText input = new EditText(getActivity());
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String inputText = input.getText().toString();
                    if(BuildConfig.DEBUG) Log.d(TAG, "CDTS Username set to: " + inputText);

                    if(getActivity() != null && !inputText.isEmpty()) {
                        if(inputText.length() >= 3 && inputText.length() <= 128) {
                            SharedPreferencesHelper.put(getActivity(), SharedPreferencesHelper.CDTS_USERNAME, inputText);
                            cdtsUsernamePref.setContentText(inputText);
                            activity.cdtsUsernameUpdated();
                        } else {
                            Snackbar snackbar = Snackbar.make(getActivity().findViewById(R.id.container), R.string.cdts_username_length_err, Snackbar.LENGTH_SHORT);
                            snackbar.show();
                        }
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        }
    }

    @OnClick(R.id.pref_about)
    void showAboutDialog() {
        if(getActivity() != null) {
            AlertDialog.Builder aboutBuilder = new AlertDialog.Builder(getActivity(), R.style.DialogTheme);
            aboutBuilder.setTitle(R.string.pref_about);
            aboutBuilder.setMessage(R.string.pref_about_content);

            aboutBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            AlertDialog aboutDiag = aboutBuilder.create();
            aboutDiag.show();
            TextView alertText = aboutDiag.findViewById(android.R.id.message);
            if(alertText != null) {
                alertText.setMovementMethod(LinkMovementMethod.getInstance());
            }

            aboutDiag.show();
        }
    }

    @OnClick(R.id.del_cdts_pref)
    void delCdtsSettings() {
        if(BuildConfig.DEBUG)Log.d(TAG, "Deleting CDTS settings");

        if(getActivity() != null) {
            cdtsUsernamePref.setContentText("");
            activity.cdtsUsernameDeleted();
        }
    }

    @OnClick(R.id.data_usage_preference)
    void openDataUsageDialog() {
        activity.openPreferenceView();
    }

    /**
     * Shows the recommender preferences dialog
     */
    @OnClick(R.id.recommender_preference)
    void openRecommenderDialog() {
        if (activity != null) {
            activity.retrieveAvailableRecommenders((recommenders) -> activity.runOnUiThread(() -> {
                FragmentManager fm = getChildFragmentManager();
                RecommenderPreferencesDialogFragment collectUserDataDialogFragment = RecommenderPreferencesDialogFragment.newInstance(recommenders);
                collectUserDataDialogFragment.show(fm, SharedPreferencesHelper.RECOMMENDER_PREFERENCES_KEY);
            }));
        }
    }

    @OnClick(R.id.substitution_provider_preference)
    void openSubstitutionProviderDialog() {
        FragmentManager fm = getChildFragmentManager();
        SelectSubstitutionDialogFragment dialogFragment = SelectSubstitutionDialogFragment.newInstance();
        dialogFragment.show(fm, SelectSubstitutionDialogFragment.class.getSimpleName());
        dialogFragment.registerOnDismissListener(() -> {
            fillPreferenceContent(getContext());
            activity.updateSubstitutionProvider();
        });
    }

    @OnClick(R.id.substitution_playlist_preference)
    void openSubstitutionPlaylistDialog() {
        activity.getAllPlayLists(playLists -> {
            if (activity != null)
                activity.runOnUiThread(() -> {
                    FragmentManager fm = getChildFragmentManager();
                    SelectPlaylistDialogFragment dialogFragment = SelectPlaylistDialogFragment.newInstance(playLists);
                    dialogFragment.show(fm, SelectPlaylistDialogFragment.class.getSimpleName());
                    dialogFragment.registerOnDismissListener(() -> fillPreferenceContent(getContext()));
                });
        });
    }

    @OnClick(R.id.web_view_pref)
    void openRadioWebDialog() {
        activity.openWeburlConfigFileChooser();
    }

    private void toggleDarkMode(boolean darkMode) {
        if (getContext() != null && activity != null) {
            SharedPreferencesHelper.put(this.getContext(), "dark_mode", darkMode);
            activity.recreate();
        }
    }


    public interface SettingsActivity {

        void retrieveAvailableRecommenders(OnSearchResultListener<Recommender[]> resultListener);

        void runOnUiThread(Runnable runnable);

        void recreate();

        void openPreferenceView();

        void openWeburlConfigFileChooser();

        void getAllPlayLists(OnSearchResultListener<List<TrackLikeService.PlayList>> resultListener);

        void removeSubstitutionPlayList();

        void setSubstitutionPlayList(TrackLikeService.PlayList playList);

        void updateSubstitutionProvider();

        void cdtsUsernameUpdated();

        void cdtsUsernameDeleted();
    }


}
