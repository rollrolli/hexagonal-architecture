package com.dskym.topologyinventory.application.ports.output;

import com.dskym.topologyinventory.domain.entity.Switch;
import com.dskym.topologyinventory.domain.vo.Id;

public interface SwitchManagementOutputPort {
    Switch retrieveSwitch(Id id);
}
