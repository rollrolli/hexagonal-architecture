# 3. 포트와 유스케이스를 통한 동작 처리

- 포트와 유스케이스 : 시스템 데이터와 비즈니스 규칙을 조정하는 역할
    - 유스케이스 : 소프트웨어에서 지원하는 동작을 정의
    - 입력 포트와 출력 포트 : 헥사고날 시스템 내의 통신 흐름을 설정

## 유스케이스를 통한 소프트웨어 동작 표현

- 소프트웨어 시스템 : 사용자나 다른 소프트웨어 시스템이 정의한 목표를 달성하기 위해 함께 동작하는 일련의 행위 집합
    - 이해당사자 또는 액터 : 소프트웨어 목표에 관심을 갖는 사람들
        - 드라이버 액터 : SuD 행위 중 하나를 트리거하는 사람이나 시스템
        - 드리븐 액터 : SuD가 소비하는 외부 시스템
- 유스케이스 : SuD 목표, 목표를 충족하기 위한 수단이나 행위, 가능한 실패 시나리오와 이들이 발생하는 경우에 수행해야 할 작업을 설정하는 기법

### 유스케이스 작성 방법

- 형식을 갖춘 유스케이스 : 입력 데이터, 가능한 행위, 유스케이스 결과에 대해 상세하고 표준화된 정보를 규정하는 글
    - 액터 : 인프라 엔지니어
    - 목표 : 에지 라우터에 새로운 네트워크를 추가
    - 범위 : 인프라 부서
    - 트리거 : 다른 네트워크를 통한 네트워크 액세스를 분리하는 특별한 이유
    - 입력 데이터 : 라우터 ID, 네트워크 이름, 주소, CIDR
    - 액션 :
        - 라우터 ID를 찾는다.
        - 네트워크 주소가 이미 존재하는지 확인한다.
        - CIDR이 최솟값 아래인지 확인한다.
        - 이전 검사에서 문제가 없다면 통보된 라우터에 네트워크를 추가한다.
- 덜 형식적이면서 간단한 타입의 유스케이스 : 정보 기록 방식에 대한 표준을 따르지 않는다.
- 자동화된 테스트로 직접 사용자의 의도를 코드로 표현 : 발견, 형식화, 자동화와 관련된 행위 주도 설계(BDD) 원칙에 의존한다.

```kotlin
@addNetworkToRouter
Feature : 라우터에 네트워크를 추가한다.
기존 라우터에 네트워크를 추가할 수 있기를 원한다.
Senario : 기존 라우터에 네트워크 추가
	Given 라우터 ID와 네트워크 세부 사항을 제공한다.
	When 라우터를 발견했다.
	And 네트워크 주소가 유효하며 기존에 존재하지 않는다.
	And CIDR이 유효하다
	Then 라우터에 네트워크를 추가한다.
```

```kotlin
class AddNetworkSteps {
	val routerId: RouterId
	val router: Router
	val routerNetworkFileAdapter = RouterNetworkFileAdapter.getInstance()
	val network = Network(IP("20.0.0.0"), "Marketing", 8)
}
```

```kotlin
@Given("I provide a router ID and the network details")
fun obtain_routerId(): Unit {
	this.routerId = RouterId.withId("~~~")
}

@When("I found the router")
fun lookup_router(): Unit {
	router = routerNetworkFileAdapter.fetchRouterById(routerId)
}

@And("The network address is valid and doesn't already exist")
fun check_address_validity_and_exsitence(): Unit {
	val availabiltySpec = NetworkAvailabilitySpecification(network.getAddress(), network.getName(), network.getCidr())
	
	if(availabilitySpec.isSatisfiedBy(router)) {
		throw IllegalArgumentException("Address already exist")
	}
}
```

```kotlin
@Given("The CIDR is valid")
fun check_cidr(): Unit {
	val cidrSpec = CIDRSpecification()
	if(cidrSpec.isSatisfiedBy(network.getCidr()) {
		throw IllegalArgumentException("CIDR is below" + CIDRSpecification.MINIMUM_ALLOWED_CIDR)
	}
}

@Then("Add the network to the router")
fun add_network(): Unit {
	router.addNetworkToSwitch(network)
}
```

- 위 유스케이스들은 시스템 행위를 설명한다는 동일한 목적을 달성하는 방법에 차이점이 있다.

```kotlin
interface RouterNetworkUseCase {
	fun addNetworkToRouter(routerId: RouterId, network: Network): Router
}
```

- 유스케이스 목표를 달성하는 다양한 방법 제공
- 구현보다는 추상적 개념에 대한 의존성 허용
- 유스케이스의 역할은 입력 포트 구현을 허용하는 것

## 입력 포트를 갖는 유스케이스 구현

- 드라이빙 액터 : 애플리케이션에 요청을 보내는 사람
- 드리븐 액터 : 애플리케이션에서 액세스하는 외부 컴포넌트
- 입력 포트
    - 드라이빙 액터와 헥사고날 시스템에 의해 노출되는 드라이빙 오퍼레이션 사이의 통신 흐름을 허용하기 위해 사용
    - 드라이빙 액터로부터 데이터가 프레임워크 헥사곤의 어댑터 중 하나를 통해 헥사고날 시스템에 도달할 때 데이터가 흐르도록 하는 파이프와 같기 때문에 통합하는 역할
    - 도메인 헥사곤으로부터 비즈니스 규칙과 통신을 위한 파이프 제공
    - 출력 포트와 어댑터를 통한 외부 시스템과의 통신도 조정
