package org.ab.domain;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
public class StockItem {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	private String symbol;
	//TODO: all fields as primitives to improve performance
	private BigDecimal price;
	private Integer volume;
	private Date date;
    
	public boolean populated() {
		return symbol != null && price != null && volume != null && date != null;
	}
}
