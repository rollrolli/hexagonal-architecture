# 02 도메인 헥사곤으로 비즈니스 규칙 감싸기

## 엔티티를 활용한 문제 영역 모델링
* 코드 작성 전 도메인 전문가와 많은 논의가 있어야 한다.
* 엔티티를 엔티티로 간주하려면, 엔티티가 식별자를 가져야 한다.

### 도메인 엔티티의 순수성
* 문제 영역 모델링의 핵심은 엔티티를 만드는 것이다.
* 엔티티를 만들 때는 비즈니스 요구사항과 밀접한 관계를 가지게 해야 하고, 기술적인 것으로부터는 분리해야 한다.
* 도메인 엔티니는 비즈니스 관심사만 처리한다는 점에서 순수성을 가져야 한다.
### 관련 엔티티
* 엔티티를 만들 때 데이터 부분만 표현하고 규칙은 표현하지 않는 경우가 많은데 이는 바람직하지 않다. 이러한 모델을 `빈약한 도메인 모델(anemic domain model)`이라 한다.
* 엔티티 동작에 본질적이지 않은 로직을 적용하는 것 또한 바람직하지 않다.
* 앞 장의 예제에서 `Router` 클래스에 라우터들을 필터링하고 나열하기 위해 `retrieveRouter` 메서드를 만들었다. 이 동작은 실제 세계의 라우터의 본질적인 동작인가? 또한 리스트에 라우터를 추가하기 전에 라우터 타입을 확인하는 데 사용한 제약사항도 라우터의 고유한 동작이 맞을까?
  * 이러한 검증을 라우터의 고유한 동작으로 본다면, `이 제약사항을 엔티티 클래스에 직접 포함`시키거나 `해당 제약사항을 assertion 처리 하기 위한 명세를 생성`하는 두 가지 선택지가 생긴다.
  * 다음 코드는 라우터 타입을 확인하는 제약사항을 직접 포함하고 있는 `Router` 엔티티 클래스의 예다.
    ```java
    public class Router {
        /* 코드 생략 */
        public static Predicate<Router> filterRouterByType(RouterType routerType){
            return routerType.equals(RouterType.CORE)
                    ? Router.isCore() :
                    Router.isEdge();
        }

        public static Predicate<Router> isCore(){
            return p -> p.getRouterType() == RouterType.CORE;
        }

        public static Predicate<Router> isEdge(){
            return p -> p.getRouterType() == RouterType.EDGE;
        }
        /* 코드 생략 */
    }
    ```
  * 도메인 서비스 메서드를 수용하기 위해 먼저 `RouterSearch` 라는 도메인 서비스 클래스를 만들고, 다음과 같이 `Router` 클래스에서 이 클래스로 `retrieveRouter` 메서드를 옮겨야 한다.
  * `isCore`, `isEdge`, `filterRouterByType` 제약사항 메서드는 계속 `Router` 엔티티 클래스에 있다.
  * 이제 `retrieveRouter` 메서드는 도메인 헥사곤과 다른 헥사곤에 있는 다양한 객체들이 서비스로 사용할 수 있다.
### UUID 를 이용한 식별자 정의
* 식별자 생성을 위해 데이터베이스 시퀀스 메커니즘에 의존하게 되면, 이러한 책임이 데이터베이스로 넘어가 소프트웨어의 중요 부분을 외부 시스템과 결합게 된다.
* 일반적으로는 범용적 고유 식별자(Universally Unique Identifier, UUID) 를 사용한다. 
* UUID 는 컴퓨터 시스템에서 보편적인 고유성을 보장하기 위해 널리 사용되는 128비트 숫자이다.
* UUID 생성하는 방법
  * 시간 기반
  * 분산 컴퓨터 환경 보안
  * 이름 기반
  * 무작위 생성
* 데이터 소스가 관계형 데이터베이스인 경우 성능 문제 발생할 수 있다. 문자열이므로 DB 의 자동 생성id 보다 더 많은 메모리, 용량을 소비하고 인덱스 관리에도 영향을 준다.
* 엔티티 ID 는 불변 속성이다. 이러한 불변 속성은 엔티티 ID 속성을 값 객체로 모델링 하기에 적합하게 만든다.
* 다음 코드는 `Router` 엔티티에 대한 ID 를 나타내는 값 객체 클래스이다.
    ```java
    public class RouterId {

        private final UUID id;

        private RouterId(UUID id){
            this.id = id;
        }

        public static RouterId withId(String id){
            return new RouterId(UUID.fromString(id));
        }

        public static RouterId withoutId(){
            return new RouterId(UUID.randomUUID());
        }
    }
    ```
