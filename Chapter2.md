# 2. 도메인 헥사곤으로 비즈니스 규칙 감싸기

- 도메인 헥사곤
    - 가장 내부에 있는 헥사곤으로 상위에 있는 어떤 헥사곤에도 의존하지 않는다.
    - 다른 모든 헥사곤이 도메인 헥사곤에 의존하는 방식으로 오퍼레이션을 수행하기 때문에 다른 헥사곤보다 도메인 헥사곤에 훨씬 높은 수준의 책임과 관련성을 부과한다.
    - 우리가 해결하고자 하는 문제를 가장 잘 표현하고 그룹화하는 모든 비즈니스 규칙과 데이터가 해당 도메인 상에 있기 때문이다.
- 도메인 주도 설계
    - 문제 영역을 모델링 하는 기법 중 하나로 비즈니스에 대한 지식을 전달하는 매개체
    - 핵심 문제 영역을 구성하는 것과 부차적인 것을 분리
        - 기술 코드와 비즈니스 코드를 분리하는 헥사고날 아키텍처의 목표를 지원하는 적합한 방법

## 엔티티를 활용한 문제 영역 모델링

- 도메인 주도 설계에서는 코드를 작성하기 전에 개발자와 비즈니스를 깊게 이해하는 도메인 전문가 사이에서 많은 논의가 필요하다.
    - 개발자와 도메인 전문가 사이의 브레인스토밍에 기반한 지식 크런칭
    - 보편 언어를 통해 지식이 통합
        - 프로젝트와 관련된 모든 사람 사이에서 공용어 역할을 하고 문서, 일상 대화, 코드에 존재
    - 엔티티를 다룰 때는 코드를 읽는 것만으로 비즈니스에 대해 얼마나 많이 배울 수 있는지 유념해야 한다.

### 도메인 엔티티의 순수성

- 문제 영역 모델링의 주된 초점은 가능한 한 정확하게 실제 시나리오를 코드로 변환하는 것
- 문제 영역 모델링의 핵심은 엔티티를 만드는 것
    - 엔티티는 비즈니스 요구사항과 밀접한 관계를 가져야 한다.
    - 기술적인 요구사항으로부터 보호하기 위해 노력해야 한다.
    - 비즈니스 관련 코드와 기술 관련 코드가 혼동되는 것을 방지하기 위해 노력해야 한다.
- 비즈니스 관심사만 처리한다는 점에서 순수해야 한다.

### 관련 엔티티

- 비즈니스 규칙과 비즈니스 데이터라는 두 요소의 존재는 관련 엔티티의 특징을 결정한다.
- 비즈니스 규칙이 누출된 엔티티는 무엇을 하는지 추측하기 어렵기 때문에 빈약한 도메인 모델이 된다.
- 모델링하려는 엔티티에 본질적이지 않은 로직으로 엔티티 클래스에 과부화를 줘서는 안된다.
    - 이런 로직들은 도메인 서비스를 사용한다.

```kotlin
class RouterSearch {
    companion object {
        fun retrieveRouter(routers: List<Router>, predicate: Predicate<Router>): List<Router> = routers.stream().filter(predicate).toList()
    }
}
```

### UUID를 이용한 식별자 정의

- 식별자의 중복 생성 및 방지를 위해 데이터베이스 시퀀스 메커니즘에 의존하는 경우가 있는데, 이는 외부 시스템과 결합을 이끈다.
- 일반적으로 식별자를 만드는 방법은 UUID를 사용하는 것이다.
    - 시간 기반
    - 분산 컴퓨터 환경 보안
    - 이름 기반
    - 무작위 생성
- 데이터 소스가 RDB이면 UUID는 문자열이기 때문에 자동 생성된 ID로 만들어진 정수보다 더 많은 메모리를 소비해서 성능 문제가 발생할 수 있다.
- 엔티티 ID는 한 번 정의하고 나면 변경하지 않는 불변 속성이므로 ID 속성을 값 객체로 모델링 할 수 있다.

```kotlin
class RouterId(
    private val id: UUID,
) {
    companion object {
        fun withId(id: String): RouterId = RouterId(UUID.fromString(id))
        fun withoutId(): RouterId = RouterId(UUID.randomUUID())
    }
}
```

- 엔티티는 헥사고날 아키텍처의 일급 객체이다.
- 도메인의 모든 것이 ID를 갖는 것은 아니기 때문에 고유하게 식별한 필요가 없는 객체를 표현하기 위해 값 객체를 사용한다.

