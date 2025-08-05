# Distributed Transactions in Microservices with Spring Boot

SpringBoot를 사용하는 MSA 환경에서 분산 트랜잭션을 적용하는 방법을 알아보자.

**참고 문헌**

[SAGA Pattern Blog](https://medium.com/swlh/microservices-architecture-what-is-saga-pattern-and-how-important-is-it-55f56cfedd6b)

https://medium.com/trendyol-tech/saga-pattern-briefly-5b6cf22dfabc

<br>

## 목차

### [1.접근법](https://github.com/beer-one/distribution-transaction-example?tab=readme-ov-file#%EC%A0%91%EA%B7%BC%EB%B2%95)
 * [Two Phase Commit Protocol](https://github.com/beer-one/distribution-transaction-example?tab=readme-ov-file#two-phase-commit-protocol)

### [2. SAGA Pattern](https://github.com/beer-one/distribution-transaction-example?tab=readme-ov-file#saga)
  * [Choreography based](https://github.com/beer-one/distribution-transaction-example?tab=readme-ov-file#choreography-based-saga)
  * [Orchestration based](https://github.com/beer-one/distribution-transaction-example?tab=readme-ov-file#orchestration-based-saga)

### [3. 테스트 프로젝트](https://github.com/beer-one/distribution-transaction-example?tab=readme-ov-file#%ED%85%8C%EC%8A%A4%ED%8A%B8-%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8)
  * [프로젝트 구조](https://github.com/beer-one/distribution-transaction-example?tab=readme-ov-file#%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8-%EA%B5%AC%EC%A1%B0)
  * [Orchestration based SAGA 구현](https://github.com/beer-one/distribution-transaction-example?tab=readme-ov-file#orchestration-based-saga-%EA%B5%AC%ED%98%84)
  * [SAGA 트랜잭션 흘러가는 과정](https://github.com/beer-one/distribution-transaction-example?tab=readme-ov-file#saga-%ED%8A%B8%EB%9E%9C%EC%9E%AD%EC%85%98-%ED%9D%98%EB%9F%AC%EA%B0%80%EB%8A%94-%EA%B3%BC%EC%A0%95)

### [4. 테스트 프로젝트 실습](https://github.com/beer-one/distribution-transaction-example?tab=readme-ov-file#%ED%85%8C%EC%8A%A4%ED%8A%B8-%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8-%EC%8B%A4%EC%8A%B5)
  * [Prerequisites](https://github.com/beer-one/distribution-transaction-example?tab=readme-ov-file#prerequisites)


## 접근법

분산 트랜잭션을 적용하는 방법으로는 크게 두 가지가 있다.

* Two Phase Commit Protocol
* Eventual Consistency and Compensation (SAGA Pattern) :heavy_check_mark:


<br>

### Two Phase Commit Protocol

트랜잭션 처리와 데이터베이스 컴퓨터 네트워킹에서 정보가 성공적으로 수정되었음을 확인하기 위해 사용하는 ACP(Atomic Commit Protocol)이다. 트랜잭션 성공과 실패를 확인하고 이러한 작업들이 원자적으로 이루어질 수 있도록 조정하는 분산 알고리즘을 제공한다.


<br>

#### 2PC 동작 과정

2PC가 동작하기 위해서는 트랜잭션 관리자인 **Coordinator** 가 필요하다. Coordinator 외의 나머지 노드들은 cohorts(또는 participants)로 불린다. 

![img](https://docs.google.com/drawings/d/1hLs56P5nwgZVnRfvHE9wG8nNDyB_6kG_dCTk9Mo0Z0M/pub?w=543&h=520)

2PC는 **작업 요청 단계** 와 **커밋 단계** 인 2단계로 구분된다.

<br>

##### 작업 요청 단계

1. Coordinator는 모든 Cohorts에게 query to commit 메시지를 전송하고 응답이 끝나기를 기다린다.
2. Cohorts는 트랜잭션 지점을 설정하고 작업을 진행한 뒤 commit 준비를 한다. 실패하는 Cohorts가 발생할 경우, 실행 취소를 위해 redo log와 undo log를 준비한다.
3. 각 cohorts들은 agreement 메시지를 전송한다. Ex) 작업 성공: Yes, 작업 실패: No



<br>

##### 커밋 단계 - 성공

모든 Cohorts들로부터 작업 성공 agreement 메시지를 받았다면 성공이고, 커밋을 실행한다.

1.  Coordinator는 모든 Cohorts들에게 commit 메시지를 전송한다.
2. 각 Cohorts들은 커밋 후 리소스 잠금을 해제한 후 Coordinator에게 acknowledgement를 전송한다.
3. 모든 Cohorts로부터 ack를 받으면 작업 완료

<br>

##### 커밋 단계 - 실패

하나 이상의 Cohorts들로부터 작업 실패 agreement 메시지를 받았거나 Timeout이 발생하면 실패이고, 롤백을 수행한다.

1.  Coordinator는 모든 Cohorts들에게 rollback 메시지를 전송한다.
2. 각 Cohorts들은 undo log를 이용해서 롤백하고 리소스 잠금을 해제한 후 ack를 전송한다.
3. 모든 Cohorts로부터 ack를 받으면 트랜잭션 복구



<br>

#### 2PC의 단점 및 문제점

Blocking Protocol이기 때문에 Coordinator가 영구적으로 실패하면 트랜잭션을 영원히 해결하지 못하는 Cohorts들이 생길 수 있다.

2PC는 DBMS간 분산 트랜잭션을 지원해야 적용 가능한데, NoSQL은 지원하지 않고, 함께 사용되는 DBMS가 동일해야 한다. 

그리고 2PC는 보통 하나의 엔드포인트를 통해 서비스 요청이 들어오고 내부적으로 DB가 분산되어 있을 때 사용된다. 반면, MSA 환경에서는 애플리케이션이 분산되어 있어 각기 다른 App에서 API간 통신을 통해 서비스 요청이 이루어지기 때문에 구현이 쉽지않다.



<br>

## SAGA

SAGA는 2PC와는 다르게 트랜잭션 관리 주체가 DBMS가 아닌 애플리케이션에 있다. MSA와 같이 애플리케이션이 분산되어 있을 때, 각 애플리케이션 하위에 존재하는 DB는 로컬 트랜잭션 처리만 담당한다. 따라서 각각의 애플리케이션에 대해서 연속적인 트랜잭션 요청이 실패할 경우, 롤백 처리를 애플리케이션 단에서 구현해야 한다. 

SAGA 패턴은 Choreography-based SAGA 와 Orchestration-based SAGA로 두 종류가 있다.



<br>

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



<br>

#### 장점

* 별도의 Orchestrator가 없어서 Orchestration-based 보다 성능상 이점이 있다. (인스턴스를 만들지 않아도 되거나 별도의 Orchestrator 서비스가 없어도 됨.)
* 구현하기 쉽다.
* 개념에 대해 이해하기 쉽다.



<br>

#### 단점

* 트랜잭션 시나리오가 하나 추가된다면 관리하기가 힘들어 질 수 있다.
* 어떤 서비스가 어떤 이벤트를 수신하는지 추측하기 힘들다.
* 모든 서비스는 호출되는 각 서비스의 이벤트를 들어야 한다.



<br>

### Orchestration based SAGA

Orchestration based SAGA에서는 하나의 책임을 가지는 여러 개의 서비스와 그 서비스들 간의 트랜잭션 처리를 담당하는 Orchestrator가 존재한다. Choreography-based SAGA 처럼 각 서비스가 서로 다른 서비스의 이벤트를 청취해야 하는 것 과는 다르게 Orchestrator가 모든 서비스의 이벤트를 청취하고 엔드포인트를 트리거할 책임을 가지고 있다. 

![Image for post](https://miro.medium.com/max/683/1*OxfdbfsX2M7qrv5WsSXAMg.png)

위의 다이어그램에서, Order Orchestrator는 command/reply 방식으로 각 서비스와 통신한다. 



Orchestration based SAGA에서는 Orchestrator가 한 트랜잭션의 흐름을 모두 알고있다는 것을 알 수 있다. 만약 트랜잭션에서 에러가 난다면, 그 에러로 인해 에러 발생 이전에 대한 모든 것들을 롤백하는 책임 또한 Orchestrator가 가지고 있다. 

Orchestrator가 각 변환이 Command나 message에 해당하는 상태 시스템으로 볼 수 있으므로 Orchestration based SAGA를 구현하는 방식 중 하나는 `State Machine Pattern`을 적용하는 것이다. State Machine Pattern은 구현하기 쉽기 때문에 잘 정의된 동작을 구조화하는 데 좋은 패턴이다.



<br>

#### 장점

* 트랜잭션 시라니오에 변화가 생겨도 Orchestrator만 변경하면 되기 때문에 유지보수에 용이하다.
* Orchestrator가 모든 서비스와 통신하기 때문에 서비스 간의 순환 참조를 피할 수 있다.



<br>

#### 단점

* 아무래도 구현하기가 힘들다.
* Orchestrator에게 트랜잭션 관련된 로직들이 엄청 많이 쌓이는데 비즈니스 로직이 추가된다면 유지보수에 엄청 힘들어 질 것이다. 그래서 Orchestration-based SAGA를 구현한다면 Orchestrator에는 트랜잭션 순서?에 관한 로직 (only Command / Reply) 만 작성할 수 있도록 관리해야 한다.
* Orchestrator가 추가되기 때문에 인프라 복잡성이 증가한다.



<br>

## Note

> 보통, SAGA 패턴은 성능과 신뢰성, 확장성을 높이기 위해 Kafka나 RabbitMQ와 같은 message broker를 사용한다.




<br>

## 테스트 프로젝트

[State Pattern](https://github.com/YunSeoWon/TIL-1YEAR/tree/main/design-patterns/state-machine)을 이용해서 Orchestration based SAGA를 구현해볼 예정이다.

<br>

### 프로젝트 구조

SAGA 실습 프로젝트 구조는 아래 그림과 같다.

애플리케이션 서비스는 주문 관련 도메인 기능을 담당하는 OrderService, 상품 관련 도메인 기능을 담당하는 ProductService, 계좌 관련 도메인 기능을 담당하는 AccountService가 있고 이 각각은 각자의 독립된 데이터베이스를 사용한다. 그리고 주문 관련 도메인 분산 트랜잭션 처리를 담당하는 OrderOrchestrator가 있다. (AccountOrchestrator, ProductOchestrator는 일단 생략한다.) 


![스크린샷 2021-01-27 오후 11 57 23](https://user-images.githubusercontent.com/35602698/106384714-9e6a8480-640f-11eb-8927-1d4f517705f8.png)

<br>

### Orchestration based SAGA 구현

#### FSM

먼저 실습을 시작하기 앞서, 주문 로직을 Final State Machine을 표현해보았다.

![FSM](https://user-images.githubusercontent.com/35602698/106384732-b04c2780-640f-11eb-9dc4-7575445569d4.png)


1. 먼저 주문 생성 요청이 들어오면 주문을 Pending 상태로 만들어놓는다. [OrderPending]

2. 주문 내역에 포함된 상품의 재고가 있는지 확인한 다음,

   2-1 재고가 있다면 상품 재고를 요청 상품 개수만큼 뺀다. [OrderProductChecked]

   2-2 재고가 없다면 상품 재고가 없다는 에러와 함께 주문을 Canceled 상태로 만든다. [OrderProductCheckFailed]->[OrderFailed]

3. 상품 재고가 있다면 결제를 진행한다.

   3-1 잔고가 상품 총 가격보다 많다면 잔고를 상품 총 가격만큼 뺀 후 주문을 Approved 상태로 만든다. [OrderPaymentCompleted]->[OrderCompleted]

   3-2 잔고가 부족하다면 상품 재고를 롤백시키고 주문을 Canceled 상태로 만들어놓는다. [OrderPaymentFailed]->[OrderRollBacked]->[OrderFailed]



<br>

#### SAGA 코드 구현

SAGA는 하나의 비즈니스 트랜잭션의 흐름을 관리하는 객체로 트랜잭션에 관한 상태를 가지고 있으며, 해당 상태에서 다음 상태로 가기 위한 적절한 액션을 취하는 객체이다. 

FSM 그림을 참고하면, OrderSaga라는 Saga 객체가 있으며, 이 객체는 OrderPending, OrderProductChecked 와 같은 상태를 가지고 `OrderProductChecked` 상태를 가질 때 이 객체는 결제를 진행하기 위해 `ApplyPayment` 이벤트를 날린다.(액션)

Saga 객체를 코드로 작성하면 다음과 같다.

```kotlin
class OrderSaga private constructor (
    private val eventPublisher: TransactionEventPublisher,
    private var state: OrderSagaState,
    val orderId: Int,
    val customerId: Int,
    val productId: Int,
    val count: Int,
    val key: String
) {

    companion object {
        fun init(
            eventPublisher: TransactionEventPublisher,
            key: String,
            event: OrderCreateEvent
        ): OrderSaga = OrderSaga(
            eventPublisher = eventPublisher,
            state = OrderPending(),
            orderId = event.orderId,
            customerId = event.customerId,
            productId = event.productId,
            count = event.count,
            key = key
        )
    }

    suspend fun changeStateAndOperate(state: OrderSagaState) {
        this.state = state
        this.operate()
    }

    suspend fun operate() {
        state.operate(this)
    }

    fun publishEvent(topic: String, key: String, event: Any): Mono<SenderResult<Void>> {
        return eventPublisher.publishEvent(topic, key, event)
    }
}
```

* private constructor로 생성자로 객체를 생성하는 것을 막고, init()이라는 팩토리 메서드를 만들어서 객체를 생성할 땐 `무조건` OrderPending 상태로 만들어놓게끔 설계했다.
* Saga 객체는 상태를 가지며, 그 상태를 표현하는 객체는 OrderSagaState이다.
* operate() 메서드를 호출함으로써 Saga의 상태에 맞는 적절한 액션을 취하게 되는데, 이는 상태, 즉 OrderSagaState에게 기능을 위임한다. 
* OrderSaga 공통적으로 해당 상태에서 다음 상태로 가기 위해 다른 서비스로 이벤트를 날린다. 그래서 Saga 객체에 eventPublisher가 있다. TransactionEventPublisher는 이벤트 발행을 하는 인터페이스로, Kafka, RabbitMQ 등으로 구현할 수 있다.



<br>

#### SagaState 코드 구현

SagaState는 Saga의 상태를 나타내며, 이 객체에 해당 상태에 맞는 액션을 정의하는 인터페이스다.

```kotlin
interface OrderSagaState {
    suspend fun operate(saga: OrderSaga)
}

```

* operate() 메서드에 해당 상태에 따른 액션을 구현하면 된다.



그리고 FSM에서 설계한 모든 SagaState를 구현체로 만들면 된다.

```kotlin
class OrderPending : OrderSagaState {

    override suspend fun operate(saga: OrderSaga) {
        saga.publishEvent(
            Topic.CHECK_PRODUCT,
            saga.key,
            CheckProductEvent(saga.productId, saga.count)
        ).awaitSingle()
    }
}

class OrderProductChecked (
    private val totalPrice: Int
) : OrderSagaState {

    override suspend fun operate(saga: OrderSaga) {
        saga.publishEvent(
            Topic.APPLY_PAYMENT,
            saga.key,
            ApplyPaymentEvent(saga.customerId, totalPrice)
        ).awaitSingle()
    }
}

...

class OrderPaymentFailed(
    val failureReason: String
): OrderSagaState {

    override suspend fun operate(saga: OrderSaga) {
        saga.publishEvent(
            Topic.PRODUCT_ROLLBACK,
            saga.key,
            ProductRollBackEvent(saga.productId, saga.count, failureReason)
        ).awaitSingle()
    }
}

class OrderPaymentFinished: OrderSagaState {

    override suspend fun operate(saga: OrderSaga) {
        saga.publishEvent(
            Topic.ORDER_COMPLETED,
            saga.key,
            OrderCompleted(saga.orderId)
        ).awaitSingle()
    }
}
```

* 각 SagaState에서는 해당 상태에서 다음 상태로 가기 위해 이벤트를 발행한다.
* 그런데 만들다보니까 Final State인 OrderApproved, OrderCanceled가 없다.
  * FinalState는 (ORDER_APPROVED, ORDER_CANCELED)만 날려줘도 괜찮기 때문(이라고 생각)





<br>

### SAGA 트랜잭션 흘러가는 과정

먼저 주문의 정상적인 시나리오를 도식화하면 다음과 같다.

![스크린샷 2021-01-31 오후 10 02 00](https://user-images.githubusercontent.com/35602698/106384796-0ae58380-6410-11eb-9ab6-df23312742f9.png)




주문 생성 트랜잭션은 성공 기준으로 크게 4가지 스텝이 있다.

1. 주문 Pending 상태로 저장
2. 재고 확인
3. 결제 진행
4. 주문 Approve 상태로 저장



먼저 클라이언트가 주문 생성 요청을 하면 OrderService에서 주문을 `Pending` 상태로 만들어놓고 저장한다. 그 후 OrderOrchestrator가 주문 생성 트랜잭션 작업을 수행하도록 하기 위해 ORDER_CREATED 이벤트를 발행한다.

OrderOrchestrator가 ORDER_CREATED 이벤트를 수신하면 OrderSaga 객체를 생성하여 주문 생성 트랜잭션을 관리한다.

**[OrderOrchestrator] OrderCreationEventListener.kt**

```kotlin
@Component
class OrderCreationEventListener(
    private val eventPublisher: TransactionEventPublisher,
    private val sagaRepository: SagaRepository,
    private val objectMapper: ObjectMapper
) : AcknowledgingMessageListener<String, String> {

    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [ORDER_CREATED], groupId = "order-orchestrator", containerFactory = "orderCreationEventListenerContainerFactory")
    override fun onMessage(data: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        val (key, event) = data.key() to objectMapper.readValue(data.value(), OrderCreateEvent::class.java)

        logger.info("Topic: $ORDER_CREATED, key: $key, event: $event")

        val orderSaga = OrderSaga.init(eventPublisher, key, event)

        sagaRepository.save(key, orderSaga)
        boundedElasticScope.launch {
            orderSaga.operate()
        }
        acknowledgment.acknowledge()
    }
}

```

* OrderCreateEvent 객체에는 주문 생성 트랜잭션에 필요한 데이터가 들어있어야 한다.
* 여기서는 init() 팩토리 메서드로  OrderPending 상태인 OrderSaga를 생성하여 OrderSagaRepository에 저장한 후 OrderSaga의 액션을 실행한다.
* OrderSaga를 만들 때 key가 필요한데 key는 Saga 인스턴스를 식별할 수 있는 유일한 값으로 저장되어야 한다. 코드에는 나와있지 않지만 UUID를 사용하였다.



**[OrderOrchestrator] OrderPending.kt**

```kotlin
class OrderPending : OrderSagaState {

    override suspend fun operate(saga: OrderSaga) {
        saga.publishEvent(
            Topic.CHECK_PRODUCT,
            saga.key,
            CheckProductEvent(saga.productId, saga.count)
        ).awaitSingle()
    }
}
```

* OrderPending 상태에서는 CHECK_PRODUCT 이벤트를 발행한다. CHECK_PRODUCT 이벤트 수신 측은 주문 요청한 상품의 재고가 남아있는지 확인하고 남아있다면 재고를 뺀다.
* Saga 트랜잭션에 관련된 이벤트를 발행할 때는 saga 인스턴스가 가지고있는 고유 키를 넘겨줘야 한다. 트랜잭션 흐름을 유지하기 위해서이다.



CHECK_PRODUCT 이벤트가 발행이 되면 ProductService에서 해당 이벤트를 수신하여 주문 요청한 상품의 재고가 남아있는지 확인하고 남아있다면 재고를 뺀다.

**[ProductService] ProductEventListener.kt**

```kotlin
@Component
class ProductEventListener(
    private val objectMapper: ObjectMapper,
    private val productCommandService: ProductCommandService,
    private val transactionEventPublisher: TransactionEventPublisher
) : AcknowledgingMessageListener<String, String> {

    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [CHECK_PRODUCT], groupId = "product-consumer", containerFactory = "productEventListenerContainerFactory")
    override fun onMessage(data: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        val (key, event) = data.key() to objectMapper.readValue(data.value(), CheckProductEvent::class.java)

        logger.info("Topic: $CHECK_PRODUCT, key: $key, event: $event")

        try {
            val price = productCommandService.checkAndSubtractProduct(event)
            transactionEventPublisher.publishEvent(
                topic = CHECK_PRODUCT_COMPLETED,
                key = key,
                event = CheckProductCompleted(price)
            )
        } catch (e: CustomException) {
            logger.error("[Error]: ", e)

            transactionEventPublisher.publishEvent(
                topic = CHECK_PRODUCT_FAILED,
                key = key,
                event = CheckProductFailed(e.message!!)
            )
        }.let {
            boundedElasticScope.launch {
                it.awaitFirstOrNull()
            }
        }

        acknowledgment.acknowledge()
    }
}
```

* 상품의 재고를 확인한 후 재고가 남아있다면 재고를 주문 수량만큼 뺀 후, CHECK_PRODUCT_COMPLETED 이벤트를 발행한다. 결제 절차를 진행하기 위해 이벤트 메시지에 상품의 총 가격을 담는다.
* 상품 수량 확인을 하지 못했다면 CHECK_PRODUCT_FAILED 이벤트를 발행한다. 이 이벤트 메시지에는 상품 확인 실패 사유를 담는다. (여기서는 재고 부족)





<br>

#### 성공 시나리오

상품 재고 확인에 성공하면 CHECK_PRODUCT_COMPLETED 이벤트를 발행하고, 이 이벤트는 OrderOrchestrator가 수신한다.  (Orchestration based SAGA는 Orchestrator -> Service -> Orchestrator 이런식으로 티키타카한다.) 

재고 확인에 성공하면 Orchestrator는 다음 스텝인 결제 진행을 요청 할 것이다. CHECK_PRODUCT_COMPLETED 이벤트 리스너 코드를 보자.

**[OrderOrchestrator] OrderProductCheckCompletedEventListener.kt**

```kotlin
@Component
class OrderProductCheckCompletedEventListener(
    private val objectMapper: ObjectMapper,
    private val sagaRepository: SagaRepository
) : AcknowledgingMessageListener<String, String> {

    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [CHECK_PRODUCT_COMPLETED], groupId = "order-orchestrator", containerFactory = "orderProductCheckCompletedEventListenerContainerFactory")
    override fun onMessage(data: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        val (key, event) = data.key() to objectMapper.readValue(data.value(), CheckProductCompleted::class.java)

        logger.info("Topic: $CHECK_PRODUCT_COMPLETED, key: $key, event: $event")

        sagaRepository.findById(key)?.let {
            boundedElasticScope.launch {
                it.changeStateAndOperate(
                    OrderProductChecked(event.totalPrice)
                )
            }
            acknowledgment.acknowledge()
        }
    }
}
```

* Service가 비즈니스 로직을 마친 후 발행한 이벤트기 때문에 SagaState가 변경되어야 한다. 이벤트가 발행할 때 key값도 같이 넘겨주기 때문에 key 값을 이용하여 Saga 인스턴스를 꺼낸다.
* Saga 인스턴스를 꺼낸 후 해당 Saga를 OrderProductChecked 상태로 변경하고 즉시 액션을 실행한다.



**[OrderOrchestrator] OrderProductChecked.kt**

```kotlin
class OrderProductChecked (
    private val totalPrice: Int
) : OrderSagaState {

    override suspend fun operate(saga: OrderSaga) {
        saga.publishEvent(
            Topic.APPLY_PAYMENT,
            saga.key,
            ApplyPaymentEvent(saga.customerId, totalPrice)
        ).awaitSingle()
    }
}
```

* OrderProductChecked 상태에서는 결제를 진행하기 위해 APPLY_PAYMENT 이벤트를 발행한다. 해당 이벤트는 AccountService가 수신한다.



**[AccountService] AccountEventListener.kt**

```kotlin
@Component
class AccountEventListener(
    private val objectMapper: ObjectMapper,
    private val accountCommandService: AccountCommandService,
    private val transactionEventPublisher: TransactionEventPublisher
) : AcknowledgingMessageListener<String, String> {

    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [APPLY_PAYMENT], groupId = "account-consumer", containerFactory = "accountEventListenerContainerFactory")
    override fun onMessage(data: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        val (key, event) = (data.key() to objectMapper.readValue(data.value(), ApplyPaymentEvent::class.java))

        logger.info("Topic: $APPLY_PAYMENT, key: $key, event: $event")

        try {
            val restBalance = accountCommandService.applyPayment(event)
            transactionEventPublisher.publishEvent(
                topic = PAYMENT_COMPLETED,
                key = key,
                event = PaymentCompleted(restBalance)
            )
        } catch (e: CustomException) {
            logger.error("[Error]: ", e)

            transactionEventPublisher.publishEvent(
                topic = PAYMENT_FAILED,
                key = key,
                event = PaymentFailed(e.message!!)
            )
        }.let {
            boundedElasticScope.launch {
                it.awaitFirstOrNull()
            }
        }

        acknowledgment.acknowledge()
    }
}
```

* 여기서는 주문 요청한 회원의 계좌 잔고를 확인하여 잔고가 남아있을 경우 결제를 진행한 다음 주문 승인 상태로 만들기 위해 PAYMENT_COMPLETED 이벤트를 발행한다.
* 잔고가 부족하다면 주문 취소 상태로 만들기 위해 PAYMENT_FAILED 이벤트를 발행한다.



결제 진행이 완료되어 PAYMENT_COMPLETED 이벤트가 발행이 되면 Orchestrator는 다음 스텝인 주문 승인 처리를 할 것이다.

key를 이용해 Saga 인스턴스를 조회한 다음 OrderPaymentFinished 상태로 변경한다.

**[OrderOrchestrator] OrderPaymentCompletedEventListener.kt**

```kotlin
@Component
class OrderPaymentCompletedEventListener(
    private val objectMapper: ObjectMapper,
    private val sagaRepository: SagaRepository
) : AcknowledgingMessageListener<String, String> {

    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [PAYMENT_COMPLETED], groupId = "order-orchestrator", containerFactory = "orderPaymentCompletedEventListenerContainerFactory")
    override fun onMessage(data: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        val (key, event) = data.key() to objectMapper.readValue(data.value(), PaymentCompleted::class.java)

        logger.info("Topic: $PAYMENT_COMPLETED, key: $key, event: $event")

        sagaRepository.findById(key)?.let {
            boundedElasticScope.launch {
                it.changeStateAndOperate(
                    OrderPaymentCompleted()
                )
                sagaRepository.deleteById(key)
            }
            acknowledgment.acknowledge()
        }
    }
}

```

* Saga 인스턴스가 OrderPaymentFinished 상태가 되면 주문을 승인 상태로 만든다.
* 주문 승인 상태가 되면 주문 생성 트랜잭션이 끝나기 때문에 Saga 인스턴스를 삭제시킨다.



**[OrderOrchestrator] OrderPaymentFinished.kt**

```kotlin
class OrderPaymentFinished: OrderSagaState {

    override suspend fun operate(saga: OrderSaga) {
        saga.publishEvent(
            Topic.ORDER_COMPLETED,
            saga.key,
            OrderCompleted(saga.orderId)
        ).awaitSingle()
    }
}
```

* OrderPaymentFinished 상태인 Saga 인스턴스는 다음 스텝인 주문 승인 처리를 하기위해 ORDER_COMPLETED 이벤트를 발행한다.



**[OrderService] OrderCompletedEventListener.kt**

```kotlin
@Component
class OrderCompletedEventListener(
    private val objectMapper: ObjectMapper,
    private val orderCommandService: OrderCommandService
) : AcknowledgingMessageListener<String, String> {

    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [ORDER_COMPLETED], groupId = "order-consumer", containerFactory = "orderCompletedEventListenerContainerFactory")
    override fun onMessage(data: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        val (key, event) = data.key() to objectMapper.readValue(data.value(), OrderCompleted::class.java)

        logger.info("Topic: $ORDER_COMPLETED, key: $key, event: $event")

        boundedElasticScope.launch {
            orderCommandService.approve(event.orderId)
        }

        acknowledgment.acknowledge()
    }
}
```

* OrderService에서 ORDER_COMPLETED 이벤트를 발행하여 주문을 승인 상태로 만들어놓는다.
* 주문 생성 트랜잭션을 요청하는 주체가 OrderService지만, Orchestrator에서 비즈니스 로직을 심으면 안되기 때문에 주문 승인 / 취소 처리를 하는 로직을 OrderService로 두었다. 

<br>

#### 실패 시나리오

![스크린샷 2021-01-31 오후 10 02 57](https://user-images.githubusercontent.com/35602698/106384810-205aad80-6410-11eb-97c2-880b699ccb96.png)

만약 결제를 실패했을 경우에는 **(1)주문 수량만큼 뺐던 상품 수량을 다시 되돌리고**, **(2)주문을 취소 상태로** 만들어야 한다.

결제 실패가 되면 PAYMENT_FAILED 이벤트를 발행하는데 OrderOrchestrator가 이 이벤트를 수신하여 상품 수량을 롤백시키고 주문을 취소 상태로 만들어 놓는다.



**[OrderOrchestrator] OrderPaymentFailedEventListener.kt**

```kotlin
@Component
class OrderPaymentFailedEventListener(
    private val objectMapper: ObjectMapper,
    private val sagaRepository: SagaRepository
) : AcknowledgingMessageListener<String, String> {

    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [PAYMENT_FAILED], groupId = "order-orchestrator", containerFactory = "orderPaymentFailedEventListenerContainerFactory")
    override fun onMessage(data: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        val (key, event) = data.key() to objectMapper.readValue(data.value(), PaymentFailed::class.java)

        logger.info("Topic: $PAYMENT_FAILED, key: $key, event: $event")
        logger.info("Failure reason: ${event.failureReason}")

        sagaRepository.findById(key)?.let {
            boundedElasticScope.launch {
                it.changeStateAndOperate(
                    OrderPaymentFailed(event.failureReason)
                )
            }
            acknowledgment.acknowledge()
        }
    }
}

```



**[OrderOrchestrator] OrderPaymentFailed.kt**

```kotlin
class OrderPaymentFailed(
    val failureReason: String
): OrderSagaState {

    override suspend fun operate(saga: OrderSaga) {
        saga.publishEvent(
            Topic.PRODUCT_ROLLBACK,
            saga.key,
            ProductRollBackEvent(saga.productId, saga.count, failureReason)
        ).awaitSingle()
    }
}
```

* OrderPaymentFailed 상태에서는 보상 트랜잭션을 실행한다.
* 상품을 롤백시키는 보상 트랜잭션을 실행하기 위해 CHECK_PRODUCT_ROLLBACK 이벤트를 발행한다.



SAGA에서는 롤백을 직접 애플리케이션 단에서 구현해야 한다. 상품 수량을 롤백시키기 위해 Product Service 에서 리스너를 만든다.

**[ProductService] ProductRollBackEventListener.kt**

```kotlin
@Component
class ProductRollBackEventListener(
    private val objectMapper: ObjectMapper,
    private val productCommandService: ProductCommandService,
    private val transactionEventPublisher: TransactionEventPublisher
) : AcknowledgingMessageListener<String, String> {

    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [PRODUCT_ROLLBACK], groupId = "product-consumer", containerFactory = "productRollBackEventListenerContainerFactory")
    override fun onMessage(data: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        val (key, event) = data.key() to objectMapper.readValue(data.value(), ProductRollBackEvent::class.java)

        logger.info("Topic: $PRODUCT_ROLLBACK, key: $key, event: $event")

        productCommandService.incrementProductCount(event)

        boundedElasticScope.launch {
            transactionEventPublisher.publishEvent(
                topic = Topic.ORDER_ROLLBACKED,
                key = key,
                event = OrderRollBacked(event.failueReason)
            ).awaitFirstOrNull()
        }

        acknowledgment.acknowledge()
    }
}
```

* incrementProductCount() 메서드로 주문 수량만큼 뺀 상품의 수량을 다시 추가시킨다.
* 롤백이 완료되면 ORDER_ROLLBACKED 이벤트를 발행하여 주문을 취소 상태로 만든다.



롤백이 완료되면 ORDER_ROLLBACKED 이벤트를 수신하는 OrderOrchestrator측에서 주문을 취소 상태로 만들기 위해 Saga 상태를 OrderRollbacked로 바꾼다.

**[OrderOrchestrator] OrderRollBackedEventListener.kt**

```kotlin
@Component
class OrderRollBackedEventListener(
    private val objectMapper: ObjectMapper,
    private val sagaRepository: SagaRepository
) : AcknowledgingMessageListener<String, String> {

    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [ORDER_ROLLBACKED], groupId = "order-orchestrator", containerFactory = "orderProductCheckFailedEventListenerContainerFactory")
    override fun onMessage(data: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        val (key, event) = data.key() to objectMapper.readValue(data.value(), CheckProductFailed::class.java)

        logger.info("Topic: $ORDER_ROLLBACKED, key: $key, event: $event")

        sagaRepository.findById(key)?.let {
            boundedElasticScope.launch {
                it.changeStateAndOperate(
                    OrderRollBacked(event.failureReason)
                )
                sagaRepository.deleteById(key)
            }
            acknowledgment.acknowledge()
        }
    }
}
```

* Saga 인스턴스가 OrderRollBacked 상태가 되면 주문을 취소 상태로 만든다.
* 주문이 취소 상태가 되면 주문 생성 트랜잭션이 끝나기 때문에 해당 Saga 인스턴스를 삭제시킨다.

<br>


## 테스트 프로젝트 실습

### Prerequisites
docker-compose를 사용하여 테스트 환경을 간단히 만들어보자.

```shell
$ cd dockers
$ docker-compose up -d
```

### 초기 데이터 생성

Saga 패턴을 이용하여 분산 트랜잭션 환경을 만들어봤는데 실제로 API를 만들어 요청 한 다음 잘 되는지 보고 결과를 분석해보았다. 실험 데이터는 아래와 같이 설정해두었다.
* 처음 애플리케이션을 구동할 때 `application.yaml`의 `spring.jpa.generate-ddl: true`로 설정하여 테이블을 자동 생성한 후 다음과 같이 요청하여 데이터를 추가하자.

```shell
# 회원 계좌 생성
$ curl -XPOST -H 'Content-Type: application/json' "localhost:6000/accounts" -d '{"customerId": 1, "balance": 54000}'

# 상품 생성
curl -XPOST -H 'Content-Type: application/json' "localhost:6020/products" -d '{"name": "사과", "count": 30, "price": 2000}'
```

**상품**

```json
{
  "id": 1,
  "name": "사과",
  "count": 30,
  "price": 2000
}
```

**회원 계좌**

```json
{
  "id": 1,
  "customerId": 1,
  "balance": 54000
}
```

### 주문 생성으로 SAGA 실습

실험 환경에는 2000원 짜리 물건이 30개 있고, 회원 계좌에는 54000원이 쌓여있다. 그리고 주문은 2000원짜리 5개를 요청할 것이다.

```shell
$ curl 
```
**주문**

```json
{
  "productId": 1,
  "count": 5,
  "customerId": 1
}
```


2000원짜리 5개 주문을 15번 요청해보았다. 잔액이 54000원 있기 때문에 5번은 성공할 것이고 10번은 실패할 것이다.

```shell
# 15번 요청
$ curl -XPOST -H 'Content-Type: application/json' "localhost:6010/orders" -d '{"productId": 1, "count": 5, "customerId": 1}'
```
<br>

그리고 주문 결과를 확인해보았다

```shell
# 주문 결과 확인
$ curl -XGET -H 'Content-Type: application/json' "localhost:6010/orders?customerId=1" | jq .
```

```json
[
  {
    "id": 1,
    "productId": 1,
    "count": 5,
    "customerId": 1,
    "orderStatus": "APPROVED",
    "canceledReason": ""
  },
  {
    "id": 2,
    "productId": 1,
    "count": 5,
    "customerId": 1,
    "orderStatus": "APPROVED",
    "canceledReason": ""
  },
  {
    "id": 3,
    "productId": 1,
    "count": 5,
    "customerId": 1,
    "orderStatus": "APPROVED",
    "canceledReason": ""
  },
  {
    "id": 4,
    "productId": 1,
    "count": 5,
    "customerId": 1,
    "orderStatus": "APPROVED",
    "canceledReason": ""
  },
  {
    "id": 5,
    "productId": 1,
    "count": 5,
    "customerId": 1,
    "orderStatus": "APPROVED",
    "canceledReason": ""
  },
  {
    "id": 6,
    "productId": 1,
    "count": 5,
    "customerId": 1,
    "orderStatus": "CANCELED",
    "canceledReason": "잔액이 부족합니다. current: 4000, required: 10000"
  },
  {
    "id": 7,
    "productId": 1,
    "count": 5,
    "customerId": 1,
    "orderStatus": "CANCELED",
    "canceledReason": "상품의 재고가 부족합니다. current: 0, required: 5"
  },
  {
    "id": 8,
    "productId": 1,
    "count": 5,
    "customerId": 1,
    "orderStatus": "CANCELED",
    "canceledReason": "상품의 재고가 부족합니다. current: 0, required: 5"
  },
  {
    "id": 9,
    "productId": 1,
    "count": 5,
    "customerId": 1,
    "orderStatus": "CANCELED",
    "canceledReason": "상품의 재고가 부족합니다. current: 0, required: 5"
  },
  {
    "id": 10,
    "productId": 1,
    "count": 5,
    "customerId": 1,
    "orderStatus": "CANCELED",
    "canceledReason": "상품의 재고가 부족합니다. current: 0, required: 5"
  },
  {
    "id": 11,
    "productId": 1,
    "count": 5,
    "customerId": 1,
    "orderStatus": "CANCELED",
    "canceledReason": "상품의 재고가 부족합니다. current: 0, required: 5"
  },
  {
    "id": 12,
    "productId": 1,
    "count": 5,
    "customerId": 1,
    "orderStatus": "CANCELED",
    "canceledReason": "잔액이 부족합니다. current: 4000, required: 10000"
  },
  {
    "id": 13,
    "productId": 1,
    "count": 5,
    "customerId": 1,
    "orderStatus": "CANCELED",
    "canceledReason": "잔액이 부족합니다. current: 4000, required: 10000"
  },
  {
    "id": 14,
    "productId": 1,
    "count": 5,
    "customerId": 1,
    "orderStatus": "CANCELED",
    "canceledReason": "잔액이 부족합니다. current: 4000, required: 10000"
  },
  {
    "id": 15,
    "productId": 1,
    "count": 5,
    "customerId": 1,
    "orderStatus": "CANCELED",
    "canceledReason": "상품의 재고가 부족합니다. current: 0, required: 5"
  }
]
```

예상과 같게 5번은 APPROVED 상태고 10번은 CANCELED 상태로 저장되었다. 하지만 canceledReason을 보면 **(1) 잔액이 부족합니다.** 와 **(2) 상품의 재고가 부족합니다.** 라는 두 가지 사유가 있다.

실험 데이터 대로라면 결과적으로는 상품의 재고는 5개 남아있지만 **상품의 재고가 부족합니다.** 라는 사유도 같이 있다. 해당 예시에서 SAGA의 단점(또는 한계?)가 드러나는데 SAGA 패턴은 **Isolation**을 지원하지 않는다.



<br>

## 마무리

### SAGA의 특성

분산 트랜잭션에서 2PC와는 다르게 Saga는 DBMS에서 지원하는 트랜잭션을 사용할 수 없기 때문에 Isolation을 지원하지 않는다.

* Atomicity: 트랜잭션, 보상 트랜잭션으로 All or Nothing 보장
* Consistency: 한 서비스 내의 일관성은 로컬 DB가, 여러 서비스 간의 일관성은 애플리케이션에서 보장
* Duratility: 각 서비스의 로컬 DB가 보장




<br>

### Saga 패턴의 한계(?) 또는 이슈

* 디버깅이 힘들다. (여러 마이크로 서비스를 뜯어봐야 한다.)
* 복잡성이 커진다.
* 애플리케이션에서 롤백 시나리오를 구현하기 때문에 DBMS에서 제공하는 롤백을 사용할 수 없다. (오직 커밋만..)
* Saga 워크플로우를 모니터링 할 수 있으면 좋다.
* Isolation을 지원하지 않는다.

