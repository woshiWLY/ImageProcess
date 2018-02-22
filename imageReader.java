import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import java.util.*;

public class imageReader {

	JFrame frame;
	JLabel lbIm1;
	JLabel lbIm2;
	BufferedImage img;
	BufferedImage newImg;
	int quanSize = 4;
	int[] rQ = new int[quanSize];
	int[] gQ = new int[quanSize];
	int[] bQ = new int[quanSize];
	
	/*int[] rQ = {10, 200};
	int[] gQ = {5, 250};
	int[] bQ = {30, 192};*/
	
	int[] rVal = new int[256];
	int[] gVal = new int[256];
	int[] bVal = new int[256];
	
	public final int WIDTH = 352;
	public final int HEIGHT = 288;
	public final int TOTAL = WIDTH*HEIGHT;
	public final double[][] RGB2YUV_M = {
			{0.299, 0.587, 0.114},
			{0.596, -0.274, -0.322},
			{0.211, -0.523, 0.312}
	};
	public final double[][] YUV2RGB_M = {
			{1.000, 0.956, 0.612},
			{1.000, -0.272, -0.647},
			{1.000, -1.106, 1.703}
	};
	
	private int calError(byte[] inputRGB, double[] outputRGB) {
		int ret = 0;
		for(int i = 0; i < TOTAL; i++) {
			int iR = (int)inputRGB[i];
			int iG = (int)inputRGB[i+TOTAL];
			int iB = (int)inputRGB[i+TOTAL*2];
			
			int oR = (int)outputRGB[i];
			int oG = (int)outputRGB[i+TOTAL];
			int oB = (int)outputRGB[i+TOTAL*2];
			
			ret = ret + Math.abs(oR - iR)+Math.abs(oG - iG)+Math.abs(oB - iB);
		}
		
		return ret;
	}
	
	private void calQuanLevel() {
		int r1 = 0;
		int fir = 0;
		int gap = 256 / quanSize;
		
		for(int i = 0; i < quanSize; i++ ) {
			for(int j = 0; j < gap; j++) {
				int val = i*gap+j;
				if(val > 255)
					break;
						
				r1 += val*rVal[val];
				fir += rVal[val];
			}
			rQ[i] = r1/fir;
			r1 = 0;
			fir = 0;
		}
		
		
		for(int i = 0; i < quanSize; i++ ) {
			for(int j = 0; j < gap; j++) {
				int val = i*gap+j;
				if(val > 255)
					break;
						
				r1 += val*gVal[val];
				fir += gVal[val];
			}
			gQ[i] = r1/fir;
			r1 = 0;
			fir = 0;
		}
		
		for(int i = 0; i < quanSize; i++ ) {
			for(int j = 0; j < gap; j++) {
				int val = i*gap+j;
				if(val > 255)
					break;
						
				r1 += val*bVal[val];
				fir += bVal[val];
			}
			bQ[i] = r1/fir;
			r1 = 0;
			fir = 0;
		}
		
	}
	
	private void addQuanMap(int val, int color) {
		if(color == 0) {
			//r
			rVal[val]++;
		}else if(color == 1) {
			//g
			gVal[val]++;
		}else if(color == 2) {
			//b
			bVal[val]++;
		}
	}
	
	public double[] RGB2YUV(byte[] rgb){
		double[] ret = new double[WIDTH*HEIGHT*3];
		for(int i = 0; i < TOTAL; i++) {
			double r = (double)rgb[i];
			double g = (double)rgb[i+TOTAL];
			double b = (double)rgb[i+2*TOTAL];
			
			//change range from -128.0 ~ 127 to 0~255
			r = r < 0 ? r + 256.0 : r;
			g = g < 0 ? g + 256.0 : g;
			b = b < 0 ? b + 256.0 : b;
			
			//System.out.println("r = "+r+" g = "+g+" b = "+b);
		
			ret[i] = RGB2YUV_M[0][0]*r + RGB2YUV_M[0][1]*g + RGB2YUV_M[0][2]*b;     		//Y
			ret[i+TOTAL] = RGB2YUV_M[1][0]*r + RGB2YUV_M[1][1]*g + RGB2YUV_M[1][2]*b;   	//U
			ret[i+TOTAL*2] = RGB2YUV_M[2][0]*r + RGB2YUV_M[2][1]*g + RGB2YUV_M[2][2]*b; 	//V
			
			
		}

		return ret;
	}
	
