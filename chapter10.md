# 모듈화된 헥사고날 애플리케이션에 쿼커스 추가
* 자바 클라우드 네이티브 프레임워크 쿼커스 사용

## JVM 다시 살펴보기
* 자바는 소스코드를 바이트코드로 컴파일하고, JVM은 바이트코드에 포함된 명령어를 이해할 수 있다.
* VM
  * 프로그램이 기반 운영체제와 직접 통신하지 않고 VM하고만 통신한다.
  * VM이 바이트코드 명령어를 네이티브 코드 명령으로 변환한다.
  * 재컴파일 없이 다른 운영체제와 CPU 아키텍처에서 소프트웨어를 실행할 수 있다.
* JVM을 통한 자바의 이식성
  * 클라우드 컴퓨팅 세계는 이전보다 더 많은 이식성을 갖게 만든다. (도커, 쿠버네티스 등)
  * 컨테이너 가상화를 통해, 컴파일된 소프트웨어를 런타임 환경 및 의존성과 함께 컨테이너 이미지로 패키징해서 이식성을 달성할 수 있다.
  * JVM과 재컴파일 없이도 도커 이미지로 애플리케이션을 패키징할 수 있기 때문에 더 이상 매력적이지 않다.
* JVM의 메모리 관리
  * 프로그램의 메모리의 해제와 할당을 JVM이 알아서 처리해준다.
  * 기술적인 것보다 기능적인 세부사항에 더 집중할 수 있다.
  * 가비지 컬렉터(garbage collector)
    * 프로그램이 더 이상 사용하지 않는 메모리를 해제할 수 있도록 객체가 더 이상 사용되지 않거나 참조되지 않을 때 이를 자동으로 확인하는 것
    * 객체 참조를 추적하고 더 이상 참조되지 않는 객체를 해제하도록 하는 알고리즘을 마크 앤 스윕(mark and sweep)이라고 부른다.
* JVM의 애플리케이션 수명 주기 담당
  * 자바 소스 코드 파일 컴파일 -> 자바 클래스 파일(바이트코드)로 컴파일됨 -> JVM 로드
  * 바이트코드 로딩 성능 개선을 위한 `JIT 컴파일`과 `AOT 컴파일`이 있다.

### JIT 컴파일러를 통한 런타임 성능 향상
* 프로그램이 실행되는 동안 더 나은 성능을 위해 특정 프로그램 명령어를 최적화한다.
* 프로그램에서 가장 많이 실행되는 명령어를 찾아 최적화 한다.
* 전통적인 컴파일러는 프로그램 실행 전에 모든 바이트코드를 네이티브 코드로 컴파일한다.
* JIT 컴파일러는 동적 최적화 알고리즘을 사용해 바이트코드의 일부를 선택하고 해당 부분을 컴파일하고 최적화를 적용한다. 
* JIT(Just-in-time)이라는 용어는 코드가 실행되기 직전에 최적화가 수행되기 때문에 사용된 것이다.
* 가장 큰 단점 - 애플리케이션의 시작 시간 증가
* 시작 시간 증가 문제를 극복하기 위한 AOT 컴파일 기법이 있다.

### AOT 컴파일을 통한 시작 시간 개선
* 전통적인 자바 시스템
  * 시작 시 시간과 컴퓨팅 용량이 많이 든다.
  * 이런 특성은 자바 워크로드를 클라우드로 이전하고 싶은 사람에게 걸림돌이 된다.
* AOT
  * 네이티브 코드
  * 자바 바이트 코드를 머신 코드(네이티브 코드)로 변환한다.
  * 단점
    * 네이티브 바이너리를 만들기 위해 자바 컴파일러가 바이트코드 클래스를 만드는 데 필요한 시간보다 더 많은 시간을 사용하여 CI 파이프라인에 영향을 줄 수 있다.
    * 리플렉션을 사용해 적절한 동작을 하게 만들려면 추가적인 몇 가지 작업을 수행해야 한다.
* GraalVM: 자바 및 다른 자바 기반 언어에 대한 네이티브 바이너리를 제공하는 데 사용되는 AOT 컴파일러

## 쿼커스 소개
* 쿼커스는 GraalVM을 기반으로 하는 네이티브 실행 파일을 지원한다.
* 라이브 개발 기능(코드가 변경될 때마다 애플리케이션의 재시작을 방지해 생산성을 향상시키는 기능)을 지원한다.
* 클라우드 네이티브 환경의 제약사항을 처리하고 그에 대한 혜택을 이용할 수 있게 하는 도구들이 준비되어 있다.
  * CDI(Contexts and Dependency Injection) 프레임워크
  * 하이버네트 ORM 구현을 갖는 JPA(Jakarta Persistence API) 명세
  * RESTEasy를 통해 구현된 JAX-RS(Jakarta RESTful Web Services) 명세

