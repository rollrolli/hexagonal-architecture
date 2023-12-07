# 03 포트와 유스케이스를 통한 동작 처리

## 유스케이스를 통한 소프트웨어 동작 표현
* 소프트웨어의 동작은 단독, 혹은 다른 소프트웨어 동작과 결합해 사용자나 시스템이 관심을 갖고 표현하는 요구사항을 실현하는 데 기여한다.
* 이처럼 관심을 갖는 사람들을 이해당사자(stakehoder)나 액터(actor)로 분류할 수 있다.
* 헥사고날 아키텍처에서 드라이버 액터(driver actor)는 SuD(System under Discussion) 행위 중 하나를 트리거하는 사람이나 시스템이다.
* 헥사고날 아키텍처에서 드리븐 액터(driver actor)는 SuD(System under Discussion)가 소비하는 외부 시스템이다.
* 유스케이스
  * SuD 행위의 상세한 글로 작성된 디스크립션을 제공하는 심층 분석을 수행한다.
  * SuD 목표, 목표를 충족하기 위한 수단이나 행위, 가능한 실패 시나리오와 해당 경우에 수행해야 할 작업을 설정한다.
  * 관심사의 분리를 위한 중요한 부분

## 유스케이스 작성 방법
* 형식을 갖춘 유스케이스<sup>fully dressed usecase</sup> (형식을 갖춘 작성 기법)
  * 액터(Actor): 인프라 엔지니어
  * 목표(Goal): 에지 라우터에 새로운 네트워크를 추가
  * 범위(Scope): 인프라 부서
  * 트리거(Trigger): 다른 네트워크를 통한 네트워크 액세스를 분리하는 특별한 이유
  * 입력 데이터(Input data): 라우터 ID, 네트워크 이름, 주소, CIDR
  * 액션(Actions)
    1. 라우터 ID를 찾는다.
    2. 네트워크 주소가 이미 존재하는지 확인한다.
    3. CIDR이 최솟값 아래인지 확인한다.
    4. 이전 검사에서 문제가 없다면 통보된 라우터에 네트워크를 추가한다.
* 간단한 타입의 유스케이스 (간단한 작성 기법)
  * 정보 기록 방식의 표준을 따르지 않는다.
  * 한 두 단락으로 가능한 한 많은 의미를 전달하려 한다.
* BDD 기반의 유스케이스 (자동화된 테스트로 직접 사용자의 의도를 코드로 표현하는 기법)
  * 발견(discovery), 형식화(formulation), 자동화(automation)와 관련된 행위 주도 설계(BDD: Behavior Driven Design) 원칙에 의존한다.
    1. 발견: 비즈니스 관련자의 요구를 발견하기 위해 그들과 이야기하고 그 결과를 비즈니스 요구를 기술하는 상황과 행동에 대한 예제로 만든다.
    2. 형식화: 그 예제로 구조화된 문서를 만든다.
    3. 자동화: 이전 단계의 예제에서 나온 행동을 검증하기 위해 테스트가 만들어지고 실행된다.
  * 큐컴버(Cucumber) 같은 도구를 통해 헥사고날 애플리케이션에 BDD 방식을 적용할 수 있다.
    1. 큐컴버의 기능 파일(Feature file)을 생성하면 글로 작성한 유스케이스를 변환할 수 있다.
        ```
        Feature: 라우터에 네트워크 추가
        Scenario: 기존 라우터에 네트워크 추가
            Given 라우터 ID, 네트워크 세부 사항
            When 라우터 발견
            And 네트워크 주소 유효, 기존에 존재 x
            And CIDR 유효
            Then 라우터에 네트워크 추가
        ```
    2. 기능 파일을 통해 유스케이스 단계의 유효성 검사를 자동화하는 테스트 클래스를 작성한다.
    3. 먼저 테스트 수행에 사용할 객체를 선언하고 초기화한다.
        ```java
        public class AddNetworkSteps {

            RouterId routerId;

            Router router;

            RouterNetworkFileAdapter routerNetworkFileAdapter =  RouterNetworkFileAdapter.getInstance();

            Network network = new Network(new IP("20.0.0.0"), "Marketing", 8);
        
            /* 코드 생략 */
        }
        ```
    4. 테스트를 구현한다.
        ```java
        @Given("I provide a router ID and the network details")
        public void obtain_routerId() {
            this.routerId = RouterId.withId("ca23800e-9b5a-11eb-a8b3-0242ac130003");
        }

        @When("I found the router")
        public void lookup_router() {
            router = routerNetworkFileAdapter.fetchRouterById(routerId);
        }

        @And("The network address is valid and doesn't already exists")
        public void check_address_validity_and_existence() {
            var availabilitySpec = new NetworkAvailabilitySpecification(network.getAddress(), network.getName(), network.getCidr());
            if(!availabilitySpec.isSatisfiedBy(router))
                throw new IllegalArgumentException("Address already exist");
        }

        @And("The CIDR is valid")
        public void check_cidr() {
            var cidrSpec = new CIDRSpecification();
            if(cidrSpec.isSatisfiedBy(network.getCidr()))
                throw new IllegalArgumentException("CIDR is below "+CIDRSpecification.MINIMUM_ALLOWED_CIDR);
        }

        @Then("Add the network to the router")
        public void add_network() {
            router.addNetworkToSwitch(network);
        }
        ```
