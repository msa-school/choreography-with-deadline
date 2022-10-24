```
http :8088/points userId="jjy" point=10000
http :8088/deadlines  # deadline 이 생성됨
http :8088/orders holderId="jjy" currencyId=1 amount=500 
http :8088/points   # 포인트가 차감됨
http :8083/transactions  # 처리된 트랜잭션이 확인됨
http :8088/exchanges  # 환전이 벌어짐


http :8088/orders holderId="jjy" currencyId=1 amount=50000   # 가진 포인트보다 많은 환전 시도 --> 취소처리되어야


http :8088/orders holderId="jjy" currencyId=1 amount=500 delaySecond=70   # deadline 을 초과하는 거래 시도 ---> 취소처리되어야

```