### JAX-RS를 통한 REST 엔드포인트 생성
```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-resteasy</artifactId>
</dependency>
```
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
* RESTEasy를 사용한 REST 서비스 생성하기
  * `@Path` 애노테이션으로 엔드포인트 주소 설정
  * `@GET`을 통해 해당 엔드포인트에서 지원하는 HTTP 메서드 설정
  * `@Produces`를 사용해 요청에 대한 반환 타입 정의

### 쿼커스 DI를 통한 의존성 주입
* 자바 EE 6에 뿌리를 둔, CDI 명세에서 유래한 쿼커스 ArC(Quarkus ArC)를 기반으로 하는 자체적인 의존성 주입 메커니즘을 가지고 있다.
```java
@Inject
BeanExample beanExample;
```
* `@Inject` 애노테이션을 사용해 의존성을 주입할 수 있다. 해당 애노테이션이 동작하려면 먼저 매니지드 빈으로 의존성을 선언해야 한다.
```java
@ApplicationScoped
public class BeanExample {
    public String simpleBean() {
        return "This is a simple bean";
    }
}
```
* `@ApplicationScoped` 애노테이션은 애플리케이션이 종료되지 않는 한 이 빈을 사용할 수 있음을 의미한다.
```java
@Path("/app")
public class RestExample {

    @Inject
    BeanExample beanExample;

    @GET
    @Path("/simple-bean")
    @Produces(MediaType.TEXT_PLAIN)
    public String simpleRest() {
        return beanExample.simpleBean();
    }
}
```

### 객체의 유효성 검증
```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-hibernate-validator</artifactId>
</dependency>
```
```java
public class SampleObject {
    @NotBlank(message = "The field cannot be empty")
    public String field;

    @Min(message = "The minimum value is 10", value = 10)
    public int value;
}
```
* `@NotBlank` 애노테이션을 사용해 해당 field 변수가 절대 비어있으면 안된다고 명시한다.
* `@Min` 애노테이션을 사용해 value 변수는 항상 10 이상의 숫자가 포함되는 것을 보장한다.
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
* SampleObject 바로 앞의 `@Valid` 애노테이션은 요청이 해당 엔드포인트에 도달할 때마다 유효성 검사가 트리거 된다.
* 유효성 검사에서 실패하면 `ConstraintViolationException`이 발생하고 HTTP 400 Bad Request 가 응답으로 반환된다.

### 데이터 소스 구성 및 하이버네이트 ORM 사용
* 쿼커스를 사용한 데이터 소스 연결 방법 두 가지
  * agroal 라이브러리와 JDBC 드라이버를 사용하는 JDBC 연결
  * vert.x 리액티브 드라이버를 사용하는 데이터 스트림 처리
* 전통적인 JDBC 메서드를 사용해서 데이터 소스 연결을 해보자.
    1. 다음과 같은 의존성이 필요하다.
        ```xml
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-agroal</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-jdbc-h2</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-hibernate-orm</artifactId>
        </dependency>
        ```
    2. application.yml 파일에서 데이터 소스 설정을 구성한다.
        ```yaml
        quarkus:
          datasource:
            db-kind: h2
            jdbc:
            url: jdbc:h2:mem:default;DB_CLOSE_DELAY=-1
          hibernate-orm:
            dialect: org.hibernate.dialect.H2Dialect
            database:
            generation: drop-and-create
        ```
