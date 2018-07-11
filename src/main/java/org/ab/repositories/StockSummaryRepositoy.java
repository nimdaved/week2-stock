package org.ab.repositories;

import java.sql.Date;

import org.ab.domain.StockSummary;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

public interface StockSummaryRepositoy extends Repository<StockSummary, String> {
	@Query(value = "select new org.ab.domain.StockSummary(s.symbol, MAX(s.price), "
			+ "MIN(s.price), SUM(s.volume)) "
			+ "from StockItem s"
			+ " where s.symbol = ?1 and s.date >= ?2 and s.date < ?3 GROUP BY s.symbol")
	StockSummary findForPeriod(String symbol, Date startDate, Date endDate);

}
