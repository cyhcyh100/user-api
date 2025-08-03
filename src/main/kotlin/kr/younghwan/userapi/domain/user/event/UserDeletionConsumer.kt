package kr.younghwan.userapi.domain.user.event

import kr.younghwan.userapi.config.RabbitMqConfig
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class UserDeletionConsumer {
    private val logger = LoggerFactory.getLogger(UserDeletionConsumer::class.java)

    @RabbitListener(queues = [RabbitMqConfig.USER_DELETE_QUEUE])
    fun receive(userId: Long) {
        logger.info("Sending email to user {}", userId)
        logger.info("Removing files for user {}", userId)
    }
}