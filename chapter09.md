# 09 자바 모듈을 이용한 의존성 역전 적용

## JPMS 소개
* 자바 SE 9 버전 이전까지는 자바에서 의존성을 처리하는 유일한 메커니즘은 classpath 매개변수였다.
* 섀도잉(Shadowing): 같은 의존성을 포함하는 둘 이상의 JAR 파일이 classpath 매개변수에 있을 때 JAR 파일 중 하나만 로드되고 나머지는 가려지는 상황 (JAR 지옥이라고도 함)
* JPMS
  * 의존성 버전 불일치와 섀도잉 관련 JAR 지옥 문제를 완전하게 방지할 수는 없다.
  * JPMS에서는 더 이상 다른 의존성에서 모든 퍼블릭 타입에 액세스할 수 없다.
  * 다른 모듈에서 어떤 패키지의 퍼블릭 타입에 액세스 하려면 module에 명시해주어야 한다.
    ```java
    module domain {
        exports com.example.domain.entity;
        exports com.example.domain.entity.factory;
        exports com.example.domain.service;
        exports com.example.domain.specification;
        exports com.example.domain.vo;
        requires static lombok;
    }
    ```
    ```java
    module application {
        requires domain;
    }
    ```
* 클라우드 기반 환경에서의 module 시스템
  * 애플리케이션을 실행하는 데 필요한 모듈만 포함하는 사용자 정의 자바 런타임을 구성할 수 있다.
  * 자바 런타임이 더 작아지면 쿠버네티스 파드를 띄울 때 컴퓨팅 리소스를 절약할 수 있다.

## 헥사고날 시스템에서 의존성 역전
* 의존성 역전 원칙(DIP): 상위 수준 컴포넌트는 하위 수준 컴포넌트에 의존해서는 안된다. 대신 이들 모두는 추상화에 의존해야 한다.
  * 상위 수준 컴포넌트 - 주된 시스템 동작을 활성화하도록 조정된 일련의 오퍼레이션. 구체적이거나 추상적인 요소일 수 있음.
  * 하위 수준 컴포넌트 - 상위 수준 컴포넌트의 목표를 지원하기 위해 특별한 동작을 제공. 항상 구현 세부사항을 제공해야 함.
* 상위 수준 컴포넌트가 하위 수준 컴포넌트에 직접 의존하면 시스템을 경직되게 만든다.
* 의존성을 반전시키려면 상위 수준 컴포넌트를 하위 수준 컴포넌트에서 유도된 것과 동일한 추상화에 의존하게 만들어야 한다. 
* 객체지향 설계에서는 하위 수준 컴포넌트는 추상화를 구현하고, 상위 수준 컴포넌트는 하위 수준 구현 대신 해당 추상화를 참조한다.
* JPMS의 메커니즘
  * 소비자, 서비스, 공급자 기반
  * ServiceLoader는 시스템이 특정 추상화의 구현체를 찾고 검색할 수 있게 한다.
  * uses 지시자를 통해 `공급자` 모듈이 제공하는 `서비스`를 소비해야 하는 필요성을 선언하는 `소비자` 모듈을 호출한다.
  * `서비스`는 uses 지시자에서 알려주는 인터페이스를 구현하거나 추상 클래스를 확장하는 객체이다.
  * `공급자`는 `서비스` 인터페이스의 구현을 각각 `공급자`와 `지시자`로 선언하는 모듈이다.

### 유스케이스와 입력 포트를 통한 서비스 제공
* 헥사고날 시스템에서 JPMS
  * `서비스`: 유스케이스(인터페이스)와 입력 포트(구현)
  * 해당 `서비스`를 제공하는 모듈: 애플리케이션 헥사곤 모듈
  * `소비자`: 프레임워크 헥사곤 모듈
* JPMS 적용 전 - 프레임워크 헥사곤의 입력 어댑터가 애플리케이션 헥사곤의 입력 포트 구현에 의존한다.
* JPMS 적용 후 - 프레임워크 헥사곤의 입력 어댑터가 애플리케이션 헥사곤의 유스케이스 인터페이스 타입에 의존한다.
* 즉, 입력 어댑터를 상위 수준 컴포넌트로 간주하고, 입력 포트를 하위 수준 컴포넌트로 간주한다. 
* `입력 어댑터(상위 수준 컴포넌트)` -> `유스케이스(추상화)` <- `입력 포트(하위 수준 컴포넌트)`
* 수정 전 - 상위 수준 컴포넌트(`RouterManagementH2Adapter`)가 하위 수준 컴포넌트(`RouterManagementInputPort`)에 직접 의존하고 있다.
    ```java
    private RouterManagementUseCase routerManagementUseCase;

    public RouterManagementGenericAdapter() {
        setPorts();
    }

    private void setPorts() {
        this.routerManagementUseCase = new RouterManagementInputPort(
            RouterManagementH2Adapter.getInstance()
        );
    }
    ```
