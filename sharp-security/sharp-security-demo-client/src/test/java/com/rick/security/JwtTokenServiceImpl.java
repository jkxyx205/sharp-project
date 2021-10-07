package com.rick.security;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import org.apache.commons.codec.digest.Md5Crypt;

/**
 * @author Rick
 * @createdAt 2021-09-17 22:52:00
 */
public class JwtTokenServiceImpl {

    public String generateTokenByRSA() throws JOSEException {

        //创建JWS头，设置签名算法和类型
        JWSHeader jwsHeader = new JWSHeader.Builder(JWSAlgorithm.RS512).
                type(JOSEObjectType.JWT)
                .build();
        //将负载信息封装到Payload中
        Payload payload = new Payload("{\"name\": \"Rick\"}");
        //创建JWS对象
        JWSObject jwsObject = new JWSObject(jwsHeader, payload);
        //签名
        //创建HMAC签名器
        JWSSigner jwsSigner = new MACSigner(Md5Crypt.apr1Crypt("aa"));
        //签名
        jwsObject.sign(jwsSigner);
        return jwsObject.serialize();
    }

    public static void main(String[] args) throws JOSEException {
        JwtTokenServiceImpl jwtTokenService = new JwtTokenServiceImpl();
        System.out.println(jwtTokenService.generateTokenByRSA());
    }

}
