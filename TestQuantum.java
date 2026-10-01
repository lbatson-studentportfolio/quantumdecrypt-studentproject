package cmis202honors;

import java.math.BigInteger;

import javax.sound.sampled.Control;
import javax.swing.Renderer;

import org.redfx.strange.Complex;
import org.redfx.strange.ControlledBlockGate;
import org.redfx.strange.Program;
import org.redfx.strange.QuantumExecutionEnvironment;
import org.redfx.strange.Qubit;
import org.redfx.strange.Result;
import org.redfx.strange.Step;
import org.redfx.strange.algorithm.Classic;
import org.redfx.strange.gate.Add;
import org.redfx.strange.gate.AddModulus;
import org.redfx.strange.gate.Cnot;
import org.redfx.strange.gate.Fourier;
import org.redfx.strange.gate.Hadamard;
import org.redfx.strange.gate.InvFourier;
import org.redfx.strange.gate.Measurement;
import org.redfx.strange.gate.MulModulus;
import org.redfx.strange.gate.Oracle;
import org.redfx.strange.gate.Toffoli;
import org.redfx.strange.gate.X;
import org.redfx.strange.local.SimpleQuantumExecutionEnvironment;

public class TestQuantum {

    // Based on Stephane Beauregard's 2n + 3 qubit quantum circuit except Strange sucks and now I have to hand-write all
    // of the (reversable) quantum gates myself. god damn it.
    // I give up
    public static int customBeauregardFindPeriod(int a, int N) {

        int length = (int) Math.ceil(Math.log(N) / Math.log(2));

        int k = 1; // placeholder until I can figure out how to return new phase shift gates
        double phase = (2 * Math.PI) / Math.pow(2, k);
        Complex phaseFactor = Complex.HC; // whatever this is
        Complex[][] phaseShiftGate = {{Complex.ONE, Complex.ZERO},
                                    {Complex.ZERO, phaseFactor}};
        Complex[][] controlledPhaseShiftGate = {{Complex.ONE, Complex.ZERO, Complex.ZERO, Complex.ZERO},
                                                {Complex.ZERO, Complex.ONE, Complex.ZERO, Complex.ZERO},
                                                {Complex.ZERO, Complex.ZERO, Complex.ONE, Complex.ZERO},
                                                {Complex.ZERO, Complex.ZERO, Complex.ZERO, phaseFactor}};

        Program program = new Program(2 * length + 3);

        

        return 0;
        
    }

    // doesn't work
    // well, I tried my best and it wasn't good enough.
    public static int shorFindPeriod(int a, int N) {

        int length = (int) Math.ceil(Math.log(N) / Math.log(2));

        Program program = new Program(3 * length);

        Step prep = new Step();
        
        // Hadamard transform on top register
        for (int index = 0; index < 2 * length - 1; index++) {
            prep.addGate(new Hadamard(index));
        }

        // Initialize bottom register's first qubit to |1> (in total makes bottom register |1>)
        prep.addGate(new X(2 * length));

        program.addStep(prep);

        // DEBUG
        // QuantumExecutionEnvironment qee = new SimpleQuantumExecutionEnvironment();
        // Result result = qee.runProgram(program);
        // 

        // In reverse order (bottom to top) control the unitary transformation Ua (which is basically just the MulModulus)
        for (int index = 2 * length - 1; index > 0; index--) {
            MulModulus Ua = new MulModulus(2 * length, 3 * length - 1, (int) Math.pow((int) Math.pow(a, 2), (2 * length - 1) - index), N);
            ControlledBlockGate<MulModulus> cbg = new ControlledBlockGate<>(Ua, 2 * length, index);
            program.addStep(new Step(cbg));
        }

        // Apply inverse quantum fourier transform to top register to transfer the results to a frequency domain
        program.addStep(new Step(new InvFourier(2 * length, 0)));

        // Result of program
        QuantumExecutionEnvironment qee = new SimpleQuantumExecutionEnvironment();
        Result result = qee.runProgram(program);
        Qubit[] qubits = result.getQubits();

        // Measure qubits of top register and extract period of modular exponential function
        int period = 0;
        for (int index = 0; index < 2 * length; index++) {
            period = period + qubits[index].measure()*(1 << index); // 2^index * measured value = actual value of bit
        }

        return 0;

    }

