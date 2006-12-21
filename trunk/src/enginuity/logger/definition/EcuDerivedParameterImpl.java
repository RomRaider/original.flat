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

package enginuity.logger.definition;

import static enginuity.logger.definition.EcuDataType.PARAMETER;
import static enginuity.util.ParamChecker.checkNotNull;
import static enginuity.util.ParamChecker.checkNotNullOrEmpty;

import java.util.HashSet;
import java.util.Set;

public final class EcuDerivedParameterImpl implements EcuParameter {
    private final String id;
    private final String name;
    private final String description;
    private final EcuDerivedParameterConvertor[] convertors;
    private final String[] addresses;
    private final Set<ConvertorUpdateListener> listeners = new HashSet<ConvertorUpdateListener>();
    private int selectedConvertorIndex = 0;

    public EcuDerivedParameterImpl(String id, String name, String description, EcuData[] ecuDatas, EcuDerivedParameterConvertor[] convertors) {
        checkNotNullOrEmpty(name, "id");
        checkNotNullOrEmpty(name, "name");
        checkNotNull(description, "description");
        checkNotNullOrEmpty(ecuDatas, "ecuDatas");
        checkNotNullOrEmpty(convertors, "convertors");
        this.id = id;
        this.name = name;
        this.description = description;
        this.convertors = convertors;
        addresses = setAddresses(ecuDatas);
        setEcuDatas(ecuDatas);
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

    public EcuDataConvertor getSelectedConvertor() {
        return convertors[selectedConvertorIndex];
    }

    public EcuDataConvertor[] getConvertors() {
        return convertors;
    }

    public void selectConvertor(EcuDataConvertor convertor) {
        if (convertor != getSelectedConvertor()) {
            for (int i = 0; i < convertors.length; i++) {
                EcuDerivedParameterConvertor parameterConvertor = convertors[i];
                if (convertor == parameterConvertor) {
                    selectedConvertorIndex = i;
                }
            }
            notifyUpdateListeners();
        }
    }

    public EcuDataType getDataType() {
        return PARAMETER;
    }

    public void addConvertorUpdateListener(ConvertorUpdateListener listener) {
        checkNotNull(listener, "listener");
        listeners.add(listener);
    }

    private String[] setAddresses(EcuData[] ecuDatas) {
        String[] addresses = new String[0];
        for (EcuData ecuData : ecuDatas) {
            String[] newAddresses = ecuData.getAddresses();
            String[] tmp = new String[addresses.length + newAddresses.length];
            System.arraycopy(addresses, 0, tmp, 0, addresses.length);
            System.arraycopy(newAddresses, 0, tmp, addresses.length, newAddresses.length);
            addresses = tmp;
        }
        return addresses;
    }

    private void setEcuDatas(EcuData[] ecuDatas) {
        for (EcuDerivedParameterConvertor convertor : convertors) {
            convertor.setEcuDatas(ecuDatas);
        }
    }

    private void notifyUpdateListeners() {
        for (ConvertorUpdateListener listener : listeners) {
            listener.notifyConvertorUpdate(this);
        }
    }
}
