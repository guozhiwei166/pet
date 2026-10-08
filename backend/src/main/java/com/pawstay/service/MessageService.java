package com.pawstay.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.pawstay.entity.Message;
import com.pawstay.mapper.MessageMapper;
import com.pawstay.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageMapper messageMapper;

    /** 站内消息：下单、接单、日记更新等关键节点触达用户 */
    public void send(Long userId, String title, String content, String type, Long refId) {
        if (userId == null) return;
        Message m = new Message();
        m.setUserId(userId);
        m.setTitle(title);
        m.setContent(content);
        m.setType(type);
        m.setRefId(refId);
        m.setIsRead(0);
        m.setCreateTime(LocalDateTime.now());
        messageMapper.insert(m);
    }

    public List<Message> myMessages() {
        return messageMapper.selectList(new LambdaQueryWrapper<Message>()
                .eq(Message::getUserId, UserContext.requireLogin().getId())
                .orderByDesc(Message::getId)
                .last("LIMIT 50"));
    }

    public long unreadCount() {
        return messageMapper.selectCount(new LambdaQueryWrapper<Message>()
                .eq(Message::getUserId, UserContext.requireLogin().getId())
                .eq(Message::getIsRead, 0));
    }

    public void read(Long id) {
        Message m = messageMapper.selectById(id);
        if (m != null && m.getUserId().equals(UserContext.requireLogin().getId())) {
            m.setIsRead(1);
            messageMapper.updateById(m);
        }
    }

    public void readAll() {
        messageMapper.update(null, new LambdaUpdateWrapper<Message>()
                .eq(Message::getUserId, UserContext.requireLogin().getId())
                .set(Message::getIsRead, 1));
    }
}
