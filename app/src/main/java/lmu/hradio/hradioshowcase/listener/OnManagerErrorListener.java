package lmu.hradio.hradioshowcase.listener;

import androidx.annotation.NonNull;

import lmu.hradio.hradioshowcase.error.GeneralError;

/**
 * error callback class
 */
public interface OnManagerErrorListener {

    /**
     * error callback
     *
     * @param error - the exception occured
     */
    void onError(@NonNull GeneralError error);
}
