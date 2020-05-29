package lmu.hradio.hradioshowcase.view.fragment.questionaire;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hradio.prudac.model.Answer;
import com.hradio.prudac.model.Question;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import lmu.hradio.hradioshowcase.R;
import lmu.hradio.hradioshowcase.view.adapter.MultipleChoiceAdapter;

public class MultipleChoiceFragment extends Fragment {
    private static final String ANSWER_PARAM = "answer";
    private static final String QUESTION_PARAM = "question";
    private static final String ALLOW_MULTISELECTION_PARAM = "allow-multi-select";

    @BindView(R.id.answer_grid)
    RecyclerView answerGrid;

    private MultipleChoiceAdapter adapter;

    public MultipleChoiceFragment() {
        // Required empty public constructor
    }


    public static MultipleChoiceFragment newInstance(Answer answer,Question question, boolean isMultipleChoice) {
        MultipleChoiceFragment fragment = new MultipleChoiceFragment();
        Bundle args = new Bundle();
        args.putSerializable(ANSWER_PARAM, answer);
        args.putSerializable(QUESTION_PARAM, question);
        args.putBoolean(ALLOW_MULTISELECTION_PARAM, isMultipleChoice);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            boolean isMultipleChoice = getArguments().getBoolean(ALLOW_MULTISELECTION_PARAM);
            Answer answer = (Answer) getArguments().getSerializable(ANSWER_PARAM);
            Question question = (Question) getArguments().getSerializable(QUESTION_PARAM);
            adapter= new MultipleChoiceAdapter(question, answer, isMultipleChoice);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_multiple_choice, container, false);
        ButterKnife.bind(this, view);
        answerGrid.setAdapter(adapter);
        answerGrid.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

}
