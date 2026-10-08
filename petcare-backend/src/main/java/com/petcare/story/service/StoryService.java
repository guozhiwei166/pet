package com.petcare.story.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.petcare.common.BusinessException;
import com.petcare.common.Params;
import com.petcare.pet.mapper.PetMapper;
import com.petcare.security.SecurityUtils;
import com.petcare.story.entity.PetStory;
import com.petcare.story.mapper.PetStoryMapper;
import com.petcare.user.entity.SysUser;
import com.petcare.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 主人分享故事墙 */
@Service
@RequiredArgsConstructor
public class StoryService {

    private final PetStoryMapper storyMapper;
    private final PetMapper petMapper;
    private final SysUserMapper userMapper;

    /** 公开故事列表（审核通过） */
    public List<Map<String, Object>> listPublished() {
        return storyMapper.selectList(new LambdaQueryWrapper<PetStory>()
                        .eq(PetStory::getStatus, 1).orderByDesc(PetStory::getCreateTime).last("LIMIT 50"))
                .stream().map(this::toVO).toList();
    }

    public List<Map<String, Object>> listAll() {
        return storyMapper.selectList(new LambdaQueryWrapper<PetStory>()
                .orderByDesc(PetStory::getCreateTime)).stream().map(this::toVO).toList();
    }

    @Transactional
    public void publish(Map<String, Object> body) {
        Long uid = SecurityUtils.currentId();
        String title = Params.str(body, "title");
        String content = Params.str(body, "content");
        if (title == null) throw new BusinessException("请填写故事标题");
        if (content == null) throw new BusinessException("请写点内容");
        Long petId = Params.longOrNull(body, "petId", "宠物");
        if (petId != null) {
            var pet = petMapper.selectById(petId);
            if (pet == null || !pet.getUserId().equals(uid)) throw new BusinessException("宠物不存在");
        }
        PetStory story = new PetStory();
        story.setUserId(uid);
        story.setPetId(petId);
        story.setTitle(title);
        story.setContent(content);
        story.setPhotos(Params.str(body, "photos"));
        story.setLikes(0);
        story.setStatus(1);
        storyMapper.insert(story);
    }

    @Transactional
    public void toggleStatus(Long id, Integer status) {
        SecurityUtils.requireRole("ROLE_ADMIN");
        PetStory story = storyMapper.selectById(id);
        if (story == null) throw new BusinessException("故事不存在");
        story.setStatus(status);
        storyMapper.updateById(story);
    }

    @Transactional
    public void like(Long id) {
        storyMapper.update(null, new LambdaUpdateWrapper<PetStory>()
                .eq(PetStory::getId, id).setSql("likes = likes + 1"));
    }

    private Map<String, Object> toVO(PetStory s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", s.getId());
        m.put("title", s.getTitle());
        m.put("content", s.getContent());
        m.put("photos", s.getPhotos());
        m.put("likes", s.getLikes());
        m.put("status", s.getStatus());
        m.put("createTime", s.getCreateTime());
        SysUser u = s.getUserId() == null ? null : userMapper.selectById(s.getUserId());
        m.put("author", u == null ? "匿名主人" : u.getNickname());
        return m;
    }
}