## 값 객체를 통한 서술성 향상

- 문제 영역을 모델링 하기 위해 내장 타입만 사용하는 것은 충분치 않기 때문에 시스템의 본질과 목적을 명확하게 하기 위해 내장 타입, 사용자 정의 타입을 값 객체로 감싸야 한다.
- 값 객체의 특성
    - 불변
    - 식별자를 갖지 않는다.
- 예시 - 엔티티의 속성에 값 객체를 사용하지 않는 경우

```kotlin
class Event(
    private val id: EventId,
    private val timestamp: OffsetDateTime,
    private val protocol: String,
    private val activity: String,
): Comparable<Event> {
    override fun compareTo(other: Event): Int {
        TODO("Not yet implemented")
    }
}
```

- activity 필드의 표현력이 부족하다.

```kotlin
class Activity(
    description: String,
    private var srcHost: String,
    private var dstHost: String) {

    init {
        this.srcHost = description.split(">")[0]
        this.dstHost = description.split(">")[1]
    }
    
    fun retrieveSrcHost(): String = this.srcHost
}

class Event(
    private val id: EventId,
    private val timestamp: OffsetDateTime,
    private val protocol: String,
    private val activity: Activity,
): Comparable<Event> {
    override fun compareTo(other: Event): Int {
        TODO("Not yet implemented")
    }
}
```

## Aggregate을 통한 일관성 보장

- 관련 엔티티와 값 객체의 그룹이 함께 전체적인 개념을 설명할 때는 Aggregate을 사용한다.
- Aggregate 내부의 객체들은 일관되고 격리된 방식으로 동작한다.
- 일관성을 달성하기 위해 Aggregate 객체에 대한 모든 변경은 해당 Aggregate에 부과되는 변경 사항에 따라 결정되는 것이 보장 되어야 한다.
- Aggregate 영역과 상호작용할 진입점을 Aggregate 루트라고 하고, Aggregate의 일부인 엔티티와 값 객체들에 대한 참조를 유지한다.
- 성능과 확장성 관점에서 항상 Aggregate을 가능한 한 작게 유지하려고 노력해야 한다.
- 두 개의 서로 다른 Aggregate이 상호작용하기 위해 Aggregate 루트를 사용한다.

```kotlin
class Network (
    private val address: IP,
    private val name: String,
    private val cidr: Int,
) {
    init {
        if(cidr < 1 || cidr > 32) {
            throw IllegalArgumentException("Invalid CIDR value")
        }
    }
}

class IP (
    private val address: String,
) {
    private val protocol: Protocol

    init {
        if(address == null) {
            throw IllegalArgumentException("Null IP address")
        }

        if(address.length <= 15) {
            this.protocol = Protocol.IPV4
        } else {
            this.protocol = Protocol.IPV6
        }
    }
}

enum class Protocol {
    IPV4,
    IPV6,
}
```

```kotlin
class Switch (
    private val switchType: SwitchType,
    private val switchId: SwitchId,
    private val networks: List<Network>,
    private val address: IP,
) {

    fun addNetwork(network: Network): Switch {
        val networks = arrayListOf(network)
        networks.add(network)
        return Switch(this.switchType, this.switchId, this.networks, this.address)
    }

    fun getNetworks(): List<Network> = networks
}
```

```kotlin
class Router (
    private val routerType: RouterType,
    private val routerId: RouterId,
) {
    private lateinit var networkSwitch: Switch

    companion object {
        fun filterRouterByType(routerType: RouterType): Predicate<Router> = if(routerType == RouterType.CORE) isCore() else isEdge()
        private fun isCore(): Predicate<Router> = Predicate { p: Router -> p.routerType == RouterType.CORE }
        private fun isEdge(): Predicate<Router> = Predicate { p -> p.routerType == RouterType.EDGE }
    }

    fun addNetworkToSwitch(network: Network) {
        this.networkSwitch = networkSwitch.addNetwork(network)
    }

    fun createNetwork(address: IP, name: String, cidr: Int) = Network(address, name, cidr)

    fun retrieveNetworks() = networkSwitch.getNetworks()

    fun getRouterType() = routerType

    override fun toString(): String {
        return "Router(routerType=$routerType, routerId=$routerId, networkSwitch=$networkSwitch)"
    }
}
```

## 도메인 서비스 활용