* 입력 포트 클래스를 JPMS 공급자 클래스로 사용할 수 있게 바꿔보자. (모든 공급자 클래스는 매개변수를 갖지 않는 퍼블릭 생성자가 필요하다.)
    1. 먼저 인수가 없는 생성자를 추가한다.
        ```java
        @NoArgsConstructor
        public class RouterManagementInputPort implements RouterManagementUseCase {
            /* 코드 생략 */
        }
        ```
    2. 그다음 유스케이스 인터페이스에서 `setOutputPort` 메서드를 선언한다.
        ```java
        public interface RouterManagementUseCase {
            void setOutputPort(RouterManagementOutputPort routerManagementOutputPort);
        }
        ```
    3. 마지막으로, 입력 포트에서 `setOutputPort` 메서드를 구현해야 한다.
        ```java
        @Override
        public void setOutputPort(RouterManagementOutputPort routerManagementOutputPort) {
            this.routerManagementOutputPort = routerManagementOutputPort;
        }
        ``` 
* 이제 application 모듈에서 입력 포트, 출력 포트, 유스케이스에 액세스할 수 있도록 module 디스크립터를 수정한다.
    ```java
    module application {
        requires domain;
        requires static lombok;

        exports com.example.application.ports.input;
        exports com.example.application.ports.output;
        exports com.example.application.ports.usecases;
        /* 코드 생략 */
    }
    ```
* 다음으로 유스케이스 인터페이스와 그에 대한 구현인 입력 포트를 제공하기 위해 서비스를 선언한다.
    ```java
    module application {
        requires domain;
        requires static lombok;

        exports com.example.application.ports.input;
        exports com.example.application.ports.output;
        exports com.example.application.ports.usecases;
        
        provides com.example.application.usecases.RouterManagementUseCase
                with com.example.application.ports.input.RouterManagementInputPort; // router
        provides com.example.application.usecases.SwitchManagementUseCase
                with com.example.application.ports.input.SwitchManagementInputPort; // switch
        provides com.example.application.usecases.NetworkManagementUseCase
                with com.example.application.ports.input.NetworkManagementInputPort; // network
        
    }
    ```

## 출력 포트와 출력 어댑터를 통한 서비스 제공
* 프레임워크 헥사곤에서는 인터페이스로 출력 포트를, 인터페이스의 구현으로는 출력 어댑터를 갖는다.
* `입력 포트(상위 수준 컴포넌트)` -> `출력 포트(추상화)` <- `출력 어댑터(하위 수준 컴포넌트)`
* 모든 공급자 클래스는 매개변수를 갖지 않는 퍼블릭 생성자가 필요하므로 코드를 수정한다.
    ```java
    /* private -> */public RouterManagementH2Adapter() {
        setUpH2Database();
    }
    ```
* 이제 framework 모듈 디스크립터에 서비스를 선언한다.
    ```java
    module framework {
        requires domain;
        requires application;

        /* 코드 생략 */

        exports com.example.framework.adapters.output.h2.data;
        opens com.example.framework.adapters.output.h2.data;

        provides com.example.application.ports.output.RouterManagementOutputPort
                with com.example.framework.adapters.output.h2.RouterManagementH2Adapter; // router
        provides com.example.application.ports.output.SwitchManagementOutputPort
                with com.example.framework.adapters.output.h2.SwitchManagementH2Adapter; // switch
    }
    ```
    * opens: 런타임에 출력 어댑터에 대한 리플렉션 액세스를 허용하기 위함

