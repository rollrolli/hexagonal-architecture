package com.dskym.topologyinventory.framework;

import com.dskym.topologyinventory.domain.vo.*;
import com.dskym.topologyinventory.framework.adapter.input.RouterManagementGenericAdapter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class RouterTest extends FrameworkTestData {
    RouterManagementGenericAdapter routerManagementGenericAdapter;

    public RouterTest() {
        this.routerManagementGenericAdapter = new RouterManagementGenericAdapter();
        loadData();
    }

    @Test
    public void retrieveRouter() {
        var id = Id.withId("~~~");
        var actualId = routerManagementGenericAdapter.retreiveRouter(id).getId();
        assertEquals(id, actualId);
    }

    @Test
    public void createRouter() {
        var ipAddress = "40.0.0.1";
        var routerId = this.routerManagementGenericAdapter.createRouter(Vendor.DLINK, Model.XYZ0001, IP.fromAddress(ipAddress), locationA, RouterType.EDGE).getId();
        var router = this.routerManagementGenericAdapter.removeRouter(routerId);

        assertEquals(routerId, router.getId());
        assertEquals(Vendor.DLINK, router.getVendor());
        assertEquals(Model.XYZ0001, router.getModel());
        assertEquals(ipAddress, router.getIp().getIpAddress());
        assertEquals(locationA, router.getLocation());
        assertEquals(RouterType.EDGE, router.getRouterType());
    }

    @Test
    public void addRouterToCoreRouter() {
        var routerId = Id.withId("~~~");
        var coreRouterId = Id.withId("~~~~");
        var actualRouter = this.routerManagementGenericAdapter.addRouterToCoreRouter(routerId, coreRouterId);
        assertEquals(routerId, actualRouter.getRouters().get(routerId).getId());
    }

    @Test
    public void removeRouterFromCoreRouter() {
        var routerId = Id.withId("~~~");
        var coreRouterId = Id.withId("~~~~");
        var removedRouter = this.routerManagementGenericAdapter.removeRouterFromCoreRouter(routerId, coreRouterId);
        var coreRouter = this.routerManagementGenericAdapter.removeRouter(coreRouterId);

        assertEquals(routerId, removedRouter.getId());
        assertFalse(coreRouter.getRouters().containsKey(routerId));
    }
}
