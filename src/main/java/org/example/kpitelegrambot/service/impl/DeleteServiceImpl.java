package org.example.kpitelegrambot.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.kpitelegrambot.entity.MessageID;
import org.example.kpitelegrambot.entity.repository.MessageIDRepository;
import org.example.kpitelegrambot.service.DeleteService;
import org.jvnet.hk2.annotations.Service;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;

import java.util.Optional;


@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DeleteServiceImpl implements DeleteService {
    MessageIDRepository idRepository;

    @Override
    public DeleteMessage deleteLastBotsMessage(Long chatId, Integer messageId) {
        return null;
    }

    @Override
    public void saveMessageId(int chatId, int id) {

        Optional<MessageID> messageID1= idRepository.findById(Math.toIntExact(chatId));
        MessageID messageID;
        if(messageID1.isPresent()) {
            messageID = messageID1.get();
            messageID.setMessageID(id);
        }else{
            messageID = new MessageID(chatId, id);
        }
        idRepository.save(messageID);
    }

    @Override
    public int getLastMessageId(int chatId) {
        Optional<MessageID> messageID= idRepository.findById(Math.toIntExact(chatId));
       if(messageID.isPresent()){
           return messageID.get().getMessageID();
       }else{
           return 0;
       }
    }
}
