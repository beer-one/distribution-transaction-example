# Distributed Transactions in Microservices with Spring Boot

SpringBoot를 사용하는 MSA 환경에서 분산 트랜잭션을 적용하는 방법을 알아보자.

**참고 문헌**

[블로그](https://piotrminkowski.com/2020/06/19/distributed-transactions-in-microservices-with-spring-boot/)

[Axon framework](https://docs.axoniq.io/reference-guide/implementing-domain-logic/complex-business-transactions)

[예제코드](https://github.com/piomin/sample-spring-microservices-transactions.git)



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

























