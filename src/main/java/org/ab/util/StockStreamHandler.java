package org.ab.util;

import static com.fasterxml.jackson.core.JsonToken.END_ARRAY;
import static com.fasterxml.jackson.core.JsonToken.END_OBJECT;
import static com.fasterxml.jackson.core.JsonToken.START_ARRAY;
import static com.fasterxml.jackson.core.JsonToken.START_OBJECT;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.ab.domain.StockItem;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class StockStreamHandler {

	private static final String DATE = "date";
	private static final String VOLUME = "volume";
	private static final String PRICE = "price";
	private static final String SYMBOL = "symbol";
	private static final int StockItemFieldCount = 4;
	private static final int BracketOverhead = 2;
	private static final int UnknownFieldsOverhead = 4;
	private static final int TokenCountMax = StockItemFieldCount + BracketOverhead + UnknownFieldsOverhead;

	private final JsonFactory jsonFactory;
	private final ObjectMapper objectMapper;

	public StockStreamHandler(JsonFactory jsonFactory, ObjectMapper objectMapper) {
		super();
		this.jsonFactory = jsonFactory;
		this.objectMapper = objectMapper;
	}

	public JsonParser fromStream(InputStream inputStream) throws JsonParseException, IOException {
		return jsonFactory.createParser(inputStream);
	}

	public StockItem readNextStockItem(JsonParser jParser) throws IOException {
		StockItem item = new StockItem();

		for (int i = 0; !item.populated() && i < TokenCountMax; i++) {
			JsonToken token = jParser.nextValue();
			if (token == START_OBJECT || token == START_ARRAY || token == END_OBJECT) {
				continue;
			}
			if (token == END_ARRAY) {
				return null;
			}

			String fieldname = jParser.getCurrentName();
			/**
			 * Given small number of fields using explicit field names for performance,
			 *  vs. possible reflective introspection
			 */
			if (SYMBOL.equals(fieldname)) {				
				item.setSymbol(jParser.getText());
			} else if (PRICE.equals(fieldname)) {
				item.setPrice(jParser.getDecimalValue());
			} else if (VOLUME.equals(fieldname)) {
				item.setVolume(jParser.getIntValue());
			} else if (DATE.equals(fieldname)) {
				item.setDate(objectMapper.readValue(jParser, Date.class));
			}
		}
		if (!(item == null || item.populated())) {
			throw new IllegalArgumentException("Invalid format. Missing fields");
		}

		return item;

	}

}
