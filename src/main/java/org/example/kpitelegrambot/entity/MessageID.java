package org.example.kpitelegrambot.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Entity
@Table(name="last_message_id")
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MessageID {

    @Id
    @Column(name = "chat_id")
    int chatId;

    @Column(name = "message_id")
    int messageID;

}
