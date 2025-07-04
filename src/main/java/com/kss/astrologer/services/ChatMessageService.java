package com.kss.astrologer.services;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.kss.astrologer.dto.ChatMessageDto;
import com.kss.astrologer.models.ChatMessage;
import com.kss.astrologer.repository.ChatMessageRepository;

@Service
public class ChatMessageService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    public ChatMessage save(ChatMessage chatMessage) {
        return chatMessageRepository.save(chatMessage);
    }

    public Page<ChatMessageDto> getMessages(UUID sessionId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page-1, size, Direction.DESC, "createdAt");
        Page<ChatMessage> messages = chatMessageRepository.findBySessionId(sessionId, pageable);
        return messages.map(ChatMessageDto::new);
    }
}
