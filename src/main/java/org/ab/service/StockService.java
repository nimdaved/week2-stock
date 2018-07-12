package org.ab.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Date;
import java.util.HashSet;
import java.util.Set;

import org.ab.domain.StockItem;
import org.ab.domain.StockSummary;
import org.ab.repositories.StockRepository;
import org.ab.repositories.StockSummaryRepositoy;
import org.ab.util.StockStreamHandler;
import org.ab.util.SupplierWithExcepetion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonParser;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class StockService {

	private final StockStreamHandler jsonStreamHandler;
	private final StockRepository stockRepository;
	private final StockSummaryRepositoy stockSummaryRepository;

	public StockService(StockStreamHandler jsonStreamHandler, StockRepository stockRepository,
			StockSummaryRepositoy stockSummaryRepository) {
		this.jsonStreamHandler = jsonStreamHandler;
		this.stockRepository = stockRepository;
		this.stockSummaryRepository = stockSummaryRepository;
	}

	@Value("${load.batch.size:512}")
	private int loadBatchSize;

	/**
	 * Loading file using Json Stream APi, as file may be to large to hold in memory
	 * 
	 * @param file
	 * @return
	 */
	public int loadStocks(MultipartFile file) {
		return loadStocks(file::getInputStream);
	}

	public int loadStocks(String url) {
		return loadStocks(() -> new URL(url).openConnection().getInputStream());
	}

	public int loadStocks(SupplierWithExcepetion<InputStream, IOException> supplier) {
		final String sourceName = "InputStream";
		log.info("Starting to load stocks from {}", sourceName);
		int stockCount = 0;
		try (InputStream fileStream = supplier.get(); JsonParser parser = jsonStreamHandler.fromStream(fileStream)) {
			StockItem item = null;
			Set<StockItem> items = new HashSet<>(loadBatchSize);
			do {
				item = jsonStreamHandler.readNextStockItem(parser);
				if (item != null) {
					items.add(item);
					if (++stockCount % loadBatchSize == 0) {
						log.info("Saving next batch of {} records, ", loadBatchSize);
						stockRepository.saveAll(items);
						items = new HashSet<>(loadBatchSize);
					}
				} else if (!items.isEmpty()) {
					log.info("Saving last batch remain {} records", stockCount % loadBatchSize);

					stockRepository.saveAll(items);
				}

			} while (item != null);

		} catch (IOException e) {
			String msg = "Unable to load file" + sourceName;
			log.error(msg, e);
			throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, msg);
		}

		log.info("Loaded {} stocks from {}", stockCount, sourceName);

		return stockCount;
	}

	public StockSummary getStockSummary(String symbol, Date startDate) {
		log.info("MethodEntry getStockSummary {}, {}", symbol, startDate);
		return getStockSummary(symbol, startDate, addDay(startDate));
	}

	public StockSummary getStockSummary(String symbol, Date startDate, Date endDate) {
		return stockSummaryRepository.findForPeriod(symbol, startDate, endDate);
	}

	private static Date addDay(Date startDate) {
		return Date.valueOf(startDate.toLocalDate().plusDays(1));
	}

}
