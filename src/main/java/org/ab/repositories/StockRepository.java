package org.ab.repositories;


import org.ab.domain.StockItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<StockItem, Long> {

}