* 엔티티는 헥사고날 아키텍처의 일급 객체다.
* 하지만 도메인의 모든 것이 ID 를 갖지는 않기 때문에 고유하게 식별할 필요가 없는 객체를 표현하기 위한 값 객체를 사용해야 한다.
  

## 값 객체를 통한 서술력 향상
* 문제 영역을 모델링하기 위해서는 프로그래밍 언어의 내장 타입(앞 예제의 `UUID`)만으로는 충분치 않기 때문에 잘 정의된 값 객체(앞 예제의 `RouterId`)로 감싸야 한다.
* 값 객체의 두 가지 기본 특성
  * 값 객체는 불변이다.
  * 값 객체는 식별자를 갖지 않는다.
* 값 객체는 폐기할 수 있어야 하고 엔티티나 다른 객체 타입을 구성하는 데 사용할 수 있는 쉽게 교체 가능한 객체여야 한다.
* 엔티티 클래스를 모델링할 때 값 객체를 사용하지 않는 예를 살펴보자.
  * `Event` 엔티티 클래스에서 네트워크 트래픽 액티비티를 나타내는 `activity`를 `String` 으로 나타내는 경우 출발지 호스트나 목적지 호스트를 조회하려는 클라이언트에게 부담을 준다.
    ```js
    var srcHost = event.getActivity().split(">")[0]
    ```
  * 이번에는 `activity`를 `Activity` 값 객체를 사용해 나타내보자.
    ```java
    public class Activity {

        private final String srcHost;
        private final String dstHost;

        public Activity (String srcHost, String dstHost){
            this.srcHost = srcHost;
            this.dstHost = dstHost;
        }

        public String retrieveSrcHost() {
            return this.srcHost;
        }
    ```
  * 출발지 호스트나 목적지 호스트를 조회하려는 클라이언트 코드가 더 명확해지고 표현력이 좋아진다.
    ```js
    var srcHost = event.getActivity().retrieveSrcHost();
    ```
* 값 객체를 통해 데이터에 대해 더 높은 유연성과 제어권을 갖기 때문에 더 응집력 있는 방법으로 도메인 모델을 표현할 수 있다.
## 애그리게잇을 통한 일관성 보장
* 관련 엔티티와 값 객체의 그룹이 함께 전체적인 개념을 설명하는 경우 `애그리게잇(aggregate)`을 사용해야 한다.
* 애그리게잇 내부의 객체들은 일관되고 격리된 방식으로 동작해야 하므로 애그리게잇 객체에 대한 모든 변경은 해당 애그리게잇에 부과되는 변경 사항에 따라 결정되어야만 한다.
* 애그리게잇은 객체의 데이터와 동작을 조정하는 오케스트레이터와 같다.
* 애그리게잇 영역과 상호작용할 진입점은 애그리게잇 루트(aggregate root)라 하는데, 애그리게잇의 일부인 엔티티와 값 객체들에 대한 참조를 유지한다.
* 애그리게잇 바운더리를 통해 바운더리 내부의 객체가 수행하는 오퍼레이션에서 더 나은 일관성을 보장할 수 있다.
* 개념적 바운더리를 낙관적 잠금, 비관적 잠금, JTA(Java Transaction API) 같은 기법으로 설정할 수도 있다.
* 성능과 확장성 관점에서 항상 애그리게잇은 가능한 한 작게 유지하려고 노력해야 한다. 큰 애그리게잇 객체는 더 많은 메모리를 사용해 JVM 성능을 저하시킬 수 있기 때문이다.
* 애그리게잇의 특징
  * 작은 애그리게잇은 애그리게잇 루트로 동작하는 엔티티와 다른 값 객체들을 포함한다.
  * 두 개의 서로 다른 애그리게잇이 상호작용하게 만드는 방법은 애그리게잇 루트(고유 ID를 갖는 엔티티 루트)를 사용하는 것이다.
  * 애그리게잇 루트를 통해 자식 객체를 변경할 수 있다. 변경 후에는 같은 애그리게잇 루트를 사용해 지속성 시스템에 반영한다.
