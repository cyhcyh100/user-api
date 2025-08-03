package kr.younghwan.userapi.config

import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitMqConfig {
    companion object {
        const val USER_DELETE_QUEUE = "user.delete.queue"
    }

    @Bean
    fun userDeleteQueue(): Queue = Queue(USER_DELETE_QUEUE, true)

    @Bean
    fun amqpAdmin(connectionFactory: ConnectionFactory): RabbitAdmin = RabbitAdmin(connectionFactory).apply {
        isAutoStartup = false
    }
}