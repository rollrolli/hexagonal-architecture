# 9. 자바 모듈을 이용한 의존성 역전 적용

## JPMS 소개

- 자바 SE 9 버전 이전까지는 classpath 매개변수를 이용해서 JAR 파일 형식으로 의존성을 추가했다.
- 같은 의존성을 포함하는 둘 이상의 JAR 파일이 classpath 매개변수에 있을 때 JAR 파일 중 하나만 로드되고 나머지는 가려지는 상황을 섀도잉이라고 한다.
- 다양한 JAR 파일의 public 타입에 대한 액세스를 제어할 수 있는 방법이 없어 이름과 패키지가 같은 클래스의 충돌이 일어나기도 한다.
- JPMS를 이용하면 이러한 문제들을 해결할 수 있다.

```java
module domain {
    exports com.dskym.topologyinventory.domain.entity;
    exports com.dskym.topologyinventory.domain.service;
    exports com.dskym.topologyinventory.domain.specification;
    exports com.dskym.topologyinventory.domain.vo;
    exports com.dskym.topologyinventory.domain.entity.factory;
    requires static lombok;
}
```

```java
module application {
    requires domain;
}
```

- exports 지시어를 사용해 public 타입을 포함한 패키지들을 다른 모듈에서 사용할 수 있게 명시한다.
- requirs 지시어를 사용해 export된 모듈에 액세스한다.
- module 시스템의 특성을 통해 필요한 모듈만 포함하는 사용자 정의 자바 런타임을 구성하여 애플리케이션의 시작 시간과 메모리 사용량을 감소시킬 수 있다.

## 헥사고날 시스템에서 의존성 역전

- 의존성 역전 원칙 : 상위 수준 컴포넌트는 하위 수준의 컴포넌트에 의존해서는 안 된다. 대신 추상화에 의존해야 한다.
    - 상위 수준 컴포넌트는 주된 시스템 동작을 활성화하도록 조정된 일련의 오퍼레이션을 갖는다.
    - 주요 시스템 동작을 제공하기 위해 하위 수준 컴포넌트에 의존할 수 있다.
    - 하위 수준 컴포넌트는 상위 수준 컴포넌트의 목표를 지원하기 위해 특별한 동작을 사용할 수 있다.
    - 상위 수준 컴포넌트는 구체적이거나 추상적인 요소이고 하위 수준 컴포넌트는 항상 구현 세부사항을 제공해야 하기 때문에 구체적이다.
- 하위 수준 컴포넌트에 대한 구현 세부사항을 변경하면 직접 의존하는 상위 수준 컴포넌트에서 즉각적인 문제를 발생시킬 수 있다.
- 하위 수준 컴포넌트는 추상화하고 상위 수준 컴포넌트는 하위 수준 구현 대신 해당 추상화를 참조하여 문제를 피한다.

### 유스케이스와 입력 포트를 통한 서비스 제공

- 어플리케이션 헥사곤 모듈은 해당 서비스를 제공하는 모듈이다.
- 프레임워크 헥사곤 모듈은 애플리케이션 헥사곤 모듈의 직접적인 소비자이다.
- 프레임워크 헥사곤의 입력 어댑터는 더 이상 애플리케이션 헥사곤의 입력 포트 구현에 의존하지 않는다.
- 입력 어댑터는 입력 포트의 구체적인 타입보다 유스케이스 인터페이스 타입에만 의존한다.

```java
private RouterManagementUseCase routerManagementUseCase;

public RouterManagementGenericAdapter() {
	setPorts();
}

private void setPorts() {
	this.routerManagementUseCase = new RouterManagementInputPort(RouterManagementH2Adapter.getInstance());
}
```

```java
@NoArgsConstructor
public class RouterManagementInputPort implements RouterManagementUseCase {
	/* ... */
	@Override
	public void setOutputPort(RouterManagementOutputPort routerManagementOutputPort) {
		this.routerManagementOutputPort = routerManagementOutputPort
	}
}

public interface RouterManagementUseCase {
	void setOutputPort(RouterManagementOutputPort routerManagementOutputPort);
}
```

```java
module application {
    requires domain;
    requires static lombok;

    exports com.dskym.topologyinventory.application.ports.output;
    exports com.dskym.topologyinventory.application.usecases;
    exports com.dskym.topologyinventory.application.ports.input;

		provides com.dskym.topologyinventory.application.usecases.RouterManagementUseCase with com.dskym.topologyinventory.application.ports.input.RouterManagementInputPort;
		provides com.dskym.topologyinventory.application.usecases.SwitchManagementUseCase with com.dskym.topologyinventory.application.ports.input.SwitchManagementInputPort;
		provides com.dskym.topologyinventory.application.usecases.NetworkManagementUseCase with com.dskym.topologyinventory.application.ports.input.NetworkManagementInputPort;
}
```

