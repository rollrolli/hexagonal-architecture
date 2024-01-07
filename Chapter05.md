## 드라이빙 오퍼레이션을 통한 헥사고날 애플리케이션에 대한 요청 호출

- 아무도 시스템과 상호작용하지 않고 시스템도 다른 사용자나 시스템과 상호작용하지 않는 것은 불가능하다.
- 헥사고날 애플리케이션의 동작을 시작하게 하고 유도하는 것을 드라이빙 오퍼레이션이라고 부른다.
- 주요 액터는 드라이빙 오퍼레이션을 트리거하는 역할을 담당한다.
- 주요 액터는 명령행 콘솔, 웹 사용자 인터페이스 어플리케이션, 테스트 에이전트, 다른 시스템 등이 될 수 있다.

### 웹 어플리케이션을 헥사고날 시스템에 통합

- MVC 패턴을 적용하면서 비즈니스 로직과 프레젠테이션 코드를 하나의 소프트웨어 단위로 그룹화했다.
- MVC의 목적은애플리케이션에서 서로 다른 범주의 컴포넌트(모델, 뷰, 컨트롤러) 사이의 명확한 경계를 만드는 것이다.
- 프레젠테이션 코드와 비즈니스 코드가 같은 소프트웨어 프로젝트의 일부이면 비즈니스 로직이 프레젠테이션 코드로 누출되는 경우가 많았다.
- 프레젠테이션 코드와 비즈니스 코드 사이의 완전한 통합을 위한 프레임워크들을 사용하면서 프런트엔드 코드와 백엔드 코드가 서로 가깝게 있으면 프로젝트의 엔트로피의 원인이 될 수 있다.
- 프런트엔드 시스템이 하나 이상의 백엔드 시스템이 있는 네트워크를 통해 상호작용하는 분리된 독립 실행형 어플리케이션이 있는 분리된 아키텍처가 생겨났다.

```kotlin
// 유스케이스
interface RouterNetworkUseCase {
	fun addNetworkToRouter(routerId: RouterId, network: Network): Router
	**fun getRouter(routerId: RouterId): Router**
}
```

```kotlin
// 입력 포트
class RouterNetworkInputPort(
	private val routerNetworkOutputPort: RouterNetworkOutputPort,
): RouterNetworkUseCase {
	override fun addNetworkToRouter(routerId: RouterId, network: Network): Router {
		val router = fetchRouter(routerId)
		return createNetwork(router, network)
	}

	**override fun getRouter(routerId: RouterId): Router {
		return fetchRouter(routerId)
	}**

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
// 입력 어댑터
abstract class RouterNetworkAdapter {
    protected val router: Router
    protected val routerNetworkUseCase: RouterNetworkUseCase

    protected fun addNetworkRouter(params: Map<String, String>): Router {
        val routerId = RouterId.withId(params.get("routerId"))
        val network = Network(IP.fromAddress(params.get("address")), params.get("name"), Integer.valueOf(params.get("cidr")))
				return routerNetworkUseCase.addNetworkToRouter(routerId, network)
    }

    abstract fun processRequest(requestParams: Any): Router

		**fun getRouter(params: Map<String, String>): Router {
			val routerId = RouterId.withId(params.get("routerId"))
			return routerNetworkUseCase.getRouter(routerId)
		}**
}

class RouterNetworkRestAdapter(
    private val outerNetworkUseCase: RouterNetworkUseCase,
): RouterNetworkAdapter() {
    override fun processRequest(requestParams: Any): Router {
				/* ... */
				**if(exchange.requestURI.path == "/network/add") {
					try {
						router = this.addNetowrkToRouter(params)
					} catch (e: Exception) {
						exchange.sendResponseHeaders(400, e.message.bytes.length)
						val output = exchange.reponseBody
						output.write(e.message.bytes)
						output.flush()
					}
				}
				if(exchange.requestURI.path.contains("/network/get")) {
					router = this.getRouter(params)
				}**
				/* ... */
    }
}
```

- HTML5와 JS를 활용하여 프런트엔트 애플리케이션을 개발해서 Rest Adapter를 호출한다.

### 테스트 에이전트 실행

