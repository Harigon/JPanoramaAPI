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
	
	public int hs_imode[];
	
	public int numhs;
	
	private long mi[][];

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
	
	public int hs_xv[];
	public int hs_yv[];

	public int imagePixels[][];

	public boolean antialias;

	public double max_oversampling;

	public long frames;

	public double autopan;
	public double mt[][];
	
	public double dist_e;
	
	public double hs_xp[];
	public double hs_yp[];
	public double hs_up[];
	public double hs_vp[];
	
	public Object hs_him[];
	
	public boolean hs_visible[];
	
	// original values kept as a reference
	public double pitch_max_org;
	public double pitch_min_org;

//	static final int NATAN = 4096;
//	static final int NSQRT = 4096;
//	static final int NSQRT_SHIFT = 12;	// NSQRT = 2^NSQRT_SHIFT
//	static final int NATAN = 16384;
//	static final int NSQRT = 16384;
//	static final int NSQRT_SHIFT = 14;	// NSQRT = 2^NSQRT_SHIFT
	public static final int NATAN = 65536;
	public static final int NSQRT = 65536;
	public static final int NSQRT_SHIFT = 16;	// NSQRT = 2^NSQRT_SHIFT
	
	// multiplier and corresponding shift
		// used to increase the resolution of the values
		//   in the integer transformation matrix mi[][]
