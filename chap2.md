## 도메인 헥사곤으로 비즈니스 규칙 감싸기

 - 가장 내부에 있는 헥사곤으로, 그 어떤 규칙에도 의존하지 않는다.
 - 도메인 엔티티는 문제 영역을 엔테테로 만드는 것
 - 이부분은 기술과 관련없이 순수한 언어로 이뤄져야 한다.

관련 엔티티
 - 빈약한 엔티티를 -> 데이터가 많지만 동작은 없는것
   - 데이터 모델을 그대로 들고왔을 때, 이런 엔티티가 종종 생기기도 한다.
 - 도메인 vs 엔티티 ?
   - 엔티티를 감싸고 기능(비즈니스) 을 넣은게 도메인?
 - 엔티티가 동작이 없을 경우, 찾아봐야한다.
```java
public class Router {
    
    private final RouterType routerType;
    private final RouterId routerId;
    private Switch networkSwitch;
    
    public static Predicate<Router> filter(RouterType rt) {
        return rt == CORE ? Router.isCore() : Router.isEdge()
    }
    public static Predicate<Router> isCore() {
        return p -> p.getRouterType() == RouterType.CORE;
    }

    public static Predicate<Router> isEdge() {
        return p -> p.getRouterType() == RouterType.EDGE;
    }
    
    public Network createNetwork(IP address, ...) {
        return new Network(address, ...);
    }
    
    public addNetworkToSwitch(Network network) {
        this.networkSwitch.addNetwork(network);
    }
}

public class Switch {
    private SwitchType type;
    private SwitchId id; // VO
    private List<Netwrok> networks;
    private Ip Address; // VO
    
    public Switch addNetwork(Network network) {
        
    }
}

public class RouterSearch {
    public static List<Router> retrieveRouter(List<Router> routers, Predicate<Router> predicate) {
        return routers.stream().filter(predicate).collec(Collectors.<Router>toList());
    }
}

@Service
public class NetworkOper {
   public void createNewNetwork(Router router, Ip address, ...) {
       valid(...);
       valid(address);
       checkDup(address);
       router.createNetwork(address);
   }  
   private void checkDup(Router router, IP address) { // 근데 이건 라우터쪽 로직이 맞지 않나?
       List<Network> networks = router.getNetwork();
       networks.checkDup(address);
   }
}
```
[이 예제는 Router Entity에서는, 제약사항만 걸고 실제로 움직이는 도메인 로직은 도메인 레이어로 뺀 상황이다.]

## UUID를 이용한 식별자
 - 도메인과 외부기술 로직 (DB의 시퀀스, Auto Incre등) 과 결합하지 않기 위한 작업
 - 트레이드오프를 잘 고려해야함
 - 엔티티는 헥사고날의 일급 객체로써 사용하므로, Id 를 또다른 일급 객체로 사용하는게 좋다
   - 위으 Router에서 RouterId 등

## 값 객체!
 - 값 객체를 잘 사용하는것은 중요하다.
 - JPA의 임베디드, 또는 파싱된 값을 가지는 벨류 등 여러 값을 묶어 하나의 타입으로 정의하는건, 가독성을 올려준다
 - 값 객체의 일부 특성은 절대 변경돼서는 안된다 (?)
```java
public class Event {
    private EventId id;
    private OffsetDateTime timestamp;
    private String protocol;
    private String activity;
}

public class Event {
    private EventId id;
    private OffsetDateTime timestamp;
    private String protocol;
    private Activity activity;
    
    public static class Activity {
        private String desc;
        private final String srcHost;
        private final String destHost;
    }
}

   public static void main(String[] args) {
        String srcHost = event.getActivity().split(">")[0];
        String srcHost2 = event.getActivity().getSrcHost();
   }
```
   
## 애그리게잇을 통한 일관성 보장
 - 관련 엔티티와 값 객체의 그룹이 함께 전체적인 개념을 설명하는 경우에는, aggregate 을 사용
 - agg는 객체의 데이터와 동작을 조정하는 오케스트레이터와 같음
 - 아 DDD aggregate root
   - 여러 엔티티 (주로 하나의 엔티티던데) 를 관리하는 역할을 하게됨
   - 엔티티를 분리하거나 완벽하게 동일하게 돌아가는 엔티티에서 가장 최상단에서 바깥의 요청을 받아주고 돌아가는 엔티티가 생기게 됨
   - Router 하위에 있는 것들

## 도메인 서비스 활용
 - 도메인, 헥사곤, 엔티티, 에그리게잇 등 없는 경우도 있음
 - Domain Service 에 넣게 됨
   - 이거완전 브릿지들 역할하는 그런거 같은데
 - 위의 NetworkOper 에서 checkDup 에 해당하는 것과 같은 기능을 넣는다.
 - 엔티티에 어올리지 않는 작업을 처리하는 부분인데 엔티티와 VO가  너무 많은 기능을 가지고 커지는것을 방지하는 활동
   - 이걸 방지하는게 맞는걸까 (?)

## 정책 패턴 + 명세 패턴
 - strategy 패턴 
   - 제공된 데이터에 대한 어떤 처릴 캡슐화 해서 많이 사용
 - 명세
   - 조건등등을 캡슐화 해서 사용
 - 아래객체 나중에 정리!

```java
interface Spec<T> {
   boolean isSatisfiedBy(T t);

   default Sepc<T> and(Spec<T> spec) {
      return new AndSpec<T>(this, spec);
   }
}

class AndSpec<T> implements Spec<T> {

   Spec<T> spec1;
   Spec<T> spec2;

   public AndSpec(Spec<T> spec1, Spec<T> spec2) {
      this.spec1 = spec1;
      this.spec2 = spec2;
   }

   public boolean isSatisfiedBy(T t) {
      return spec1.isSatisfiedBy(t) && spec2.isSatisfiedBy(t)
   }

   public Sepc<T> and(Spec<T> spec) {
      return Spec.super.and(spec);
   }
}

public class CidrSpec implements Spec<Integer> {

   public boolean isSatisfiedBy(Integer integer) {
      return integer in 0. .24;
   }
}

public class NetworkSpec implements Spec<Router> {
   
    private Ip adress;
    
    public NetworkSpec(Ip adress, ...) {
        this.adress = adress;
    }
    
    public boolean isSatisfiedBy(Router router) {
      return router.getNetwork().stream().filter.has(adress);
   }
}
```

## POJO를 통한 비즈니스 규칙의 장점
 - 비즈니스 규칙에 나도모르게 들어오는 기술적 요구사항을 막을 수 있음
 - 여러 시스템에서 재활용 하기 쉬움
 - 특정 기술 의존성이 없어, 이해가 쉬움 (다른걸 이해할 필요 x)