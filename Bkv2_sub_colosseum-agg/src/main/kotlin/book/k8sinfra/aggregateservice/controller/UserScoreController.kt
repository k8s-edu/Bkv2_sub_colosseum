package book.k8sinfra.aggregateservice.controller

import book.k8sinfra.aggregateservice.domain.UserScore
import book.k8sinfra.aggregateservice.service.UserScoreService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@RestController
@RequestMapping("api/v1/score")
class ScoreController(private val userScoreService: UserScoreService) {

    private val logger = LoggerFactory.getLogger(ScoreController::class.java)

    @GetMapping("/{userId}")
    @Operation(summary = "Get a score by user ID", description = "Get a score by user ID", tags = ["score"])
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successful operation", content = [Content(mediaType = "application/json", schema = Schema(implementation = UserScore::class))]),
            ApiResponse(responseCode = "404", description = "User score not found", content = [Content()])
        ]
    )
    fun getUserScore(@PathVariable userId: Long): ResponseEntity<UserScore> {
        return userScoreService.getUserScore(userId)
            .map {
                logger.info("Get user score by user ID: $userId")
                ResponseEntity.ok(it)
            }.orElseGet {
                logger.info("Get user score by user ID: $userId : not found")
                ResponseEntity.notFound().build()
            }
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Update a score by user ID", description = "Update a score by user ID", tags = ["score"])
    fun updateScore(@PathVariable userId: Long, @RequestBody scoreRequest: UpdateScoreRequest): UpdateScoreResponse {
        val savedScore = userScoreService.saveUserScore(userId, scoreRequest.score)
        logger.info("Update user score by user ID: $userId to ${savedScore.score}")
        return UpdateScoreResponse(status = HttpStatus.OK.value(), message = "success", score = savedScore)
    }

    @PostMapping("/{userId}")
    @Operation(summary = "Store accumulate a score by user ID", description = "Store accumulate a score by user ID", tags = ["score"])
    fun upsertScore(@PathVariable userId: Long, @RequestBody scoreRequest: UpdateScoreRequest): UpdateScoreResponse {
        val savedScore = userScoreService.getUserScore(userId).map {
            logger.info("User score by user ID: $userId found, adding ${scoreRequest.score} to current score")
            userScoreService.saveUserScore(userId, it.score + scoreRequest.score)
        }.orElseGet {
            logger.info("User score by user ID: $userId not found, creating new score to ${scoreRequest.score}")
            userScoreService.saveUserScore(userId, scoreRequest.score)
        }
        val miliseconds = (1000..3000).random().toLong()
        simulateCpuLoadWithHmac(miliseconds)
        logger.warn("This request was processed abnormally")
        return UpdateScoreResponse(status = HttpStatus.OK.value(), message = "success", score = savedScore)
    }

    fun simulateCpuLoadWithHmac(durationMillis: Long) {
        val start = System.currentTimeMillis()
        val secret = "verySecretKey".toByteArray()
        val data = UUID.randomUUID().toString().toByteArray()

        val hmacSha256 = Mac.getInstance("HmacSHA256")
        hmacSha256.init(SecretKeySpec(secret, "HmacSHA256"))

        var iterations = 0
        while (System.currentTimeMillis() - start < durationMillis) {
            val hash = hmacSha256.doFinal(data)
            iterations++
        }
}

    @Schema(description = "Request body for upserting a score", example = """{"score": 100}""")
    data class UpdateScoreRequest(
        @Schema(description = "Score to be added to the current score", example = "60")
        val score: Long
    )

    @Schema(description = "Response body for updating a score", example = """{"status": 200, "message": "success", "score": {"userId": 1, "score": 100}}""")
    data class UpdateScoreResponse(
        @Schema(description = "HTTP Status code", example = "200")
        val status: Int,
        @Schema(description = "Status message from apiserver", example = "success message")
        val message: String,
        @Schema(description = "Current User score", example = """{"userId": 1, "score": 100}""")
        val score: UserScore
    )
}