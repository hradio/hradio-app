package lmu.hradio.hradioshowcase.view.fragment.dialog;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import lmu.hradio.hradioshowcase.R;
import lmu.hradio.hradioshowcase.manager.substitution.SubstitutionProviderType;
import lmu.hradio.hradioshowcase.util.SharedPreferencesHelper;
import lmu.hradio.hradioshowcase.view.adapter.SelectSubstitutionProviderAdapter;
import lmu.hradio.hradioshowcase.view.fragment.SettingsFragment;

import static lmu.hradio.hradioshowcase.util.SharedPreferencesHelper.SUBSTITUTION_PROVIDER_TYPE;

public class SelectSubstitutionDialogFragment extends DialogFragment {

    private static final String TAG = SelectSubstitutionDialogFragment.class.getSimpleName();

    @BindView(R.id.substitution_recycler_view)
    RecyclerView recyclerView;

    private SettingsFragment.SettingsActivity activity;

    public SelectSubstitutionDialogFragment() {
        // Required empty public constructor
    }

    public static SelectSubstitutionDialogFragment newInstance() {
        SelectSubstitutionDialogFragment fragmentDialog = new SelectSubstitutionDialogFragment();
        Bundle bundle = new Bundle();
        fragmentDialog.setArguments(bundle);
        return fragmentDialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dialog_substitution_provider_list, container, false);
        ButterKnife.bind(this, view);
        int provider = SharedPreferencesHelper.getInt(view.getContext(),SUBSTITUTION_PROVIDER_TYPE,0);
        SelectSubstitutionProviderAdapter adapter = new SelectSubstitutionProviderAdapter(SubstitutionProviderType.values()[provider]);
        recyclerView.setAdapter(adapter);
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        if(getDialog() != null && getDialog().getWindow() != null)
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void show(@NonNull FragmentManager manager, @Nullable String tag) {
        if(manager.isStateSaved())
            return;
        super.show(manager, tag);
    }


    @OnClick(R.id.ok_button)
    void onOk(){
        dismiss();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        if(getActivity() instanceof SettingsFragment.SettingsActivity)
            activity = (SettingsFragment.SettingsActivity) getActivity();
        return super.onCreateDialog(bundle);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if(listener != null){
            listener.onDismiss();
            listener = null;
        }
    }

    private OnDismissListener listener;

    public void registerOnDismissListener(OnDismissListener listener) {
        this.listener = listener;
    }

    @FunctionalInterface
    public interface OnDismissListener{
        void onDismiss();
    }

}
