package com.dskym.topologyinventory.application.ports.output;

import com.dskym.topologyinventory.domain.entity.Router;
import com.dskym.topologyinventory.domain.vo.Id;

public interface RouterManagementOutputPort {
    Router retrieveRouter(Id id);

    Router removeRouter(Id id);

    Router persistRouter(Router router);
}
