package org.ab.controller;

import java.sql.Date;

import org.ab.domain.LoadResponse;
import org.ab.domain.StockSummary;
import org.ab.service.StockService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/")
@Slf4j
public class StockController {

	private final StockService stockService;

	public StockController(StockService stockService) {
		super();
		this.stockService = stockService;
	}

	@PostMapping("/load")
	public @ResponseBody LoadResponse load(@RequestParam("file") MultipartFile file) {
		final String fileName = file.getOriginalFilename();
		log.info("Starting to load file: {}", fileName);
		int stockCount = stockService.loadStocks(file);
		return new LoadResponse(fileName, stockCount);
	}

	@GetMapping("/summary/{symbol}")
	public @ResponseBody StockSummary getDateRangeSummary(@PathVariable("symbol") String symbol,
			@RequestParam("startDate") Date startDate, @RequestParam("endDate") Date endDate) {
		return stockService.getStockSummary(symbol, startDate, endDate);

	}

	@GetMapping("/summary/{symbol}/{startDate}")
	public @ResponseBody StockSummary getDailySummary(@PathVariable("symbol") String symbol,
			@PathVariable("startDate") Date startDate) {
		return stockService.getStockSummary(symbol, startDate);

	}

}
