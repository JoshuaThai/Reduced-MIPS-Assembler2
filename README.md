# Reduced MIPS Assembler (Milestone 2)

This is the second milestone for my Reduced MIPS Assembler project, which I developed with Abdul Wahab Malik
for our Computer Architecture course during my junior year at the University of Wisconsin - Milwaukee.
This is the final milestone for this project. It builds on the first milestone to simulate the
MARS MIPS simulator through generating a .data and .text file associated with the
assembly program being run.
# What does it do?
Basically, the program will read an assembly file that the user will input into the terminal as an argument. It will generate a .text and .data files similar to the ones generated in MARS, a MIPS assembly simulator.
# How to get it to work?
It is recommended that you use Intellij to run this program. However, other IDE should be able to run this program as well. 

You will first need to build the project. Upon building, you will generate an out folder. Navigate through the out folder to the artifacts subfolder,
and then to the jar subfolder. Inside the jar subfolder, you will put the assembly file you want to test into there. Upon doing that, you should input the line into the terminal like in the example below:
```
java -jar < Name of jar >.jar "test.asm"
```
That line should work on all OS!
