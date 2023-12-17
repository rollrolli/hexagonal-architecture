# 4. 외부와 상호작용하는 어댑터 만들기

## 어댑터 이해

- 시스템이 다른 기술이나 프로토콜과 호환되게 만들기 위해 어댑터를 사용한다.
- 헥사고날 아키텍처에서 제공되는 기능은 애플리케이션 헥사곤과 도메인 헥사곤에서 제공되는 유스케이스, 포트, 그리고 비즈니스 규칙으로 구성된다.
- REST, gRPC 클라이언트에서 해당 기능을 사용하고 싶다면 REST, gRPC 어댑터를 만들어야 한다.
    - 애플리케이션 기능을 노출하는 데 사용되는 어댑터를 입력 어댑터라고 부른다.
    - 입력을 시스템의 나머지 부분에 연결하려면 입력 포트를 입력 어댑터와 연결시킨다.
- 헥사고날 애플리케이션에서 생성된 데이터를 변환하고 외부 시스템과 통신하기 위해 출력 어댑터를 정의할 수 있다.

## 드라이빙 오퍼레이션 허용을 위한 입력 어댑터 사용

- 입력 어댑터는 소프트웨어를 다른 기술들과 호환되게 만드는 요소이다.
    - 헥사곤 내부와 외부 사이의 명확한 경계를 표시하고 드라이빙 오퍼레이션을 수행한다.
- 주요 액터는 헥사곤 외부에서 헥사곤 어플리케이션과 상호작용하는 사용자나 시스템을 가리켜 애플리케이션 유스케이스를 형성하는 중추적인 역할을 한다.
- 주요 액터와 헥사고날 애플리케이션 사이의 상호작용은 입력 어댑터를 통해 일어난다.
    - 이런 상호작용은 드라이빙 오퍼레이션으로 정의된다.
- DDD에서는 어댑터를 사용하는 다른 목적을 제안한다.
    - 레거시 시스템의 요소를 새로운 시스템에 통합하고 싶을 때, 반부패 계층을 사용하여 레거시 시스템과 새로운 시스템 모두에서 경계 컨텍스트를 통합하는 데 어댑터를 사용할 수 있다.
        - 이렇게 하면 새로운 시스템의 설계가 레거시 시스템의 설계로 인해 오염되는 것을 방지하는 역할을 한다.

### 입력 어댑터 생성

- 입력 포트는 유스케이스의 목표를 달성하기 위해 입력 포트가 수행하는 오퍼레이션의 수행 방법을 지정해 유스케이스를 구현하는 수단이다.

```kotlin
abstract class RouterNetworkAdapter {
    protected val router: Router
    protected val routerNetworkUseCase: RouterNetworkUseCase

    protected fun addNetworkRouter(params: Map<String, String>): Router {
        val routerId = RouterId.withId(params.get("routerId"))
        val network = Network(IP.fromAddress(params.get("address")), params.get("name"), Integer.valueOf(params.get("cidr")))
    }

    abstract fun processRequest(requestParams: Any): Router
}
```

- 어댑터와 관련된 입력 포트와 통신을 위한 표준 오퍼레이션을 제공한다.
- 입력 포트를 직접 참조하지 않고 유스케이스 인터페이스 참조를 활용한다.
- 유스케이스 참조는 입력 어댑터의 생성자에 의해 전달되고 초기화된다.

```kotlin
// 프레임워크 기반 의존성 주입 기법을 사용하면 제거할 수 있다.
val outputPort = RouterNetworkH2Adapter.getInstance()
val usecase = RouterNetworkInputPort(outputPort)
val inputAdapter = RouterNetworkRestAdapter(usecase)

class RouterNetworkRestAdapter(
    private val outerNetworkUseCase: RouterNetworkUseCase,
): RouterNetworkAdapter() {
    override fun processRequest(requestParams: Any): Router {
				/* ... */
        httpserver.createContext("/network/add") {
            if("GET" == it.requestMethod) {
                val query = it.requestURI.rawQuery
                httpParams(query, params)
                router = this.addNetworkRouter(params)
                val mapper = ObjectMapeer()
                val routerJson = mapper.writeValueAsString(RouterJsonFileMapper.toJson(router))
                it.reponseHeaders["Content-Type"] = "application/json"
                it.sendResponseHeaders(200, routerJson.bytes.length)
                val output = it.responseBody
                output.write(routerJson.bytes)
                output.flush()
            } else {
                it.sendResponseHeaders(405, -1)
            }
        }
				/* ... */
    }
}
```

