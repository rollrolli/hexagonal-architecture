package com.dskym.topologyinventory.application.ports.input;

import com.dskym.topologyinventory.application.usecases.SwitchManagementUseCase;
import com.dskym.topologyinventory.domain.entity.EdgeRouter;
import com.dskym.topologyinventory.domain.entity.Switch;
import com.dskym.topologyinventory.domain.vo.*;

public class SwitchManagementInputPort implements SwitchManagementUseCase {
    @Override
    public Switch createSwitch(Vendor vendor, Model model, IP ip, Location location, SwitchType switchType) {
        return Switch.builder().id(Id.withoutId()).vendor(vendor).model(model).ip(ip).location(location).switchType(switchType).build();
    }

    @Override
    public EdgeRouter addSwitchToEdgeRouter(Switch networkSwitch, EdgeRouter edgeRouter) {
        edgeRouter.addSwitch(networkSwitch);
        return edgeRouter;
    }

    @Override
    public EdgeRouter removeSwitchFromEdgeRouter(Switch networkSwitch, EdgeRouter edgeRouter) {
        edgeRouter.removeSwitch(networkSwitch);
        return edgeRouter;
    }
}
