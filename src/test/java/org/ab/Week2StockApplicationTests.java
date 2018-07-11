package org.ab;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.ab.domain.LoadResponse;
import org.ab.domain.StockItem;
import org.ab.domain.StockSummary;
import org.ab.domain.UrlHolder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class Week2StockApplicationTests {
	private final Random RAND = new Random();

	@Autowired
	private MockMvc mvc;

	@Autowired
	ObjectMapper objectMapper;

	@Test
	public void testUploadSingle() throws Exception {
		String fileName = "mySingleFile";

		LoadResponse lr = upload(fileName, create("SYM", new BigDecimal("34.21"), 123, new Date()));

		assertEquals(1, (int) lr.getStockCount());
	}
	
	@Test
	public void testWebUpload() throws Exception {
		UrlHolder url = new UrlHolder("https://bootcamp-training-files.cfapps.io/week2/week2-stocks.json");

		ResultActions ra = mvc.perform(post("/load/web").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(url))).andExpect(status().isOk());
		LoadResponse lr = toDomainObject(ra, LoadResponse.class);
		assertNotNull(lr);
		assertEquals(url.getUrl(), lr.getFileName());
		
	}

	@Test
	public void testGetDailySummary() throws Exception {
		// prepare data
		String symbol = "APPL";
		Pair<String, Set<StockItem>> dailySeries = symbolDailySeries(symbol, 76);
		upload("dailySeriesFile", dailySeries.getSecond());

		String dailySummaryPath = "/summary/" + symbol + "/" + dailySeries.getFirst();

		ResultActions ra = mvc.perform(get(dailySummaryPath)).andExpect(status().isOk());
		StockSummary summary = toDomainObject(ra, StockSummary.class);
		// calculate required values
		long volume = dailySeries.getSecond().stream().mapToInt(s -> s.getVolume()).sum();
		BigDecimal highest = dailySeries.getSecond().stream().map(s -> s.getPrice()).distinct()
				.max((p1, p2) -> p1.compareTo(p2)).get();
		BigDecimal lowest = dailySeries.getSecond().stream().map(s -> s.getPrice()).distinct()
				.min((p1, p2) -> p1.compareTo(p2)).get();

		assertEquals(summary, new StockSummary(symbol, highest, lowest, volume));
	}

	private LoadResponse upload(String fileName, StockItem... items) throws Exception {
		MockMultipartFile mf = toMultipart(fileName, items);
		ResultActions ra = mvc.perform(multipart("/load").file(mf)).andExpect(status().isOk());
		LoadResponse lr = toDomainObject(ra, LoadResponse.class);
		assertNotNull(lr);
		assertEquals(fileName, lr.getFileName());

		return lr;
	}

	//// helpers

	private LoadResponse upload(String fileName, Collection<StockItem> items) throws Exception {
		return upload(fileName, items.toArray(new StockItem[items.size()]));
	}

	private MockMultipartFile toMultipart(String fileName, StockItem... items) throws JsonProcessingException {
		final byte[] content = objectMapper.writeValueAsBytes(Arrays.asList(items));
		return new MockMultipartFile("file", fileName, "application/json", content);
	}

	private static StockItem create(String symbol, BigDecimal price, int volume, Date date) {
		StockItem si = new StockItem();
		si.setSymbol(symbol);
		si.setPrice(price);
		si.setVolume(volume);
		si.setDate(date);

		return si;
	}

	private Pair<String, Set<StockItem>> symbolDailySeries(String symbol, long count) throws JsonProcessingException {

		int low = 3;
		int high = 37;
		LocalDateTime ld = LocalDateTime.now().plusDays(-1);
		String dateStr = ld.format(DateTimeFormatter.ISO_LOCAL_DATE);

		Set<StockItem> items = RAND.ints(count, low, high)
				.mapToObj(i -> create(symbol, new BigDecimal(100 * i).setScale(2), 104 * i,
						Date.from(ld.plusSeconds(i).toInstant(ZoneOffset.ofHours(-6)))))
				.collect(Collectors.toSet());

		return Pair.of(dateStr, items);

	}

	@SuppressWarnings("unchecked")
	private <T> T toDomainObject(ResultActions resultActions, Class<T> domainClass) throws IOException {
		MvcResult result = resultActions.andReturn();
		String asString = result.getResponse().getContentAsString();
		return String.class == domainClass ? (T) asString : objectMapper.readValue(asString, domainClass);
	}

}
