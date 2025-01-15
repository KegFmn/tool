package com.likc.tool.util;

import com.ama.recharge.common.BizException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Map;

/**
 * @Author: likc
 * @Date: 2022/02/19/20:36
 * @Description: jwt工具类
 */
@Slf4j
public class JwtUtils {

    private static final String SECRET = "f6f31a5f2136758f86b67cde583cb115";
    private static final Long EXPIRE = 30L;

    /**
     * 根据payload信息生成JSON WEB TOKEN
     *
     * @return
     */
    public static String createJwt(String id) {
        Instant now = Instant.now();
        Instant expire = now.plus(EXPIRE, ChronoUnit.DAYS);

        Algorithm algorithm = Algorithm.HMAC512(Base64.getDecoder().decode(SECRET.getBytes()));

        return JWT.create()
                .withHeader(Map.of("typ", "JWT"))
                .withSubject(id)
                .withIssuedAt(now)
                .withExpiresAt(expire)
                .sign(algorithm);
    }

    /**
     * 校验并获得Token中的信息
     * 使用实例：decodedJWT.getClaim("exp").asDate()
     *
     * @param token
     * @return
     */
    public static DecodedJWT verify(String token) {
        Algorithm algorithm = Algorithm.HMAC512(Base64.getDecoder().decode(SECRET.getBytes()));
        try {
            return JWT.require(algorithm).build().verify(token);
        } catch (Exception e) {
            log.error("解析token出错", e);
        }

        throw new BizException("解析token出错");
    }
}