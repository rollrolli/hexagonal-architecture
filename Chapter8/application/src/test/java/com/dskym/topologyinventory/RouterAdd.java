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

public class RouterAdd {
    @Given("I have an edge router")
    public void assert_edge_router_exists() {
        edgeRouter = (EdgeRouter)this.routerManagementUseCase.createRouter(Vendor.HP, Model.XYZ0004, IP.fromAddress("20.0.0.1"), locationA, EDGE);
        assertNotNull(edgeRouter);
    }

    @And("I have a core router")
    public void assert_core_router_exists() {
        coreRouter = (CoreRouter)this.routerManagementUseCase.createRouter(Vendor.CISCO, Model.XYZ0001, IP.fromAddress("30.0.0.1"), locationA, CORE);
        assertNotNull(coreRouter);
    }

    @Then("I add an edge router to a core router")
    public void add_edge_to_core_router() {
        var actualEdgeId = edgeRouter.getId();
        var routerWithEdge = (CoreRouter)this.routerManagementUseCase.addRouterToCoreRouter(edgeRouter, coreRouter);
        var expectedEdgeId = routerWithEdge.getRouters().get(actualEdgeId).getId();
        assertEquals(actualEdgeId, expectedEdgeId);
    }
}
