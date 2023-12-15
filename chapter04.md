# 04 외부와 상호작용하는 어댑터 만들기

## 어댑터 이해
* GoF 디자인 패턴에서의 어댑터: 두 분기 클래스의 인터페이스가 서로 호환되게 한다.
* 헥사고날 아키텍처에서의 어댑터: 시스템이 다른 기술이나 프로토콜과 호환되게 한다.
* 자바 애플리케이션끼리의 통신하는 기능
  * REST 나 gRPC 같은 통신 프로토콜을 사용할 수 있다.
  * 각 프로토콜마다 어댑터가 필요하다. -> REST 어댑터와 gRPC 어댑터를 만들어야 한다.
  * 입력 어댑터: 애플리케이션 기능을 노출하는 데 사용되는 어댑터
  * 이러한 입력을 시스템의 나머지 부분(애플리케이션 헥사곤, 도메인 헥사곤)에 연결하려면 입력 포트와 입력 어댑터를 연결해야 한다.
  * 출력 어댑터: 헥사고날 애플리케이션에서 생성된 데이터를 변환하고 외부 시스템과 통신하기 위한 어댑터

## 드라이빙 오퍼레이션 허용을 위한 입력 어댑터 사용
* 입력 어댑터는 헥사고날 시스템이 제공하는 기능에 `액세스 수단으로 지원되는 기술`을 정의한다.
* 입력 어댑터는 헥사곤 내부와 외부 사이의 명확한 경계를 표시하고 드라이빙 오퍼레이션을 수행한다.
* 액터: 헥사곤 외부에 있는, 헥사곤 애플리케이션과 상호작용하는 사용자나 시스템. 애플리케이션 유스케이스를 형성하는 중추적인 역할을 한다.
* 주요 액터는 헥사고날 시스템의 상태와 행위를 시작하게 하고 영향을 주므로 `드라이빙`으로 표현한다.
* DDD 기반 아키텍처 -> 레거시 시스템과 새로운 시스템을 통합할 때 반부패 계층(anti-corruption layer)으로써 어댑터를 사용할 수 있다.

### 입력 어댑터 생성
* 입력 포트: 유스케이스의 목표를 달성하기 위해 입력 포트가 수행하는 오퍼레이션의 수행 방법을 지정해 유스케이스를 구현하는 수단
* 입력 포트는 오퍼레이션 수행을 위해 **자극**<sup>stimulus</sup>을 수신해야 한다.
* 입력 포트 객체는 입력 어댑터가 보낸 자극을 통해 오퍼레이션을 수행하는 데 필요한 모든 데이터를 수신한다.
* 입력 데이터를 도메인 헥사곤과 호환되는 형식으로의 변환을 발생시키기도 한다.
* 라우터에 네트워크를 추가하는 유스케이스 예제
  * 어댑터 추상 base 클래스 정의
  * 두 개의 어댑터 구현
    * HTTP REST
    * CLI
  * 두 개의 어댑터에 대한 액세스 시뮬레이션 용도의 클라이언트 클래스

#### 기반 어댑터
* 추상 base 클래스 `RouterNetworkAdapter`
    ```java
    public abstract class RouterNetworkAdapter {

        protected Router router;
        protected RouterNetworkUseCase routerNetworkUseCase;

        protected Router addNetworkToRouter(Map<String, String> params){
            var routerId = RouterId.withId(params.get("routerId"));
            var network = new Network(IP.fromAddress(params.get("address")),
                    params.get("name"),
                    Integer.valueOf(params.get("cidr")));
            return routerNetworkUseCase.addNetworkToRouter(routerId, network);
        }

        public abstract Router processRequest(Object requestParams);
    }
    ```
    * 어댑터와 관련된 입력 포트와 통신을 위한 표준 오퍼레이션 제공
    * `addNetworkToRouter` 메서드: `RouterId`와 `Network` 객체를 만들기 위한 매개변수 수신
    * 입력 포트를 직접 참조하지 않고 유스케이스 인터페이스를 참조한다. 이 유스케이스 참조는 입력 어댑터의 생성자에 의해 전달되고 초기화된다.

#### REST 입력 어댑터
* REST 어댑터 클래스 `RouterNetworkRestAdapter`의 생성자에서 유스케이스 참조를 수신하여 초기화한다.
    ```java
        public RouterNetworkRestAdapter(RouterNetworkUseCase routerNetworkUseCase){
            this.routerNetworkUseCase = routerNetworkUseCase;
        }
    ```
