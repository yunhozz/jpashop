package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jpabook.jpashop.domain.item.Item;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;

/**
 * Order 엔티티와 Item 엔티티는 서로 다대다 관계에 놓여있다. (1주문 : n상품 || 1상품 : n주문)
 * 하지만 이런 관계는 거의 사용하지 않으므로 중간에 OrderItem 엔티티를 추가하여 관계를 풀어낸다.
 */
@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id @GeneratedValue
    @Column(name = "order_item_id")
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    private int orderPrice; //주문 가격

    private int count; //주문 수량

    /**
     * 생성 메소드
     */
    public static OrderItem createOrderItem(Item item, int orderPrice, int count) {
        /*
        <SetOrder 를 넣지 않은 이유>
        OrderService 에서 createOrder 를 생성하기 전에 createOrderItem 을 먼저 선언해야 한다.
        그 다음 createOrder 를 선언하면 내부적으로 setOrder() 가 실행된다.
         */
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setOrderPrice(orderPrice);
        orderItem.setCount(count);

        item.removeStockQuantity(count); //해당 상품 재고수량 감소

        return orderItem;
    }

    /**
     * 비즈니스 로직
     */
    public void cancel() {
        item.addStockQuantity(count); //해당 상품의 재고수량을 원복해준다.
    }

    /**
     * 조회 로직
     */
    public int getTotalPrice() { //주문 상품 전체 가격 조회
        return getOrderPrice() * getCount();
    } //get 함수를 굳이 안써도 된다. -> equals, hashcode

    /*
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        return orderPrice == orderItem.orderPrice && count == orderItem.count && Objects.equals(id, orderItem.id) && Objects.equals(order, orderItem.order) && Objects.equals(item, orderItem.item);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, order, item, orderPrice, count);
    }
     */
}
