package com.dskym.topologyinventory.domain.entity;

import com.dskym.topologyinventory.domain.specification.CIDRSpecification;
import com.dskym.topologyinventory.domain.specification.NetworkAmountSpec;
import com.dskym.topologyinventory.domain.specification.NetworkAvailabilitySpec;
import com.dskym.topologyinventory.domain.vo.*;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.function.Predicate;

@Getter
public class Switch extends Equipment {
    private final SwitchType switchType;
    private final List<Network> switchNetworks;

    @Builder
    public Switch(Id id, Vendor vendor, Model model, IP ip, Location location, SwitchType switchType, List<Network> switchNetworks){
        super(id, vendor, model, ip, location);
        this.switchType = switchType;
        this.switchNetworks = switchNetworks;
    }

    public static Predicate<Network> getNetworkProtocolPredicate(Protocol protocol){
        return s -> s.getNetworkAddress().getProtocol().equals(protocol);
    }

    public static Predicate<Switch> getSwitchTypePredicate(SwitchType switchType){
        return s -> s.switchType .equals(switchType);
    }

    public boolean addNetworkToSwitch(Network network) {
        var availabilitySpec = new NetworkAvailabilitySpec(network);
        var cidrSpec = new CIDRSpecification();
        var amountSpec = new NetworkAmountSpec();

        cidrSpec.check(network.getNetworkCidr());
        availabilitySpec.check(this);
        amountSpec.check(this);

        return this.switchNetworks.add(network);
    }

    public boolean removeNetworkFromSwitch(Network network){
        return this.switchNetworks.remove(network);
    }
}
