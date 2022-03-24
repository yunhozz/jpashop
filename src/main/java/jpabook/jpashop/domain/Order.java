package jpabook.jpashop.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "orders") //관례상 orders 로 설정
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    /**
     * <JPA 연관관계 매핑>
     *     1. 다대일: @ManyToOne (Order-Member, OrderItem-Order, OrderItem-Item)
     *     2. 일대다: @OneToMany (Member-Order, Order-OrderItem, Item-OrderItem)
     *     3. 일대일: @OneToOne (Order-Delivery)
     *     4. 다대다: @ManyToMany (Item-Category) -> 자원 관리에 한정적 -> 일대다, 다대일 매핑으로 풀어내서 사용
     *
     * FK o -> @JoinColumn(name = "id 명") 추가
     * FK x -> (mappedBy = "변수 명")
     *
     * 연관관계는 꼭 필요한 경우에만 설정하는 것이 좋다. -> 비즈니스 상황에 맞추어 최소화한다.
     * 실무에서 모든 연관관계는 꼭! 즉시로딩(EAGER)이 아닌 지연로딩(LAZY)으로 설정해야 한다. -> 모든 @ManyToOne, @OneToOne 에 추가!!!
     * 보통, @ManyToOne 과 @JoinColumn 이 같이 쓰이고, @OneToMany 와 mappedBy 가 같이 쓰인다.
     * .@OneToOne 의 경우 외래키를 가지는 도메인을 주인으로 설정한다.
     */

    /**
     * <도메인 주도 설계>
     * 서비스의 많은 로직이 엔티티로 이동, 서비스는 엔티티를 호출하는 정도의 얇은 비즈니스 로직을 갖게된다.
     * 이렇게 하면 information expert pattern 을 지키면서 개발할 수 있다.
     * 엔티티를 객체로 사용하는 방식이다.
     *
     * <서비스 주도 설계>
     * 엔티티는 단순히 getter, (setter) 만 제공하고, 서비스가 비즈니스 로직을 모두 갖도록 설계한다.
     * 엔티티를 자료 구조로 사용하는 방식이다.
     */

    @Id @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    /*
    LAZY : proxy 타입으로 조회(ByteBuddyInterceptor) -> 두 entity 간 서로 조회하는 경우가 별로 없을 때
    EAGER : 실제 entity 조회 -> 두 entity 를 함께 조회할 경우가 많을 때
    실무에서는 꼭 지연 로딩(LAZY)만을 사용하자!! -> 즉시 로딩(EAGER)은 JPQL 에서 N+1 문제를 일으킴
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    //cascade : 상위 엔티티에서 하위 엔티티로 특정 작업을 전파시키는 옵션 (ALL: 모든 작업) -> 온전히 개인 소유일 때만 사용!
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL) //FK
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    private LocalDateTime orderDate; //주문 시간

    @Enumerated(EnumType.STRING) //ORDINAL(기본): 숫자 출력 -> STRING 선언
    private OrderStatus status; //주문상태 (ORDER, CANCEL)

    /**
     * 연관관계 편의 메소드 -> 주로 비즈니스의 중심이 되는 곳에 설계한다.
     */
    public void setMember(Member member) {
        this.member = member;
        member.getOrders().add(this);
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
        delivery.setOrder(this);
    }

    /**
     * 생성 메소드 (정적 팩토리 메소드 + setter)
     */
    public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems) {
        Order order = new Order();

        order.setMember(member);
        order.setDelivery(delivery);
        order.setStatus(OrderStatus.ORDER); //setter
        order.setOrderDate(LocalDateTime.now()); //setter

        for (OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }

        return order;
    }

    /*
    객체를 생성하는 3가지 방법 (@Setter 미사용시)

    1. 생성자(constructor)
    2. 정적 팩토리 메소드 -> private
    3. 생성자에 Builder 패턴 추가 -> private

    객체 생성이 간단할 때는 생성자를 사용한다.
    만약 객체 생성이 복잡하고, 의미를 가지는 것이 좋다면 나머지 방법 중 하나를 선택한다.
    중요한 것은 이렇게 생성자에 파라미터를 넘기는 기법을 사용해서, 변경이 필요없는 필드에 추가적인 setter 를 외부에 노출하는 것을 줄이는 것이 핵심!!
     */

    /**
     * 비즈니스 로직
     */
    public void cancel() { //주문 취소
        if(delivery.getStatus() == DeliveryStatus.COMP) {
            throw new IllegalStateException("이미 배송 완료된 상품은 취소가 불가능합니다.");
        }

        this.setStatus(OrderStatus.CANCEL);

        for (OrderItem orderItem : orderItems) {
            orderItem.cancel();
        }
    }

    /**
     * 조회 로직
     */
    public int getTotalPrice() { //전체 주문 가격 조회
        int totalPrice = 0;

        for(OrderItem orderItem : orderItems) {
            totalPrice += orderItem.getTotalPrice();
        }

        return totalPrice;
    }
}