//		static final int MI_MULT = 64;
//		static final int MI_SHIFT = 6;
	public static final int MI_MULT = 4096;
		public static final int MI_SHIFT = 12;
		
		public int atan_LU_HR[];
		public int sqrt_LU[];
		public double atan_LU[];
		public int PV_atan0_HR;
		public int PV_pi_HR;
	
	
	public Renderer renderer = this;
	
	// vertical position of the horizon, computed as the distance of the horizon from the
		// top of the image as a % value
		public int horizonPosition;
		
		// this variable is <> 0 if horizonPosition is <> 50 (default value)
		// it is the number of padding pixels that we should add to the pano image
		// in order to have the horizon in the middle
		// it is > 0 if we need to add space at the top of the image
		// it is < 0 if we need to add space at the bottom of the image
		public int deltaYHorizonPosition;


		
	public Renderer(){
		renderer.source = null;
		renderer.vwidth = 0;
		renderer.vheight = 0;
		renderer.pwidth = 0;
		renderer.pheight = 0;
		renderer.vdata = null;
		renderer.hs_vdata = null;
		renderer.imagePixels = null;
		renderer.yaw = 0.0D;
		renderer.hfov = 70D;
		renderer.hfov_min = 10.5D;
		renderer.hfov_max = 165D;
		renderer.pitch = 0.0D;
		renderer.pitch_max = 90D;
		renderer.pitch_min = -90D;
		renderer.yaw_max = 180D;
		renderer.yaw_min = -180D;
		renderer.frames = 0L;
		renderer.lastframe = 0L;
		renderer.antialias = false;
		renderer.scaledPanos = null;
		renderer.max_oversampling = 1.5D;
		renderer.atan_LU_HR = null;
		renderer.atan_LU = null;
		renderer.dist_e = 1.0D;
		renderer.PV_atan0_HR = 0;
		renderer.PV_pi_HR = 0;
		renderer.numhs = 0;
		renderer.horizonPosition = 50;
		renderer.autopan = 0.0D;
	}
		
		
	public void setImage(int[][] pixels, int width, int height){
		this.imagePixels = pixels;
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


	void lanczos2_compute_view_scale() {
		double wDT;

		wDT = hfov * pwidth / 360.0;
		view_scale = vwidth / wDT;
	}
	
	public String DisplayHSCoordinates(int i, int j) {
		double ad[];
		(ad =
				math_view2pano(
				i,
				j,
				renderer.vwidth,
				renderer.vheight,
				renderer.pwidth,
				renderer.pheight - deltaYHorizonPosition,
				renderer.yaw,
				renderer.pitch,
				renderer.hfov))[0] =
			Math.rint((ad[0] * 100000D) / (double) renderer.pwidth) / 1000D;
		ad[1] = Math.rint((ad[1] * 100000D) / (double) renderer.pheight) / 1000D;
		return "X = " + ad[0] + "; Y = " + ad[1];
	}
	
	public void getViewAtCameraPos(int pd[][], int vData[], byte hv[], int vWidth, double hfov, double pan, double tilt, int quality){
		
		boolean useBilinear = false;
		boolean useLanczos2 = false;
		
		
		if(quality == 0){
			useBilinear = false;
			useLanczos2 = false;
		} else if(quality == 1){
			
		}
		
		renderer.math_extractview(pd, vData, hv, vWidth, hfov, pan, tilt, useBilinear, useLanczos2);
		
	}
	

	public final int[] math_extractview(int pd[][], int v[], byte hv[], int vw, double fov, double pan, double tilt, boolean bilinear, boolean lanczos2) {

		if(lanczos2) {
			double prev_view_scale = view_scale;
			lanczos2_compute_view_scale();
			if (view_scale != prev_view_scale)
				lanczos2_compute_weights(view_scale);
		}

		math_set_int_matrix(fov, pan, tilt, vw);
		int[] vdata = math_transform(pd,pd[0].length, pd.length + deltaYHorizonPosition, v, hv, vw, v.length / vw, bilinear, lanczos2);
		return vdata;
	}

	final void math_set_int_matrix(double fov, double pan, double tilt, int vw) {
		double a = (fov * 2D * 3.1415926535897931D) / 360D; // field of view in rad
		double p = (double) vw / (2D * Math.tan(a / 2D));
		Utils.SetMatrix(
				(tilt * 2D * 3.1415926535897931D) / 360D,
				(pan * 2D * 3.1415926535897931D) / 360D,
				renderer.mt,
				1);
		renderer.mt[0][0] /= p;
		renderer.mt[0][1] /= p;
		renderer.mt[0][2] /= p;
		renderer.mt[1][0] /= p;
		renderer.mt[1][1] /= p;
		renderer.mt[1][2] /= p;
		double ta =
				a <= 0.29999999999999999D ? 436906.66666666669D : 131072D / a;
		for (int j = 0; j < 3; j++) {
			for (int k = 0; k < 3; k++) 
				mi[j][k] = (long) (ta * renderer.mt[j][k] * MI_MULT + 0.5D);
			//			mi[j][k] = (int) (ta * mt[j][k] * MI_MULT + 0.5D);
			//			mi[j][k] = (int) (ta * mt[j][k] + 0.5D);

		}

	}
	
	final int PV_sqrt(int pi, int pj) {
		long i = pi;
		long j = pj;
		if (i > j)
			return (int) (i * sqrt_LU[(int) ((j << NSQRT_SHIFT) / i)] >> NSQRT_SHIFT);
		if (j == 0)
			return 0;
		else
			return (int) (j * sqrt_LU[(int) ((i << NSQRT_SHIFT) / j)] >> NSQRT_SHIFT);
	}

	
	
	public void math_init() {
		renderer.mt = new double[3][3];
		mi = new long[3][3];
	}

	public void math_dispose() {
		atan_LU_HR = null;
		sqrt_LU = null;
		renderer.mt = null;
		mi = null;
	}

	public final void math_setLookUp(int ai[][]) {
		if (ai != null) {
			if (atan_LU_HR == null) {
				atan_LU_HR = new int[NATAN + 1];
				atan_LU = new double[NATAN + 1];
				sqrt_LU = new int[NSQRT + 1];
//				double d1 = 0.000244140625D;
				double d1 = 1.0 / (double) NSQRT;
				double d = 0.0D;
				for (int i = 0; i < NSQRT;) {
					sqrt_LU[i] = (int) (Math.sqrt(1.0D + d * d) * NSQRT);
					i++;
					d += d1;
				}

				sqrt_LU[NSQRT] = (int) (Math.sqrt(2D) * NSQRT);
//				d1 = 0.000244140625D;
				d1 = 1.0 / (double) NATAN;
				d = 0.0D;
				for (int j = 0; j < NATAN + 1;) {
					if (j < NATAN)
						atan_LU[j] = Math.atan(d / (1.0D - d)) * 256D;
					else
						atan_LU[j] = 402.12385965949352D;
					j++;
					d += d1;
				}

			}
			math_updateLookUp(ai[0].length);
		}
	}

	public final void math_updateLookUp(int i) {
		int j = i << 6;
		if (PV_atan0_HR != j) {
			dist_e = (double) i / 6.2831853071795862D;
			PV_atan0_HR = j;
			PV_pi_HR = 128 * i;
			for (int k = 0; k < NATAN + 1; k++)
				atan_LU_HR[k] = (int) (dist_e * atan_LU[k] + 0.5D);

		}
	}

	
	public final void hs_setCoordinates(
			int vw,
			int vh,
			int pw,
			int ph,
			double pan,
			double tilt,
			double fov) {
				
			// deltaY is the height of missing upper part of the panorama if this pano is not fully spherical
			//   it is == 0 if the tilt angle goes from -90 to + 90 
			int deltaY = (pw/2 - ph)/2;
			if( deltaY < 0 ) deltaY = 0; 

			int sw2 = pw >> 1;
			int sh2 = (ph >> 1) + deltaY;
			double mt[][] = new double[3][3];
			double a = (fov * 2D * 3.1415926535897931D) / 360D; // field of view in rad
			double p = (double) vw / (2D * Math.tan(a / 2D));
			Utils.SetMatrix(
				(-tilt * 2D * 3.1415926535897931D) / 360D,
				(-pan * 2D * 3.1415926535897931D) / 360D,
				mt,
				0);
			double v0;
			for (int i = 0; i < numhs; i++) {
				double x = hs_xp[i] - (double) sw2;
				double y = (double) (renderer.pheight + 2*deltaY) - (hs_yp[i] + deltaY + deltaYHorizonPosition/2 - (double) sh2);
				double theta = (x / (double) sw2) * 3.1415926535897931D;
				double phi = ((y / (double) sh2) * 3.1415926535897931D) / 2D;
				double v2;
				if (Math.abs(theta) > 1.5707963267948966D)
					v2 = 1.0D;
				else
					v2 = -1D;
				double d5;
				v0 = v2 * Math.tan(theta);
				d5 = v0;
				double v1 = Math.sqrt(v0 * v0 + v2 * v2) * Math.tan(phi);
				x = mt[0][0] * d5 + mt[1][0] * v1 + mt[2][0] * v2;
				y = mt[0][1] * d5 + mt[1][1] * v1 + mt[2][1] * v2;
				double z = mt[0][2] * d5 + mt[1][2] * v1 + mt[2][2] * v2;
				hs_xv[i] = (int) ((x * p) / z + (double) vw / 2D);
				hs_yv[i] = (int) ((y * p) / z + (double) vh / 2D);
				int hs_vis_hor = 12;
				int hs_vis_ver	 = 12;
				if (hs_him[i] != null && (hs_him[i] instanceof Image)) {
					hs_vis_hor = ((Image) hs_him[i]).getWidth(null) >> 1;
					hs_vis_ver = ((Image) hs_him[i]).getHeight(null) >> 1;
				} else if (
					hs_him[i] != null
						&& (hs_him[i] instanceof String)
						&& (hs_imode[i] & 0x10) > 0) {
					hs_vis_hor = 100;
					hs_vis_ver = 100;
				} else if (hs_up[i] != -200D && hs_vp[i] != -200D) {
					hs_vis_hor = 100;
					hs_vis_ver = 100;
				}
				if (hs_xv[i] >= -hs_vis_hor
					&& hs_xv[i] < renderer.vwidth + hs_vis_hor
					&& hs_yv[i] >= -hs_vis_ver
					&& hs_yv[i] < renderer.vheight + hs_vis_ver
					&& z < 0.0D)
					hs_visible[i] = true;
				else
					hs_visible[i] = false;
			}

		}


	/*
	 * if bilinear == true use bilinear interpolation
	 * if lanczos2 == true use lanczos2 interpolation
	 * if bilinear == false && lanczos2 == false use nearest neighbour interpolation
	 */
	final int[] math_transform(int pd[][], int pw, int ph, int vz[], byte hv[], int vw, int vh, boolean bilinear, boolean lanczos2) {

		
		int[] v = new int[renderer.vwidth * renderer.vheight];
		
		// flag: use nearest neighbour interpolation
		boolean nn = (!bilinear && !lanczos2);

		boolean firstTime;	// flag
		int itmp;	// temporary variable used as a loop index

		int mix = pw - 1;
		int miy = ph - deltaYHorizonPosition - 1;
		int w2 = vw - 1 >> 1;
			int h2 = vh >> 1;
		int sw2 = pw >> 1;
		int sh2 = ph >> 1;
		int x_min = -w2;
		int x_max = vw - w2;
		int y_min = -h2;
		int y_max = vh - h2;
		int cy = 0;

		int xs_org, ys_org;	// used for lanczos2 interpolation
		int l24 = 0;
		int pd_0[] = pd[0];
		int pd_1[] = pd[1];
		long m0 = mi[1][0] * y_min + mi[2][0];
		long m1 = mi[1][1] * y_min + mi[2][1];
		long m2 = mi[1][2] * y_min + mi[2][2];
		long mi_00 = mi[0][0];
		long mi_02 = mi[0][2];
		double vfov_2 = Utils.math_fovy(renderer.hfov, vw, vh) / 2D;

		// number of points to be computed with linear interpolation
		// between two correctly computed points along the x-axis
		int N_POINTS_INTERP_X = vw/20;
		//System.out.println("Max view: " + (pitch + vfov_2) );

		//		if (pitch + vfov_2 > 45D || pitch - vfov_2 < -45D) N_POINTS_INTERP_X = vw/30; 
		//		if (pitch + vfov_2 > 50D || pitch - vfov_2 < -50D) N_POINTS_INTERP_X = vw/40; 
		if (renderer.pitch + vfov_2 > 65D || renderer.pitch - vfov_2 < -65D) N_POINTS_INTERP_X = vw/35; 
		if (renderer.pitch + vfov_2 > 70D || renderer.pitch - vfov_2 < -70D) N_POINTS_INTERP_X = vw/50; 
		if (renderer.pitch + vfov_2 > 80D || renderer.pitch - vfov_2 < -80D) N_POINTS_INTERP_X = vw/200; 
		int N_POINTS_INTERP_X_P1 = N_POINTS_INTERP_X + 1;

		// number of rows to be computed with linear interpolation
		// between two correctly computed rows along the y-axis
		int N_POINTS_INTERP_Y;
		int N_POINTS_INTERP_Y_P1;

		///////////////////////////////////////////////////
		// the standard settings cause artifacts at the poles, so we disable interpolation 
		// between rows when we draw the poles
		//
		// since correctly drawing the poles requires very few interpolated points in each row
		// we will interpolate on a larger distance between rows when we are far away from the poles
		// in order to speed up computation
		//
		// so if a pole is in the viewer window things will go this way, considering rows
		// from top to bottom:
		//  - the first rows are very far from the pole and they will be drawn with double
		//    y interpolation to speed up things
		//  - then some rows are nearer to the pole and will be drawn with standard y interpolation
		//  - now we draw the pole without y interpolation
		//  - then we draw some lines with standard y interpolation
		//  - the last lines are farther from the pole and will be drawn with double
		//    y interpolation
		//
		// first row to draw without y-interpolation (default none)
		int N_ROW_NO_INTERP_MIN = y_max + 100;
		// last row to draw without y-interpolation (default none)
		int N_ROW_NO_INTERP_MAX = N_ROW_NO_INTERP_MIN;

		// last row of the upper part of the window to draw with double y-interpolation (default none)
		// we will use double distance from row 0 to this row
		int N_ROW_DOUBLE_INTERP_LOW = y_min - 100;
		// first row of the lower part of the window to draw with double y-interpolation (default none)
		// we will use double distance from this row to the last one
		int N_ROW_DOUBLE_INTERP_HIGH = y_max + 100;

		if( vfov_2 > 10 ) { // only if not zooming in too much...
			// we consider critical the zone at +/- 5 degrees from the poles
			if (renderer.pitch + vfov_2 > 87.5 || renderer.pitch - vfov_2 < -87.5) {
				if( renderer.pitch > 0 ) {
					// looking upwards
					N_ROW_NO_INTERP_MIN = y_min + (int) ((y_max - y_min)*
							(1 - (92.5 - (renderer.pitch - vfov_2))/(2*vfov_2)));
					N_ROW_NO_INTERP_MAX = y_min + (int) ((y_max - y_min)*
							(1 - (87.5 - (renderer.pitch - vfov_2))/(2*vfov_2)));
				}
				else {
					N_ROW_NO_INTERP_MIN = y_min + (int) ((y_max - y_min)*
							(1 - (-87.5 - (renderer.pitch - vfov_2))/(2*vfov_2)));
					N_ROW_NO_INTERP_MAX = y_min + (int) ((y_max - y_min)*
							(1 - (-92.5 - (renderer.pitch - vfov_2))/(2*vfov_2)));

				}
			}
			// we draw with double y-interpolation the zone outside +/- 10 degrees from the poles
			double angle = 10;
			if (renderer.pitch + vfov_2 > 90 - angle || renderer.pitch - vfov_2 < -90 + angle) {
				if( renderer.pitch > 0 ) {
					// looking upwards
					N_ROW_DOUBLE_INTERP_LOW = y_min + (int) ((y_max - y_min)*
							(1 - (90 + angle - (renderer.pitch - vfov_2))/(2*vfov_2)));
					N_ROW_DOUBLE_INTERP_HIGH = y_min + (int) ((y_max - y_min)*
							(1 - (90 - angle - (renderer.pitch - vfov_2))/(2*vfov_2)));
				}
				else {
					N_ROW_DOUBLE_INTERP_LOW = y_min + (int) ((y_max - y_min)*
							(1 - (-90 + angle - (renderer.pitch - vfov_2))/(2*vfov_2)));
					N_ROW_DOUBLE_INTERP_HIGH = y_min + (int) ((y_max - y_min)*
							(1 - (-90 - angle - (renderer.pitch - vfov_2))/(2*vfov_2)));

				}
			}
			//			System.out.println( "Min " + N_ROW_NO_INTERP_MIN + "       Max " + N_ROW_NO_INTERP_MAX );
			//			System.out.println( "Low " + N_ROW_DOUBLE_INTERP_LOW + "       High " + N_ROW_DOUBLE_INTERP_HIGH );
		}
		///////////////////////////////////////////////////////////

		// data used for interpolation between rows:
		// size of the arrays used to store row values
		int ROWS_INT_SIZE = vw / N_POINTS_INTERP_X + 4;	// just to be safe...
		// coordinates of vertices in the upper computed row
		int[] row_xold = new int[ROWS_INT_SIZE];
		int[] row_yold = new int[ROWS_INT_SIZE];
		// coordinates of vertices in the lower computed row
		int[] row_xnew = new int[ROWS_INT_SIZE];
		int[] row_ynew = new int[ROWS_INT_SIZE];
		// difference between each interpolated line
		int[] row_xdelta = new int[ROWS_INT_SIZE];
		int[] row_ydelta = new int[ROWS_INT_SIZE];
		// used when drawing a line, contains the interpolted values every N_POINTS_INTERP_P1 pixels
		int[] row_xcurrent = new int[ROWS_INT_SIZE];
		int[] row_ycurrent = new int[ROWS_INT_SIZE];

		// shifted widh of the panorama
		int pw_shifted = (pw << 8);
		int pw_shifted_2 = pw_shifted / 2;
		int pw_shifted_3 = pw_shifted / 3;

		// used for linear interpolation 
		int x_old;
		int y_old;

		firstTime = true;
		long v0 = m0 + x_min*mi_00;
		long v1 = m1;
		long v2 = m2 + x_min*mi_02;

		N_POINTS_INTERP_Y = N_POINTS_INTERP_X;
		N_POINTS_INTERP_Y_P1 = N_POINTS_INTERP_Y + 1;
		int nPtsInterpXOrg = N_POINTS_INTERP_X;	// stores the original value for future reference

		for (int y = y_min; y < y_max;) {
			int idx;
			int x_center, y_center, x_tmp;

			idx = cy;

			// if we are drawing one of the poles we disable interpolation between rows
			// to avoid artifacts
			if( (y + N_POINTS_INTERP_Y_P1 > N_ROW_NO_INTERP_MIN) &&
					(y < N_ROW_NO_INTERP_MAX) ) {
				N_POINTS_INTERP_Y = 0;
				if( N_POINTS_INTERP_X != nPtsInterpXOrg ) {
					N_POINTS_INTERP_X = nPtsInterpXOrg;
					firstTime = true;   // to recompute the arrays
				}
			}
			else {
				if( (y + N_POINTS_INTERP_Y_P1 < N_ROW_DOUBLE_INTERP_LOW) ||
						(y > N_ROW_DOUBLE_INTERP_HIGH) ) {
					// we are farther from the pole so we compute more rows with interpolation
					N_POINTS_INTERP_Y = nPtsInterpXOrg * 4;
					// since we are far from the poles we can interpolate between more pixels
					if( N_POINTS_INTERP_X != nPtsInterpXOrg * 4 ) {
						N_POINTS_INTERP_X = nPtsInterpXOrg * 4;
						firstTime = true;   // to recompute the arrays
					}
				} else {
					N_POINTS_INTERP_Y = N_POINTS_INTERP_X;
				}
			}
			N_POINTS_INTERP_Y_P1 = N_POINTS_INTERP_Y + 1;
			N_POINTS_INTERP_X_P1 = N_POINTS_INTERP_X;
			//System.out.println( "y = " + y + "  " + N_POINTS_INTERP_Y );			

			if( !firstTime ) {
				// row_old[] = row_new[]
				for( itmp = 0; itmp < ROWS_INT_SIZE; itmp++ ) {
					row_xold[itmp] = row_xnew[itmp];
					row_yold[itmp] = row_ynew[itmp];
				}
				m0 += mi[1][0] * N_POINTS_INTERP_Y_P1;
				m1 += mi[1][1] * N_POINTS_INTERP_Y_P1;
				m2 += mi[1][2] * N_POINTS_INTERP_Y_P1;
			}

			// computes row_new[]
			v0 = m0 + x_min*mi_00;
			v1 = m1;
			v2 = m2 + x_min*mi_02;
			int irow = 0;	  // index in the row_*[] arrays
			int curx = x_min;  // x position of the current pixel in the viewer window
			row_xnew[irow] = PV_atan2_HR( (int) v0 >> MI_SHIFT, (int) v2 >> MI_SHIFT);
			row_ynew[irow] = PV_atan2_HR( (int) v1 >> MI_SHIFT, PV_sqrt( (int) Math.abs(v2 >> MI_SHIFT), (int) Math.abs(v0 >> MI_SHIFT)));
			//if(firstTime){
			//	System.out.println( "row_xnew[0], row_ynew[0]" + row_xnew[irow] + "  " + row_ynew[irow] );
			//	System.out.println( "v0, v2 " + (int)(v0 >> MI_SHIFT) + "  " + (int)(v2 >> MI_SHIFT) );
			//	System.out.println( PV_pi_HR );
			//}
			while( curx <= x_max ) {
				v0 += mi_00 * N_POINTS_INTERP_X_P1;
				v2 += mi_02 * N_POINTS_INTERP_X_P1;

				curx += N_POINTS_INTERP_X_P1;
				irow++;
				row_xnew[irow] = PV_atan2_HR( (int) v0 >> MI_SHIFT, (int) v2 >> MI_SHIFT);
				row_ynew[irow] = PV_atan2_HR( (int) v1 >> MI_SHIFT, PV_sqrt( (int) Math.abs(v2 >> MI_SHIFT), (int) Math.abs(v0 >> MI_SHIFT)));
			}

			if( firstTime ) {
				// the first time only computes the first row and loops: that computation should be done before the loop
				// but I didn't like the idea of duplicating so much code so I arranged the code in such a way
				firstTime = false;
				continue;
			}

			// computes row_delta[], the difference between each row
			for( itmp = 0; itmp < ROWS_INT_SIZE; itmp++ ) {
				if ((row_xnew[itmp] < -pw_shifted_3) && (row_xold[itmp] > pw_shifted_3))
					row_xdelta[itmp] =
					(row_xnew[itmp] + pw_shifted - row_xold[itmp]) / (N_POINTS_INTERP_Y_P1);
				else {
					if ((row_xnew[itmp] > pw_shifted_3) && (row_xold[itmp] < -pw_shifted_3))
						row_xdelta[itmp] =
						(row_xnew[itmp] - pw_shifted - row_xold[itmp]) / (N_POINTS_INTERP_Y_P1);
					else
						row_xdelta[itmp] = (row_xnew[itmp] - row_xold[itmp]) / (N_POINTS_INTERP_Y_P1);
				}
				row_ydelta[itmp] = (row_ynew[itmp] - row_yold[itmp]) / N_POINTS_INTERP_Y_P1;
			}

			// row_current[] contains the values for the current row
			for( itmp = 0; itmp < ROWS_INT_SIZE; itmp++ ) {
				row_xcurrent[itmp] = row_xold[itmp];
				row_ycurrent[itmp] = row_yold[itmp];
			}

			// now draws a set of lines
			for( int ky = 0; ky < N_POINTS_INTERP_Y_P1; ky++) {

				if( y >= y_max ) break;

				irow = 0;
				x_old = row_xcurrent[irow];
				y_old = row_ycurrent[irow];

				for (int x = x_min + 1; x <= x_max;) {
					v0 += mi_00 * N_POINTS_INTERP_X_P1;
					v2 += mi_02 * N_POINTS_INTERP_X_P1;
					irow++;
					// determines the next point: it will interpolate between the new and old point
					int x_new = row_xcurrent[irow];
					int y_new = row_ycurrent[irow];

					int delta_x;
					if ((x_new < -pw_shifted_3) && (x_old > pw_shifted_3))
						delta_x =
						(x_new + pw_shifted - x_old) / (N_POINTS_INTERP_X_P1);
					else {
						if ((x_new > pw_shifted_3) && (x_old < -pw_shifted_3))
							delta_x =
							(x_new - pw_shifted - x_old) / (N_POINTS_INTERP_X_P1);
						else
							delta_x = (x_new - x_old) / (N_POINTS_INTERP_X_P1);
					}
					int delta_y = (y_new - y_old) / (N_POINTS_INTERP_X_P1);

					// now computes the intermediate points with linear interpolation
					int cur_x = x_old;
					int cur_y = y_old;
					for (int kk = 0; kk < N_POINTS_INTERP_X_P1; kk++) {
						if (x > x_max)
							break;
						if (cur_x >= pw_shifted_2)
							cur_x -= pw_shifted;
						if (cur_x < -pw_shifted_2)
							cur_x += pw_shifted;
						cur_y += delta_y;
						int dx = cur_x & 0xff;
						int dy = cur_y & 0xff;
						int xs = (cur_x >> 8) + sw2;
						int ys;
						int v_idx = v[idx];

						// used for nn interpolation
						ys_org = (cur_y >> 8) + sh2 - deltaYHorizonPosition;
						int[] pd_row = null;
						int row_index, col_index;
						if( nn ) {
							if( dy < 128 )
								row_index = ys_org;
							else
								row_index = ys_org + 1;
							if( row_index < 0 ) row_index = 0;
							if( row_index > miy ) row_index = miy;
							pd_row = pd[row_index];
						}
						if (v_idx == 0 ) {
							// draws the pixel
							xs_org = xs;
							if (v_idx == 0) {
								if(nn) {
									if( dx < 128 ) 
										col_index = xs_org; 
									else 
										col_index = xs_org + 1;
									if( col_index < 0 ) col_index = 0;
									if( col_index > mix ) col_index = mix;
									int pxl = pd_row[col_index];
									v[idx] = pxl | 0xff000000;
									hv[idx] = (byte) (pxl >> 24);
								}
								else {
									int px00;
									int px01;
									int px10;
									int px11;
									if ((ys = ys_org) == l24
											&& xs >= 0
											&& xs < mix) {
										px00 = pd_0[xs];
										px10 = pd_1[xs++];
										px01 = pd_0[xs];
										px11 = pd_1[xs];
									} else if (
											ys >= 0 && ys < miy && xs >= 0 && xs < mix) {
										l24 = ys;
										pd_0 = pd[ys];
										pd_1 = pd[ys + 1];
										px00 = pd_0[xs];
										px10 = pd_1[xs++];
										px01 = pd_0[xs];
										px11 = pd_1[xs];
									} else {
										if (ys < 0) {
											pd_0 = pd[0];
											l24 = 0;
										} else if (ys > miy) {
											pd_0 = pd[miy];
											l24 = miy;
										} else {
											pd_0 = pd[ys];
											l24 = ys;
										}
										if (++ys < 0)
											pd_1 = pd[0];
										else if (ys > miy)
											pd_1 = pd[miy];
										else
											pd_1 = pd[ys];
										if (xs < 0) {
											px00 = pd_0[mix];
											px10 = pd_1[mix];
										} else if (xs > mix) {
											px00 = pd_0[0];
											px10 = pd_1[0];
										} else {
											px00 = pd_0[xs];
											px10 = pd_1[xs];
										}
										if (++xs < 0) {
											px01 = pd_0[mix];
											px11 = pd_1[mix];
										} else if (xs > mix) {
											px01 = pd_0[0];
											px11 = pd_1[0];
										} else {
											px01 = pd_0[xs];
											px11 = pd_1[xs];
										}
									}
									if(lanczos2){
										v[idx] = lanczos2_interp_pixel( pd, pw, ph - deltaYHorizonPosition, xs_org, ys_org, dx, dy);
									} else {
										v[idx] = Utils.bil(px00, px01, px10, px11, dx, dy);
									}
									hv[idx] = (byte) (px00 >> 24);
								}
							}
						}
						idx++;
						x++;
						cur_x += delta_x;
					}
					x_old = x_new;
					y_old = y_new;
				}

				// computes the next line using interpolation at the rows level
				for( itmp = 0; itmp < ROWS_INT_SIZE; itmp++ ) {
					row_xcurrent[itmp] += row_xdelta[itmp];
					row_ycurrent[itmp] += row_ydelta[itmp];
				}

				y++;
				cy += vw;
			}
		}
		return v;
	}
	
	final int PV_atan2_HR(int pi, int pj)
	{
			long i = pi;
			long j = pj;
			int index;
			if(j > 0)
					if(i > 0)
							return atan_LU_HR[(int)((NATAN * i) / (j + i))];
					else
							return -atan_LU_HR[(int) ((NATAN * -i) / (j - i))];
			if(j == 0)
					if(i > 0)
							return PV_atan0_HR;
					else
							return -PV_atan0_HR;
			if(i < 0) {
				index = (int) ((NATAN * i) / (j + i));
				return atan_LU_HR[index] - PV_pi_HR;
//				return atan_LU_HR[(int) ((NATAN * i) / (j + i))] - PV_pi_HR;
			}
			else
					return -atan_LU_HR[(int) ((NATAN * -i) / (j - i))] + PV_pi_HR;
	}
	
	// computes some parameters depending on the horizon position
		public void CheckHorizonPosition() {
			deltaYHorizonPosition = (100 - 2*horizonPosition)*renderer.pheight/100;
			if (renderer.pheight != renderer.pwidth >> 1) {
				double d = ((double) renderer.pheight / (double) renderer.pwidth) * 180D;
				double deltaPitch = ((double) deltaYHorizonPosition/(double) renderer.pwidth)*180;
				renderer.pitch_min = pitch_min_org;
				renderer.pitch_max = pitch_max_org;
				if (renderer.pitch_max > (d - deltaPitch) )
					renderer.pitch_max = d - deltaPitch;
				if (renderer.pitch_min < (-d - deltaPitch) )
					renderer.pitch_min = -d - deltaPitch;
			}
		}


	/////////////////////////////////////////////
	// start of Lanczos2 interpolation stuff
	/////////////////////////////////////////////

	// number of subdivisions of the x-axis unity
	//  static int UNIT_XSAMPLES = 1024;
	static int UNIT_XSAMPLES = 256;
	// number of subdivisions of the y-axis unity
	static int UNIT_YSAMPLES = 1024;
	// number of bits to shift to return to the 0-255 range
	// corresponds to the division by (UNIT_YSAMPLES*UNIT_YSAMPLES)
	static int SHIFT_Y = 20;

	// maximum number of weights used to interpolate one pixel
	static int MAX_WEIGHTS = 20;

	// maximum value for the quality parameter
	public static int MAX_QUALITY = 6;

	// lookup table
	static int lanczos2_LU[];
	// lookup table for the interpolation weights
	static int lanczos2_weights_LU[][];

	// number of points on each side for an enlarged image
	static int lanczos2_n_points_base = 2;

	// number of points actually used on each side, changes with view_scale
	int lanczos2_n_points;

	// temporary arrays used during interpolation
	int aR[], aG[], aB[];

	// current wiewing scale:
	// < 1: pano image is reduced
	// > 1: pano image is enlarged
	double view_scale;

	double sinc(double x) {
		double PI = 3.14159265358979;

		if (x == 0.0)
			return 1.0;
		else
			return Math.sin(PI * x) / (PI * x);
	}

	public void lanczos2_init() {
		double x, dx;
		int k;

		// sets up the lookup table

		lanczos2_LU = new int[UNIT_XSAMPLES * 2 + 1];
		x = 0.0;
		dx = 1.0 / UNIT_XSAMPLES;
		for (k = 0; k <= UNIT_XSAMPLES * 2; k++) {
			lanczos2_LU[k] =
					(int) (sinc(x) * sinc(x / 2.0) * UNIT_YSAMPLES + 0.5);
			x += dx;
		}

		// allocates the weights lookup table
		// the values are set up by lanczos2_compute_weights()
		lanczos2_weights_LU = new int[UNIT_XSAMPLES + 1][MAX_WEIGHTS];

		// allocates temporary buffers
		aR = new int[MAX_WEIGHTS];
		aG = new int[MAX_WEIGHTS];
		aB = new int[MAX_WEIGHTS];
	}

	// computes the weiths for interpolating pixels
	// the weights change with view_scale
	void lanczos2_compute_weights(double pscale) {
		double s, corr;

		if (pscale > 1.0)
			pscale = 1.0;
		if (pscale >= 1.0)
			lanczos2_n_points = lanczos2_n_points_base;
		else
			lanczos2_n_points = (int) (lanczos2_n_points_base / pscale);

		// sets up the lookup table for the interpolation weights
		for (int j = 0; j <= UNIT_XSAMPLES; j++) {
			// computes the weights for this x value
			int k;
			s = 0;
			int i = j + UNIT_XSAMPLES * (lanczos2_n_points - 1);
			for (k = 0; k < lanczos2_n_points; k++) {
				lanczos2_weights_LU[j][k] =
						lanczos2_LU[(int) (i * pscale + 0.5)];
				s += lanczos2_weights_LU[j][k];
				i -= UNIT_XSAMPLES;
			}
			i = -i;
			for (; k < lanczos2_n_points * 2; k++) {
				lanczos2_weights_LU[j][k] =
						lanczos2_LU[(int) (i * pscale + 0.5)];
				s += lanczos2_weights_LU[j][k];
				i += UNIT_XSAMPLES;
			}
			// normalizes weights so that the sum == UNIT_YSAMPLES
			corr = UNIT_YSAMPLES / s;
			for (k = 0; k < lanczos2_n_points * 2; k++) {
				lanczos2_weights_LU[j][k] =
						(int) (lanczos2_weights_LU[j][k] * corr);
			}
		}
	}



	// interpolates one pixel
	final int lanczos2_interp_pixel(
			int[][] pd,
			int pw,
			int ph,
			int xs,
			int ys,
			int dx,
			int dy) {
		int tmpR, tmpG, tmpB;
		int itl, jtl;
		int ki, kj;
		int i, j;
		int np2, rgb;

		// cordinates of the top-left pixel to be used
		jtl = (xs) - lanczos2_n_points + 1;
		itl = (ys) - lanczos2_n_points + 1;

		// computes the index for the weights lookup table
		int iw = dx;

		// interpolates each row in the x-axis direction
		np2 = lanczos2_n_points * 2;
		//    np2 = lanczos2_n_points << 1;
		i = itl;
		for (ki = 0; ki < np2; ki++) {
			tmpR = tmpG = tmpB = 0;
			j = jtl;
			for (kj = 0; kj < np2; kj++) {
				int r, g, b;
				int i2, j2;

				// checks for out-of-bounds pixels
				i2 = i;
				j2 = j;
				if (i2 < 0)
					i2 = -i2 - 1;
				if (i2 >= ph)
					i2 = ph - (i2 - ph) - 1;
				if (j2 < 0)
					j2 = -j2 - 1;
				if (j2 >= pw)
					j2 = pw - (j2 - pw) - 1;

				rgb = pd[i2][j2];

				r = (rgb >> 16) & 0xff;
				g = (rgb >> 8) & 0xff;
				b = (rgb >> 0) & 0xff;

				int w = lanczos2_weights_LU[iw][kj];

				tmpR += r * w;
				tmpG += g * w;
				tmpB += b * w;
				//		tmpR = tmpR + r*w;
				//		tmpG = tmpG + g*w;
				//		tmpB = tmpB + b*w;

				j++;
			}
			// stores the result for the current row
			aR[ki] = tmpR;
			aG[ki] = tmpG;
			aB[ki] = tmpB;

			i++;
		}

		// computes the index for the weights lookup table
		iw = dy;

		// final interpolation in the y-axis direction
		tmpR = tmpG = tmpB = 0;
		for (ki = 0; ki < np2; ki++) {
			int w = lanczos2_weights_LU[iw][ki];
			tmpR += aR[ki] * w;
			tmpG += aG[ki] * w;
			tmpB += aB[ki] * w;
		}

		tmpR >>= SHIFT_Y;
		tmpG >>= SHIFT_Y;
		tmpB >>= SHIFT_Y;

		if (tmpR > 255)
			tmpR = 255;
		else {
			if (tmpR < 0)
				tmpR = 0;
		}
		if (tmpG > 255)
			tmpG = 255;
		else {
			if (tmpG < 0)
				tmpG = 0;
		}
		if (tmpB > 255)
			tmpB = 255;
		else {
			if (tmpB < 0)
				tmpB = 0;
		}

		return (tmpR << 16) + (tmpG << 8) + tmpB + 0xff000000;
	}


	/////////////////////////////////////////////
	// end of Lanczos2 interpolation stuff
	/////////////////////////////////////////////

	public void hs_rel2abs(int i, int j) {
		for (int k = 0; k < numhs; k++) {
			if (hs_xp[k] < 0.0D) {
				hs_xp[k] = (-hs_xp[k] * (double) i) / 100D;
				if (hs_xp[k] >= (double) i)
					hs_xp[k] = i - 1;
			}
			if (hs_yp[k] < 0.0D) {
				hs_yp[k] = (-hs_yp[k] * (double) j) / 100D;
				if (hs_yp[k] >= (double) j)
					hs_yp[k] = j - 1;
			}
			if (hs_up[k] < 0.0D && hs_up[k] != -200D) {
				hs_up[k] = (-hs_up[k] * (double) i) / 100D;
				if (hs_up[k] >= (double) i)
					hs_up[k] = i - 1;
			}
			if (hs_vp[k] < 0.0D && hs_vp[k] != -200D) {
				hs_vp[k] = (-hs_vp[k] * (double) j) / 100D;
				if (hs_vp[k] >= (double) j)
					hs_vp[k] = j - 1;
			}
		}

	}
}
