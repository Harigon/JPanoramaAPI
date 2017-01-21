package panorama.pano;

public class Utils {

	
	// calculate vertical field of view
	  public final static double math_fovy(double hFov, int vw, int vh) {
			return (360.0 / Math.PI)
				* Math.atan(
					((double) vh / (double) vw)
						* Math.tan(((hFov / 2D) * Math.PI) / 180D));
		}
	
	  
	  public static int[][] im_halfsize(int ai[][]) {
			int i = ai.length;
			int j = ai[0].length;
			int k = i >> 1;
			int l = j >> 1;
			int ai1[][];
			if ((ai1 = new int[k][l]) == null)
				return null;
			int i1 = 0;
			int j1 = 0;
			for (int k1 = 1; i1 < k; k1 += 2) {
				int ai2[] = ai[j1];
				int ai3[] = ai[k1];
				int ai4[] = ai1[i1];
				int l1 = 0;
				int i2 = 0;
				for (int j2 = 1; l1 < l; j2 += 2) {
					ai4[l1] = im_pixelaverage(ai2[i2], ai2[j2], ai3[i2], ai3[j2]);
					l1++;
					i2 += 2;
				}

				i1++;
				j1 += 2;
			}

			return ai1;
		}

	  public static byte[][] im_halfsize(byte abyte0[][]) {
			int i = abyte0.length;
			int j = abyte0[0].length;
			int k = i >> 1;
			int l = j >> 1;
			byte abyte1[][];
			if ((abyte1 = new byte[k][l]) == null)
				return null;
			int i1 = 0;
			for (int j1 = 0; i1 < k; j1 += 2) {
				byte abyte2[] = abyte0[j1];
				byte abyte3[] = abyte1[i1];
				int k1 = 0;
				for (int l1 = 0; k1 < l; l1 += 2) {
					abyte3[k1] = abyte2[l1];
					k1++;
				}

				i1++;
			}

			return abyte1;
		}

		public static final int im_pixelaverage(int i, int j, int k, int l) {
			int i1;
			if ((i1 =
				(i >> 16 & 0xff)
					+ (j >> 16 & 0xff)
					+ (k >> 16 & 0xff)
					+ (l >> 16 & 0xff) >> 2)
				< 0)
				i1 = 0;
			if (i1 > 255)
				i1 = 255;
			int j1;
			if ((j1 =
				(i >> 8 & 0xff)
					+ (j >> 8 & 0xff)
					+ (k >> 8 & 0xff)
					+ (l >> 8 & 0xff) >> 2)
				< 0)
				j1 = 0;
			if (j1 > 255)
				j1 = 255;
			int k1;
			if ((k1 = (i & 0xff) + (j & 0xff) + (k & 0xff) + (l & 0xff) >> 2) < 0)
				k1 = 0;
			if (k1 > 255)
				k1 = 255;
			return (i & 0xff000000) + (i1 << 16) + (j1 << 8) + k1;
		}
		
		
		public static final void SetMatrix(double d, double d1, double ad[][], int i) {
			double ad1[][] = new double[3][3];
			double ad2[][] = new double[3][3];
			ad1[0][0] = 1.0D;
			ad1[0][1] = 0.0D;
			ad1[0][2] = 0.0D;
			ad1[1][0] = 0.0D;
			ad1[1][1] = Math.cos(d);
			ad1[1][2] = Math.sin(d);
			ad1[2][0] = 0.0D;
			ad1[2][1] = -ad1[1][2];
			ad1[2][2] = ad1[1][1];
			ad2[0][0] = Math.cos(d1);
			ad2[0][1] = 0.0D;
			ad2[0][2] = -Math.sin(d1);
			ad2[1][0] = 0.0D;
			ad2[1][1] = 1.0D;
			ad2[1][2] = 0.0D;
			ad2[2][0] = -ad2[0][2];
			ad2[2][1] = 0.0D;
			ad2[2][2] = ad2[0][0];
			if (i == 1) {
				matrix_matrix_mult(ad1, ad2, ad);
				return;
			} else {
				matrix_matrix_mult(ad2, ad1, ad);
				return;
			}
		}

