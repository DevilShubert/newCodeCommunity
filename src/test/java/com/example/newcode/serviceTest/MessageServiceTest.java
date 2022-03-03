package com.example.newcode.serviceTest;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.newcode.entity.Message;
import com.example.newcode.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@Slf4j
public class MessageServiceTest {

    @Autowired
    MessageService messageService;

    @Test
    public void selectConversationsTest(){
        Page<Message> messageList = messageService.selectConversations(111, 1, 20);
        for(Message message: messageList.getRecords()) {
            log.info(message.toString());
        }

        System.out.println(messageService.selectConversationCount(111));
    }

    @Test
    public void selectLettersTest(){
        Page<Message> messageLists = messageService.selectLetters("111_112", 1, 20);
        for(Message message : messageLists.getRecords()) {
            log.info(message.toString());
        }
    }

    @Test
    public void selectLetterCount(){
        System.out.println(messageService.selectLetterCount("111_112"));
    }

    @Test
    public void selectLetterUnreadCount(){
        System.out.println(messageService.selectLetterUnreadCount(111, null));
    }


    @Test
    public void updateStatusTest(){
        ArrayList<Integer> integers = new ArrayList<>();
        integers.add(7);
        messageService.updateStatus(integers, 0);
    }
}
