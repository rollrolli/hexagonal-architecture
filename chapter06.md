# 06 도메인 헥사곤 만들기

## 도메인 헥사곤 생성
* 기존 프로젝트와 이번 예제의 차이점 - 자바 모듈에 도메인 헥사곤을 캡슐화하기 위해 **JPMS(Java Platform Module System)** 사용
* 이번 예제를 위한 멀티 모듈 메이븐 프로젝트 생성
  * 자바 9 버전 이후부터 `module-info.java` 모듈 디스크립터 파일을 자바 프로젝트 루트 디렉터리에 넣으면 모듈을 생성할 수 있다.
  * 생성된 자바 모듈은 해당 모듈 내의 모든 공용 패키지에 대한 엑세스를 차단한다.
  * 다른 모듈에서 이 모듈을 사용하려면 모듈 디스크립터 파일에서 원하는 패키지를 export 해야 한다.

## 문제 영역 이해
* 코어 라우터 
  * 코어 라우터와 에지 라우터 모두에 연결 가능하다.
  * 더 빠르고 높은 트래픽 부하를 처리한다.
  * 스위치와 스위치의 네트워크에서 생성된 트래픽을 직접적으로 처리하지 않는다.
* 에지 라우터
  * 스위치와 네트워크에 연결된다.
  * 스위치에 스위치의 네트워크에서 생성된 트래픽을 직접 처리한다.
  * 다른 에지 라우터와 연결할 수 없다.
* 스위치
  * 여러 개의 네트워크를 가질 수 있다.
* 토폴로지 및 인벤토리 시스템을 구축하는 방법
  * 먼저 도메인 모델을 이용해 최상위 수준에서 시스템의 목적 달성에 필요한 오퍼레이션과 규칙을 포함하는 도메인 헥사곤을 생성해야 한다. 
  * 오퍼레이션과 같은 비즈니스 아이디어들이 애플리케이션 헥사곤이나 프레임워크 헥사곤으로 이동하게 되면 기술에 특화되어 버린다. 즉, 도메인 헥사곤 내에 핵심적인 시스템 기능이 유지되어야 한다.
* 도메인 헥사곤의 메서드와 클래스 검증 방법
  * 단위 테스트 작성

## 값 객체 정의 
* 패키지명 : vo
* 값 객체는 엔티티와 다르게 식별자를 갖지 않는다.
* 식별자를 정의할 필요가 없는 시스템 요소를 기술할 때 값 객체를 사용한다.
* 도메인 헥사곤을 만들 때는, 더 정교한 값 객체나 엔티티를 만들 때 사용되므로 값 객체 생성으로 시작하는 것이 좋다.
* 값 객체 정의 시작하기
    1. `Id` 값 객체 생성
        ```java
        @Getter
        @ToString
        @EqualsAndHashCode
        public class Id {

            private final UUID id;

            private Id(UUID id) {
                this.id = id;
            }

            public static Id withId(String id) {
                return new Id(UUID.fromString(id));
            }

            public static Id withoutId() {
                return new Id(UUID.randomUUID());
            }
        }
        ```
    2. `Vendor` 열거형 값 객체 생성
        ```java
        public enum Vendor {
            CISCO,
            NETGEAR,
            HP,
            TPLINK,
            DLINK,
            JUNIPER
        }
        ```
    3. `Model` 열거형 값 객체 생성
        ```java
        public enum Model {
            XYZ0001,
            XYZ0002,
            XYZ0003,
            XYZ0004
        }
        ```
    4. 인터넷 프로토콜 버전 4와 6 표현을 위한 `Protocol` 열거형 값 객체 생성
        ```java
        public enum Protocol {
            IPV4,
            IPV6;
        }
        ```
    5. 라우터 종류 정의를 위한 `RouterType` 열거형 값 객체 생성
        ```java
        public enum RouterType {
            EDGE,
            CORE;
        }
        ```
    6. 사용 가능한 스위치 타입 정의를 위한 `SwitchType` 열거형 값 객체 생성
        ```java
        public enum SwitchType {
            LAYER2,
            LAYER3;
        }
        ```
    7. 모든 라우터와 스위치의 위치를 표현하기 위한 `Location` 값 객체 생성
        ```java
        @Builder
        @AllArgsConstructor
        @Getter
        @ToString
        @EqualsAndHashCode
        public class Location {

            private String address;
            private String city;
            private String state;
            private int zipCode;
            private String country;

            private float latitude;
            private float longitude;
        }
        ```
