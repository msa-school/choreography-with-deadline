# Install
```
pip install httpie

cd kafka
docker-compose up
```

# Watch kafka logs:
```
cd kafka 
docker-compose exec -it kafka /bin/bash
cd /bin
kafka-console-consumer --bootstrap-server localhost:9092 --topic choreography.deadline --from-beginning
```

# Run each microservice


# Test

- init
```
http :8088/points userId="jjy" point=10000
http :8088/points/jjy   # 포인트 확인 => 10000

```

- happy path
```
http :8088/orders holderId="jjy" currencyId=1 amount=500 
http :8088/deadlines  # deadline 이 생성됨. 

http :8088/points/jjy   # 포인트가 차감됨. 
http :8083/transactions  # 처리된 트랜잭션이 확인됨.
http :8088/exchanges  # 환전이 벌어짐

http :8088/orders/1   # APPROVED
```
> kafka logs:
```
{"eventType":"OrderCreated","timestamp":1666666399668,"id":7,"currencyId":"1","amount":500.0,"holderId":"jjy"}
{"eventType":"ExchangeSucceed","timestamp":1666666399791,"id":null,"productId":null,"stock":null,"orderId":7,"userId":"jjy","point":500.0}
{"eventType":"PointUsed","timestamp":1666666399861,"id":null,"userId":null,"point":9500.0,"orderId":7}
{"eventType":"OrderPlaced","timestamp":1666666399911,"id":7,"holderId":"jjy"}
```


- compensation with point limit:
```

http :8088/orders holderId="jjy" currencyId=1 amount=50000   # 가진 포인트보다 많은 환전 시도 --> 취소처리되어야
http :8088/orders/2   # REJECTED DUE TO POINT LIMIT 
http :8088/points/jjy   # 포인트가 그대로 9500

```
> kafka logs:
```
{"eventType":"OrderCreated","timestamp":1666666433335,"id":8,"currencyId":"1","amount":50000.0,"holderId":"jjy"}
{"eventType":"ExchangeSucceed","timestamp":1666666433344,"id":null,"productId":null,"stock":null,"orderId":8,"userId":"jjy","point":50000.0}
{"eventType":"PointUseFailed","timestamp":1666666433354,"id":null,"reason":null,"orderId":8}
{"eventType":"OrderRejected","timestamp":1666666433419,"id":8,"currencyId":"1","amount":50000.0,"holderId":"jjy"}
{"eventType":"ExchangeCompensated","timestamp":1666666433529,"id":null,"productId":null,"stock":null,"orderId":8,"userId":null,"point":50000.0}
```

- compensation with downtime:
stop exchange service and test:
```
ctrl+c
```

and create an order:
```
http :8088/orders holderId="jjy" amount=500   
```

start exchange service:
```
cd exchange
mvn spring-boot:run
```

and see the difference:
```
http :8088/orders   # REJECTED DUE TO DEADLINE 
http :8088/points/jjy   # 포인트가 그대로 9500
```

> kafka logs:
```

{"eventType":"OrderCreated","timestamp":1666662644318,"id":5,"currencyId":null,"amount":500.0,"holderId":"jjy"}
{"eventType":"DeadlineReached","timestamp":1666662649358,"id":5,"deadline":"2022-10-25T01:50:49.318+00:00","orderId":5}
{"eventType":"OrderRejected","timestamp":1666662649368,"id":5,"currencyId":null,"amount":500.0,"holderId":"jjy"}

# expired events are ignored
```

- deadline handling with ignoring expired events:
```
http :8088/orders holderId="jjy" currencyId=100 amount=500   # currencyId 를 100으로 주면 구간 1에 대하여 10 초 delay 하게 해놨음.

# wait 5 seconds
http :8088/orders   # REJECTED DUE TO DEADLINE 
http :8088/points/jjy   # 포인트가 그대로 9500

```

> kafka logs:
```

{"eventType":"OrderCreated","timestamp":1666662644318,"id":5,"currencyId":null,"amount":500.0,"holderId":"jjy"}
{"eventType":"DeadlineReached","timestamp":1666662649358,"id":5,"deadline":"2022-10-25T01:50:49.318+00:00","orderId":5}
{"eventType":"OrderRejected","timestamp":1666662649368,"id":5,"currencyId":null,"amount":500.0,"holderId":"jjy"}

# expired events are ignored
```

[NOTE] deadline timer 가 가끔씩 잠을 잔다. http:8088/deadlines 를 호출해주어서 일깨워줘야 한다. 개발기에서만 이러는가? 

# QUIZ1

## 문제


- deadline handling with compensation:
 currencyId 를 200으로 주면 expired event 를 걸러내는 이후의 아주 짧은 순간이지만 여기에 delay 를 주입하여 미쳐확인하지 못한 expired event (처리중에 expired 된) 가 스며들 게 하였다. 이는 deadline 이 넘어선 OrderCreated 를 결국 처리한 꼴이 되며, 어쩔 수 없이 compensation 처리가 필요한 상황이 된다.

