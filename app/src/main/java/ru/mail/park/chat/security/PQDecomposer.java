package ru.mail.park.chat.security;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;

import java.math.BigInteger;
import java.util.Random;

/**
 * Created by Михаил on 15.04.2016.
 */
public abstract class PQDecomposer {
    public static Pair<Integer, Integer> decomposePQ(long pq) {
        int result = ferma(pq).intValue();
        return new Pair<>(result, (int) (pq / result));
    }


    public static BigInteger factorizeValue(BigInteger what) {
        BigInteger p, q;
        Random random = new Random();
        int it = 0, i, j;
        BigInteger g = BigInteger.ZERO;
        for (i = 0; i < 3 || it < 1000; i++) {
            BigInteger t = BigInteger.valueOf((random.nextLong() & 15) + 17).mod(what);
            BigInteger x = BigInteger.valueOf(random.nextLong()).mod(what.subtract(BigInteger.ONE)).add(BigInteger.ONE);
            BigInteger y = x;
            int lim = 1 << (i + 18);
            for (j = 1; j < lim; j++) {
                ++it;
                BigInteger a = x, b = x, c = t;
                while (b.compareTo(BigInteger.ZERO) != 0) {
                    if (b.and(BigInteger.ONE).compareTo(BigInteger.ZERO) > 0) {
                        c = c.add(a);
                        if (c.compareTo(what) >= 0) {
                            c = c.subtract(what);
                        }
                    }
                    a = a.add(a);
                    if (a.compareTo(what) >= 0) {
                        a = a.subtract(what);
                    }
                    b = b.shiftRight(1);
                }
                x = c;
                BigInteger z = x.compareTo(y) < 0 ? what.add(x).subtract(y) : x.add(y);
                g = z.gcd(what);
                if (g.compareTo(BigInteger.ONE) != 0) {
                    break;
                }
                if (!((j & (j - 1)) != 0)) {
                    y = x;
                }
            }
            if (g.compareTo(BigInteger.ONE) > 0 && g.compareTo(what) < 0) {
                break;
            }
        }

        if (g.compareTo(BigInteger.ONE) > 0 && g.compareTo(what) < 0) {
            p = g;
            q = what.divide(g);
            if (p.compareTo(q) > 0) {
                BigInteger tmp = p;
                p = q;
                q = tmp;
            }
            return p;
        } else {
            Log.e("factorization failed", what.toString());
            p = BigInteger.ZERO;
            q = BigInteger.ZERO;
            return p;
        }
    }

    private static BigInteger ferma(long pq) {
        long x = Math.round(Math.sqrt(pq));
        if (x*x == pq)
            return BigInteger.valueOf(x);

        for(int i = 0;;i++) {
            if (x+x-1 == pq) {
                throw new Error("error fact");
            } else {
                long x2 = x*x;
                long y = Math.round(Math.sqrt(x2 - pq));
                if (x2 - y*y == pq) {
                    return BigInteger.valueOf(pq / (x + y));
                } else {
                    x++;
                }
            }
        }
        // Log.e("error fact", pq.toString(16));
        // throw new Error("error fact");
    }

    private static BigInteger sqrt(BigInteger i) {
        return BigInteger.valueOf(Math.round(Math.sqrt(i.longValue())));
    }

    private static int decomposePQTrivial(long pq) {
        int i = 2;
        if (pq % i == 0)
            return i;
        i = 3;
        while (pq % i != 0) {
            i += 2;
        }
        return i;
    }

    private static BigInteger decomposePQNotSoTrivial(BigInteger pq) {
        BigInteger i = sqrt(pq);
        do {
            i = i.nextProbablePrime();
        } while (pq.mod(i).compareTo(BigInteger.ZERO) != 0);
        return i;
    }
}
