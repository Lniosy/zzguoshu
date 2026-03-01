package com.fruitveg.controller.order;

import com.fruitveg.common.Result;
import com.fruitveg.service.MockDataService;
import com.fruitveg.utils.JwtUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/order")
public class OrderController {

    private final MockDataService mockDataService;
    private final JwtUtils jwtUtils;

    public OrderController(MockDataService mockDataService, JwtUtils jwtUtils) {
        this.mockDataService = mockDataService;
        this.jwtUtils = jwtUtils;
    }

    @GetMapping("/list")
    public Result<Map<String, Object>> getOrderList(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String orderNumber
    ) {
        return Result.success(mockDataService.listUserOrders(page, pageSize, status, orderNumber));
    }

    @GetMapping("/after-sales")
    public Result<Map<String, Object>> getAfterSales(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            HttpServletRequest request
    ) {
        return Result.success(mockDataService.listUserAfterSales(getUserId(request), page, pageSize));
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getOrderDetail(@PathVariable Long id) {
        Map<String, Object> detail = mockDataService.getUserOrderDetail(id);
        if (detail == null) {
            return Result.error(404, "订单不存在");
        }
        return Result.success(detail);
    }

    @PostMapping("/create")
    public Result<Map<String, Object>> createOrder(@RequestBody(required = false) Map<String, Object> payload, HttpServletRequest request) {
        Long userId = getUserId(request);
        return Result.success(mockDataService.createOrder(userId, payload));
    }

    @PutMapping("/{id}/cancel")
    public Result<Void> cancelOrder(@PathVariable Long id) {
        return mockDataService.updateOrderStatus(id, "cancelled", 5) ? Result.success() : Result.error(404, "订单不存在");
    }

    @PutMapping("/{id}/confirm")
    public Result<Void> confirmOrder(@PathVariable Long id) {
        return mockDataService.updateOrderStatus(id, "completed", 4) ? Result.success() : Result.error(404, "订单不存在");
    }

    @PutMapping("/{id}/pay")
    public Result<Void> payOrder(@PathVariable Long id) {
        return mockDataService.updateOrderStatus(id, "shipped", 1) ? Result.success() : Result.error(404, "订单不存在");
    }

    @PutMapping("/{id}/receive")
    public Result<Void> receiveOrder(@PathVariable Long id) {
        return mockDataService.updateOrderStatus(id, "completed", 4) ? Result.success() : Result.error(404, "订单不存在");
    }

    @PostMapping("/{id}/refund")
    public Result<Map<String, Object>> refundOrder(@PathVariable Long id,
                                                   @RequestBody(required = false) Map<String, Object> payload,
                                                   HttpServletRequest request) {
        Map<String, Object> complaint = mockDataService.applyRefund(id, getUserId(request), payload);
        if (complaint == null) {
            return Result.error(404, "订单不存在");
        }
        return Result.success(complaint);
    }

    @PostMapping("/{id}/review")
    public Result<Map<String, Object>> reviewOrder(@PathVariable Long id,
                                                   @RequestBody(required = false) Map<String, Object> payload,
                                                   HttpServletRequest request) {
        Map<String, Object> review = mockDataService.submitOrderReview(getUserId(request), id, payload);
        if (review == null) {
            return Result.error(404, "订单不存在");
        }
        if (review.isEmpty()) {
            return Result.error(400, "仅已完成订单可评价");
        }
        return Result.success(review);
    }

    @GetMapping("/{id}/review")
    public Result<Map<String, Object>> getReview(@PathVariable Long id, HttpServletRequest request) {
        Map<String, Object> review = mockDataService.getOrderReview(getUserId(request), id);
        if (review == null) {
            return Result.error(404, "评价不存在");
        }
        return Result.success(review);
    }

    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            return jwtUtils.getUserIdFromToken(token.substring(7));
        }
        return 1L;
    }
}
