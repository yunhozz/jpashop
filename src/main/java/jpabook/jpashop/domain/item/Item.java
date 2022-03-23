package jpabook.jpashop.domain.item;

import jpabook.jpashop.domain.Category;
import jpabook.jpashop.exception.NotEnoughStockException;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) //싱글테이블 전략
@DiscriminatorColumn(name = "dtype")
public abstract class Item {

    @Id @GeneratedValue
    @Column(name = "item_id")
    private Long id;

    //Item -> OrderItem 을 알 필요는 없다.
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "order_item_id")
//    private List<OrderItem> orderItems = new ArrayList<>();

    @ManyToMany(mappedBy = "items")
    private List<Category> categories = new ArrayList<>();

    private String name; //상품 이름

    private int price; //상품 가격

    private int stockQuantity; //재고 수량

    /**
     * 비즈니스 로직
     */
    //stock 증가
    public void addStockQuantity(int quantity) {
        this.stockQuantity += quantity;
    }

    //stock 감소
    public void removeStockQuantity(int quantity) {
        int restStock = this.stockQuantity - quantity;

        if (restStock >= 0) {
            this.stockQuantity = restStock;
        } else {
            throw new NotEnoughStockException("need more stock");
        }
    }
}
