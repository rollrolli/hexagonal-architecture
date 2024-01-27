# 8. 프레임워크 헥사곤 만들기

- 프레임워크 헥사곤은 시스템의 기능을 노출하고 이러한 기능을 활성화하는 데 사용한느 기술을 정의하기 위해 어댑터를 활용한다.
    - 입력 포트에 입력 어댑터를 연결해 어플리케이션 기능을 노출한다.
    - 외부 시스템에서 데이터를 가져오거나 유지해야 하는 경우 출력 어댑터를 출력 포트에 연결한다.

## 프레임워크 헥사곤 부트스트래핑

- 헥사고날 방식은 헥사고날 시스템의 내부, 혹은 외부의 기반 기술에 대한 결정을 연기할 수 있다.
    - 시스템의 일부 기능을 REST를 통해 접근할 수 있게 노출시키고 싶다면 REST 입력 어댑터를 만들어 입력 포트에 연결하면 된다.
    - gRPC를 사용해 노출하고자 한다면 gRPC 입력 어댑터를 만들어 같은 입력 포트에 연결하면 된다.
    - 외부 데이터 소스를 다룰 때도 같은 출력 포트에 다양한 출력 어댑터를 연결함으로써 기본 데이터 소스 기술을 변경할 수 있다.

## 출력 어댑터 구현

```java
public interface RouterManagementOutputPort {
    Router retrieveRouter(Id id);
    Router removeRouter(Id id);
    Router persistRouter(Router router);
}
```

```java
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
```

```java
public interface SwitchManagementOutputPort {
    Switch retrieveSwitch(Id id);
}
```

```java
public class SwitchManagementH2Adapter implements SwitchManagementOutputPort {
    @Override
    public Switch retrieveSwitch(Id id) {
        var switchData = em.getReference(SwitchData.class, id.getUuid());
        return RouterH2Mapper.switchDataToDomain(switchData);
    }
}
```

## 입력 어댑터 구현

```java
public class RouterManagementGenericAdapter {
    private RouterManagementUseCase routerManagementUseCase;

    public RouterManagementGenericAdapter() {
        setPorts();
    }

    private void setPorts() {
        this.routerManagementUseCase = new RouterManagementInputPort(RouterManagementH2Adapter.getInstance());
    }

    public Router retreiveRouter(Id id) {
        return routerManagementUseCase.retrieveRouter(id);
    }

    public Router removeRouter(Id id) {
        return routerManagementUseCase.retrieveRouter(id);
    }

    private Router createRouter(Vendor vendor, Model model, IP ip, Location location, RouterType routerType) {
        var router = routerManagementUseCase.createRouter(vendor, model, ip, location, routerType);
        return routerManagementUseCase.persistRouter(router);
    }

    public Router addRouterToCoreRouter(Id routerId, Id coreRouterId) {
        var router = routerManagementUseCase.retrieveRouter(routerId);
        var coreRouter = (CoreRouter) routerManagementUseCase.retrieveRouter(coreRouterId);
        return routerManagementUseCase.addRouterToCoreRouter(router, coreRouter);
    }
    
    public Router removeRouterFromCoreRouter(Id routerId, Id coreRouterId) {
        var router = routerManagementUseCase.retrieveRouter(routerId);
        var coreRouter = (CoreRouter) routerManagementUseCase.retrieveRouter(coreRouterId);
        return routerManagementUseCase.removeRouterFromCoreRouter(router, coreRouter);
    }
}
```

```java
public class SwitchManagementGenericAdapter {
    private SwitchManagementUseCase switchManagementUseCase;
    private RouterManagementUseCase routerManagementUseCase;
    
    public SwitchManagementGenericAdapter() {
        setPorts();
    }
    
    private void setPorts() {
        this.switchManagementUseCase = new SwitchManagementInputPort(SwitchManagementH2Adapter.getInstance());
        this.routerManagementUseCase = new RouterManagementInputPort(RouterManagementH2Adapter.getInstance());
    }
    
    public Switch retrieveSwitch(Id switchId) {
        return switchManagementUseCase.retrieveSwitch(switchId);
    }
    
    public EdgeRouter createAndSwitchToEdgeRouter(Vendor vendor, Model model, IP ip, Location location, SwitchType switchType, Id routerId) {
        Switch newSwitch = switchManagementUseCase.createSwitch(vendor, model, ip, location, switchType);
        
        Router edgeRouter = routerManagementUseCase.retrieveRouter(routerId);
        
        if(!edgeRouter.getRouterType().equals(RouterType.EDGE)) {
            throw new UnsupportedOperationException("Please inform the id of an edge router to add a switch");
        }
        
        Router router = switchManagementUseCase.addSwitchToEdgeRouter(newSwitch, (EdgeRouter) edgeRouter);
        
        return (EdgeRouter) routerManagementUseCase.persistRouter(router);
    }
    
    public EdgeRouter removeSwitchFromEdgeRouter(Id switchId, Id edgeRouterId) {
        EdgeRouter edgeRouter = (EdgeRouter) routerManagementUseCase.retrieveRouter(edgeRouterId);
        Switch networkSwitch = edgeRouter.getSwitches().get(switchId);
        Router router = switchManagementUseCase.removeSwitchFromEdgeRouter(networkSwitch, edgeRouter);
        return (EdgeRouter) routerManagementUseCase.persistRouter(router);
    }
}
```

```java
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
```

## 프레임워크 헥사곤 테스트하기