* 애그리게잇 모델링 방법
  * 예시: 에지 라우터 - 레벨 3 스위치 - 여러 VLAN 네트워크
  * 애그리게잇의 진원지는 애그리게잇 루트가 되는 에지 라우터 엔티티다.
  * 에지 라우터 엔티티 아래에는 스위치 엔티티가 있고 VLAN 네트워크 값 객체로 모델링한다.
  * 제일 아래 수준부터 `Network` 를 값 객체로 갖는다. 
    ```java
    public class Network {

        private final IP address;
        private final String name;
        private final int cidr;

        public Network(IP address, String name, int cidr){
            if(cidr <1 || cidr>32){
                throw new IllegalArgumentException("Invalid CIDR value");
            }
            this.address = address;
            this.name= name;
            this.cidr = cidr;
        }
    }
    ```
  * `IP` 주소 속성도 값 객체다.
    ```java
    public class IP {

        private final String address;
        private final Protocol protocol;

        private IP(String address){
        if(address == null)
            throw new IllegalArgumentException("Null IP address");
        this.address = address;
        if(address.length()<=15) {
            this.protocol = Protocol.IPV4;
        } else {
            this.protocol = Protocol.IPV6;
        }
        }
    }
    ```
  * `IP`와 `Network` 값 객체 클래스의 생성자에 유효성 검사 규칙이 있다.
  * `Protocol` enum 값 객체도 있다.
    ```java
    public enum Protocol {
        IPV4,
        IPV6;
    }
    ```
  * `IP`, `Network`, `Protocol` 값 객체 모델링이 끝났으니 `Switch` 클래스의 모델링을 해보자.
    ```java
    public class Switch {

        private SwitchType switchType;
        private SwitchId switchId;
        private List<Network> networks;
        private IP address;

        public Switch (SwitchType switchType, SwitchId switchId, List<Network> networks, IP address){
            this.switchType = switchType;
            this.switchId = switchId;
            this.networks = networks;
            this.address = address;
        }

        public Switch addNetwork(Network network){
            var networks = new ArrayList<>(Arrays.asList(network));
            networks.add(network);
            return new Switch(this.switchType, this.switchId, networks, this.address);
        }

        public List<Network> getNetworks() {
            return networks;
        }
    }
    ```
  * `addNetwork()`: 스위치에 더 많은 네트워크를 추가하는 기능 (이 메서드는 새로운 `Switch` 객체를 리턴한다.)
  * `Router` 엔티티 클래스를 만들어 지금까지 생성한 값 객체를 토대로 애그리게잇 루트를 갖는 바운더리를 만든다.
    ```java
    public class Router {

        private final RouterType routerType;
        private final RouterId routerid;
        private Switch networkSwitch;

        public Router(RouterType routerType, RouterId routerid){
            this.routerType = routerType;
            this.routerid = routerid;
        }

        public static Predicate<Router> filterRouterByType(RouterType routerType){
            return routerType.equals(RouterType.CORE)
                    ? Router.isCore() :
                    Router.isEdge();
        }

        public static Predicate<Router> isCore(){
            return p -> p.getRouterType() == RouterType.CORE;
        }

        public static Predicate<Router> isEdge(){
            return p -> p.getRouterType() == RouterType.EDGE;
        }

        public void addNetworkToSwitch(Network network){
            this.networkSwitch = networkSwitch.addNetwork(network);
        }

        public Network createNetwork(IP address, String name, int cidr){
            return new Network(address, name, cidr);
        }

        public List<Network> retrieveNetworks(){
            return networkSwitch.getNetworks();
        }

        public RouterType getRouterType(){
            return routerType;
        }
    }
    ```
  * `createNetwork` (새로운 네트워크를 생성하는 메서드), `addNetworkToSwitch` (스위치에 기존 네트워크를 연결하는 메서드) 와 같은 메서드를 애그리게잇 루트에 두면 
    * 해당 컨텍스트 하위의 모든 객체를 처리하는 책임을 애그리게잇 루트에 위임하게 된다.
    * 이러한 객체 집합을 다루는 경우 일관성이 향상된다.
    * 어떤 종류의 동작도 없는 빈약한 도메인 모델을 방지할 수 있다.

## 도메인 서비스 활용
* 라우터 항목 조회를 담당하는 메서드를 `Router` 엔티티에서 제거했었다. 이와 같은 상황을 처리하기 위해 라우터를 리스트로 만드는 메서드를 별도의 객체(`도메인 서비스, Domain Service`)로 리팩토링 했다. 
* 도메인 서비스
  * MVC 아키텍처에서 도메인 서비스는 애플리케이션의 서로 다른 측면을 연결하고 데이터를 처리하며, 시스템의 내부와 외부에서 호출을 조정하는 다리 역할을 한다.
  * Spring 의 `@Service` 애노테이션