* 완전한 형식을 갖춘 유스케이스, 간단한 유스케이스, BDD 기반의 유스케이스의 공통점과 차이점
  * 모두 같은 것을 표현하기 때문에 `대상(what)`은 같지만 시스템 행위를 설명한다는 동일한 목적을 달성하는 `방법(how)`이 다르다.
* 이러한 형식적인 유스케이스 구성은 필수는 아니지만, 아이디어를 명확히 하고 조직화하기에 좋다.
* 헥사고날 아키텍처에서는 유스케이스를 구현보다는 추상적 개념으로 설계한다. (인터페이스 혹은 추상 클래스를 사용)
* 유스케이스를 인터페이스로 정의하는 이유
  * 유스케이스 목표를 달성하는 다양한 방법 제공
  * 구현보다는 추상적 개념에 대한 의존성 허용
* 헥사고날 아키텍처에서 유스케이스의 역할 - 입력 포트 구현을 허용하는 것

## 입력 포트를 갖는 유스케이스 구현
* 드라이빙 액터와 드라이빙 오퍼레이션 사이의 통신 흐름을 허용하기 위해 **입력 포트**를 사용한다.
* 입력 포트
  * 드라이빙 액터로부터 데이터가 프레임워크 헥사곤의 어댑터 중 하나를 통해 헥사고날 시스템에 도달할 때 데이터가 흐르도록 하는 파이프와 같은 역할
  * 출력 포트와 출력 어댑터를 통한 외부 시스템과의 통신도 조정한다.
  * 외부에서 들어온 것들이 도메인 헥사곤과 애플리케이션 헥사곤 방향으로 가도록 변환한다.
* 유스케이스 인터페이스 정의
    ```java
    public interface RouterNetworkUseCase {
        Router addNetworkToRouter(RouterId routerId, Network network);
    }
    ```
* 출력 포트 인터페이스 정의
    ```java
    public interface RouterNetworkOutputPort {
        Router fetchRouterById(RouterId routerId);
        boolean persistRouter(Router router);
    }
    ```
    * 이 출력 포트는 애플리케이션이 데이터를 외부 소스로부터 얻어 유지하려는 의도를 나타낸다.
    * 헥사곤 시스템은 외부 소스가 데이터베이스나 플랫 파일, 또는 다른 시스템인지 항상 알지 못한다. 따라서 출력 포트에는 외부에서 데이터를 가져오려는 **의도**만 표시된다.
