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



### SAGA

SAGA는 2PC와는 다르게 트랜잭션 관리 주체가 DBMS가 아닌 애플리케이션에 있다. MSA와 같이 애플리케이션이 분산되어 있을 때, 각 애플리케이션 하위에 존재하는 DB는 로컬 트랜잭션 처리만 담당한다. 따라서 각각의 애플리케이션에 대해서 연속적인 트랜잭션 요청이 실패할 경우, 롤백 처리를 애플리케이션 단에서 구현해야 한다. 

![img](https://blog.kakaocdn.net/dn/bL3MKk/btqBuiX8Wnl/9S2jkqnfbKN5Gkkt2SV5dK/img.png)



![img](https://blog.kakaocdn.net/dn/FyIyP/btqBr8pnDgR/JFUUCdDaxYi8lwWWRC3El0/img.png)



SAGA 패턴은 연속적인 업데이트 연산으로 이루어져있으며, 전체가 동시에 데이터가 영속화 되는 것이 아니라 순차적인 단계로 트랜잭션이 이루어진다. 따라서 애플리케이션 비즈니스 로직에서 요구되는 마지막 트랜잭션이 끝났을 때(성공했을 때) 데이터가 완전히 영속되었음을 인지하고 종료한다.

2PC와는 다르게 saga는 데이터 원자성을 보장해주지는 않는다. 대신, application 트랜잭션 관리를 통해 최종 일관성 (Eventually Consistency)을 달성할 수 있기 때문에 분산되어있는 DB간에 정합성을 맞출 수 있다. 또한 트랜잭션 관리를 application에서 하기 때문에 DBMS를 다양한 제품군으로 구성할 수 있는 장점도 있다.



## SpringBoot로 구현하기

먼저, SAGA 패턴을 사용하여 분산 트랜잭션을 구현해보자. SpringBoot로 서버를 구현하고, [^1]Axon Framework를 사용하여 구현할 것이다. 





## 아키텍처

![spring-microservice-transactions-arch1](https://piotrminkowski.files.wordpress.com/2020/06/spring-microservice-transactions-arch1.png?resize=617%2C350)

API를 제공하는 3개의 마이크로서비스인 Order-service, account-service, product-service와, 분산 트랜잭션을 관리하는 transaction-server, 그리고 MSA를 관리하는 discovery-server가 있다.



### 시나리오

![spring-microservices-transactions-arch2 (1)](https://piotrminkowski.files.wordpress.com/2020/06/spring-microservices-transactions-arch2-1.png?resize=700%2C309)

분산 트랜잭션을 사용해야 하는 시나리오를 하나 작성해보자. 예를 들어 다음의 상황에서는 분산 트랜잭션을 사용해야 한다.

1. Order-service는 주문 엔티티를 만들고 DB에 저장한다. 그 후, 새로운 분산 트랜잭션을 시작한다.
2. 트랜잭션이 실행된 후, order-service는 product-service에게 저장된 상품의 개수를 업데이트하고, 가격을 알려달라고 요청한다.
3. 동시에, Product-service는 transaction-server에게 트랜잭션에 참여하고 있다는(?) 정보를 준다.
4. 그리고 Order-service는 고객 계정에서 필요한 자금을 인출하여 판매자에게 이체하려고 한다.
5. 마지막으로, 트랜잭션 범위 내의 order-service의 메서드에서 예외가 발생하여 트랜잭션을 롤백한다.
6. 롤백은 전체 분산 트랜잭션에 대해 진행되어야 한다. 



### 트랜잭션 서버 구축

트랜잭션 서버는 MSA 시스템의 모든 마이크로서비스로부터 분산 트랜잭션을 관리할 수 있어야 한다. 새 트랜잭션을 추가하고 상태를 업데이트 하기 위해 다른 모든 마이크로서비스에 사용할 수 있는 REST API를 노출시켜야 한다. 또한, 트랜잭션을 생성한 마이크로서비스로부터 커밋 또는 롤백을 받은 후 비동기 이벤트도 날릴 수 있어야 한다. 이건 RabbitMQ와 같은 message broker를 사용하여 구현할 수 있다. (회사에서는 kafka, 그래서 난 카프카로 구현하는 쪽으로 코드변경 해야 함.) 그리고 모든 마이크로서비스는 들어오는 이벤트를 리스닝할 수 있어야 한다. 그 이벤트가 들어온 후 마이크로 서비스는 로컬 트랜잭션을 커밋/롤백 한다.  





### 각주

[^1]: DDD기반의 이벤트 소싱, CQRS를 구현할 때 사용하는 프레임워크