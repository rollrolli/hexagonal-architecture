# 7. 애플리케이션 헥사곤 만들기

- 시스템 기능을 더욱 잘 보여주기 위해 기능과 시나리오 같은 개념을 사용해 시스템의 동작을 설명하는 행위 주도 개발 기술인 큐컴버를 사용한다.

## 애플리케이션 헥사곤 생성

- 애플리케이션 헥사곤은 도메인 헥사곤을 통한 내부 요청과 프레임워크 헥사곤을 통한 외부 요청을 조정한다.
- 애플리케이션 헥사곤의 목표는 헥사고날 시스템에서 데이터 흐름을 정의하고 제어하는 것이다.

## 유스케이스 정의

- 유스케이스를 글 형식과 코드 형식으로 모두 표현하기 위해 큐컴버를 사용
- 큐컴버가 사용하는 기능 파일에서 유스케이스를 구조화해야 한다.
- 기능 파일은 유스케이스를 정의하는 글로 작성된 일련의 디스크립션을 기술하는 곳이다.
- 유스케이스를 테스트하기 위해 클래스를 구현하는 동안 사용되는 것과 같은 글로 작성된 디스크립션이다.

### 라우터 관리 유스케이스에 대한 디스크립션 작성

```jsx
@RouterAdd
Feature: 코어 라우터에 에지 라우터를 추가할 수 있는가?
  Scenario: 코어 라우터에 에지 라우터 추가
    Given 에지 라우터가 있다
    And 코어 라우터가 있다
    Then 코어 라우터에 에지 라우터를 추가한다

  Scenario: 또 다른 코어 라우터에 코어 라우터를 추가한다
    Given 코어 라우터가 있다
    And 또 다른 코어 라우터가 있다
    Then 코어 라우터에 이 코어 라우터를 추가한다

@RouterCreate
Feature: 새로운 라우터를 만들 수 있는가?
  Scenario: 새로운 코어 라우터 만들기
    Given 코어 라우터를 만들기 위해 필요한 모든 데이터를 제공한다
    Then 새로운 코어 라우터가 생성된다
  Scenario: 새로운 에지 라우터 생성하기
    Given 에지 라우터를 만들기 위해 필요한 모든 데이터를 제공한다
    Then 새로운 에지 라우터가 생성된다

@RouterRemove
Feature: 라우터를 제거할 수 있는가>
  Scenario: 코어 라우터에서 에지 라우터 제거하기
    Given 코어 라우터는 적어도 하나의 에지 라우터와 연결되어 있다
    And 스위치는 연결된 네트워크를 갖고 있지 않다
    And 에지 라우터는 연결된 네트워크를 갖고 있지 않다
    Then 코어 라우터에서 에지 라우터를 제거한다
  Scenario: 코어 라우터를 또 다른 코어 라우터에서 제거하기
    Given 코어 라우터는 적어도 하나의 코어 라우터와 연결되어 있다
    And 코어 라우터는 연결된 또 다른 라우터를 갖고 있지 않다
    Then 코어 라우터를 또 다른 코어 라우터에서 제거한다
```

### 라우터 관리를 위한 유스케이스 인터페이스 정의

```java
public interface RouterManagementUseCase {
    Router createRouter(Vendor vendor, Model model, IP ip, Location location, RouterType routerType);
    CoreRouter addRouterToCoreRouter(Router router, CoreRouter coreRouter);
    Router removeRouterFromCoreRouter(Router router, CoreRouter coreRouter);

    Router retrieveRouter(Id id);
    Router persistRouter(Router router);
}
```

### 스위치 관리 유스케이스에 대한 디스크립션 작성

```java
@SwitchAdd
Feature: 에지 라우터에 스위치를 추가할 수 있는가?
  Scenario: 에지 라우터에 스위치 추가하기
    Given 스위치를 제공한다
    Then 에지 라우터에 스위치를 추가한다

@SwitchCreate
Feature: 새로운 스위치를 생성할 수 있는가?
  Scenario: 새로운 스위치 생성하기
    Given 스위치 생성에 필요한 모든 데이터를 제공한다
    Then 새로운 스위치가 생성된다

@SwitchRemove
Feature: 에지 라우터에서 스위치를 제거할 수 있는가?
  Scenario: 에지 라우터에서 스위치 제거하기
    Given 제거하기 원하는 스위치를 알고 있다
    And 스위치는 아무런 네트워크도 갖고 있지 않다
    Then 에지 라우터에서 스위치를 제거한다
```

