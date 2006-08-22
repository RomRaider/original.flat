package enginuity.logger.ui;

import enginuity.logger.definition.EcuData;
import static enginuity.util.ParamChecker.checkNotNull;

public final class LoggerDataRow {
    private final EcuData ecuData;
    private double minValue;
    private double maxValue;
    private double currentValue;
    private boolean updated = false;

    public LoggerDataRow(EcuData ecuData) {
        checkNotNull(ecuData, "ecuData");
        this.ecuData = ecuData;
    }

    public EcuData getEcuData() {
        return ecuData;
    }

    public String getName() {
        return ecuData.getName();
    }

    public String getMinValue() {
        return ecuData.getConvertor().format(minValue);
    }

    public String getMaxValue() {
        return ecuData.getConvertor().format(maxValue);
    }

    public String getCurrentValue() {
        return ecuData.getConvertor().format(currentValue);
    }

    public String getUnits() {
        return ecuData.getConvertor().getUnits();
    }

    public void updateValue(byte[] bytes) {
        currentValue = ecuData.getConvertor().convert(bytes);
        if (currentValue < minValue || !updated) {
            minValue = currentValue;
        }
        if (currentValue > maxValue || !updated) {
            maxValue = currentValue;
        }
        updated = true;
    }
}