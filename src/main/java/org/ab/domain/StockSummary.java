package org.ab.domain;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class StockSummary {
	@Id
	private String symbol;
	private BigDecimal highestPrice;
	private BigDecimal lowestPrice;
	private long volume;

}
