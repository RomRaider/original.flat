package enginuity.logger.innovate.lc1.plugin;

import enginuity.logger.ecu.external.ExternalDataItem;
import enginuity.logger.innovate.generic.plugin.DataConvertor;
import enginuity.logger.innovate.generic.plugin.DataListener;

public final class Lc1DataItem implements ExternalDataItem, DataListener {
    private final DataConvertor convertor = new Lc1DataConvertor();
    private byte[] bytes;

    public String getName() {
        return "LC-1";
    }

    public String getDescription() {
        return "Innovate LC-1 AFR data";
    }

    public String getUnits() {
        return "AFR";
    }

    public double getData() {
        if (bytes != null) {
            return convertor.convert(bytes);
        } else {
            return 0.0;
        }
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }
}
