package com.dskym.topologyinventory.application.ports.input;

import com.dskym.topologyinventory.application.usecases.NetworkManagementUseCase;
import com.dskym.topologyinventory.domain.entity.Switch;
import com.dskym.topologyinventory.domain.vo.IP;
import com.dskym.topologyinventory.domain.vo.Network;

public class NetworkManagementInputPort implements NetworkManagementUseCase {
    @Override
    public Network createNetwork(IP networkAddress, String networkName, int networkCidr) {
        return Network.builder().networkAddress(networkAddress).networkName(networkName).networkCidr(networkCidr).build();
    }

    @Override
    public Switch addNetworkToSwitch(Network network, Switch networkSwitch) {
        networkSwitch.addNetworkToSwitch(network);
        return networkSwitch;
    }

    @Override
    public Switch removeNetworkFromSwitch(Network network, Switch networkSwitch) {
        networkSwitch.removeNetworkFromSwitch(network);
        return networkSwitch;
    }
}
