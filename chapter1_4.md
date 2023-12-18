# 1-4. 외부와 상호작용하는 어댑터 만들기

# 어댑터 이해

---

- 헥사고날 아키텍처에서의 어댑터는 GoF의 디자인 패턴에 설명된 어댑터와는 다르다.
    - [https://refactoring.guru/ko/design-patterns/adapter](https://refactoring.guru/ko/design-patterns/adapter)
- 헥사고날 아키텍처에서는 시스템이 다른 기술이나 프로토콜과 호환하기 위해 어댑터를 사용
- `무언가를 다른 것에 적절하게 맞추게 조정`하는 목적은 동일하다.

### 입력 어댑터

- 애플리케이션 기능을 노출하는 데 사용되는 어댑터
    - 사용자와 다른 시스템이 애플리케이션과 상호작용하도록 정의

### 출력 어댑터

- 애플리케이션에서 생성된 데이터를 변환하고 외부 시스템과 통신하는 역할

→ 입출력 어댑터 모두 프레임워크 헥사곤의 가장 끝부분에 위치한다.

# 드라이빙 오퍼레이션 허용을 위한 입력 어댑터 사용

---

- 원격 통신 프로토콜과 유사
- 헥사고날 시스템이 제공하는 기능에 액세스 수단으로 지원되는 기술을 정의하는 프로토콜처럼 동작한다.
- 헥사곤 내부와 외부 사이의 명확한 경계를 표시
- 드라이빙 오퍼레이션 수행
- 헥사곤 외부에는 헥사곤 애플리케이션과 상호작용하는 사용자나 시스템이 있을 수 있는데 이런 역할을 하는 주체를 주요 액터(primary actor)라고 부른다.
- **주요 액터 ↔ 헥사고날 애플리케이션의 상호작용은 입력 어댑터를 통해 일어난다.**
    - 상호작용을 드라이빙 오퍼레이션으로 정의
- 입력 어댑터가 헥사고날 시스템을 외부 세계에 노출시키는 역할을 하므로 시스템과의 상호작용에 관심 갖는 모두에게 인터페이스가 된다.

## 입력 어댑터 생성

- 입력 포트는 오퍼레이션의 수행 방법을 구체적으로 지정해서 유스케이스를 구현하는 수단
- 예시)
    - 라우터에 네트워크 추가하는 유스케이스
    - 이러한 유스케이스를 구현하는 입력 포트
    - 입력 포트 객체로 자극(?)을 발신하는 입력 어댑터

### 기반 (입력) 어댑터

- 우선 추상 기반 클래스를 정의한다.
- 어댑터 관련된 입력 포트와 통신을 위한 표준 오퍼레이션만 제공한다.

```java
public abstract class RouterNetworkAdaptor {

		protected Router router;
		protected RouterNetworkUseCase routerNetworkUseCase;

		protected Router addNetworkToRouter(Map<String, String> params) {
				var routerId = RouterId.withId(params.get("routerId"));
				var network = new Network(IP.fromAddress(params.get("address")), paramse.get("name"), Integer.valueOf(params.get("cidr")));

				return routerNetworkUseCase.addNetworkToRouter(routerId, network);
		}

		public abstract Router processRequest(Object requestParams);

}
```

- 입력 포트를 직접 참조하지 않고 유스케이스 인터페이스 참조를 활용한다.
- 유스케이스 참조는 이력 어댑터 생성자에 의해 전달되고 초기화

### REST 입력 어댑터

- 우선 RouterNetworkRestAdapter 생성자를 정의한다.

```java
public RouterNetworkRestAdapter(RouterNetworkUseCase routerNetworkUseCase) {
		this.routerNetworkUseCase = routerNetworkUseCase;
}
```

- 클라이언트에서 RouterNetworkRestAdapter 입력 어댑터 호출 및 초기화

```java
RouterNetworkOutputPort outputPort = RouterNetworkH2Adapter.getInstance();
RouterNetworkUseCase useCase = new RouterNetworkUseCase(outputPort);
RouterNetworkAdaptor inputAdapter = new RouterNetworkRestAdapter(userCase);
```

- RouterNetworkAdapter 정의 후 processRequest 메서드 구현한다.
- httpServer 객체를 수신한다.

```java
@Override
public Router processRequest(Object requestParams) {

	// ...

	httpServer.createContext("/network/add", (exchange -> {
			if ("GET".equals(exchange.getReqeustMethod())) {
					var query = exchange.getRequestURI().getRawQuery();
					httpParams(query, params);
					router = this.addNetworkToRouter(params);
					ObjectMapper mapper = new ObjectMapper();
					var routerJson =
								mapper.writeValueAsString(RouterJsonFileMapper.toJson(router));
					exchange.getRousponseHeaders().set("Content-Type", "application/json");
					exchange.sendResponseHeaders(200, routerJson.getBytes().length);
					OutputStream output = exchange.getResponseBody();
					output.write(routerJson.getBytes());
					output.flush();
		 } else {
					exchange.sendResponseHeaders(405, -1);
		 }

	//...

	}
}
```

