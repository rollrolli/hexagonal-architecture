# 08 프레임워크 헥사곤 만들기

## 프레임워크 헥사곤 부트스트래핑
* 헥사고날 아키텍처의 장점
  * 헥사고날 아키텍처에서는 도메인 헥사곤에서 문제 영역 모델링을 먼저 하고, 애플리케이션 헥사곤에서 유스케이스를 설계하고 구현 한 후에 어떤 기술을 사용할지 결정해도 된다.
  * 즉, 외부의 기반 기술에 대한 결정을 연기할 수 있게 한다.
  * 어댑터를 플러그인 형식으로 처리하여 그때그때 필요한 어댑터를 포트에 연결할 수 있다.
* 프레임워크 헥사곤을 모듈로 만들어 사용할 수 있다.
  * 프레임워크 모듈은 도메인 모듈과 애플리케이션 모듈 모두에 의존하므로 도메인 모듈과 애플리케이션 모듈을 모두 의존성으로 추가해주어야 한다.

## 출력 어댑터 구현
* 입력 어댑터를 구현할 때 출력 어댑터를 참조하기 때문에 출력 어댑터를 먼저 구현한다.

### 라우터 관리 출력 어댑터
* `RouterManagementOutputPort` 출력 포트 인터페이스
    ```java
    public interface RouterManagementOutputPort {
        Router retrieveRouter(Id id);
        Router removeRouter(Id id);
        Router persistRouter(Router router);
    }
    ```
* H2 인메모리 데이터베이스를 사용할 수 있게 하는 출력 어댑터 `RouterManagementH2Adapter` 출력 어댑터 클래스
    ```java
    public class RouterManagementH2Adapter implements RouterManagementOutputPort {

        private static RouterManagementH2Adapter instance;

        @PersistenceContext
        private EntityManager em;

        private RouterManagementH2Adapter(){
            setUpH2Database();
        }
        /* 코드 생략 */
    }
    ```
    * `EntityManager`: H2 데이터베이스 연결 제어
    * `retrieveRouter()` 메서드 구현
        ```java
            @Override
            public Router retrieveRouter(Id id) {
                var routerData = em.getReference(RouterData.class, id.getUuid());
                return RouterH2Mapper.routerDataToDomain(routerData);
            }
        ```
        * `EntityManager`의 `getReference()` 메서드는 `RouterData.class`와 함께 호출되고 Id 객체에서 UUID 값이 추출된다.
        * `RouterData`는 데이터베이스 엔티티 클래스로 데이터베이스에서 오는 데이터를 `Router` 도메인 엔티티 클래스로 매핑하는 데 사용된다. (매핑에 `RouterH2Mapper.routerDataToDomain()` 사용)
    * `removeRouter()` 메서드 구현
        ```java
            @Override
            public Router removeRouter(Id id) {
                var routerData = em.getReference(RouterData.class, id.getUuid());
                em.remove(routerData);
                return null;
            }
        ```
        * 제거할 라우터를 조회하기 위해 `getReference()` 메서드를 호출한다.
        * 데이터베이스 엔티티를 의미하는 `RouterData` 객체를 갖게 되면 `EntityManager`의 `remove()` 메서드를 호출할 수 있다.
    * `persistRouter()` 메서드 구현
        ```java
            @Override
            public Router persistRouter(Router router) {
                var routerData = RouterH2Mapper.routerDomainToData(router);
                em.persist(routerData);
                return router;
            }
        ```
        * `Router` 도메인 엔티티 객체를 받아 `RouterData` 데이터베이스 엔티티 객체로 변환하여 `EntityManager`의 `persist()` 메서드로 저장한다.

### 스위치 관리 출력 어댑터
* 스위치는 직접 persist 하거나 제거할 필요가 없기 때문에 더 간단하다.
* 스위치 출력 어댑터의 유일한 목적은 데이터베이스에서 스위치를 조회할 수 있게 만드는 것이다.
* `SwitchManagementOutputPort` 출력 포트 인터페이스
    ```java
    public interface SwitchManagementOutputPort {
        Switch retrieveSwitch(Id id);
    }
    ```
* `SwitchManagementH2Adapter` 출력 어댑터 클래스
    ```java
    public class SwitchManagementH2Adapter implements SwitchManagementOutputPort {
        /* 코드 생략 */
        @Override
        public Switch retrieveSwitch(Id id) {
            var switchData = em.getReference(SwitchData.class, id.getUuid());
            return RouterH2Mapper.switchDataToDomain(switchData);
        }
        /* 코드 생략 */
    }
    ```
    * `retrieveRouter()`
        * `SwitchData` 데이터베이스 엔티티 객체를 조회하기 위해 `SwitchData.class`와 UUID 값을 매개변수로 `EntityManager`의 `getReference()` 메서드를 호출한다.
        * 그 다음 데이터베이스에서 조회된 객체를 `RouterH2Mapper.switchDataToDomain()` 메서드로 `Switch` 도메인 엔티티로 변환한다.