* SampleEntity 클래스
    ```java
    @Entity
    @NamedQuery(name = "SampleEntity.findAll",
        query = "SELECT f FROM SampleEntity f ORDER BY f.field",
        hints = @QueryHint(name = "org.hibernate.cacheable",
            value = "true")
    )
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
* 데이터베이스 엔티티를 만들고 조회하는 PersistenceExample 빈
    ```java
    @ApplicationScoped
    public class PersistenceExample {
        
        @Inject
        EntityManager em; // EntityManager 주입

        @Transactional
        public String createEntity(SampleObject sampleObject) {
            SampleEntity sampleEntity = new SampleEntity();
            sampleEntity.setField(sampleObject.field);
            sampleEntity.setValue(sampleObject.value);
            em.persist(sampleEntity);
            return "Entity with field " + sampleObject.field + "created!";
        }

        @Transactional
        public List<SampleEntity> getAllEntities() {
            return em.createNamedQuery("SampleEntity.findAll", SampleEntity.class).getResultList();
        }
    }
    ```
* 데이터베이스 엔티티의 생성과 검색을 트리거하는 REST 엔드포인트
    ```java
    @Path("/app")
    public class RestExample {

        @Inject
        PersistenceExample persistenExample;

        /* 코드 생략 */

        @POST
        @Path("/create-entity")
        @Produces(MediaType.TEXT_PLAIN)
        @Consumes(MediaType.APPLICATION_JSON)
        public String persistData(@Valid SampleObject sampleObject) {
            return persistenceExample.createEntity(sampleObject);
        }
    }
    ```
* 데이터베이스에서 모든 엔티티를 조회하기 위한 엔드포인트
    ```java
    @Path("/app")
    public class RestExample {

        @Inject
        PersistenceExample persistenExample;

        /* 코드 생략 */

        @GET
        @Path("/get-all-entities")
        public List<SampleEntity> retrieveAllEntities() {
            return persistenceExample.getAllEntities();
        }
    }
    ```
* 헥사고날 애플리케이션에 쿼커스를 적용할 때
  * RESTEasy - REST를 지원하기 위한 입력 어댑터 구현
  * 쿼커스 DI - 프레임워크 헥사곤과 애플리케이션 헥사곤에서 객체의 수명주기를 더 잘 관리할 수 있음
  * 쿼커스 유효성 검증 메커니즘 - 헥사고날 시스템에 입력되는 데이터 검증에 기여
  * 데이터 소스 구성과 하이버네이트 ORM - 출력 어댑터 재구성

## 모듈화된 헥사고날 애플리케이션에 쿼커스 추가
* 헥사고날 아키텍처에서는 어떤 모듈이 쿼커스 엔진의 시작을 담당해야 할까?
  * 각 모듈의 책임이 모호해지는 것을 방지하기 위해 다른 헥사고날 시스템 모듈과 애그리게이션 관계를 만들고 쿼커스 엔진을 구동하는 목적만 갖는 전용 모듈을 만들 것이다.
  * 이름은 bootstrap 모듈!
* bootstrap 모듈
  * 쿼커스 초기화에 필요한 의존성을 제공하는 에그리게이터 모듈
  * 쿼커스와 함께 사용되어야 하는 hexagonal 모듈의 의존성
* bootstrap 모듈 생성하기
    1. 모듈 디렉토리 구조 생성
    2. 프로젝트 루트 pom.xml 에 쿼커스 의존성 설정 (`io.quarkus:quarkus-universe-bom`)
        ```xml
        <dependencyManagement>
            <dependencies>
                <dependency>
                    <groupId>io.quarkus</groupId>
                    <artifactId>quarkus-universe-bom</artifactId>
                    <version>${quarkus.platform.version}</version>
                    <type>pom</type>
                    <scope>import</scope>
                </dependency>
            <dependencies>
        </dependencyManagement>
        ```
        * 쿼커스의 모든 확장 기능 사용이 가능해진다.
    3. 프로젝트 루트 pom.xml 에 jandex-maven-plugin 플러그인 구성
        ```xml
        <plugin>
            <groupId>org.jboss.jandex</groupId>
            <artifactId>jandex-maven-plugin</artifactId>
            <version>${jandex.version}</version>
            <executions>
                <execution>
                    <id>make-index</id>
                    <goals>
                        <goal>jandex</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
        ```
        * 다중 모듈 애플리케이션으로 동작하기 때문에 다른 모듈에서 CDI 빈을 검색할 수 있게 해야 한다.
    4. bootstrap 모듈의 pom.xml 에 quarkus-maven-plugin 플러그인 구성
        ```xml
        <build>
            <plugins>
                <plugin>
                    <groupId>io.quarkus</groupId>
                    <artifactId>quarkus-maven-plugin</artifactId>
                    <version>${quarkus-plugin.version}</version>
                    <extensions>true</extensions>
                    <executions>
                        <execution>
                            <goals>
                                <goal>build</goal>
                                <goal>generate-code</goal>
                                <goal>generate-code-tests</goal>
                            </goals>
                        </execution>
                    </executions>
                </plugin>
            </plugins>
        </build>
        ```
        * `<goal>build</goal>`은 bootstrap 모듈에 대해 빌드 골을 설정하는 부분이다. 이 모듈이 쿼커스 엔진을 시작하는 책임을 갖게 만든다는 의미이다.
    5. bootstrap 모듈에 도메인 헥사곤과 애플리케이션 헥사곤, 프레임워크 헥사곤의 의존성 추가
        ```xml 
        <dependency>
            <groupId>dev.davivieira</groupId>
            <artifactId>domain</artifactId>
        </dependency>
        <dependency>
            <groupId>dev.davivieira</groupId>
            <artifactId>application</artifactId>
        </dependency>
        <dependency>
            <groupId>dev.davivieira</groupId>
            <artifactId>framework</artifactId>
        </dependency>
        ```
    6. 쿼커스와 헥사곤 모듈에 대해 requires 지시자를 갖는 module-info.java 자바 모듈 디스크립터 생성
        ```java
        module dev.davivieira.boostrap {
            requires quarkus.core;
            requires domain;
            requires application;
            requires framework;
        }
        ```
* 우버.jar
  * 3개의 헥사고날 모듈을 하나의 배포 단위로 만들기 위해서 우버.jar 파일을 생성하도록 구성할 수 있다.
  * 애플리케이션을 하나의 JAR 로 실행하기 위해 필요한 모든 의존성을 그룹화한다.
* 우버.jar 생성하기
    1. 프로젝트 루트의 pom.xml 설정
        ```xml
        <quarkus.package.type>uber-jar</quarkus.package.type>
        ```
    2. 애플리케이션 컴파일
        ```sh
        mvn clean package
        ```
    3. 애플리케이션 시작
        ```sh
        java -jar boostrap/target/boostrap-1.0-SNAPSHOT-runner.jar
        ```