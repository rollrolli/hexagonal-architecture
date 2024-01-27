package com.dskym.topologyinventory.domain.specification;

import com.dskym.topologyinventory.domain.entity.CoreRouter;
import com.dskym.topologyinventory.domain.entity.Equipment;
import com.dskym.topologyinventory.domain.exception.GenericSpecificationException;
import com.dskym.topologyinventory.domain.specification.shared.AbstractSpecification;

public class SameCountrySpec extends AbstractSpecification<Equipment> {
    private final Equipment equipment;

    public SameCountrySpec(Equipment equipment) {
        this.equipment = equipment;
    }

    @Override
    public boolean isSatisfiedBy(Equipment anyEquipment) {
        if(anyEquipment instanceof CoreRouter) {
            return true;
        } else if (anyEquipment != null && this.equipment != null) {
            return this.equipment.getLocation().getCountry().
                    equals(anyEquipment.getLocation().getCountry());
        } else{
            return false;
        }
    }

    @Override
    public void check(Equipment equipment) throws GenericSpecificationException {
        if(!isSatisfiedBy(equipment))
            throw new GenericSpecificationException("The equipments should be in the same country");
    }
}