```kotlin
// 프레임워크 기반 의존성 주입 기법을 사용하면 제거할 수 있다.
val outputPort = RouterNetworkH2Adapter.getInstance()
val usecase = RouterNetworkInputPort(outputPort)
val inputAdapter = RouterNetworkCLIAdapter(usecase)

class RouterNetworkCLIAdapter (
    private val outerNetworkUseCase: RouterNetworkUseCase,
): RouterNetworkAdapter() {
    override fun processRequest(requestParams: Any): Router {
        val params = stdinParams(requestParams)
        router = this.addNetworkRouter(params)
        val mapper = ObjectMapper()
        try {
            val routerJson = mapper.writeValueAsString(RouterJsonFileMapper.toJson(router))
            println(routerJson)
        } catch (e: JsonProcessingException) {
            e.printStackTrace()
        }
        return router
    }
}
```

```kotlin
class App {
    /* ... */
    fun setAdapter(adapter: String): Unit {
        when(adapter) {
            "rest" -> {
                outputPort = RouterNetworkH2Adapter.getInstance()
                usecase = RouterNetworkInputPort(outputPort)
                inputAdapter = RouterNetworkRestAdapter(usecase)
                rest()
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

		private fun rest() {
        try {
            println("REST endpoint listening on port 8080...")
            val httpserver = HttpServer.create(InetSocketAddress(8080), 0)
            inputAdapter.processRequest(httpserver)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    
    private fun cli() {
        val scanner = Scanner(System.`in`)
        inputAdapter.processRequest(scanner)
    }
}
```

## 다양한 데이터 소스와 통신하기 위한 출력 어댑터 사용

- 객체지향 언어와 엔터프라이즈 소프트웨어는 데이터를 획득하고 유지하는 방법에 의존한다.

### 출력 어댑터 생성

- 출력 어댑터는 드리븐 오퍼레이션을 처리한다.
    - 드리븐 오퍼레이션은 일부 데이터를 보내거나 받기 위해 외부 시스템과 상호작용하는 헥사고날 애플리케이션 자체에 의해 시작된 오퍼레이션이다.
    - 드리븐 오퍼레이션은 유스케이스를 통해 서술되며, 유스케이스의 입력 포트 구현에 있는 오퍼레이션에 의해 트리거된다.
- 애플리케이션 헥사곤에 있는 출력 포트는 외부 시스템과의 상호작용을 추상적인 방법으로 표현하고, 출력 어댑터는 이러한 상호작용이 발생하는 방법을 구체적인 용어로 설명한다.
- 출력 어댑터를 통해 시스템이 데이터 지속성과 기타 유형의 외부 통합을 허용하기 위해 사용할 수 있는 기술을 결정할 수 있다.

```kotlin
class RouterNetworkH2Adapter: RouterNetworkOutputPort {
    private lateinit var em: EntityManager

    companion object {
        private lateinit var instance: RouterNetworkH2Adapter

        fun getInstance(): RouterNetworkH2Adapter {
            if(instance == null) {
                instance = RouterNetworkH2Adapter()
            }
            return instance
        }
    }

    init {
        setUpH2Database()
    }

    override fun fetchRouterById(routerId: RouterId): Router {
        val routerData = em.getReference(RouterData::class, routerId.uuid)
        return RouterH2Mapper.toDomain(routerData)
    }

    override fun persistRouter(router: Router): Boolean {
        val routerData = RouterH2Mapper.toH2(router)
        em.persist(routerData)
        return true
    }

    private fun setUpH2Database {
        val entityManagerFactory = Persistence.createEntityManagerFactory("inventory")
        val em = entityManagerFactory.createEntityManager()
        this.em = em
    }

    /* ... */
}
```

