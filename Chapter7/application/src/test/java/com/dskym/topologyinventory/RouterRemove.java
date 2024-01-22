package com.dskym.topologyinventory;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

public class RouterRemove {
    @Given("The core router has at least one edge router connected to it")
    public void the_core_router_has_at_least_one_edge_router_connected_to_it(){
        var predicate = Router.getRouterTypePredicate(EDGE);
        edgeRouter = (EdgeRouter) this.coreRouter.getRouters().entrySet().stream().map(routerMap -> routerMap.getValue()).filter(predicate).findFirst().get();
        assertEquals(EDGE, edgeRouter.getRouterType());
    }

    @And("The switch has no networks attached to it")
    public void the_switch_has_no_networks_attached_to_it(){
        var networksSize = networkSwitch.getSwitchNetworks().size();
        assertEquals(1, networksSize);
        networkSwitch.removeNetworkFromSwitch(network);
        networksSize = networkSwitch.getSwitchNetworks().size();
        assertEquals(0, networksSize);
    }

    @And("The edge router has no switches attached to it")
    public void the_edge_router_has_no_switches_attached_to_it(){
        var switchesSize = edgeRouter.getSwitches().size();
        assertEquals(1, switchesSize);
        edgeRouter.removeSwitch(networkSwitch);
        switchesSize = edgeRouter.getSwitches().size();
        assertEquals(0, switchesSize);
    }

    @Then("I remove the edge router from the core router")
    public void edge_router_is_removed_from_core_router(){
        var actualID = edgeRouter.getId();
        var expectedID = this.routerManagementUseCase.removeRouterFromCoreRouter(edgeRouter, coreRouter).getId();
        assertEquals(expectedID, actualID);
    }
}
