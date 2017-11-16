package com.example.core.services

import com.example.core.domain.Message
import com.example.core.repositories.MessageRepository
import mu.KLogging
import org.springframework.context.annotation.Profile
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.LinkedBlockingQueue

@EnableScheduling

class BufferedPersistentMessageQueue(private val messageRepository: MongoRepository<Message, String>) : PersistentMessageQueue {

    private companion object : KLogging()

    private val queue = LinkedBlockingQueue<Message>(50)

    override fun offer(message: Message): Boolean {
        val offered = queue.offer(message)
        if (!offered) {
            logger.warn { "message rejected due to queue overflow" }
        }
        return offered
    }

    @Scheduled(fixedDelay = 5_000)
    private fun persist() {
        if (queue.isEmpty()) return

        val batch = LinkedList<Message>()
        queue.drainTo(batch)
        messageRepository.saveAll(batch)
    }

}