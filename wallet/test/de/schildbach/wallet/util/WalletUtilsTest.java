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

import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Andreas Schildbach
 */
public class WalletUtilsTest {
    @Test
    public void restoreWalletFromProtobufOrBase58() throws Exception {
        WalletUtils.restoreWalletFromProtobuf(getClass().getResourceAsStream("backup-protobuf-testnet"),
                TestNet3Params.get());
    }

    @Test(expected = IOException.class)
    public void restoreWalletFromProtobuf_wrongNetwork() throws Exception {
        WalletUtils.restoreWalletFromProtobuf(getClass().getResourceAsStream("backup-protobuf-testnet"),
                MainNetParams.get());
    }

    @Test
    public void longHash_allZeros() {
        final byte[] bytes = new byte[32];
        final org.bitcoinj.core.Sha256Hash hash = org.bitcoinj.core.Sha256Hash.wrap(bytes);
        final long value = WalletUtils.longHash(hash);
        assertEquals(0L, value);
    }

    @Test
    public void longHash_allOnes() {
        final byte[] bytes = new byte[32];
        for (int i = 0; i < 32; i++) {
            bytes[i] = (byte) 0xFF;
        }
        final org.bitcoinj.core.Sha256Hash hash = org.bitcoinj.core.Sha256Hash.wrap(bytes);
        final long value = WalletUtils.longHash(hash);
        assertEquals(-1L, value);
    }

    @Test
    public void longHash_sequentialBytes() {
        final byte[] bytes = new byte[32];
        for (int i = 0; i < 32; i++) {
            bytes[i] = (byte) i;
        }
        final org.bitcoinj.core.Sha256Hash hash = org.bitcoinj.core.Sha256Hash.wrap(bytes);
        final long value = WalletUtils.longHash(hash);
        // Expected: little-endian composition of bytes[24-31]
        // bytes[31]=31, bytes[30]=30, ..., bytes[24]=24
        // In little-endian: 31 | (30 << 8) | (29 << 16) | (28 << 24) | (27 << 32) | (26 << 40) | (25 << 48) | (24 << 56)
        // This test will fail with the current bug (bytes[23] instead of bytes[24])
        assertEquals(0x18191A1B1C1D1E1FL, value);
    }

    @Test
    public void longHash_onlyLastByteSet() {
        final byte[] bytes = new byte[32];
        bytes[31] = (byte) 0x42;
        final org.bitcoinj.core.Sha256Hash hash = org.bitcoinj.core.Sha256Hash.wrap(bytes);
        final long value = WalletUtils.longHash(hash);
        assertEquals(0x42L, value);
    }

    @Test
    public void longHash_onlyFirstRelevantByteSet() {
        final byte[] bytes = new byte[32];
        bytes[24] = (byte) 0xAB;
        final org.bitcoinj.core.Sha256Hash hash = org.bitcoinj.core.Sha256Hash.wrap(bytes);
        final long value = WalletUtils.longHash(hash);
        // This should be 0xAB00000000000000L but with the bug it will be 0x0L
        assertEquals(0xAB00000000000000L, value);
    }

    @Test
    public void longHash_signedByteHandling() {
        final byte[] bytes = new byte[32];
        // Set a byte that would be negative if interpreted as signed
        bytes[31] = (byte) 0x80; // -128 as signed, 128 as unsigned
        final org.bitcoinj.core.Sha256Hash hash = org.bitcoinj.core.Sha256Hash.wrap(bytes);
        final long value = WalletUtils.longHash(hash);
        // Should be 0x80 (128), not negative
        assertEquals(0x80L, value);
    }

    @Test
    public void longHash_multipleSignedBytes() {
        final byte[] bytes = new byte[32];
        bytes[31] = (byte) 0xFF; // -1 as signed, 255 as unsigned
        bytes[30] = (byte) 0xFF;
        bytes[29] = (byte) 0xFF;
        bytes[28] = (byte) 0xFF;
        bytes[27] = (byte) 0xFF;
        bytes[26] = (byte) 0xFF;
        bytes[25] = (byte) 0xFF;
        bytes[24] = (byte) 0xFF;
        final org.bitcoinj.core.Sha256Hash hash = org.bitcoinj.core.Sha256Hash.wrap(bytes);
        final long value = WalletUtils.longHash(hash);
        assertEquals(-1L, value);
    }

    @Test
    public void longHash_mixedPattern() {
        final byte[] bytes = new byte[32];
        bytes[31] = (byte) 0x01;
        bytes[30] = (byte) 0x02;
        bytes[29] = (byte) 0x03;
        bytes[28] = (byte) 0x04;
        bytes[27] = (byte) 0x05;
        bytes[26] = (byte) 0x06;
        bytes[25] = (byte) 0x07;
        bytes[24] = (byte) 0x08;
        final org.bitcoinj.core.Sha256Hash hash = org.bitcoinj.core.Sha256Hash.wrap(bytes);
        final long value = WalletUtils.longHash(hash);
        // Expected: 0x01 | (0x02 << 8) | (0x03 << 16) | (0x04 << 24) | (0x05 << 32) | (0x06 << 40) | (0x07 << 48) | (0x08 << 56)
        assertEquals(0x0807060504030201L, value);
    }

    @Test
    public void longHash_realHashExample() {
        // Use a real Bitcoin transaction hash pattern
        final byte[] bytes = new byte[32];
        // Fill with pseudo-random pattern
        for (int i = 0; i < 32; i++) {
            bytes[i] = (byte) ((i * 7 + 13) % 256);
        }
        final org.bitcoinj.core.Sha256Hash hash = org.bitcoinj.core.Sha256Hash.wrap(bytes);
        final long value = WalletUtils.longHash(hash);
        // Verify it's deterministic and produces a valid long
        final long secondCall = WalletUtils.longHash(hash);
        assertEquals(value, secondCall);
    }

    @Test
    public void longHash_ignoresFirstBytes() {
        // Test that bytes[0-23] are ignored
        final byte[] bytes1 = new byte[32];
        final byte[] bytes2 = new byte[32];
        
        // Set last 8 bytes the same
        for (int i = 24; i < 32; i++) {
            bytes1[i] = (byte) i;
            bytes2[i] = (byte) i;
        }
        
        // Set first 24 bytes differently
        for (int i = 0; i < 24; i++) {
            bytes1[i] = (byte) i;
            bytes2[i] = (byte) (i + 100);
        }
        
        final org.bitcoinj.core.Sha256Hash hash1 = org.bitcoinj.core.Sha256Hash.wrap(bytes1);
        final org.bitcoinj.core.Sha256Hash hash2 = org.bitcoinj.core.Sha256Hash.wrap(bytes2);
        
        assertEquals(WalletUtils.longHash(hash1), WalletUtils.longHash(hash2));
    }
}