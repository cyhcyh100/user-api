package kr.younghwan.userapi.domain.user.event

import kr.younghwan.userapi.config.RabbitMqConfig
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component

@Component
class UserDeletionProducer(
    private val rabbitTemplate: RabbitTemplate,
) {
    fun sendUserDeletedEvent(userId: Long) {
        rabbitTemplate.convertAndSend(RabbitMqConfig.USER_DELETE_QUEUE, userId)
    }
}