- 문제 영역을 모델링 할 때 도메인 헥사곤, 엔티티, 값 객체, Aggregate 등 어떤 객체 범주에도 적합하지 않은 객체가 있을 수 있는데 이러한 객체를 도메인 서비스라고 부른다.
- 도메인 서비스는 가치 있는 작업을 수행하지만 문제 영역의 제한된 범위 내에서만 수행한다.

```kotlin
class NetworkOperation {
    private val MINIMUM_ALLOWED_CIDR = 8

    fun createNetwork(router: Router, address: IP, name: String, cidr: Int) {
        if(cidr < MINIMUM_ALLOWED_CIDR) {
            throw IllegalArgumentException("CIDR is below $MINIMUM_ALLOWED_CIDR")
        }

        if(isNetworkAvailable(router, address, cidr)) {
            throw IllegalArgumentException("Address already exist")
        }

        val network = router.createNetwork(address, name, cidr)
        router.addNetworkToSwitch(network)
    }

    private fun isNetworkAvailable(router: Router, address: IP, cidr: Int): Boolean {
        var availablilty = true
        for (network in router.retrieveNetworks()) {
            if(network.getAddress() == address && network.getCidr() == cidr) {
                availablilty = false
                break
            }
        }
        return availablilty
    }
}
```

- 엔티티와 값 객체에 잘 어울리지 않는 작업을 처리하는 도메인 서비스 클래스에 책임을 위임한다.

## 정책 및 명세 패턴을 활용한 비즈니스 규칙 처리

- 시스템이 가지고 있는 가장 큰 가치 중 하나는 성문화된 비즈니스 규칙이다.
- 실제 문제를 이해하고, 이해한 바를 동작하는 소프트웨어로 변환하기 위한 필수적인 노력을 나타낸다.
- 정책 패턴과 명세 패턴은 코드의 비즈니스 규칙을 더 잘 구조화하기 위한 두 가지 패턴이다.
- 정책
    - 전략으로도 알려져 있고 코드 블록의 문제 영역의 일부를 캡슐화 하는 패턴이다.
    - 제공된 데이터에 대해 어떤 작업이나 처리를 한다.
    - 커플링을 피하기 위해 의도적으로 엔티티와 값 객체를 분리해 유지한다.
- 명세
    - 객체의 특성을 보장하는 데 사용되는 조건이나 Predicate와 같다.
    - 단순한 논리적인 연산자보다는 더 표현적인 방법으로 Predicate를 캡슐화한다.
- 명세 예시

```kotlin
interface Specification<T> {
    fun isSatisfiedBy(t: T): Boolean
    fun and(specification: Specification<T>): Specification<T>
}

abstract class AbstractSpecification<T>: Specification<T> {
    abstract override fun isSatisfiedBy(t: T): Boolean
    fun and(specification: Specification<T>): Specification<T> = AndSpecification<T>(this, specification)
}

class AndSpecification<T> (
    private val spec1: Specification<T>,
    private val spec2: Specification<T>,
): AbstractSpecification<T>() {
    override fun isSatisfiedBy(t: T): Boolean = spec1.isSatisfiedBy(t) && spec2.isSatisfiedBy(t)
}

class CIDRSpecification: AbstractSpecification<Int>() {
    companion object {
        val MINIMUM_ALLOWED_CIDR = 8
    }

    override fun isSatisfiedBy(cidr: Int): Boolean = cidr > MINIMUM_ALLOWED_CIDR
}

class NetworkAvailabilitySpecification (
    private val address: IP,
    private val name: String,
    private val cidr: Int,
): AbstractSpecification<Router>() {
    override fun isSatisfiedBy(router: Router): Boolean = isNetworkAvailable(router)

    private fun isNetworkAvailable(router: Router): Boolean {
        var availablilty = true
        for (network in router.retrieveNetworks()) {
            if(network.getAddress() == address && network.getName() == name && network.getCidr() == cidr) {
                availablilty = false
                break
            }
        }
        return availablilty
    }
}

class NetworkAmountSpecification: AbstractSpecification<Router>() {
    companion object {
        val MAXIMUM_ALLOWED_NETWORKS = 6
    }
    
    override fun isSatisfiedBy(router: Router): Boolean = router.retrieveNetworks().size <= MAXIMUM_ALLOWED_NETWORKS
}

class RouterTypeSpecification: AbstractSpecification<Router>() {
    override fun isSatisfiedBy(router: Router): Boolean = router.getRouterType() == RouterType.EDGE && router.getRouterType() == RouterType.CORE
}
```

