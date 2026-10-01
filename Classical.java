package cmis202honors;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

// Not to be confused with the Classic library provided by the redfx.strange package.
// Uses a quantum approach with period finding but with a classical implementation inspired by Johan Vos

public class Classical {

    // Not to be instantiated.
    private Classical() {}

    // privateKey[0] = modulus "n"
    // privateKey[1] = decryption exponent "d"
    public static int decryptCiphertext(int cipherText, int[] privateKey) {
        
        // Again, I hate that I have to do this.
        BigDecimal bigCipherText = BigDecimal.valueOf(cipherText);
        BigDecimal bigModulus = BigDecimal.valueOf(privateKey[0]);

        // Lord, please forgive me
        int message = bigCipherText.pow(Math.abs(privateKey[1])).remainder(bigModulus).intValue();

        return message;

    }

    // publicKey[0] = modulus "n" as a product of two large primes "p" and "q"
    // publicKey[1] = encryption exponent "e"
    public static int[] findPrivateKey(int[] publicKey) {

        System.out.println(""); // new line for debugging

        int[] factors = new int[2];

        // Factor modulus N until successful.
        do {
            factors = factor(publicKey[0]);
        }
        while (factors[0] == -1);

        // Compute carmichael totient of N
        int lambdaN = lcm(factors[0] - 1, factors[1] - 1);
        System.out.println("lambdaN = " + lambdaN);

        // Compute decryption exponent
        int decryptionExponent = extendedEuclid(publicKey[1], lambdaN)[1];
        System.out.println("decryption exponent = " + decryptionExponent);

        int[] privateKey = {publicKey[0], decryptionExponent};

        return privateKey;
    }

    // Factors a number (the cooler one)
    public static int[] factor(int N) {

        // Preset to -1 for both so that returning an error is obvious and I don't have to make guard clauses longer than I need to.
        int[] factors = new int[2];
        factors[0] = -1;
        factors[1] = -1;

        // Find a random number A
        int a = 1 + (int) (Math.random() * (N - 1));
        System.out.println("random number a: " + a);
        int gcd = gcd(N, a);
        System.out.println("gcd of " + a + " and " + N + " = " + gcd);
        
        // if a is a factor of N, cool! we're done!
        if (gcd != 1) {
            factors[0] = gcd;
            factors[1] = N / gcd;
            System.out.println("factors found: p = " + factors[0] + ", q = " + factors[1]);
            return factors;
        }

        // Else, find the period of a modular exponential function a^x mod N
        int period = findPeriod(a, N);
        System.out.println("period of " + a + "^x mod " + N + ": " + period);

        // We cannot use odd periods because we would end up with a non-integer result (I think? pls check this out later)
        if (period % 2 != 0) {
            System.out.println("test failed, " + period + " is not even.");
            return factors;
        }

        // Okay, turns out, these numbers can get MASSIVE.
        BigInteger firstGuess = BigInteger.valueOf(a).pow(period/2).add(BigInteger.ONE);
        System.out.println("first guess: " + firstGuess);
        if (firstGuess.mod(BigInteger.valueOf((long) N)) == BigInteger.ZERO) { // this is egregious.
            System.out.println("Test failed");
            return factors; // not a factor of N
        }

        BigInteger secondGuess = BigInteger.valueOf(a).pow(period/2).subtract(BigInteger.ONE);
        System.out.println("second guess: " + secondGuess);
        int firstFactor = bigGCD(BigInteger.valueOf((long) N), secondGuess).intValue();
        factors[0] = firstFactor;
        factors[1] = N / firstFactor;
        System.out.println("factors found: p = " + factors[0] + ", q = " + factors[1]);

        return factors;

    }

    // Factors a number (sort of brute force)
    public static int[] factorNaive(int number) {

        int[] factors = new int[2];
        int attempts = 1;
        int maxAttempts = (int) Math.sqrt(number);

        // starts at one, goes to the square root of the number.
        while (attempts++ < maxAttempts) {
            if (number % attempts == 0) break;
        }

        factors[0] = attempts;
        factors[1] = number / attempts;

        return factors;

    }

    // Returns gcd and bezout coefficients of ax + by = gcd(a, b)
    // result[0] = gcd(a, b)
    // result[1] = x
    // result[2] = y
    public static int[] extendedEuclid(int a, int b) {

        int x = 0, y = 1;
        int lastX = 1, lastY = 0;
        int temp = 0; // for swapping variables;

        while (b != 0) {
            int quotient = a / b;
            int remainder = a % b;

            a = b;
            b = remainder;

            temp = x;
            x = lastX - quotient * x;
            lastX = temp;

            temp = y;
            y = lastY - quotient * y;
            lastY = temp;
        }

        return new int[]{
            a, // gcd
            lastX, // x
            lastY // y
        };

    }

    // Based on the goat Euclid
    // Divide a pair of numbers recursively by the remainder until the remainder is zero.
    public static int gcd(int a, int b) {
        if (a % b == 0) return (int) b;
        return gcd(b, a % b);
    }

    // chunky version of gcd. I hate that I had to make this.
    public static BigInteger bigGCD(BigInteger a, BigInteger b) {
        if (a.mod(b) == BigInteger.ZERO) return b;
        return bigGCD(b, a.mod(b));
    }

    public static int lcm(int a, int b) {

        int numerator = Math.abs(a * b);
        int denominator = gcd(a, b);

        return numerator / denominator;

    }

    // Returns the period of a modular exponential function a^x mod N
    public static int findPeriod(int a, int N) {
        int period = 1; // the periodicity of a function is at least 1.
        long result = (long) (Math.pow(a, period)) % N;

        // to account for large evaluations
        BigInteger bigN = BigInteger.valueOf(N);
        BigInteger bigA = BigInteger.valueOf(a);

        // Evaluate until we get an equal result from a different period.
        while (result != 1) {
            period++;
            BigInteger newResult = bigA.pow(period).mod(bigN);
            result = newResult.longValue();
        }
        return period;
    }

}
