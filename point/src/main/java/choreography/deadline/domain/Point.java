package choreography.deadline.domain;

import choreography.deadline.PointApplication;
import choreography.deadline.domain.PointUseFailed;
import choreography.deadline.domain.PointUsed;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Point_table")
@Data
public class Point {

    @Id
    //@GeneratedValue(strategy = GenerationType.AUTO)  // FOCUS: disable auto-gen for key. 
    private String userId;

    private Double point;

    public static PointRepository repository() {
        PointRepository pointRepository = PointApplication.applicationContext.getBean(
            PointRepository.class
        );
        return pointRepository;
    }

    public static void usePoint(ExchangeSucceed exchangeSucceed) {

        //FOCUS: 멱등성 관리. 한번 처리된 적이 있다면 스킵. handle idempotent:   once processed, skip the process:
        if(Transaction.repository().findById(exchangeSucceed.getOrderId()).isPresent())
            return;
        
        repository().findById(exchangeSucceed.getUserId()).ifPresent(point->{

            if(point.getPoint() > exchangeSucceed.getPoint()){

                point.setPoint(point.getPoint() - exchangeSucceed.getPoint()); 
                repository().save(point);

                // 멱등성 처리를 위하여 한번 포인트 사용한 주문에 대해서는 처리되었음을 동일 트랜잭션 범위 내에서 플래그 처리함.
                Transaction transaction = new Transaction();
                transaction.setOrderId(exchangeSucceed.getOrderId());
                transaction.setPointUsed(exchangeSucceed.getPoint());
                transaction.setUserId(exchangeSucceed.getUserId());
                Transaction.repository().save(transaction);

                PointUsed pointUsed = new PointUsed(point);
                pointUsed.setOrderId(exchangeSucceed.getOrderId());
                pointUsed.publishAfterCommit();
            }else{
                PointUseFailed pointUseFailed = new PointUseFailed(point);
                pointUseFailed.publishAfterCommit();    
                pointUseFailed.setOrderId(exchangeSucceed.getOrderId());
            }

         });

    }

    public static void compensate(OrderRejected orderRejected) {
        
        Transaction.repository().findById(orderRejected.getId()).ifPresent/*OrElse*/(tx ->{

            repository().findById(orderRejected.getHolderId()).ifPresent(point->{
    
                point.setPoint(point.getPoint() + tx.getPointUsed()); 
                repository().save(point);

                Transaction.repository().delete(tx);  //FOCUS: 멱등성을 관리하기 위하여 두번 보상 처리되는 것을 막기 위해 트랜잭션 이력을 삭제함 (플래그로 처리해도 되긴 함).  handle idempotent. delete to prevent to process twice

                new PointUseCompensated(point).publish();
    
             });

        }
        // ,()->{
        //     throw new RuntimteException("Compensation failed due to point")
        // }
        
        );

    }

    public static void compensate(ExchangeCompensated exchangeCompensated) {

        Transaction.repository().findById(exchangeCompensated.getOrderId()).ifPresent/*OrElse*/(tx ->{

            repository().findById(tx.getUserId()).ifPresent(point->{
    
                point.setPoint(point.getPoint() + tx.getPointUsed()); 
                repository().save(point);

                Transaction.repository().delete(tx);  //FOCUS: 멱등성을 관리하기 위하여 두번 보상 처리되는 것을 막기 위해 트랜잭션 이력을 삭제함 (플래그로 처리해도 되긴 함).  handle idempotent. delete to prevent to process twice

                new PointUseCompensated(point).publish();
    
             });

        }
        // ,()->{
        //     throw new RuntimteException("Compensation failed due to point")
        // }
        
        );
    }
}
