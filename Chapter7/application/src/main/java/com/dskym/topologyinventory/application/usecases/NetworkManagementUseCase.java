package com.dskym.topologyinventory.application.usecases;

import com.dskym.topologyinventory.domain.entity.Switch;
import com.dskym.topologyinventory.domain.vo.IP;
import com.dskym.topologyinventory.domain.vo.Network;

public interface NetworkManagementUseCase {
    Network createNetwork(IP networkAddress, String networkName, int networkCidr);
    Switch addNetworkToSwitch(Network network, Switch networkSwitch);
    Switch removeNetworkFromSwitch(Network network, Switch networkSwitch);
}
