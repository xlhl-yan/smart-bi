package com.yupi.springbootinit.manager;

import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.ThrowUtils;
import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * AiManager
 *
 * @author xlhl
 * @version 1.0
 * @description 对接第三方AI接口
 */
@Component
@Slf4j
public class AiManager {
    @Resource
    private YuCongMingClient client;

    public String doChat(Long modelId, String message) {
        DevChatRequest chatRequest = new DevChatRequest();
        chatRequest.setModelId(modelId);
        chatRequest.setMessage(message);
        BaseResponse<DevChatResponse> doChatResponse = client.doChat(chatRequest);
        ThrowUtils.throwIf(doChatResponse == null, ErrorCode.SYSTEM_ERROR, "AI 响应错误，请稍后再试");
        DevChatResponse data = doChatResponse.getData();
        ThrowUtils.throwIf(data == null, ErrorCode.SYSTEM_ERROR, "无法访问 AI，可能是剩余次数不足~");
        String content = data.getContent();
        ThrowUtils.throwIf(StringUtils.isBlank(content), ErrorCode.SYSTEM_ERROR, "无法访问 AI，可能是剩余次数不足~");

        log.info("AI 生成的原始结果是：{}", content);
        return content;
    }
}
