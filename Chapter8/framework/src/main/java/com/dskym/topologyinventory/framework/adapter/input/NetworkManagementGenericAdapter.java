package com.dskym.topologyinventory.framework.adapter.input;

import com.dskym.topologyinventory.application.ports.input.NetworkManagementInputPort;
import com.dskym.topologyinventory.application.ports.input.SwitchManagementInputPort;
import com.dskym.topologyinventory.application.usecases.NetworkManagementUseCase;
import com.dskym.topologyinventory.application.usecases.SwitchManagementUseCase;
import com.dskym.topologyinventory.domain.entity.Switch;
import com.dskym.topologyinventory.domain.vo.Id;
import com.dskym.topologyinventory.domain.vo.Network;
import com.dskym.topologyinventory.framework.adapter.output.h2.RouterManagementH2Adapter;

public class NetworkManagementGenericAdapter {
    private SwitchManagementUseCase switchManagementUseCase;
    private NetworkManagementUseCase networkManagementUseCase;

    public NetworkManagementGenericAdapter() {
        setPorts();
    }

    private void setPorts() {
        this.switchManagementUseCase = new SwitchManagementInputPort(RouterManagementH2Adapter.getInstance());
        this.networkManagementUseCase = new NetworkManagementInputPort(RouterManagementH2Adapter.getInstance());
    }

    public Switch addNewtorkToSwitch(Network network, Id switchId) {
        Switch networkSwitch = switchManagementUseCase.retrieveSwitch(switchId);
        return networkManagementUseCase.addNetworkToSwitch(network, networkSwitch);
    }

    public Switch removeNetworkFromSwitch(String newtorkName, Id switchId) {
        Switch networkSwitch = switchManagementUseCase.retrieveSwitch(switchId);
        return networkManagementUseCase.removeNetworkFromSwitch(newtorkName, networkSwitch);
    }
}
