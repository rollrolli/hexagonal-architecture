module application {
    exports com.dskym.topologyinventory.application.ports.output;
    exports com.dskym.topologyinventory.application.usecases;
    exports com.dskym.topologyinventory.application.ports.input;
    requires domain;
    requires static lombok;
}