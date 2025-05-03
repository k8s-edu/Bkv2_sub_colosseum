package book.k8sinfra.aggregateservice.repository

import book.k8sinfra.aggregateservice.domain.UserScore
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserScoreRepository: CrudRepository<UserScore, Long> {}
