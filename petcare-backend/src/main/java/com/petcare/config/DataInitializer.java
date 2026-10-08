package com.petcare.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcare.employee.entity.Employee;
import com.petcare.employee.mapper.EmployeeMapper;
import com.petcare.user.entity.SysRole;
import com.petcare.user.entity.SysUser;
import com.petcare.user.entity.SysUserRole;
import com.petcare.user.mapper.SysRoleMapper;
import com.petcare.user.mapper.SysUserMapper;
import com.petcare.user.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

/**
 * 首次启动时创建演示账号（密码 BCrypt 运行时生成）：
 * admin / admin123（管理员）、staff / staff123（员工 李晓宇）、user / user123（用户 小明）
 */
@Slf4j
@Component
@Order(10)
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final EmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userMapper.selectCount(null) > 0) return;
        createUser("admin", "admin123", "平台管理员", "13800000000", "ROLE_ADMIN", null);
        createUser("staff", "staff123", "李晓宇", "13900000001", "ROLE_STAFF",
                Map.of("empNo", "EMP001", "position", "宠物护理师", "area", "寄养区A"));
        createUser("staff2", "staff123", "王芳", "13900000002", "ROLE_STAFF",
                Map.of("empNo", "EMP002", "position", "高级宠物护理师", "area", "寄养区B"));
        createUser("staff3", "staff123", "陈浩", "13900000003", "ROLE_STAFF",
                Map.of("empNo", "EMP003", "position", "宠物驯养师", "area", "活动区"));
        createUser("user", "user123", "小明", "13800000001", "ROLE_USER", null);
        log.info("演示账号初始化完成：admin/admin123, staff/staff123, staff2+staff3, user/user123");
    }

    private void createUser(String username, String password, String nickname, String phone,
                            String roleCode, Map<String, String> employeeInfo) {
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname);
        user.setStatus(1);
        userMapper.insert(user);

        SysRole role = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getCode, roleCode));
        SysUserRole ur = new SysUserRole();
        ur.setUserId(user.getId());
        ur.setRoleId(role.getId());
        userRoleMapper.insert(ur);

        if (employeeInfo != null) {
            Employee emp = new Employee();
            emp.setUserId(user.getId());
            emp.setEmpNo(employeeInfo.get("empNo"));
            emp.setRealName(nickname);
            emp.setPhone(phone);
            emp.setPosition(employeeInfo.get("position"));
            emp.setArea(employeeInfo.get("area"));
            emp.setStatus(1);
            emp.setHireDate(LocalDate.now().minusDays(365));
            employeeMapper.insert(emp);
        }
    }
}
