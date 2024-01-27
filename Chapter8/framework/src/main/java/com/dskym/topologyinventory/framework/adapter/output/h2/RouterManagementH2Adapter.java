package com.dskym.topologyinventory.framework.adapter.output.h2;

import com.dskym.topologyinventory.application.ports.output.RouterManagementOutputPort;
import com.dskym.topologyinventory.domain.entity.Router;
import com.dskym.topologyinventory.domain.vo.Id;

public class RouterManagementH2Adapter implements RouterManagementOutputPort {
    private static RouterManagementH2Adapter instance;

    @PersistenceContext
    private EntityManager em;

    private RouterManagementH2Adapter() {
        setUpH2Database();
    }

    @Override
    public Router retrieveRouter(Id id) {
        var routerData = em.getReference(RouterData.class, id.getUuid());
        return RouterH2Mapper.routerDataToDomain(routerData);
    }

    @Override
    public Router removeRouter(Id id) {
        var routerData = em.getReference(RouterData.class, id.getUuid());
        em.remove(routerData);
        return null;
    }

    @Override
    public Router persistRouter(Router router) {
        var routerData = RouterH2Mapper.routerDomainToData(router);
        em.persist(routerData);
        return router;
    }
}
