package lmu.hradio.hradioshowcase.listener;

/**
 * {@link OnTunerScanListener} implementation with empty Methods
 * {@link OnTunerScanListener#tunerScanFinished()}
 * {@link OnTunerScanListener#tunerScanStarted()}
 * {@link OnTunerScanListener#tunerScanProgress(int)}
 */
public abstract class OnServiceUpdateTunerScanListener implements OnTunerScanListener {

    /**
     * {@inheritDoc}
     */
    @Override
    public void tunerScanStarted() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void tunerScanProgress(final int percentScanned) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void tunerScanFinished() {
    }


}