### 입력 어댑터가 추상화에 의존하게 만들기
* provides와 with 지시자를 통해 노출한 `서비스`를 소비하는 첫 단계는 `소비자`의 framework 헥사곤 모듈의 module 디스크립터를 uses 지시자를 사용해 업데이트하는 것이다.
    1. 모듈 디스크립터를 수정한다.
        ```java
        module framework {
            /* 코드 생략 */
            uses com.example.application.usecases.RouterManagementUseCase;
            uses com.example.application.usecases.SwitchManagementUseCase;
            uses com.example.application.usecases.NetworkManagementUseCase;
            uses com.example.application.output.RouterManagementOutputPort;
            uses com.example.application.output.SwitchManagementOutputPort;
        }
        ```
    2. 이제 입력 어댑터가 애플리케이션 헥사곤 모듈의 유스케이스 인터페이스에만 의존하도록 리팩터링해보자. 먼저 `RouterManagementGenericAdapter`부터 구성한다.
        ```java
        public class RouterManagementGenericAdapter {
            private RouterManagementUseCase routerManagementUseCase;

            public RouterManagementGenericAdapter(
                RouterManagementUseCase routerManagementUseCase
            ) {
                this.routerManagementUseCase = routerManagementUseCase;
            }
            /* 코드 생략 */
        }
        ```
    3. `SwitchManagementGenericAdapter`의 의존성을 구성한다.
        ```java
        public class SwitchManagementGenericAdapter {
            private RouterManagementUseCase routerManagementUseCase;
            private SwitchManagementUseCase switchManagementUseCase;

            public SwitchManagementGenericAdapter(
                RouterManagementUseCase routerManagementUseCase,
                SwitchManagementUseCase switchManagementUseCase
            ) {
                this.routerManagementUseCase = routerManagementUseCase;
                this.switchManagementUseCase = switchManagementUseCase;
            }
            /* 코드 생략 */
        }
        ```
    4. 마지막으로 `NetworkManagementGenericAdpater`도 같은 방식으로 의존성을 구성해준다.

## 자바 플랫폼의 ServiceLoader 클래스를 사용해 JPMS 공급자 구현체 검색하기
* ServiceLoader: module 디스크립터가 제공하는 구성을 통해 특정 서비스 공급자 인터페이스에 대한 구현을 찾는다.
* `FrameworkTestData` 테스트 클래스의 `loadPortsAndUseCases` 메서드를 만들어보자. 입력 어댑터를 인스턴스화 하는 데 필요한 객체를 검색하는 데 ServiceLoader를 사용하는 메서드이다.
    ```java
    public class FrameworkTestData {
        protected RouterManagementGenericAdapter routerManagementGenericAdapter;
        protected SwitchManagementGenericAdapter switchManagementGenericAdapter;
        protected NetworkManagementGenericAdapter networkManagementGenericAdapter;
        /* 코드 생략 */
    }
    ```

### RouterManagementGenericAdapter 초기화
1. `RouterManagementUseCase` 검색
    ```java
    protected void loadPortsAndUseCases() {
        ServiceLoader<RouterManagementUseCase> loaderUseCaseRouter = ServiceLoader.load(RouterManagementUseCase.class);
        RouterManagementUseCase routerManagementUseCase = loaderUseCaseRouter.findFirst().get();
        /* 코드 생략 */
    }
    ```
2. `RouterManagementOutputPort` 객체 검색
    ```java
    ServiceLoader<RouterManagementOutputPort> loaderOutputPortRouter = ServiceLoader.load(RouterManagementOutputPort.class);
    RouterManagementOutputPort routerManagementOutputPort = loaderOutputPortRouter.findFirst().get();
    ```
3. `RouterManagementUseCase`에서 `RouterManagementOutputPort` 객체 설정
    ```java
    routerMangementUseCase.setOutputPort(routerManagementOutputPort);
    ```
4. `RouterManagementGenericAdapter`에 방금 생성한 `RouterManagementUseCase`를 전달
    ```java
    this.routerManagementGenericAdapter = new RouterManagementGenericAdapter(routerManagementUseCase);
    ```

### SwitchManagementGenericAdapter 초기화
1. `SwitchManagementUseCase` 검색
2. `SwitchManagementOutputPort` 검색
3. `SwitchManagementUseCase`에서 `SwitchManagementOutputPort` 객체 설정
4. `SwitchManagementGenericAdapter`에 `RouterManagementUseCase`와 `SwitchManagementUseCase`를 전달

### NetworkManagementGenericAdapter 초기화
1. `NetworkManagementUseCase` 검색
2. `NetworkManagementUseCase`에서 `RouterManagementOutputPort` 객체 설정
3. `SwitchManagementGenericAdapter`에 `SwitchManagementUseCase`와 `NetworkManagementUseCase`를 전달