	public double[] YUV2RGB(double[] yuv){
		double[] ret = new double[WIDTH*HEIGHT*3];
		for(int i = 0; i < TOTAL; i++){
			double y = yuv[i];
			double u = yuv[i+TOTAL];
			double v = yuv[i+TOTAL*2];
			
			ret[i] = YUV2RGB_M[0][0]*y + YUV2RGB_M[0][1]*u + YUV2RGB_M[0][2]*v;			//R
			ret[i+TOTAL] = YUV2RGB_M[1][0]*y + YUV2RGB_M[1][1]*u + YUV2RGB_M[1][2]*v;		//G
			ret[i+TOTAL*2] = YUV2RGB_M[2][0]*y + YUV2RGB_M[2][1]*u + YUV2RGB_M[2][2]*v;	//B
			//limit the rgb value to 0~255
			ret[i] = ret[i] > 255.0 ? 255.0: (ret[i] < 0.0 ? 0.0 : ret[i]);
			ret[i+TOTAL] = ret[i+TOTAL] > 255.0 ? 255: (ret[i+TOTAL] < 0.0 ? 0.0 : ret[i+TOTAL]);
			ret[i+TOTAL*2] = ret[i+TOTAL*2] > 255.0 ? 255.0: (ret[i+TOTAL*2] < 0.0 ? 0.0 : ret[i+TOTAL*2]);
			
			//quantization
			ret[i] = Math.round(ret[i]);
			ret[i+TOTAL] = Math.round(ret[i+TOTAL]);
			ret[i+TOTAL*2] = Math.round(ret[i+TOTAL*2]);
			
			addQuanMap((int)ret[i], 0);
			addQuanMap((int)ret[i+TOTAL], 1);
			addQuanMap((int)ret[i+TOTAL*2], 2);
			//System.out.println("r = "+ret[i]+" g = "+ret[i+TOTAL]+" b = "+ret[i+TOTAL*2]);
			
			//if(quanSize != 256) {
				//System.out.println("r = "+ret[i]+" g = "+ret[i+TOTAL]+" b = "+ret[i+TOTAL*2]);
				/*ret[i] = quantizeValue(ret[i], quanSize);
				ret[i+TOTAL] = quantizeValue(ret[i+TOTAL], quanSize);
				ret[i+TOTAL*2] = quantizeValue(ret[i+TOTAL*2], quanSize);*/
				//System.out.println("r = "+ret[i]+" g = "+ret[i+TOTAL]+" b = "+ret[i+TOTAL*2]);
				//System.out.println("------------------------------------------------------------------");
			//}
						
			
		}
		
		calQuanLevel();
		System.out.println("r: "+rQ[0]+"   "+rQ[1]);
		System.out.println("g: "+gQ[0]+"   "+gQ[1]);
		System.out.println("b: "+bQ[0]+"   "+bQ[1]);
		
		for(int i = 0; i < TOTAL; i++) {
			ret[i] = quantizeValue(ret[i], quanSize, 0);
			ret[i+TOTAL] = quantizeValue(ret[i+TOTAL], quanSize, 1);
			ret[i+TOTAL*2] = quantizeValue(ret[i+TOTAL*2], quanSize, 2);
			
			//change 0~255 to -128 ~ 127
			ret[i] = (ret[i] >= 128.0 ? ret[i] - 256.0 : ret[i]);
			ret[i+TOTAL] = (ret[i+TOTAL] >= 128.0 ? ret[i+TOTAL] - 256.0 : ret[i+TOTAL]);
			ret[i+TOTAL*2] = (ret[i+TOTAL*2] >= 128.0 ? ret[i+TOTAL*2] - 256.0 : ret[i+TOTAL*2]);
			
			
			//boundary check
			ret[i] = ret[i] > 127.0 ? 127.0: (ret[i] < -128.0 ? -128.0 : ret[i]);
			ret[i+TOTAL] = ret[i+TOTAL] > 127.0 ? 127: (ret[i+TOTAL] < -128.0 ? -128.0 : ret[i+TOTAL]);
			ret[i+TOTAL*2] = ret[i+TOTAL*2] > 127.0 ? 127.0: (ret[i+TOTAL*2] < -128.0 ? -128.0 : ret[i+TOTAL*2]);
		}
		
		
		return ret;
	}
	