* 도메인 서비스는 문제 영역의 제한된 범위 내에서만 수행한다. 이는 도메인 서비스가 애플리케이션 헥사곤이나 프레임워크 헥사곤에서 동작하는 서비스나 다른 객체를 호출해서는 안된다는 의미이다.
* 반면, 애플리케이션 헥사곤이나 프레임워크 헥사곤의 객체들은 도메인 서비스를 호출하는 클라이언트들이다.
* 다음 코드는 `createNetwork`, `addNetworkToSwitch` 메서드를 사용하는 서비스 클래스이다.
    ```java
    public class NetworkOperation {

        final private int MINIMUM_ALLOWED_CIDR = 8;

        public void createNewNetwork(Router router, IP address, String name, int cidr) {
            if(cidr < MINIMUM_ALLOWED_CIDR)
                throw new IllegalArgumentException("CIDR is below " + MINIMUM_ALLOWED_CIDR);

            if(isNetworkAvailable(router, address, cidr))
                throw new IllegalArgumentException("Address already exist");

            Network newNetwork = router.createNetwork(address, name, cidr);
            router.addNetworkToSwitch(newNetwork);
        }

        private boolean isNetworkAvailable(Router router, IP address, int cidr) {
            var availability = true;
            for (Network network : router.retrieveNetworks()) {
                if (network.getAddress().equals(address) && network.getCidr() == cidr) {
                    availability = false;
                    break;
                }
            }
            return availability;
        }
    }
    ```
* 이와 같은 방식으로, 엔티티와 값 객체에 잘 어울리지 않는 작업을 처리하는 `NetworkOperation` 도메인 서비스 클래스에 책임을 위임한다.
* 이것은 엔티티와 값 객체 클래스가 문제 영역을 따라 필요 이상으로 많은 기능을 가지고 너무 커지는 것을 방지하기 위한 것이다.

## 정책 패턴과 명세 패턴을 활용한 비즈니스 규칙 처리
* 비즈니스 규칙을 소프트웨어로 변환하기 위한 두 가지 패턴 - 정책 패턴, 명세 패턴
* `정책(Policy)`
  * 전략 패턴이라고도 한다.
  * 코드 블록으로 문제 영역의 일부를 캡슐화하는 전략이다.
  * 제공된 데이터에 대해 어떤 작업이나 처리를 한다.
  * 커플링을 피하기 위해 의도적으로 엔티티와 값 객체를 분리해 유지한다.
  * 이러한 디커플링은 직접적인 영향이나 부작용 없이 한 부분을 발전시킨다.
* `명세(Specification)`
  * 객체의 특성을 보장하는 데 사용되는 조건(condition) 이나 predicate 와 같다.
  * 단순한 논리적인 연산자보다는 더 표현적인 방법으로 predicate 를 캡슐화한다.
  * 이러한 명세들을 캡슐화하면 재사용할 수 있고 함께 결합해서 문제를 더 잘 표현할 수 있다.
* 정책과 명세를 같이 사용하면 비즈니스 규칙의 견고성과 일관성을 향상시킨다. 명세는 정책에 적합한 개체만 처리되는 것을 보장한다.
* 명세를 사용해 `NetworkOperation` 서비스 클래스를 리팩토링해보자.
* 먼저 `Specification` 인터페이스를 만든다.
  ```java
  public interface Specification<T> {

      boolean isSatisfiedBy(T t);

      Specification<T> and(Specification<T> specification);
  }
  ```
* `isSatisfiedBy` 구현을 통해 predicate 를 정의하기 위해 다른 명세와 결합할 수 있도록 하는 `and` 메서드를 구현하는 추상 클래스를 만든다.
    ```java
    public abstract class AbstractSpecification<T> implements Specification<T> {

        public abstract boolean isSatisfiedBy(T t);

        public Specification<T> and(final Specification<T> specification) {
            return new AndSpecification<T>(this, specification);
        }
    }
    ```
* 기본 타입에 대한 생성을 마치기 위해 다음과 같이 `AndSpecification` 클래스를 구현한다.
    ```java
    public class AndSpecification<T> extends AbstractSpecification<T> {

        private Specification<T> spec1;
        private Specification<T> spec2;

        public AndSpecification(final Specification<T> spec1, final Specification<T> spec2) {
            this.spec1 = spec1;
            this.spec2 = spec2;
        }

        public boolean isSatisfiedBy(final T t) {
            return spec1.isSatisfiedBy(t) && spec2.isSatisfiedBy(t);
        }
    }
    ```
