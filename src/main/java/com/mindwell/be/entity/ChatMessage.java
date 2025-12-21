package com.mindwell.be.entity;

import com.mindwell.be.entity.enums.SenderType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer messageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    private Integer senderId;

    @Enumerated(EnumType.STRING)
    private SenderType senderType;

    @Column(columnDefinition = "text")
    private String messageContent;

    private LocalDateTime sentAt;
}
