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

- happy path
```
http :8088/points userId="jjy" point=10000
http :8088/points/jjy   # 포인트 확인 => 10000

http :8088/orders holderId="jjy" currencyId=1 amount=500 
http :8088/deadlines  # deadline 이 생성됨. 

http :8088/points/jjy   # 포인트가 차감됨. 
http :8083/transactions  # 처리된 트랜잭션이 확인됨.
http :8088/exchanges  # 환전이 벌어짐

http :8088/orders/1   # APPROVED
```
> kafka logs:
```
{"eventType":"OrderCreated","timestamp":1666662150890,"id":1,"currencyId":"1","amount":500.0,"holderId":"jjy"}
{"eventType":"ExchangeSucceed","timestamp":1666662151255,"id":null,"productId":null,"stock":null,"orderId":1,"userId":"jjy","pointUsed":500.0}
{"eventType":"PointUsed","timestamp":1666662151415,"id":null,"userId":null,"point":9500.0,"orderId":1}
{"eventType":"OrderPlaced","timestamp":1666662151535,"id":1,"holderId":"jjy"}
```


- compensation with point limit:
```

http :8088/orders holderId="jjy" currencyId=1 amount=50000   # 가진 포인트보다 많은 환전 시도 --> 취소처리되어야
http :8088/orders/2   # REJECTED DUE TO POINT LIMIT 
http :8088/points/jjy   # 포인트가 그대로 9500

```
> kafka logs:
```
{"eventType":"OrderCreated","timestamp":1666662166363,"id":2,"currencyId":"1","amount":50000.0,"holderId":"jjy"}
{"eventType":"ExchangeSucceed","timestamp":1666662166375,"id":null,"productId":null,"stock":null,"orderId":2,"userId":"jjy","pointUsed":50000.0}
{"eventType":"PointUseFailed","timestamp":1666662166384,"id":null,"reason":null,"orderId":2}
{"eventType":"OrderRejected","timestamp":1666662166398,"id":2,"currencyId":"1","amount":50000.0,"holderId":"jjy"}
```

- compensation with downtime:
stop exchange service and test:
```
ctrl+c
```

and create an order:
```
http :8088/orders holderId="jjy" amount=500   

# wait 5 seconds
http :8088/orders   # REJECTED DUE TO DEADLINE 
http :8088/points/jjy   # 포인트가 그대로 9500
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

- deadline handling with igonring expired events:
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


- deadline handling with compensation:
 currencyId 를 200으로 주면 expired event 를 걸러내는 이후의 아주 짧은 순간이지만 여기에 delay 를 주입하여 미쳐확인하지 못한 expired event (처리중에 expired 된) 가 스며들 게 하였다. 이는 deadline 이 넘어선 OrderCreated 를 결국 처리한 꼴이 되며, 어쩔 수 없이 compensation 처리가 필요한 상황이 된다.

```
http :8088/orders holderId="jjy" currencyId=200 amount=500 

# wait 5 seconds
http :8088/orders   # REJECTED DUE TO DEADLINE 됐었다가 #APPROVED 가 되는 일관성이 없는 상태 발생. 
http :8088/points/jjy   # 포인트가 감소한 상황 9000

```
> kafka logs:
```
{"eventType":"OrderCreated","timestamp":1666663281996,"id":11,"currencyId":"200","amount":500.0,"holderId":"jjy"}
{"eventType":"DeadlineReached","timestamp":1666663289358,"id":11,"deadline":"2022-10-25T02:01:26.996+00:00","orderId":11}
{"eventType":"OrderRejected","timestamp":1666663289366,"id":11,"currencyId":"200","amount":500.0,"holderId":"jjy"}
{"eventType":"ExchangeSucceed","timestamp":1666663292007,"id":null,"productId":null,"stock":null,"orderId":11,"userId":"jjy","pointUsed":500.0}
{"eventType":"PointUsed","timestamp":1666663292017,"id":null,"userId":null,"point":7000.0,"orderId":11}
{"eventType":"OrderPlaced","timestamp":1666663292025,"id":11,"holderId":"jjy"}

```


## 문제해결: 프로세스의 변경
- 원인: ExchangeSucceded 가 발생한 후에 
- 기존 OrderRejected -> {exchange.compensate, point.compensate} 에서 OrderRejected -> exchange.compensate (ExchangeCompensated) -> point.compensate 로 수정해야한다.

