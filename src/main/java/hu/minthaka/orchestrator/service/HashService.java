package hu.minthaka.orchestrator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.time.Duration;

@Slf4j
@RequiredArgsConstructor
@Service
public class HashService {

  private final ReactiveRedisTemplate<String, String> redisTemplate;

  public Mono<Boolean> duplicate(String key, String hash) {
    return redisTemplate.opsForList().indexOf(key, hash)
        .map(index -> true)
        .switchIfEmpty(
            redisTemplate.opsForList().rightPush(key, hash)
                .flatMap(record -> redisTemplate.expire(key, Duration.ofMinutes(5)))
                .map(success -> false)
        );


  }

}
