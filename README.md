# MIPS Reduced Instruction Set Simulator

## Overview
This project is a **Java-based MIPS simulator** that interprets and executes MIPS machine code using a **single-cycle datapath**. It simulates the internal workings of a MIPS processor and accurately executes a reduced instruction set, closely mimicking the behavior of the **MARS simulator**.

## Features
- **Executes a subset of MIPS instructions** (`add`, `sub`, `lw`, `sw`, `syscall`, etc.).
- **Reads MIPS machine code from a `.text` file** and simulates execution.
- **Manages CPU state, registers, and memory** to provide an accurate simulation.
- **Supports user interaction via standard syscalls**, including:
  - Printing strings and integers.
  - Reading integers from user input.
  - Properly handling program exits.

## How It Works
The program takes two input files:
1. **Text file (`.text`)** – Contains MIPS instructions in ASCII hex format, one instruction per line.
2. **Data file (`.data`)** – Contains raw data values in ASCII hex format, one word per line.

### Execution Flow:
1. The program **reads the `.text` file** and **parses** each instruction.
2. It **updates register values** and **simulates memory interactions**.
3. Executes **syscalls** for input/output operations.
4. Maintains a **log of execution state** (registers, memory, program counter, etc.).
5. **Prints the final output**, simulating a MIPS environment.

## Installation & Usage
### **Prerequisites**
- **Java 8+**
- A terminal or command prompt

### **Running the Simulator**
```bash
java -jar MIPSsimulator.jar path/to/textfile.text path/to/datafile.data
```
Example:
```bash
java -jar MIPSsimulator.jar EvenOrOdd.text EvenOrOdd.data
```

## Why This Project?
- Designed to **deepen understanding of CPU architecture & instruction execution**.
- Useful for **learning low-level programming** and **computer organization**.
- Demonstrates ability to **build a processor simulator from scratch** using Java.

## Future Enhancements
- Expand instruction set support.
- Implement **multi-cycle and pipeline execution**.
- Add GUI for interactive debugging.

## Author
**Logan Lapierre**  
[GitHub](https://github.com/lupenox) | [LinkedIn](https://linkedin.com/in/logan-lapierre)

## License
MIT License - Free to use and modify.