* 클라이언트가 `RouterNetworkRestAdapter` 입력 어댑터를 호출하고 초기화하는 코드
    ```java
    RouterNetworkOutputPort outputPort = RouterNetworkH2Adapter.getInstance();
    RouterNetworkUseCase usecase = new RouterNetworkInputPort(outputPort);
    RouterNetworkAdapter inputAdapter = new RouterNetworkRestAdapter(usecase);
    ```
    * 나중에 이러한 모든 어댑터 생성자는 쿼커스나 스프링 같은 프레임워크에서 제공하는 의존성 주입 애노테이션을 사용해 제거할 수 있다.
* `processRequest` 메서드 구현
    ```java
    @Override
    public Router processRequest(Object requestParams){
        Map<String, String> params = new HashMap<>();
        if(requestParams instanceof HttpServer) {
            var httpserver = (HttpServer) requestParams;
            httpserver.createContext("/network/add", (exchange -> {
                if ("GET".equals(exchange.getRequestMethod())) {
                    var query = exchange.getRequestURI().getRawQuery();
                    httpParams(query, params);
                    router = this.addNetworkToRouter(params); // here!
                    ObjectMapper mapper = new ObjectMapper();
                    var routerJson = mapper.writeValueAsString(RouterJsonFileMapper.toJson(router));
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, routerJson.getBytes().length);
                    OutputStream output = exchange.getResponseBody();
                    output.write(routerJson.getBytes());
                    output.flush();
                } else {
                    exchange.sendResponseHeaders(405, -1);
                }
                exchange.close();
            }));
            httpserver.setExecutor(null);
            httpserver.start();
        }
        return router;
    }
    ```
    * `/network/add`에서 GET 요청을 수신하기 위해 HTTP 엔드포인트를 생성하는 데 사용되는 `httpServer` 객체를 수신한다.
    * `processRequest`를 호출하는 클라이언트 코드
        ```java
        var httpServer = HttpServer.create(new InetSocketAddress(8080), 0);
        routerNetworkAdapter.processRequest(httpServer);
        ```
  * REST 어댑터는 HTTP 요청을 통해 사용자 데이터를 수신하고 요청 매개변수를 파싱한다. 이 HTTP 요청을 통해 `RouterNetworkAdapter`의 `addNetworkToRouter`를 호출한다.
  * 입력 어댑터 `RouterNetworkAdapter`의 `addNetworkToRouter` 메서드는 입력 포트를 트리거하는 데 사용되는 입력 데이터(`routerId`, `network`)를 유스케이스 참조를 사용해 적절한 매개변수(`router`)로 변환한다.
    ```java
    Router router = routerNetworkUseCase.addNetworkToRouter(routerId, network);
    return router
    ```
  * 입력 어댑터에서 유스케이스 참조를 사용해 변환된 매개변수(`router`)는 프레임워크 헥사곤을 떠나 애플리케이션 헥사곤으로 간다.

#### CLI 입력 어댑터
* 위와 같은 입력 포트에 다른 어댑터(`RouterNetworkCLIAdapter`)를 연결해본다.
* base 클래스 `RouterNetworkCLIAdapter`를 extends 한 어댑터 클래스 `RouterNetworkCLIAdapter`
    ```java
    public class RouterNetworkCLIAdapter extends RouterNetworkAdapter {
        public RouterNetworkCLIAdapter(RouterNetworkUseCase routerNetworkUseCase) {
            this.routerNetworkUseCase = routerNetworkUseCase;
        }
        /* 코드 생략 */
    }
    ```
  * 유스케이스를 수신하고 초기화하기 위해 생성자를 정의한다.
* `RouterNetworkCLIAdapter` 입력 어댑터를 초기화하는 클라이언트 코드
    ```java
    RouterNetworkOutputPort outputPort = RouterNetworkFileAdapter.getInstance();
    RouterNetworkUseCase usecase = new RouterNetworkInputPort(outputPort);
    RouterNetworkAdapter inputAdapter = new RouterNetworkCLIAdapter(usecase);
    ```