* 방금 생성한 값 객체를 기반으로 한 더 정교한 값 객체 생성
    1. `IP` 값 객체 생성
        ```java
        @Getter
        @ToString
        @EqualsAndHashCode
        public class IP {

            private String ipAddress;
            private Protocol protocol;

            public IP(String ipAddress){
            if(ipAddress == null)
                throw new IllegalArgumentException("Null IP address");
            this.ipAddress = ipAddress;
            if(ipAddress.length()<=15) {
                this.protocol = Protocol.IPV4;
            } else {
                this.protocol = Protocol.IPV6;
            }
            }

            public static IP fromAddress(String ipAddress){
                return new IP(ipAddress);
            }
        }
        ```
        IP 값 객체를 통해 IPv4와 IPv6 주소를 모두 만들 수 있다. IP 주소 검증 로직에는 commons-validator 라이브러리의 `InetAddressValidator` 도 사용 가능하다.
    2. 스위치에 추가될 네트워크를 나타내는 `Network` 값 객체 생성
        ```java
        @Builder
        @Getter
        @ToString
        @EqualsAndHashCode
        public class Network {

            private IP networkAddress;
            private String networkName;
            private int networkCidr;

            public Network(IP networkAddress, String networkName, int networkCidr){
                if(networkCidr < 1 || networkCidr > 32){
                    throw new IllegalArgumentException("Invalid CIDR value");
                }
                this.networkAddress = networkAddress;
                this.networkName = networkName;
                this.networkCidr = networkCidr;
            }
        }
        ```

## 엔티티와 명세 정의
* 패키지명 : 엔티티 - entity, 명세 - specification
* 엔티티를 특정짓는 것은 식별자와 비즈니스 규칙, 데이터의 존재이다.
* 이 시스템에서는 `Equipment`, `Router`, `Switch` 를 엔티티로 갖는다.

### Equipment 와 Router 추상 엔티티
* 라우터와 스위치 모두 네트워크 장비이므로 `Equipment` 추상 클래스 생성
    ```java
    @Getter
    @AllArgsConstructor
    public abstract class Equipment {
        protected Id id;
        protected Vendor vendor;
        protected Model model;
        protected IP ip;
        protected Location location;

        public static Predicate<Equipment> getVendorPredicate(Vendor vendor){
            return r -> r.getVendor().equals(vendor);
        }
    }
    ```  
    * `getVendorPredicate()` : 특정 공급업체의 장비만 검색하는 필터 
* `Equipment` 에서 파생되는 `Router` 추상 클래스 생성
    ```java
    @Getter
    public abstract class Router extends Equipment {

        protected final RouterType routerType;

        public static Predicate<Equipment> getRouterTypePredicate(RouterType routerType){
            return r -> ((Router)r).getRouterType().equals(routerType);
        }

        public static Predicate<Equipment> getModelPredicate(Model model){
            return r -> r.getModel().equals(model);
        }

        public static Predicate<Equipment> getCountryPredicate(Location location){
            return p -> p.location.getCountry().equals(location.getCountry());
        }
        /* 코드 생략 */
    }
    ```
    * 코어 라우터나 에지 라우터의 공통적인 프레디케이트 정의
      * `getRouterTypePredicate()` : 특정 유형의 라우터만 검색하는 필터
      * `getModelPredicate()` : 특정 모델의 라우터만 검색하는 필터
      * `getCountryPredicate()` : 특정 국가의 라우터만 검색하는 필터

