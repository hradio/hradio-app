package lmu.hradio.hradioshowcase.view.fragment.dialog;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import java.util.Date;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import lmu.hradio.hradioshowcase.R;
import lmu.hradio.hradioshowcase.util.SharedPreferencesHelper;
import lmu.hradio.hradioshowcase.view.activity.MainActivity;

public class CollectUserDataDialogFragment extends DialogFragment {

    @BindView(R.id.gender_picker)
    Spinner genderPicker;
    @BindView(R.id.age_picker)
    Spinner agePicker;
    @BindView(R.id.location_switch)
    Switch locationSwitch;
    @BindView(R.id.country_switch)
    Switch countrySwitch;
    @BindView(R.id.country_card_view)
    CardView countryCardView;

    @BindView(R.id.privacy_data_collect_link)
    TextView privacyLink;

    private OnCollectUserDataInteractionListener listener;


    public CollectUserDataDialogFragment() {
        // Required empty public constructor
    }

    @OnClick(R.id.accept_button)
    void onAcceptClicked() {
        int genderPosition = genderPicker.getSelectedItemPosition()-1;

        // The agePicker does not contain the first two items of the AgeGroup enumeration.
        // This is where the "+ 1" comes from. If item position <= 0, ageGroup is either not specified
        // or the position is invalid (-1). This is indicated with setting the ageGroup index to -1.
        int ageGroupPosition = agePicker.getSelectedItemPosition() <= 0 ? -1 : agePicker.getSelectedItemPosition() + 1;

        listener.acceptDataCollection(genderPosition,ageGroupPosition,locationSwitch.isChecked(),countrySwitch.isChecked());
        this.getDialog().dismiss();
    }


    @OnClick(R.id.decline_button)
    void onDeclineClicked(){
        listener.declineDataCollection();
        this.getDialog().dismiss();
    }


    public static CollectUserDataDialogFragment newInstance() {
        return new CollectUserDataDialogFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_collect_user_data_dialog, container, false);
        ButterKnife.bind(this, view);
        locationSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            int visibility = (b)? View.GONE : View.VISIBLE;
            countryCardView.setVisibility(visibility);
        });
        locationSwitch.setChecked(true);
        countrySwitch.setChecked(true);
        countryCardView.setVisibility(View.GONE);
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(view.getContext(), R.array.gender, R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        genderPicker.setAdapter(genderAdapter);
        ArrayAdapter<CharSequence> ageGroup = ArrayAdapter.createFromResource(view.getContext(), R.array.user_groups, R.layout.simple_spinner_item);
        ageGroup.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        agePicker.setAdapter(ageGroup);

        privacyLink.setMovementMethod(LinkMovementMethod.getInstance());

        setCancelable(false);
        return view;
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
        if(getDialog() != null && getDialog().getWindow() != null)
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

     @NonNull
    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        listener = (OnCollectUserDataInteractionListener) getActivity();
        return super.onCreateDialog(bundle);
    }

    public interface OnCollectUserDataInteractionListener{
        void declineDataCollection();
        void acceptDataCollection(int genderIndex, int ageIndex, boolean trackLocation, boolean trackCountry);
    }


}
