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

package de.schildbach.wallet.util;

import org.bitcoinj.core.Sha256Hash;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Regression tests for {@link WalletUtils#longHash(Sha256Hash)}.
 */
public class WalletUtilsLongHashTest {
    @Test
    public void longHash_bytesSequence() {
        final byte[] bytes = new byte[32];
        for (int i = 0; i < 32; i++) {
            bytes[i] = (byte) i;
        }
        final Sha256Hash hash = Sha256Hash.wrap(bytes);
        final long value = WalletUtils.longHash(hash);
        assertEquals(0x18191A1B1C1D1E1FL, value);
    }
}
