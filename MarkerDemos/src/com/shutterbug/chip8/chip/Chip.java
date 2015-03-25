package com.shutterbug.chip8.chip;
import java.io.*;
import com.shutterbug.chip8.*;
import java.lang.Character;
import java.util.*;


/**
 * The class which contains the opcodes and data for the processor.
 */
public class Chip
{
//	public Opcodetoast optoast = new Opcodetoast();
	
	private boolean redrawFlag;
	
	/**
	 * 4kB of 8-bit memory<br/>
	 * At position 0x50: The "bios" fontset
	 * At position 0x200: The start of every program
	 */
	private char[] memory;
	/**
	 * 16 8-bit registers.<br/>
	 * They will be used to store data which is used in several operation<br/>
	 * Register 0xF is used for Carry, Borrow and collision detection
	 */
	private char[] V;
	/**
	 * 16-bit (only 12 are used) to point to a specific point in the memory
	 */
	private char I;
	/**
	 * The 16-bit (only 12 are used) to point to the current operation
	 */
	private char pc;

	/**
	 * Subroutine callstack<br/>
	 * Allows up to 16 levels of nesting
	 */
	private char stack[];
	/**
	 * Points to the next free slot int the stack
	 */
	private int stackPointer;

	/**
	 * This timer is used to delay events in programs/games
	 */
	private int delay_timer;
	/**
	 * This timer is used to make a beeping sound
	 */
	private int sound_timer;

	/**
	 * This array will be our keyboard state
	 */
	private byte[] keys;

	/**
	 * The 64x32 pixel monochrome (black/white) display
	 */
	private byte[] display;

	/**
	 * Reset the Chip 8 memory and pointers
	 */
	public void init() {
		memory = new char[4096];
		V = new char[16];
		I = 0x0;
		pc = 0x200;

		stack = new char[16];
		stackPointer = 0;

		delay_timer = 0;
		sound_timer = 0;

		keys = new byte[16];

		display = new byte[64 * 32];
	}
	
	public void run() {
		//fetch Opcode
		char opcode = (char)((memory[pc] << 8) | memory[pc + 1]);
		System.out.print(Integer.toHexString(opcode) + ": ");
		//decode opcode
		switch(opcode & 0xF000) {
			
			case 0x0000:
				switch(opcode & 0x00FF) {
			case 0x00EE: //00EE: Returns from subroutine
				stackPointer--;
				pc = (char)(stack[stackPointer] + 2);
				System.out.println("Returning to " + Integer.toHexString(pc).toUpperCase());
				break;
				}
				break;
			case 0x1000: {
					int nnn = opcode & 0x0FFF;
					pc = (char)nnn;
					break;
				}
			
			case 0x2000:
					stack[stackPointer] = pc;
					stackPointer++;
					pc = (char)(opcode & 0x0FFF);
					break;
					
			case 0x3000: {
					int x = (opcode & 0x0F00) >> 8;
					int nn = (opcode & 0x00FF);
					if(V[x] == nn) {
						pc += 4;
					} else {
						pc += 2;
					}
					break;
				}
					
			case 0x6000:
				I = (char)(opcode & 0x0fff);
				pc += 2;
				break;
				
			case 0xa000:
				V[(char)(opcode & 0x0F00) >> 8] = (char)(opcode & 0x00FF);
				pc += 2;
				break;
				
			case 0x7000:{
				int x = (opcode & 0x0F00) >> 8;
				int nn = (opcode & 0x00FF);
				V[x] = (char)((V[x] + nn) & 0xFF);
				pc += 2;
				break;
				}
			case 0x8000: //Contains more data in last nibble

				switch(opcode & 0x000F) {

					case 0x0000: //8XY0: Sets VX to the value of VY.
					default:
//						Log.d("Opcode info","Unsupported Opcode! " + Integer.toHexString(opcode));
//						optoast.toastdisplay(opcode, c);
						System.exit(0);
						break;
				}

				break;
				
			case 0xC000: { //CXNN: Set VX to a random number and NN
					int x = (opcode & 0x0F00) >> 8;
					int nn = (opcode & 0x00FF);
					int randomNumber = new Random().nextInt(255) & nn;
					System.out.println("V[" + x + "] has been set to (randomised) " + randomNumber);
					V[x] = (char)randomNumber;
					pc += 2;
					break;
					}
				
			case 0xD000: { //DXYN: Draw a sprite (X, Y) size (8, N). Sprite is located at I
					int x = V[(opcode & 0x0F00) >> 8];
					int y = V[(opcode & 0x00F0) >> 4];
					int height = opcode & 0x000F;

					V[0xF] = 0;

					for(int _y = 0; _y < height; _y++) {
						int line = memory[I + _y];
						for(int _x = 0; _x < 8; _x++) {
							int pixel = line & (0x80 >> _x);
							if(pixel != 0) {
								int totalX = x + _x;
								int totalY = y + _y;
								int index = totalY * 64 + totalX;

								if(display[index] == 1)
									V[0xF] = 1;

								display[index] ^= 1;
							}
						}
					}
					pc += 2;
					redrawFlag = true;
					break;
					}
					case 0xF000:
			case 0x0033: { //FX33 Store a binary-coded decimal value VX in I, I + 1 and I + 2
					int x = (opcode & 0x0F00) >> 8;
					int value = V[x];
					int hundreds = (value - (value % 100)) / 100;
					value -= hundreds * 100;
					int tens = (value - (value % 10))/ 10;
					value -= tens * 10;
					memory[I] = (char)hundreds;
					memory[I + 1] = (char)tens;
					memory[I + 2] = (char)value;
					System.out.println("Storing Binary-Coded Decimal V[" + x + "] = " + (int)(V[(opcode & 0x0F00) >> 8]) + " as { " + hundreds+ ", " + tens + ", " + value + "}");
					pc += 2;
					break;
				}
				
			default:
//				Log.d("Opcode info","Unsupported Opcode! " + Integer.toHexString(opcode));
				System.exit(0);
		}
		}
	
	public void loadProgram(String file) {
		DataInputStream input = null;
		try {
			input = new DataInputStream(new FileInputStream(new File(file)));

			int offset = 0;
			while(input.available() > 0) {
				memory[0x200 + offset] = (char)(input.readByte() & 0xFF);
				offset++;
			}

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		} finally {
			if(input != null) {
				try { input.close(); } catch (IOException ex) {}
			}
		}
		}
		public byte[] getDisplay(){
			return display;
		}
		public boolean needsRedraw(){
		return redrawFlag;
		}
	public void removeRedrawFlag(){
		redrawFlag = false;
		}
}
