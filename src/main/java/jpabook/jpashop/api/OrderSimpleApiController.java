package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.OrderSimpleQueryDto;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ManyToOne, OneToOne 에서의 성능 최적화
 * Order
 * Order -> Member
 * Order -> Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;

    //엔티티를 API 응답으로 외부로 노출 x -> DTO 로 변환해서 반환하자
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        //양방향 무한루프에 빠진다 -> @JsonIgnore, Hibernate5Module
        List<Order> all = orderRepository.findAllByString(new OrderSearch());

        for (Order order : all) {
            order.getMember().getName(); //Lazy 강제 초기화
            order.getDelivery().getAddress(); //Lazy 강제 초기화
        }

        return all;
    }

    //Lazy 로딩에 의한 쿼리 생성이 너무 많이 됨
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2() {
        //ORDER N번
        //N + 1 문제 발생 -> 1 + 회원 N + 배송 N -> SQL (2N + 1)번 실행 (V1 과 쿼리수 결과는 같다)
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<SimpleOrderDto> result = orders.stream().map(o -> new SimpleOrderDto(o)).toList();

        return result;
    }

    /**
     * 엔티티를 DTO 로 변환하여 조회 (우선 이 방법을 사용해보자)
     */
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3() {
        //fetch join 을 이용한 방법 -> 정말 많이 사용하므로 꼭 100퍼센트 이해하고 넘어가자!!
        //SQL 1번 실행
        List<Order> orders = orderRepository.findAllWithMemberDelivery(); //order -> member, delivery
        List<SimpleOrderDto> result = orders.stream().map(o -> new SimpleOrderDto(o)).toList();

        return result;
    }

    /**
     * DTO 로 바로 조회
     */
    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4() {
        //new 명령어를 사용해서 JPQL 의 결과를 DTO 로 즉시 변환
        //V3과 성능차이가 미비하다.
        return orderRepository.findOrderDtos();
    }

    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName(); //Lazy 초기화
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress(); //Lazy 초기화
        }
    }
}