## 입력 어댑터 구현

### 라우터 관리 입력 어댑터
* `RouterManagementGenericAdapter` 입력 어댑터 클래스
    ```java
    public class RouterManagementGenericAdapter {

        private RouterManagementUseCase routerManagementUseCase;

        public RouterManagementGenericAdapter(){
            setPorts();
        }
        private void setPorts(){
            this.routerManagementUseCase = new RouterManagementInputPort(
                    RouterManagementH2Adapter.getInstance()
            );
        }
        /* 코드 생략 */
    }
    ```
    * `RouterManagementInputPort`를 선언하지 않고 `RouterManagementUseCase`를 선언하고 입력 포트를 연결해준다.
    * 시스템에서 라우터를 조회하고 제거하는 오퍼레이션 `retrieveRouter`, `removeRouter`
        ```java
            /**
             * GET /router/retrieve/{id}
            * */
            public Router retrieveRouter(Id id){
                return routerManagementUseCase.retrieveRouter(id);
            }

            /**
             * GET /router/remove/{id}
            * */
            public Router removeRouter(Id id){
                return routerManagementUseCase.removeRouter(id);
            }
        ```
    * 새로운 라우터를 만들기 위한 오퍼레이션 `createRouter`
        ```java
            /**
             * POST /router/create
            * */
            public Router createRouter(Vendor vendor,
                                            Model model,
                                            IP ip,
                                            Location location,
                                            RouterType routerType){
                var router = routerManagementUseCase.createRouter(
                        null,
                        vendor,
                        model,
                        ip,
                        location,
                        routerType

                );
                return routerManagementUseCase.persistRouter(router);
            }
        ```
    * 코어 라우터에서 라우터 추가와 제거를 허용하기 위한 오퍼레이션 `addRouterToCoreRouter`, `removeRouterFromCoreRouter`
        ```java
            /**
             * POST /router/add
            * */
            public Router addRouterToCoreRouter(Id routerId, Id coreRouterId){
                Router router = routerManagementUseCase.retrieveRouter(routerId);
                CoreRouter coreRouter = (CoreRouter) routerManagementUseCase.retrieveRouter(coreRouterId);
                return routerManagementUseCase.
                        addRouterToCoreRouter(router, coreRouter);
            }

            /**
             * POST /router/remove
            * */
            public Router removeRouterFromCoreRouter(Id routerId, Id coreRouterId){
                Router router = routerManagementUseCase.retrieveRouter(routerId);
                CoreRouter coreRouter = (CoreRouter) routerManagementUseCase.retrieveRouter(coreRouterId);
                return routerManagementUseCase.
                        removeRouterFromCoreRouter(router, coreRouter);
            }
        ```

### 스위치 관리 입력 어댑터
* `SwitchManagementGenericAdapter` 입력 어댑터 클래스
    ```java
    public class SwitchManagementGenericAdapter {

        private RouterManagementUseCase routerManagementUseCase;
        private SwitchManagementUseCase switchManagementUseCase;

        public SwitchManagementGenericAdapter(){
            setPorts();
        }

        private void setPorts(){
            this.routerManagementUseCase = new RouterManagementInputPort(
                    RouterManagementH2Adapter.getInstance()
            );
            this.switchManagementUseCase = new SwitchManagementInputPort(
                    SwitchManagementH2Adapter.getInstance()
            );
        }
        /* 코드 생략 */
    }
    ```
    * 두 개의 입력 포트 사용
      * `RouterManagementUseCase` 유스케이스에 연결된 `RouterManagementInputPort` 입력 포트
      * `SwitchManagementUseCase` 유스케이스에 연결된 `SwitchManagementInputPort` 입력 포트
    * 스위치를 조회하는 오퍼레이션 `retrieveSwitch`
        ```java
            /**
             * GET /switch/retrieve/{id}
            * */
            public Switch retrieveSwitch(Id switchId) {
                return switchManagementUseCase.retrieveSwitch(switchId);
            }
        ```
    * 에지 라우터에 스위치를 생성하고 추가할 수 있는 오퍼레이션 `createAndAddSwitchToEdgeRouter`
        ```java
            /**
             * POST /switch/create
            * */
            public EdgeRouter createAndAddSwitchToEdgeRouter(
                    Vendor vendor,
                    Model model,
                    IP ip,
                    Location location,
                    SwitchType switchType,
                    Id routerId
            ) {
                Switch newSwitch = switchManagementUseCase.createSwitch(vendor, model, ip, location, switchType);
                Router edgeRouter = routerManagementUseCase.retrieveRouter(routerId);
                if(!edgeRouter.getRouterType().equals(RouterType.EDGE))
                    throw new UnsupportedOperationException("Please inform the id of an edge router to add a switch");
                Router router = switchManagementUseCase.addSwitchToEdgeRouter(newSwitch, (EdgeRouter) edgeRouter);
                return (EdgeRouter) routerManagementUseCase.persistRouter(router);
            }
        ```
    * 에지 라우터에서 스위치를 제거하는 `removeSwitchFromEdgeRouter`
        ```java
            /**
             * POST /switch/remove
            * */
            public EdgeRouter removeSwitchFromEdgeRouter(Id switchId, Id edgeRouterId) {
                EdgeRouter edgeRouter = (EdgeRouter) routerManagementUseCase
                        .retrieveRouter(edgeRouterId);
                Switch networkSwitch = edgeRouter.getSwitches().get(switchId);
                Router router = switchManagementUseCase
                        .removeSwitchFromEdgeRouter(networkSwitch, edgeRouter);
                return (EdgeRouter) routerManagementUseCase.persistRouter(router);
            }
        ```