		public static final void matrix_matrix_mult(
			double ad[][],
			double ad1[][],
			double ad2[][]) {
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++)
					ad2[i][j] =
						ad[i][0] * ad1[0][j]
							+ ad[i][1] * ad1[1][j]
							+ ad[i][2] * ad1[2][j];

			}

		}
		
		
		// this version of the function does not cause strange shifts in the scaled image
		public static void scaleImage(int pd[][], int width, int height) {
			if (pd != null) {
				int ph = pd.length;
				int pw = pd[0].length;
				int scaleX = (4096 * width) / pw;
				int scaleY = (4096 * height) / ph;
				int shiftX = scaleX/2 - 2048;
				int shiftY = scaleY/2 - 2048;
				int w1 = width - 1;

				for (int y = ph - 1; y >= 0; y--) {
					int ys = y*scaleY + shiftY;
					int dy = (ys >> 4) & 0xff;
					int yd = ys >> 12; 
					int ys0, ys1;
					if (yd < 0)
						ys0 = ys1 = 0;
					else if (yd >= height - 1) {
						ys0 = ys1 = height - 1;
					} else {
						ys0 = yd++;
						ys1 = yd;
					}
					for (int x = pw - 1; x >= 0; x--) {
						int xs = x*scaleX + shiftX;
						int dx = (xs >> 4) & 0xff;
						xs >>= 12;
						int xs0;
						int xs1;
						if (xs < 0)
							xs0 = xs1 = 0;
						else if (xs >= w1) {
							xs0 = xs1 = w1;
						} else {
							xs0 = xs++;
							xs1 = xs;
						}
						pd[y][x] =
							bil(
								pd[ys0][xs0],
								pd[ys0][xs1],
								pd[ys1][xs0],
								pd[ys1][xs1],
								dx,
								dy);
					}

				}

			}
		}

		
		
//		 Scale pixel area to pwidth/pheight
//		 Use same procedure as Panorama Tools
//		void scaleImage(int pd[][], int width, int height) {
//			if (pd != null) {
//				int ph = pd.length;
//				int pw = pd[0].length;
//				int scale = (256 * width) / pw;
//				int w2 = (pw << 7) - 128;
//				int h2 = (ph << 7) - 128;
//				int sw2 = (width << 7) - 128;
//				int sh2 = (height << 7) - 128;
//				int w3 = (-w2 * width) / pw + sw2;
//				int w1 = width - 1;
//				for (int y = ph - 1; y >= 0; y--) {
//					int yd;
//					int dy = (yd = (((y << 8) - h2) * width) / pw + sh2) & 0xff;
//					int ys0;
//					int ys1;
//					if ((yd >>= 8) < 0)
//						ys0 = ys1 = 0;
//					else if (yd >= height - 1) {
//						ys0 = ys1 = height - 1;
//					} else {
//						ys0 = yd++;
//						ys1 = yd;
//					}
//					for (int x = pw - 1; x >= 0; x--) {
//						int xs;
//						int dx = (xs = x * scale + w3) & 0xff;
//						int xs0;
//						int xs1;
//						if ((xs >>= 8) < 0)
//							xs0 = xs1 = 0;
//						else if (xs >= w1) {
//							xs0 = xs1 = w1;
//						} else {
//							xs0 = xs++;
//							xs1 = xs;
//						}
//						pd[y][x] =
//							bil(
//								pd[ys0][xs0],
//								pd[ys0][xs1],
//								pd[ys1][xs0],
//								pd[ys1][xs1],
//								dx,
//								dy);
//					}
	//
//				}
	//
