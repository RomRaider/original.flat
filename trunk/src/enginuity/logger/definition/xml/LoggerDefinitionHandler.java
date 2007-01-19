/*
 *
 * Enginuity Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006 Enginuity.org
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 */

package enginuity.logger.definition.xml;

import enginuity.logger.comms.query.EcuInit;
import enginuity.logger.definition.EcuData;
import enginuity.logger.definition.EcuDataConvertor;
import enginuity.logger.definition.EcuDerivedParameterConvertor;
import enginuity.logger.definition.EcuDerivedParameterConvertorImpl;
import enginuity.logger.definition.EcuDerivedParameterImpl;
import enginuity.logger.definition.EcuParameter;
import enginuity.logger.definition.EcuParameterConvertorImpl;
import enginuity.logger.definition.EcuParameterImpl;
import enginuity.logger.definition.EcuSwitch;
import enginuity.logger.definition.EcuSwitchConvertorImpl;
import enginuity.logger.definition.EcuSwitchImpl;
import static enginuity.util.ParamChecker.checkNotNullOrEmpty;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class LoggerDefinitionHandler extends DefaultHandler {
    private static final String TAG_PROTOCOL = "protocol";
    private static final String TAG_PARAMETER = "parameter";
    private static final String TAG_ADDRESS = "address";
    private static final String TAG_DEPENDS = "depends";
    private static final String TAG_CONVERSION = "conversion";
    private static final String TAG_CONVERSIONS = "conversions";
    private static final String TAG_BYTE = "byte";
    private static final String TAG_REF = "ref";
    private static final String TAG_SWITCH = "switch";
    private static final String TAG_ECUPARAM = "ecuparam";
    private static final String TAG_ECU = "ecu";
    private static final String ATTR_ID = "id";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_DESC = "desc";
    private static final String ATTR_ECUBYTEINDEX = "ecubyteindex";
    private static final String ATTR_ECUBIT = "ecubit";
    private static final String ATTR_UNITS = "units";
    private static final String ATTR_EXPRESSION = "expr";
    private static final String ATTR_FORMAT = "format";
    private static final String ATTR_BYTE = "byte";
    private static final String ATTR_BIT = "bit";
    private static final String ATTR_PARAMETER = "parameter";
    private final String protocol;
    private final String fileLoggingControllerSwitchId;
    private final EcuInit ecuInit;
    private List<EcuParameter> params;
    private List<EcuSwitch> switches;
    private EcuSwitch fileLoggingControllerSwitch;
    private Map<String, EcuData> ecuDataMap;
    private String id;
    private String name;
    private String desc;
    private String ecuByteIndex;
    private String ecuBit;
    private String ecuIds;
    private Set<String> addressList;
    private Set<String> dependsList;
    private Map<String, String[]> ecuAddressMap;
    private boolean derived;
    private int bit;
    private Set<EcuDataConvertor> convertorList;
    private Set<EcuDerivedParameterConvertor> derivedConvertorList;
    private StringBuilder charBuffer;
    private boolean parseProtocol;

    public LoggerDefinitionHandler(String protocol, String fileLoggingControllerSwitchId, EcuInit ecuInit) {
        checkNotNullOrEmpty(protocol, "protocol");
        checkNotNullOrEmpty(fileLoggingControllerSwitchId, "fileLoggingControllerSwitchId");
        this.protocol = protocol;
        this.fileLoggingControllerSwitchId = fileLoggingControllerSwitchId;
        this.ecuInit = ecuInit;
    }

    public void startDocument() {
        params = new ArrayList<EcuParameter>();
        switches = new ArrayList<EcuSwitch>();
        ecuDataMap = new HashMap<String, EcuData>();
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        if (TAG_PROTOCOL.equals(qName)) {
            parseProtocol = protocol.equalsIgnoreCase(attributes.getValue(ATTR_ID));
        } else if (parseProtocol) {
            if (TAG_PARAMETER.equals(qName)) {
                id = attributes.getValue(ATTR_ID);
                name = attributes.getValue(ATTR_NAME);
                desc = attributes.getValue(ATTR_DESC);
                ecuByteIndex = attributes.getValue(ATTR_ECUBYTEINDEX);
                ecuBit = attributes.getValue(ATTR_ECUBIT);
            } else if (TAG_ADDRESS.equals(qName)) {
                addressList = new LinkedHashSet<String>();
                ecuAddressMap = new HashMap<String, String[]>();
                derived = false;
            } else if (TAG_DEPENDS.equals(qName)) {
                dependsList = new LinkedHashSet<String>();
                derived = true;
            } else if (TAG_REF.equals(qName)) {
                dependsList.add(attributes.getValue(ATTR_PARAMETER));
            } else if (TAG_CONVERSIONS.equals(qName)) {
                convertorList = new LinkedHashSet<EcuDataConvertor>();
                derivedConvertorList = new LinkedHashSet<EcuDerivedParameterConvertor>();
            } else if (TAG_CONVERSION.equals(qName)) {
                if (derived) {
                    derivedConvertorList.add(new EcuDerivedParameterConvertorImpl(attributes.getValue(ATTR_UNITS),
                            attributes.getValue(ATTR_EXPRESSION),
                            attributes.getValue(ATTR_FORMAT)));
                } else {
                    convertorList.add(new EcuParameterConvertorImpl(attributes.getValue(ATTR_UNITS),
                            attributes.getValue(ATTR_EXPRESSION),
                            attributes.getValue(ATTR_FORMAT)));
                }
            } else if (TAG_SWITCH.equals(qName)) {
                id = attributes.getValue(ATTR_ID);
                name = attributes.getValue(ATTR_NAME);
                desc = attributes.getValue(ATTR_DESC);
                addressList = new HashSet<String>();
                addressList.add(attributes.getValue(ATTR_BYTE));
                bit = Integer.valueOf(attributes.getValue(ATTR_BIT));
            } else if (TAG_ECUPARAM.equals(qName)) {
                id = attributes.getValue(ATTR_ID);
                name = attributes.getValue(ATTR_NAME);
                desc = attributes.getValue(ATTR_DESC);
            } else if (TAG_ECU.equals(qName)) {
                ecuIds = attributes.getValue(ATTR_ID);
                addressList = new LinkedHashSet<String>();
            }
        }
        charBuffer = new StringBuilder();
    }

    public void characters(char[] ch, int start, int length) {
        if (parseProtocol) {
            charBuffer.append(ch, start, length);
        }
    }

    public void endElement(String uri, String localName, String qName) {
        if (TAG_PROTOCOL.equals(qName)) {
            parseProtocol = false;
        } else if (parseProtocol) {
            if (TAG_BYTE.equals(qName)) {
                addressList.add(charBuffer.toString());
            } else if (TAG_PARAMETER.equals(qName)) {
                if (derived) {
                    Set<EcuData> dependencies = new HashSet<EcuData>();
                    for (String refid : dependsList) {
                        if (ecuDataMap.containsKey(refid)) {
                            dependencies.add(ecuDataMap.get(refid));
                        }
                    }
                    if (dependsList.size() == dependencies.size()) {
                        EcuParameter param = new EcuDerivedParameterImpl(id, name, desc,
                                dependencies.toArray(new EcuData[dependencies.size()]),
                                derivedConvertorList.toArray(
                                        new EcuDerivedParameterConvertor[derivedConvertorList.size()]));
                        params.add(param);
                        ecuDataMap.put(param.getId(), param);
                    }
                } else {
                    if (ecuByteIndex == null || ecuBit == null || ecuInit == null || isSupportedParameter(ecuInit,
                            ecuByteIndex, ecuBit)) {
                        EcuParameter param = new EcuParameterImpl(id, name, desc, toArray(addressList),
                                convertorList.toArray(new EcuDataConvertor[convertorList.size()]));
                        params.add(param);
                        ecuDataMap.put(param.getId(), param);
                    }
                }
            } else if (TAG_SWITCH.equals(qName)) {
                EcuSwitch ecuSwitch = new EcuSwitchImpl(id, name, desc, toArray(addressList),
                        new EcuDataConvertor[]{new EcuSwitchConvertorImpl(bit)});
                switches.add(ecuSwitch);
                ecuDataMap.put(ecuSwitch.getId(), ecuSwitch);
                if (id.equalsIgnoreCase(fileLoggingControllerSwitchId)) {
                    fileLoggingControllerSwitch = new EcuSwitchImpl(id, name, desc, toArray(addressList),
                            new EcuDataConvertor[]{new EcuSwitchConvertorImpl(bit)});
                }
            } else if (TAG_ECUPARAM.equals(qName)) {
                if (ecuInit != null && ecuAddressMap.containsKey(ecuInit.getEcuId())) {
                    EcuParameter param = new EcuParameterImpl(id, name, desc, ecuAddressMap.get(ecuInit.getEcuId()),
                            convertorList.toArray(new EcuDataConvertor[convertorList.size()]));
                    params.add(param);
                    ecuDataMap.put(param.getId(), param);
                }
            } else if (TAG_ECU.equals(qName)) {
                String[] addresses = toArray(addressList);
                for (String ecuId : ecuIds.split(",")) {
                    ecuAddressMap.put(ecuId, addresses);
                }
            }
        }
    }

    private boolean isSupportedParameter(final EcuInit ecuInit, final String ecuByteIndex, final String ecuBit) {
        byte[] bytes = new byte[1];
        System.arraycopy(ecuInit.getEcuInitBytes(), Integer.parseInt(ecuByteIndex), bytes, 0, 1);
        return (bytes[0] & 1 << Integer.parseInt(ecuBit)) > 0;
    }

    public List<EcuParameter> getEcuParameters() {
        return params;
    }

    public List<EcuSwitch> getEcuSwitches() {
        return switches;
    }

    public EcuSwitch getFileLoggingControllerSwitch() {
        return fileLoggingControllerSwitch;
    }

    private String[] toArray(Set<String> set) {
        String[] addresses = new String[set.size()];
        set.toArray(addresses);
        return addresses;
    }
}