### 코어 라우터 엔티티와 명세
* `CoreRouter` 엔티티 클래스 구현
    ```java
    @Getter
    @ToString
    public class CoreRouter extends Router{

        @Getter
        private Map<Id, Router> routers;

        @Builder
        public CoreRouter(Id id, Vendor vendor, Model model, IP ip, Location location, RouterType routerType, Map<Id, Router> routers) {
            super(id, vendor, model, ip, location, routerType);
            this.routers = routers;
        }

        public Router addRouter(Router anyRouter) {
            var sameCountryRouterSpec = new SameCountrySpec(this);
            var sameIpSpec = new SameIpSpec(this);

            sameCountryRouterSpec.check(anyRouter);
            sameIpSpec.check(anyRouter);

            return this.routers.put(anyRouter.id, anyRouter);
        }

        public Router removeRouter(Router anyRouter) {
            var emptyRoutersSpec = new EmptyRouterSpec();
            var emptySwitchSpec = new EmptySwitchSpec();

            switch (anyRouter.routerType) {
                case CORE:
                    var coreRouter = (CoreRouter)anyRouter;
                    emptyRoutersSpec.check(coreRouter);
                    break;
                case EDGE:
                    var edgeRouter = (EdgeRouter)anyRouter;
                    emptySwitchSpec.check(edgeRouter);
            }
            return this.routers.remove(anyRouter.id);
        }
    }
    ```
    * `addRouter()` : 다른 코어 라우터나 에지 라우터에 연결될 수 있게 하는 메서드
      * 연결할 에지 라우터가 같은 국가 안에 있는지 확인하는 `SameCountrySpec` 명세 사용
      * 연결할 라우터가 IP 주소를 갖고 있는지 확인하는 `SameIpSpec` 명세 사용
      * 명세를 사용하면 비즈니스 규칙이 더욱 명시적으로 표현되고, 코드가 이해하기 쉬워진다.
    * `removeRouter()` : 연결되어 있던 라우터를 제거하는 메서드
      * 다른 라우터에 연결된 라우터는 제거하지 못하게 하는 `EmptyRouterSpec` 명세 사용
      * 라우터에 연결된 스위치가 있는지 확인하는 `EmptySwitchSpec` 명세 사용
    * 코어 라우터는 다른 라우터만 처리하므로 스위치에 대한 참조를 갖지 않는다.
    * `SameCountrySpec` 명세
        ```java
        public class SameCountrySpec extends AbstractSpecification<Equipment> {

            private Equipment equipment;

            public SameCountrySpec(Equipment equipment){
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
        ```
        * `isSatisfiedBy()` 메서드 재정의
            * 코어 라우터에는 적용되지 않으므로 객체가 `CoreRouter` 이면 항상 true 반환
            * 에지 라우터이면 장비가 다른 국가에 있는지 유효성 검사 진행
        * `check()` 메서드 재정의
            * 다른 클래스들이 명세를 충족하는지 확인하기 위해 호출하는 메서드
    * `SameIpSpec` 명세
        ```java
        public class SameIpSpec extends AbstractSpecification<Equipment>{

            private Equipment equipment;

            public SameIpSpec(Equipment equipment){
                this.equipment = equipment;
            }

            @Override
            public boolean isSatisfiedBy(Equipment anyEquipment) {
                return !equipment.getIp().equals(anyEquipment.getIp());
            }

            @Override
            public void check(Equipment equipment) {
                if(!isSatisfiedBy(equipment))
                    throw new GenericSpecificationException("It's not possible to attach routers with the same IP");
            }
        }
        ```
    * `EmptyRouterSpec` 명세
        ```java
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
        ```
    * `EmptySwichSpec` 명세
        ```java
        public class EmptySwitchSpec extends AbstractSpecification<EdgeRouter> {

            @Override
            public boolean isSatisfiedBy(EdgeRouter edgeRouter) {
                return edgeRouter.getSwitches()==null ||
                        edgeRouter.getSwitches().isEmpty();
            }

            @Override
            public void check(EdgeRouter edgeRouter) {
                if(!isSatisfiedBy(edgeRouter))
                    throw new GenericSpecificationException("It isn't allowed to remove an edge router with a switch attached to it");
            }
        }
        ```

### 에지 라우터와 명세
* `EdgeRouter` 엔티티 클래스 생성
    ```java
    @Getter
    @ToString
    public class EdgeRouter extends Router {

        private Map<Id, Switch> switches;

        @Builder
        public EdgeRouter(Id id, Vendor vendor, Model model, IP ip, Location location, RouterType routerType, Map<Id, Switch> switches) {
            super(id, vendor, model, ip, location, routerType);
            this.switches = switches;
        }

        public void addSwitch(Switch anySwitch) {
            var sameCountryRouterSpec = new SameCountrySpec(this);
            var sameIpSpec = new SameIpSpec(this);

            sameCountryRouterSpec.check(anySwitch);
            sameIpSpec.check(anySwitch);

            this.switches.put(anySwitch.id,anySwitch);
        }

        public Switch removeSwitch(Switch anySwitch) {
            var emptyNetworkSpec = new EmptyNetworkSpec();
            emptyNetworkSpec.check(anySwitch);

            return this.switches.remove(anySwitch.id);
        }
    }
    ```
    * `addSwitch()` : 스위치를 에지 라우터에 연결
      * `CoreRouter` 클래스 구현에 사용한 `SameCountrySpec`, `SameIpSpec` 재사용
    * `removeSwitch()` : 스위치를 에지 라우터에서 연결 해제
      * 스위치에 연결된 네트워크가 없는지 확인하는 `EmptyNetworkSpec` 명세 사용

