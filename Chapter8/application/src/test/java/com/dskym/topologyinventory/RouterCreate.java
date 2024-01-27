package com.dskym.topologyinventory;

import com.dskym.topologyinventory.domain.entity.CoreRouter;
import com.dskym.topologyinventory.domain.entity.EdgeRouter;
import com.dskym.topologyinventory.domain.vo.IP;
import com.dskym.topologyinventory.domain.vo.Model;
import com.dskym.topologyinventory.domain.vo.Vendor;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

public class RouterCreate {
    @Given("I provide all required data to create a core router")
    public void create_core_router() {
        router = this.routerManagementUseCase.createRouter(Vendor.CISCO, Model.XYZ0001, IP.fromAddress("20.0.0.1"), locationA, CORE);
    }

    @Then("A new core router is created")
    public void a_new_core_router_is_created() {
        assertNotNull(router);
        assertEquals(CORE, router.getRouterType());
    }

    @Given("I provide all required data to create a edge router")
    public void create_edge_router() {
        router = this.routerManagementUseCase.createRouter(Vendor.CISCO, Model.XYZ0001, IP.fromAddress("20.0.0.1"), locationA, EDGE);
    }

    @Then("A new edge router is created")
    public void a_new_edge_router_is_created() {
        assertNotNull(router);
        assertEquals(EDGE, router.getRouterType());
    }
}
