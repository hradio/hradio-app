package lmu.hradio.hradioshowcase.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hradio.prudac.model.Answer;
import com.hradio.prudac.model.Question;

import butterknife.BindView;
import butterknife.ButterKnife;
import lmu.hradio.hradioshowcase.R;

public class MultipleChoiceAdapter extends RecyclerView.Adapter<MultipleChoiceAdapter.ViewHolder> {

    private Question answers;
    private Answer selection;
    private boolean allowMultiselection;

    public MultipleChoiceAdapter(Question question, Answer answer, boolean allowMultiselect) {
        this.answers = question;
        this.selection = answer;
        this.allowMultiselection = allowMultiselect;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_multiple_choice_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.toggle.setOnCheckedChangeListener((c,b) ->{});
        holder.toggle.setChecked(selection.getAnswer()[position] == 1);
        holder.toggle.setText(answers.getOptions()[position]);
        holder.toggle.setOnCheckedChangeListener((compoundButton, b) ->{
            if(b && !allowMultiselection){
                resetSelection();
            }
            selection.getAnswer()[position] = b ? 1 : 0;
            notifyDataSetChanged();
        });
    }

    private void resetSelection(){
        for(int i = 0 ; i < selection.getAnswer().length; i++){
            selection.getAnswer()[i] = 0;
        }

    }


    @Override
    public int getItemCount() {
        return answers.getOptions().length;
    }


    static class ViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.selected_toggle)
        CheckBox toggle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
