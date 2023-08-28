package com.yupi.springbootinit.manager;

import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.ThrowUtils;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * RedisLimitManager
 *
 * @author xlhl
 * @version 1.0
 * @description 专门提供 RedissonLimiter 服务类（提供了通用能力）
 */
@Component
public class RedisLimiterManager {

    @Resource
    private RedissonClient redisson;

    /**
     * 限流操作
     *
     * @param key  不同的限流区分器 例：用户id
     * @param role 是否为管理员 身份越高，值越小
     */
    public void doRateLimit(String key, Long role) {
        RRateLimiter rateLimiter = redisson.getRateLimiter(key);

        //1 秒允许访问 1次      参数分别代表：模式，是否存放在统一redis中    允许请求的次数       间隔          间隔时间单位
        rateLimiter.trySetRate(RateType.OVERALL, 2, 1, RateIntervalUnit.SECONDS);

        //  每当一个操作进入之后，请求一个令牌
        //                                  每个请求获取几个令牌
        boolean acquire = rateLimiter.tryAcquire(role);
        ThrowUtils.throwIf(!acquire, ErrorCode.TOO_MANY_REQUEST);
    }

}
