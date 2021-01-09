# Distributed Transactions in Microservices with Spring Boot

SpringBoot를 사용하는 MSA 환경에서 분산 트랜잭션을 적용하는 방법을 알아보자.

**참고 문헌**

[SAGA Pattern Blog](https://medium.com/swlh/microservices-architecture-what-is-saga-pattern-and-how-important-is-it-55f56cfedd6b)

https://medium.com/trendyol-tech/saga-pattern-briefly-5b6cf22dfabc

## 접근법

분산 트랜잭션을 적용하는 방법으로는 크게 두 가지가 있다.

* Two Phase Commit Protocol
* Eventual Consistency and Compensation (SAGA Pattern) :heavy_check_mark:



### Two Phase Commit Protocol

트랜잭션 처리와 데이터베이스 컴퓨터 네트워킹에서 정보가 성공적으로 수정되었음을 확인하기 위해 사용하는 ACP(Atomic Commit Protocol)이다. 트랜잭션 성공과 실패를 확인하고 이러한 작업들이 원자적으로 이루어질 수 있도록 조정하는 분산 알고리즘을 제공한다.



#### 2PC 동작 과정

2PC가 동작하기 위해서는 트랜잭션 관리자인 **Coordinator** 가 필요하다. Coordinator 외의 나머지 노드들은 cohorts(또는 participants)로 불린다. 

![img](https://docs.google.com/drawings/d/1hLs56P5nwgZVnRfvHE9wG8nNDyB_6kG_dCTk9Mo0Z0M/pub?w=543&h=520)

2PC는 **작업 요청 단계** 와 **커밋 단계** 인 2단계로 구분된다.

##### 작업 요청 단계

1. Coordinator는 모든 Cohorts에게 query to commit 메시지를 전송하고 응답이 끝나기를 기다린다.
2. Cohorts는 트랜잭션 지점을 설정하고 작업을 진행한 뒤 commit 준비를 한다. 실패하는 Cohorts가 발생할 경우, 실행 취소를 위해 redo log와 undo log를 준비한다.
3. 각 cohorts들은 agreement 메시지를 전송한다. Ex) 작업 성공: Yes, 작업 실패: No



##### 커밋 단계 - 성공

모든 Cohorts들로부터 작업 성공 agreement 메시지를 받았다면 성공이고, 커밋을 실행한다.

1.  Coordinator는 모든 Cohorts들에게 commit 메시지를 전송한다.
2. 각 Cohorts들은 커밋 후 리소스 잠금을 해제한 후 Coordinator에게 acknowledgement를 전송한다.
3. 모든 Cohorts로부터 ack를 받으면 작업 완료

##### 커밋 단계 - 실패

하나 이상의 Cohorts들로부터 작업 실패 agreement 메시지를 받았거나 Timeout이 발생하면 실패이고, 롤백을 수행한다.

1.  Coordinator는 모든 Cohorts들에게 rollback 메시지를 전송한다.
2. 각 Cohorts들은 undo log를 이용해서 롤백하고 리소스 잠금을 해제한 후 ack를 전송한다.
3. 모든 Cohorts로부터 ack를 받으면 트랜잭션 복구



#### 2PC의 단점 및 문제점

Blocking Protocol이기 때문에 Coordinator가 영구적으로 실패하면 트랜잭션을 영원히 해결하지 못하는 Cohorts들이 생길 수 있다.

2PC는 DBMS간 분산 트랜잭션을 지원해야 적용 가능한데, NoSQL은 지원하지 않고, 함께 사용되는 DBMS가 동일해야 한다. 

그리고 2PC는 보통 하나의 엔드포인트를 통해 서비스 요청이 들어오고 내부적으로 DB가 분산되어 있을 때 사용된다. 반면, MSA 환경에서는 애플리케이션이 분산되어 있어 각기 다른 App에서 API간 통신을 통해 서비스 요청이 이루어지기 때문에 구현이 쉽지않다.



## SAGA

SAGA는 2PC와는 다르게 트랜잭션 관리 주체가 DBMS가 아닌 애플리케이션에 있다. MSA와 같이 애플리케이션이 분산되어 있을 때, 각 애플리케이션 하위에 존재하는 DB는 로컬 트랜잭션 처리만 담당한다. 따라서 각각의 애플리케이션에 대해서 연속적인 트랜잭션 요청이 실패할 경우, 롤백 처리를 애플리케이션 단에서 구현해야 한다. 

SAGA 패턴은 Choreography-based SAGA 와 Orchestration-based SAGA로 두 종류가 있다.



### Choreography-based SAGA

![Saga_Choreography_Flow.001](/Users/yunseowon/Desktop/Saga_Choreography_Flow.001.jpeg)

Choreography-based SAGA는 각 서비스마다 자신의 로컬 트랜잭션을 관리하며 현재 상태를 바꾼 후 완료가 되었으면 완료 이벤트를 발생시켜 이벤트를 다음 트랜잭션을 관리하는 서비스에 전달하여 트랜잭션을 처리하는 방식으로 구현된다. 만약 트랜잭션이 롤백되어야 할 경우, 보상 이벤트를 발생시킴으로써 보상 트랜잭션이 실행될 수 있도록 하여 트랜잭션을 관리한다.

해당 그림에서는 다음과 같은 순서로 트랜잭션이 보장된다.

**Commit**

1. OrderService가 Order를 생성시키고 pending 상태로 놔둔 후 OrderCreated 이벤트를 생성한다.
2. Customer Service가 Order Created 이벤트를 받은 후 Credit을 생성한 후 Credit Reserved 이벤트를 발생시킨다.
3. OrderService는 CreditReserved 이벤트를 받은 후 pending 상태의 Order를 approved로 변경하여 트랜잭션이 Commit 될 수 있도록 한다.

**Rollback**

1. OrderService가 Order를 생성시키고 pending 상태로 놔둔 후 OrderCreated 이벤트를 생성한다.
2. Customer Service가 Order Created 이벤트를 받았지만 Credit 제한이 걸려 Credit을 생성할 수 없다면 CreditLimitExceeded 이벤트를 발생시킨다.
3. OrderService는 CreditLimitExceeded 이벤트를 받은 후 pending 상태의 Order를 reject로 변경하여 트랜잭션이 Rollback 될 수 있도록 한다.



#### 장점

* 별도의 Orchestrator가 없어서 Orchestration-based 보다 성능상 이점이 있다. (인스턴스를 만들지 않아도 되거나 별도의 Orchestrator 서비스가 없어도 됨.)
* 구현하기 쉽다.
* 개념에 대해 이해하기 쉽다.



#### 단점

* 트랜잭션 시나리오가 하나 추가된다면 관리하기가 힘들어 질 수 있다.
* 어떤 서비스가 어떤 이벤트를 수신하는지 추측하기 힘들다.
* 모든 서비스는 호출되는 각 서비스의 이벤트를 들어야 한다.



### Command / Orchestration based SAGA

Command / Orchestration based SAGA에서는 하나의 책임을 가지는 여러 개의 서비스와 그 서비스들 간의 트랜잭션 처리를 담당하는 Orchestrator가 존재한다. Choreography-based SAGA 처럼 각 서비스가 서로 다른 서비스의 이벤트를 청취해야 하는 것 과는 다르게 Orchestrator가 모든 서비스의 이벤트를 청취하고 엔드포인트를 트리거할 책임을 가지고 있다. 

![Image for post](https://miro.medium.com/max/683/1*OxfdbfsX2M7qrv5WsSXAMg.png)

위의 다이어그램에서, Order Orchestrator는 command/reply 방식으로 각 서비스와 통신한다. 



Orchestration based SAGA에서는 Orchestrator가 한 트랜잭션의 흐름을 모두 알고있다는 것을 알 수 있다. 만약 트랜잭션에서 에러가 난다면, 그 에러로 인해 에러 발생 이전에 대한 모든 것들을 롤백하는 책임 또한 Orchestrator가 가지고 있다. 

Orchestrator가 각 변환이 Command나 message에 해당하는 상태 시스템으로 볼 수 있으므로 Orchestration based SAGA를 구현하는 방식 중 하나는 `State Machine Pattern`을 적용하는 것이다. State Machine Pattern은 구현하기 쉽기 때문에 잘 정의된 동작을 구조화하는 데 좋은 패턴이다.



#### 장점

* 트랜잭션 시라니오에 변화가 생겨도 Orchestrator만 변경하면 되기 때문에 유지보수에 용이하다.
* Orchestrator가 모든 서비스와 통신하기 때문에 서비스 간의 순환 참조를 피할 수 있다.



#### 단점

* 아무래도 구현하기가 힘들다.
* Orchestrator에게 트랜잭션 관련된 로직들이 엄청 많이 쌓이는데 비즈니스 로직이 추가된다면 유지보수에 엄청 힘들어 질 것이다. 그래서 Orchestration-based SAGA를 구현한다면 Orchestrator에는 트랜잭션 순서?에 관한 로직 (only Command / Reply) 만 작성할 수 있도록 관리해야 한다.



## Note

> 보통, SAGA 패턴은 성능과 신뢰성, 확장성을 높이기 위해 Kafka나 RabbitMQ와 같은 message broker를 사용한다.





## Axon Framework 에서의 SAGA

Axon Framework는 DDD, Event Sourcing, CQRS 패턴을 중점으로 하는 Framework인데 여기서도 SAGA 패턴이 사용된다. MSA에서 떠오르는 프레임워크로 알고있어서 Axon에서는 어떻게 SAGA 패턴을 구현하였는지 알아보고 싶어서(영감을 받지 않을까? 해서) 정리한다.



Axon에서의 Saga는 비즈니스 트랜잭션을 담당하는 이벤트 리스너의 한 종류로 취급한다. Axon에서는 각 SAGA 인스턴스는 하나의 비즈니스 트랜잭션을 관리하는 책임을 가지고 있다. 이는 SAGA가 트랜잭션을 관리하는 데 필요한 상태를 유지하고 트랜잭션 플로우를 계속 진행하거나 어떠한 에러로 인해 이전에 작업했던 것들을 롤백 시키기 위한 보상 행동을 취한다는 것을 의미한다. 그리고 일반적인 이벤트 리스너와는 다르게, SAGA는 이벤트에 의해 트리거되는 시작 지점과 끝나는 지점이 있다. SAGA의 시적지점이 보통 명백한 것에 비해 SAGA의 끝나는 지점은 여러 개가 될 수 있다.



Axon에서, SAGA는 하나 이상의 @SagaEventHandler 메서드를 정의하는 클래스이다. 일반적인 이벤트 핸들러와는 다르게 SAGA의 여러 인스턴스가 언제든지 존재할 수 있다. SAGA는 특정 saga 타입에 대한 이벤트를 처리하는 하나의 이벤트 프로세서에 의해 관리된다.



### Life Cycle

하나의 Saga 인스턴스는 오직 하나의 트랜잭션을 관리하는 책임을 가지고 있다. 즉, Saga의 시작지점과 끝 지점의 라이프사이클에 대해 알고 있어야 한다.

Saga에서, 이벤트 핸들러는 @SagaEventHandler 애노테이션으로 정의한다. 어떤 특정한 이벤트가 트랜잭션의 시작을 나타내는 경우, @StartSaga 애노테이션을 추가한다. 이 애노테이션은 새로운 saga를 만들고 관련된 이벤트가 발생했을 때 이벤트 핸들러 메서드를 호출한다. 

기본적으로, 새로운 saga는 적합한 기존 saga를 찾을 수 없는 경우에만 시작된다. 그리고 @StartSaga 애노테이션의 forceNew 프로퍼티를 true로 설정함으로써 새로운 Saga 인스턴스를 강제로 생성할 수도 있다. 

Saga는 두 가지 방식으로 끝날 수 있다. 어떤 이벤트가 항상 Saga life cycle의 끝을 나타내는 경우, Saga의 이벤트 핸들러에게 @EndSaga 애노테이션을 추가한다. Saga의 라이프사이클은 해당 핸들러가 호출되면 종료된다. 아니면, SagaLifecycle.end() 메서드를 호출함으로써 Saga를 종료시킬 수 있다. 이 메서드는 특정한 조건에서 Saga를 종료시킬 수 있다.





### Event Handling

Saga에서의 이벤트 핸들링은 일반적인 이벤트 리스너와는 비교가 될 수 있다. 일반적인 리스너와의 주요 차이점은 모든 수신된 이벤트를 처리하는 이벤트 리스너는 단일 인스턴스인 반면에, Saga는 인스턴스가 여러개이며 각 인스턴스는 서로 다른 이벤트를 리스닝한다. 예를 들면, 주문과 관련된 Saga 인스턴스가 두 개 있는데, 하나는 OrderId=1인 이벤트를 리스닝하고 나머지 하나는 OrderId=2인 이벤트를 리스닝한다. (서로 다른 Order에 대한 이벤트를 리스닝 함)

모든 이벤트를 모든 Saga 인스턴스에게 날리는 것 대신에 Axon은 이벤트와 관련된 Saga 인스턴스에게만 날린다. 이는 AssociationValue를 사용하면 가능하다. AssociationValue는 key-value 구조로 되어있다. key는 Order의 orderId 처럼 유일한 id 타입을 나타내고 value는 id의 값을 나타낸다.

@SagaEventHandler 어노테이션이 등록된 메서드가 평가되는 순서는 @EventHandler 어노테이션이 등록된 메서드와 동일하다. 핸들러 메서드의 파라미터가 수신된 이벤트와 매치되고 saga가 핸들러 메서드에 정의된 프로퍼티와 관련이 있다면 메서드가 매치된다.

@SagaEventHandler는 두 개의 어트리뷰트를 가지고 있다. 어트리뷰트 중 하나인 associationProperty는 관련된 saga를 찾는 데 사용해야 하는 수신 이벤트의 프로퍼티 이름이다. associatioValue의 key가 그 프로퍼티의 이름이다. 

또는, assoricationProperty의 키 값이 자신이 원하는 이름이 아닐 경우, (또는 다른 이름으로 사용하고 싶은 경우) keyName 어트리뷰트를 추가하면 되는데 이는 associationProperty의 이름을 keyName으로 사용할 수 있도록 해준다.





### Injecting Resources

Saga는 보통 이벤트를 기반으로 상태를 유지하는 것 이상의 일을 한다. Saga는 다른 외부 컴포넌트와도 상호작용한다. 그래서 Saga는 컴포넌트와 상호작용하는 데 필요한 리소스를 접근할 수 있어야 한다. 보통, 이러한 리소스들은 Saga와 그의 상태의 일부가 아니고  그 리소스는 그렇게 지속되면 안된다.(??) 그러나, Saga가 재구성된다면 이벤트가 해당 인스턴스로 라우팅되기 전에 리소스를 주입해야 한다.

이러한 목적으로, ResourceInjector가 존재한다. ResourceInjector는 리소스를 Saga에 주입시키기 위해 SagaRepository에 의해 사용된다. Axon은 SpringResourceInjector를 제공한다. SpringResourceInjector는 애플리케이션 컨텍스트의 리소스와 함께 애노테이션이 등록된 필드와 메서드를 주입한다. Axon은 또한 @Inject 애노테이션이 달린 메서드와 필드에 등록된 리소스를 주입하는 SimpleResourceInjector를 제공한다.

SimpleResourceInjector는 미리 정의된 리소스들의 컬렉션이 주입되도록 한다. Saga의 @Inject 애너테이션이 달려있는 메서드와 필드를 스캔하여 주입한다.

Configuration API를 사용할 때, Axon은 기본적으로 ConfigurationResourceInjector를 사용한다. 이는 Configuration에서 이용가능한 모든 리소스를 주입한다. EventBus, EventStore, CommandBus, CommandGateway와 같은 컴포넌트들이 기본적으로 이용가능하다. 그리고 configurer.registerComponent()를 사용함으로써 사용자가 정의한 컴포넌트들도 등록할 수 있다.

SpringResourceInjector는 리소스를 Saga에 주입하기 위해 Spring에 의존하는 주입 메커니즘을 사용한다. 이는 setter에 의한 주입과 직접적인 필드 주입 모두 사용할 수 있다는 의미이다. (@Autowired와 같은..)





### Saga Infrastructure

이벤트는 적절한 Saga 인스턴스로 리다이렉트 되어야 한다. 그러기 위해서는, 일부 인프라스트럭쳐 클래스들이 필요하다. Saga에서는 특히 SagaManager와 SagaRepository가 중요하다.





#### Saga Manager

이벤트를 핸들링하는 다른 컴포넌트와 마찬가지로 이벤트 프로세서에 의해 처리된다. 하지만 이벤트를 핸들링 하는 Saga는 싱글턴으로 되어있지 않는다. Saga는 각각의 관리해야 하는 라이프사이클을 가지고 있다. 

Axon은 AnnotatedSagaManager를 통해 라이프사이클 관리를 지원한다. AnnotatedSagaManager는 실제 핸들러를 호출하는 이벤트 프로세서가 제공된다. Saga Manager는 관리하고자 하는 saga의 타입과 그 saga의 타입이 저장되고 검색되는 SagaRepository를 이용하여 초기화된다. 하나의 AnnotatedSagaManager는 오직 하나의 Saga 타입을 관리한다.

Configuration API를 사용한다면, Axon은 대부분의 컴포넌트에 적당한 기본값을 사용한다. 그러나 개발에 사용할 SagaStore를 직접 정의하는 것이 좋다. SagaStore는 물리적으로 Saga 인스턴스를 저장하는 매커니즘이다. AnnotatedSagaRepository는 Saga 인스턴스를 저장하고 찾기 위해서 사용된다.



#### Saga Repository와 Saga Store

SagaRepository는 saga를 저장하고 찾는 책임을 가지고 있으며, SagaRepository는 SagaManager에 의해 사용된다. SagaRepository는 특정한 Saga 인스턴스를 id와 다른 연관된 값을 사용하여 검색할 수 있다.

SagaRepository에는 몇 가지 특별한 요구사항이 있다. 먼저, Saga에서 동시성 처리는 매우 중요한 절차이기 때문에 레포지토리는 반드시 각 개념적인 saga 인스턴스가 JVM에 오직 하나의 인스턴스만 존재하도록 보장해야 한다.

Axon은 AnnotatedSagaRepository 구현체를 제공하는데, 이는 saga 인스턴스를 조회하는 동시에 하나의 saga 인스턴스만 접근할 수 있도록 한다. 그리고 이는 SagaStore를 이용하여 saga 인스턴스의 실제 영속성을 수행한다.

레포지토리에서 사용하기 위해 의존할 애플리케이션에 의해 사용되는 스토리지 엔진을 선택해야 한다. Axon은 JdbcSagaStore, InMemorySagaStore, JpaSagaStore, MongoSagaStore를 지원한다. 





























