package com.fruitveg.controller.merchant;

import com.fruitveg.common.Result;
import com.fruitveg.service.RuntimeDataService;
import com.fruitveg.utils.JwtUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/merchant")
@PreAuthorize("hasAnyRole('USER','MERCHANT','ADMIN')")
public class MerchantController {

    private final RuntimeDataService mockDataService;
    private final JwtUtils jwtUtils;

    public MerchantController(RuntimeDataService mockDataService, JwtUtils jwtUtils) {
        this.mockDataService = mockDataService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/apply")
    public Result<Map<String, Object>> apply(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
        return Result.success(mockDataService.applyMerchant(getUserId(request), payload));
    }

    @GetMapping("/info")
    public Result<Map<String, Object>> getInfo(HttpServletRequest request) {
        Map<String, Object> info = mockDataService.getMerchantInfo(getUserId(request));
        if (info == null) {
            return Result.error(404, "您还不是商家，请先申请入驻");
        }
        return Result.success(info);
    }

    @PutMapping("/info")
    public Result<Map<String, Object>> updateInfo(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
        Map<String, Object> info = mockDataService.updateMerchantInfo(getUserId(request), payload);
        if (info == null) {
            return Result.error(403, "您不是商家，无权编辑店铺信息");
        }
        return Result.success(info);
    }

    @GetMapping("/circle/list")
    public Result<Map<String, Object>> myCirclePosts(@RequestParam(required = false) Integer page,
                                                     @RequestParam(required = false) Integer size,
                                                     HttpServletRequest request) {
        return Result.success(mockDataService.listMerchantCirclePosts(getUserId(request), page, size));
    }

    @PostMapping("/circle")
    public Result<Map<String, Object>> createCirclePost(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
        return Result.success(mockDataService.createMerchantCirclePost(getUserId(request), payload));
    }

    @PutMapping("/circle/{id}")
    public Result<Map<String, Object>> updateCirclePost(@PathVariable Long id,
                                                        @RequestBody Map<String, Object> payload,
                                                        HttpServletRequest request) {
        Map<String, Object> post = mockDataService.updateMerchantCirclePost(getUserId(request), id, payload);
        if (post == null) {
            return Result.error(404, "动态不存在或无权限");
        }
        return Result.success(post);
    }

    @DeleteMapping("/circle/{id}")
    public Result<Void> deleteCirclePost(@PathVariable Long id, HttpServletRequest request) {
        return mockDataService.deleteMerchantCirclePost(getUserId(request), id)
                ? Result.success()
                : Result.error(404, "动态不存在或无权限");
    }

    @GetMapping("/orders")
    public Result<Map<String, Object>> merchantOrders(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String orderNo,
            HttpServletRequest request
    ) {
        return Result.success(mockDataService.listMerchantOrders(getUserId(request), page, pageSize, status, orderNo));
    }

    @GetMapping("/orders/{id}")
    public Result<Map<String, Object>> merchantOrderDetail(@PathVariable Long id, HttpServletRequest request) {
        Map<String, Object> detail = mockDataService.getMerchantOrderDetail(getUserId(request), id);
        if (detail == null) {
            return Result.error(404, "订单不存在或无权限");
        }
        return Result.success(detail);
    }

    @PutMapping("/orders/{id}/ship")
    public Result<Void> shipOrder(@PathVariable Long id,
                                  @RequestBody(required = false) Map<String, String> payload,
                                  HttpServletRequest request) {
        if (mockDataService.getMerchantOrderDetail(getUserId(request), id) == null) {
            return Result.error(404, "订单不存在或无权限");
        }
        String logisticsCompany = payload == null ? null : payload.get("logisticsCompany");
        String logisticsNo = payload == null ? null : payload.get("logisticsNo");
        return mockDataService.shipOrder(id, logisticsCompany, logisticsNo)
                ? Result.success()
                : Result.error(404, "订单不存在");
    }

    @GetMapping("/after-sales")
    public Result<Map<String, Object>> afterSales(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String orderNo,
            HttpServletRequest request
    ) {
        return Result.success(mockDataService.listMerchantAfterSales(getUserId(request), page, pageSize, status, orderNo));
    }

    @PutMapping("/after-sales/{id}/handle")
    public Result<Void> handleAfterSale(@PathVariable Long id,
                                        @RequestBody(required = false) Map<String, String> payload,
                                        HttpServletRequest request) {
        String result = payload == null ? null : payload.get("result");
        String remark = payload == null ? null : payload.get("remark");
        return mockDataService.handleMerchantAfterSale(getUserId(request), id, result, remark)
                ? Result.success()
                : Result.error(404, "售后单不存在或无权限");
    }

    @GetMapping("/products")
    public Result<Map<String, Object>> products(@RequestParam(required = false) Integer page,
                                                @RequestParam(required = false) Integer pageSize,
                                                @RequestParam(required = false) String name,
                                                @RequestParam(required = false) Integer auditStatus,
                                                HttpServletRequest request) {
        return Result.success(mockDataService.listMerchantProducts(getUserId(request), page, pageSize, name, auditStatus));
    }

    @PostMapping("/products")
    public Result<Map<String, Object>> saveProduct(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
        Map<String, Object> product = mockDataService.saveMerchantProduct(getUserId(request), payload);
        if (product == null) {
            return Result.error(404, "商品不存在或无权限");
        }
        return Result.success(product);
    }

    @PutMapping("/products/{id}/status")
    public Result<Void> updateProductStatus(@PathVariable Long id, @RequestBody Map<String, Integer> payload, HttpServletRequest request) {
        Integer status = payload == null ? 0 : payload.getOrDefault("auditStatus", 0);
        return mockDataService.updateMerchantProductStatus(getUserId(request), id, status)
                ? Result.success()
            : Result.error(400, "仅支持提交审核，商品不存在或无权限");
    }

    @GetMapping("/trace/list")
    public Result<Map<String, Object>> traceList(@RequestParam(required = false) Integer page,
                                                 @RequestParam(required = false) Integer pageSize,
                                                 HttpServletRequest request) {
        return Result.success(mockDataService.listMerchantTraces(getUserId(request), page, pageSize));
    }

    @PutMapping("/trace/{productId}")
    public Result<Map<String, Object>> saveTrace(@PathVariable Long productId,
                                                 @RequestBody Map<String, Object> payload,
                                                 HttpServletRequest request) {
        Map<String, Object> trace = mockDataService.saveMerchantTrace(getUserId(request), productId, payload);
        if (trace == null) {
            return Result.error(404, "溯源信息不存在或无权限");
        }
        return Result.success(trace);
    }

    @GetMapping("/trace/{productId}/qrcode")
    public Result<Map<String, Object>> traceQr(@PathVariable Long productId) {
        Map<String, Object> qr = mockDataService.generateTraceQr(productId);
        if (qr == null) {
            return Result.error(404, "商品不存在");
        }
        return Result.success(qr);
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats(HttpServletRequest request) {
        return Result.success(mockDataService.getMerchantStats(getUserId(request)));
    }

    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            return jwtUtils.getUserIdFromToken(token.substring(7));
        }
        return 1L;
    }
}