* 유스케이스를 구현한 입력 포트
    ```java
    public class RouterNetworkInputPort implements RouterNetworkUseCase {

        private final RouterNetworkOutputPort routerNetworkOutputPort;

        public RouterNetworkInputPort(RouterNetworkOutputPort routerNetworkOutputPort){
            this.routerNetworkOutputPort = routerNetworkOutputPort;
        }

        @Override
        public Router addNetworkToRouter(RouterId routerId, Network network) {
            var router = fetchRouter(routerId);
            return createNetwork(router, network);
        }
        
        private Router fetchRouter(RouterId routerId) {
            return routerNetworkOutputPort.fetchRouterById(routerId);
        }

        private Router createNetwork(Router router, Network network) {
            var newRouter = NetworkOperation.createNewNetwork(router, network);
            return persistNetwork(router) ? newRouter :
                    router;
        }

        private boolean persistNetwork(Router router) {
            return routerNetworkOutputPort.persistRouter(router);
        }
    }
    ```
    * `fetchRouter`: 출력 포트를 구현하는 출력 어댑터에 의해 수행될 외부 호출을 조정한다.
    * `createNetwork`: `NetworkOperation.createNewNetwork`라는 도메인 서비스와 상호작용한다.
    * `persistNetwork`: 전체 오퍼레이션의 지속성을 조정한다.
    * 이 입력 포트의 주된 관심사는 도메인 서비스를 통한 내부 호출, 외부 포트를 통한 외부 호출 조정 및 데이터 처리이다.
    * 오퍼레이션의 실행 순서를 설정하고 도메인 헥사곤이 이해할 수 있는 형식으로 데이터를 제공한다.
    * 외부 호출은 외부 시스템으로 데이터를 전송하거나 외부 시스템에 데이터를 보관하기 위해 헥사고날 애플리케이션이 수행하는 상호작용이다.

## 출력 포트를 갖는 외부 데이터 처리
* 보조 포트(secondary port)로도 알려져 있다.
* 출력 포트는 외부 데이터를 처리하려는 애플리케이션의 의도를 나타낸다.
* 외부 세계와의 통신을 위해 출력 포트를 드리븐 액터와 오퍼레이션에 연결할 수 있다.
* 드리븐 액터는 외부 시스템이지만 드리븐 오퍼레이션은 이러한 드리븐 액터와 통신하는 데 사용된다.
* > 로버트 마틴 "애플리케이션의 요구사항을 충족시키기 위해 어떤 기술이 사용될 것인지에 대한 모든 결정을 가능한 한 연기하라."

### 리포지토리만 문제가 아니다
* 헥사고날 아키텍처에서는 리포지토리를 출력 포트로 대체한다.
* 출력 포트의 기본 개념은 지속성이나 모든 종류의 외부 통신이 데이터베이스 시스템에서 발생할 것이라 단정짓지 않는 것이다.
* 출력 포트의 범위는 데이터베이스, 메시징 시스템, 로컬 파일 시스템, 네트워크 파일 시스템 등 어떠한 시스템도 될 수 있다.
* 출력 포트로 다양한 어댑터를 연결할 수 있다. 이러한 어댑터는 출력 포트에 표현된 대로 데이터를 얻기 위해 필요한 작업을 수행한다.
* 출력 포트의 주된 목표는 데이터를 가져오는 방법을 지정하지 않고 어떤 종류의 데이터가 필요한지 지정하는 것이다. 이것이 구현이 아닌 인터페이스를 지정하는 이유다.

### 어디에 출력 포트를 사용하는가?
* 출력 포트를 사용하는 이유는 유스케이스가 어떤 목표를 달성하기 위함이다. 
* 코드 상에서 출력 포트에 대한 참조는 유스케이스의 인터페이스 선언에는 나타나지 않는다. 출력 포트는 입력 포트를 가진 유스케이스를 구현할 때 명시적으로 사용된다.
* 출력 포트에 대한 인스턴스는 출력 어댑터가 제공하는 구현이다.
* 유스케이스에서 정의되고 입력 포트에 의해 구현되는 오퍼레이션 중 일부 오퍼레이션은 외부 소스에서 데이터를 가져오거나 데이터를 유지하는 역할을 하기 때문에 유스케이스의 구현인 입력 포트에 출력 포트가 필요한 것이다.

## 애플리케이션 헥사곤을 통한 동작 자동화
* 신용카드 확인 시스템
  * 유스케이스 - 카드 소지자의 신용을 확인하는 데 필요한 단계들을 표현
  * 입력 포트 - 유스케이스 목표를 이루기 위해 비즈니스 규칙과 필요한 모든 데이터, 소비 처리, 출력 포트를 통한 외부 시스템에 대한 처리
  * 출력 포트 - 외부 시스템에 대한 처리 정의
* 애플리케이션 헥사곤의 장점 - 시스템의 요구사항을 만족시키기 위해 사용해야 하는 기술을 지정할 필요가 없다.

## 요약