	public double[] subSampling(int ys, int us, int vs, double[] yuv){
		double[] ret = new double[TOTAL*3];
		//test for the bound
		ys = ys > WIDTH ? WIDTH : ys;
		us = us > WIDTH ? WIDTH : us;
		vs = vs > WIDTH ? WIDTH : vs;
		
		int idx = 0;
		for(int i =0; i < HEIGHT; i++)
			for(int j = 0; j< WIDTH; j++){
				if(j%ys != 0){
					//int pre = j/ys * ys;
					int pre = j - j%ys;
					int lat = pre + ys;
					pre +=  WIDTH*i;
					if(lat >= WIDTH)
						ret[idx] = yuv[pre];
					else{
						lat = pre + ys;
						ret[idx] = (yuv[pre] + yuv[lat])/(double)2.0;
						//ret[idx] = yuv[pre] + (yuv[lat] - yuv[pre]) * (j%ys)/ys;
						
					}
				}else {
					ret[idx] = yuv[idx];
				}
				if(j % us != 0){
					int pre = j - j%us;
					int lat = pre + us;
					pre +=  WIDTH*i;
					//System.out.println(yuv[idx + TOTAL]);
					if(lat >= WIDTH)
						ret[idx + TOTAL] = yuv[pre + TOTAL];
					else{
						lat = pre + us;
						ret[idx + TOTAL] = (yuv[pre + TOTAL] + yuv[lat + TOTAL])/(double)2.0;
						//ret[idx + TOTAL] = yuv[pre+TOTAL] + (yuv[lat+TOTAL] - yuv[pre+TOTAL]) * (j%us)/us;
					}
					//System.out.println(yuv[idx + TOTAL]);
				}else {
					ret[idx + TOTAL] = yuv[idx+TOTAL];
				}
				if(j % vs != 0){
					int pre = j - j%vs;
					int lat = pre + vs;
					pre += WIDTH*i;
					
					if(lat >=WIDTH)
						ret[idx + TOTAL*2] = yuv[pre+TOTAL*2];
					else{
						lat = pre + vs;
						ret[idx+ TOTAL*2] = (yuv[pre + TOTAL*2] + yuv[lat + TOTAL*2])/(double)2.0;
						//ret[idx + TOTAL*2] = yuv[pre+TOTAL*2] + (yuv[lat+TOTAL*2] - yuv[pre+TOTAL*2]) * (j%vs)/vs;
					}
				}else {
					ret[idx + TOTAL*2] = yuv[idx+TOTAL*2];
				}
				idx++;
			}
		
	
		
		
		return ret;
	}
	
	private double quantizeValue(double val, int quanSize) {
		int gap = 256 / (quanSize-1);
		
		int level = (int)val / gap * gap - 1;
		int higherLevel = level + gap;
		level = level < 0? 0 : level;
		
		return (double)((int)val - level < higherLevel - (int)val ? level : higherLevel);
	}

