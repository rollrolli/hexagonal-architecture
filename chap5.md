# 드라이빙 오퍼레이션 / 드리븐 오퍼레이션

헥사고날 시스템을 둘러싼 환경
헥사고날 애플리케이션과 상호작용하는 외부 요소

## 드라이빙 오퍼레이션을 통한 헥사고날 호출
 - 여기선 프론트앤드 애플리케이션이 액터로 작용
 - 헥사고날 아키텍쳐 시점으로 보면 시스템의 입력 측은 드라이빙 오퍼레이션에 의해 제어
 - 애플리케이션을 동작하게 되기 떄문에 드라이빙으로 불림
 - UI / Server / CLI 등

### 웹 어플리케이션을 헥사고날 시스템에 통합
 - 기술의 발전에 따라 FE가 나오면서, 프런트와 백을 가까이 두는것은 엔트로피의 원인이 될 수 있다.
 - 그래서 최근은 당연하게 분리하는 추세

```kotlin
data class RouterId(val routerId: String)
class Router() {
    constructor(routerId: RouterId): this()
}

interface RouterNetworkOutputPort {
    fun fetchRouterById(routerId: RouterId): Router 
}

interface RouterNetworkUseCase {
    fun getRouter(routerId: RouterId): Router
}

class RouterNetworkInputPort(
    val routerNetworkOutputPort: RouterNetworkOutputPort // interface 
) : RouterNetworkUseCase {
    
    override fun getRouter(routerId: RouterId): Router {
        return fetchRouter(routerId)
    }

    private fun fetchRouter(routerId: RouterId): Router {
        return routerNetworkOutputPort.fetchRouterById(routerId) // OutputPort(Adapter) 에 전달한다.
    }
}
```
 - 포트끼리 통신을 하게된다
   - UseCase의 구현체인 InputPort -> OutputPort(interface)
   - 잘못알고 있었던듯 하다
   - 외부 Primary Adapter -> UseCase(inputPort) - (Domain) - outputPort(Secondary Adapter)
   - 이게 복잡해지면, 헥사고날 처럼 되는건가?
   - 저 앞쪽이 Controller영역 + Facade로 감싸는 영역으로 많이 처리하다보니 좀 헷갈린다. Adapter쪽이 스프링쪽에서도 쓰이나?
     - 만약 API / WebSocket / Message(topic) 으로 들어오는게 있고, 이게 모두 같은 서비스로직을 실행시키면 어떤식으로 구현할지? 인터페이스를 사용하는지?
   
### 애플리케이션간 헥사고날 시스템 호출
- 시스템 은 다른 시스탬을 호출할때, Adapter중 하나를 통해 요청을 트리거 한다. (Port 아니였나)?
- 이 요청은 다른 입력 어댑터로 들어가게 된다
  
 - 모놀리스 VS MSA
 - 하나의 애플리케이션(?) 은 하나의 그것만 헥사고날만 가지나..?
   - MSA환경에서는 그렇게 하는건지 

## 드리븐 오퍼레이션을 통한 외부 리소스 처리
 - 헥사고날 -> 외부로 나가는 요청들을 드리븐
 - 헥사고날 어플리케이션에 없는 데이터나 기능을 제공
 - 출력 포트 & 출력 어댑터

### 데이터 지속성
 - 데이터 지속성을 기반으로 하는 드리븐 오퍼레이션이 일반적이다 
   - DB 말하는듯 
   - ORM
- 트랜잭션 메커니즘도 지속성 기반 드리븐 오퍼레이션의 일부다
- 트랜잭션을 활용할 때 헥사고날 시스템이 직접 트랜잭션 경계를 처리하거나, 이러한 책임을 애플리케이션 서버로 위임할 수 있다.
  - 트랜잭셔널은 헥사고날 경계에서 처리해야 한다?

### 메시징과 이벤트
 - 모든 시스템이 동기식은 아님
 - 메시징 또는 이벤트에 의존하는 경우도 많게 된다.
 - 메시지 기반 시스템은 헥사고날 애플리케이션에 의해 유도되는 보조 액터다 (?)
 - 메시지 가번 시스템이 헥사고날 애플리케이션과 통신을 시작하는 상황이 있다.
   - 이러면 드라이빙 아닌가
   - 이러한 상황은 카프카 (PUB SUB 다하는) 같은 기술을 사용할 때 흔히 접할 수 있다.
 - 카프카 토픽과 결합해 의도를 표현해야 한다.
 - 이벤트를 받는 이유는, 바로 아웃풋 포트를 처리해야 해서?
   - 이부분은 인풋으로 처리해서 다시 로직을 실행시켜야 하는거 아닐까..
   - 콜백같은 개념이면 그럴수도 있겠다
   - 코드를 보니까, 프레임워크 레이어?의 인풋 어댑터에 있는데, 이것도 결국 드리븐이라 (프론트를 호출하는 개념) 여기있는걸까?> 


```kotlin
interface NotifyEventOutputPort {
    fun sendEvent(event: String)
    fun getEvent(): String // ? 이건 인풋에 있어야 하는거 아니였나, Infra 영역이라 이렇게 되는걸까?
}
```