    public static int classicFindPeriod(int a, int N) {

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

    public static int johanFindPeriod(int a, int N) {

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

    

    // Why couldn't I just have a NORMAL project? Instead I learned quantum computing. What the hell.
    public static void randomBit() {

        Program program = new Program(1);
        Step step = new Step(new Hadamard(0));
        program.addStep(step);

        QuantumExecutionEnvironment qee = new SimpleQuantumExecutionEnvironment();
        Result result = qee.runProgram(program);
        Qubit[] qubits = result.getQubits();

        System.out.println(qubits[0].measure());
        

    }

    // same thing as below but using superposition to keep key secure. Like that's ever gonna happen.
    public static void secureQuantumKeyGen(boolean[] aliceBits) {

        final int SIZE = aliceBits.length;

        QuantumExecutionEnvironment simulator = new SimpleQuantumExecutionEnvironment();
        Program program = new Program(SIZE);
        Step init = new Step();
        Step step1 = new Step();
        Step step2 = new Step();
        Step step3 = new Step();
        for (int index = 0; index < SIZE; index++) {
            if (aliceBits[index]) init.addGate(new X(index));
            step1.addGate(new Hadamard(index));
            step2.addGate(new Hadamard(index));
            step3.addGate(new Measurement(index));
        }

        program.addSteps(init, step1, step2, step3);

        int[] measurements = new int[SIZE];
        boolean[] bobBits = new boolean[SIZE];
        Result result = simulator.runProgram(program);
        Qubit[] qubits = result.getQubits();

        for (int index = 0; index < SIZE; index++) {
            measurements[index] = qubits[index].measure();
            bobBits[index] = measurements[index] == 1;
            System.out.println("Alice sent " + (aliceBits[index] ? "1" : "0")
                                + " and Bob received " + (bobBits[index] ? "1" : "0"));
        }

        
    }

    // Alice = sender, Bob = receiver. Prints what is sent and what is received.
    public static void naiveQuantumKeyGen(boolean[] aliceBits) {

        final int SIZE = aliceBits.length;

        QuantumExecutionEnvironment simulator = new SimpleQuantumExecutionEnvironment();
        Program program = new Program(SIZE);
        Step step1 = new Step();
        Step step2 = new Step();

        for (int index = 0; index < SIZE; index++) {
            if (aliceBits[index]) step1.addGate(new X(index));
            step2.addGate(new Measurement(index));
        }

        program.addStep(step1);
        program.addStep(step2);
        Result result = simulator.runProgram(program);
        Qubit[] qubits = result.getQubits();
        boolean[] bobBits = new boolean[SIZE];
        int[] measurements = new int[SIZE];

        for (int index = 0; index < SIZE; index++) {
            measurements[index] = qubits[index].measure();
            bobBits[index] = measurements[index] == 1;

            System.out.println("Alice sent " + (aliceBits[index] ? "1" : "0")
                                + " and Bob received " + (bobBits[index] ? "1" : "0"));
        }

        

    }

    // only for one-qubit arithmetic
    public static int add(int a, int b) {

        Program program = new Program(3);
        Step init = new Step();
        if (a > 0) {
            init.addGate(new X(0));
        }
        if (b > 0) {
            init.addGate(new X(1));
        }
        Step step1 = new Step(new Toffoli(0, 1, 2));
        Step step2 = new Step(new Cnot(0, 1));

        program.addSteps(init, step1, step2);
        QuantumExecutionEnvironment qee = new SimpleQuantumExecutionEnvironment();
        Result result = qee.runProgram(program);
        Qubit[] qubits = result.getQubits();

        

        return qubits[1].measure() + (qubits[2].measure() << 1);

    }

    // Never understood this. Probably better that way.
    public static int oracleExample() {

        Program program = new Program(2);
        QuantumExecutionEnvironment simulator = new SimpleQuantumExecutionEnvironment();

        Complex[][] matrix = {
            {Complex.ONE, Complex.ZERO, Complex.ZERO, Complex.ZERO},
            {Complex.ZERO, Complex.ONE, Complex.ZERO, Complex.ZERO},
            {Complex.ZERO, Complex.ZERO, Complex.ZERO, Complex.ONE},
            {Complex.ZERO, Complex.ZERO, Complex.ONE, Complex.ZERO}
        };

        Oracle oracle = new Oracle(matrix);
        Step step1 = new Step(new Hadamard(1));
        Step step2 = new Step(oracle);
        program.addSteps(step1, step2);
        Result result = simulator.runProgram(program);

        return 0;

    }

    
    
}
