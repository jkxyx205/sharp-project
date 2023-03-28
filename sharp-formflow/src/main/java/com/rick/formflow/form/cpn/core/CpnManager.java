package com.rick.formflow.form.cpn.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Rick
 * @createdAt 2021-11-02 21:32:00
 */
@Component
public class CpnManager {

    private static Map<CpnTypeEnum, Cpn> cpnMap;

    @Autowired
    public void setCpnList(Set<Cpn> cpnList) {
        if (CpnManager.cpnMap != null) {
            throw new IllegalArgumentException("cpnMap has been init");
        }
        CpnManager.cpnMap = cpnList.stream().collect(Collectors.toMap(cpn -> cpn.getCpnType(), v -> v));
    }

    public static Cpn getCpnByType(CpnTypeEnum cpnType) {
        return cpnMap.get(cpnType);
    }

}
