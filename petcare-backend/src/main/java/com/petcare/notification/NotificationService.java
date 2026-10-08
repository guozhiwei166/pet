package com.petcare.notification;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.petcare.notification.entity.Notification;
import com.petcare.notification.mapper.NotificationMapper;
import com.petcare.user.entity.SysUser;
import com.petcare.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

/**
 * 通知服务：站内通知落库 + Redis 未读数缓存（带降级）
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final String UNREAD_PREFIX = "notify:unread:";

    private final NotificationMapper notificationMapper;
    private final SysUserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public void send(Long userId, String title, String content, String type, String refType, Long refId) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setTitle(title);
        n.setContent(content);
        n.setType(type);
        n.setIsRead(0);
        n.setRefType(refType);
        n.setRefId(refId);
        notificationMapper.insert(n);
        // 未读数缓存失效（下次查询重建）；Redis 不可用仅降级，不影响通知落库
        try { redisTemplate.delete(UNREAD_PREFIX + userId); } catch (Exception ignored) { }
    }

    /** 通知某角色的全部用户（如通知管理员） */
    public void sendToRole(String roleCode, String title, String content, String type, String refType, Long refId) {
        List<SysUser> users = userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getStatus, 1));
        for (SysUser u : users) {
            if (hasRole(u.getId(), roleCode)) {
                send(u.getId(), title, content, type, refType, refId);
            }
        }
    }

    private boolean hasRole(Long userId, String roleCode) {
        // 轻量实现：仅查 sys_user_role + sys_role（角色数量少，可接受）
        List<com.petcare.user.entity.SysUserRole> urs = userRoleMapper()
                .selectList(new LambdaQueryWrapper<com.petcare.user.entity.SysUserRole>()
                        .eq(com.petcare.user.entity.SysUserRole::getUserId, userId));
        for (com.petcare.user.entity.SysUserRole ur : urs) {
            com.petcare.user.entity.SysRole role = roleMapper().selectById(ur.getRoleId());
            if (role != null && roleCode.equals(role.getCode())) return true;
        }
        return false;
    }

    // 懒加载依赖（避免构造器循环依赖风险）
    private com.petcare.user.mapper.SysUserRoleMapper userRoleMapper;
    private com.petcare.user.mapper.SysRoleMapper roleMapper;

    private com.petcare.user.mapper.SysUserRoleMapper userRoleMapper() {
        if (userRoleMapper == null) userRoleMapper = com.petcare.util.SpringContextHolder.getBean(com.petcare.user.mapper.SysUserRoleMapper.class);
        return userRoleMapper;
    }

    private com.petcare.user.mapper.SysRoleMapper roleMapper() {
        if (roleMapper == null) roleMapper = com.petcare.util.SpringContextHolder.getBean(com.petcare.user.mapper.SysRoleMapper.class);
        return roleMapper;
    }

    public List<Notification> list(Long userId, boolean unreadOnly) {
        LambdaQueryWrapper<Notification> w = new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getId)
                .last("LIMIT 50");
        if (unreadOnly) w.eq(Notification::getIsRead, 0);
        return notificationMapper.selectList(w);
    }

    /** 未读数：Redis 缓存 60 秒，miss 回源 */
    public long unreadCount(Long userId) {
        try {
            Object cached = redisTemplate.opsForValue().get(UNREAD_PREFIX + userId);
            if (cached != null) return Long.parseLong(cached.toString());
        } catch (Exception ignored) {
            // Redis 不可用时降级直查数据库
        }
        long count = notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId).eq(Notification::getIsRead, 0));
        try {
            redisTemplate.opsForValue().set(UNREAD_PREFIX + userId, count, Duration.ofSeconds(60));
        } catch (Exception ignored) {
        }
        return count;
    }

    public void read(Long userId, Long id) {
        notificationMapper.update(null, new LambdaUpdateWrapper<Notification>()
                .eq(Notification::getId, id).eq(Notification::getUserId, userId)
                .set(Notification::getIsRead, 1));
        try { redisTemplate.delete(UNREAD_PREFIX + userId); } catch (Exception ignored) { }
    }

    public void readAll(Long userId) {
        notificationMapper.update(null, new LambdaUpdateWrapper<Notification>()
                .eq(Notification::getUserId, userId).set(Notification::getIsRead, 1));
        try { redisTemplate.delete(UNREAD_PREFIX + userId); } catch (Exception ignored) { }
    }
}
