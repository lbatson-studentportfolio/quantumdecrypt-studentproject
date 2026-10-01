/*
 * Using Strange Java framework to simulate quantum computing.
 */

// What the does any of this mean.

package cmis202honors;

import java.math.BigInteger;

import javax.swing.Renderer;

import org.redfx.strange.ControlledBlockGate;
import org.redfx.strange.Program;
import org.redfx.strange.QuantumExecutionEnvironment;
import org.redfx.strange.Qubit;
import org.redfx.strange.Result;
import org.redfx.strange.Step;
import org.redfx.strange.gate.Hadamard;
import org.redfx.strange.gate.InvFourier;
import org.redfx.strange.gate.MulModulus;
import org.redfx.strange.gate.X; // Pauli-X gate (kinda like binary negation)
import org.redfx.strange.local.Computations;
import org.redfx.strange.local.SimpleQuantumExecutionEnvironment;

public class Quantum {

    // This is here because I was testing probability, probably should remove later.
    public static String doSomething() {

        String thing = null;

        int count = 0;
        QuantumExecutionEnvironment simulator = new SimpleQuantumExecutionEnvironment();
        Program program = new Program(2);
        Step step1 = new Step();
        step1.addGate(new Hadamard(0));
        step1.addGate(new Hadamard(1));
        program.addStep(step1);
        for (int index = 0; index < 1000; index++) {
            Result result = simulator.runProgram(program);
            Qubit[] qubits = result.getQubits();
            Qubit zero = qubits[0];
            Qubit one = qubits[1];
            if (zero.measure() == 1 && one.measure() == 1)
                count++; 
        }

        thing = "We calculated 1000 tosses of two coins and found that " + count + " of the tosses were both tails";

        return thing;

    }

    // publicKey[0] = modulus "n" as a product of two large primes "p" and "q"
    // publicKey[1] = encryption exponent "e"
    public static int[] findPrivateKey(int[] publicKey){

        System.out.println(""); // new line for debugging

        int[] factors = new int[2];

        do {
            factors = factor(publicKey[0]);
        } while (factors[0] == -1);

        int lambdaN = Classical.lcm(factors[0] - 1, factors[1] - 1);
        System.out.println("lambdaN = " + lambdaN);

        int decryptionExponent = Classical.extendedEuclid(publicKey[1], lambdaN)[1];
        System.out.println("decryption exponent = " + decryptionExponent);

        int[] privateKey = {publicKey[0], decryptionExponent};

        return privateKey;
        
    }

    // Quantum order (period) finding of a modular exponential function a^x mod N
    // Implementation provided by Johan Vos, who was inspired by Stephane Beauregard, who implemented a 2n + 3 qubit quantum circuit
    // of Peter Shor's polynomial time algorithm for prime factorization.
    public static int findPeriod(int a, int N) {

        // Holds bit length of N using properties of logarithms
        int length = (int) Math.ceil(Math.log(N) / Math.log(2));
        int offset = length + 1;
        Program program = new Program(2 * length + 3 + offset); // 3n + 3 qubits

        // Put first register into a hadamard transform
        Step prep = new Step();
        for (int index = 0; index < offset; index++) {
            prep.addGate(new Hadamard(index));
        }

        // Prepare register for modular exponentiation
        // Starts at one to prepare for unitary transformation of multiple modular multiplication gates
        Step prepAncilla = new Step(new X(offset));

        program.addStep(prep);
        program.addStep(prepAncilla);

        // Go through first register in reverse order
        for (int j = length - 1; j > -1; j--) {
            int mult = 1;
            // Classically hardwires a^2^j into modular multiplication
            for (int k = 0; k < (1 << j); k++) {
                mult = mult * a % N; // repeated multiplication -> exponentiation
            }
            // Create the controlled unitary transformation for ancilla register controlled by first register
            MulModulus mul = new MulModulus(length, 2 * length - 1, mult, N);
            ControlledBlockGate<MulModulus> cbg = new ControlledBlockGate<>(mul, offset, j);
            program.addStep(new Step(cbg));
        }

        // Puts first register through an inverse quantum fourier transform, resulting in a frequency domain.
        program.addStep(new Step(new InvFourier(offset, 0)));

        // Run the program, get the qubits
        QuantumExecutionEnvironment qee = new SimpleQuantumExecutionEnvironment();
        Result result = qee.runProgram(program);
        Qubit[] qubits = result.getQubits();
        
        int period = 0;
        for (int index = 0; index < offset; index++) {
            period = period + qubits[index].measure() * (1 << index); // adds a bit's actual value
        }
        
        return period;
    }

    // Functions (near) identically to Classical.factor(N) except the period finding function is contained in Quantum.
    // i.e. check out classical implementation for comments
    public static int[] factor(int N) {

        int[] factors = new int[2];
        factors[0] = -1;
        factors[1] = -1;

        // PREPROCESSING
        int a = 1 + (int) (Math.random() * (N - 1));
        System.out.println("random 1 < a < N: " + a);
        int gcd = Classical.gcd(a, N);
        System.out.println("gcd of a and N: " + gcd);

        if (gcd != 1) {
            System.out.println(a + " factors " + N);
            factors[0] = gcd;
            factors[1] = N / gcd;
            return factors;
        }

        // PERIOD FINDING
        int period = findPeriod(a, N);
        System.out.println("period of " + a + "^x mod " + N + ": " + period);

        if (period % 2 != 0) {
            System.out.println(period + " is odd, retry.");
            return factors;
        }

        // POSTPROCESSING
        BigInteger firstGuess = BigInteger.valueOf(a).pow(period/2).add(BigInteger.ONE);
        System.out.println("first guess: " + firstGuess);
        if (firstGuess.mod(BigInteger.valueOf((long) N)) == BigInteger.ZERO) {
            System.out.println("Test failed");
            return factors;
        }

        BigInteger secondGuess = BigInteger.valueOf(a).pow(period/2).subtract(BigInteger.ONE);
        System.out.println("second guess: " + secondGuess);
        int firstFactor = Classical.bigGCD(BigInteger.valueOf((long) N), secondGuess).intValue();
        factors[0] = firstFactor;
        factors[1] = N / firstFactor;
        System.out.println("factors found: p = " + factors[0] + ", q = " + factors[1]);

        return factors;

    }


    
}
