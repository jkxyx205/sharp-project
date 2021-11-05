package com.rick.formflow.form.cpn.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Rick
 * @createdAt 2021-11-02 21:32:00
 */
@Getter
@Component
@RequiredArgsConstructor
public class CpnManager implements InitializingBean {

    private final Set<Cpn> cpnList;

    private Map<CpnTypeEnum, Cpn> cpnMap;

    public Cpn getCpnByType(CpnTypeEnum cpnType) {
        return cpnMap.get(cpnType);
    }

    @Override
    public void afterPropertiesSet() {
        cpnMap = cpnList.stream().collect(Collectors.toMap(Cpn::getCpnType, v -> v));
    }
}
