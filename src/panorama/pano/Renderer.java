package panorama.pano;

import java.awt.Image;
import java.awt.image.MemoryImageSource;
import java.util.Vector;

public class Renderer {

	
	
	
	
	public int vdata[];
	public int vwidth;
	public int vheight;
	public MemoryImageSource source;
	
	public double pitch;
	public double pitch_max;
	public double pitch_min;
	public double hfov;
	
	public int pwidth;
	public int pheight;
	
	public long lastframe;
	
	public double yaw;
	public double yaw_max;
	public double yaw_min;
	
	public boolean dirty;
	public byte hs_vdata[];
	
	public Vector scaledPanos;
	
	public double hfov_min;
	public double hfov_max;
	
	public int imagePixels[][];
	
	public boolean antialias;
	
	public double max_oversampling;
	
	public long frames;
	
	public double autopan;
	public double mt[][];

	
	public void setImage(int[][] pixels, int width, int height){
		
	}
	
	
	public void setupView(int width, int height, boolean showToolbar){
		
		
		if (vwidth == 0)
			vwidth = width;
		if (vheight == 0) { 
			vheight = height;
		}
//		else {
//			vheight = org_vheight;
//		}
		if( showToolbar ) {
			// makes room for the toolbar
			//vheight -= ((toolbar) tlbObj).getHeight();
		}
		if(Utils.math_fovy(hfov, vwidth, vheight) > pitch_max - pitch_min ) {
			// reduces hfov to fit in the current window
			for (;
					Utils.math_fovy(hfov, vwidth, vheight) > pitch_max - pitch_min;
			hfov /= 1.03D);
			hfov *= 1.03D;
			for (;
					Utils.math_fovy(hfov, vwidth, vheight) > pitch_max - pitch_min;
			hfov /= 1.001D);	// second step needed to have more precision
		}
		double d = Utils.math_fovy(hfov, vwidth, vheight) / 2D;
		if (pitch > pitch_max - d && pitch_max != 90D)
			pitch = pitch_max - d;	// sets the highest possible pitch instead of 0
//			pitch = 0.0D;
		if (pitch < pitch_min + d && pitch_min != -90D)
			pitch = pitch_min + d;	// sets the lowest possible pitch instead of 0
//			pitch = 0.0D;
		vdata = new int[vwidth * vheight];
		hs_vdata = new byte[vwidth * vheight];
		//if (filename != null&& filename.toLowerCase().endsWith(".mov")) {
			//for (int k = 0; k < hs_vdata.length; k++)
				//hs_vdata[k] = 0;

		//} else {
			for (int i1 = 0; i1 < hs_vdata.length; i1++)
				hs_vdata[i1] = -1;

		//}
		dirty = true;
		source = new MemoryImageSource(vwidth, vheight, vdata, 0, vwidth);
		source.setAnimated(true);
		
		if (antialias && imagePixels != null) {
			scaledPanos = new Vector();
			scaledPanos.addElement(imagePixels);
			int ai2[][] = imagePixels;
			double d5 = hfov_max / ((double) vwidth * 360D * max_oversampling);
			for (int l1 = 0; ai2 != null && (double) ai2[0].length * d5 > 1.0D; l1++) {
				ai2 = Utils.im_halfsize(ai2);
				scaledPanos.addElement(ai2);
			}

		}
		
	}
	
	public int setCameraPos(double pan, double tilt, double fov){
		// to avoid weird behaviour when this function is called while loading ROIs
//		if( loadingROI && dynLoadROIs ) return; 

		// reduces fov if it is too large for the vertical extension of this pano
		while (Utils.math_fovy(fov, vwidth, vheight) > pitch_max - pitch_min) {
			fov /= 1.03;
		}

		label0 : {
			if (pan == yaw && tilt == pitch && fov == hfov)
				return 0;
			for (; pan > 180D; pan -= 360D);
			for (; pan < -180D; pan += 360D);
			double f = Utils.math_fovy(fov, vwidth, vheight) / 2D;
			if (tilt > pitch_max - f && pitch_max != 90D)
				tilt = pitch_max - f;
			else if (tilt > pitch_max)
				tilt = pitch_max;
			else if (tilt < pitch_min + f && pitch_min != -90D)
				tilt = pitch_min + f;
			else if (tilt < pitch_min)
				tilt = pitch_min;
			if (yaw_max != 180D || yaw_min != -180D) {
				// check left edge
				double xl =
					math_view2pano(0,pitch <= 0.0D ? vheight - 1 : 0,vwidth,vheight,pwidth,pheight,pan,tilt,fov)[0];
				double xr =
					math_view2pano(
							vwidth - 1,
							pitch <= 0.0D ? vheight - 1 : 0,
								vwidth,
								vheight,
						pwidth,
						pheight,
						pan,
						tilt,
						fov)[0];
				if (math_view2pano(vwidth - 1,
						pitch <= 0.0D ? vheight - 1 : 0,
							vwidth,
							vheight,
					pwidth,
					pheight,
					pan,
					tilt,
					fov)[0]
					- xl
					> ((yaw_max - yaw_min) / 360D) * (double) pwidth)
					break label0;
				if (xl < ((yaw_min + 180D) / 360D) * (double) pwidth) {
					if (lastframe > frames)
						autopan *= -1D;
					pan += yaw_min - ((xl / (double) pwidth) * 360D - 180D);
				}
				if (xr > ((yaw_max + 180D) / 360D) * (double) pwidth) {
					if (lastframe > frames)
						autopan *= -1D;
					pan -= (xr / (double) pwidth) * 360D - 180D - yaw_max;
				}
			}
			if (2D * f <= pitch_max - pitch_min && fov <= hfov_max && fov >= hfov_min
				&& fov <= yaw_max - yaw_min
				&& tilt <= pitch_max
				&& tilt >= pitch_min
				&& pan <= yaw_max
				&& pan >= yaw_min
				&& (pan != yaw || tilt != pitch || fov != hfov)) {
				yaw = pan;
				pitch = tilt;
				hfov = fov;
				dirty = true;
				
				return 1;
			}
			// If we reach this point, then there is no change
			// We have probably reached the end of an autopan
			
			return 2;
		}
		return 3;
	}
	
	
	public final double[] math_view2pano(
			int i,
			int j,
			int k,
			int l,
			int i1,
			int j1,
			double d,
			double d1,
			double d2) {
			double d8 = (double) i1 / 6.2831853071795862D;
			double d3 = (d2 * 2D * 3.1415926535897931D) / 360D;
			double d4 = (int) ((double) k / (2D * Math.tan(d3 / 2D)) + 0.5D);
			Utils.SetMatrix(
				(d1 * 2D * 3.1415926535897931D) / 360D,
				(d * 2D * 3.1415926535897931D) / 360D,
				mt,
				1);
			i -= k >> 1;
			j -= l >> 1;
			double d5 =
				mt[0][0] * (double) i + mt[1][0] * (double) j + mt[2][0] * d4;
			double d6 =
				mt[0][1] * (double) i + mt[1][1] * (double) j + mt[2][1] * d4;
			double d7 =
				mt[0][2] * (double) i + mt[1][2] * (double) j + mt[2][2] * d4;
			double ad[];
			(ad = new double[2])[0] = d8 * Math.atan2(d5, d7) + (double) i1 / 2D;
			ad[1] =
				d8 * Math.atan2(d6, Math.sqrt(d7 * d7 + d5 * d5))
					+ (double) j1 / 2D;
			return ad;
		}
	
}
