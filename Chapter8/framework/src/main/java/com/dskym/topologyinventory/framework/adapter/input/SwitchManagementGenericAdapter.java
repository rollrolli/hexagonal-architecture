package com.dskym.topologyinventory.framework.adapter.input;

import com.dskym.topologyinventory.application.ports.input.RouterManagementInputPort;
import com.dskym.topologyinventory.application.ports.input.SwitchManagementInputPort;
import com.dskym.topologyinventory.application.usecases.RouterManagementUseCase;
import com.dskym.topologyinventory.application.usecases.SwitchManagementUseCase;
import com.dskym.topologyinventory.domain.entity.EdgeRouter;
import com.dskym.topologyinventory.domain.entity.Router;
import com.dskym.topologyinventory.domain.entity.Switch;
import com.dskym.topologyinventory.domain.vo.*;
import com.dskym.topologyinventory.framework.adapter.output.h2.SwitchManagementH2Adapter;

public class SwitchManagementGenericAdapter {
    private SwitchManagementUseCase switchManagementUseCase;
    private RouterManagementUseCase routerManagementUseCase;

    public SwitchManagementGenericAdapter() {
        setPorts();
    }

    private void setPorts() {
        this.switchManagementUseCase = new SwitchManagementInputPort(SwitchManagementH2Adapter.getInstance());
        this.routerManagementUseCase = new RouterManagementInputPort(RouterManagementH2Adapter.getInstance());
    }

    public Switch retrieveSwitch(Id switchId) {
        return switchManagementUseCase.retrieveSwitch(switchId);
    }

    public EdgeRouter createAndSwitchToEdgeRouter(Vendor vendor, Model model, IP ip, Location location, SwitchType switchType, Id routerId) {
        Switch newSwitch = switchManagementUseCase.createSwitch(vendor, model, ip, location, switchType);

        Router edgeRouter = routerManagementUseCase.retrieveRouter(routerId);

        if(!edgeRouter.getRouterType().equals(RouterType.EDGE)) {
            throw new UnsupportedOperationException("Please inform the id of an edge router to add a switch");
        }

        Router router = switchManagementUseCase.addSwitchToEdgeRouter(newSwitch, (EdgeRouter) edgeRouter);

        return (EdgeRouter) routerManagementUseCase.persistRouter(router);
    }

    public EdgeRouter removeSwitchFromEdgeRouter(Id switchId, Id edgeRouterId) {
        EdgeRouter edgeRouter = (EdgeRouter) routerManagementUseCase.retrieveRouter(edgeRouterId);
        Switch networkSwitch = edgeRouter.getSwitches().get(switchId);
        Router router = switchManagementUseCase.removeSwitchFromEdgeRouter(networkSwitch, edgeRouter);
        return (EdgeRouter) routerManagementUseCase.persistRouter(router);
    }
}
