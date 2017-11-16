package com.example.core.autoconfig

import com.example.core.domain.Message
import com.example.core.repositories.MessageRepository
import com.example.core.services.BufferedPersistentMessageQueue
import com.mongodb.MongoClient
import com.mongodb.MongoClientOptions
import com.mongodb.ServerAddress
import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.TypeExcludeFilter
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.*
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactoryBean
import org.springframework.scheduling.annotation.EnableScheduling


@Configuration
@EnableConfigurationProperties(MongoProperties::class)
@EnableMongoRepositories(repositoryBaseClass = MessageRepository::class)
@EnableScheduling
class MongoAutoConfiguration(private val mongoProperties: MongoProperties) {

    @ConditionalOnMissingBean
    @Bean
    fun mongoClient(): MongoClient {
        val options = MongoClientOptions.builder()
                .connectionsPerHost(4)
                .minConnectionsPerHost(2)
                .build()

        return MongoClient(ServerAddress(mongoProperties.host, mongoProperties.port.toInt()),
                options)
    }

    @Bean
    fun startMongoRepo(o: MongoOperations): MessageRepository {
        return MongoRepositoryFactory(o).getRepository(MessageRepository::class.java)
    }

    @Bean
    @Profile("prod")
    internal fun bufferedPersistentMessageQueue(messageRepository: MongoRepository<Message, String>): BufferedPersistentMessageQueue {
        return BufferedPersistentMessageQueue(messageRepository)
    }
}