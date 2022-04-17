package br.com.demoprojectaws.service;

import br.com.demoprojectaws.controller.ProductController;
import br.com.demoprojectaws.enums.EventType;
import br.com.demoprojectaws.model.Envelope;
import br.com.demoprojectaws.model.Product;
import br.com.demoprojectaws.model.ProductEvent;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sns.model.Topic;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class ProductPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(ProductPublisher.class);

    private AmazonSNS snsClient;
    private Topic productEventsTopic;
    private ObjectMapper objectMapper;

    public ProductPublisher(AmazonSNS snsClient,
                            @Qualifier("productEventsTopic")Topic productEventsTopic,
                            ObjectMapper objectMapper){

        this.snsClient = snsClient;
        this.productEventsTopic = productEventsTopic;
        this.objectMapper = objectMapper;
    }


    public void publishProductEvent(Product product, EventType eventType, String username){
        ProductEvent productEvent = new ProductEvent();
        productEvent.setProductId(product.getId());
        productEvent.setCode(product.getCode());
        productEvent.setUsername(username);

        Envelope envelope = new Envelope();
        envelope.setEventType(eventType);

        try {
            envelope.setData(objectMapper.writeValueAsString(productEvent));

            PublishResult publish = snsClient.publish(
                    productEventsTopic.getTopicArn(),
                    objectMapper.writeValueAsString(envelope));

            LOG.info("Product event sent - Event:{} - MessageId:{}",envelope.getEventType(),publish.getMessageId());

        } catch (JsonProcessingException e) {
            LOG.error("Failed to create product event message");
        }

    }


}