	private double quantizeValue(double val, int quanSize, int color) {
		double ret = 0;
		
		if(color == 0) {
			int min = Math.abs((int)val - rQ[0]);
			ret = (double)rQ[0];
			for(int i = 1 ; i < rQ.length;i++) {
				if(Math.abs((int)val - rQ[i]) < min)
					ret = (double)rQ[i];
			}
			//ret = Math.abs((int)val - rQ[0]) < Math.abs((int)val - rQ[1]) ? (double)rQ[0]:(double)rQ[1];
		}else if(color == 1) {
			int min = Math.abs((int)val - gQ[0]);
			ret = (double)gQ[0];
			for(int i = 1 ; i < gQ.length;i++) {
				if(Math.abs((int)val - gQ[i]) < min)
					ret = (double)gQ[i];
			}
			//ret = Math.abs((int)val - gQ[0]) < Math.abs((int)val - gQ[1]) ? (double)gQ[0]:(double)gQ[1];
		}else if(color == 2) {
			int min = Math.abs((int)val - bQ[0]);
			ret = (double)bQ[0];
			for(int i = 1 ; i < bQ.length;i++) {
				if(Math.abs((int)val - bQ[i]) < min)
					ret = (double)bQ[i];
			}
			//ret = Math.abs((int)val - bQ[0]) < Math.abs((int)val - bQ[1]) ? (double)bQ[0]:(double)bQ[1];
		}
		
		
		
		return ret;
	}
	public void showIms(String[] args){
		//int width = Integer.parseInt(args[1]);
		//int height = Integer.parseInt(args[2]);
		int width = WIDTH;
		int height = HEIGHT;
		
		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		newImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		try {
			String filePath = "src/"+args[0];
			//File file = new File(args[0]);
			File file = new File(filePath);
			InputStream is = new FileInputStream(file);

			long len = file.length();
			byte[] bytes = new byte[(int)len];

			int offset = 0;
			int numRead = 0;
			while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
				offset += numRead;
			}


			int ind = 0;
			for(int y = 0; y < height; y++){

				for(int x = 0; x < width; x++){

					byte a = 0;
					byte r = bytes[ind];
					byte g = bytes[ind+height*width];
					byte b = bytes[ind+height*width*2];		
					int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
					//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
				
					img.setRGB(x,y,pix);
					ind++;
				}
			}
			
			double[] yuv = RGB2YUV(bytes);
			yuv = subSampling(1, 1, 1, yuv);
			double[] rgb = YUV2RGB(yuv);

			int error = calError(bytes, rgb);
			//System.out.println(error);
			
			//System.exit(0);
			ind=0;
			for(int y = 0; y < height; y++)
				for(int x = 0; x < width; x++){
				
					byte a = 0;
					byte r = (byte)rgb[ind];
					byte g = (byte)rgb[ind+height*width];
					byte b = (byte)rgb[ind+height*width*2];
					
					
					
					int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
					//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
					
					newImg.setRGB(x, y, pix);
					ind++;
				}

		}  catch (IOException e) {
			e.printStackTrace();
		}

		// Use labels to display the images
		frame = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);

		JLabel lbText1 = new JLabel("Original image (Left)");
		lbText1.setHorizontalAlignment(SwingConstants.CENTER);
		JLabel lbText2 = new JLabel("Image after modification (Right)");
		lbText2.setHorizontalAlignment(SwingConstants.CENTER);
		lbIm1 = new JLabel(new ImageIcon(img));
		lbIm2 = new JLabel(new ImageIcon(newImg));

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;
		frame.getContentPane().add(lbText1, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.5;
		c.gridx = 1;
		c.gridy = 0;
		frame.getContentPane().add(lbText2, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;
		frame.getContentPane().add(lbIm1, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 1;
		frame.getContentPane().add(lbIm2, c);

		frame.pack();
		frame.setVisible(true);
	}
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		imageReader ren = new imageReader();
		ren.showIms(args);
	}

}