- processRequest 를 사용하는 클라이언트 코드

```java
var httpserver = HttpServer.create(new InetSocketAddress(8080), 0);
routernetworkAdapter.processRequest(httpserver);
```

- httpserver를 수신하고 파싱한 후 부모 클래스에 정의된  addNetworkToRouter 호출

```java
router = this.addNetworkToRouter(params);
```

- 유스케이스 참조를 사용해 입력 포트를 트리거하기 위해 사용되는 사용자 데이터를 적절한 매개변수로 변환하는 역할을 한다.

```java
routerNetworkUseCase.addNetworkToRouter(routerId, network);
```

### CLI 입력 어댑터

- p.83~ 85 예시 참고

### 입력 어댑터 호출하기

- 선택할 어댑터 제어를 위한 클라이언트 코드

```java
public class App {
	
	/* 생략 */
	void setAdapter(String adapter) {
			switch(adapter) {
					case "rest":
							outputPort = RouterNetworkH2Adapter.getInstance();
							usecase = new RouterNetworkInputPort(outputPort);
							inputAdapter = new RouterNetworkRestAdapter(usercase);
							rest();
							break;
					default:
							outputPort = RouterNetworkFileAdapter.getInstance();
							usecase = new RouterNetworkInputPort(outputPort);
							inputAdapter = new RouterNetworkCLIAdapter(usercase);
							cli();
			}
	}

}
```

- adapter 매개변수와 switch-case 조건에 따라 rest 또는 cli 메서드를 호출한다.

```java
private void rest() {
		try {
				System.out.println("REST endpoint listening on port 8080..");
				var httpserver = HttpServer.create(new InetSocketAddress(8080), 0);
				inputAdapter.processRequest(httpserver);
		} catch (IOException e) {
				e.printstacktrace();
		}
}
```

```java
private void cli() {
		Scanner sc = new Scanner(System.in);
		inputAdapter.processRequest(sc);
}
```

# 다양한 데이터 소스와 통신하기 위한 출력 어댑터 사용

---

- 마이크로서비스 같은 아키텍처에서는 시스템을 다양한 기술과 통합하는 상황이 많다.
- 기술적으로 이기종 환경의 변화를 극복하기 위한 기술이 필요
- 출력 어댑터를 만들기 위한 유일한 요구사항은 애플리케이션 헥사곤의 출력 포트 인터페이스를 구현하는 것이다.
    - 이는 헥사고날 방식이 기술적 관심사로부터 비즈니스 로직을 보호하기 위함이다.

## 출력 어댑터 생성

- 출력 어댑터의 역할은 드리븐 오퍼레이션을 처리하는 것
- **`드리븐 오퍼레이션이란?`**
    - 일부 데이터를 보내거나 받기 위해 외부 시스템과 상호작용하는 헥사고날 애플리케이션 자체에서 시작된 오퍼레이션
    - 드리븐 오퍼레이션은 유스케이스를 통해 서술(정의? 선언?)되고,
    - 유스케이스의 입력 포트 구현에 있는 오퍼레이션에 의해 트리거된다.
- 출력 포트는 외부 시스템과의 상호작용을 추상적인 방법으로 표현(선언만..?)한다.
- 출력 어댑터를 통해 상호작용 발생 방법을 구체적으로 설명할 책임을 가진다.
- **애플리케이션 헥사곤**에 `**출력 포트(인터페이스)**` / **프레임워크 헥사곤**에 `**출력 어댑터(출력 포트 구현클래스)**`
    - 이렇게 함으로써 다양한 기술을 지원하는 헥사고날 시스템을 구성할 수 잇다.

### H2 출력 어댑터

- routers, switches, networks 테이블 생성(p.91~2 참고)

- RouterNetworkOutputPort 출력 포트를 RouternetworkH2Adapter 클래스로 아래와 같이 구현한다.

```java
public class RouterNetworkH2Adapter implements RouternetworkOutputPort {

		private static RouterNetworkH2Adapter instance;

		@PersistenceContext
		private EntityManager em;
	
		private RouterNetworkH2Adapter() {
				setUpH2Database();
		}

		@Override
		public Router fetchRouterById(RouterId routerId) {
				var routerData = em.getReference(RouterData.class, routerId.getUUID());
				return RouterH2Mapper.toDomain(routerData);
		}

		@Override
		public boolean persistRouter(Router router) {
				var routerData = RouterH2Mapper.toH2(router);
				em.persist(routerData);
				return true;
		}

		private void setUpH2Database() {
				var entityManagerFactory = 
						Persistence.createEntityManagerFactory("inventory");
				var em = entityManagerFactory.createEntityManager();
				this.em = em;
		}
		// ...
}
```

- fetchRouterById는 엔티티 매니저 참조를 사용하여 라우터를 가져온다.
    
    → Router 를 바로 사용할 수 없어 mapper 를 사용하여 변환해준다.(toDomain 메서드)
    
- RouterNetworkH2Adapter 인스턴스를 하나만 생성하기 위해 싱글턴도 정의한다.

###