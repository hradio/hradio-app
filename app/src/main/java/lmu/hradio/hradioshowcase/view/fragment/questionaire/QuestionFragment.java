package lmu.hradio.hradioshowcase.view.fragment.questionaire;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hradio.prudac.model.Answer;
import com.hradio.prudac.model.Question;

import butterknife.BindView;
import butterknife.ButterKnife;
import lmu.hradio.hradioshowcase.R;


public class QuestionFragment extends Fragment {
    private static final String QUESTION_PARAM = "question";
    private static final String ANSWER_PARAM = "answer";

    private Question question;
    private Answer answer;

    @BindView(R.id.question_name_text_view)
    TextView questionNameTextView;

    @BindView(R.id.question_text_view)
    TextView questionTextView;


    public QuestionFragment() {
        // Required empty public constructor
    }

    public static QuestionFragment newInstance(Question question, Answer answer) {
        QuestionFragment fragment = new QuestionFragment();
        Bundle args = new Bundle();
        args.putSerializable(QUESTION_PARAM, question);
        args.putSerializable(ANSWER_PARAM, answer);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            question = (Question) getArguments().getSerializable(QUESTION_PARAM);
            answer = (Answer) getArguments().getSerializable(ANSWER_PARAM);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_question, container, false);
        ButterKnife.bind(this, view);
        questionNameTextView.setText(question.getName());
        questionTextView.setText(question.getQdescription());
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Fragment fragment = createAnswerFragment(question,answer);
        getChildFragmentManager().beginTransaction().replace(R.id.answer_container, fragment).commit();
    }

    private Fragment createAnswerFragment(Question question, Answer answer){
            switch (question.getType()){
                case mc: return MultipleChoiceFragment.newInstance(answer, question,false);
                case bool: return MultipleChoiceFragment.newInstance(answer, question, false);
                case cbx: return MultipleChoiceFragment.newInstance(answer, question, true);
                default: throw new IllegalArgumentException("Invalid question type");
             }
        }

}
