# 1. 왜 핵사고날 아키텍처인가?

- 비즈니스 로직이 기술 코드와 분리되고, 기술 코드에 의존하지 않고 진화할 수 있는 접근 방법
- 기술 부채를 다루는 데 도움이 된다.

## 소프트웨어 아키텍처 검토

### 보이지 않는 것들

- 수익성이 있는 소프트웨어 : 실 세계의 문제를 해결하고 비즈니스 요구를 충족하는 소프트웨어
- 보안, 유지보수성, 운영 가능성과 같은 비기능적 요구사항도 중요하다.
- 무언가를 올바르게 만들기 위해 문제 영역에 대한 깊은 이해가 필수적이다.
- 도메인 주도 설계 같은 기법은 문제에 접근하는 데 도움이 될 수 있다.

### 기술 부채

- 기술 부채 : 소프트웨어 코드 내에 존재하는 불필요한 복잡성이 얼마나 존재하는지를 설명
    - 불필요한 복잡성을 크러프트라고 하는데 현재 코드와 이상적인 코드와의 차이이다.

### 악순환

- 기술 부채의 증가를 결정하는 것은 소프트웨어의 변경 속도와 특성이다.
- 빈번하고 복잡한 변경이 있으면 기술 부채가 증가할 가능성이 높다.

### 아키텍처는 모두를 위한 것이 아니다.

- 무질서하게 성장해서 이해하거나 유지하기에는 복잡한 소프트웨어를 커다란 진흙 덩어리 라고 부른다.
- 소프트웨어 아키텍처는 문화적인 요소와 조직적인 요소에 의해 약화될 수 있다.
    - 아키텍처 원칙에 전혀 신경 쓰지 않는 팀원
- 고객 기능 면에서는 큰 가치를 더하지 않는 기술적인 측면을 처리하는 데 더 많은 시간을 쓰는 것이 장기적으로 더 유지보수하기 쉬운 소프트웨어가 된다는 것을 알아야 한다.

### 모놀리식 시스템과 분산 시스템

- 반복적으로 시스템의 컴포넌트 구성과 책임을 논의한다.
- 모놀리식 시스템은 너무 많은 책임을 축적하고 있고 유지하기에는 과도하게 복잡해서 조금만 변경해도 전체 시스템이 망가질 수 있는 심각한 위험성을 내포하고 있다.
- 소프트웨어의 규모가 너무 크면 개발자는 로컬 컴퓨터에서 소프트웨어를 실행하고 테스트하는 데 어려움을 겪을 수 있다.
- 서비스 지향 아키텍처, 마이크로서비스 아키텍처는 자율적으로 큰 모놀리식 시스템을 런타임 환경에서 격리되는 소프트웨어 구성 요소인 더 작고 관리하기 쉬운 소프트웨어 컴포넌트로 분해할 수 있다.
- 네트워크 전체에 흩어져 있는 복잡성을 처리해야 한다.
- 헥사고날 아키텍처는 모놀리식 시스템과 분산 시스템 모두에 적용할 수 있다.

### 의사결정

- 소프트웨어 코드가 잘 구성되고 유지보수 가능한 정도는 소프트웨어의 내부 품질에 해당한다.
- 소프트웨어가 얼마나 가치 있고 좋은지에 대한 가치 인식은 소프트웨어의 외부 품질에 해당한다.
- 대부분의 소프트웨어 시스템에서는 시간이 지날수록 새로운 기능을 추가하는 것이 어려워지므로 내부 품질을 잘 관리해야 한다.
- 소프트웨어 설계에 있어 중요한 포인트는 시스템 변경의 필요성과 능력이다.
- 헥사고날 아키텍처는 변화에 대처할 수 있는 변경에 강한 어플리케이션을 만들어 결정을 미룰 수 있게 해주는 소프트웨어 아키텍처이다.

## 헥사고날 아키텍처 이해

- 비즈니스 코드를 기술 코드로부터 분리하는 것
- 기술 측면이 비즈니스 측면에 의존하는지 확인해 관련된 비즈니스 코드에 피해를 주지 않고 기술 코드를 변경할 수 있어야 한다.

### 도메인 헥사곤

- 소프트웨어가 해결하기를 원하는 핵심 문제를 설명하는 요소들을 결합
- 실 세계 문제를 이해하고 모델링하는 활동
- 중요한 비즈니스 데이터와 규칙에 관련된 엔티티를 가짐
- 엔티티
    - 식별자를 할당할 수 있는 것
    - 표현력 있는 코드를 작성하는데 도움
    - 연속성과 정체성
        - 연속성 : 객체의 수명주기 및 변경 가능한 특성
        - 식별자를 가진다.

```kotlin
class Router (
    private val routerType: RouterType,
    private val routerId: RouterId,
) {
    companion object {
        fun filterRouterByType(routerType: RouterType): Predicate<Router> = if(routerType == RouterType.CORE) isCore() else isEdge()
        private fun isCore(): Predicate<Router> = Predicate { p: Router -> p.routerType == RouterType.CORE }
        private fun isEdge(): Predicate<Router> = Predicate { p -> p.routerType == RouterType.EDGE }
        fun retrieveRouter(routers: List<Router>, predicate: Predicate<Router>): List<Router> = routers.stream().filter(predicate).toList()
    }
}
```

