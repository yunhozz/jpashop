package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderItemQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * OneToMany 에서의 성능 최적화
 * Order -> OrderItem -> Item
 */
@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    /**
     * v1 : 엔티티를 조회해서 그대로 반환
     * v2 : 엔티티 조회 후 DTO 로 변환
     * v3 : 페치 조인으로 쿼리 수 최적화 - 페이징이 불가능
     * v3.1 : 컬렉션 페이징과 한계 돌파
     * v4 : JPA 에서 DTO 를 직접 조회
     * v5 : 컬렉션 조회 최적화 - 일대다 관계인 컬렉션은 in 절을 활용해서 메모리에 미리 조회해서 최적화
     * v6 : 플랫 데이터 최적화 - join 결과를 그대로 조회 후 애플리케이션에서 원하는 모양으로 직접 변환
     *
     * v1, v2, v3, v3.1 -> 엔티티 조회
     * v4, v5, v6 -> DTO 직접 조회
     * 엔티티 조회 방식으로 우선 접근, 해결이 안되면 DTO 조회 방식 사용
     */

    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());

        for (Order order : all) {
            order.getMember().getName();
            order.getDelivery().getAddress();

            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.forEach(o -> o.getItem().getName());
        }

        return all;
    }

    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<OrderDto> collect = orders.stream()
                .map(o -> new OrderDto(o))
                .toList();

        return collect;
    }

    /**
     * 페치 조인으로 쿼리 수 최적화
     * OneToMany 상황에서 데이터가 뻥튀기 된다.
     * (SQL) distinct 추가하여 데이터 중복 조회를 제거한다!! 단, 페이징(데이터 개수 설정) 불가능!! -> v3.1
     */
    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem(); //order -> member, delivery, orderItem, item
        List<OrderDto> collect = orders.stream()
                .map(o -> new OrderDto(o))
                .toList();

        return collect;
    }

    /**
     * 페이징 적용
     * XToOne 관계를 모두 fetch join 하고, XToMany 관계는 지연로딩으로 조회한다.
     *  -> hibernate.default_batch_fetch_size(application), @BatchSize(개별)
     * 1 x m x n -> 1 x 1 x 1 와 같은 어마어마한 효과를 볼 수 있다.
     */
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> ordersV3_page(@RequestParam(value = "offset", defaultValue = "0") int offset,
                                        @RequestParam(value = "limit", defaultValue = "100") int limit) {

        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit); //order -> member, delivery
        List<OrderDto> collect = orders.stream()
                .map(o -> new OrderDto(o)) //orderItems 조회 -> item 조회 (lazy)
                .toList();

        return collect;
    }

    /**
     * XToOne 관계들을 먼저 조회, XToMany 관계는 각각 별도로 처리.
     */
    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4() {
        return orderQueryRepository.findOrderQueryDtos();
    }

    /**
     * 최적화 -> query : 루트 1번, 컬렉션 1번
     * 데이터를 한꺼번에 처리할 때 많이 사용하는 방식
     */
    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5() {
        return orderQueryRepository.findAllByDto_optimization();
    }

    /**
     * DTO 로 직접 조회, 플랫 데이터 최적화
     */
    @GetMapping("/api/v6/orders")
    public List<OrderQueryDto> ordersV6() {
        List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();

        //OrderFlatDto -> OrderQueryDto 변환
        return flats.stream()
                .collect(Collectors.groupingBy(o -> new OrderQueryDto(o.getOrderId(), o.getName(), o.getOrderDate(),
                                o.getOrderStatus(), o.getAddress()),
                        Collectors.mapping(o -> new OrderItemQueryDto(o.getOrderId(), o.getItemName(), o.getOrderPrice(),
                                o.getCount()), Collectors.toList())
                )).entrySet().stream()
                .map(e -> new OrderQueryDto(e.getKey().getOrderId(), e.getKey().getName(), e.getKey().getOrderDate(),
                        e.getKey().getOrderStatus(), e.getKey().getAddress(), e.getValue()))
                .toList();
    }

    @Data
    static class OrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
//        private List<OrderItem> orderItems; -> dto 에서도 entity 를 노출하면 안된다. -> dto 생성
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            orderItems = order.getOrderItems().stream()
                    .map(orderItem -> new OrderItemDto(orderItem))
                    .toList();
        }
    }

    @Data
    static class OrderItemDto {

        private String itemName;
        private int orderPrice;
        private int count;

        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }
}
