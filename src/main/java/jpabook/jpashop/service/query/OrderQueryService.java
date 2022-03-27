package jpabook.jpashop.service.query;

import jpabook.jpashop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderQueryService {

    /**
     * 화면이나 API 에 맞춘 서비스 (주로 읽기 전용 트랜잭션 사용)
     */

    private final OrderRepository orderRepository;

//    @GetMapping("/api/v3/orders")
//    public List<OrderDto> ordersV3() {
//        List<Order> orders = orderRepository.findAllWithItem();
//
//        return orders.stream()
//                .map(OrderDto::new)
//                .collect(toList());
//    }
}
