package jpabook.jpashop.repository;

import jpabook.jpashop.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepository {

    private final EntityManager em;

    public void save(Item item) {
        if(item.getId() == null) {
            em.persist(item); //신규로 등록할 때
        } else {
            //merge 는 실무에선 사용 x -> 위험
            em.merge(item); //이미 DB에 등록되어 있을 때 -> update 와 유사, 준영속 상태의 엔티티를 영속 상태로 변경
        }
    }

    public Item findOne(Long id) {
        return em.find(Item.class, id);
    }

    public List<Item> findAll() {
        return em.createQuery("select i from Item i", Item.class)
                    .getResultList();
    }
}
