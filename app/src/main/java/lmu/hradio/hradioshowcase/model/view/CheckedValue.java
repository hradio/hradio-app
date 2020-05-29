package lmu.hradio.hradioshowcase.model.view;

import java.io.Serializable;

public class CheckedValue<T extends Serializable> implements Serializable {

    private static final long serialVersionUID = 7593393332751361374L;

    private T value;
    private boolean checked;
    private transient OnCheckChangeListener listener;

    public CheckedValue(T value, boolean checked) {
        this.checked = checked;
        this.value = value;
    }
    public CheckedValue(T value, boolean checked, OnCheckChangeListener listener) {
        this.checked = checked;
        this.value = value;
        this.listener = listener;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
        if(listener != null)
            listener.onCheckChange();
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    public void setListener(OnCheckChangeListener listener) {
        this.listener =  listener;
    }

    @FunctionalInterface
    public interface OnCheckChangeListener{
        void onCheckChange();
    }
}