- 값 객체
    - 엔티티들을 합성하기 위해 사용하는 불변 컴포넌트
    - 고유하게 식별하거나 객체의 정체성보다는 속성 자체에 관심이 있는 경우
    - 값 객체는 변경할 수 없게 해야한다.

```kotlin
enum class RouterType {
    EDGE,
    CORE,
}
```

### 애플리케이션 헥사곤

- 도메인 헥사곤에 있는 비즈니스 규칙을 사용, 처리하고 조정하는 역할
- 비즈니스 측면과 기술 측면 사이에 있고 양쪽과 상호작용하는 중개자 역할
- 애플리케이션 특화 작업을 추상적으로 처리하는 곳
- 도메인 비즈니스 규칙에 기반한 사용자의 의도와 기능을 표현
- 유스케이스
    - 도메인 제약사항을 지원하기 위해 시스템 동작을 애플리케이션 특화 오퍼레이션을 통해 나타냄
    - 인터페이스로 정의된 추상화

```kotlin
interface RouterViewUseCase {
    fun getRouters(filter: Predicate<Router>): List<Router>
}
```

- 입력 포트
    - 유스케이스 인터페이스를 구현

```kotlin
class RouterViewInputPort(
    private val routerListOutputPort: RouterViewOutputPort,
): RouterViewUseCase {
    override fun getRouters(filter: Predicate<Router>): List<Router> {
        val routers = routerListOutputPort.fetchRouters()
        return Router.retrieveRouter(routers, filter)
    }
}
```

- 출력 포트
    - 유스케이스나 입력 포트가 오퍼레이션을 수행하기 위해 어떤 종류의 데이터를 외부에서 가져와야 하는지를 기술에 구애받지 않고 설명하는 인터페이스

```kotlin
interface RouterViewOutputPort {
    fun fetchRouters(): List<Router>
}
```

### 프레임워크 헥사곤

- 외부 인터페이스를 제공하여 애플리케이션 기능의 노출 방법을 결정할 수 있는 곳
- 소프트웨어와 통신할 수 있는 기술을 결정
- 드라이빙 방식 - 입력 어댑터
    - 소프트웨어에 동작을 요청
    - 외부 엔티티가 시스템과 상호작용하고 외부 엔티티의 요청을 도메인 어플리케이션으로 변환하는 방법을 정의한 API를 통해 통신이 일어남

```kotlin
class RouterViewCLIAdapter() {
    private lateinit var routerViewUseCase: RouterViewUseCase

    init {
        setAdapters()
    }

    fun obtainRelatedRouters(type: String): List<Router> = routerViewUseCase.getRouters(outer.filterRouterByType(RouterType.valueOf(type)))

    private fun setAdapters() {
        this.routerViewUseCase = RouterViewInputPort(RouterViewFileAdapter::class.java.newInstance())
    }
}
```

- 드리븐 방식 - 출력 어댑터
    - 어플리케이션 내에서 호출되고 외부에서 소프트웨어 요구사항을 충족시키는 데 필요한 데이터를 가져온다.
    - 의존성 역전을 이용해서 애플리케이션 및 도메인 헥사곤에 의존하게 만든다.

```kotlin
class RouterViewFileAdapter: RouterViewOutputPort {
    override fun fetchRouters(): List<Router> = readFileAsString()

    companion object {
        private fun readFileAsString(): List<Router> {
            val routers = arrayListOf<Router>()
            BufferedReader(InputStreamReader(RouterViewFileAdapter::class.java.classLoader.getResourceAsStream("routers.txt")!!)).lines().use {
                it.forEach { line ->
                    val routerEntry = line.split(";")
                    val id = routerEntry[0]
                    val type = routerEntry[1]
                    val router = Router(RouterType.valueOf(type), RouterId.of(id))
                    routers.add(router)
                }
            }
            return routers
        }
    }
}
```

### 헥사고날 접근 방식의 장점

- 변경 허용
    - 기술 변화가 빠르게 이루어져도 헥사고날 아키텍처에서는 포트와 어댑터를 통해 기술 변화를 마찰이 적은 방향으로 흡수할 수 있다.
- 유지보수성
    - 관심사의 분리를 통해 소프트웨어 구조를 파악하는 데 예측 가능성을 보장한다.
        - 비즈니스 규칙을 변경해야 하는 경우 유일하게 변경해야 하는 곳은 도메인 헥사곤 변경
        - 애플리케이션에서 지원하지 않는 특정 기술이나 프로토콜을 사용하는 기존 기능을 고객이 트리거 할 수 있게 허용해야 하는 경우 프레임워크 헥사곤 변경
- 테스트 용이성
    - 가장 중요한 부분을 테스트하는 데 필요한 유연성을 제공한다.
