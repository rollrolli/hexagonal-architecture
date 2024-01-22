# 2-7. 애플리케이션 헥사곤 만들기

# 주제

1. 애플리케이션 헥사곤 생성
2. 유스케이스 정의
3. 유스케이스와 입력 포트 구현
4. 애플리케이션 헥사곤 테스트

# 애플리케이션 헥사곤 생성

---

- 애플리케이션 헥사곤
    - 도메인 헥사곤을 통한 내부 요청 & 프레임워크 헥사곤을을 통한 외부 요청을 조정
    - 제약조건이나 비즈니스 규칙을 정의하지 않는다.
    - 헥사고날 시스템에서 데이터 프름을 정의하고 제어하는 것이 목표

# 유스케이스 정의

---

- 구성한 도메인 모델의 관점에서 시스템의 기능을 구성한다.
- 큐컴버와 같은 도구를 사용하여 유스케이스 파악을 돕는다.
- 큐컴버 개념으로 유스케이스 디스크립션을 쉽게 만들 수 있다.
    - 큐컴버 기능 파일에서 유스케이스를 구조화한다.

### 라우터 관리 유스케이스에 대한 디스크립션 작성

- RouterAdd.feature

```java
@RouterAdd
Feature: 코어 라우터에 에지 라우터를 추가할 수 있는가?

	Scenario: 코어 라우터에 에지 라우터 추가
		Given 에지 라우터가 있다
		And 코어 라우터가 있다
		Then 코어 라우터에 에지 라우터를 추가한다

	Scenario: 또 다른 코어 라우터에 코어 라우터를 추가한다
		Given 코어 라우터가 있다
		And 또 다른 코어 라우터가 있다
		Then 코어 라우터에 이 코어 라우터를 추가한다
```

- RouterCreate.feature

```java
@RouterCreate
Feature: 새로운 라우터를 만들 수 있는가?

	Scenario: 새로운 코어 라우터 만들기
		Given 코어 라우터를 만들기 위해 필요한 모든 데이터를 제공한다
		Then 새로운 코어 라우터가 생성된다

	Scenario: 새로운 에지 라우터 만들기
		Given 에지 라우터를 만들기 위해 필요한 모든 데이터를 제공한다
		Then 새로운 에지 라우터가 생성된다
```

- RouterRemove.feature

```java
@RouterRemove
Feature: 라우터를 제거할 수 있는가?

	Scenario: 코어 라우터에서 에지 라우터 제거하기
		Given 코어 라우터는 적어도 하나의 에지 라우터와 연결되어 있다
		And 스위치는 연결된 네트워크를 갖고 있지 않다
		And 에지 라우터는 연결된 네트워크를 갖고 있지 않다
		Then 코어 라우터에서 에지 라우터를 제거한다

	Scenario: 코어 라우터에서 또 다른 코어 라우터에서 제거하기
		Given 코어 라우터는 적어도 하나의 코어 라우터와 연결되어 있
		And 코어 라우터는 연결된 또 다른 라우터를 갖고 있지 않다
		Then 코어 라우터를 또 다른 코어 라우터에서 제거한다
```

### 라우터 관리를 위한 유스케이스 인터페이스 정의

```java
public interface RouterManagementUseCase {
		
	Router createRouter(Vendor vendor, Model model, Ip ip, Location location, RouterType routerType);
	CoreRouter addRouterToCoreRouter(Router router, CoreRouter coreRouter);
	Router removeRouterFromCoreRouter(Router router, CoreRouter coreRouter);

	Router retrieveRouter(Id id);
	Router persistRouter(Router router);

}
```

- createRouter → RouterCreate.feature에 기반한다.
- addRouterToCoreRouter → RouterAdd.feature에 기반한다.
- removeRouterFromCoreRouter → RouterRemove.feature에 기반한다.

# 입력 포트를 갖는 유스케이스 구현

---

- 입력 포트는 애플리케이션 헥사곤의 중심 요소다.
- 도메인 헥사곤과 프레임워크 헥사곤 사이의 간극을 메운다.
- 애플리케이션 헥사곤을 생성할 때 출력 포트 인터페이스를 선언은 할 수 있다.(하지만 출력 어댑터 구현체는 없는 상태이므로 사용할 수는 없는 상태)

```java
@NoArgsConstructor
public class RouterManagementInputPort implements RouterManagementUseCase {
		RouterManagementOutputPort outputPort;
}
```

- createRouter 구현

```java
@Override
public Router createRouter(Vendor vendor, Model model, Ip ip, Location location, RouterType routerType) {
	return RouterFactory.getRouter(vendor, model, ip, location, routerType);
}
```

- addRouterToCoreRouter 구현

```java
@Override
public CoreRouter addRouterToCoreRouter(Router router, CoreRouter coreRouter) {
		var added = coreRouter.addRouter(router);
		// persist router
		return added;
}
```

- removeRouterFromCoreRouter 구현

```java
@Override
public Router removeRouterFromCoreRouter(Router router, CoreRouter coreRouter) {
		var removed = coreRouter.removeRouter(router);
		// persist router
		return removed;
}
```

- retrieveRouter 구현

```java
@Override
public Router retrieveRouter(Id id) {
		return outputPort.retrieveRouter(id);
}
```

- persistRouter 구현

```java
@Override
public Router persistRouter(Router router) {
		return outputPort.persistRouter(router);
}
```

### 애플리케이션 헥사곤 테스트

- 코어 라우터에 에지 라우터 추가 테스트
    - Given - 첫 단계로 에지 라우터를 가져온다
        
        ```java
        @Given("I have an edge router")
        public void assert_edge_router_exists() {
        		edgeRouter = (EdgeRouter)
        				this.routerManagementUseCase.createRouter(
        					Vendor.HP, Model.XYZ1234, Ip.fromAddress("12.12.12"), locationA, EDGE);
        		assertNotNull(edgeRouter);
        }
        ```
        
    - And - 에지 라우터를 생성했으므로 코어 라우터를 생성한다
        
        ```java
        @And("I have an core router")
        public void assert_core_router_exists() {
        		coreRouter = (CoreRouter)
        				this.routerManagementUseCase.createRouter(
        					Vendor.CISCO, Model.XYZ4321, Ip.fromAddress("21.21.21"), locationA, CORE);
        		assertNotNull(coreRouter);
        }
        ```
        
    - Then - 에지 라우터를 코어 라우터에 추가하고 비교 확인한다
        
        ```java
        @Then("I add an edge router to a core router")
        public void add_edge_to_core_router() {
        		var actualEdgeId = edgeRouter.getId();
        		var routerWithEdge = (CoreRouter) this.routerManagementUseCase.
        											addRouterToCoreRouter(edgeRouter, coreRouter);
        		var expectedEdgeId = routerWithEdge.getRouters().get(actualEdgeId).getId();
        		assertEquals(actualEdgeId, expectedEdgeId);		
        }
        ```
        
    
    # 요약
    
    ---
    
    - 큐컴버를 사용하여 유스케이스를 코드 용어, 글로 표현할 수 있다.
    - 디스크립션을 작성하여 큐컴버 기능 파일을 생성 → 기능 파일을 참조해서 유스케이스 인터페이스를 생성 → 기능 파일에 작성 디스크립션 기반으로 유스케이스 테스트 작성
    - 큐컴버로 선언적이고 단순하게 시스템 행동을 표현하여 애플리케이션 헥사곤을 구현하고 테스트할 수 잇다.