- 기능이 잘 동작하는지 확인하기 위해 헥사고날 시스템과 상호작용하는 테스트와 모니터링 에이전트가 있다.
- 특정 애플리케이션의 엔드포인트가 정상인지 아닌지 확인하기 위해 엔드포인트에 대한 요청을 주기적으로 발행할 수 있다.
- 이러한 엔드포인트를 제공하는 도구로 스프링 액추에이터가 있다.
- 애플리케이션이 활성화 되었는지 확인하기 위해 주기적으로 애플리케이션에 요청을 보내는 프로브 메커니즘을 포함하기도 한다.
- Postman으로 테스트 케이스를 테스트 컬렉션으로 만들고 뉴먼을 사용해 테스트 컬렉션을 실행할 수 있다.
- 이런 방법은 지속적인 통합 파이프라인에 통합하는데 좋다.

### 애플리케이션 간의 헥사고날 시스템 호출

- 모놀리스 시스템으로 개발할 지 마이크로서비스 시스템으로 개발할 지 논쟁이 많다.
    - 모놀리스 시스템
        - 객체와 메서드 호출 사이에 직접적으로 데이터가 흐른다.
        - 같은 애플리케이션 내의 모든 소프트웨어 명령어가 그룹화 되어 있다.
        - 통신 오버헤드가 적고 시스템에서 생성된 로그가 중앙 집중화 되어 있다.
    - 마이크로서비스와 분산 시스템
        - 협력하는 독립 실행형 애플리케이션 사이에서 일부 데이터가 네트워크를 통해 흐른다.
        - 개발이 분리되고 컴포넌트가 더욱더 모듈화된다.
        - 패키지 사이즈가 작아 컴파일 시간이 빠르고 CI도구에서 빠른 피드백 루프를 만든다.
        - 로그가 중앙 집중화 되어 있지 않고 네트워크 통신 오버데이트가 제한 요소로 나타날 수 있다.
    - 분산 방식에서 헥사고날 시스템을 구성할 수 있다.
        - 시스템 A가 출력 어댑터 중 하나를 통해 요청을 트리거하면 시스템 B가 해당 요청을 입력 어댑터에서 받아서 처리한다.
        - 각 시스템은 독립적인 언어로 개발할 수 있다.

## 드리븐 오퍼레이션을 통한 외부 리소스 처리

- 비즈니스 어플리케이션의 일반적인 특징은 다른 시스템으로 데이터를 보내거나 데이터를 요청해야 한다는 점이다.
- 외부 리소스를 보조 액터라고 부르고 헥사고날 어플리케이션에 없는 데이터나 기능을 제공한다.
- 드라이빙 오퍼레이션은 헥사고날 시스템의 행위를 유도하는 주요 액터의 요청으로 비롯된다.
- 드리븐 오퍼레이션은 헥사고날 시스템에 의해 데이터베이스나 다른 시스템 같은 보조 액터 쪽으로 시작된 요청이다.

### 데이터 지속성

- 헥사고날 시스템과 데이터베이스 사이에서 객체를 처리하고 변환하기 위해 ORM 기법을 사용한다.
    - 자바에서는 JPA 구현을 사용한다.
- 트랜잭션 메커니즘을 활용해서 직접 트랜잭션 경계를 처리하거나 애플리케이션 서버로 책임을 위임한다.

### 메시징과 이벤트

- 시스템 컴포넌트 사이의 통신이 비동기적으로 발생할 경우 메시지와 이벤트가 논블로킹 방식으로 애플리케이션의 행위를 유도하게 할 수 있다.
- 메시지 기반 시스템은 헥사고날 아키텍처에 의해 유도되는 보조 액터다.

