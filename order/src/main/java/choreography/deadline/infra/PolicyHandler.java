package choreography.deadline.infra;

import choreography.deadline.config.kafka.KafkaProcessor;
import choreography.deadline.domain.*;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.naming.NameParser;
import javax.naming.NameParser;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class PolicyHandler {

    @Autowired
    OrderRepository orderRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString) {}

    @StreamListener(
        value = KafkaProcessor.INPUT,
        condition = "headers['type']=='ExchangeFailed'"
    )
    public void wheneverExchangeFailed_Reject(
        @Payload ExchangeFailed exchangeFailed
    ) {
        ExchangeFailed event = exchangeFailed;
        System.out.println(
            "\n\n##### listener Reject : " + exchangeFailed + "\n\n"
        );

        // Sample Logic //
        Order.reject(event);
    }

    @StreamListener(
        value = KafkaProcessor.INPUT,
        condition = "headers['type']=='DeadlineReached'"
    )
    public void wheneverDeadlineReached_Reject(
        @Payload DeadlineReached deadlineReached
    ) {
        DeadlineReached event = deadlineReached;
        System.out.println(
            "\n\n##### listener Reject : " + deadlineReached + "\n\n"
        );

        // Sample Logic //
        Order.reject(event);
    }

    @StreamListener(
        value = KafkaProcessor.INPUT,
        condition = "headers['type']=='PointUseFailed'"
    )
    public void wheneverPointUseFailed_Reject(
        @Payload PointUseFailed pointUseFailed
    ) {
        PointUseFailed event = pointUseFailed;
        System.out.println(
            "\n\n##### listener Reject : " + pointUseFailed + "\n\n"
        );

        // Sample Logic //
        Order.reject(event);
    }

    @StreamListener(
        value = KafkaProcessor.INPUT,
        condition = "headers['type']=='PointUsed'"
    )
    public void wheneverPointUsed_Approve(@Payload PointUsed pointUsed) {
        PointUsed event = pointUsed;
        System.out.println(
            "\n\n##### listener Approve : " + pointUsed + "\n\n"
        );

        // Sample Logic //
        Order.approve(event);
    }
}
