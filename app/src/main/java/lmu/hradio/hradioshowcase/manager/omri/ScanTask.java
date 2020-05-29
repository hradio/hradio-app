package lmu.hradio.hradioshowcase.manager.omri;

import android.os.Bundle;

import androidx.annotation.Nullable;

import org.omri.tuner.Tuner;
import org.omri.tuner.TunerListener;
import org.omri.tuner.TunerType;

import java.util.Iterator;

import eu.hradio.httprequestwrapper.query.elastic.ESQuery;

public class ScanTask {

    private Bundle arguments;
    private Iterator<Tuner> toBeScanned;
    private int size;
    private int remaining;

    private boolean deleteExisting = false;

    public static ScanTask createTask(Iterator<Tuner> toBeScanned, int size, boolean deleteExisting) {
        return createTask(null, toBeScanned, size, deleteExisting);
    }

    public static ScanTask createTask(@Nullable Bundle arguments, Iterator<Tuner> toBeScanned, int size, boolean deleteExisting) {
        ScanTask scanTask = new ScanTask();
        scanTask.arguments = arguments;
        scanTask.toBeScanned = toBeScanned;
        scanTask.size = size;
        scanTask.remaining = size;
        scanTask.deleteExisting = deleteExisting;
        return scanTask;
    }

    public boolean scanNextTuner(TunerListener listener) {
        if (toBeScanned == null)
            return false;
        boolean returnValue = toBeScanned.hasNext();
        if (returnValue) {
            remaining--;
            Tuner tuner = toBeScanned.next();
            tuner.subscribe(listener);


            Bundle args = tuner.getTunerType().equals(TunerType.TUNER_TYPE_IP_EDI) || tuner.getTunerType().equals(TunerType.TUNER_TYPE_IP_SHOUTCAST) ?
                    arguments : new Bundle();

            if (tuner.getTunerType() == TunerType.TUNER_TYPE_IP_EDI)
                args.putString(ESQuery.Keys.BEARERS_MIME, "*edi");


            tuner.startRadioServiceScan(args);
            args.remove(ESQuery.Keys.BEARERS_MIME);
        }
        return returnValue;
    }

    public double calculateProgress(int percentScanned) {
        return (double) percentScanned * (size - remaining) / size;
    }

    public boolean isDeleteExisting() {
        return deleteExisting;
    }

    public Bundle getBundle() {
        return arguments;
    }
}
