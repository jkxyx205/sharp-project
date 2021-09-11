package com.rick.sms.core;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 短信验证码
 * @author Rick
 * @createdAt 2021-03-03 15:30:00
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class AliSender implements Sender {

    private final IAcsClient client;

    @Override
    public void send(String phone, String signName, String templateCode, Map<String, String> params) {
        ObjectMapper mapper = new ObjectMapper();
        String paramsJson;
        try {
            paramsJson = mapper.writeValueAsString(params);
        } catch (JsonProcessingException e) {
            log.error("短信发送失败:{}", e);
            throw new RuntimeException("短信参数错误");
        }

        log.info("【发送短信】向手机{}发送信息{}", phone, params);

        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain("dysmsapi.aliyuncs.com");
        request.setSysVersion("2017-05-25");
        request.setSysAction("SendSms");
        request.putQueryParameter("RegionId", "cn-hangzhou");
        request.putQueryParameter("PhoneNumbers", phone);
        request.putQueryParameter("SignName", signName);
        request.putQueryParameter("TemplateCode", templateCode);
        request.putQueryParameter("TemplateParam", paramsJson);
        try {
            CommonResponse response = client.getCommonResponse(request);
            log.info(response.getData());
        } catch (ServerException e) {
            log.error("短信发送失败:{}", e);
            throw new RuntimeException("短信发送失败", e);
        } catch (ClientException e) {
            log.error("短信发送失败:{}", e);
            throw new RuntimeException("短信发送失败", e);
        }

    }
}
