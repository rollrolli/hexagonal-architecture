# 07 애플리케이션 헥사곤 만들기

## 애플리케이션 헥사곤 생성
* 애플리케이션 헥사곤
  * 도메인 헥사곤을 통한 내부 요청과 프레임워크 헥사곤을 통한 외부 요청을 조정한다.
  * 제약조건이나 비즈니스 규칙을 정의하지 않는다.
  * 목표 - 헥사고날 시스템에서 데이터 흐름을 정의하고 제어하는 것
* 유스케이스 구조화와 테스트에 큐컴버를 사용할 것

## 유스케이스 정의
* 토폴로지 및 인벤토리 시스템의 유스케이스 정의하기
  * 앞 장에서 만든 도메인 모델을 가지고 시스템의 기능을 구성해야 한다.
* `큐컴버`로 유스케이스 디스크립션 만들기
  * 큐컴버가 사용하는 기능 파일에서 유스케이스를 구조화해야 한다.
  * 기능 파일 : 유스케이스를 정의하는 글로 작성된 일련의 디스크립션을 기술하는 곳

### 라우터 관리 유스케이스에 대한 디스크립션 작성
* 시스템에 라우터를 추가하는 유스케이스를 서술하는 `RouterAdd.feature` 파일 생성
    ```
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
    ```
* `RouterCreate.feature` 파일 생성
    ```
    @RouterCreate
    Feature: 새로운 라우터를 만들 수 있는가?

        Scenario: 새로운 코어 라우터 만들기
            Given 코어 라우터를 만들기 위해 필요한 모든 데이터를 제공한다
            Then 새로운 코어 라우터가 생성된다

        Scenario: 새로운 에지 라우터 생성하기
            Given 에지 라우터를 만들기 위해 필요한 모든 데이터를 제공한다
            Then 새로운 에지 라우터가 생성된다
    ```