### 네트워크 관리 입력 어댑터
* `NetworkManagementGenericAdapter` 입력 어댑터 클래스
    ```java
    public class NetworkManagementGenericAdapter {

        private SwitchManagementUseCase switchManagementUseCase;
        private NetworkManagementUseCase networkManagementUseCase;

        public NetworkManagementGenericAdapter(){
            setPorts();
        }

        private void setPorts(){
            this.switchManagementUseCase = new SwitchManagementInputPort(SwitchManagementH2Adapter.getInstance());
            this.networkManagementUseCase = new NetworkManagementInputPort(RouterManagementH2Adapter.getInstance());
        }
        /* 코드 생략 */
    }
    ```
    * 두 개의 입력 포트 사용
      * `SwitchManagementUseCase` 유스케이스에 연결된 `SwitchManagementInputPort` 입력 포트
      * `NetworkManagementUseCase` 유스케이스에 연결된 `NetworkManagementInputPort` 입력 포트
    * 스위치에 네트워크를 추가하는 오퍼레이션 `addNetworkToSwitch`
        ```java
            /**
             * POST /network/add
            * */
            public Switch addNetworkToSwitch(Network network, Id switchId) {
                Switch networkSwitch = switchManagementUseCase.retrieveSwitch(switchId);
                return networkManagementUseCase.addNetworkToSwitch(network, networkSwitch);
            }
        ```
    * 스위치에서 네트워크를 제거하는 오퍼레이션 `removeNetworkFromSwitch`
        ```java
            /**
            * POST /network/remove
            * */
            public Switch removeNetworkFromSwitch(String networkName, Id switchId) {
                Switch networkSwitch = switchManagementUseCase.retrieveSwitch(switchId);
                return networkManagementUseCase.removeNetworkFromSwitch(networkName, networkSwitch);
            }
        ```

## 프레임워크 헥사곤 테스트하기
* 프레임워크 헥사곤 테스트를 통해
  * 입력 어댑터와 출력 어댑터의 동작 확인 가능
  * 도메인 헥사곤과 애플리케이션 헥사곤 같은 다른 헥사곤이 프레임워크 헥사곤에서 나오는 요청에 대한 응답에 제대로 역할을 하고 있는지 확인 가능
* 라우터 관리 입력 어댑터 테스트
    ```java
    public class RouterTest extends FrameworkTestData {

        RouterManagementGenericAdapter routerManagementGenericAdapter;

        public RouterTest() {
            this.routerManagementGenericAdapter = new RouterManagementGenericAdapter();
            loadData();
        }

        /* 코드 생략 */
    }
    ```
    * 라우터 조회 테스트 (테스트 대상 메서드: `routerManagementGenericAdapter.retrieveRouter()`)
        ```java
            @Test
            public void retrieveRouter() {
                var id = Id.withId("b832ef4f-f894-4194-8feb-a99c2cd4be0c");
                var actualId = routerManagementGenericAdapter.
                        retrieveRouter(id).getId();
                assertEquals(id, actualId);
            }
        ```
    * 라우터 생성 테스트 (테스트 대상 메서드: `routerManagementGenericAdapter.createRouter()`)
    * 코어 라우터의 라우터 추가 테스트 (테스트 대상 메서드: `routerManagementGenericAdapter.addRouterToCoreRouter()`)
    * 코어 라우터의 라우터 제거 테스트 (테스트 대상 메서드: `routerManagementGenericAdapter.removeRouterToCoreRouter()`)

## 요약
* H2 출력 어댑터 구현
* 3개의 입력 어댑터 구현 (라우터, 스위치, 네트워크)
* 어댑터와 전체 헥사고날 시스템을 위한 테스트 구현