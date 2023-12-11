# 1-3. 포트와 유스케이스를 통한 동작 처리

# 유스케이스를 통한 소프트웨어 동작 표현

- 드라이버 액터
    - SuD(System under Discussion)행위 중 하나를 트리거하는 사람이나 시스템
- 드리븐 액터
    - SuD가 소비하는 외부 시스템

### 유스케이스

- SuD 목표, 목표를 충족하기 위한 수단이나 행위, 가능한 실패 시나리오, 발생할 경우 수행해야 할 작업 설정 기법

### 유스케이스 종류

- 형식을 갖춘 유스케이스(Fully dressed)
- 간단한 유스케이스
- BDD 기반 유스케이스

## 형식을 갖춘 유스케이스

- 액터(Actor) : 인프라 엔지니어
- 목표(Goal) : 에지 라우터에 새로운 네트워크 추가
- 범위(Scope) : 인프라 부서
- 트리거(Trigger) : 다른 네트워크를 통한 네트워크 액세스를 분리하는 특별한 이유
- 입력 데이터(Input data) : 라우터 ID, 네트워크 이름, 주소, CIDR

### Action

1. 라우터 ID를 찾는다.
2. 네트워크 주소가 이미 존재하느지 확인한다.
3. CIDR이 최소값 아래인지 확인한다.
4. 이전 검사에서 문제가 없다면 통보된 라우터에 네트워크를 추가한다.

## 간단한 타입 유스케이스

“인프라 엔지니어는 애플리케이션에 라우터 ID, 네트워크 이름, 주소 그리소 CIDR을 포함하는 요청을 보낸다. 애플리케이션은….. 네트워크를 추가한다.”

식으로 표준없이 한두 단락으로 표현

## BDD 기반 유스케이스

- 발견, 형식화, 자동화 관련된 행위 주도 설계(Behavior Driven Design) 원칙에 의존
- 요구를 발견, 상황과 행동에 대한 예제 도출 → 구조화된 문서화 → 행동 검증을 위한 테스트를 만들고 실행
- 큐컴버(Cucumber)같은 도구 활용 가능
    - 큐컴버 기능파일 만들기
    
    ```
    @addnetworkToRouter
    Feature: 라우터에 네트워크를 추가한다
    기존 라우터에 네트워크를 추가할 수 있기를 원한다.
    
    Scenario: 기존 라우터에 네트워크 추가
    	Given 라우터 아이디와 네트워크 세부 사항을 제공한다.
    	When 라우터를 발견했다.
    	And 네트워크 주소가 유효하며 기존에 존재하지 않는다.
    	And CIDR이 유효하다.
    	Then 라우터에 네트워크를 추가한다.
    ```
    
- Given, When, And, Then 단계를 기반으로 유스케이스 단계 유효성 검사를 자동화하는 테스트 클래스 작성

```
public class AddNetwordSteps {
	RouterId routerId;
	Router router;
	RouterNetworkFileAdpater routerNetworkFileAdpator =
			RouterNetworkFileAdapter.getInstance(0;
	Network network = new Network(new IP("20.01.1.1"), "marketing", 8);
	// ...

}
```

1. given
    
    ```
    @Given("I provide a router ID and the network details")
    public void obtain_routerId() {
    	//...
    }
    ```
    
2. when
    
    ```
    @When("I foudn the router")
    public void lookup_router() {
    	router = routerNetworkFileAdpater.fetchRouterById(id);
    }
    ```
    
3. and
    
    ```
    @And("The network address is valid and doesnt already exist")
    public void check_address_validity_and_existence() {
    		var availabilitySpec = new NetworkAvailabilitySpecification(
    			network.getAddress(), network.getName(), network.getCidr()
    		);
    		if (!availabilitySpec.isSatisfiedBy(router))
    			throw new IllegalArgumentException("already exist");
    }
    ```
    
4. given
    
    ```
    @Given("The CIDR is valid")
    public void check_cidr() {
    	var cirdSpec = new CIDRSpecification();
    	if (cidrSpec.isSatisfiedBy(networkd.getCidr()))
    			throw new IllegalArgumentException("CIDR is below" + CIDRSpecification.MINIMUM_ALLOWED_CIDR);
    }
    ```
    
5. then
    
    ```
    @Then("Add the network to the router")
    public void add_network() {
    	// ...
    }
    ```
    

### 유스케이스 역할

- 입력 포트 구현을 허용하는 것
- 헥사곤 서비스, 다른 유스케이스, 출력 포트를 통해 외부 리소스를 호출하는 로직은 입력 포트를 통해 이뤄진다.

### 입력 포트를 갖는 유스케이스 구현

- 드라이빙 액터는 애플리케이션에 요청을 보내는 사람
- 드리븐 액터는 애플리케이션에서 액세스하는 외부 컴포넌트를 의미
- 드라이빙 오퍼레이션 사이의 통신 흐름을 허용하기 위해 입력 포트를 사용한다.
- 유스케이스는 애플리케이션이 지원해야 하는 동작을 알려준다.
- (입력 포트는 이러한 동작의 수행 방법을 가르쳐줌)
- 입력 포트는 외부에서 들어온 것들이 도메인 헥사곤과 애플리케이션 헥사곤 방향으로 가도록 변환

### 출력 포트를 이용한 외부 데이터 처리

- 드리븐 액터와 오퍼레이션에 연결
- 드리븐 오퍼레이션은 이런 시스템과 통신하는 데 사용
- 외부 기술이 애플리케이션 설계 방법을 지시하지 않게 하자는 의미
- 외부 기술은 데이터베이스, REST api 외에도 다른 것이 될 수 있다.
    
    (구현이 아닌 인터페이스로 정의하는 이유)
    

### 리포지토리

- 익숙한 DAO, Repository 등이 헥사고날 애플리케이션에서는 출력 포트로 대체된다.
- 기본 개념은 지속성이나 모든 종류의 외부 통신이 데이터베이스 시스템에서 발생할 것이라고 추론(단정짓지?)하지 않는 것
- 모든 시스템과의 통신에 열어둔다.
- (최대한 POJO 지향..?)

### 어디에 출력 포트를 사용하는가?

- 입력 포트를 가진 유스케이스 구현시에 사용

```
public class RouterNetworkInputPort implements XXXUseCase {

	private final RouterNetworkOutputPort outputPort;

	public RouterNetworkInputPort(RouterNetworkOutputPort outputPort) {
			this.outputPort = outputPort;
	}

	@Override
	public Router addNetwordToRouter() {
		//....
	}
}
```

### 애플리케이션 헥사곤을 통한 동작 자동화

- 신용카드 확인 시스템 예시
    - 필요한 단계를 유스케이스로 표현
    - 입력 포트 구현 과정에서 필요한 단계를 출력 포트를 통해 처리
- 핵심은 요구사항 만족을 위해 필요한 기술을 지정할 필요가 없다는 것
- 나중에 개발 프레임 추가하는 것이 가능하다.
- (개발 프레임워크 자체는 소프트웨어 개발의 핵심이 아니라는 것을 깨닫게 된다….)