## 출력 포트와 출력 어댑터를 통한 서비스 제공

- 프레임워크 헥사곤에서는 인터페이스로 출력 포트를, 인터페이스의 구현으로는 출력 어댑터를 갖는다.
- 입력 포트는 출력 포트에 의존하기 때문에 출력 포트가 제공하는 추상화에 의존해야 한다.

```java
module framework {
    requires domain;
    requires application;

    exports com.dskym.topologyinventory.framework.adapters.output.h2.data;
		opens com.dskym.topologyinventory.framework.adapters.output.h2.data;

		provides com.dskym.topologyinventory.application.ports.output.RouterManagementOutputPort with com.dskym.topologyinventory.framework.adapter.output.RouterManagementH2Adapter;
		provides com.dskym.topologyinventory.application.ports.output.SwitchManagementOutputPort with com.dskym.topologyinventory.framework.adapter.output.SwitchManagementH2Adapter;
}
```

### 입력 어댑터가 추상화에 의존하게 만들기

```java
module framework {
		/* ... */

		uses com.dskym.topologyinventory.application.usecases.RouterManagementUseCase;
		uses com.dskym.topologyinventory.application.usecases.SwitchManagementUseCase;
		uses com.dskym.topologyinventory.application.usecases.NetworkManagementUseCase;
		uses com.dskym.topologyinventory.application.ports.output.RouterManagementOutputPort;
		uses com.dskym.topologyinventory.application.ports.output.SwitchManagementOutputPort;
}
```

```java
public class RouterManagementGenericAdapter {
		private RouterManagementUseCase routerManagementUseCase;

		public RouterManagementGenericAdapter(RouterManagementUseCase routerManagementUseCase) {
				this.routerManagementUseCase = routerManagementUseCase
		}
}
```

```java
public class SwitchManagementGenericAdapter {
		private RouterManagementUseCase routerManagementUseCase;
		private SwitchManagementUseCase switchManagementUseCase;

		public SwitchManagementGenericAdapter(RouterManagementUseCase routerManagementUseCase, SwitchManagementUseCase switchManagementUseCase) {
				this.routerManagementUseCase = routerManagementUseCase
				this.switchManagementUseCase = switchManagementUseCase
		}
}
```

```java
public class NetworkManagementGenericAdapter {
		private NetworkManagementUseCase networkManagementUseCase;
		private SwitchManagementUseCase switchManagementUseCase;

		public NetworkManagementGenericAdapter(NetworkManagementUseCase networkManagementUseCase, SwitchManagementUseCase switchManagementUseCase) {
				this.networkManagementUseCase = networkManagementUseCase
				this.switchManagementUseCase = switchManagementUseCase
		}
}
```

## 자바 플랫폼의 ServiceLoader 클래스를 사용해 JPMS 공급자 구현체 검색하기

### RouterManagementGenericAdapter 초기화하기

```java
protected vodi loadPortsAndUseCases() {
	ServiceLoader<RouterManagementUseCase> loaderUseCaseRouter = ServiceLoader.load(RouterManagementUseCase.class);
	RouterManagementUseCase routerManagementUseCase = loaderUseCaseRouter.findFirst().get();

	ServiceLoader<RouterManagmentOutputPort> loaderOutputRouter = ServiceLoader.load(RouterManagmentOutputPort.class);
	RouterManagmentOutputPort routerManagmentOutputPort = loaderOutputRouter.findFirst().get();
	
	routerManagementUseCase.setOutputPort(routerManagementOutputPort);
	
	this.routerManagementGenericAdapter = new RouterManagementGenericAdapter(routerManagmentUseCase);
}
```

### SwitchManagementGenericAdapter 초기화

```java
ServiceLoader<SwitchManagementUseCase> loaderUseCaseSwitch = ServiceLoader.load(SwitchManagementUseCase.class);
SwitchManagementUseCase switchManagementUseCase = loaderUseCaseSwitch.findFirst().get();

ServiceLoader<SwitchManagmentOutputPort> loaderOutputSwitch = ServiceLoader.load(SwitchManagmentOutputPort.class);
SwitchManagmentOutputPort switchManagmentOutputPort = loaderOutputSwitch.findFirst().get();

switchManagementUseCase.setOutputPort(switchManagementOutputPort);

this.switchMaganementGenericAdapter = new SwitchManagementGenericAdapter(routerManagementUseCase, switchManagementUseCase);
```

### NetworkManagementGenericAdapter 초기화
