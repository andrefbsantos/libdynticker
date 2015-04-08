package mobi.boilr.libdynticker.exchanges;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import mobi.boilr.libdynticker.core.Exchange;
import mobi.boilr.libdynticker.core.Pair;
import mobi.boilr.libdynticker.core.exception.NoMarketDataException;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;

public final class BittrexExchange extends Exchange {

	public BittrexExchange(long expiredPeriod) {
		super("Bittrex", expiredPeriod);
	}
	
	@Override
	protected List<Pair> getPairsFromAPI() throws JsonProcessingException, MalformedURLException,
			IOException {
		List<Pair> pairs = new ArrayList<Pair>();
		Iterator<JsonNode> elements = readJsonFromUrl("https://bittrex.com/api/v1.1/public/getmarkets").get("result").getElements();
		JsonNode element;
		String coin, exchange;
		while(elements.hasNext()) {
			element = elements.next();
			if(element.get("IsActive").asBoolean()) {
				coin = element.get("MarketCurrency").getTextValue();
				exchange = element.get("BaseCurrency").getTextValue();
				pairs.add(new Pair(coin, exchange));
			}
		}
		return pairs;
	}

	@Override
	protected String getTicker(Pair pair) throws JsonProcessingException, MalformedURLException,
			IOException, NoMarketDataException {
		JsonNode node = readJsonFromUrl("https://bittrex.com/api/v1.1/public/getticker?market=" +
			pair.getExchange() + "-" + pair.getCoin());
		if(node.get("success").getBooleanValue()) {
			return parseTicker(node, pair);
		} else {
			throw new MalformedURLException(node.get("message").getTextValue());
		}
	}

	@Override
	public String parseTicker(JsonNode node, Pair pair) throws IOException {
		String last = node.get("result").get("Last").asText();
		if(last.equals("null")) {
			throw new NoMarketDataException(pair);
		}
		return last;
	}
}