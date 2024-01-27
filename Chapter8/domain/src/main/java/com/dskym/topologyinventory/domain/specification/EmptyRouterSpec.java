package com.dskym.topologyinventory.domain.specification;

import com.dskym.topologyinventory.domain.entity.CoreRouter;
import com.dskym.topologyinventory.domain.exception.GenericSpecificationException;
import com.dskym.topologyinventory.domain.specification.shared.AbstractSpecification;

public class EmptyRouterSpec extends AbstractSpecification<CoreRouter> {
    @Override
    public boolean isSatisfiedBy(CoreRouter coreRouter) {
        return coreRouter.getRouters()==null||
                coreRouter.getRouters().isEmpty();
    }

    @Override
    public void check(CoreRouter coreRouter) {
        if(!isSatisfiedBy(coreRouter))
            throw new GenericSpecificationException("It isn't allowed to remove a core router with other routers attached to it");
    }
}
