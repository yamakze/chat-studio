package com.wokoba.czh.api;

import com.wokoba.czh.api.dto.AiChatContextRequestDTO;
import com.wokoba.czh.api.dto.AiChatRequestDTO;
import com.wokoba.czh.api.dto.ChatResponseDTO;
import jakarta.validation.constraints.NotNull;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 聊天 API 接口.
 */
public interface IAiChatApi {


    /**
     * @param requestDTO 聊天参数
     * @return 响应体
     */
    ResponseEntity<ChatResponseDTO> aiChat(@Validated AiChatRequestDTO requestDTO);

    /**
     * 流式对话
     *
     * @param requestDTO 聊天参数
     * @return 流式响应体
     */
    ResponseEntity<Flux<ChatResponseDTO>> aiChatStream(@Validated AiChatRequestDTO requestDTO);


    ResponseEntity<String> agentChat(Long clientId,String input);
    /**
     * 装配聊天上下文
     *
     * @param requestDTO 装配参数
     * @return true
     */
    ResponseEntity<Boolean> armoryChatContext(Long clientId, @Validated List<AiChatContextRequestDTO> requestDTO);

    /**
     * 清空聊天上下文
     *
     * @param clientId 客户端Id
     * @return true
     */
    ResponseEntity<Boolean> clearChatContext(Long clientId);

}
