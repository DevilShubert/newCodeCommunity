package com.example.newcode.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.newcode.entity.Message;
import com.example.newcode.entity.MyPage;
import com.example.newcode.entity.User;
import com.example.newcode.service.MessageService;
import com.example.newcode.service.UserService;
import com.example.newcode.util.CommunityUtils;
import com.example.newcode.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
public class MessageController {

    @Autowired
    MessageService messageService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    private UserService userService;

    /**
     * 获取当前用户的会话列表
     * @param model
     * @param myPage
     * @return
     */
    @RequestMapping(path = "/letter/list", method = RequestMethod.GET)
    public String getLetterList(Model model, MyPage myPage){
        myPage.setLimit(5);
        myPage.setPath("/letter/list");

        // query all conversation counts
        int userID = hostHolder.getUser().getId();
        int conversationCount = messageService.selectConversationCount(userID);
        myPage.setRows(conversationCount);

        // query all unRead Message counts
        int allUnreadCount = messageService.selectLetterUnreadCount(userID, null);
        model.addAttribute("letterUnreadCount", allUnreadCount);

        // only store all conversation lists
        List<Map<String, Object>> conversations = new ArrayList<>();
        // query every latest private message in each conversation
        Page<Message> conversationListsPage = messageService.selectConversations(userID, myPage.getCurrent(), myPage.getLimit());
        myPage.setTotal((int)conversationListsPage.getPages());
        for (Message message : conversationListsPage.getRecords()) {
            HashMap<String, Object> hashMap = new HashMap<>();
            // store one message
            hashMap.put("conversation", message);
            // store all counts in one conversation
            hashMap.put("letterCount", messageService.selectLetterCount(message.getConversationId()));
            // store unread counts in one conversation;
            hashMap.put("unreadCount", messageService.selectLetterUnreadCount(userID, message.getConversationId()));
            // store conversation private message target
            int targetID = userID == message.getFromId() ? message.getToId() : message.getFromId();
            hashMap.put("target", userService.selectById(targetID));
            conversations.add(hashMap);
        }

        model.addAttribute("conversations", conversations);
        model.addAttribute("page", myPage);
        return "/site/letter";
    }


    /**
     * 获取某一会话详情页
     * @param conversationId 会话ID
     * @param myPage
     * @param model
     * @return
     */
    @RequestMapping(path = "/letter/detail/{conversationId}", method = RequestMethod.GET)
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, MyPage myPage, Model model) {
        myPage.setLimit(5);
        myPage.setPath("/letter/detail/" + conversationId);
        //  查询这个会话当前页面下所包含的message列表
        Page<Message> messagePage = messageService.selectLetters(conversationId, myPage.getCurrent(), myPage.getLimit());
        // 设置总行数
        myPage.setRows((int)messagePage.getTotal());
        // 设置总页数
        myPage.setTotal((int)messagePage.getPages());

        // 得到当前会话所有的私信
        List<Message> messageLists = messagePage.getRecords();
       if (messageLists != null) {
           List<Map<String, Object>> letters = new ArrayList<>();
           for(Message message : messageLists) {
               HashMap<String, Object> hashMap = new HashMap<>();
               hashMap.put("letter", message);
               hashMap.put("fromUser", userService.selectById(message.getFromId()));
               letters.add(hashMap);
           }

           // 获取当前会话中需要更新状态为已读的私信message的id
           List<Integer> updateIds = getLetterIds(messageLists);
           if (updateIds.size() != 0) {
               messageService.updateStatus(updateIds, 1);
           }

           // 会话中消息实体
           model.addAttribute("letters", letters);
       }

        // 传递消息发送方user对象
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);
        if (hostHolder.getUser().getId() == id0) {
            User user = userService.selectById(id1);
            model.addAttribute("target",user);
        } else {
            User user = userService.selectById(id0);
            model.addAttribute("target", user);
        }
        // page实体
        model.addAttribute("page", myPage);
        return "/site/letter-detail";
    }


    /**
     * 获取需要更新的messageID
     * @param letterList
     * @return
     */
    private List<Integer> getLetterIds(List<Message> letterList){
        // 这一步的逻辑是只更新接收方的ids，假设我们打开一个会话详情页之后，如果把所有的massageID都更新为已读
        // 则发送方的最新发送的message状态status也会变已读，我们只想更新接收方的message为已读
        List<Integer> ids = new ArrayList<>();
        int userID = hostHolder.getUser().getId();
        for (Message message : letterList) {
            // 当前user作为接收方
            if (message.getToId() == userID && message.getStatus() == 0) {
                ids.add(message.getId());
            }
        }
        return ids;
    }


    /**
     * 发送私信
     * @param toName
     * @param content
     * @return
     */
    @RequestMapping(path = "/letter/send", method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String toName, String content){
        List<User> selectByName = userService.selectByName(toName);
        if (StringUtils.isBlank(toName)) {
            return CommunityUtils.getJSONString(1, "目标用户不能为空!");
        }
        if (StringUtils.isBlank(content)) {
            return CommunityUtils.getJSONString(1, "文章内容不能为空!");
        }
        if (selectByName.size() == 0) {
            return CommunityUtils.getJSONString(1, "目标用户不存在");
        }
        Message message = new Message();
        // 当前user：消息发送者
        int fromUserID = hostHolder.getUser().getId();
        message.setFromId(fromUserID);
        // 目标对象：消息接受者
        int toUserID = selectByName.get(0).getId();
        message.setToId(toUserID);
        // 设置会话id
        if (message.getFromId() < message.getToId()) {
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        } else {
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }
        message.setCreateTime(new Date());
        message.setContent(content);
        messageService.insertMessage(message);

        System.out.println("====");
        return CommunityUtils.getJSONString(0);
    }

}
