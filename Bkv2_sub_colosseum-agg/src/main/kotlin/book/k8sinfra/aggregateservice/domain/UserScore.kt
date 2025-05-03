package book.k8sinfra.aggregateservice.domain

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash

@RedisHash(value = "userScore", timeToLive = 86400)
@Schema(description = "User score", example = """{"userId": 1, "score": 100}""")
data class UserScore(
    @Id
    @Schema(description = "User ID", example = "1")
    val userId: Long,
    @Schema(description = "User score", example = "150")
    val score: Long
) {
    companion object {
        fun from(userId: Long, score: Long): UserScore {
            require(userId > 0) { "User ID must be greater than 0." }
            require(score >= 0) { "Score must be non-negative." }
            return UserScore(userId, score)
        }
    }
}