- 유스케이스는 애플리케이션이 지원해야 하는 동작을 알려주고, 입력 포트는 이러한 동작의 수행 방법을 알려준다.

```kotlin
class RouterNetworkInputPort(
	private val routerNetworkOutputPort: RouterNetworkOutputPort,
): RouterNetworkUseCase {
	override fun addNetworkToRouter(routerId: RouterId, network: Network): Router {
		val router = fetchRouter(routerId)
		return createNetwork(router, network)
	}

	private fun fetchRouter(routerId: RouterId): Router {
		return routerNetworkOutputPort.fetchRouterById(routerId)
	}

	private fun createNetwork(router: Router, network: Network): Router {
		val newRouter = NetworkOperation.createNewNetwork(router, network)
		return persistNetwork(router) ? newRouter : router
	}

	private fun persistNetwork(router: Router): Boolean {
		return routerNetworkOutputPort.persistRouter(router)
	}
}
```

```kotlin
interface RouterNetworkOutputPort {
	fun fetchRouterById(routerId: RouterId): Router
	fun persistRouter(router: Router): Boolean
}
```

- 출력 포트는 애플리케이션이 데이터를 외부 소스로부터 얻어 유지하려는 의도
- 외부 소스가 어떤 형태인지는 모르고 외부에서 데이터를 가져오려는 의도만 명시
- 입력 포트의 주된 관심사는 도메인 서비스를 통한 내부 호출과 외부 포트를 통한 외부 호출을 조정하고 데이터를 처리하는 것

## 출력 포트를 이용한 외부 데이터 처리

- 출력 포트는 보조 포트, 외부 데이터를 처리하려는 애플리케이션의 의도
- 출력 포트를 통해 시스템이 외부 세계와 통신할 수 있도록 준비
- 애플리케이션의 요구사항을 충족시키기 위해 어떤 기술이 사용될 것인지에 대한 모든 결정을 가능한 연기하면 기술적인 세부사항보다 문제 영역에 더 집중할 수 있다.

### 리포지토리만 문제가 아니다

- 데이터베이스에서 지속성과 관련된 애플리케이션의 행위를 설명하기 위해 리포지토리나 데이터 접근 객체를 사용

```kotlin
interface PasswordResetTokenRepository: JpaRepository<PasswordResetToken, Long> {
	fun findByToken(token: String): PasswordResetToken
	fun findByUser(user: User): PasswordResetToken
	fun findAllByExpiryDateLessThan(now: Date): Stream<PasswordResetToken>
	fun deleteByExpiryDateLessThan(now: Date): Unit

	@Modifying
	@Query("delete from PasswordResetToken t where t.expiryDate <= ?1")
	fun deleteAllExpiredSInce(now: Date): Unit
}
```

- 출력 포트의 기본 개념은 지속성이나 모든 종류의 외부 통신이 데이터베이스 시스템에서 발생할 것이라 추론하지 않는 것이다.

```kotlin
interface PasswordResetTokenOutputPort {
	fun findByToken(token: String): PasswordResetToken
	fun findByUser(user: User): PasswordResetToken
	fun findAllByExpiryDateLessThan(now: Date): Stream<PasswordResetToken>
	fun deleteByExpiryDateLessThan(now: Date): Unit
	fun deleteAllExpiredSince(now: Date): Unit
}
```

- 출력 포트를 POJO로 변경함으로써 특정 프레임워크에 결합하지 않도록 한다.
- 액티비티를 수행하는 데 필요한 데이터의 종류를 표현하는 것이다.
- 출력 포트로 다양한 어댑터를 연결할 수 있다.
- 출력 포트의 주된 목표는 데이터를 가져오는 방법을 지정하지 않고 어떤 종류의 데이터가 필요한지 지정하는 것이다.

### 어디에 출력 포트를 사용하는가?

```kotlin
class RouterNetworkInputPort(
	private val routerNetworkOutputPort: RouterNetworkOutputPort,
): RouterNetworkUseCase {
	override fun addNetworkToRouter(routerId: RouterId, network: Network): Router {
		val router = fetchRouter(routerId)
		return createNetwork(router, network)
	}

	private fun fetchRouter(routerId: RouterId): Router {
		return routerNetworkOutputPort.fetchRouterById(routerId)
	}

	private fun createNetwork(router: Router, network: Network): Router {
		val newRouter = NetworkOperation.createNewNetwork(router, network)
		return persistNetwork(router) ? newRouter : router
	}

	private fun persistNetwork(router: Router): Boolean {
		return routerNetworkOutputPort.persistRouter(router)
	}
}
```

- 출력 포트는 입력 포트를 가진 유스케이스를 구현할 때 명시적으로 사용

## 애플리케이션 헥사곤을 통한 동작 자동화

- 애플리케이션 헥사곤을 구현하는 한 가지 이점은 시스템의 요구사항을 만족시키기 위해 사용해야 하는 기술을 지정할 필요가 없다.
- 기술적인 세부 사항에 초점을 두지 않는 순수한 방법