```kotlin
class NetworkOperation {
    fun createNetwork(router: Router, address: IP, name: String, cidr: Int) {
        val availabilitySpec = NetworkAvailabilitySpecification(address, name, cidr)
        val cidrSpec = CIDRSpecification()
        val routerTypeSpec = RouterTypeSpecification()
        val amountSpec = NetworkAmountSpecification()
        
        if(!cidrSpec.isSatisfiedBy(cidr)) {
            throw IllegalArgumentException("CIDR is below ${CIDRSpecification.MINIMUM_ALLOWED_CIDR}")
        }
        
        if(availabilitySpec.isSatisfiedBy(router)) {
            throw IllegalArgumentException("Address already exist")
        }
        
        if(amountSpec.isSatisfiedBy(routerTypeSpec).and(routerTypeSpec.isSatisfiedBy(router))) {
            val network = router.createNetwork(address, name, cidr)
            router.addNetworkToSwitch(network)
        }
    }
}
```

- 정책 예시

```kotlin
interface EventParser {
    val formatter: DateTimeFormatter
        get() = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").withZone(ZoneId.of("UTC"))

    fun parseEvent(event: String): Event
}

class RegEventParser: EventParser {
    override fun parseEvent(event: String): Event {
        val regex = "(\\\"[^\\\"]+\\\")|\\S+"
        val pattern = Pattern.compile(regex, Pattern.MULTILINE)
        
        val matcher = pattern.matcher(event)
        val fields = arrayListOf<String>()
        
        while(matcher.find()) {
            fields.add(matcher.group(0))
        }
        
        val timestamp = LocalDateTime.parse(matcher.group(0), formatter).atOffset(ZoneOffset.UTC)
        val id = EventId.of(matcher.group(1))
        val protocol = Protocol.valueOf(matcher.group(2))
        val activity = Activity(matcher.group(3), matcher.group(5))
        
        return Event(timestamp, id, protocol, activity)
    }
}

class SplitEventParser: EventParser {
    override fun parseEvent(event: String): Event {
        val fields = event.split(" ")
        
        val timestamp = LocalDateTime.parse(fields[0], formatter).atOffset(ZoneOffset.UTC)
        val id = EventId.of(fields[1])
        val protocol = Protocol.valueOf(fields[2])
        val activity = Activity(fields[3], fields[5])
        
        return Event(timestamp, id, protocol, activity)
    }
}

class Event(
    private val id: EventId,
    private val timestamp: OffsetDateTime,
    private val protocol: Protocol,
    private val activity: Activity,
): Comparable<Event> {
    companion object {
        fun parsedEvent(unparsedEvent: String, policy: ParsePolicyType): Event {
            return when(policy) {
                REGEX -> RegEventParser().parseEvent(unparsedEvent)
                SPLIT -> SplitEventParser().parseEvent(unparsedEvent)
                else -> throw IllegalArgumentException("Invalid policy")
            }
        }
    }

    override fun compareTo(other: Event): Int {
        TODO("Not yet implemented")
    }
}

enum class ParsePolicyType {
    REGEX,
    SPLIT,
}

class EventSearch {
    fun retrieveEvents(unparsedEvents: List<String>, policyType: ParsePolicyType): List<Event> {
        val parsedEvent = arrayListOf<Event>()
        unparsedEvents.stream().forEach { event ->
            parsedEvent.add(Event.parsedEvent(event, policyType))
        }
        return parsedEvent
    }
}
```

## POJO를 이용한 비즈니스 규칙 정의

- 과거에는 EJB 기술을 통해 트랜잭션 관리, 보안, 객체 수명주기와 같은 개발 활동을 관리하는 데 도움을 받았다.
- 하지만, EJB를 만들고 유지하려면 XML 구성 및 배포 기술자와 관련된 일이 많았고 상용구 코드가 많아서 EJB 객체를 재사용 하기 어려웠다.
- POJO는 일반적인 자바 객체이기 때문에 개발자 친화적이고 어플리케이션의 여러 부분에서 이해하고 재사용하기 쉽다.
- 동일한 POJO를 트랜잭션, 지속성, 사용자 표현 컨텍스트에서 활용할 수 있고 비즈니스 규칙도 나타낼 수 있다.
- 다른 기술적인 세부사항으로부터 도메인 객체를 보호하여 관심사의 분리에 기여한다.