* `processRequest` 메서드 구현
    ```java
    @Override
    public Router processRequest(Object requestParams){
        var params = stdinParams(requestParams);
        router = this.addNetworkToRouter(params);
        ObjectMapper mapper = new ObjectMapper();
        try {
            var routerJson = mapper.writeValueAsString(RouterJsonFileMapper.toJson(router));
            System.out.println(routerJson);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return router;
    }
    ```
    * REST 어댑터의 `httpParams` 메서드 - HTTP 요청으로부터 데이터 추출
    * CLI 어댑터의 `stdinParams` 메서드 - 콘솔로부터 데이터 추출
    * REST 어댑와 CLI 어댑터 모두 추출한 데이터를 `params` 라는 변수에 담아 `addNetworkToRouter` 메서드를 호출한다.


#### 입력 어댑터 호출하기
* 선택할 어댑터를 제어하기 위한 클라이언트 코드
    ```java
    public class App {
        /* 코드 생략 */
            void setAdapter(String adapter) {
            switch (adapter){
                case "rest":
                    outputPort = RouterNetworkH2Adapter.getInstance();
                    usecase = new RouterNetworkInputPort(outputPort);
                    inputAdapter= new RouterNetworkRestAdapter(usecase);
                    rest();
                    break;
                default:
                    outputPort = RouterNetworkFileAdapter.getInstance();
                    usecase = new RouterNetworkInputPort(outputPort);
                    inputAdapter= new RouterNetworkCLIAdapter(usecase);
                    cli();
            }
        }
    }
    ```
  * 프로그램을 실행할 때 rest 를 매개변수로 전달하면 REST 어댑터 인스턴스를 생성하고 `rest` 메서드를 호출한다. 
* `rest` 메서드는 REST 입력 어댑터의 `processRequest` 메서드를 호출한다. 
    ```java
    private void rest() {
        try {
            System.out.println("REST endpoint listening on port 8080...");
            var httpserver = HttpServer.create(new InetSocketAddress(8080), 0);
            inputAdapter.processRequest(httpserver);
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    ```
* `cli` 메서드는 CLI 입력 어댑터의 `processRequest` 메서드를 호출한다.
    ```java
    private void cli() {
        Scanner scanner = new Scanner(System.in);
        inputAdapter.processRequest(scanner);
    }
    ```

## 다양한 데이터 소스와 통신하기 위한 출력 어댑터 사용
* 무언가를 저장해야 하는 필요성
  * RDBMS
  * NoSQL
  * 파일 시스템
  * 메시지 브로커
  * 디렉터리 기반 스토리지(LDAP)
  * 메인프레임 스토리지
* 이럴 때 사용할 수 있는 게 다양한 종류의 출력 어댑터

### 출력 어댑터 생성
* 출력 어댑터는 드리븐 오퍼레이션을 처리한다.
* 드리븐 오퍼레이션: 일부 데이터를 보내거나 받기 위해 외부 시스템과 상호작용하는 헥사고날 애플리케이션 자체에 의해 시작된 오퍼레이션
* 드리븐 오퍼레이션은 유스케이스를 통해 서술되며, 유스케이스의 입력 포트 구현에 있는 드라이빙 오퍼레이션에 의해 트리거된다.
* 출력 어댑터를 통해 데이터 지속성과 기타 유형의 외부 통합을 허용하기 위해 사용할 수 있는 기술을 결정할 수 있다. 
* 애플리케이션 헥사곤 - **출력 포트** 인터페이스
* 프레임워크 헥사곤 - 출력 포트의 구현인 **출력 어댑터**
* 애플리케이션 헥사곤의 출력 포트는 도메인 헥사곤의 도메인 모델에 의존해야 한다.
* 예제로 보는 두 개의 출력 어댑터
  * `RouterNetworkH2Adapter`: 인메모리 데이터베이스의 데이터 처리
  * `RouterNetworkFileAdapter`: 로컬 파일 시스템에서 파일을 읽고 유지

### H2 출력 어댑터
* H2 출력 어댑터를 구현하기 전에 먼저 토폴로지 및 인벤토리 시스템의 데이터베이스 구조를 정의해야 한다.
    ```sql
    CREATE TABLE routers(
        router_id UUID PRIMARY KEY NOT NULL,
        router_type VARCHAR(255)
    );
    CREATE TABLE switches (
        switch_id UUID PRIMARY KEY NOT NULL,
        router_id UUID,
        switch_type VARCHAR(255),
        switch_ip_protocol VARCHAR(255),
        switch_ip_address VARCHAR(255),
        PRIMARY KEY (switch_id),
        FOREIGN KEY (router_id) REFERENCES routers(router_id)
    );
    CREATE TABLE networks (
        network_id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
        switch_id UUID,
        network_protocol VARCHAR(255),
        network_address VARCHAR(255),
        network_name VARCHAR(255),
        network_cidr VARCHAR(255),
        PRIMARY KEY (network_id),
        FOREIGN KEY (switch_id) REFERENCES switches(switch_id)
    );

    # insert 문 생략
    ```
    * switches 는 엔티티로, networks 를 도메인 모델의 Router 엔티티의 일부인 값 객체로 취급한다.
    * switches 와 networks 의 기본키는 도메인 모델에서는 참조로 사용하지 않는다.
    * 대신 Router 엔티티와 switch 와 network 객체, 그리고 이들의 개별 데이터베이스 테이블을 연관시키기 위해 router_id 값을 사용한다.
    * Router 가 애그리게잇 루트이고, Switch 와 Network 는 애그리게잇 구성 요소가 된다.
