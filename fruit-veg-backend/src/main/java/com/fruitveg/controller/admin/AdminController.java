package com.fruitveg.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fruitveg.common.Result;
import com.fruitveg.entity.BizMerchant;
import com.fruitveg.entity.SysUser;
import com.fruitveg.mapper.BizMerchantMapper;
import com.fruitveg.mapper.SysUserMapper;
import com.fruitveg.service.RuntimeDataService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasAnyRole('ADMIN','SUB_ADMIN')")
public class AdminController {

    private final SysUserMapper sysUserMapper;
    private final BizMerchantMapper bizMerchantMapper;
    private final PasswordEncoder passwordEncoder;
    private final RuntimeDataService mockDataService;

    public AdminController(SysUserMapper sysUserMapper,
                           BizMerchantMapper bizMerchantMapper,
                           PasswordEncoder passwordEncoder,
                           RuntimeDataService mockDataService) {
        this.sysUserMapper = sysUserMapper;
        this.bizMerchantMapper = bizMerchantMapper;
        this.passwordEncoder = passwordEncoder;
        this.mockDataService = mockDataService;
    }

    @GetMapping("/users")
        @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> getUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize
    ) {
        LambdaQueryWrapper<SysUser> qw = new LambdaQueryWrapper<>();
        if (username != null && !username.isEmpty()) qw.like(SysUser::getUsername, username);
        if (phone != null && !phone.isEmpty()) qw.like(SysUser::getPhone, phone);
        if (status != null) qw.eq(SysUser::getStatus, status);

        List<SysUser> users = sysUserMapper.selectList(qw);
        String roleFilter = role == null ? null : role.trim().toUpperCase();
        List<Map<String, Object>> rows = users.stream()
                .map(this::toUserRow)
                .filter(item -> roleFilter == null || roleFilter.isEmpty() || roleFilter.equals(item.get("role")))
                .collect(Collectors.toList());

        return Result.success(page(rows, page, pageSize));
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
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
        item.put("role", resolveRole(user));
        item.put("status", user.getStatus());
        item.put("createTime", user.getCreateTime());
        item.put("updateTime", user.getUpdateTime());
        return Result.success(item);
    }

    @GetMapping("/users/export")
    @PreAuthorize("hasRole('ADMIN')")
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
        StringBuilder csv = new StringBuilder("id,username,nickname,phone,role,gender,status,createTime\n");
        for (SysUser u : users) {
            csv.append(u.getId()).append(',')
                    .append(escapeCsv(u.getUsername())).append(',')
                    .append(escapeCsv(u.getNickname())).append(',')
                    .append(escapeCsv(u.getPhone())).append(',')
                    .append(escapeCsv(resolveRole(u))).append(',')
                    .append(u.getGender()).append(',')
                    .append(u.getStatus()).append(',')
                    .append(u.getCreateTime())
                    .append('\n');
        }
        return Result.success(csv.toString());
    }

    @PutMapping("/users/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> toggleUserStatus(@PathVariable Long id, @RequestBody Map<String, Integer> payload) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        if ("ADMIN".equals(resolveRole(user))) {
            return Result.error(400, "总管理员账号不可禁用");
        }
        user.setStatus(payload.getOrDefault("status", 1));
        sysUserMapper.updateById(user);
        return Result.success();
    }

    @PostMapping("/sub-admins")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> createSubAdmin(@RequestBody Map<String, String> payload) {
        String username = Optional.ofNullable(payload.get("username")).orElse("").trim();
        String phone = Optional.ofNullable(payload.get("phone")).orElse("").trim();
        String password = Optional.ofNullable(payload.get("password")).orElse("").trim();
        String nickname = Optional.ofNullable(payload.get("nickname")).orElse("").trim();

        if (username.isEmpty() || phone.isEmpty() || password.length() < 6) {
            return Result.error(400, "请填写完整信息，且密码至少6位");
        }

        LambdaQueryWrapper<SysUser> existsQw = new LambdaQueryWrapper<>();
        existsQw.eq(SysUser::getUsername, username).or().eq(SysUser::getPhone, phone);
        if (sysUserMapper.selectCount(existsQw) > 0) {
            return Result.error(400, "用户名或手机号已存在");
        }

        SysUser sysUser = new SysUser();
        sysUser.setUsername(username);
        sysUser.setPhone(phone);
        sysUser.setPassword(passwordEncoder.encode(password));
        sysUser.setNickname(nickname.isEmpty() ? "子管理员" : nickname);
        sysUser.setGender(0);
        sysUser.setStatus(1);
        sysUser.setRole("SUB_ADMIN");
        sysUser.setCreateTime(new Date());
        sysUser.setUpdateTime(new Date());
        sysUserMapper.insert(sysUser);

        return Result.success(toUserRow(sysUser));
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
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> getOrderDetail(@PathVariable Long id) {
        Map<String, Object> row = mockDataService.getAdminOrderDetail(id);
        return row == null ? Result.error(404, "订单不存在") : Result.success(row);
    }

    @PutMapping("/orders/{id}/ship")
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> getStats() {
        int userCount = sysUserMapper.selectCount(null).intValue();
        return Result.success(mockDataService.getStats(userCount));
    }

    @GetMapping("/content/banners")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> adminBanners(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String title
    ) {
        return Result.success(mockDataService.listAdminBanners(page, pageSize, title));
    }

    @PostMapping("/content/banner")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> saveBanner(@RequestBody Map<String, Object> payload) {
        return Result.success(mockDataService.saveBanner(payload));
    }

    @DeleteMapping("/content/banner/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteBanner(@PathVariable Long id) {
        return mockDataService.deleteBanner(id) ? Result.success() : Result.error(404, "轮播图不存在");
    }

    @GetMapping("/content/notices")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> adminNotices(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String title
    ) {
        return Result.success(mockDataService.listAdminNotices(page, pageSize, type, title));
    }

    @PostMapping("/content/notice")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> saveNotice(@RequestBody Map<String, Object> payload) {
        return Result.success(mockDataService.saveNotice(payload));
    }

    @DeleteMapping("/content/notice/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteNotice(@PathVariable Long id) {
        return mockDataService.deleteNotice(id) ? Result.success() : Result.error(404, "公告不存在");
    }

    @GetMapping("/circle/comments")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> listCircleComments(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) Integer auditStatus,
            @RequestParam(required = false) String keyword
    ) {
        return Result.success(mockDataService.listAdminCircleComments(page, pageSize, auditStatus, keyword));
    }

    @PutMapping("/circle/comments/{postId}/{commentId}/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> auditCircleComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestBody(required = false) Map<String, Object> payload
    ) {
        int status = payload == null ? 1 : ((Number) payload.getOrDefault("status", 1)).intValue();
        String remark = payload == null ? "" : String.valueOf(payload.getOrDefault("remark", ""));
        return mockDataService.auditCircleComment(postId, commentId, status, remark)
                ? Result.success()
                : Result.error(404, "评论不存在或参数不合法");
    }

    @GetMapping("/settings")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> getSettings() {
        return Result.success(mockDataService.getSystemSettings());
    }

    @PutMapping("/settings")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> updateSettings(@RequestBody(required = false) Map<String, Object> payload) {
        return Result.success(mockDataService.updateSystemSettings(payload));
    }

    @GetMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<Map<String, Object>>> roles() {
        return Result.success(mockDataService.listRoles());
    }

    @PutMapping("/roles/{role}/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> updateRolePermissions(@PathVariable String role, @RequestBody(required = false) Map<String, Object> payload) {
        @SuppressWarnings("unchecked")
        List<String> permissions = payload == null ? Collections.emptyList() : (List<String>) payload.getOrDefault("permissions", Collections.emptyList());
        Map<String, Object> row = mockDataService.updateRolePermissions(role.toUpperCase(), permissions);
        return row == null ? Result.error(404, "角色不存在") : Result.success(row);
    }

    @GetMapping("/traces/templates")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> traceTemplates() {
        return Result.success(mockDataService.listAdminTraceTemplates());
    }

    @PostMapping("/traces/template")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> saveTraceTemplate(@RequestBody(required = false) Map<String, Object> payload) {
        return Result.success(mockDataService.saveTraceTemplate(payload == null ? Collections.emptyMap() : payload));
    }

    @DeleteMapping("/traces/template/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteTraceTemplate(@PathVariable Long id) {
        return mockDataService.deleteTraceTemplate(id) ? Result.success() : Result.error(404, "溯源模板不存在");
    }

    @GetMapping("/traces")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> getTraceList(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) Integer auditStatus,
            @RequestParam(required = false) String keyword
    ) {
        return Result.success(mockDataService.listAdminTraces(page, pageSize, auditStatus, keyword));
    }

    @PutMapping("/traces/{productId}/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> auditTrace(@PathVariable Long productId, @RequestBody(required = false) Map<String, Object> payload) {
        int status = payload == null ? 1 : ((Number) payload.getOrDefault("status", 1)).intValue();
        String remark = payload == null ? "" : String.valueOf(payload.getOrDefault("remark", ""));
        return mockDataService.auditTrace(productId, status, remark) ? Result.success() : Result.error(404, "溯源信息不存在");
    }

    @GetMapping("/traces/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> traceStats() {
        return Result.success(mockDataService.getTraceStats());
    }

    private Map<String, Object> toUserRow(SysUser u) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", u.getId());
        item.put("username", u.getUsername());
        item.put("nickname", u.getNickname());
        item.put("phone", u.getPhone());
        item.put("gender", u.getGender());
        item.put("role", resolveRole(u));
        item.put("status", u.getStatus());
        item.put("createTime", u.getCreateTime());
        item.put("updateTime", u.getUpdateTime());
        return item;
    }

    private String resolveRole(SysUser user) {
        String role = user.getRole();
        if (role != null && !role.trim().isEmpty()) {
            return role.trim().toUpperCase();
        }
        if ("admin".equalsIgnoreCase(user.getUsername()) || "13800138000".equals(user.getPhone())) {
            return "ADMIN";
        }
        BizMerchant merchant = bizMerchantMapper.selectByUserId(user.getId());
        if (merchant != null && merchant.getStatus() != null && merchant.getStatus() == 1) {
            return "MERCHANT";
        }
        return "USER";
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