* 자체적인 명세를 생성해보자.
* 첫 번째 명세는 새로운 네트워크 생성에 허용되는 최소 cidr 을 제한하는 비즈니스 규칙이다.
    ```java
    if(cidr < MINIMUM_ALLOWED_CIDR)
        throw new IllegalArgumentException("CIDR is below " + MINIMUM_ALLOWED_CIDR);
    ```
    ```java
    public class CIDRSpecification extends AbstractSpecification<Integer> {

        final static public int MINIMUM_ALLOWED_CIDR = 8;

        @Override
        public boolean isSatisfiedBy(Integer cidr) {
            return cidr > MINIMUM_ALLOWED_CIDR;
        }
    }
    ```
* 두 번째 명세는 네트워크 주소가 이미 사용되고 있는지 검사하는 비즈니스 규칙이다.
    ```java
        if(isNetworkAvailable(router, address, cidr))
            throw new IllegalArgumentException("Address already exist");
    ...
    private boolean isNetworkAvailable(Router router, IP address, int cidr) {
        var availability = true;
        for (Network network : router.retrieveNetworks()) {
            if (network.getAddress().equals(address) && network.getCidr() == cidr) {
                availability = false;
                break;
            }
        }
        return availability;
    }
    ```
    ```java
    public class NetworkAvailabilitySpecification extends AbstractSpecification<Router> {

        private IP address;
        private String name;
        private int cidr;

        public NetworkAvailabilitySpecification(IP address, String name, int cidr) {
            this.address = address;
            this.name = name;
            this.cidr = cidr;
        }

        @Override
        public boolean isSatisfiedBy(Router router) {
            return router!=null && isNetworkAvailable(router);
        }

        private boolean isNetworkAvailable(Router router){
            var availability = true;
            for (Network network : router.retrieveNetworks()) {
                if(network.getAddress().equals(address) && network.getName().equals(name) && network.getCidr() == cidr)
                    availability = false;
                break;
            }
            return availability;
        }
    }
    ```
* 세 번째 명세는 최대 허용 네트워크를 설정하면서 에지 라우터 또는 코어 라우터만 처리하는 비즈니스 규칙이다. `and` 메서드를 통해 두 명세를 결합하는 방법을 확인해 보자. 
    ```java
    public class NetworkAmountSpecification extends AbstractSpecification<Router> {

        final static public int MAXIMUM_ALLOWED_NETWORKS = 6;

        @Override
        public boolean isSatisfiedBy(Router router) {
            return router.retrieveNetworks().size() <=MAXIMUM_ALLOWED_NETWORKS;
        }
    }
    ```
    ```java
    public class RouterTypeSpecification extends AbstractSpecification<Router> {

        @Override
        public boolean isSatisfiedBy(Router router) {
            return router.getRouterType().equals(RouterType.EDGE) || router.getRouterType().equals(RouterType.CORE);
        }
    }
    ```
* 이제 이러한 명세들을 사용하기 위해 다음과 같이 새로운 네트워크의 생성을 담당하는 도메인 서비스를 리팩토링해보자.
    ```java
    public class NetworkOperation {

        public static Router createNewNetwork(Router router, Network network) {
            var availabilitySpec = new NetworkAvailabilitySpecification(network.getAddress(), network.getName(), network.getCidr());
            var cidrSpec = new CIDRSpecification();
            var routerTypeSpec = new RouterTypeSpecification();
            var amountSpec = new NetworkAmountSpecification();

            if(cidrSpec.isSatisfiedBy(network.getCidr()))
                throw new IllegalArgumentException("CIDR is below "+CIDRSpecification.MINIMUM_ALLOWED_CIDR);

            if(!availabilitySpec.isSatisfiedBy(router))
                throw new IllegalArgumentException("Address already exist");

            if(amountSpec.and(routerTypeSpec).isSatisfiedBy(router)) {
                Network newNetwork = router.createNetwork(network.getAddress(), network.getName(), network.getCidr());
                router.addNetworkToSwitch(newNetwork);
            }
            return router;
        }
    }
    ```
* 정책이 동작하는 방법을 이해하기 위해 원시 이벤트 데이터를 특정 알고리즘 기반으로 파싱해서 네트워크 이벤트 리스트를 추출하는 서비스 클래스를 만들 것이다.    
* 만들고자 하는 두 가지 정책
  * 정규 표현식 기반 알고리즘을 사용해 `Event` 객체로 문자열 로그 항목 파싱
  * 공백 구분 기호만으로 분할 하는 알고리즘을 사용해 `Event` 객체로 문자열 로그 항목 파싱
