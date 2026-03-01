package com.fruitveg.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fruitveg.common.Result;
import com.fruitveg.entity.SysUser;
import com.fruitveg.mapper.SysUserMapper;
import com.fruitveg.service.MockDataService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final SysUserMapper sysUserMapper;
    private final MockDataService mockDataService;

    public AdminController(SysUserMapper sysUserMapper, MockDataService mockDataService) {
        this.sysUserMapper = sysUserMapper;
        this.mockDataService = mockDataService;
    }

    @GetMapping("/users")
    public Result<Map<String, Object>> getUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize
    ) {
        LambdaQueryWrapper<SysUser> qw = new LambdaQueryWrapper<>();
        if (username != null && !username.isEmpty()) qw.like(SysUser::getUsername, username);
        if (phone != null && !phone.isEmpty()) qw.like(SysUser::getPhone, phone);
        if (status != null) qw.eq(SysUser::getStatus, status);

        List<SysUser> users = sysUserMapper.selectList(qw);
        List<Map<String, Object>> rows = users.stream().map(u -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", u.getId());
            item.put("username", u.getUsername());
            item.put("nickname", u.getNickname());
            item.put("phone", u.getPhone());
            item.put("gender", u.getGender());
            item.put("status", u.getStatus());
            item.put("createTime", u.getCreateTime());
            return item;
        }).collect(Collectors.toList());

        return Result.success(page(rows, page, pageSize));
    }

    @GetMapping("/users/{id}")
    public Result<Map<String, Object>> getUserDetail(@PathVariable Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", user.getId());
        item.put("username", user.getUsername());
        item.put("nickname", user.getNickname());
        item.put("phone", user.getPhone());
        item.put("gender", user.getGender());
        item.put("avatar", user.getAvatar());
        item.put("role", "admin".equalsIgnoreCase(user.getUsername()) ? "ADMIN" : "USER");
        item.put("status", user.getStatus());
        item.put("createTime", user.getCreateTime());
        item.put("updateTime", user.getUpdateTime());
        return Result.success(item);
    }

    @GetMapping("/users/export")
    public Result<String> exportUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Integer status
    ) {
        LambdaQueryWrapper<SysUser> qw = new LambdaQueryWrapper<>();
        if (username != null && !username.isEmpty()) qw.like(SysUser::getUsername, username);
        if (phone != null && !phone.isEmpty()) qw.like(SysUser::getPhone, phone);
        if (status != null) qw.eq(SysUser::getStatus, status);

        List<SysUser> users = sysUserMapper.selectList(qw);
        StringBuilder csv = new StringBuilder("id,username,nickname,phone,gender,status,createTime\n");
        for (SysUser u : users) {
            csv.append(u.getId()).append(',')
                    .append(escapeCsv(u.getUsername())).append(',')
                    .append(escapeCsv(u.getNickname())).append(',')
                    .append(escapeCsv(u.getPhone())).append(',')
                    .append(u.getGender()).append(',')
                    .append(u.getStatus()).append(',')
                    .append(u.getCreateTime())
                    .append('\n');
        }
        return Result.success(csv.toString());
    }

    @PutMapping("/users/{id}/status")
    public Result<Void> toggleUserStatus(@PathVariable Long id, @RequestBody Map<String, Integer> payload) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        user.setStatus(payload.getOrDefault("status", 1));
        sysUserMapper.updateById(user);
        return Result.success();
    }

    @GetMapping("/products")
    public Result<Map<String, Object>> getProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer auditStatus,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize
    ) {
        return Result.success(mockDataService.listAdminProducts(page, pageSize, name, categoryId, auditStatus));
    }

    @GetMapping("/products/{id}")
    public Result<Map<String, Object>> getProductDetail(@PathVariable Long id) {
        Map<String, Object> row = mockDataService.getAdminProductDetail(id);
        return row == null ? Result.error(404, "商品不存在") : Result.success(row);
    }

    @PutMapping("/products/{id}/audit")
    public Result<Void> auditProduct(@PathVariable Long id, @RequestBody Map<String, Integer> payload) {
        return mockDataService.auditProduct(id, payload.getOrDefault("status", 1)) ? Result.success() : Result.error(404, "商品不存在");
    }

    @GetMapping("/orders")
    public Result<Map<String, Object>> getOrders(
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize
    ) {
        return Result.success(mockDataService.listAdminOrders(page, pageSize, orderNo, phone, status));
    }

    @GetMapping("/orders/{id}")
    public Result<Map<String, Object>> getOrderDetail(@PathVariable Long id) {
        Map<String, Object> row = mockDataService.getAdminOrderDetail(id);
        return row == null ? Result.error(404, "订单不存在") : Result.success(row);
    }

    @PutMapping("/orders/{id}/ship")
    public Result<Void> shipOrder(@PathVariable Long id, @RequestBody(required = false) Map<String, String> payload) {
        String logisticsCompany = payload == null ? null : payload.get("logisticsCompany");
        String logisticsNo = payload == null ? null : payload.get("logisticsNo");
        return mockDataService.shipOrder(id, logisticsCompany, logisticsNo) ? Result.success() : Result.error(404, "订单不存在");
    }

    @GetMapping("/complaints")
    public Result<Map<String, Object>> getComplaints(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize
    ) {
        return Result.success(mockDataService.listComplaints(page, pageSize, status, orderNo));
    }

    @PutMapping("/complaints/{id}/handle")
    public Result<Void> handleComplaint(@PathVariable Long id, @RequestBody(required = false) Map<String, String> payload) {
        String result = payload == null ? null : payload.get("result");
        String remark = payload == null ? null : payload.get("remark");
        return mockDataService.handleComplaint(id, result, remark) ? Result.success() : Result.error(404, "投诉单不存在");
    }

    @GetMapping("/merchants")
    public Result<Map<String, Object>> getMerchants(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String contactName,
            @RequestParam(required = false) String contactPhone,
            @RequestParam(required = false) Integer auditStatus,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize
    ) {
        return Result.success(mockDataService.listMerchants(page, pageSize, name, contactName, contactPhone, auditStatus));
    }

    @GetMapping("/merchants/{id}")
    public Result<Map<String, Object>> getMerchantDetail(@PathVariable Long id) {
        Map<String, Object> row = mockDataService.getMerchantDetail(id);
        return row == null ? Result.error(404, "商家不存在") : Result.success(row);
    }

    @PutMapping("/merchants/{id}/audit")
    public Result<Void> auditMerchant(@PathVariable Long id, @RequestBody Map<String, Integer> payload) {
        return mockDataService.auditMerchant(id, payload.getOrDefault("status", 1)) ? Result.success() : Result.error(404, "商家不存在");
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        int userCount = sysUserMapper.selectCount(null).intValue();
        return Result.success(mockDataService.getStats(userCount));
    }

    @GetMapping("/content/banners")
    public Result<Map<String, Object>> adminBanners(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String title
    ) {
        return Result.success(mockDataService.listAdminBanners(page, pageSize, title));
    }

    @PostMapping("/content/banner")
    public Result<Map<String, Object>> saveBanner(@RequestBody Map<String, Object> payload) {
        return Result.success(mockDataService.saveBanner(payload));
    }

    @DeleteMapping("/content/banner/{id}")
    public Result<Void> deleteBanner(@PathVariable Long id) {
        return mockDataService.deleteBanner(id) ? Result.success() : Result.error(404, "轮播图不存在");
    }

    @GetMapping("/content/notices")
    public Result<Map<String, Object>> adminNotices(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String title
    ) {
        return Result.success(mockDataService.listAdminNotices(page, pageSize, type, title));
    }

    @PostMapping("/content/notice")
    public Result<Map<String, Object>> saveNotice(@RequestBody Map<String, Object> payload) {
        return Result.success(mockDataService.saveNotice(payload));
    }

    @DeleteMapping("/content/notice/{id}")
    public Result<Void> deleteNotice(@PathVariable Long id) {
        return mockDataService.deleteNotice(id) ? Result.success() : Result.error(404, "公告不存在");
    }

    @GetMapping("/settings")
    public Result<Map<String, Object>> getSettings() {
        return Result.success(mockDataService.getSystemSettings());
    }

    @PutMapping("/settings")
    public Result<Map<String, Object>> updateSettings(@RequestBody(required = false) Map<String, Object> payload) {
        return Result.success(mockDataService.updateSystemSettings(payload));
    }

    @GetMapping("/roles")
    public Result<List<Map<String, Object>>> roles() {
        return Result.success(mockDataService.listRoles());
    }

    @PutMapping("/roles/{role}/permissions")
    public Result<Map<String, Object>> updateRolePermissions(@PathVariable String role, @RequestBody(required = false) Map<String, Object> payload) {
        @SuppressWarnings("unchecked")
        List<String> permissions = payload == null ? Collections.emptyList() : (List<String>) payload.getOrDefault("permissions", Collections.emptyList());
        Map<String, Object> row = mockDataService.updateRolePermissions(role.toUpperCase(), permissions);
        return row == null ? Result.error(404, "角色不存在") : Result.success(row);
    }

    @GetMapping("/traces/templates")
    public Result<Map<String, Object>> traceTemplates() {
        return Result.success(mockDataService.listAdminTraceTemplates());
    }

    @PostMapping("/traces/template")
    public Result<Map<String, Object>> saveTraceTemplate(@RequestBody(required = false) Map<String, Object> payload) {
        return Result.success(mockDataService.saveTraceTemplate(payload == null ? Collections.emptyMap() : payload));
    }

    @DeleteMapping("/traces/template/{id}")
    public Result<Void> deleteTraceTemplate(@PathVariable Long id) {
        return mockDataService.deleteTraceTemplate(id) ? Result.success() : Result.error(404, "溯源模板不存在");
    }

    @GetMapping("/traces")
    public Result<Map<String, Object>> getTraceList(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) Integer auditStatus,
            @RequestParam(required = false) String keyword
    ) {
        return Result.success(mockDataService.listAdminTraces(page, pageSize, auditStatus, keyword));
    }

    @PutMapping("/traces/{productId}/audit")
    public Result<Void> auditTrace(@PathVariable Long productId, @RequestBody(required = false) Map<String, Object> payload) {
        int status = payload == null ? 1 : ((Number) payload.getOrDefault("status", 1)).intValue();
        String remark = payload == null ? "" : String.valueOf(payload.getOrDefault("remark", ""));
        return mockDataService.auditTrace(productId, status, remark) ? Result.success() : Result.error(404, "溯源信息不存在");
    }

    @GetMapping("/traces/stats")
    public Result<Map<String, Object>> traceStats() {
        return Result.success(mockDataService.getTraceStats());
    }

    private String escapeCsv(Object value) {
        if (value == null) {
            return "";
        }
        String s = String.valueOf(value).replace("\"", "\"\"");
        return "\"" + s + "\"";
    }

    private Map<String, Object> page(List<Map<String, Object>> rows, Integer page, Integer pageSize) {
        int p = page == null || page < 1 ? 1 : page;
        int s = pageSize == null || pageSize < 1 ? 10 : pageSize;
        int from = Math.min((p - 1) * s, rows.size());
        int to = Math.min(from + s, rows.size());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("records", rows.subList(from, to));
        result.put("total", rows.size());
        result.put("current", p);
        result.put("size", s);
        return result;
    }
}
