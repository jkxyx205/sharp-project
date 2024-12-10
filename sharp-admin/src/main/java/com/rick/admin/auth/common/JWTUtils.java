package com.rick.admin.auth.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.rick.admin.auth.authentication.AdminUserDetails;
import com.rick.admin.sys.user.entity.User;
import com.rick.common.http.exception.BizException;
import com.rick.common.http.model.ResultUtils;
import com.rick.common.util.JsonUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

/**
 * @author Rick
 * @createdAt 2021-09-24 17:33:00
 */
public class JWTUtils {

    /**
     * 创建秘钥
     */
    private static final byte[] SECRET = "6MNSobBRCHGIO0fS6MNSobBRCHGIO0fS".getBytes();

    /**
     * 过期时间500秒
     */
    private static final long EXPIRE_TIME = 1000 * 500;


    public static String createToken(Authentication authentication) {
        return createToken(authentication, null);
    }
    /**
     * 生成Token
     * @param authentication
     * @return
     */
    public static String createToken(Authentication authentication, Map<String, Object> additionalInformation) {
        try {
            /**
             * 1.创建一个32-byte的密匙
             */
            MACSigner macSigner = new MACSigner(SECRET);
            /**
             * 2. 建立payload 载体
             */
            AdminUserDetails principal = (AdminUserDetails) authentication.getPrincipal();
            User user = principal.getUser();
            user.setRoleList(null);
            user.setDocumentList(null);
            user.setAuthorityList(null);
            JWTClaimsSet.Builder bulder = new JWTClaimsSet.Builder()
                    .subject(JsonUtils.toJson(user))
                    .issuer("http://xhope.top")
                    .expirationTime(new Date(System.currentTimeMillis() + EXPIRE_TIME))
                    .claim("authorities",
                            authentication.getAuthorities().stream()
                                    .map(grantedAuthority -> grantedAuthority.getAuthority()).toArray());
            
            if (MapUtils.isNotEmpty(additionalInformation)) {
                bulder.claim("details", additionalInformation);
            }
            
            JWTClaimsSet claimsSet = bulder.build();

            /**
             * 3. 建立签名
             */
            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
            signedJWT.sign(macSigner);

            /**
             * 4. 生成token
             */
            String token = signedJWT.serialize();
            return token;
        } catch (KeyLengthException e) {
            e.printStackTrace();
        } catch (JOSEException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Authentication toAuthentication(String accessToken) throws ParseException, JOSEException, IOException {
        SignedJWT jwt = SignedJWT.parse(accessToken);

        JWSVerifier jwsVerifier = new MACVerifier(SECRET);
        if (!jwt.verify(jwsVerifier)) {
            throw new RuntimeException("无效的 token");
        }

        Payload payload = jwt.getPayload();
        JsonNode jsonNode = JsonUtils.toJsonNode(payload.toString());

        ArrayNode arrayNode = (ArrayNode) jsonNode.get("authorities");
        Iterator<JsonNode> iterator = arrayNode.iterator();
        List<GrantedAuthority> grantedAuthorities = new ArrayList(arrayNode.size());

        while (iterator.hasNext()) {
            grantedAuthorities.add(new SimpleGrantedAuthority(iterator.next().asText()));
        }

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(new AdminUserDetails(JsonUtils.toObject(jsonNode.get("sub").asText(), User.class), grantedAuthorities)
                , null, grantedAuthorities);

        JsonNode details = jsonNode.get("details");
        if (details != null) {
            authentication.setDetails(JsonUtils.toObject(details.toString(), Map.class));
        }

        //校验超时
        if (Objects.nonNull(jwt.getJWTClaimsSet().getExpirationTime()) && new Date().after(jwt.getJWTClaimsSet().getExpirationTime())) {
            // 获取后并返回新的 token
            throw new BizException(ResultUtils.fail(403, "Token 已过期", createToken(authentication)));
        }

        return authentication;
    }
}