### 스위치 엔티티와 명세
* `Switch` 엔티티 클래스 구현
    ```java
    @Getter
    public class Switch extends Equipment {

        private SwitchType switchType;
        private List<Network> switchNetworks;

        /* 코드 생략 */
        public static Predicate<Switch> getSwitchTypePredicate(SwitchType switchType){
            return s -> s.switchType.equals(switchType);
        }
        /* 코드 생략 */
    ```
    * `getSwitchTypePredicate()` : 스위치 유형별로 스위치 컬렉션 필터
    ```java
        public boolean addNetworkToSwitch(Network network) {
            var availabilitySpec = new NetworkAvailabilitySpec(network);
            var cidrSpec = new CIDRSpecification();
            var amountSpec = new NetworkAmountSpec();

            cidrSpec.check(network.getNetworkCidr());
            availabilitySpec.check(this);
            amountSpec.check(this);

            return this.switchNetworks.add(network);
        }
    ```
    * `addNetworkToSwitch()` : 스위치에 네트워크를 추가하는 메서드
      * 네트워크가 이미 스위치에 존재하는지 확인하는 `NetworkAvailabilitySpec` 명세 사용
      * 네트워크 CIDR이 유효한지 확인하는 `CIDRSpecification` 명세 사용
      * 스위치에서 허용되는 최대 네트워크의 초과 여부를 확인하는 `NetworkAmountSpec` 명세 사용
    ```java
        public boolean removeNetworkFromSwitch(Network network){
            return this.switchNetworks.remove(network);
        }
    ```
    * `removeNetworkFromSwitch()` : 스위치에서 네트워크를 제거하는 메서드 (제약사항 없으므로 명세 사용 X)

## 도메인 서비스 정의
* 패키지명 : service
* 사용자가 네트워크 자산의 컬렉션을 관리할 수 있게 하는 도메인 서비스를 정의할 수 있다.

### 라우터 서비스
```java
public class RouterService {

    public static List<Router> filterAndRetrieveRouter(List<Router> routers, Predicate<Equipment> routerPredicate){
        return routers
                .stream()
                .filter(routerPredicate)
                .collect(Collectors.<Router>toList());
    }

    public static Router findById(Map<Id,Router> routers, Id id){
        return routers.get(id);
    }
}
```
* `filterAndRetrieveRouter()` : 라우터 컬렉션을 특정 프레디케이트로 필터링 하는 메서드
  * 라우터 리스트와 해당 리스트를 필터링하기 위한 프레디케이트를 파라미터로 전달
* `findById()` : Id 로 라우터를 조회하는 메서드

### 스위치 서비스
```java
public class SwitchService {

    public static List<Switch> filterAndRetrieveSwitch(List<Switch> switches, Predicate<Switch> switchPredicate){
        return switches
                .stream()
                .filter(switchPredicate)
                .collect(Collectors.<Switch>toList());
    }

    public static Switch findById(Map<Id,Switch> switches, Id id){
        return switches.get(id);
    }
}
```
* `filterAndRetrieveSwitch()` : 스위치 컬렉션을 특정 프레디케이트로 필터링 하는 메서드
  * 스위치 리스트와 해당 리스트를 필터링하기 위한 프레디케이트를 파라미터로 전달
* `findById()` : Id 로 스위치를 조회하는 메서드

### 네트워크 서비스
```java
public class NetworkService {

    public static List<Network> filterAndRetrieveNetworks(List<Network> networks, Predicate<Network> networkPredicate){
        return networks
                .stream()
                .filter(networkPredicate)
                .collect(Collectors.<Network>toList());
    }
}
```
* `filterAndRetrieveNetworks()` : 네트워크 컬렉션을 특정 프레디케이트로 필터링 하는 메서드
  * 네트워크 리스트와 해당 리스트를 필터링하기 위한 프레디케이트를 파라미터로 전달

