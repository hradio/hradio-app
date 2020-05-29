package lmu.hradio.hradioshowcase.view.fragment.questionaire;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.hradio.prudac.model.Report;
import com.hradio.prudac.model.Survey;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import lmu.hradio.hradioshowcase.R;
import lmu.hradio.hradioshowcase.view.adapter.QuestionairePagerAdapter;

public class QuestionaireFragment extends Fragment {

    private static final String TAG = QuestionaireFragment.class.getSimpleName();
    private static final String SURVEY_TAG = "survey";
    private static final String REPORT_TAG = "report";

    @BindView(R.id.questionaire_viewpager)
    ViewPager pager;

    @BindView(R.id.next_button)
    Button nextButton;

    private OnSurveyCompletedListener listener;

    private QuestionairePagerAdapter adapter;

    public QuestionaireFragment() {
        // Required empty public constructor
    }

    @OnClick(R.id.next_button)
    public void onNextClicked(){
        if(pager.getCurrentItem() == adapter.getCount() - 1){
            listener.onComplete(adapter.getReport(), adapter.getSurvey());
            return;
        } else if (pager.getCurrentItem() == adapter.getCount() - 2){
            nextButton.setText(R.string.send);
        }else{
            nextButton.setText(R.string.next);
        }
        int next = (pager.getCurrentItem() == adapter.getCount() - 1 )? pager.getCurrentItem() : pager.getCurrentItem() +1;
        pager.setCurrentItem(next, true);
    }

    @OnClick(R.id.back_button)
    public void onBackButton(){
        int back = (pager.getCurrentItem() ==0 )? pager.getCurrentItem() : pager.getCurrentItem() -1;
        pager.setCurrentItem(back, true);
        nextButton.setText(R.string.next);
    }

    public static QuestionaireFragment newInstance(Survey survey, Report report) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(SURVEY_TAG, survey);
        bundle.putSerializable(REPORT_TAG, report);
        QuestionaireFragment fragment = new QuestionaireFragment();
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
        View view =  inflater.inflate(R.layout.fragment_questionaire, container, false);
        ButterKnife.bind(this, view);
        if(getArguments()!= null) {
            Survey survey = (Survey) getArguments().getSerializable(SURVEY_TAG);
            Report report = (Report) getArguments().getSerializable(REPORT_TAG);
            adapter = new QuestionairePagerAdapter(getChildFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT ,survey, report);
            pager.setAdapter(adapter);
        }
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof OnSurveyCompletedListener){
            this.listener = (OnSurveyCompletedListener) context;
        }
    }

    public interface OnSurveyCompletedListener{
        void onComplete(Report report, Survey survey);
    }

}