### 스위치 관리를 위한 유스케이스 인터페이스 정의

```java
public interface SwitchManagementUseCase {
    Switch createSwitch(Vendor vendor, Model model, IP ip, Location location, SwitchType switchType);
    EdgeRouter addSwitchToEdgeRouter(Switch networkSwitch, EdgeRouter edgeRouter);
    EdgeRouter removeSwitchFromEdgeRouter(Switch networkSwitch, EdgeRouter edgeRouter);
}
```

### 네트워크 관리 유스케이스에 대한 디스크립션 작성

```java
@NetworkAdd
Feature: 스위치에 네트워크를 추가할 수 있는가?
  Scenario: 스위치에 네트워크 추가하기
    Given 네트워크가 있다
    And 스위치에 추가할 네트워크가 있다
    Then 스위치에 네트워크를 추가한다

@NetworkCreate
Feature: 새로운 네트워크를 생성할 수 있는가?
  Scenario: 새로운 네트워크 생성하기
    Given 네트워크를 만들기 위해 필요한 모든 정보를 제공한다
    Then 새로운 네트워크가 생성된다

@NetworkRemove
Feature: 스위치에서 네트워크를 제거할 수 있는가?
  Scenario: 스위치에서 네트워크 제거하기
    Given 제거하기 원하는 네트워크를 알고 있다
    And 네트워크를 제거하려는 스위치를 알고 있다
    Then 스위치에서 네트워크를 제거한다
```

### 네트워크 관리를 위한 유스케이스 인터페이스 정의

```java
public interface NetworkManagementUseCase {
    Network createNetwork(IP networkAddress, String networkName, int networkCidr);
    Switch addNetworkToSwitch(Network network, Switch networkSwitch);
    Switch removeNetworkFromSwitch(Network network, Switch networkSwitch);
}
```

## 입력 포트를 갖는 유스케이스 구현

- 입력 포트는 도메인 헥사곤과 프레임워크 헥사곤 사이의 간극을 메우기 때문에 중요한 통합 역할을 한다.
- 외부 데이터는 출력 포트에서 가져오고 해당 데이터를 도메인 헥사곤으로 전달할 수 있다.

```java
@NoArgsConstructor
public class RouterManagementInputPort implements RouterManagementUseCase {
    RouterManagementOutputPort routerManagementOutputPort;

    @Override
    public Router createRouter(Vendor vendor, Model model, IP ip, Location location, RouterType routerType) {
        return RouterFactory.getRouter(vendor, model, ip, location,routerType);
    }

    @Override
    public CoreRouter addRouterToCoreRouter(Router router, CoreRouter coreRouter) {
        var addedRouter = coreRouter.addRouter(router);
        //persistRouter(addedRouter);
        return addedRouter;
    }

    @Override
    public Router removeRouterFromCoreRouter(Router router, CoreRouter coreRouter) {
        var removedRouter = coreRouter.removeRouter(router);
        //persistRouter(addedRouter);
        return removedRouter;
    }

    @Override
    public Router retrieveRouter(Id id) {
        return routerManagementOutputPort.retrieveRouter(id);
    }

    @Override
    public Router persistRouter(Router router) {
        return routerManagementOutputPort.persistRouter(router);
    }
}
```

```java
public class SwitchManagementInputPort implements SwitchManagementUseCase {
    @Override
    public Switch createSwitch(Vendor vendor, Model model, IP ip, Location location, SwitchType switchType) {
        return Switch.builder().id(Id.withoutId()).vendor(vendor).model(model).ip(ip).location(location).switchType(switchType).build();
    }

    @Override
    public EdgeRouter addSwitchToEdgeRouter(Switch networkSwitch, EdgeRouter edgeRouter) {
        edgeRouter.addSwitch(networkSwitch);
        return edgeRouter;
    }

    @Override
    public EdgeRouter removeSwitchFromEdgeRouter(Switch networkSwitch, EdgeRouter edgeRouter) {
        edgeRouter.removeSwitch(networkSwitch);
        return edgeRouter;
    }
}
```

```java
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
```

### 애플리케이션 헥사곤 테스트

- 큐컴버를 통해 단위 테스트를 조정하기 위해 기능 파일에 제공된 동일한 글로 작성된 시나리오 디스크립션을 사용할 수 있다.
