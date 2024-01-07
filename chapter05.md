# 05 드라이빙 오퍼레이션과 드리븐 오퍼레이션의 본질 탐색

* 드라이빙 오퍼레이션을 통한 헥사고날 애플리케이션에 대한 요청 호출
* 헥사고날 시스템과 웹 애플리케이션의 통합
* 테스트 에이전트 실행 및 다른 애플리케이션에서의 헥사고날 시스템 호출
* 드리븐 오퍼레이션을 통한 외부 리소스 처리

## 드라이빙 오퍼레이션을 통한 헥사고날 애플리케이션에 대한 요청 호출
* 헥사고날 아키텍처 시점의 시스템의 입력 - `드라이빙 오퍼레이션`에 의해 제어
  * 주요 액터: 헥사고날 시스템의 드라이빙 오퍼레이션을 트리거하는 역할 담당
    * 예시1: 명령행 콘솔을 통해 직접 시스템과 상호작용하는 사용자
    * 예시2: 브라우저에 표시하기 위한 데이터를 요청하는 웹 사용자 인터페이스(UI) 애플리케이션
    * 예시3: 특정 테스트 케이스 검증을 원하는 테스트 에이전트
    * 예시4: 헥사고날 애플리케이션이 노출하는 기능에 관심을 갖는 다른 시스템

### 웹 애플리케이션을 헥사고날 시스템에 통합
* 예제 애플리케이션의 목표
  * 사용자가 라우터에 네트워크를 추가
  * 사용자가 시스템 데이터베이스에서 기존 라우터 검색
  * 헥사고날 애플리케이션의 일부를 리팩터링 해서 프론트엔드 애플리케이션과 더 잘 통합할 수 있게 함
* `RouterNetworkUseCase` 인터페이스에 `getRouter` 메서드 추가
    ```java
    public interface RouterNetworkUseCase {

        Router addNetworkToRouter(RouterId routerId, Network network);

        Router getRouter(RouterId routerId);
    }
    ```
    * `getRouter` 메서드 - input `RouterId` output `Router`
    * 프론트엔드 애플리케이션이 라우터를 표시할 수 있게 해주는 메서드
* `RouterNetworkUseCase` 인터페이스를 implements 하는 `RouterNetworkInputPort` 클래스에 `getRouter` 메서드 구현
    ```java
    public class RouterNetworkInputPort implements RouterNetworkUseCase {
        /* 코드 생략 */
        @Override
        public Router getRouter(RouterId routerId) {
            notifyEventOutputPort.sendEvent("Retrieving router ID "+routerId.getUUID());
            return fetchRouter(routerId);
        }

        private Router fetchRouter(RouterId routerId) {
            return routerNetworkOutputPort.fetchRouterById(routerId);
        }
        /* 코드 생략 */
    }
    ```
    * 기존 `addNetworkToRouter` 구현에 사용된 private 메서드 `fetchRouter`를 사용했다.
* 입력 포트가 변경된 것을 입력 어댑터에도 전달해야 하므로 `RouterNetworkAdapter` 추상 클래스에 정의되어 있는 기본 입력 어댑터에 `getRouter` 메서드 생성
    ```java
    public abstract class RouterNetworkAdapter {
        /* 코드 생략*/
        public Router getRouter(Map<String, String> params) {
            var routerId = RouterId.withId(params.get("routerId"));
            return routerNetworkUseCase.getRouter(routerId);
        }
        /* 코드 생략*/
    ```
* 프론트엔드 애플리케이션이 헥사고날 시스템과 통신할 수 있게 하는 REST 어댑터 수정
    ```java
    public class RouterNetworkRestAdapter extends RouterNetworkAdapter {
        /* 코드 생략 */
        @Override
        public Router processRequest(Object requestParams){
            /* 코드 생략 */
            if (exchange.getRequestURI().getPath().equals("/network/add")) {
                try {
                    router = this.addNetworkToRouter(params);
                } catch (Exception e) {
                    exchange.sendResponseHeaders(400, e.getMessage().getBytes().length);
                    OutputStream output = exchange.getResponseBody();
                    output.write(e.getMessage().getBytes());
                    output.flush();
                }
            }
            if (exchange.getRequestURI().getPath().contains("/network/get")) {
                router = this.getRouter(params);
            }
            /* 코드 생략 */
        }
        /* 코드 생략 */
    ```

### 네트워크 추가 페이지 생성
### 라우터 가져오기 페이지 생성
### 테스트 에이전트 실행
* 프론트엔드 애플리케이션 말고 드리븐 오퍼레이션의 또 다른 일반적인 유형 -> 테스트와 모니터링 에이전트 (예: 포스트맨)
* 포스트맨과 뉴먼을 사용한 테스트 컬렉션 실행
  * 뉴먼을 사용해 테스트하는 것은 헥사고날 애플리케이션을 CI 파이프라인에 통합하기에 좋다.

### 애플리케이션 간의 헥사고날 시스템 호출
* 마이크로서비스와 분산 시스템
  * 전체 시스템이 제공하는 기능을 위해 협력하는 독립 실행형 애플리케이션 사이에서 일부 데이터가 네트워크를 통해 전송
  * 개발이 분리되고 컴포넌트가 모듈화됨
  * 패키지가 더 작아 컴파일 시간이 개선됨
  * CI 도구에서 더 빠른 피드백 루프를 만드는 데 기여
  * 네트워크 통신 오버헤드가 제한 요소로 나타날 수 있음