* `RouterRemove.feature` 파일 생성
    ```
    @RouterRemove
    Feature: 라우터를 제거할 수 있는가?

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
* `RouterAdd.feature`, `RouterCreate.feature`, `RouterRemove.feature`의 시나리오를 수행하는 오퍼레이션을 포함한 `RouterManagementUseCase` 인터페이스 정의
    ```java
    public interface RouterManagementUseCase {
        Router createRouter(Vendor vendor, Model model, IP ip, 
            Location location, RouterType routerType);
        CoreRouter addRouterToCoreRouter(Router router, CoreRouter coreRouter);
        Router removeRouterFromCoreRouter(Router router, CoreRouter coreRouter);

        Router retrieveRouter(Id id);
        Router persistRouter(Router router);
    }
    ```

### 스위치 관리 유스케이스를 위한 디스크립션 생성
* `SwitchAdd.feature` 파일 생성
    ```
    @SwitchAdd
    Feature: 에지 라우터에 스위치를 추가할 수 있는가?

        Scenario: 에지 라우터에 스위치 추가하기
            Given 스위치를 제공한다
            Then 에지 라우터에 스위치를 추가한다
    ```
* `SwitchCreate.feature` 파일 생성
    ```
    @SwitchCreate
    Feature: 새로운 스위치를 생성할 수 있는가?

        Scenario: 새로운 스위치 생성하기
            Given 스위치 생성에 필요한 모든 데이터를 제공한다
            Then 새로운 스위치가 생성된다
    ```
* `SwitchRemove.feature` 파일 생성
    ```
    @SwitchRemove
    Feature: 에지 라우터에서 스위치를 제거할 수 있는가?

        Scenario: 에지 라우터에서 스위치 제거하기
            Given 제거하기 원하는 스위치를 알 고 있다
            And 스위치는 아무런 네트워크도 갖고 있지 않다
            Then 에지 라우터에서 스위치를 제거한다
    ```

### 스위치 관리를 위한 유스케이스 인터페이스 정의
* `SwitchAdd.feature`, `SwitchCreate.feature`, `SwitchRemove.feature`의 시나리오를 수행하는 오퍼레이션을 포함한 `SwitchManagementUseCase` 인터페이스 정의
    ```java
    public interface SwitchManagementUseCase {
        Switch createSwitch(Vendor vendor, Model model, IP ip,
            Location location, SwitchType switchType);

        EdgeRouter addSwitchToEdgeRouter(Switch networkSwitch, EdgeRouter edgeRouter);

        EdgeRouter removeSwitchFromEdgeRouter(Switch networkSwitch, EdgeRouter edgeRouter);
    }
    ```

### 네트워크 관리자 유스케이스에 대한 디스크립션 생성
* `NetworkAdd.feature` 파일 생성
    ```
    @NetworkAdd
    Feature: 스위치에 네트워크를 추가할 수 있는가?

        Scenario: 스위치에 네트워크 추가하기
            Given 네트워크가 있다
            And 스위치에 추가할 네트워크가 있다
            Then 스위치에 네트워크를 추가한다
    ```
* `NetworkCreate.feature` 파일 생성
    ```
    @NetworkCreate
    Feature: 새로운 네트워크를 생성할 수 있는가?

        Scenario: 새로운 네트워크 생성하기
            Given 네트워크를 만들기 위해 필요한 모든 정보를 제공한다
            Then 새로운 네트워크가 생성된다
    ```
* `NetworkRemove.feature` 파일 생성
    ```
    @NetworkRemove
    Feature: 스위치에서 네트워크를 제거할 수 있는가?

        Scenario: 스위치에서 네트워크 제거하기
            Given 제거하기 원하는 네트워크를 알고 있다
            And 네트워크를 제거하려는 스위치를 알고 있다
            Then 스위치에서 네트워크를 제거한다
    ```

### 네트워크 관리를 위한 유스케이스 인터페이스 정의
* `NetworkAdd.feature`, `NetworkCreate.feature`, `NetworkRemove.feature`의 시나리오를 수행하는 오퍼레이션을 포함한 `NetworkManagementUseCase` 인터페이스 정의
    ```java
    public interface NetworkManagementUseCase {
        Network createNetwork(IP networkAddress, String networkName, int networkCidr);

        Switch addNetworkToSwitch(Network network, Switch networkSwitch);

        Switch removeNetworkFromSwitch(Network network, Switch networkSwitch);
    }
    ```

## 입력 포트를 갖는 유스케이스 구현
* 입력 포트
  * 도메인 헥사곤과 프레임워크 헥사곤 사이의 간극을 메워준다.
* 출력 포트
  * 외부 데이터는 출력 포트를 통해 가져온다.
  * 출력 포트를 통해 외부 데이터를 도메인 헥사곤으로 전달한다.
  * 도메인 헥사곤의 비즈니스 로직이 데이터에 적용되면 애플리케이션 헥사곤은 프레임워크 헥사곤의 출력 어댑터 중 하나에 도달할 때까지 해당 데이터를 다운스트림으로 이동시킨다.
  * 출력 어댑터를 구현물로 제공하는 프레임워크 헥사곤을 만들기 전에는 출력 포트 인터페이스를 정의할 수 없다.
* `RouterManagementUseCase` 인터페이스를 구현한 `RouterManagementInputPort` 클래스
    1. `RouterManagementInputPort`에 `RouterManagementOutputPort` 필드를 만드는 것으로 시작한다.
        ```java
        @NoArgsConstructor
        public class RouterManagementInputPort implements RouterManagementUseCase {
            RouterManagementOutputPort routerManagementOutputPort;
        }
        ```
        * 구현체에 직접 의존하지 않게 하기 위해 `RouterManagementOutputPort`는 인터페이스로 정의된다.
    2. `createRouter()` 메서드를 구현한다.
        ```java
        @Override
        public Router createRouter(Vendor vendor, 
                                Model model, 
                                IP ip, 
                                Location location,    
                                RouterType routerType) {
            return RouterFactory.getRouter(null, vendor, model, ip, location, routerType);
        }
        ```
        * `Router` 객체의 생성은 `RouterFactory` 클래스의 `getRouter()` 메서드에 위임된다. 
    3. `retrieveRouter()` 메서드를 구현한다.
        ```java
        @Override
        public Router retrieveRouter(Id id) {
            return routerManagementOutputPort.retrieveRouter(id);
        }
        ```
    4. `persistRouter()` 메서드를 구현한다.
        ```java
        @Override
        public Router persistRouter(Router router) {
            return routerManagementOutputPort.persistRouter(router);
        }
        ```
        * 라우터를 persist 하기 위해 Router 객체를 전달한다.
        * 새로운 `Router` 객체를 생성하거나 기존 객체를 변경하는 오퍼레이션 이후에 사용된다.
    5. `addRouterToCoreRouter()` 메서드를 구현한다.
        ```java
        @Override
        public CoreRouter addRouterToCoreRouter(Router router, CoreRouter coreRouter) {
            var addedRouter =  coreRouter.addRouter(router);
            //persistRouter(addedRouter);
            return addedRouter;
        }
        ```
        * 라우터를 persist 하기 위한 출력 어댑터가 없으므로 추가된 `Router` 객체만 반환한다.
    6. 마지막으로 `removeRouterFromCoreRouter()`를 구현한다.
        ```java
        @Override
        public Router removeRouterFromCoreRouter(Router router, CoreRouter coreRouter) {
            var removedRouter = coreRouter.removeRouter(router);
            //persistRouter(removedRouter);
            return removedRouter;
        }
        ```
       * `addRouterToCoreRouter()`와 마찬가지로 외부 데이터 소스에서 실제로 라우터를 제거하는 대신 제거 대상인 `removedRouter`만 반환한다.
* `createRouter()`에서 코어 라우터나 에지 라우터를 만들 수 있게 하는 `RouterFactory` 클래스를 구현한다.
    ```java
    public class RouterFactory {

        public static Router getRouter(Id id,
                                    Vendor vendor,
                                    Model model,
                                    IP ip,
                                    Location location,
                                    RouterType routerType){

            switch (routerType){
                case CORE:
                    return CoreRouter.builder().
                            id(id==null ? Id.withoutId():id).
                            vendor(vendor).
                            model(model).
                            ip(ip).
                            location(location).
                            routerType(routerType).
                            build();
                case EDGE:
                    return EdgeRouter.builder().
                            id(id==null ? Id.withoutId():id).
                            vendor(vendor).
                            model(model).
                            ip(ip).
                            location(location).
                            routerType(routerType).
                            build();
                default:
                    throw new UnsupportedOperationException("No valid router type informed");
            }
        }
    }
    ```
* `SwitchManagementUseCase` 인터페이스를 구현한 `SwitchManagementInputPort` 클래스
    1. `createSwitch()` 메서드를 구현한다.
        ```java
        @Override
        public Switch createSwitch(
                Vendor vendor,
                Model model,
                IP ip,
                Location location,
                SwitchType switchType) {
            return Switch
                    .builder()
                    .id(Id.withoutId())
                    .vendor(vendor)
                    .model(model)
                    .ip(ip)
                    .location(location)
                    .switchType(switchType)
                    .build();
        }
        ```
        * `createSwitch()` 메서드의 경우, 라우터와 비교해 객체의 변형이 없기 때문에 객체의 생성을 위한 팩토리 메서드가 필요하지 않다.
    2. `addSwitchToEdgeRouter()`를 구현한다.
        ```java
        @Override
        public EdgeRouter addSwitchToEdgeRouter(Switch networkSwitch, EdgeRouter edgeRouter) {
            edgeRouter.addSwitch(networkSwitch);
            return edgeRouter;
        }
        ```
        * 라우터를 persist 하지 않고 스위치를 persist 하는 방법이 없으므로 persist 메서드를 넣지 않는다.
        * 따라서 라우터가 persist 될 때만 스위치도 persist 되도록 강제해야 한다.
        * `Router`는 `Switch` 타입 객체를 포함해 다른 엔티티와 값 객체의 수명 주기를 제어하는 `애그리게잇`이다.
    3. 마지막으로 `removeSwitchFromEdgeRouter()` 메서드를 구현한다.
        ```java
        @Override
        public EdgeRouter removeSwitchFromEdgeRouter(Switch networkSwitch, EdgeRouter edgeRouter) {
            edgeRouter.removeSwitch(networkSwitch);
            return edgeRouter;
        }
        ```
* `NetworkManagementUseCase` 인터페이스를 구현한 `NetworkManagementInputPort` 클래스
    1. `createNetwork()` 메서드를 구현한다.
        ```java
        @Override
        public Network createNetwork(
                IP networkAddress, String networkName, int networkCidr) {
            return Network
                    .builder()
                    .networkAddress(networkAddress)
                    .networkName(networkName)
                    .networkCidr(networkCidr)
                    .build();
        }
        ```
    2. `addNetworkToSwitch()`를 구현한다.
        ```java
        @Override
        public Switch addNetworkToSwitch(Network network, Switch networkSwitch) {
            networkSwitch.addNetworkToSwitch(network);
            return networkSwitch;
        }
        ```
    3. 마지막으로 `removeNetworkFromSwitch()` 메서드를 구현한다.
        ```java
        @Override
        public Switch removeNetworkFromSwitch(Network network, Switch networkSwitch) {
            networkSwitch.removeNetworkFromSwitch(network);
            return networkSwitch;
        }
        ```

### 애플리케이션 헥사곤 테스트
* 큐컴버를 사용하면 기능 파일에 제공된, 동일한 글로 작성된 시나리오 디스크립션으로 단위 테스트를 작성할 수 있다.
* 이번 절의 단위 테스트 목적 : 입력 포트 구현 테스트
* 먼저 큐컴버를 활성화하는 `ApplicationTest` 테스트 클래스를 만들어야 한다.
    ```java
    @RunWith(Cucumber.class)
    @CucumberOptions(
        plugin = {"pretty", "html:target/cucumber-result"}
    )
    public class ApplicationTest {

    }
    ```
    * `@RunWith(Cucumber.class)`는 큐컴버 엔진을 초기화 하는 부분이다.
* `RouterAdd.feature` 기능 파일에 대응하는 `RouterAdd` 테스트 클래스를 생성한다.
* 코어 라우터에 에지 라우터를 추가하는 시나리오
    1. 첫 단계는 에지 라우터를 가져오는 것이다.
        ```java
        @Given("I have an edge router")
        public void assert_edge_router_exists(){
            edgeRouter = (EdgeRouter) this.routerManagementUseCase.createRouter(
                    Vendor.HP,
                    Model.XYZ0004,
                    IP.fromAddress("20.0.0.1"),
                    locationA,
                    EDGE
            );
            assertNotNull(edgeRouter);
        }
        ```
        * 에지 라우터 객체를 생성하기 위해 `RouterManagementUseCase`의 `createRouter()` 메서드를 사용한다.
    2. 이제 `EdgeRouter`가 있으므로 `createRouter()` 메서드를 다시 사용해 `CoreRouter`를 생성한다.
        ```java
        @And("I have a core router")
        public void assert_core_router_exists(){
            coreRouter = (CoreRouter) this.routerManagementUseCase.createRouter(
                    Vendor.CISCO,
                    Model.XYZ0001,
                    IP.fromAddress("30.0.0.1"),
                    locationA,
                    CORE
            );
            assertNotNull(coreRouter);
        }
        ``` 
    3. 이제 이 두 객체 `EdgeRouter`와 `CoreRouter`로 에지 라우터를 코어 라우터에 추가하는 것을 테스트할 수 있다.
        ```java
        @Then("I add an edge router to a core router")
        public void add_edge_to_core_router(){
            var actualEdgeId = edgeRouter.getId();
            var routerWithEdge = (CoreRouter) this.routerManagementUseCase.
                    addRouterToCoreRouter(edgeRouter, coreRouter);
            var expectedEdgeId = routerWithEdge.getRouters().get(actualEdgeId).getId();
            assertEquals(actualEdgeId, expectedEdgeId);
        }
        ```
        * 메서드의 마지막에 에지 라우터가 올바르게 코어 라우터에 추가되었는지 확인하기 위해 실제 에지 라우터의 ID와 예상되는 에지 라우터 ID를 비교한다.
* `RouterCreate.feature` 기능 파일에 대응하는 `RouterCreate` 테스트 클래스를 생성한다.
* 새로운 코어 라우터를 생성하는 시나리오
    1. 첫 번째 단계는 새로운 코어 라우터를 생성하는 것이다.
    2. 그 다음, 생성된 라우터가 실제로 코어 라우터인지 확인한다.
* 새로운 에지 라우터를 생성하는 시나리오
    1. 먼저 에지 라우터를 생성한다.
    2. 마지막 시나리오 단계에서 생성된 객체의 참조가 null 은 아닌지, 타입이 `EdgeRouter`인지 확인한다.
* `RouterRemove.feature` 기능 파일에 대응하는 `RouterRemove` 테스트 클래스를 생성한다.
* 코어 라우터에서 에지 라우터를 제거하는 시나리오
    1. 먼저 대상 코어 라우터가 하나 이상의 에지 라우터와 연결되어 있는지 알아야 한다.
    2. 다음으로, 에지 라우터에 연결된 스위치에 연결되어 있는 네트워크가 있는지 확인해야 한다. 확인하지 않으면 에지 라우터에서 스위치를 제거할 수 없다.
    3. 다음으로 계속해서 에지 라우터에 연결된 스위치가 있는지 확인할 수 있다.
    4. 이제 코어 라우터에서 에지 라우터가 제거되는 것을 테스트할 수 있다.
