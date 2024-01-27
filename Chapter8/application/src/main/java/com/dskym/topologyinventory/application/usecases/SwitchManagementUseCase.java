package com.dskym.topologyinventory.application.usecases;

import com.dskym.topologyinventory.domain.entity.EdgeRouter;
import com.dskym.topologyinventory.domain.entity.Switch;
import com.dskym.topologyinventory.domain.vo.*;

public interface SwitchManagementUseCase {
    Switch createSwitch(Vendor vendor, Model model, IP ip, Location location, SwitchType switchType);
    EdgeRouter addSwitchToEdgeRouter(Switch networkSwitch, EdgeRouter edgeRouter);
    EdgeRouter removeSwitchFromEdgeRouter(Switch networkSwitch, EdgeRouter edgeRouter);
}
