# 10. 모듈화된 헥사고날 애플리케이션에 쿼커스 추가

## JVM 다시 살펴보기

- C, C++ 같은 언어에서는 소스코드를 특정 운영체제와 CPU 아키텍처에 맞게 조정된 네이티브 코드로 컴파일한다.
- 자바는 소스코드를 바이트코드로 컴파일하고 JVM은 바이트코드에 포함된 명령어를 이해할 수 있다.
- 프로그램은 VM 하고만 통신하고 VM은 바이트코드 명령어를 네이티브 코드 명령으로 변환한다.
- JVM의 장점
    - 한번 작성한 프로그램을 어디서나 실행할 수 있어 이식성이 좋다.
        - 최근에는 컨테이너 가상화를 통해서 런타임 환경 및 의존성을 함께 컨테이너 이미지로 패키징해서 이식성을 달성할 수 있다.
    - 가비지 컬렉터가 JVM 내부의 메모리 관리를 담당하여 메모리의 할당과 해제를 처리하는 방법을 걱정하지 않아도 된다.
        - 마크 앤 스윕 알고리즘
- 어플리케이션의 전체적인 수명주기를 담당한다.
- JVM의 바이트코드 로딩 성능을 개선하는 기법으로 JIT 컴파일과 AOT 컴파일이 있다.

### JIT 컴파일러를 통한 런타임 성능 향상

- 프로그램이 실행되는 동안 더 나은 성능을 위해 특정 프로그램 명령어가 최적화될 수 있다.
- 잠재적으로 최적화할 가능성이 있는 프로그램 명령어(가장 많이 실행되는 명령)를 찾고 바이트코드 부분을 네이티브 코드로 컴파일하고 최적화를 적용한다.
- 프로그램 실행 전 JIT 컴파일러가 수행하는 초기 최적화 때문에 애플리케이션의 시작 시간이 증가한다는 단점이 있다.

### AOT 컴파일을 통한 시작 시간 개선

- AOT를 사용하면 JVM과 바이트코드가 제공하는 크로스 플랫폼 기능을 포기한다.
- AOT 컴파일은 네이티브 바이너리를 만들기 때문에 실행 시간이 오래 걸려 CI 파이프라인에 큰 영향을 줄 수 있다.
- GraalVM은 자바 및 다른 자바 기반 언어에 대한 네이티브 바이너리를 제공하는 데 사용되는 AOT 컴파일러이다.

## 쿼커스 소개

- GraalVM을 기반으로 하는 네이티브 실행 파일을 지원해서 프로그램의 시작 시간을 단축할 수 있다.
- 라이브 개발을 제공하여 코드가 변경될 때마다 애플리케이션의 재시작을 방지해 생산성을 향상시킨다.
- 클라우드 기술을 다루기 위해 설계된 소프트웨어 개발 프레임워크이다.

### JAX-RS를 통한 REST 엔드포인트 생성

```java
@Path("/app")
public class RestExample {
	@GET
	@Path("/simple-rest")
	@Produces(MediaType.TEXT_PLAIN)
	public String simpleRest() {
		return "This REST endpoint is provided by Quarkus";
	}
}
```

- @Path 어노테이션으로 엔드포인트를 설정한다.
- @GET 을 통해 해당 엔드포인트에서 지원하는 HTTP 메서드를 설정한다.
- @Produces 를 사용해 요청에 대한 반환 타입을 정의한다.

### 쿼커스 DI를 통한 의존성 주입

- 쿼커스는 CDI 명세에서 유래한 쿼커스 ArC 기반으로 하는 자체적인 의존성 주입 메커니즘을 갖고 있다.

```java
@ApplicationScoped
public class BeanExample {
	public String simpleBean() {
		return "This is a simple bean";
	}
}
```

- @ApplicationScoped 애노테이션은 애플리케이션이 종료되지 않는 한 이 빈을 사용할 수 있다.

```java
@Path("/app")
public class RestExample {
	@Inject
	BeanExample beanExample;

	@GET
	@Path("/simple-rest")
	@Produces(MediaType.TEXT_PLAIN)
	public String simpleRest() {
		return beanExample.simpleBean();
	}
}
```

- @Inject 애노테이션을 통해 BeanExample 의존성을 주입한다.

### 객체의 유효성 검증

```java
public class SampleObject {
	@NotBlank(message = "The field cannot be empty")
	public String field;

	@Min(message = "The minimum value is 10", value = 10)
	public int value;
}
```

- @Blank 애노테이션을 사용해 field 변수의 값이 비어 있으면 안된다고 명시한다.
- @Min 애노테이션을 사용해 value 변수의 최소값을 보장한다.

```java
@POST
@Path("/request-validation")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public Result validation(@Valid SampleObject sampleObject) {
	try {
		return new Result("The request data is valid!");
	} catch (ConstraintViolationException e) {
		return new Result(e.getConstraintViolations());
	}
}
```

### 데이터 소스 구성 및 하이버네이트 ORM 사용

- JDBC 연결 기반

```java
@Entity
@NamedQuery(name = "SampleEntity.findAll",
	query = "SELECT f FROM SampleEntity f ORDER BY f.field",
	hints = @QueryHint(name = "org.hibernate.cacheable",
	value = "true"))
public class SampleEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Getter
	@Setter
	private String field;

	@Getter
	@Setter
	private int value;
}
```

```java
@ApplicationScoped
public class PersistenceExample {
	@Inject
	Entity em;

	@Transactional
	public String createEntity(SampleObject sampleObject) {
		SampleEntity sampleEntity = new SampleEntity();
		sampleEntity.setField(sampleObject.field);
		sampleEntity.setValue(sampleEntity.value);
		em.persist(sampleEntity);
		return "Entity with field " + sampleEntity.field + "created!";
	}

	@Transactional
	public List<SampleEntity> getAllEntities() {
		return em.createNamedQuery("SampleEntity.findAll", SampleEntity.class).getResultList();
	}
}
```

```java
@Inject
PersistenceExample persistenceExample;

@POST
@Path("/create-entity")
@Produces(MediaType.TEXT_PLAIN)
@Consumes(MediaType.APPLICATION_JSON)
public String persistData(@Valid SampleObject sampleObject) {
	return persistenceExample.createEntity(sampleObject);
}

@GET
@Path("/get-all-entites")
public List<SampleEntity> retrieveAllEntities() {
	return persistenceExample.getAllEntities();
}
```

### 모듈화된 헥사고날 애플리케이션에 쿼커스 추가

- 쿼커스 엔진을 구동하는 목적만 갖는 전용 모델을 만들고 다른 헥사고날 시스템 모듈과 애그리게이션 관계를 만들어 각 모듈의 책임이 모호해지는 것을 방지한다.
- bootstrap 모듈은 쿼커스 초기화에 필요한 의존성을 제공하는 애그리게이터 모듈이다.
