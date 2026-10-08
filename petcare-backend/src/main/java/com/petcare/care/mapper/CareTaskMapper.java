package com.petcare.care.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcare.care.entity.CareTask;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface CareTaskMapper extends BaseMapper<CareTask> {

    /** 各员工任务量与完成量（DONE=已完成），由数据库聚合避免全表加载（表无逻辑删除列） */
    @Select("""
            SELECT staff_id                                      AS staffId,
                   COUNT(*)                                     AS total,
                   SUM(CASE WHEN status = 'DONE' THEN 1 ELSE 0 END) AS done
            FROM care_task
            WHERE staff_id IS NOT NULL
            GROUP BY staff_id
            """)
    List<Map<String, Object>> countGroupByStaff();
}
