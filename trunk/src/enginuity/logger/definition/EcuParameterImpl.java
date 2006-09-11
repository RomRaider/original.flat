package enginuity.logger.definition;

import static enginuity.logger.definition.EcuDataType.PARAMETER;
import static enginuity.util.ParamChecker.checkNotNull;
import static enginuity.util.ParamChecker.checkNotNullOrEmpty;

public final class EcuParameterImpl implements EcuParameter {
    private final String id;
    private final String name;
    private final String description;
    private final String[] addresses;
    private final EcuDataConvertor convertor;

    public EcuParameterImpl(String id, String name, String description, String[] address, EcuDataConvertor convertor) {
        checkNotNullOrEmpty(name, "id");
        checkNotNullOrEmpty(name, "name");
        checkNotNull(description, "description");
        checkNotNullOrEmpty(address, "addresses");
        checkNotNull(convertor, "convertor");
        this.id = id;
        this.name = name;
        this.description = description;
        this.addresses = address;
        this.convertor = convertor;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String[] getAddresses() {
        return addresses;
    }

    public EcuDataConvertor getConvertor() {
        return convertor;
    }

    public EcuDataType getDataType() {
        return PARAMETER;
    }
}
