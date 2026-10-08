package com.tourism.controller;

import com.tourism.common.BizException;
import com.tourism.common.Constants;
import com.tourism.common.PageVO;
import com.tourism.common.Result;
import com.tourism.common.ResultCode;
import com.tourism.dto.HomestayQueryDTO;
import com.tourism.dto.HomestaySaveDTO;
import com.tourism.security.LoginUser;
import com.tourism.security.Public;
import com.tourism.service.HomestayService;
import com.tourism.service.MerchantService;
import com.tourism.vo.HomestayVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 民宿接口
 * - 游客:公开浏览(仅已上架)
 * - 商家:管理本人名下民宿(商家角色)
 * - 管理员:见 AdminController(审核/监管)
 */
@RestController
@RequestMapping("/api/homestay")
@RequiredArgsConstructor
public class HomestayController {

    private final HomestayService homestayService;
    private final MerchantService merchantService;

    // ==================== 游客 / 公开 ====================

    @Public
    @GetMapping("/page")
    public Result<PageVO<HomestayVO>> page(HomestayQueryDTO query) {
        return Result.success(homestayService.page(query));
    }

    @Public
    @GetMapping("/recommend")
    public Result<List<HomestayVO>> recommend(@RequestParam(defaultValue = "6") int limit,
                                              @RequestParam(required = false) Long destinationId) {
        return Result.success(homestayService.recommendList(limit, destinationId));
    }

    /** 民宿详情:仅已上架对游客可见;未上架仅本人(商家)或管理员可见 */
    @Public
    @GetMapping("/{id}")
    public Result<HomestayVO> detail(@PathVariable Long id) {
        HomestayVO vo = homestayService.detail(id);
        boolean online = Constants.AUDIT_APPROVED.equals(vo.getAuditStatus())
                && Constants.BIZ_OPEN.equals(vo.getBusinessStatus());
        if (online) {
            // 公开浏览计数,驱动热门排序与推荐"热门程度"因子
            homestayService.incrView(id);
        } else {
            String role = LoginUser.role();
            boolean admin = Constants.ROLE_ADMIN.equals(role);
            boolean merchantOwner = Constants.ROLE_MERCHANT.equals(role)
                    && vo.getMerchantId() != null
                    && vo.getMerchantId().equals(merchantService.currentMerchant().getId());
            if (!admin && !merchantOwner) {
                throw new BizException(ResultCode.NOT_FOUND, "民宿不存在或未上架");
            }
        }
        return Result.success(vo);
    }

    // ==================== 商家管理 ====================

    /** 我的民宿列表 */
    @GetMapping("/merchant/list")
    public Result<List<HomestayVO>> myList() {
        LoginUser.requireRole("MERCHANT");
        return Result.success(homestayService.listByMerchant(merchantService.currentMerchant().getId()));
    }

    /** 新增民宿 */
    @PostMapping("/merchant")
    public Result<HomestayVO> create(@RequestBody HomestaySaveDTO dto) {
        LoginUser.requireRole("MERCHANT");
        return Result.success(homestayService.create(dto, merchantService.currentMerchant().getId()));
    }

    /** 编辑民宿 */
    @PutMapping("/merchant")
    public Result<HomestayVO> update(@RequestBody HomestaySaveDTO dto) {
        LoginUser.requireRole("MERCHANT");
        return Result.success(homestayService.update(dto, merchantService.currentMerchant().getId()));
    }

    /** 下架 / 重新提交审核 */
    @PutMapping("/merchant/{id}/status")
    public Result<Void> changeStatus(@PathVariable Long id, @RequestParam String status) {
        LoginUser.requireRole("MERCHANT");
        homestayService.changeStatus(id, merchantService.currentMerchant().getId(), status);
        return Result.success();
    }

    /** 删除民宿(无订单时) */
    @DeleteMapping("/merchant/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        LoginUser.requireRole("MERCHANT");
        homestayService.delete(id, merchantService.currentMerchant().getId());
        return Result.success();
    }
}
