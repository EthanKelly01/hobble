# Hobble
### "cause it certainly won't run"<br><br>

CSCI 4020U - Compilers<br>
Professor - Ken Pu<br>

To Ken: you made a dire mistake in giving us this power<br>

---

Hobble is a language designed around the core principles of [Dreamberd](https://github.com/TodePond/DreamBerd)<br>
# Key design features

### Used Dreamberd features
- expression delimiter is '!' (instead of ';' like c, for example)
- end a line with '?' for debug information
- bools now have 3 values: true, false, and maybe. Bools take 1.5 bits to store
- variables fall into 4 categories:
  - var var: can be edited and reassigned
  - var const: can be reassigned but not edited
  - const var: can be edited but not reassigned
  - const const: can't be changed in any way

## New features
### Data
- const is a function that takes an existing variable and makes it constant
  - const functions take a **function call** and make current arguments static by reference
  - const const functions make arguments static by value
```
string name = "Bob"!
const print(name)!         //outputs "Bob"
print()!                   //outputs "Bob"
name = "Alice"!
print()!                   //outputs "Alice"
```
```
string name = "Bob"!
const const print(name)!   //outputs "Bob"
name = "Alice"!
print()!                   //outputs "Bob"
```
***NOTE:** when a variable used in a const function goes out of scope, the deconstructor casts the const function to a const const function*

- deconst makes a constant variable mutable

- bus is a new type of data representing a first-in-first-out stack of bits
  - bits must be assigned manually
  - bits must also be read manually
```
bus myBus.push(01100011010)!
//or
int myInt = 4!
myBus.push(myInt.binary)!

int newInt = myBus.pop(Int.size).toInt()!
```
- long is a function that increases the size of a variable by its default size
- short is a function to decrease the size of a variable by its default size
- longer is a function that doubles the size of a variable
- shorter is a function to halves the size of a variable (rounding up)
  - a 32 bit data type will always increase or decrease by 32 bits, even if its already been resized
  - most data types will add or remove size from the front of the variable (often the leading zeros) but bus resizes the end of the stack (most recently appended)
  - shortering a bool makes it a classic 2 value boolean
  - if a bus is too small to fit your data, you can resize it appropriately
```
bus myBus = 0!       //initialized to all 0s
print(myBus.size)!   //outputs 32

int myInt = 10!      //size: 32 bits
long myInt!          //size: 64 bits
long myInt!          //size: 96 bits

for (int i = 0! i < (myInt.size / Bus.size)! i++) {
  long myBus!        //resizes 3 times, to a total of 128 bits
}
myBus.push(myInt)!
```

### Operations

- == checks if two values are *roughly* the same
- === checks that the values match
- ==== ensures values *and* datatypes match
- ===== checks that values have the same memory address
- Use the keyword "please" to run code without safety checks (we hope you know what you're doing!)
- "nevermind" will undo all actions taken in the current scope
```
int myInt = 4!
{
  myInt = 8!
  nevermind!
  print(myInt)! //outputs "4"
}
```

### Progress

Language is still in design phase and has not begun development, watch this repo to keep up with development!
