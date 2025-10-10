/*
 * Copyright the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.schildbach.wallet.exchangerate;

import com.squareup.moshi.Moshi;
import okio.BufferedSource;
import okio.Okio;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Andreas Schildbach
 */
public class CoinGeckoTest {
    private final CoinGecko coinGecko = new CoinGecko(new Moshi.Builder().build());

    @Test
    public void parse() throws Exception {
        final BufferedSource json = Okio.buffer(Okio.source(getClass().getResourceAsStream("coingecko.json")));
        final List<ExchangeRateEntry> rates = coinGecko.parse(json);
        assertEquals(45, rates.size());
    }

    @Test
    public void parse_stringValues() throws Exception {
        final BufferedSource json = Okio.buffer(Okio.source(getClass().getResourceAsStream("coingecko-string-values.json")));
        final List<ExchangeRateEntry> rates = coinGecko.parse(json);
        assertEquals(3, rates.size());
    }

    @Test
    public void parse_decimalStringValues() throws Exception {
        final BufferedSource json = Okio.buffer(Okio.source(getClass().getResourceAsStream("coingecko-decimal-values.json")));
        final List<ExchangeRateEntry> rates = coinGecko.parse(json);
        assertEquals(4, rates.size());
    }

    @Test
    public void parse_onlyFiatTypesIncluded() throws Exception {
        final BufferedSource json = Okio.buffer(Okio.source(getClass().getResourceAsStream("coingecko-mixed-types.json")));
        final List<ExchangeRateEntry> rates = coinGecko.parse(json);
        // Should only include fiat, not crypto or commodity
        assertEquals(2, rates.size());
    }

    @Test
    public void parse_filtersZeroAndNegativeRates() throws Exception {
        final BufferedSource json = Okio.buffer(Okio.source(getClass().getResourceAsStream("coingecko-zero-negative.json")));
        final List<ExchangeRateEntry> rates = coinGecko.parse(json);
        // Should exclude zero and negative values
        assertEquals(1, rates.size());
    }

    @Test
    public void parse_handlesInvalidValues() throws Exception {
        final BufferedSource json = Okio.buffer(Okio.source(getClass().getResourceAsStream("coingecko-invalid-values.json")));
        final List<ExchangeRateEntry> rates = coinGecko.parse(json);
        // Should skip invalid entries and continue
        assertEquals(2, rates.size());
    }

    @Test
    public void parse_handlesScientificNotation() throws Exception {
        final BufferedSource json = Okio.buffer(Okio.source(getClass().getResourceAsStream("coingecko-scientific-notation.json")));
        final List<ExchangeRateEntry> rates = coinGecko.parse(json);
        assertEquals(3, rates.size());
    }

    @Test
    public void parse_handlesVeryLargeValues() throws Exception {
        final BufferedSource json = Okio.buffer(Okio.source(getClass().getResourceAsStream("coingecko-large-values.json")));
        final List<ExchangeRateEntry> rates = coinGecko.parse(json);
        assertEquals(2, rates.size());
    }

    @Test
    public void parse_emptyRatesMap() throws Exception {
        final BufferedSource json = Okio.buffer(Okio.source(getClass().getResourceAsStream("coingecko-empty.json")));
        final List<ExchangeRateEntry> rates = coinGecko.parse(json);
        assertEquals(0, rates.size());
    }

    @Test
    public void mediaType_returnsCorrectType() {
        assertEquals("application/json; charset=utf-8", coinGecko.mediaType().toString());
    }

    @Test
    public void url_returnsCorrectUrl() {
        assertEquals("https://api.coingecko.com/api/v3/exchange_rates", coinGecko.url().toString());
    }
}