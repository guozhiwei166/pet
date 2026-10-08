package com.tourism.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 统一分页返回
 */
@Data
public class PageVO<T> implements Serializable {

    private List<T> records;
    private long total;
    private long current;
    private long size;
    private long pages;

    public static <T> PageVO<T> of(IPage<T> page) {
        PageVO<T> vo = new PageVO<>();
        vo.records = page.getRecords();
        vo.total = page.getTotal();
        vo.current = page.getCurrent();
        vo.size = page.getSize();
        vo.pages = page.getPages();
        return vo;
    }
}
