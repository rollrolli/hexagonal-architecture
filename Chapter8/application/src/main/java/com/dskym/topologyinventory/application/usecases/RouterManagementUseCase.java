package com.dskym.topologyinventory.application.usecases;

import com.dskym.topologyinventory.domain.entity.CoreRouter;
import com.dskym.topologyinventory.domain.entity.Router;
import com.dskym.topologyinventory.domain.vo.*;

public interface RouterManagementUseCase {
    Router createRouter(Vendor vendor, Model model, IP ip, Location location, RouterType routerType);
    CoreRouter addRouterToCoreRouter(Router router, CoreRouter coreRouter);
    Router removeRouterFromCoreRouter(Router router, CoreRouter coreRouter);

    Router retrieveRouter(Id id);
    Router persistRouter(Router router);
}