* 출력 포트 `RouterNetworkOutputPort`의 구현인 출력 어댑터 `RouterNetworkH2Adapter`
    ```java
    public class RouterNetworkH2Adapter implements RouterNetworkOutputPort {

        private static RouterNetworkH2Adapter instance;

        @PersistenceContext
        private EntityManager em;

        private RouterNetworkH2Adapter(){
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
            EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("inventory");
            EntityManager em = entityManagerFactory.createEntityManager();
            this.em = em;
        }
        /* 코드 생략 */
    }
    ```
  * `fetchRouterById`
    * EntityManager 참조를 사용해 H2 데이터베이스에서 라우터를 가져오기 위해 routerId 수신하는 메서드
    * toDomain 메서드를 사용하는 이유
      * Router 도메인 엔티티를 데이터베이스에 바로 매핑할 수 없다.
      * 데이터베이스 엔티티를 도메인 엔티티로 사용할 수도 없다.
  * `persistRouter`: 도메인 엔티티를 데이터베이스에 매핑하기 위해 toH2 메서드를 사용한다.
  * `setUpH2Database`: 애플리케이션을 시작할 때 데이터베이스를 구동한다.
    ```java
        public static RouterNetworkH2Adapter getInstance() {
            if (instance == null) {
                instance = new RouterNetworkH2Adapter();
            }
            return instance;
        }
    ```
  * `getInstance`: H2 어댑터의 인스턴스를 하나만 생성하기 위해 싱글턴을 정의한다.