* `EventParser` 인터페이스 생성
    ```java
    public interface EventParser {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").withZone(ZoneId.of("UTC"));

        Event parseEvent(String event);
    }
    ```
* 정규식 파서 정책 구현
    ```java
    public class RegexEventParser implements EventParser{

        @Override
        public Event parseEvent(String event) {
            final String regex = "(\\\"[^\\\"]+\\\")|\\S+";
            final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
            final Matcher matcher = pattern.matcher(event);

            var fields = new ArrayList<>();
            while (matcher.find()) {
                fields.add(matcher.group(0));
            }

            var timestamp = LocalDateTime.parse(matcher.group(0), formatter).atOffset(ZoneOffset.UTC);
            var id = EventId.of(matcher.group(1));
            var protocol = Protocol.valueOf(matcher.group(2));
            var activity = new Activity(matcher.group(3), matcher.group(5));

            return new Event(timestamp, id, protocol, activity);
        }
    }
    ```
* 분할 파서 정책 구현
    ```java
    public class SplitEventParser implements EventParser{
        @Override
        public Event parseEvent(String event) {
            var fields = Arrays.asList(event.split(" "));

            var timestamp = LocalDateTime.parse(fields.get(0), formatter).atOffset(ZoneOffset.UTC);
            var id = EventId.of(fields.get(1));
            var protocol = Protocol.valueOf(fields.get(2));
            var activity = new Activity(fields.get(3), fields.get(5));

            return new Event(timestamp,id, protocol, activity);
        }
    }
    ```
* 정책과 함께 동작할 수 있도록 `Event` 엔티티 클래스를 업데이트한다.
    ```java
    public class Event implements Comparable<Event> {
        /* 코드 생략 */
        public static Event parsedEvent(String unparsedEvent, ParsePolicyType policy){
            switch (policy) {
                case REGEX:
                    return new RegexEventParser().parseEvent(unparsedEvent);
                case SPLIT:
                    return new SplitEventParser().parseEvent(unparsedEvent);
                default: throw  new IllegalArgumentException("");
            }
        }
        /* 코드 생략 */
    }
    ```
* 두 가지 정책 중에서 하나를 선택할 수 있는 스위치는 열거형에 만들어 둔다.
    ```java
    public enum ParsePolicyType {
        REGEX,
        SPLIT;
    }
    ```
* 이제 네트워크 이벤트를 검색하는 메서드를 갖는 `EventSearch` 서비스 클래스를 만들자.
    ```java
    public class EventSearch {

        public List<Event> retrieveEvents(List<String> unparsedEvents, ParsePolicyType policyType){
            var parsedEvents = new ArrayList<Event>();
            unparsedEvents.stream().forEach(event ->{
                parsedEvents.add(Event.parsedEvent(event, policyType));
            });
            return parsedEvents;
        }
    }
    ```

## POJO 를 통한 비즈니스 규칙 정의
* EJB 의 단점
  * 첫 버전에서 EJB 를 만들고 유지하려면 시간이 많이 들고 지루한 작업을 해야 한다.
  * 다양한 XML 구성 및 배포 기술자 관련 일이 많다.
  * boilerplate 가 너무 많아서 EJB 객체 재사용이 거의 불가능하다.
* EJB 의 단점을 해결하고 POJO 의 단순성을 이용하는 솔루션들이 나왔다. (EJB 3, Spring, Quarkus, ...)
* POJO 의 장점
  * 일반적인 자바 객체에 불과하기 때문에 이해하기 쉽다.
  * 애플리케이션의 여러 부분에서 이해하고 재사용하기가 더 쉽다.
  * 변경을 허용할 수 있는 애플리케이션에서 사용하기 좋다.
  * 애플리케이션이 다양한 기술이나 프레임워크 사이를 전환할 수 있게 해준다. 즉, 특정 기술과의 결합도가 낮다.
  * 필요한 경우 동시에 다른 시스템 부서들이 트랜잭션, 지속성, 사용자 표현 컨텍스트에서 동일한 POJO 를 사용할 수 있다.
  * POJO 를 사용해 비즈니스 규칙도 나타낼 수 있다. (이번 장의 엔티티, 정책, 명세 객체)
  * 다른 기술적인 세부사항으로부터 도메인 객체를 보호할 수 있다.

## 요약