```kotlin
@Getter
@AllArgsConstructor
@NoArgsConstrucor
@Entity
@Table(name="router")
@SecondaryTable(name="switches")
@MappedSuperclass
@Converter(name="uuidConverter", converterClass=UUIDTypeConverter::class)
class RouterData: Serializable {
	@Id
	@Column(name="router_id", columnDefinition="uuid", updatable=false)
	@Convert("uuidConverter")
	private routerId: UUID

	@Embedded
	@Enumerated(EnumType.STRING)
	@Column(name="router_type")
	private routerType: RouterTypeData

	@OneToOne(cascade=CascadeType.ALL)
	@JoinColumn(table="switches", name="router_id", referencedColumnName="router_id")
	private networkSwitch: SwitchData
}

class UUIDTypeConverter: Converter {
	override fun convertObjectValueToDataValue(objectValue: Any, session: Session): UUID {
		return (UUID) objectValue
	}

	override fun convertDataValueToObjectValue(dataValue: Any, session: Session): UUID {
		return (UUID) dataValue
	}

	override fun isMutable(): Boolean {
		return true
	}

	override fun initialize(mapping: DatabaseMapping, session: Session) {
		val field = mapping.field()
		field.sqlType = Types.OTHER
		field.typeName = "java.util.UUID"
		field.columnDefinition = "UUID"
	}
}

@Embeddable
enum RouterTypeData {
	EDGE,
	CORE,
}
```

```kotlin
@Getter
@AllArgsConstructor
@NoArgsConstrucor
@Entity
@Table(name="switches")
@SecondaryTable(name="networks")
@MappedSuperclass
@Converter(name="uuidConverter", converterClass=UUIDTypeConverter::class)
class SwitchData: Serializable {
	@Id
	@Column(name="switch_id", columnDefinition="uuid", updatable=false)
	@Convert("uuidConverter")
	private switchId: UUID

	@Column(name="router_id")
	@Convert("uuidConverter")
	private routerId: UUID

	@Embedded
	@Enumerated(EnumType.STRING)
	@Column(name="switch_type")
	private switchType: SwitchTypeData

	@OneToMany
	@JoinColumn(table="networks", name="switch_id", referencedColumnName="switch_id")
	private networks: List<NetworkData>

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name="address", column="@Column(name="switch_ip_address")),
		@AttributeOverride(name="protocol", column="@Column(name="switch_ip_protocol")),
	})
	private ip: IPData
}

@Embeddable
enum SwitchTypeData {
	LAYER2,
	LAYER3,
}
```

```kotlin
@Getter
@AllArgsConstructor
@NoArgsConstrucor
@Entity
@Table(name="networks")
@MappedSuperclass
@Converter(name="uuidConverter", converterClass=UUIDTypeConverter::class)
class NetworkData: Serializable {
	@Id
	@Column(name="network_id")
	private id: Int

	@Column(name="switch_id")
	@Convert("uuidConverter")
	private switch_id: UUID

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name="address", column="@Column(name="network_address")),
		@AttributeOverride(name="protocol", column="@Column(name="network_protocol")),
	})
	private ip: IPData

	@Column(name="network_name")
	private name: String

	@Column(name="network_cidr")
	private cidr: Integer
}

@Embeddable
@Getter
class IPData {
	private address: String

	@Enumerated(EnumType.STRING)
	@Embedded
	private protocol: ProtocolData

	private IPData(address: String) {
		if(address == null) {
			throw IllegalArgumentException("Null IP address")
		}
		this.address = address
		if(address.length <= 15) {
			this.protocol = ProtocolData.IPV4
		} else {
			this.protocol = ProtocolData.IPV6
		}
	}

	companion object {
		fun fromAddress(address: String): IPData {
			return IPData(address)
		}
	}
}

@Embeddable
enum ProtocolData {
	IPV4,
	IPV6,
}
```