//			}
//		}
		
		
		public static final int bil(int p00, int p01, int p10, int p11, int dx, int dy) {
			int k1 = 255 - dx;
			int l1 = 255 - dy;
			int i2 = k1 * l1;
			int j2 = dy * k1;
			int k2 = dx * dy;
			int l2 = dx * l1;
			int i3 =
				i2 * (p00 >> 16 & 0xff)
					+ l2 * (p01 >> 16 & 0xff)
					+ j2 * (p10 >> 16 & 0xff)
					+ k2 * (p11 >> 16 & 0xff) & 0xff0000;
			int j3 =
				i2 * (p00 >> 8 & 0xff)
					+ l2 * (p01 >> 8 & 0xff)
					+ j2 * (p10 >> 8 & 0xff)
					+ k2 * (p11 >> 8 & 0xff) >> 16;
			int k3 =
				i2 * (p00 & 0xff)
					+ l2 * (p01 & 0xff)
					+ j2 * (p10 & 0xff)
					+ k2 * (p11 & 0xff) >> 16;
			return i3 + (j3 << 8) + k3 + 0xff000000;
		}
		
		
		
		public static int[][] im_allocate_pano(int ai[][], int i, int j) {
			if (ai == null || ai.length != j || ai[0].length != i)
				try {
					return new int[j][i];
				} catch (Exception _ex) {
					return null;
				} else
				return ai;
		}

		public static void im_drawGrid(int ai[][], int i, int j) {
			int k3 = i | 0xff000000;
			int l3 = j | 0xff000000;
			if (ai != null) {
				int i4 = ai.length;
				int j4 = ai[0].length;
				for (int j1 = 0; j1 < i4; j1++) {
					for (int k = 0; k < j4; k++)
						ai[j1][k] = k3;

				}

				int k1 = 0;
				for (int k2 = (36 * i4) / j4; k2 >= 0; k2--) {
					int i3 = k1 + 1;
					for (int l = 0; l < j4; l++) {
						ai[k1][l] = l3;
						ai[i3][l] = l3;
					}

					if (k2 != 0)
						k1 += (i4 - 2 - k1) / k2;
				}

				int i1 = 0;
				for (int l2 = 36; l2 >= 0; l2--) {
					if (i1 == 0) {
						for (int l1 = 0; l1 < i4; l1++)
							ai[l1][i1] = l3;

					} else if (i1 >= j4 - 1) {
						i1 = j4 - 1;
						l2 = 0;
						for (int i2 = 0; i2 < i4; i2++)
							ai[i2][i1] = l3;

					} else {
						int j3 = i1 + 1;
						for (int j2 = 0; j2 < i4; j2++) {
							ai[j2][i1] = l3;
							ai[j2][j3] = l3;
						}

					}
					if (l2 != 0)
						i1 += (j4 - 1 - i1) / l2;
				}

			}
		}

		// Set alpha channel in rectangular region of
		// two dimensional array p  to 'alpha'
		public static void SetPAlpha(int x0, int y0, int x1, int y1, int alpha, int p[][]) {
			int hmask = (alpha << 24) + 0xffffff;
			int h = p.length;
			int w = p[0].length;
			int ymin;
			if ((ymin = Math.min(y0, y1)) < 0)
				ymin = 0;
			int ymax;
			if ((ymax = Math.max(y0, y1)) >= h)
				ymax = h - 1;
			if (x0 < 0)
				x0 = 0;
			if (x0 >= w)
				x0 = w - 1;
			if (x1 < 0)
				x1 = 0;
			if (x1 >= w)
				x1 = w - 1;
			if (x1 >= x0) {
				for (int y = ymin; y <= ymax; y++) {
					for (int x = x0; x <= x1; x++)
						p[y][x] &= hmask;

				}

				return;
			}
			for (int y = ymin; y <= ymax; y++) {
				for (int x = 0; x <= x1; x++)
					p[y][x] &= hmask;

				for (int x = x0; x < w; x++)
					p[y][x] &= hmask;

			}

		}


		public static void im_insertRect(
			int pd[][],
			int xd,
			int yd,
			int id[],
			int iwidth,
			int xs,
			int ys,
			int width,
			int height) {
			try {
				int y = 0;
				for (int yp = yd; y < height; yp++) {
					int x = 0;
					for (int idx = (ys + y) * iwidth + xs; x < width; idx++) {
						int px;
						if (((px = id[idx]) & 0xff000000) != 0) { //Non transparent
		
							int xp = x + xd;
							pd[yp][xp] = px & (pd[yp][xp] | 0xffffff);
						}
						x++;
					}
		
					y++;
				}
		
				return;
			} catch (Exception _ex) {
				System.out.println("Insert can't be fit into panorama");
			}
		}

		
}
