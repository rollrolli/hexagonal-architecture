package com.dskym.topologyinventory.domain.entity;

import com.dskym.topologyinventory.domain.specification.EmptyRouterSpec;
import com.dskym.topologyinventory.domain.specification.EmptySwitchSpec;
import com.dskym.topologyinventory.domain.specification.SameCountrySpec;
import com.dskym.topologyinventory.domain.specification.SameIpSpec;
import com.dskym.topologyinventory.domain.vo.*;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;

@Getter
@ToString
public class CoreRouter extends Router {

    private final Map<Id, Router> routers;

    @Builder
    public CoreRouter(Id id, Vendor vendor, Model model, IP ip, Location location, RouterType routerType, Map<Id, Router> routers) {
        super(id, vendor, model, ip, location, routerType);
        this.routers = routers;
    }

    public Router addRouter(Router anyRouter) {
        var sameCountryRouterSpec = new SameCountrySpec(this);
        var sameIpSpec = new SameIpSpec(this);

        sameCountryRouterSpec.check(anyRouter);
        sameIpSpec.check(anyRouter);

        return this.routers.put(anyRouter.id, anyRouter);
    }

    public Router removeRouter(Router anyRouter) {
        var emptyRoutersSpec = new EmptyRouterSpec();
        var emptySwitchSpec = new EmptySwitchSpec();

        switch (anyRouter.routerType) {
            case CORE:
                var coreRouter = (CoreRouter)anyRouter;
                emptyRoutersSpec.check(coreRouter);
                break;
            case EDGE:
                var edgeRouter = (EdgeRouter)anyRouter;
                emptySwitchSpec.check(edgeRouter);
        }
        return this.routers.remove(anyRouter.id);
    }
}
