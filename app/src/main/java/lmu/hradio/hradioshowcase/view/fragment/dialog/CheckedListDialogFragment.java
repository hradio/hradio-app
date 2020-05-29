package lmu.hradio.hradioshowcase.view.fragment.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import lmu.hradio.hradioshowcase.BuildConfig;
import lmu.hradio.hradioshowcase.R;
import lmu.hradio.hradioshowcase.model.view.CheckedValue;


public class CheckedListDialogFragment extends DialogFragment {

    private final static String TAG = "CheckedDialog";

    private static final String ARG_SELECTED_ITEMS = "items";
    private static final String ARG_TITLE_RESOURCE = "title-resource";

    @BindView(R.id.list)
    RecyclerView recyclerView;

    @BindView(R.id.title_text)
    TextView titleText;

    public static CheckedListDialogFragment newInstance(ArrayList<? extends CheckedValue> selectedItems, int titleResource) {
        final CheckedListDialogFragment fragment = new CheckedListDialogFragment();
        final Bundle args = new Bundle();
        args.putSerializable(ARG_SELECTED_ITEMS, selectedItems);
        args.putInt(ARG_TITLE_RESOURCE, titleResource);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // request a window without the title
        Objects.requireNonNull(dialog.getWindow()).requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_checked_list_dialog, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ArrayList<CheckedValue> selectedItems;
        if(getArguments() != null){
            selectedItems = (ArrayList<CheckedValue>) getArguments().getSerializable(ARG_SELECTED_ITEMS);
            titleText.setText(getArguments().getInt(ARG_TITLE_RESOURCE));
            recyclerView.setAdapter(new TunerFilterItemAdapter(selectedItems));

        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @OnClick(R.id.accept_button)
    void onAcceptClicked(){
        dismiss();
    }

    @Override
    public void show(@NonNull FragmentManager manager, @Nullable String tag) {
        if(manager.isStateSaved())
            return;
        super.show(manager, tag);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tuner_type_check_box)
        CheckBox checkBox;

        @BindView(R.id.tuner_type_check_explanation)
        ImageButton explanationButton;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

    }



    private class TunerFilterItemAdapter extends RecyclerView.Adapter<ViewHolder> {

        private ArrayList<CheckedValue> items;

        TunerFilterItemAdapter(ArrayList<CheckedValue> selectedItems) {
            this.items = selectedItems;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_checked_list_dialog_item,  parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            final CheckedValue value = items.get(position);
            holder.checkBox.setOnCheckedChangeListener((v,b) ->{});
            holder.checkBox.setChecked(value.isChecked());
            holder.checkBox.setText(value.toString());
            holder.checkBox.setOnCheckedChangeListener((v,b) -> {
                value.setChecked(b);
            });
            holder.explanationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (BuildConfig.DEBUG) Log.d(TAG, "Check " + value.toString() + " clicked");
                    if (getActivity() != null) {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                        String title;
                        String content;
                        switch (value.toString()) {
                            case "EDI": {
                                alertDialogBuilder.setTitle(R.string.guide_edi_reception);
                                alertDialogBuilder.setMessage(R.string.guide_edi_reception_explanation);
                                break;
                            }
                            case "Shoutcast": {
                                alertDialogBuilder.setTitle(R.string.guide_shoutcast_reception);
                                alertDialogBuilder.setMessage(R.string.guide_shoutcast_reception_explanation);
                                break;
                            }
                            case "DAB": {
                                alertDialogBuilder.setTitle(R.string.guide_dab_reception);
                                alertDialogBuilder.setMessage(R.string.guide_dab_reception_explanation);
                                break;
                            }
                            default: {
                                alertDialogBuilder.setTitle("");
                                alertDialogBuilder.setMessage("");
                                break;
                            }
                        }

                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                        TextView alertText = alertDialog.findViewById(android.R.id.message);
                        if(alertText != null) {
                            alertText.setMovementMethod(LinkMovementMethod.getInstance());
                        }
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

    }

}
