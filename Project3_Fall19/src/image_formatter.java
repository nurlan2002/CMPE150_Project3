
import java.io.*;
import java.util.*;
	
public class image_formatter {
	/*
	 * @author Nurlan Dadashov
	 * @course CMPE 150.06
	 * @Assignment_3
	 * @due January 05 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		// mode variable represents which functionality of this program will be used
		int mode = Integer.parseInt(args[0]);
		// input file
		File file = new File(args[1]);
		
		Scanner input = new Scanner(file);
		// str - image format (P3) 
		String str = input.nextLine(); 
		Scanner input2 = new Scanner(input.nextLine());
		// height - number of rows
		int height = input2.nextInt();
		// width - number of columns
		int width = input2.nextInt();
		
		input2.close();
		// maxRange - maximum color value
		int maxRange = Integer.parseInt(input.nextLine());
		// PPM file will be converted into a 3D array result
		int[][][] result = new int[width][height][3];
		// result[# of rows][# of columns][3 color channels]
		//Converting PPM image into an array
		for(int i = 0; i < height; i++) {
			String s = input.nextLine();
			Scanner line = new Scanner(s);
			for(int j = 0; j < width; j++) {
				for(int k = 0; k < 3; k++) {
					result[i][j][k] = line.nextInt();
				}
			}
			line.close();
		}
		
		if(mode == 0) { // mode0
			mode0(result, "output.ppm", str, width, height, maxRange);
		}
		else if(mode == 1) { // mode1
			mode1(result, "black-and-white.ppm", str, width, height, maxRange);
		}
		else if(mode == 2) { //mode2
			File filterfile = new File(args[2]); // creating filter file
			mode2(filterfile, result, "convolution.ppm", str, width, height, maxRange);
		}
		if(mode == 3) { // mode3
			int range = Integer.parseInt(args[2]); // declaring range
			mode3(range, result, "quantized.ppm", str, width, height, maxRange);
		}
		
		input.close();
	}
	
	public static void mode0(int[][][] arr, String outputName, String str,int width, int height, int maxRange) throws FileNotFoundException {
		PrintStream output = new  PrintStream(outputName);
		output.println(str);
		output.println(width + " " + height);
		output.println(maxRange);
		// writing elements of the array into a new file
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				for(int k = 0; k < 3; k++) {
					if(k<2)	{
						output.print(arr[i][j][k] + " ");
					}
					else {
						output.print(arr[i][j][k]);
					}	
				}
				output.print("\t");
			}
			output.println(); 
		}
		output.close();
	}
	
	public static void mode1(int[][][] arr, String outputName, String str,int width, int height, int maxRange) throws FileNotFoundException {
		PrintStream output = new  PrintStream(outputName);
		output.println(str);
		output.println(width + " " + height);
		output.println(maxRange);
		// Creating black-and-white version by calculating average of RGB
		// writing new values to the output file
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				for(int k = 0; k < 3; k++) {
					int avg = (arr[i][j][0] + arr[i][j][1] + arr[i][j][2]) / 3;
					output.print(avg + " ");
				}
				output.print("\t");
			}
			output.println();
		}
		output.close();
	}
	
	public static void mode2(File filterfile, int[][][] arr, String outputName, String str,int width, int height, int maxRange) throws FileNotFoundException {
		Scanner line = new Scanner(filterfile);
		String s = line.nextLine();
		// a - length of the filter
		int a = Integer.parseInt(s.substring(0, s.indexOf("x")));
		// filter - array containing filter values
		int[][] filter = new int[a][a];
		// converting filter file into filter array
		for(int i = 0; i < a; i++) {
			String s1 = line.nextLine();
			Scanner line1 = new Scanner(s1);
			for(int j = 0; j < a; j++) {
				filter[i][j] = line1.nextInt();
			}
			line1.close();
		}
		// newarr - a smaller array which will contain the values after convolution
		int[][][] newarr = new int[width - a / 2 * 2][height - a / 2 * 2][3];
		// convolution process
		int sum = 0;
		for(int i = 0; i + a <= height; i++) {
			for(int j = 0; j + a <= width; j++) {
				sum = 0;
				for(int k = 0; k < 3; k++) {
					sum = 0;
					for(int l = i; l < i + a; l++) {
						for(int m = j; m < j + a; m++) {
							int num1 = arr[l][m][k];
							int num2 = filter[l - i][m - j];
							sum += num1 * num2;
						}
					}
					if(sum >= 0 && sum <= 255) {
						newarr[i][j][k] = sum;
					}
					// if value is negative = 0
					else if(sum < 0) {
						newarr[i][j][k] = 0;

					}
					// if value is >255 then = 255 
					else if(sum > 255) {
						newarr[i][j][k] = 255;
					}
				}
			}
		}
		// newarr2 - black-and-white version of the newarr
		int[][][] newarr2 = new int[width - a / 3 * 2][height - a / 3 * 2][3];
		// final image of mode2
		for(int i = 0; i < height - a / 3 * 2; i++) {
			for(int j = 0; j < width - a / 3 * 2; j++) {
				for(int k = 0; k < 3; k++) {
					int avg = (newarr[j][i][0] + newarr[j][i][1] + newarr[j][i][2]) / 3;
					newarr2[j][i][k] = avg;
				}
			}
		}
		// printing final image
		mode0(newarr2, outputName, str, width - a / 3 * 2, height - a / 3 * 2, maxRange);
		line.close();
	}
	
	public static void mode3(int range, int[][][] arr, String outputName, String str,int width, int height, int maxRange) throws FileNotFoundException {
		//sol - array to keep track of which pixels had been changed 
		int[][][] sol = new int[width][height][3]; 
		// calling recursive method for all pixels row by row then channel by channel
		for(int z = 0; z < 3; z++) {
    		for(int x = 0; x < height; x++) {
    			for(int y = 0; y < width; y++) {
    				// recursive method for changing pixel values
    				recursiveMethod(x,y,z, width, height, range, 0, sol, arr);
    			}
    		}
    	}
		// printing final image
		mode0(arr, outputName, str, width, height, maxRange);
	}
	
	public static void recursiveMethod(int x, int y, int z, int width, int height, int range, int road, int[][][] sol, int[][][] result) { 
    	
    	switch(road) {
    	case 0:
    		if(result[x][y][z] < result[x][y][z] - range || result[x][y][z] > result[x][y][z] + range) {
    			return;
    		}
        	else {
        	// changing the value of the pixel
    		result[x][y][z] = result[x][y][z];
    		// marking pixel as changed
			sol[x][y][z] = 1;
        	}
    		break;
    	case 1: // moving in x+1 direction
    		// return if the pixel is out of range
        	if(result[x][y][z] < result[x-1][y][z] - range || result[x][y][z] > result[x-1][y][z] + range) {
    			return;
    		}
        	else {
        	// changing the value of the pixel
    		result[x][y][z] = result[x-1][y][z];
    		// marking pixel as changed
			sol[x][y][z] = 1;
        	}
    		break;
    	case 2: // moving in x-1 direction
    		// return if the pixel is out of range
    		if(result[x][y][z] < result[x+1][y][z] - range || result[x][y][z] > result[x+1][y][z] + range) {
    			return;
    		}
    		else {
    		// changing the value of the pixel
    		result[x][y][z] = result[x+1][y][z];
    		// marking pixel as changed
			sol[x][y][z] = 1;
    		}
    		break;
    	case 3: // moving in y+1 direction
    		// return if the pixel is out of range
    		if(result[x][y][z] < result[x][y-1][z] - range || result[x][y][z] > result[x][y-1][z] + range) {
    			return;
    		}
    		else {
    		// changing the value of the pixel
    		result[x][y][z] = result[x][y-1][z];
    		// marking pixel as changed
			sol[x][y][z] = 1;
    		}
    		break;
    	case 4: // moving in y-1 direction
    		// return if the pixel is out of range
    		if(result[x][y][z] < result[x][y+1][z] - range || result[x][y][z] > result[x][y+1][z] + range) {
    			return;
    		}
    		else {
    		// changing the value of the pixel
    		result[x][y][z] = result[x][y+1][z];
    		// marking pixel as changed
			sol[x][y][z] = 1;
    		}
    		break;
    	case 5: // moving in z+1 direction
    		// return if the pixel is out of range
    		if(result[x][y][z] < result[x][y][z-1] - range || result[x][y][z] > result[x][y][z-1] + range) {
    			return;
    		}
    		else {
    		// changing the value of the pixel
    		result[x][y][z] = result[x][y][z-1];
    		// marking pixel as changed
			sol[x][y][z] = 1;
    		}
    		break;
    	case 6: // moving in z-1 direction
    		// return if the pixel is out of range
    		if(result[x][y][z] < result[x][y][z+1] - range || result[x][y][z] > result[x][y][z+1] + range) {
    			return;
    		}
    		else {
    		// changing the value of the pixel
    		result[x][y][z] = result[x][y][z+1];
    		// marking pixel as changed
			sol[x][y][z] = 1;
    		break;
    		}
    	}
    	// checking if x+1 direction is available
    	if(check(x+1,y,z,width,height, sol))
    		recursiveMethod(x+1, y, z, width, height, range,1, sol, result);
    	// checking if x-1 direction is available
    	if(check(x-1,y,z,width,height, sol))
    		recursiveMethod(x-1, y, z, width, height, range,2, sol, result);
    	// checking if y+1 direction is available
    	if(check(x,y+1,z,width,height, sol))
    		recursiveMethod(x, y+1, z, width, height, range,3, sol, result);
    	// checking if y-1 direction is available
    	if(check(x,y-1,z,width,height, sol))
    		recursiveMethod(x, y-1, z, width, height, range,4, sol, result);
    	// checking if z+1 direction is available
    	if(check(x,y,z+1,width,height, sol))
    		recursiveMethod(x, y, z+1, width, height, range,5, sol, result);
    	// checking if z-1 direction is available
    	if(check(x,y,z-1,width,height, sol))
    		recursiveMethod(x, y, z-1, width, height, range,6, sol, result);
    }
	// checking if the pixel is in bounds of the image and whether its value was changed or not
	public static boolean check(int x, int y, int z, int width, int height, int[][][] sol) 
    {   
        return ((x >= 0 && x < width && y >= 0 && y < height && z >= 0 && z < 3) && sol[x][y][z] == 0); 
    } 
}