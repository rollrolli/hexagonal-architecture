package com.dskym.topologyinventory.framework.adapter.input;

import com.dskym.topologyinventory.application.ports.input.RouterManagementInputPort;
import com.dskym.topologyinventory.application.usecases.RouterManagementUseCase;
import com.dskym.topologyinventory.domain.entity.CoreRouter;
import com.dskym.topologyinventory.domain.entity.Router;
import com.dskym.topologyinventory.domain.vo.*;
import com.dskym.topologyinventory.framework.adapter.output.h2.RouterManagementH2Adapter;

public class RouterManagementGenericAdapter {
    private RouterManagementUseCase routerManagementUseCase;

    public RouterManagementGenericAdapter() {
        setPorts();
    }

    private void setPorts() {
        this.routerManagementUseCase = new RouterManagementInputPort(RouterManagementH2Adapter.getInstance());
    }

    public Router retreiveRouter(Id id) {
        return routerManagementUseCase.retrieveRouter(id);
    }

    public Router removeRouter(Id id) {
        return routerManagementUseCase.retrieveRouter(id);
    }

    private Router createRouter(Vendor vendor, Model model, IP ip, Location location, RouterType routerType) {
        var router = routerManagementUseCase.createRouter(vendor, model, ip, location, routerType);
        return routerManagementUseCase.persistRouter(router);
    }

    public Router addRouterToCoreRouter(Id routerId, Id coreRouterId) {
        var router = routerManagementUseCase.retrieveRouter(routerId);
        var coreRouter = (CoreRouter) routerManagementUseCase.retrieveRouter(coreRouterId);
        return routerManagementUseCase.addRouterToCoreRouter(router, coreRouter);
    }

    public Router removeRouterFromCoreRouter(Id routerId, Id coreRouterId) {
        var router = routerManagementUseCase.retrieveRouter(routerId);
        var coreRouter = (CoreRouter) routerManagementUseCase.retrieveRouter(coreRouterId);
        return routerManagementUseCase.removeRouterFromCoreRouter(router, coreRouter);
    }
}
