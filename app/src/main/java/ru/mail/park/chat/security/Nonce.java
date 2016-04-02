package ru.mail.park.chat.security;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Created by Михаил on 02.04.2016.
 */
public class Nonce {
    public static final int LENGTH = 16;
    private byte[] nonce;

    public Nonce() {
        nonce = new byte[LENGTH];
        SecureRandom sr = new SecureRandom();
        sr.nextBytes(nonce);
    }

    public Nonce(byte[] fromNonce) throws  IllegalArgumentException {
        if (fromNonce.length == LENGTH) {
            nonce = fromNonce.clone();
        } else {
            throw new IllegalArgumentException("Nonce should be exactly 16 bytes long");
        }
    }

    public byte[] getNonce() {
        return nonce;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(nonce).build();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Nonce) {
            return Arrays.equals(getNonce(), ((Nonce) o).getNonce());
        }
        return super.equals(o);
    }
}