```kotlin
//출력 포트
interface NotifyEventOutputPort {
	fun sendEvent(event: String)
	fun getEvent(): String
}

//출력 어댑터
class NotifyEventKafkaAdapter: NotifyEventOutputPort() {
	/* ... */
	override fun sendEvent(eventMessage: String) {
		val record = ProducerRecord<Long, String>(TOPIC_NAME, eventMessage)
		try {
			val metadata = producer.send(record).get()
			println("Event message sent to the topic $TOPIC_NAME: eventMessage.")
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	override fun getEvent() {
		val noMessageToFetch = 0
		val event = AtomicReference<String>("")
		while(true) {
			/* ... */
			consumerRecords.forEach {
				event.set(it.value())
			}
		}
		val eventMessage = event.toString()
		if(sendToWebsocket)
			sendMessage(eventMessage)
		return eventMessage	
	}

	fun sendMessage(message: String) {
		try {
			val client = WebSocketClientAdapter(URI("ws://localhost:8887"))
			client.connectBlocking()
			client.send(message)
			client.closeBlocking()
		} catch(e: URISyntaxException | InterruptedException) {
			e.printStackTrace()
		}
	}
}
```

```kotlin
// 웹소켓 서버
class NotifyEventWebSockerAdapter: WebSocketServer() {
	/* ... */
	companion object {
		fun startServer() {
			val ws = NotifyEventWebSockerAdapter(InetSocketAddress("localhost", 8887))
			ws.reuseAddr = true
			ws.start()
			println("Topology & Inventory webSocket started on port: ${ws.port}")
			val sysin = BufferedReader(InputStreamReader(System.in))

			while(true) {
				val in = sysin.readLine()
				ws.broadcast(in)
				if (in == "exit") {
					ws.stop()
					break
				}
			}
		}
	}
}
```

```kotlin
// 클라이언트
class App {
    /* ... */
    fun setAdapter(adapter: String): Unit {
        when(adapter) {
            "rest" -> {
                routerOutputPort = RouterNetworkH2Adapter.getInstance()
								notifyOutputPort = NotifyEventKafkaAdapter.getInstance()
                usecase = RouterNetworkInputPort(outputPort, notifyOutputPort)
                inputAdapter = RouterNetworkRestAdapter(usecase)
                rest()
								NotifyEventWebSocketAdapter.startServer()
                break
            }
            else -> {
                outputPort = RouterNetworkH2Adapter.getInstance()
                usecase = RouterNetworkInputPort(outputPort)
                inputAdapter = RouterNetworkCLIAdapter(usecase)
                cli()
            }
        }
    }
}
```

```kotlin
//웹소켓 클라이언트
class WebSocketClientAdapter(serverUri: URI): WebSocketClient(serverUri) {
	override fun onMessage(message: String) {
		val channel = message
	}
	override fun onOpen(handshake: ServerHandshake) {
		println("opened connection")
	}
	override fun onClose(code: Int, reason: String, remote: Boolean) {
		println("closed connection")
	}
	override fun onError(ex: Exception) {
		ex.printStackTrace()
	}
}
```

```kotlin
// 입력 포트
class RouterNetworkInputPort(
	private val routerNetworkOutputPort: RouterNetworkOutputPort,
	private val notifyEventOutputPort: NotifyEventOutputPort,
): RouterNetworkUseCase {
	override fun addNetworkToRouter(routerId: RouterId, network: Network): Router {
		val router = fetchRouter(routerId)
		**notifyEventOutputPort.sendEvent("Adding ${network.name} network to router ${router.id.uuid}")**
		return createNetwork(router, network)		
	}

	override fun getRouter(routerId: RouterId): Router {
		**notifyEventOutputPort.sendEvent("Retrieving router ID ${routerId.uuid}")**
		return fetchRouter(routerId)
	}
	/* ... */
}
```

### 모의 서버

- 일반적인 소프트웨어 개발 방법은 개발, QA, 운영 같이 다양한 환경을 갖는다.
    - 개발 환경에서 시작해서 운영 환경으로 이동하면서 소프트웨어가 잘 동작하는지 계속해서 검증하고 확인하는 CI 파이프라인을 수행한다.
    - 파이프라인에서는 CI 검증, 단위 테스트, 통합 테스트가 이뤄질 수 있는데, 통합 테스트는 다른 애플리케이션이나 시스템, 데이터베이스, 서비스 같은 모두 다른 환경에서 제공되는 컴포넌트에 의존한다.
- 통합 테스트의 장애를 극복하기 위해 모의 솔루션이나 모의 서버를 사용한다.