* 도메인 모델은 데이터베이스 엔티티와 같아선 안된다.
* 데이터베이스 타입에 직접 매핑하기 위해 `RouterData`` ORM 클래스를 만들어야 한다. 여기서는 EclipseLink 를 사용하지만 모든 JPA 호환 구현을 사용할 수 있다.
    ```java 
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Entity
    @Table(name = "routers")
    @SecondaryTable(name = "switches")
    @MappedSuperclass
    @Converter(name="uuidConverter", converterClass= UUIDTypeConverter.class)
    public class RouterData implements Serializable {

        @Id
        @Column(name="router_id",
                columnDefinition = "uuid",
                updatable = false )
        @Convert("uuidConverter")
        private UUID routerId;

        @Embedded
        @Enumerated(EnumType.STRING)
        @Column(name="router_type")
        private RouterTypeData routerType;


        @OneToOne(cascade = CascadeType.ALL)
        @JoinColumn(table = "switches",
                name = "router_id",
                referencedColumnName = "router_id")
        private SwitchData networkSwitch;
    }
    ```
* EclipseLink 에서 UUID 를 ID 로 사용하기 위한 변환기 클래스
    ```java
    public class UUIDTypeConverter implements Converter {
        @Override
        public UUID convertObjectValueToDataValue(Object objectValue, Session session) {
            return (UUID) objectValue;
        }

        @Override
        public UUID convertDataValueToObjectValue(Object dataValue, Session session) {
            return (UUID) dataValue;
        }

        @Override
        public boolean isMutable() {
            return true;
        }

        @Override
        public void initialize(DatabaseMapping mapping, Session session) {
            DatabaseField field = mapping.getField();
            field.setSqlType(Types.OTHER);
            field.setTypeName("java.util.UUID");
            field.setColumnDefinition("UUID");
        }
    }
    ```
* `RotuerData`의 필드 `networkSwtich`가 참조하는 `SwitchData`
    ```java
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Entity
    @Table(name = "switches")
    @SecondaryTable(name = "networks")
    @MappedSuperclass
    @Converter(name="uuidConverter", converterClass= UUIDTypeConverter.class)
    public class SwitchData implements Serializable {

        @Id
        @Column(name="switch_id",
                columnDefinition = "uuid",
                updatable = false )
        @Convert("uuidConverter")
        private UUID switchId;

        @Column(name="router_id")
        @Convert("uuidConverter")
        private UUID routerId;

        @Enumerated(EnumType.STRING)
        @Embedded
        @Column(name = "switch_type")
        private SwitchTypeData switchType;

        @OneToMany
        @JoinColumn(table = "networks",
                name = "switch_id",
                referencedColumnName = "switch_id")
        private List<NetworkData> networks;

        @Embedded
        @AttributeOverrides({
                @AttributeOverride(
                        name = "address",
                        column = @Column(
                                name = "switch_ip_address")),
                @AttributeOverride(
                        name = "protocol",
                        column = @Column(
                                name = "switch_ip_protocol")),
        })
        private IPData ip;
    }
    ```
* `NetworkData`
    ```java
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Entity
    @Table(name = "networks")
    @MappedSuperclass
    @Converter(name="uuidConverter", converterClass= UUIDTypeConverter.class)
    public class NetworkData implements Serializable {

        @Id
        @Column(name="network_id")
        private int id;

        @Column(name="switch_id")
        @Convert("uuidConverter")
        private UUID switchId;

        @Embedded
        @AttributeOverrides({
                @AttributeOverride(
                        name = "address",
                        column = @Column(
                                name = "network_address")),
                @AttributeOverride(
                        name = "protocol",
                        column = @Column(
                                name = "network_protocol")),
        })
        IPData ip;

        @Column(name="network_name")
        String name;

        @Column(name="network_cidr")
        Integer cidr;

        public NetworkData(UUID switchId, IPData ip, String name, Integer cidr) {
            this.switchId = switchId;
            this.ip = ip;
            this.name = name;
            this.cidr = cidr;
        }
    }
    ```
* `IPData`
    ```java
    @Embeddable
    @Getter
    public class IPData {

        private String address;

        @Enumerated(EnumType.STRING)
        @Embedded
        private ProtocolData protocol;

        private IPData(String address){
            if(address == null)
                throw new IllegalArgumentException("Null IP address");
            this.address = address;
            if(address.length()<=15) {
                this.protocol = ProtocolData.IPV4;
            } else {
                this.protocol = ProtocolData.IPV6;
            }
        }

        public IPData() {

        }

        public static IPData fromAddress(String address){
            return new IPData(address);
        }
    }
    ```
* `ProtocolData`
    ```java
    @Embeddable
    public enum ProtocolData {
        IPV4,
        IPV6;
    }
    ```
* 매퍼(mapper) 메서드를 갖는 매퍼 클래스 `RouterH2Mapper` 생성
  * `toDomain` 메서드: 데이터베이스 엔티티 `RouterData` -> 도메인 엔티티 `Router`
    ```java
    public static Router toDomain(RouterData routerData){
        /* 코드 생략 */
        return new Router(routerType, routerId, networkSwitch);
    }
    ```
    * 출력 어댑터에서 데이터베이스 조회 후 H2 데이터베이스 객체를 도메인 모델로 변환할 때 사용한다.
  * `getNetworksFromData` 메서드: 데이터베이스 엔티티 `RouterData` List -> 도메인 엔티티 `Network` List
    ```java
    private static List<Network> getNetworksFromData(List<NetworkData> networkData){
        List<Network> networks = new ArrayList<>();
        networkData.forEach(data ->{
            var network = new Network(
                    IP.fromAddress(data.getIp().getAddress()),
                    data.getName(),
                    data.getCidr());
            networks.add(network);
        });
        return networks;
    }
    ```
  * `toH2`: 도메인 엔티티 `Router` -> 데이터베이스 엔티티 `RouterData`
    ```java
    public static RouterData toH2(Router router){
        /* 코드 생략 */
        return new RouterData(routerId, routerTypeData, switchData);
    }
    ```
    * 출력 어댑터에서 도메인 모델 엔티티를 H2 데이터베이스에 저장할 때 사용한다.
  * `getNetworksFromDomain`: 도메인 엔티티 `Network` List -> 데이터베이스 엔티티 `NetworkData` List
    ```java
    private static List<NetworkData> getNetworksFromDomain(List<Network> networks, UUID switchId){
        List<NetworkData> networkDataList = new ArrayList<>();
        networks.forEach(network ->{
            var networkData = new NetworkData(
                    switchId,
                    IPData.fromAddress(network.getAddress().getIPAddress()),
                    network.getName(),
                    network.getCidr()
            );
            networkDataList.add(networkData);
        });
        return networkDataList;
    }
    ```
* `RouterNetworkOutputPort`는 `RouterNetworkInputPort` 생성자에 전달한 매개변수를 기반으로 런타임에 처리된다.
* 이 기법을 통해 헥사고날 시스템은 데이터를 어디서 어떻게 가져오는지를 감춘다.

### 파일 어댑터
* 데이터 소스로 사용할 inventory.json 파일
    ```json
    [{
        "routerId": "ca23800e-9b5a-11eb-a8b3-0242ac130003",
        "routerType": "EDGE",
        "switch":{
        "switchId": "922dbcd5-d071-41bd-920b-00f83eb4bb46",
        "ip": {
            "protocol": "IPV4", "address": "9.0.0.9"
        },
        "switchType": "LAYER3",
        "networks":[
            {
            "ip": {
                "protocol": "IPV4", "address": "10.0.0.0"
            },
            "networkName": "HR", "networkCidr": "8"
            },
            {
            "ip": {
                "protocol": "IPV4", "address": "20.0.0.0"
            },
            "networkName": "Marketing", "networkCidr": "8"
            },
            {
            "ip": {
                "protocol": "IPV4", "address": "30.0.0.0"
            },
            "networkName": "Engineering", "networkCidr": "8"
            }
        ]
        }
    }]
    ```
* 출력 포트 `RouterNetworkOutputPort` 구현인 출력 어댑터 `RouterNetworkFileAdapter`
    ```java
    public class RouterNetworkFileAdapter implements RouterNetworkOutputPort {
        /* 코드 생략 */
        @Override
        public Router fetchRouterById(RouterId routerId) {
            Router router = new Router();
            for(RouterJson routerJson: routers){
                if(routerJson.getRouterId().equals(routerId.getUUID())){
                    router = RouterJsonFileMapper.toDomain(routerJson);
                    break;
                }
            }
            return router;
        }

        @Override
        public boolean persistRouter(Router router) {
            var routerJson = RouterJsonFileMapper.toJson(router);
            try {
                String localDir = Paths.get("").toAbsolutePath().toString();
                File file = new File(localDir + "/inventory.json");
                file.delete();
                objectMapper.writeValue(file, routerJson);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
        /* 코드 생략 */
    }
    ```
    * `fetchRouterById`: `RouterId` 매개변수를 사용해 .json 파일을 파싱해서 `Router` 객체를 반환한다.
    * `persistRouter`: inventory.json 파일의 변경 사항을 저장한다.
* JSON 데이터의 직렬화와 역직렬화에 Jackson 라이브러리를 사용한다.
    ```java
    private void readJsonFile(){
        try {
            this.routers = objectMapper.
                    readValue(resource, new TypeReference<List<RouterJson>>(){});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private RouterNetworkFileAdapter() {
        this.objectMapper = new ObjectMapper();
        this.resource = getClass().
                getClassLoader().
                getResourceAsStream("inventory.json");
        readJsonFile();
    }
    ```
    * 생성자: inventory.json 파일을 메모리에 로드하기 위해 `readJsonFile` 메서드 호출
* `RouterJson`
    ```java
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    public class RouterJson {

        @JsonProperty("routerId")
        private UUID routerId;

        @JsonProperty("routerType")
        private RouterTypeJson routerType;

        @JsonProperty("switch")
        private SwitchJson networkSwitch;
    }
    ```
* `RouterJsonFileMapper`: JSON 객체와 도메인 모델 객체 사이의 매핑을 위한 특별한 클래스!
    * `toDomain`
    * `toJson`
    * `getNetworksFromJson`
    * `getNetworksFromDomain`
  
### 출력 어댑터 호출
* 입력 어댑터를 REST 로 지정하면 H2 출력 어댑터를 사용하도록 구현되어 있다.
* 입력 어댑터를 CLI 로 지정하면 파일 출력 어댑터를 사용하도록 구현되어 있다.
* 출력 어댑터를 만들기 위한 유일한 요구사항 - 애플리케이션 헥사곤에서 출력 포트 인터페이스를 구현하는 것

## 요약
* 드라이빙 오퍼레이션을 허용하는 두 개의 입력 어댑터 - 같은 입력 포트 사용
* 드리븐 오퍼레이션을 수행하는 두 개의 출력 어댑터 - 같은 출력 포트 사용 