# Run kafka firstly and install httpie
```
pip install httpie

cd kafka
docker-compose up
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

- compensation with point limit:
```

http :8088/orders holderId="jjy" currencyId=1 amount=50000   # 가진 포인트보다 많은 환전 시도 --> 취소처리되어야
http :8088/orders/2   # REJECTED DUE TO POINT LIMIT 
http :8088/points/jjy   # 포인트가 그대로 9500

```

- 
```
http :8088/orders holderId="jjy" currencyId=100 amount=500   # currencyId 를 100으로 주면 10 초 delay 하게 해놨음.
http :8088/orders   # REJECTED DUE TO DEADLINE 
http :8088/points/jjy   # 포인트가 그대로 9500

```


# Analize
```
cd kafka 
docker-compose exec -it kafka /bin/bash
cd /bin
kafka-console-consumer --bootstrap-server localhost:9092 --topic choreography.deadline --from-beginning
```