```
http :8088/orders holderId="jjy" currencyId=200 amount=500 

# wait 5 seconds
http :8088/orders   # REJECTED DUE TO DEADLINE 됐었다가 #APPROVED 가 되는 일관성이 없는 상태 발생. 
http :8088/points/jjy   # 포인트가 감소한 상황 9000
http :8088/exchanges   # exchanges 는 제거된 상황 그리고 order 는 APPOVED 인. 잘못된 상태.

```
> kafka logs:
```
{"eventType":"OrderCreated","timestamp":1666666601807,"id":10,"currencyId":"200","amount":500.0,"holderId":"jjy"}
{"eventType":"DeadlineReached","timestamp":1666666611805,"id":4,"deadline":"2022-10-25T02:56:46.807+00:00","orderId":10}
{"eventType":"OrderRejected","timestamp":1666666611814,"id":10,"currencyId":"200","amount":500.0,"holderId":"jjy"}
{"eventType":"ExchangeSucceed","timestamp":1666666611837,"id":null,"productId":null,"stock":null,"orderId":10,"userId":"jjy","point":500.0}
{"eventType":"PointUsed","timestamp":1666666611914,"id":null,"userId":null,"point":9000.0,"orderId":10}
{"eventType":"OrderPlaced","timestamp":1666666611922,"id":10,"holderId":"jjy"}
{"eventType":"ExchangeCompensated","timestamp":1666666611927,"id":null,"productId":null,"stock":null,"orderId":10,"userId":null,"point":500.0}
```

- Exchange 혼자만 compesate 됐다

- 원인: OrderRejected 이벤트를 exchange 와 point 가 동시에 받기 때문에 point 는 해당 이벤트를 무시하였고, 이후에 exchange 에 의해 ExchangeSucceded 가 발생한 것을 PointUsed 가 읽어들여 포인트 차감 처리하기 때문.

## 문제해결방법1: 프로세스의 변경
- 조치:

1. 기존 OrderRejected -> {exchange.compensate, point.compensate} 에서 OrderRejected -> exchange.compensate (ExchangeCompensated) -> point.compensate 로 수정
1. 이미 REJECTED 된 상태에서 넘어온 PointUsed 에 의한 Order.approve 는 무시하도록 처리

> kafka log를 아래와 같이 떨어지도록 변경한다:
```
{"eventType":"OrderCreated","timestamp":1666663281996,"id":11,"currencyId":"200","amount":500.0,"holderId":"jjy"}
{"eventType":"DeadlineReached","timestamp":1666663289358,"id":11,"deadline":"2022-10-25T02:01:26.996+00:00","orderId":11}
{"eventType":"OrderRejected","timestamp":1666663289366,"id":11,"currencyId":"200","amount":500.0,"holderId":"jjy"}
{"eventType":"ExchangeSucceed","timestamp":1666663292007,"id":null,"productId":null,"stock":null,"orderId":11,"userId":"jjy","pointUsed":500.0}
{"eventType":"PointUsed","timestamp":1666663292017,"id":null,"userId":null,"point":7000.0,"orderId":11}
{"eventType":"ExchangeCompensated","timestamp":1666663292007,"id":null,"productId":null,"stock":null,"orderId":11,"userId":"jjy","pointUsed":500.0}

# ignore PointUsed 
```

## 문제해결방법2: ExchangeSucceded 이벤트에 대해서도 expiration 처리
- 조치:

1. Point 의 usePoint method 의 진입 초기에 expired 된 ExchangeSucceded 이벤트를 무시함
1. Exchange 는 compensate 됨.

> kafka log를 아래와 같이 떨어지도록 변경한다:
```
{"eventType":"OrderCreated","timestamp":1666663281996,"id":11,"currencyId":"200","amount":500.0,"holderId":"jjy"}
{"eventType":"DeadlineReached","timestamp":1666663289358,"id":11,"deadline":"2022-10-25T02:01:26.996+00:00","orderId":11}
{"eventType":"OrderRejected","timestamp":1666663289366,"id":11,"currencyId":"200","amount":500.0,"holderId":"jjy"}
{"eventType":"ExchangeSucceed","timestamp":1666663292007,"id":null,"productId":null,"stock":null,"orderId":11,"userId":"jjy","pointUsed":500.0}

# exchange compensated
# point ignores ExchangeSucceeded that is expired
```

## 문제해결방법3: 사람이 처리
- kafka 로그를 읽고 수작업 복구
- 그러려면 해당 주문건과 correlated 된 이벤트들만 필터링해서 보는 (orderId) 모니터링 도구 필요.


