package com.petcare.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcare.common.BusinessException;
import com.petcare.user.entity.SysRole;
import com.petcare.user.entity.SysUserRole;
import com.petcare.user.mapper.SysRoleMapper;
import com.petcare.user.mapper.SysUserRoleMapper;
import com.petcare.util.SpringContextHolder;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @RequirePermission 切面：按当前用户角色的 permissions 权限码做细粒度鉴权。
 * 权限码用逗号分隔存在 sys_role.permissions，支持 "*" 通配（ADMIN='*' 全放行）。
 * mapper 懒加载获取，避免切面实例化期依赖 Spring 上下文就绪。
 */
@Aspect
@Component
public class PermissionAspect {

    private SysRoleMapper roleMapper;
    private SysUserRoleMapper userRoleMapper;

    @Before("@annotation(requirePermission)")
    public void check(RequirePermission requirePermission) {
        LoginUser current = SecurityUtils.current();
        List<String> granted = permissionsOf(current.getId());
        for (String need : requirePermission.value()) {
            for (String have : granted) {
                if (match(have.trim(), need.trim())) return; // 任一命中即放行
            }
        }
        throw new BusinessException(403, "缺少操作权限：" + String.join(" / ", requirePermission.value()));
    }

    /** 当前用户所有角色的权限码集合（comma 分隔展开） */
    private List<String> permissionsOf(Long userId) {
        List<String> res = new ArrayList<>();
        List<SysUserRole> urs = userRoleMapper().selectList(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId));
        for (SysUserRole ur : urs) {
            SysRole role = roleMapper().selectById(ur.getRoleId());
            if (role != null && role.getPermissions() != null) {
                for (String p : role.getPermissions().split(",")) {
                    res.add(p.trim());
                }
            }
        }
        return res;
    }

    /** 通配匹配：granted "room:*" 可匹配 need "room:manage"；"*" 匹配一切 */
    private boolean match(String granted, String need) {
        if ("*".equals(granted)) return true;
        if (granted.equals(need)) return true;
        if (granted.endsWith(":*")) {
            String prefix = granted.substring(0, granted.length() - 2);
            return need.startsWith(prefix + ":");
        }
        return false;
    }

    private SysRoleMapper roleMapper() {
        if (roleMapper == null) roleMapper = SpringContextHolder.getBean(SysRoleMapper.class);
        return roleMapper;
    }

    private SysUserRoleMapper userRoleMapper() {
        if (userRoleMapper == null) userRoleMapper = SpringContextHolder.getBean(SysUserRoleMapper.class);
        return userRoleMapper;
    }
}
