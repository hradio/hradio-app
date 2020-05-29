package lmu.hradio.hradioshowcase.view.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.hradio.prudac.model.Report;
import com.hradio.prudac.model.Survey;

import lmu.hradio.hradioshowcase.view.fragment.questionaire.QuestionFragment;

public class QuestionairePagerAdapter extends FragmentStatePagerAdapter {

    private Survey survey;
    private Report report;

    public QuestionairePagerAdapter(@NonNull FragmentManager fm, int behavior, Survey survey, Report report) {
        super(fm, behavior);
        this.survey = survey;
        this.report = report;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return QuestionFragment.newInstance(survey.getQuestions()[position], report.getAnswers()[position]);
    }

    @Override
    public int getCount() {
        return survey.getQuestions().length;
    }

    public Survey getSurvey() {
        return survey;
    }

    public Report getReport() {
        return report;
    }
}
