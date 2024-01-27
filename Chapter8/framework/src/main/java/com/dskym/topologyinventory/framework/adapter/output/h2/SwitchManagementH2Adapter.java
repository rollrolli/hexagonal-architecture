package com.dskym.topologyinventory.framework.adapter.output.h2;

import com.dskym.topologyinventory.application.ports.output.SwitchManagementOutputPort;
import com.dskym.topologyinventory.domain.entity.Switch;
import com.dskym.topologyinventory.domain.vo.Id;

public class SwitchManagementH2Adapter implements SwitchManagementOutputPort {
    @Override
    public Switch retrieveSwitch(Id id) {
        var switchData = em.getReference(SwitchData.class, id.getUuid());
        return RouterH2Mapper.switchDataToDomain(switchData);
    }
}