## 도메인 헥사곤 테스트
* 도메인 헥사곤을 테스트하려면 도메인 헥사곤의 컴포넌트에만 의존하는 테스트를 작성해야 한다.
* 결국 다른 헥사곤들이 도메인에 의존해야 하며 그 반대가 되어서는 안 된다.
* 토폴로지 및 인벤토리 시스템의 오퍼레이션 중 네트워크 자산의 추가, 삭제, 조회에 대한 테스트를 작성해보자.
    1. 네트워크 장비를 추가하는 테스트
        * happy path
            ```java
            @Test
            public void addNetworkToSwitch(){
                var location = createLocation("US");
                var newNetwork = createTestNetwork("30.0.0.1", 8);
                var networkSwitch = createSwitch("30.0.0.0", 8, location);
                assertTrue(networkSwitch.addNetworkToSwitch(newNetwork));
            }
            ```
        * unhappy path - 스위치와 네트워크가 같은 IP 주소를 사용하여 실패
            ```java
            @Test
            public void addNetworkToSwitch_failBecauseSameNetworkAddress(){
                var location = createLocation("US");
                var newNetwork = createTestNetwork("30.0.0.0", 8);
                var networkSwitch = createSwitch("30.0.0.0", 8, location);
                assertThrows(GenericSpecificationException.class, () -> networkSwitch.addNetworkToSwitch(newNetwork));
            }
            ```
    2. 에지 라우터에 스위치를 추가하는 테스트
        * happy path
            ```java
            @Test
            public void addSwitchToEdgeRouter(){
                var location = createLocation("US");
                var networkSwitch = createSwitch("30.0.0.0", 8, location);
                var edgeRouter = createEdgeRouter(location,"30.0.0.1");

                edgeRouter.addSwitch(networkSwitch);

                assertEquals(1,edgeRouter.getSwitches().size());
            }
            ```
        * unhappy path - 에지 라우터와 스위치가 서로 다른 국가에 있어서 실패
            ```java
            @Test
            public void addSwitchToEdgeRouter_failBecauseEquipmentOfDifferentCountries(){
                var locationUS = createLocation("US");
                var locationJP = createLocation("JP");
                var networkSwitch = createSwitch("30.0.0.0", 8, locationUS);
                var edgeRouter = createEdgeRouter(locationJP,"30.0.0.1");

                assertThrows(GenericSpecificationException.class, () -> edgeRouter.addSwitch(networkSwitch));
            }
            ```
    3. 코어 라우터에 에지 라우터를 추가하는 테스트
        * happy path
            ```java
            @Test
            public void addEdgeToCoreRouter(){
                var location = createLocation("US");
                var edgeRouter = createEdgeRouter(location,"30.0.0.1");
                var coreRouter = createCoreRouter(location, "40.0.0.1");

                coreRouter.addRouter(edgeRouter);

                assertEquals(1,coreRouter.getRouters().size());
            }
            ```
        * unhappy path - 에지 라우터와 코어 라우터가 서로 다른 국가에 있어서 실패
            ```java
            @Test
            public void addEdgeToCoreRouter_failBecauseRoutersOfDifferentCountries(){
                var locationUS = createLocation("US");
                var locationJP = createLocation("JP");
                var edgeRouter = createEdgeRouter(locationUS,"30.0.0.1");
                var coreRouter = createCoreRouter(locationJP, "40.0.0.1");

                assertThrows(GenericSpecificationException.class, () -> coreRouter.addRouter(edgeRouter));
            }
            ```
    4. 코어 라우터를 또 다른 코어 라우터에 추가하는 테스트
    5. 코어 라우터에서 라우터를 제거하는 테스트, 에지 라우터에서 스위치를 제거하는 테스트, 스위치에서 네트워크를 제거하는 테스트
    6. 타입별로 라우터를 필터링하는 테스트
    7. 공급업체별로 라우터를 필터링하는 테스트
    8. `RouterService`의 `filterRouterByLocation` 메서드 테스트
    9. `RouterService`의 `filterRouterByModel` 메서드 테스트
    10. `SwitchService`의 `filterAndRetrieveSwitch` 메서드 테스트
    11. `RouterService`의 `findById` 메서드 테스트
    12. `SwitchService`의 `findById` 메서드 테스트