```kotlin
class RouterH2Mapper {	
	companion object {
		fun toDomain(routerData: RouterData): Router {
			/* ... */
			return Router(routerType, routerId, networkSwitch)
		}

		fun getNetworksFromData(networkData: List<NetworkData>): List<Network> {
			val networks = listOf<Network>()
			networkData.forEach {
				val network = Network(IP.fromAddress(it.ip.address, it.name, it.cidr)
				networks.add(network)
			}
			return networks
		}
		
		fun toH2(router: Router): RouterData {
			/* ... */
			return RouterData(routerId, routerTypeData, switchData)
		}

		fun getNetworksFromDomain(networks: List<Network>, switchId: UUID): List<NetworkData> {
			val networkDataList = listOf<NetworkData>()
			networks.forEach {
				val networkData = NetworkData(switchId, IPData.fromAddress(it.address.ipAddress), it.name, it.cidr)
				networkDataList.add(networkData)
			}
			return networkDataList
		}	
	}
}
```

```kotlin
class RouterNetworkFileAdapter: RouterNetworkOutputPort {
	/* ... */

	init {
		this.objectMapper = ObjectMapper()
		this.resource = getClass().getClassLoader().getResourceAsStream("inventory.json")
		readJsonFile()
	}

	override fun fetchRouterById(routerId: RouterId) {
		var router = Router()
		for(routerJson in routers) {
			if(routerJson.routerId == routerId.uuid) {
				router = RouterJsonFileMapper.toDomain(routerJson)
				break
			}
		}
		return router
	}

	override fun persistRouter(router: Router): Boolean {
		val routerJson = RouterJsonFileMapper.toJson(router)
		try {
			val localDir = Paths.get("").toAbsolutePath().toString()
			val file = File(localDir + "/inventory.json")
			file.delete()
			objectMapper.writeValue(file, routerJson)
		} catch(e: IOException) {
			e.printStackTrace()
		}
		return true
	}

	private fun readJsonFile() {
		try {
			this.routers = objectMapper.readValue(resource, TypeReference<List<RouterJson>>(){})
		} catch(e: Exception) {
			e.printStackTrace()
		}
	}
}
```

```kotlin
@JsonInclude(value=JsonInclude.Include.NON_NULL)
class RouterJson {
	@JsonProperty("routerId")
	private routerId: UUID

	@JsonProperty("routerType")
	private routerType: RouterTypeJson

	@JsonProperty("switch")
	private networkSwitch: SwitchJson
}
```

```kotlin
class RouterJsonFileMapper {
	companion object {
		fun toDomain(routerJson: RouterJson): Router {
			return Router(routerType, routerId, networkSwitch)
		}

		fun getNetworksFromJson(networkJson: List<NetworkJson>): List<Network> {
			val newtorks = listOf<Network>()
			networkJson.forEach {
				val network = Network(IP.fromAddress(json.getIp().getAddress(), json.getNetworkName(), Integer.valueOf(json.getCider()))
				networks.add(network)	
			}
			return networks
		}

		fun toJson(router: Router): RouterJson {
			/* ... */
			return RouterJson(routerId, routerTypeJson, switchJson)
		}

		fun getNetworksFromDomain(networks: List<Network>): List<NetworkJson> {
			val networkJsonList = listOf<NetworkJson>()
			networks.forEach {
				val networkJson = NetworkJson(
					IPJson.fromAddress(it.getAddress().getIPAddress()),
					network.getName(),
					String.valueOf(network.getCidr())
				)
				networkJsonList.add(networkJson)
			}
			return networkJsonList
		}
	}
}
```

- 두 개의 출력 어댑터를 생성해 헥사고날 애플리케이션이 서로 다른 데이터 소스와 통신할 수 있게 되었다.
    - 좋은 점은 도메인 헥사곤이나 애플리케이션 헥사곤에서 아무것도 변경할 필요가 없다.
- 출력 어댑터를 만들기 위해 애플리케이션 헥사곤의 출력 포트 인터페이스만 구현하면 되므로 기술적인 관심사로부터 비즈니스 로직을 보호한다.
