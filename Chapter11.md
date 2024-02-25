- 외부 의존성에 객체 인스턴스를 제공하는 책임을 위임하고 애플리케이션을 수명주기에 따라 관리하기 위해 CDI 사용
- 의존성 주입 솔루션은 일반적으로 애노테이션 속성을 사용해 객체의 의존성을 선언하는 클래스에서 자동으로 객체를 생성하고 의존성으로 제공할 수 있다.

## 쿼커스 DI 배우기

- 쿼커스 DI 또는 다른 의존성 주입 솔루션을 사용했을 때 얻는 이점은 애플리케이션이 객체의 프로비저닝과 수명주기 제어와 관련된 활동보다는 개발 중인 소프트웨어의 비즈니스 측면에 더 집중할 수 있다.

### 빈으로 작업하기

- 빈은 의존성을 주입할 때 사용하거나 다른 빈에 주입되어야 하는 의존성 자체로 동작하는 객체이다.
- 컨테이너 관리 환경에서 발생한다.
- ApplicationScoped
    - 전체 어플리케이션에서 사용할 수 있다.
    - 하나의 빈만 생성되고 이 빈이 주입되는 모든 시스템 영역에 걸쳐 공유된다.
    - 처음 호출될 때만 빈의 인스턴스가 생성되는 지연 로드가 된다.
    
    ```java
    @ApplicationScoped
    class MyBean {
    	public String name = "Test Bean";
    	public String getName() {
    		return name;
    	}
    }
    
    class Consumer {
    	@Inject
    	MyBean myBean;
    
    	public String getName() {
    		return myBean.getName();
    	}
    }
    ```
    
- Singleton
    - 하나의 빈 객체만 생성되고 시스템 전체에 공유된다.
    - 즉시 로드가 된다.

```java
@Singleton
class EagerBean {
	...
}

class Consumer {
	@Inject
	EagerBean eagerBean;
}
```

- RequestScoped
    - 일반적으로 빈과 관련된 요청이 살아 있는 동안만 빈을 사용할 수 있다
    
    ```java
    @RequestScoped
    class RequestData {
    	public String getResponse() {
    		return "string response";
    	}
    }
    
    @Path("/")
    class Consumer {
    	@Inject
    	RequestData requestData;
    
    	@GET
    	@Path("/request")
    	public String loadRequest() {
    		return requestData.getResponse();
    	}
    }
    ```
    
- Dependent
    - 빈이 사용되는 장소로 범위가 제한되어 시스템의 다른 빈에 공유되지 않는다.
    - 수명주기는 이 빈을 주입하는 빈에 정의된 수명주기와 같다.
    
    ```java
    @Dependent
    class DependentBean {
    	...
    }
    
    @ApplicationScoped
    class ConsumerApplication {
    	@Inject
    	DependentBean dependentBean;
    }
    
    @RequestScoped
    class ConsumerRequest {
    	@Inject
    	DependentBean dependentBean;
    }
    ```
    
- SessionScoped
    - 동일한 HTTP 세션의 모든 요청 사이에 빈의 컨텍스트를 공유하기 위해 이 범위를 사용한다.
    
    ```java
    @SessionScoped
    class SessionBean implements Serializable {
    	public String getSessionData() {
    		return "sessionData";
    	}
    
    }
    
    @Path("/")
    class Consumer {
    	@Inject
    	SessionBean sessionBean;
    
    	@GET
    	@Path("/sessionData")
    	public String test() {
    		return sessionBean.getSessionData();
    	}
    }
    ```
    

## 포트, 유스케이스, 어댑터를 CDI 빈으로 변환

### 라우터 관리 객체에 대한 CDI 구현

```java
@ApplicationScoped
public class RouterManagementH2Adapter implements RouterManagementOutputPort {
	@PersistenceContext
	private EntityManager em;

	private void setUpH2Database() {
		EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("inventory");
		EntityManager em = entityManagerFactory.createEntityManager();
		this.em = em;
	}
}
```

```java
@ApplicationScoped
public class RouterManagementInputPort implements RouterManagementUseCase {
	@Inject
	RouterManagementOutputPort routerManagementOutputPort;
}
```

```java
@ApplicationScoped
public class RouterManagementGenericAdapter {
	@Inject
	private RouterManagementUseCase routerManagementUseCase;
}
```

### 스위치 관리 객체에 대한 CDI 구현

```java
@ApplicationScoped
public class SwitchManagementH2Adapter implements SwitchManagementOutputPort {
	@PersistenceContext
	private EntityManager em;
}
```

```java
@ApplicationScoped
public class SwitchManagementInputPort implements SwitchManagementUseCase {
	@Inject
	private SwitchManagementOutputPort switchManagementOutputPort;
}
```

```java
@ApplicationScoped
public class SwitchManagementGenericAdapter {
	@Inject
	private RouterManagementUseCase routerManagementUseCase;

	@Inject
	private SwitchManagementUseCase switchManagementUseCase;
}
```

### 네트워크 클래스와 인터페이스를 위한 CDI 구현

```java
@ApplicationScoped
public class NetworkManagementInputPort implements NetworkManagementUseCase {
	@Inject
	private RouterManagementOutputPort routerManagementOutputPort;
}
```

```java
@ApplicationScoped
public class NetworkManagementGenericAdapter {
	@Inject
	private NetworkManagementUseCase networkManagementUseCase;

	@Inject
	private SwitchManagementUseCase switchManagementUseCase;
}
```

## 쿼커스와 큐컴버를 통한 유스케이스 테스팅

```java
@QuarkusTest
public class ApplicationTest extends CucumberQuarkusTest {
}
```

```java
@Mock
public class RouterManagmentOutputPortMock implements RouterManagementOutputPort {
	@Override
	public Router retrieveRouter(Id id) {
		return null;
	}

	@Override
	public Router removeRouter(Id id) {
		return null;
	}

	@Override
	public Router persistRouter(Router router) {
		return null;
	}
}
```

```java
@Mock
public class SwitchManagmentOutputPortMock implements SwitchManagementOutputPort {
	@Override
	public Switch retrieveSwitch(Id id) {
		return null;
	}
}
```

```java
public class RouterAdd extends ApplicationTestData {
	@Inject
	RouterManagementUseCase routerManagementUseCase;
}
```
