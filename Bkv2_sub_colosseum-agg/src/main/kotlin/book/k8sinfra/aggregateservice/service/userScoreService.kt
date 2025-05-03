package book.k8sinfra.aggregateservice.service

import book.k8sinfra.aggregateservice.domain.UserScore
import book.k8sinfra.aggregateservice.repository.UserScoreRepository
import org.springframework.stereotype.Service

@Service
class UserScoreService(
    private val userScoreRepository: UserScoreRepository
) {
    fun getUserScore(userId: Long) = userScoreRepository.findById(userId)
    fun saveUserScore(userId: Long, score: Long) = userScoreRepository.save(UserScore(userId, score))
}