* 헥사고날 시스템 `A`가 헥사고날 시스템 `B`의 드라이빙 오퍼레이션에 대한 주요 액터로 동작하는 예시
  1. 시스템 `A`의 출력 어댑터 중 하나를 통해 요청 트리거
  2. 이 요청이 시스템 `B`의 입력 어댑터 중 하나로 직접 이동

## 드리븐 오퍼레이션을 통한 외부 리소스 처리
* 비즈니스 로직을 손상시키지 않고 외부 리소스와 상호작용하기 위해 출력 포트와 출력 어댑터를 사용한다.
* 이러한 외부 리소스를 보조 액터(secondary actor)라고 하며, 헥사고날 애플리케이션에 없는 데이터나 기능을 제공한다.
* 드리븐 오퍼레이션: 헥사고날 애플리케이션이 보조 액터에게 요청을 보내는 경우 (정확히는 유스케이스를 통해 들어온 드라이빙 오퍼레이션에 대한 요청을 보조 액터에게 보내는 경우)
* 드라이빙 오퍼레이션과 드리븐 오퍼레이션
  * 드라이빙 오퍼레이션: 헥사고날 시스템의 행위를 유도하는 주요 액터의 요청에서 비롯됨
  * 드리븐 오퍼레이션: 헥사고날 시스템애 의해 데이터베이스나 다른 시스템 같은 보조 액터 쪽으로 시작된 요청

### 데이터 지속성
* 데이터 지속성을 기반으로 하는 드리븐 오퍼레이션이 가장 일반적이다.
* 주로 ORM 기법을 사용한다.
* 트랜잭션 메커니즘도 지속성 기반 드리븐 오퍼레이션의 일부이다. 트랜잭션을 활용할 때 헥사고날 시스템이 직접 트랜잭션 경계를 처리하거나 이러한 책임을 애플리케이션 서버로 위임할 수 있다.

### 메시징과 이벤트 
* 시스템 컴포넌트 사이의 비동기식 통신
  * blocking: 애플리케이션 흐름이 진행되기 위해 응답을 기다려야 하는 연결
  * non-blocking: 애플리케이션이 요청을 보내고 즉각적인 응답 없이도 계속 진행할 수 있음
* kafka
  1. kafka 설치, 실행 후 토픽 생성
  2. 카프카로 이벤트를 내보내고 소비할 출력 포트와 출력 어댑터 추가
  3. 출력 어댑터 구현 (`NotifyEventKafkaAdapter`)
     * 카프카 토픽에 메시지를 보내기 위한 producer 를 생성하는 `createProducer` 메서드 작성
     * producer 가 생성한 메시지를 소비하는 consumer 를 생성하는 `createConsumer` 메서드 작성
     * 카프카 producer 인스턴스로 메시지를 보낼 `sendEvent` 메서드 작성
     * 카프카 consumer 인스턴스로 메시지를 소비하여 WebSocket 서버로 보낼 `getEvent` 메서드  작성
        * 카프카 consumer 인스턴스를 통해 카프카 토픽에서 메시지 검색
        * 검색한 메시지를 WebSocket 서버로 전달하는 `sendMessage` 호출
  4. WebSocket 서버 `NotifyEventWebSocketAdapter` 구현
     * `startServer` 메서드: WebSocket 서버의 호스트와 포트를 포함하는 `NotifyEventWebSockterAdapter` 인스턴스 생성
     * 헥사고날 애플리케이션을 시작할 때 `NotifyEventWebSockterAdapter` 의 `startServer` 메서드가 실행된다.
  5. 카프카에서 오는 이벤트를 처리하기 위한 WebSocket 클라이언트 클래스 `WebSocketClientAdapter` 구현
     * 카프카 토픽에서 메시지가 소비되면 WebSocket 서버로 메시지를 전달하기 위해 `NotifyEventKafkaAdapter`에서 사용됨
     * WebSocket 프로토콜 오퍼레이션 `onMessage`, `onOpen`, `onClose`, `onError` 구현
  6. 방금 생성한 출력 포트와 출력 어댑터를 사용해 이벤트를 보내는 입력 포트의 `addNetworkToRouter` 메서드와 `getRouter` 메서드 작성
     * 두 메서드 모두에서 `sendEvent`를 호출한다. 즉, 네트워크를 추가하거나 라우터를 검색할 때마다 이벤트를 보낸다.
  7. 프론트엔드 애플리케이션에 이벤트 페이지 추가
     * WebSocket 서버와 연결
     * 헥사고날 애플리케이션에서 생성된 모든 이벤트는 카프카로 보내지고 실시간으로 프론트엔드 애플리케이션에 출력된다.

### 모의 서버
* 개발 환경에서 통합 테스트 하기 -> 리소스를 동시에 사용하는 경우 동시성 문제 발생 가능
* 테스트 장애를 극복하기 위해, 애플리케이션 엔드포인트와 그것들의 응답을 시뮬레이션하는 모의 솔루션(mock solution) 등장
* mock server: 애플리케이션에 필요한 응답과 서비스 엔드포인트를 쉽게 mocking 하게 해준다.
* 이러한 모의 서버는, 모의 서버의 기능을 활용하고자 하는 헥사고날 시스템에 의해 유도된 보조 액터로 볼 수 있다.