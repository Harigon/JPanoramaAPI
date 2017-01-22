/* PTViewer	-	Interactive Viewer for Panoramic Images
   Copyright (C) 2000 - Helmut Dersch  der@fh-furtwangen.de
   
   This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 2, or (at your option)
   any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.  */

/*------------------------------------------------------------*/

/*
 * PTViewer 2.8
 * 
 * Based on version 2.5 by Helmut Dersch
 * Modified by Fulvio Senore (fsenore@ica-net.it)
 * 
 * Version 2.7L2:
 *  beta1 
 *   added parameter value quality=4 to activate Lanczos2 interpolator
 *   speeded up panning (bilinear only)
 *   fixed: the mouse cursor did not change to HAND when over a hotspot and using the Sun VM
 *   fixed: pressing the spacebar did not show hotspots until the pano was dragged
 *   fixed: a custom mouse cursor did not show up until the mouse was moved
 *  beta2
 *   now when panning very slowly the image does not move in strange ways any more
 *  beta3
 *   panning and zooming with the keyboard now uses bilinear instead of Lanczos2 
 *     so it's faster (thanks to Vladimir Simunic)
 *   now if using the Sun VM V1.4 or later ptviewer draws the panorama frames using
 *     hardware accelerated graphics. On Windows it is now faster than with the Microsoft VM
 *  beta4
 *   added new parameter "dynLoadROIs" (=true) to dynamically load a panorama sliced as ROIs
 *     depending on the current view direction 
 *   fixed: when diplaying a wait image with the Sun VM the background of the applet
 *     was not set to the background colour
 *   modified ptzoom.java in order to make it work with quality==4. At the moment the image is
 *     rendered with bilinear interpolation only
 *   added new parameter "hsEnableVisibleOnly" (default false). If set to true hotspots
 *     will be disabled is not visible
 *  beta5
 *   fixed: with the beta4 version, under certain circumstances, text hotspots did not
 *     use the bgcolor setting  
 *   small speedup in ptImageTo2DArray()
 *   added new parameter showToolbar (default false) if true shows an integrated toolbar
 *   added new parameter imgLoadFeedbak (default true): if set to false ptviewer will
 *     not give feedback of the image loading progress
 *   added new parameter toolbarImage: name of the image used to draw the toolbar
 *     default: Toolbar.gif file contained in the jar file
 *  beta6
 *    fixed: if the panorama was not fully spherical hotspots were displayed at a
 *      wrong y coordinate
 *    fixed: the toolbar sometimes incorrectly showed an OVER button when leaving the
 *      applet passing over a button. Now the toolbar handles also the mouseExit event 
 *    fixed: the toolbad did not update the HS button when using the 
 *      ptviewer.showHS() and ptviewer.hideHS() methods
 *    added a flag to avoid a lot of unnecessary paint()s while dynLoading ROIs
 *      now loading is much faster
 *    fixed: with some computers the zoom buttons did not work properly because
 *      ptviewer.paint() did not execute
 *    added a limit to maximum zoom speed with the toolbar to avoid too fast panning 
 *      with very fast computers
 *  beta7
 *    fixed: the previous fix to avoid unnecessary paint()s made it impossible to pan
 *      while dynLoading the pano
 *    fixed: calling moveTo() or gotoView() from a link while dynLoading ROIs caused
 *      ptviewer to keep panning until you clicked with the mouse. Now these functions 
 *      are disabled while dynLoading
 *    modified the gotoView(): now if the requested fov is too large to fit in the current
 *      pano, it is reduced. In previous versions this caused the function to do nothing.
 *      this function was also called by moveTo(), that had the same problem
 *    if the tilt parameters requests an impossible direction of view (outside the pano)
 *      now the the tilt angle is set to the maximum or minimum possible value. previous versions
 *      set it to 0
 *    if using the toolbar the hotspot's description is also written to the toolbar when
 *      the mouse moves over the hotspot
 *      previous versions only wrote it to the browser's status bar
 *    fixed bug: the viewer crashed if the parameters 
 *      specified a wait image and the integrated toolbar
 *  beta8
 *    added new parameter "autoTime". Used with the auto parameter, it sets the time in seconds
 *      for a full 360 degrees pano revolution
 *    added public method startAutoPan( pan_inc,  tilt_inc, zoom, autoTime ): the last
 *      parameter works like the autoTime applet parameter
 *    now the toolbar is painted before static hotspots, 
 *      so it is possible to draw shotspots over the toolbar
 *    added new parameter "toolbarDescr_x" to set the x coordinate (in pixels) of the
 *      hotspots description in the toolbar
 *    added new paramater "toolbarText_color" to set the color of the hotspots description 
 *      in the toolbar. The default is black. This value is overridden by the "c" parameter
 *      in the hotspot's definition (if any) 
 *    modified the PTViewerScript() function: now the last command can end with a ";"
 *      without causing an exception
 * 
 *
 * Version 2.7.1L2:
 * 
 *  beta1
 *    added new parameter "shsEnableVisibleOnly" (default false). If set to true static hotspots
 *      will be disabled is not visible
 *    fixed: when loading new panos with newPanoFromList() the toolbar did reset its
 *      properties (text color and position) to the default. Now it keeps the values
 *      set with <param> tags.
 *    fixed: when loading new panos with newPanoFromList() from a hotspot the hotspot
 *      button of the toolbar went out of sync
 *    fixed: when using the toolbar and the parameter "view_height" the toolbar moved up
 *      each time that a pano was loaded with newPanoFromList()
 *    changed: now the tiltmax parameter accepts negative values and the tiltmin parameter
 *      accepts positive ones (hard to believe, but somebody asked for it!)
 *    added two public methods: getPanoIsLoaded() and getFilename()
 * 
 *  beta2
 *    modified scaleImage() to avoid image shift wit some image sizes
 *    added parameter "popup_panning", originally added by David Buxo to his version of PTViewer
 *    Tore Meyer (Tore.Meyer@gmx.de) added optional parameter "autoTime" to moveTo() and moveFromTo() 
 * 
 * Version 2.8:
 * 
 *  beta1
 *    modified the values for the "quality" parameter:
 *      now 4 means nearest neighbour when panning, lanczos2 when steady,
 *          5 means bilinear when panning, lanczos2 when steady.
 *          6 means nn if panning fast, bil if panning slowly, lanczos2 if steady
 *      the new default is 6
 *    modified math_transform to optimize also nn interpolation
 *    rewritten math_transform() to enable interpolation of the geometric transform
 *      between lines. Now the transform is computed using longs instead of ints to
 *      achieve more resolution: zooming in a lot and panning slowly does not cause strange
 *      movements anymore
 *    Rik Littlefield (rj.littlefield@computer.org) has rewritten the image loading code:
 *      now it is much faster, expecially with larger images
 *      As a consequence the "maxarray" is no longer used by the applet. Using maxarray 
 *      will not cause errors, but it will not be used.
 * 
 *   beta2
 *    now dynamic loading of ROIs is faster. This is more visible once the images are
 *      cached in the local computer. No more drawing on invisible ROIs, drawing uses bilinear
 *      to be faster.
 *    fixed bug: when using nn interpolator and a rectangular hotspot the hotspot was not
 *      painted correctly while panning
 *    added new parameter "mouseSensitivity", it is a decimal number, default = 1
 *      if mouseSensitivity < 1 panning will be slower
 *      if mouseSensitivity > 1 panning will be faster
 *    added new parameter "mouseQ6Threshold" used only if quality=6. It is a decimal number, default = 1
 *      mouseQ6Threshold > 1 will require a larger mouse movement to switch from bilinear to nn
 *      mouseQ6Threshold < 1 will require a smaller mouse movement to switch from bilinear to nn
 *    fixed: when loading a pano that was not tall enough to fit in the current window
 *      with the current fov ptviewer reduced too much the fov value.
 *      Now that vale is reduced to the correct value that will no require vertical panning
 * 
 *   beta3
 *    added support for *.ptv and *.ptvref custom files
 * 
 *   beta4
 *    added new parameter "outOfMemoryURL": it is a link to a page to be opened in case of 
 *      out of memory error while loading the pano 
 *    some web servers (like IIS 6) by default do not serve files with unknown extensions, but they
 *      send a "file not found". To bypass this problem .ptv and .ptvref files can be renamed as follows:
 *        pano.ptvref ==> pano.ptvref.txt
 *        pano.ptv ==> pano.ptv.jpg
 *      the trailing extension does not need to be "txt" or "jpg" but they can be any
 *      extension known by the server
 *    added parameter "mousePanTime" used to limit the maximum speed when panning with
 *      the mouse. It works like "autoTime": it is the minimum time (in seconds) needed for a full
 *      360 degrees revolution
 *    fixed: the applet did not load images (like a wait screen) if they were packed in the jar file
 *  
 *   beta5
 *    Rober Bisland (R.Bilsland@Dial.pipex.com) added a new Javascript command, DrawSHSPopup(), 
 *      and new functionality that allows multiple static hotspots to be drawn, hidden and popped up at once.
 *    he also added a new parameter: "shsStopAutoPanOnClick" (default true). If set to false clicking 
 *      on a static hotspot will not stop an AutoPan.
 *    added support for encrypted .ptv files.
 *    Ercan Gigi (ercan.gigi@philips.com) added the "autoNumTurns" parameter which is used 
 *      to limit the number of full 360 degree turns when auto-panning is on.
 * 
 * 	 beta6
 *    changed the encryption keys format from String to byte[] to avoid problems with Mac
 *      and Linux when some bytes have negative values.
 *    now the "Y" key can be used to zoom out, like the "Z" key. This helps with some
 *      keyboard layouts (Germany).
 *    fixed: if an encrypted .ptv file contained a tile smaller than 2000 bytes it
 *      caused the viewer to crash.
 * 
 *   beta7
 *    fixed: when autopanning and quality=6 moving the mouse over the viewer caused
 *      the interpolator to switch to nearest neighbour reducing image quality
 *    added new parameter "horizonPosition" to specify the position of the horizon
 *      if it is not in the middle of the pano image
 *    pressing "o" will interactively decrease the value of the horizonPosition parameter
 *      with feedback in the status bar, pressong "O" will increase it. This is useful to quickly
 *      find the correct value for the parameter. Note that after pressing the key you will 
 *      have to pan in order to see the effect. Hotspots position will not be updated since
 *      it would require more work and this feature is not intended for production use.
 *    added paramether "authoringMode" (default = false). If set to true it will enable 
 *      authoring features that could have unexpected results for end users
 *      at the moment it will enable the "o" key. 
 *    added parameter "toolbarBoldText", default false. If set to true the hotspots description
 *      in the toolber will be written in bold
 *    added parameter "statusMessage": it can be used to specify a fixed text to be written
 *      in the status bar. The text is written everytime the user drags the mouse or presses an arrow key
 *      this parameter does not work with Firefox (for security reasons)
 *    added parameter "hsShowDescrInStatusBar" (default = true). If set to false 
 *      the applet will not show the hotspots' description in the browser's status
 *      bar when the mouse moves over a hotspot
 *    changed default mouse sensitivity because panning was too fast with modern computers
 * 	  added new features in static hotspots declaration: 
 *      "y" and "b" values can be negative: if negative they are computed from the bottom 
 *      of the viewer window and not from the top
 *      "x" and "a" values can be negative: if negative they are computed from the right 
 *      of the viewer window and not from the left
 *      "x" must compute to a positive number lower than "a"
 *      "y" must compute to a positive number lower than "b"
 * 
 *   beta8
 *    fixed: when using param "pano0" and so on, the handling of parameters between
 *      "{" and "}" was case sensitive while normal parameters processing was
 *      NOT case sensitive. Now this handling is case insensitive too.
 *    changed: in .ptv files handling, references to the web address hosting the image
 *      (used to decrypt an encrypted image)
 *      were done using getCodeBase(). Now they are done using getDocumentBase() so
 *      it should not be possible to create a page on a server that shows a file from
 *      a second server using the copy of ptviewer stored in the second server
 *      (image stealing).
 * 
 */


import java.applet.*;
import java.awt.*;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.util.*;

import panorama.pano.Renderer;
import panorama.pano.Utils;

public class ptviewer extends Applet implements Runnable {

	public Renderer renderer;

	
	public ptviewer() {
		
		
		renderer = new Renderer();
		
// FS+
		quality = 6;
		backBuffer = null;
		tlbObj = null;
		dynLoadROIs = false;
		loadingROI = false;
		imgLoadFeedback = true;
		outOfMemoryURL = null;
		statusMessage = null;
		showToolbar = false;
		hsShowDescrInStatusBar = true;
		hsEnableVisibleOnly = false;
		shsEnableVisibleOnly = false;
		shsStopAutoPanOnClick = true;
		popupPanning = false;
		lastMouseX = lastMouseY = -1;
		tlbImageName = null;
		org_vheight = 0;
		usingCustomFile = false;
// FS-
		inited = false;
		bgcolor = null;
		waittime = 0L;
		WaitDisplayed = false;
		view = null;
		dwait = null;
		frame = null;
		offImage = null;
		offGraphics = null;
		offwidth = 0;
		offheight = 0;
		awidth = 320;
		aheight = 200;
		vset = false;
		vx = 0;
		vy = 0;
		show_pdata = true;
		ready = false;
		hsready = false;
		PanoIsLoaded = false;
		fatal = false;
		mouseInWindow = true;
		mouseInViewer = true;
		panning = false;
		renderer.dirty = true;
		showhs = false;
		showCoordinates = false;
		oldx = 0;
		oldy = 0;
		newx = 0;
		newy = 0;
		ptcursor = 0;
		MASS = 0.0D;
		oldspeedx = 0.0D;
		oldspeedy = 0.0D;
		autopanFrameTime = 0;
		autotilt = 0.0D;
		zoom = 1.0D;
		pan_steps = 20D;
		filename = null;
		inits = null;
		MouseOverHS = null;
		GetView = null;
		click_x = -1;
		click_y = -1;
		ptimer = 0L;
		loadPano = null;
		ptviewerScript = null;
		PTScript = null;
		PTViewer_Properties = null;
		loadAllRoi = true;
		CurrentPano = -1;
		sender = null;
		preloadthread = null;
		preload = null;
		order = null;
		im_maxarray = 0x80000;
		grid_bgcolor = 0xffffff;
		grid_fgcolor = 0;
		file_Cache = null;
		file_cachefiles = true;
		pb_color = Color.gray;
		pb_x = -1;
		pb_y = -1;
		pb_width = -1;
		pb_height = 10;
		percent = null;
		numshs = 0;
		curshs = -1;
		shotspots = null;
		numroi = 0;
		sounds = null;
		applets = null;
		app_properties = null;
		hotspots = null;
		curhs = -1;
		hs_image = null;
		authoringMode = false;
	}

	public ptviewer(int ai[][]) {
		quality = 6;
		dynLoadROIs = false;
		loadingROI = false;
		showToolbar = false;
		imgLoadFeedback = true;
		outOfMemoryURL = null;
		outOfMemoryURL = null;
		lastMouseX = lastMouseY = -1;
		hsShowDescrInStatusBar = true;
		hsEnableVisibleOnly = false;
		shsEnableVisibleOnly = false;
		shsStopAutoPanOnClick = true;
		popupPanning = false;
		tlbImageName = null;
		org_vheight = 0;
		usingCustomFile = false;
		ai = null;
		inited = false;
		bgcolor = null;
		waittime = 0L;
		WaitDisplayed = false;
		view = null;
		dwait = null;
		frame = null;
		offImage = null;
		backBuffer = null;
		tlbObj = null;
		offGraphics = null;
		offwidth = 0;
		offheight = 0;
		renderer.source = null;
		awidth = 320;
		aheight = 200;
		renderer.vwidth = 0;
		renderer.vheight = 0;
		vset = false;
		vx = 0;
		vy = 0;
		renderer.pwidth = 0;
		renderer.pheight = 0;
		renderer.vdata = null;
		renderer.hs_vdata = null;
		renderer.imagePixels = null;
		show_pdata = true;
		ready = false;
		hsready = false;
		PanoIsLoaded = false;
		fatal = false;
		mouseInWindow = true;
		mouseInViewer = true;
		panning = false;
		renderer.dirty = true;
		showhs = false;
		showCoordinates = false;
		oldx = 0;
		oldy = 0;
		newx = 0;
		newy = 0;
		ptcursor = 0;
		renderer.yaw = 0.0D;
		renderer.hfov = 70D;
		renderer.hfov_min = 10.5D;
		renderer.hfov_max = 165D;
		renderer.pitch = 0.0D;
		renderer.pitch_max = 90D;
		renderer.pitch_min = -90D;
		renderer.yaw_max = 180D;
		renderer.yaw_min = -180D;
		MASS = 0.0D;
		oldspeedx = 0.0D;
		oldspeedy = 0.0D;
		renderer.autopan = 0.0D;
		autopanFrameTime = 0;
		autotilt = 0.0D;
		zoom = 1.0D;
		pan_steps = 20D;
		filename = null;
		inits = null;
		MouseOverHS = null;
		GetView = null;
		click_x = -1;
		click_y = -1;
		renderer.frames = 0L;
		renderer.lastframe = 0L;
		ptimer = 0L;
		loadPano = null;
		ptviewerScript = null;
		PTScript = null;
		PTViewer_Properties = null;
		loadAllRoi = true;
		CurrentPano = -1;
		sender = null;
		preloadthread = null;
		preload = null;
		order = null;
		renderer.antialias = false;
		renderer.scaledPanos = null;
		renderer.max_oversampling = 1.5D;
		im_maxarray = 0x80000;
		grid_bgcolor = 0xffffff;
		grid_fgcolor = 0;
		file_Cache = null;
		file_cachefiles = true;
		pb_color = Color.gray;
		pb_x = -1;
		pb_y = -1;
		pb_width = -1;
		pb_height = 10;
		percent = null;
		numshs = 0;
		curshs = -1;
		shotspots = null;
		renderer.atan_LU_HR = null;
		renderer.atan_LU = null;
		renderer.PV_atan0_HR = 0;
		renderer.PV_pi_HR = 0;
		numroi = 0;
		sounds = null;
		applets = null;
		app_properties = null;
		hotspots = null;
		renderer.numhs = 0;
		curhs = -1;
		hs_image = null;
		renderer.imagePixels = ai;
		PanoIsLoaded = true;
		renderer.math_setLookUp(renderer.imagePixels);
		filename = "Pano";
		renderer.horizonPosition = 50;
		authoringMode = false;
	}

	void initialize() {
		renderer.numhs = 0;
		curhs = -1;
		curshs = -1;
		numroi = 0;
		loadAllRoi = true;
		renderer.yaw = 0.0D;
		renderer.hfov = 70D;
		renderer.hfov_min = 10.5D;
		renderer.hfov_max = 165D;
		renderer.pitch = 0.0D;
		renderer.pitch_max = 90D;
		renderer.pitch_min = -90D;
		renderer.yaw_max = 180D;
		renderer.yaw_min = -180D;
		renderer.autopan = 0.0D;
		autopanFrameTime = 0;
		autotilt = 0.0D;
		zoom = 1.0D;
		renderer.pwidth = 0;
		renderer.pheight = 0;
		stopPan();
		renderer.lastframe = 0L;
		renderer.dirty = true;
		showhs = false;
		showCoordinates = false;
		MouseOverHS = null;
		GetView = null;
		WaitDisplayed = false;
		pan_steps = 20D;
		order = null;
		renderer.horizonPosition = 50;
	}

	public void init() {
		fatal = false;
		preloadthread = null;
		preload = null;
		ptcursor = 0;
		file_init();
		renderer.math_init();
		// FS+
		useVolatileImage = canUseAcceleratedGraphic();
		renderer.lanczos2_init();
		if( useVolatileImage )
			vImgObj = new vimage( this );
		// FS-
		pb_init();
		app_init();
		snd_init();
		shs_init();
		hs_init();
		sender = new Hashtable();
		inited = true;
		repaint();
		byte abyte0[];
		if ((abyte0 = file_read("PTDefault.html", null)) != null)
			PTViewer_Properties = new String(abyte0);
		initialize();
		if (PTViewer_Properties != null)
			ReadParameters(PTViewer_Properties);
		ReadParameters(null);
		if (filename != null && filename.startsWith("ptviewer:")) {
			int i =
				Integer.parseInt(filename.substring(filename.indexOf(':') + 1));
			if (myGetParameter(null, "pano" + i) != null) {
				filename = null;
				ReadParameters(myGetParameter(null, "pano" + i));
			}
		}
	}

	public String getAppletInfo() {
		return "PTViewer version 2.8 - Based on 2.5 by Helmut Dersch - Modified by Fulvio Senore www.fsoft.it/panorama/ptviewer.htm";
	}

	public void start() {
		if (loadPano == null) {
			loadPano = new Thread(this);
			loadPano.start();
		}
	}

	public synchronized void stop() {
		stopThread(preloadthread);
		preloadthread = null;
		stopThread(loadPano);
		loadPano = null;
		stopAutoPan();
		stopPan();
		stopApplets(0);
		ready = false;
		hsready = false;
		renderer.vdata = null;
		renderer.hs_vdata = null;
		view = null;
		if (!vset) {
			renderer.vwidth = 0;
			renderer.vheight = 0;
		}
		offImage = null;
		backBuffer = null;
//		tlbObj = null;
		if( tlbObj != null ) ((toolbar) tlbObj).setMessage( "" );
		renderer.scaledPanos = null;
	}

	synchronized void PV_reset() {
		ready = false;
		hsready = false;
		hs_dispose();
		roi_dispose();
		PanoIsLoaded = false;
		filename = null;
		MouseOverHS = null;
		GetView = null;
		pb_reset();
		inits = null;
		order = null;
		System.gc();
	}

	public synchronized void destroy() {
		stopThread(ptviewerScript);
		ptviewerScript = null;
		PV_reset();
		if (sender != null) {
			sender.clear();
			sender = null;
		}
		renderer.vdata = null;
		renderer.hs_vdata = null;
		renderer.source = null;
		frame = null;
		view = null;
		dwait = null;
		renderer.imagePixels = null;
		renderer.math_dispose();
		shs_dispose();
		snd_dispose();
		System.gc();
	}
	
	

	public void run() {
		
		
		System.out.println("running");
		
		int k;
		try {
			// added a try block to catch out of memory error
			if (Thread.currentThread() == preloadthread && preload != null) {
				int i;
				k = getNumArgs(preload, ',');
				i = k;
				if (k > 0) {
					for (int j = 0; j < i; j++) {
						String s1;
						if ((s1 = getArg(j, preload, ',')) != null
								&& file_cachefiles && file_Cache != null
								&& file_Cache.get(s1) == null && s1 != filename)
							file_read(s1, null);
					}

				}
				return;
			}
			if (Thread.currentThread() == ptviewerScript) {
				if (PTScript != null)
					PTViewerScript(PTScript);
				return;
			}
			ResetCursor();
			if (!PanoIsLoaded) {
				show_pdata = true;
				if (filename == null)
					if (renderer.pwidth != 0)
						filename = "_PT_Grid";
					else
						show_pdata = false;
				if (filename != null && filename.toLowerCase().endsWith(".mov")){

					int[][] pixels = im_loadPano(filename, renderer.imagePixels, renderer.pwidth, renderer.pheight);

					if(pixels != null){
						int height = pixels.length;
						int width = pixels[0].length;
						renderer.setImage(pixels, width, height);
					}
					
				} else {

					//System.out.println("width: "+renderer.pwidth);

					int[][] pixels = im_loadPano(filename, renderer.imagePixels, renderer.pwidth, renderer.pheight);

					if(pixels != null){
						int height = pixels.length;
						int width = pixels[0].length;
						renderer.setImage(pixels, width, height);
					}
					//System.out.println("width2: "+renderer.pwidth);

					if (showToolbar) {
						((toolbar) tlbObj).setBarPerc(0); // clears the progress
						// bar
					}
				}
				System.gc();
			}
			if (renderer.imagePixels == null) {
				fatal = true;
				repaint();
				return;
			}
			if (filename != null && filename.toLowerCase().endsWith(".mov"))
				try {
					String s = " {file=" + filename + "} ";
					if (order != null)
						s = s + "{order=" + order + "} ";
					if (renderer.antialias) {
						s = s + "{antialias=true} ";
						s = s + "{oversampling=" + renderer.max_oversampling + "} ";
					}
					Applet applet;
					(applet = (Applet) Class.forName("ptmviewer")
							.getConstructor(
									new Class[] { Class.forName("ptviewer"),
											java.lang.String.class })
							.newInstance(new Object[] { this, s })).init();
					applet.start();
					System.gc();
				} catch (Exception _ex) {
				}

			// check if the horizon is not in the middle of the image
			renderer.pitch_min_org = renderer.pitch_min;
			renderer.pitch_max_org = renderer.pitch_max;
			renderer.CheckHorizonPosition();

			if (renderer.hfov > renderer.yaw_max - renderer.yaw_min)
				renderer.hfov = renderer.yaw_max - renderer.yaw_min;
			if (!PanoIsLoaded)
				renderer.math_setLookUp(renderer.imagePixels);
			finishInit(PanoIsLoaded);
			if( statusMessage != null ) showStatus( statusMessage );
		} catch (OutOfMemoryError ex) {
			if (outOfMemoryURL != null) {
				// opens a page that should contain an explication of the out of memory problem
				JumpToLink( outOfMemoryURL, null );
			} else {
				throw ex;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	void finishInit(boolean flag) {
		
		System.out.println("finish init");
		
		if (!flag)
			shs_setup();
		ready = true;
		requestFocus();
		ResetCursor();
		repaint();
		paint(getGraphics());
		if (loadAllRoi && !PanoIsLoaded) {
			if( dynLoadROIs )
				loadROI_dyn();
			else
				loadROI(0, numroi - 1);
		}
		if( !PanoIsLoaded && usingCustomFile ) {
			ptvf.loadTiles();
		}
		if (!PanoIsLoaded){
			
			hs_setup(renderer.imagePixels);
		}
		hsready = true;
		PanoIsLoaded = true;
		if (renderer.autopan != 0.0D)
		{			
			// E.Gigi - 2005.06.12
			if (autoNumTurns != 0.0D)
				renderer.lastframe = renderer.frames + (int) (autoNumTurns*360/renderer.autopan);
			else
				renderer.lastframe = renderer.frames + 0x5f5e100L;
		}
		int i;
		if (inits != null)
			if ((i = inits.indexOf('*')) == -1)
				JumpToLink(inits, null);
			else
				JumpToLink(inits.substring(0, i), inits.substring(i + 1));
		// FS+
		// to be able to show hotspots before panning
		renderer.dirty = true;
		if( tlbObj != null ) ((toolbar) tlbObj).syncHSButton();		// the hs button remains pressed if we omit this line
		// to show a cursor different from default at startup
		if( ptcursor != 0 ) 
			setCursor(Cursor.getPredefinedCursor(ptcursor));
		// FS-
		repaint();
		SetupSounds();
		if (preload != null && preloadthread == null) {
			preloadthread = new Thread(this);
			try {
				preloadthread.setPriority(1);
			} catch (SecurityException _ex) {
			}
			preloadthread.start();
		}
	}

	public boolean mouseDown(Event event, int i, int j) {
		boolean performStopAutoPan;

		if( tlbObj != null ) ((toolbar) tlbObj).mouseDown( i, j );

		// status bar message
		if( statusMessage != null ) showStatus( statusMessage );
		
		lastMouseX = i;
		lastMouseY = j;
		if (i >= vx && i < vx + renderer.vwidth && j >= vy && j < vy + renderer.vheight) {
			if (renderer.lastframe > renderer.frames) {
				stopThread(ptviewerScript);
				ptviewerScript = null;
				performStopAutoPan = true;
				if (!shsStopAutoPanOnClick) {
					if (hsready) {
						if (curshs >= 0) {
							performStopAutoPan = false;
						}
					}
				}
				if (performStopAutoPan) {
					stopAutoPan();
				}
				oldx = i;
				oldy = j;
				return true;
			}
			if (showCoordinates) {
				showStatus(renderer.DisplayHSCoordinates(i - vx, j - vy));
				showCoordinates = false;
				return true;
			}
		}
		if (!panning && mouseInViewer) {
			oldx = i;
			oldy = j;
			if (curhs < 0) {
				panning = true;
				if (event.shiftDown())
					zoom = 0.970873786407767D;
				else if (event.controlDown())
					zoom = 1.03D;
				else
					zoom = 1.0D;
				repaint();
				PVSetCursor(i, j);
			}
		}
		newx = i;
		newy = j;
		return true;
	}

	public boolean mouseDrag(Event event, int i, int j) {

		if( tlbObj != null ) ((toolbar) tlbObj).mouseDrag( i, j );

		lastMouseX = i;
		lastMouseY = j;
		newx = i;
		newy = j;
		if (mouseInViewer) {
			panning = true;
			if (event.shiftDown())
				zoom = 0.970873786407767D;
			else if (event.controlDown())
				zoom = 1.03D;
			else
				zoom = 1.0D;
			ResetCursor();
		}
		repaint();
		return true;
	}

	public boolean mouseUp(Event event, int i, int j) {

		if( tlbObj != null ) ((toolbar) tlbObj).mouseUp( i, j );

		lastMouseX = i;
		lastMouseY = j;
		newx = i;
		newy = j;
		stopPan();
		zoom = 1.0D;
		if (hsready) {
			if (curshs >= 0) {
				for (int k = 0; k < numshs; k++)
					if (shs_active[k])
						gotoSHS(k);

			} else if (curhs >= 0) {
				gotoHS(curhs);
				for (int l = curhs + 1; l < renderer.numhs && curhs != -1; l++)
					if (hs_link[l] == curhs)
						gotoHS(l);

				if (curhs < 0)
					return true;
			}
			PVSetCursor(i, j);
			click_x = i;
			click_y = j;
		}
		return true;
	}

	public boolean mouseEnter(Event event, int i, int j) {
		lastMouseX = i;
		lastMouseY = j;
		mouseInWindow = true;
		mouseInViewer = is_inside_viewer(i, j);
		PVSetCursor(i, j);
		return true;
	}

	public boolean mouseExit(Event event, int i, int j) {

		if( tlbObj != null ) ((toolbar) tlbObj).mouseExit( i, j );

		lastMouseX = i;
		lastMouseY = j;
		mouseInWindow = mouseInViewer = false;
		stopPan();
		zoom = 1.0D;
		ResetCursor();
		return true;
	}

	public boolean keyDown(Event event, int i) {
		if (!ready)
			return true;
		switch (i) {
			default :
				break;
			case 1004 :
				if( statusMessage != null ) showStatus( statusMessage );
				keyPanning = true;
				panUp();
				break;

			case 1005 :
				if( statusMessage != null ) showStatus( statusMessage );
				keyPanning = true;
				panDown();
				break;

			case 1006 :
				if( statusMessage != null ) showStatus( statusMessage );
				keyPanning = true;
				panLeft();
				break;

			case 1007 :
				if( statusMessage != null ) showStatus( statusMessage );
				keyPanning = true;
				panRight();
				break;

			case 43 : // '+'
			case 46 : // '.'
			case 61 : // '='
			case 62 : // '>'
			case 65 : // 'A'
			case 97 : // 'a'
				keyPanning = true;
				ZoomIn();
				break;

			case 44 : // ','
			case 45 : // '-'
			case 60 : // '<'
			case 90 : // 'Z'
			case 95 : // '_'
			case 122 : // 'z'
			case 'y' :
			case 'Y' :
				keyPanning = true;
				ZoomOut();
				break;

			case 32 : // ' '
				toggleHS();
				break;

			case 73 : // 'I'
			case 105 : // 'i'
				showStatus(getAppletInfo());
				break;

			case 118 : // 'v'
				showStatus(
					"pan = "
						+ (double) (int) (renderer.yaw * 100D) / 100D
						+ "deg; tilt = "
						+ (double) (int) (renderer.pitch * 100D) / 100D
						+ "deg; fov = "
						+ (double) (int) (renderer.hfov * 100D) / 100D
						+ "deg");
				break;

			case 80 : // 'P'
			case 112 : // 'p'
				showStatus(m1());
				break;

			case 85 : // 'U'
			case 117 : // 'u'
				showStatus(getDocumentBase().toString());
				break;

			case 104 : // 'h'
				showCoordinates = true;
				showStatus("Click Mouse to display X/Y Coordinates");
				break;

			case 10 : // '\n'
				if (!hsready)
					break;
				if (curshs >= 0) {
					for (int j = 0; j < numshs; j++)
						if (shs_active[j])
							gotoSHS(j);

					break;
				}
				if (panning || curhs < 0)
					break;
				gotoHS(curhs);
				for (int k = curhs + 1; k < renderer.numhs && curhs != -1; k++)
					if (hs_link[k] == curhs)
						gotoHS(k);

				if (curhs < 0)
					return true;
				break;
				
			case 'O' :
				// moves the horizon up
				if( authoringMode && renderer.horizonPosition < 100 ) {
					renderer.horizonPosition++;
					renderer.CheckHorizonPosition();
					showStatus( "horizonPosition = " + renderer.horizonPosition );
					renderer.dirty = true;
					// the double call is needed to properly update che screen
					// I don't know why but if I call "gotoView( yaw, pitch, hfov )" thet is what I need the results are not correct
					gotoView( renderer.yaw + 1, renderer.pitch, renderer.hfov );
					gotoView( renderer.yaw - 1, renderer.pitch, renderer.hfov ); 
					repaint();
				}
				break;
			case 'o' :
				// moves the horizon down
				if( authoringMode && renderer.horizonPosition > 0 ) {
					renderer.horizonPosition--;
					renderer.CheckHorizonPosition();
					showStatus( "horizonPosition = " + renderer.horizonPosition );
					renderer.dirty = true;
					gotoView( renderer.yaw + 1, renderer.pitch, renderer.hfov );
					gotoView( renderer.yaw - 1, renderer.pitch, renderer.hfov );
					repaint();
				}
				break;
		}
		return true;
	}

	public boolean keyUp(Event event, int i) {
		if (!ready)
			return true;

		switch (i) {
			default :
				break;

			case 1004 :
				keyPanning = false;
				break;

			case 1005 :
				keyPanning = false;
				break;

			case 1006 :
				keyPanning = false;
				break;

			case 1007 :
				keyPanning = false;
				break;

			case 43 : // '+'
			case 46 : // '.'
			case 61 : // '='
			case 62 : // '>'
			case 65 : // 'A'
			case 97 : // 'a'

				keyPanning = false;
				break;

			case 44 : // ','
			case 45 : // '-'
			case 60 : // '<'
			case 90 : // 'Z'
			case 95 : // '_'
			case 122 : // 'z'

				keyPanning = false;
				break;
		}
		return true;
	}


	public boolean mouseMove(Event event, int i, int j) {
		lastMouseX = i;
		lastMouseY = j;
		mouseInViewer = is_inside_viewer(i, j);
		if (mouseInWindow) {
			newx = i;
			newy = j;
		}
		PVSetCursor(i, j);
		
		if( tlbObj != null ) ((toolbar) tlbObj).mouseMove( i, j );
		
		return true;
	}

	void PVSetCursor(int i, int j) {
		if (!mouseInWindow) {
			ResetCursor();
			return;
		}
		int k;
		if (!ready)
			k = -1;
		else
			k = OverStaticHotspot(i, j);
		if (k != curshs) {
			curshs = k;
			if (curshs >= 0) {
				try {
					setCursor(Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ));
//					((Frame) getParent()).setCursor(12);
				} catch (Exception _ex) {
				}
				curhs = -1;
				repaint();
				return;
			}
			ResetCursor();
			repaint();
		}
		if (curshs < 0) {
			if ((panning && !popupPanning) || renderer.lastframe > renderer.frames || !mouseInViewer) {
				curhs = -1;
				ResetCursor();
				return;
			}
			int l;
			if (!hsready)
				l = -1;
			else
				l = OverHotspot(i - vx, j - vy);
			if (l != curhs) {
				curhs = l;
				if (curhs >= 0) {
					try {
						setCursor(Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ));
//						((Frame) getParent()).setCursor(12);
						if (hsready) {
							if( hsShowDescrInStatusBar ) showStatus(hs_name[curhs]);
							if( tlbObj != null ) {
								if( hs_hc[curhs] != null )
									((toolbar) tlbObj).setMessage( hs_name[curhs], hs_hc[curhs] );
								else
									((toolbar) tlbObj).setMessage( hs_name[curhs] );
							} 
							hs_exec_popup(curhs);
							repaint();
							sendHS();
						}
						return;
					} catch (Exception _ex) {
					}
				} else {
					ResetCursor();
					repaint();
					if( hsShowDescrInStatusBar ) showStatus("");
					if( tlbObj != null ) ((toolbar) tlbObj).setMessage( "" );
					sendHS();
					return;
				}
			}
			if (curhs < 0)
				ResetCursor();
		}
	}

	void ResetCursor() {
		try {
			if (mouseInViewer) {
				if (!ready) {
					setCursor(Cursor.getPredefinedCursor( 3 ));
					return;
				}
				if (getCursor().getType() != ptcursor) {
					setCursor(Cursor.getPredefinedCursor( ptcursor ));
					return;
				}
			} else if (getCursor().getType() != 0) {
				setCursor(Cursor.getPredefinedCursor( 0 ));
				return;
			}
		} catch (Exception _ex) {
		}
	}
	
	
//	void ResetCursor() {
//		try {
//			if (mouseInViewer) {
//				if (!ready) {
//					((Frame) getParent()).setCursor(3);
//					return;
//				}
//				if (((Frame) getParent()).getCursorType() != ptcursor) {
//					((Frame) getParent()).setCursor(ptcursor);
//					return;
//				}
//			} else if (((Frame) getParent()).getCursorType() != 0) {
//				((Frame) getParent()).setCursor(0);
//				return;
//			}
//		} catch (Exception _ex) {
//		}
//	}


	void sendView() {
		if (GetView != null && ready && loadPano != null)
			executeJavascriptCommand(
				GetView + "(" + renderer.yaw + "," + renderer.pitch + "," + renderer.hfov + ")");
	}

	void sendHS() {
		if (MouseOverHS != null && ready && loadPano != null)
			executeJavascriptCommand(MouseOverHS + "(" + curhs + ")");
	}

	public void update(Graphics g) {
		paint(g);
	}

public synchronized void paint(Graphics g) {


	
		long t;
		t = System.currentTimeMillis();

		if( onlyPaintToolbar ) {
			if( showToolbar ) ((toolbar) tlbObj).paint( g );
			onlyPaintToolbar = false;
			paintDone = true;
			forceBilIntepolator = false;
			return;
		}
		
		
		
		// avoids unnecessary paints that slow down ROI loading
		if( loadingROI && dynLoadROIs && !panning ) return;


		if (inited) {
			
			
			
			if (fatal) {
				setBackground(Color.red);
				g.clearRect(0, 0, getSize().width, getSize().height);
				return;
			}
			if (offImage == null) {
				awidth = getSize().width;
				aheight = getSize().height;
				if (!vset || offwidth == 0) {
					offwidth = getSize().width;
					offheight = getSize().height;
				}
				offImage = createImage(offwidth, offheight);
				offGraphics = offImage.getGraphics();
				
				// sets the size of the toolbar object
				if( showToolbar ) {
					int w, h;
					if( renderer.vwidth == 0 )
						w = offwidth;
					else
						w = renderer.vwidth;
					if( renderer.vheight == 0 )
						h = offheight;
					else
						h = renderer.vheight;
					((toolbar) tlbObj).setViewerSize( w, h, vx, vy );
				} 
			}
			if (!ready || System.currentTimeMillis() < ptimer) {
				if (dwait != null) {
					if (bgcolor != null && !WaitDisplayed) {
						  setBackground(bgcolor);
//						offGraphics.clearRect(0, 0, offwidth, offheight);
							Color curColor = offGraphics.getColor();
							offGraphics.setColor(bgcolor);
							offGraphics.fillRect(0, 0, offwidth, offheight);
							offGraphics.setColor(curColor);
					}
					if (!WaitDisplayed) {
						if (waittime != 0L)
							ptimer = System.currentTimeMillis() + waittime;
						WaitDisplayed = true;
					}
					offGraphics.drawImage(dwait, offwidth - dwait.getWidth(null) >> 1,offheight - dwait.getHeight(null) >> 1,this);
					if( imgLoadFeedback ) pb_draw(offGraphics, offwidth, offheight);
					if (percent != null && percent[0] > 0) 
						if( showToolbar ) ((toolbar) tlbObj).setBarPerc( percent[0] );
					
					
					
					
					g.drawImage(offImage, 0, 0, this);
					// paints the toolbar
					if( showToolbar ) ((toolbar) tlbObj).paint( g );
					if (ready) {
						try {
							Thread.sleep(20L);
						} catch (InterruptedException _ex) {
							return;
						}
						repaint();
						return;
					}
				} else {
					if (bgcolor != null)
						setBackground(bgcolor);
					g.clearRect(0, 0, getSize().width, getSize().height);
					if (percent != null && percent[0] > 0) {
						if( imgLoadFeedback ) 
						  g.drawString("Loading Image..." + percent[0] + "% complete", 30, getSize().height >> 1);
						if( showToolbar ) ((toolbar) tlbObj).setBarPerc( percent[0] );
						if( showToolbar ) ((toolbar) tlbObj).paint( g );
						return;
					}
					if( imgLoadFeedback ) g.drawString("Loading Image...", 30, getSize().height >> 1);
					if( showToolbar ) ((toolbar) tlbObj).paint( g );
				}
				return;
			}
			if (renderer.vdata == null) {
				renderer.setupView(getSize().width, getSize().height, showToolbar);
				
				//if (view == null)
					//view = createImage(renderer.source);
				
				
			}
			if (panning) {
				double d;
				double scale = (((0.00050000000000000001D * renderer.hfov) / 70D) * 320D) / (double) renderer.vwidth;
				d = (newx - oldx) * mouseSensitivity;
				double speedx = ((double) (0.3 * d * d) * (newx <= oldx ? -1D : 1.0D) + MASS * oldspeedx) / (1.0D + MASS);
				oldspeedx = speedx;
				d = (oldy - newy) * mouseSensitivity;
				double speedy =((double) (0.3 * d * d)* (oldy <= newy ? -1D : 1.0D)+ MASS * oldspeedy)/ (1.0D + MASS);
				oldspeedy = speedy;
				double deltaYaw = scale * speedx;
				double deltaPitch = scale * speedy;
				if( mousePanTime > 0 && lastPanningPaintTime > 0 ) {
					double deltaAngle = Math.sqrt( deltaYaw*deltaYaw + deltaPitch*deltaPitch );
					// computes the time needed for a full revolution with this deltaAngle
					double t360 = 360.0/deltaAngle*lastPanningPaintTime/1000.0;
					if( t360 < mousePanTime ) {
						// the user is panning too fast, we need to reduce the angle
						deltaYaw = deltaYaw*t360/mousePanTime;
						deltaPitch = deltaPitch*t360/mousePanTime;
					}
				}
				gotoView(renderer.yaw + deltaYaw, renderer.pitch + deltaPitch, renderer.hfov * zoom);
//				gotoView(yaw + scale * speedx * mouseSensitivity, pitch + scale * speedy * mouseSensitivity, hfov * zoom);
			}
			if (renderer.lastframe > renderer.frames)
				gotoView(renderer.yaw + renderer.autopan, renderer.pitch + autotilt, renderer.hfov * zoom);
			if (hsready && hs_drawWarpedImages(renderer.imagePixels, curhs, showhs))
				renderer.dirty = true;
			if (renderer.dirty) {
				for (int i = 0; i < renderer.vdata.length; i++)
					renderer.vdata[i] = 0;

				if (app_properties.size() == 6&& filename != null&& filename.toLowerCase().endsWith(".mov")) {
					int ai[] = get_cube_order((int) renderer.yaw, (int) renderer.pitch);
					for (int l = 0; l < 6; l++) {
						Applet applet2;
						if ((applet2 =(Applet) applets.get(app_properties.elementAt(ai[l])))!= null&& sender != null&& sender.get(applet2) != null) {
							String s1 = applet2.getAppletInfo();
							if (renderer.dirty && s1 != null && s1.equals("topFrame"))
								applet2.paint(null);
						}
					}

				} else {
					for (int j = 0; j < app_properties.size(); j++) {
						Applet applet1;
						if ((applet1 =
							(Applet) applets.get(app_properties.elementAt(j)))
							!= null
							&& sender != null
							&& sender.get(applet1) != null) {
							String s = applet1.getAppletInfo();
							if (renderer.dirty && s != null && s.equals("topFrame"))
								applet1.paint(null);
						}
					}

				}
				
				
				int[] viewData = null;
				
				if (renderer.dirty && show_pdata ) {
					int ai1[][] = renderer.imagePixels;
					if (renderer.antialias && renderer.scaledPanos != null) {
						double d3 =
								renderer.hfov / ((double) renderer.vwidth * 360D * renderer.max_oversampling);
						int j1 = 0;
						for (int k1 = renderer.imagePixels[0].length;
							(double) k1 * d3 > 1.0D;
							k1 >>= 1)
							j1++;

						if (renderer.scaledPanos.elementAt(j1) != null) {
							ai1 = (int[][]) renderer.scaledPanos.elementAt(j1);
							renderer.math_updateLookUp(ai1[0].length);
						}
					}
					
					// these variables are only used if ptviewer is set to use Lanczos2
					// they are used to force using a faster interpolator while dynLoading ROIs
					boolean useBilinear = forceBilIntepolator;
					boolean useLanczos2 = !forceBilIntepolator;
					forceBilIntepolator = false;
					
					
					
					
					switch (quality) {
						default :
							break;

						case 0 : // '\0'
							viewData = renderer.math_extractview(ai1, renderer.vdata, renderer.hs_vdata, renderer.vwidth, renderer.hfov, renderer.yaw, renderer.pitch, false, false);
							renderer.dirty = false;
							break;

						case 1 : // '\001'
							if (panning || renderer.lastframe > renderer.frames) {
								viewData = renderer.math_extractview(ai1,renderer.vdata,renderer.hs_vdata,renderer.vwidth,renderer.hfov,renderer.yaw,renderer.pitch,false,false);
							} else {
								viewData = renderer.math_extractview(ai1,renderer.vdata,renderer.hs_vdata,renderer.vwidth,renderer.hfov,renderer.yaw,renderer.pitch,true,false);
								System.gc();
								renderer.dirty = false;
							}
							break;

						case 2 : // '\002'
							if (panning) {
								viewData = renderer.math_extractview(ai1,renderer.vdata,renderer.hs_vdata,renderer.vwidth,renderer.hfov,renderer.yaw,renderer.pitch,false,false);
							} else {
								viewData = renderer.math_extractview(ai1,renderer.vdata,renderer.hs_vdata,renderer.vwidth,renderer.hfov,renderer.yaw,renderer.pitch,true,false);
								System.gc();
								renderer.dirty = false;
							}
							break;

						case 3 : // '\003'
							viewData = renderer.math_extractview(ai1,renderer.vdata,renderer.hs_vdata,renderer.vwidth,renderer.hfov,renderer.yaw,renderer.pitch,true,false);
							renderer.dirty = false;
							break;
						// FS+
						case 4 : // nn for panning & autopanning, lanczos2 else
							if (panning || renderer.lastframe > renderer.frames || keyPanning) {
								viewData = renderer.math_extractview(ai1,renderer.vdata,renderer.hs_vdata,renderer.vwidth,renderer.hfov,renderer.yaw,renderer.pitch,false,false);
							} else {
								viewData = renderer.math_extractview(ai1,renderer.vdata,renderer.hs_vdata,renderer.vwidth,renderer.hfov,renderer.yaw,renderer.pitch,useBilinear,useLanczos2);
								System.gc();
								renderer.dirty = false;
							}
							break;
						case 5 : // bilinear for panning & autopanning, lanczos2 else
							if (panning || renderer.lastframe > renderer.frames || keyPanning) {
								viewData = renderer.math_extractview(ai1,renderer.vdata, renderer.hs_vdata, renderer.vwidth, renderer.hfov, renderer.yaw, renderer.pitch, true, false);
							} else {
								viewData = renderer.math_extractview(ai1, renderer.vdata, renderer.hs_vdata, renderer.vwidth, renderer.hfov, renderer.yaw, renderer.pitch, useBilinear, useLanczos2);
								System.gc();
								renderer.dirty = false;
							}
							break;
						case 6 : // nn for fast panning & autopanning, bilinear for slow panning & autopanning, lanczos2 else
							if (panning || renderer.lastframe > renderer.frames || keyPanning) {
								// decides if panning is fast or slow
								int FEW_PIXELS = 70;
								boolean fastPanning = false;
								if( panning ) {
									// only if panning with the mouse
									int deltaX = newx - oldx;
									int deltaY = newy - oldy;
									deltaX *= mouseSensitivity/mouseQ6Threshold;
									deltaY *= mouseSensitivity/mouseQ6Threshold;
									if( Math.abs(deltaX)*renderer.vwidth/1024 > FEW_PIXELS ) fastPanning = true;
									if( Math.abs(deltaY)*renderer.vheight/768 > FEW_PIXELS ) fastPanning = true;
								}
								
								System.out.println("panning");
								
								viewData = renderer.math_extractview(ai1, renderer.vdata, renderer.hs_vdata, renderer.vwidth, renderer.hfov, renderer.yaw, renderer.pitch, !fastPanning, false);
							} else {
								System.out.println("not panning");
								viewData = renderer.math_extractview(ai1, renderer.vdata, renderer.hs_vdata, renderer.vwidth, renderer.hfov, renderer.yaw, renderer.pitch, useBilinear, useLanczos2);
							
								System.gc();
								renderer.dirty = false;
							}
							break;
						// FS-
					}
				}
				renderer.hs_setCoordinates(renderer.vwidth,renderer.vheight,renderer.pwidth,renderer.pheight,renderer.yaw,renderer.pitch,renderer.hfov);
				sendView();
				renderer.frames++;
				//renderer.source.newPixels();
				
				
				view = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(renderer.vwidth, renderer.vheight, viewData, 0, renderer.vwidth));
				
			}
			if (panning || renderer.lastframe > renderer.frames)
				PVSetCursor(newx, newy);

			if( useVolatileImage ) {
				vImgObj.setSize(offwidth, offheight);
				vImgObj.drawAcceleratedFrame( g );
			}
			else {
				drawFrame( g );
			}
		}

		// notify the toolbar that paint() has finished
		if( tlbObj != null ) ((toolbar) tlbObj).notifyEndPaint();
		
		paintDone = true;	// sets a global flag for synchronization

		t = System.currentTimeMillis() - t;
		if( panning ) lastPanningPaintTime = t;
		
		if( renderer.lastframe > renderer.frames && autopanFrameTime > 0 ) {
			if( t < autopanFrameTime ) {
				// sleeps some time to avoid too fast autopanning
				try {
					Thread.sleep( Math.round( autopanFrameTime - t ));
				} catch (InterruptedException _ex) {
				}
			}
		}
		
//		System.out.println("Time in paint(): " + t + " ms");

	}

	// creates the backbuffer used to draw frames
	void createBackBuffer() {
		if( backBuffer == null ) {
				backBuffer = createImage(offwidth, offheight);
		}
	}

	// draws a panorama frame
	void drawFrame(Graphics g) {
		createBackBuffer();
		Graphics gBB = backBuffer.getGraphics();
		renderFrame(gBB);
		g.drawImage(backBuffer, 0, 0, this);
	}

	// renders a panorama frame
	void renderFrame(Graphics gBB) {
		
		System.out.println("render!");
		
		gBB.drawImage(view, vx, vy, this);
		
		
		
		if (hsready)
			hs_draw(gBB, vx, vy, renderer.vwidth, renderer.vheight, curhs, showhs);
		
		
		
		if (frame != null)
		gBB.drawImage(frame,offwidth - frame.getWidth(null),offheight - frame.getHeight(null), this);

		// paints the toolbar
		if( showToolbar ) ((toolbar) tlbObj).paint( gBB );

		if (ready)
			shs_draw(gBB);
		Applet applet;
		for (Enumeration enumeration = sender.elements();
			enumeration.hasMoreElements();
			)
			try {
				if ((applet = (Applet) enumeration.nextElement()).getAppletInfo() != "topFrame")
					applet.paint(gBB);
			} catch (Exception _ex) {
			}
			
	}


	// tests the java version to see if we can use the VolatileImage class
	boolean canUseAcceleratedGraphic() {
		boolean retVal;
		
		try {
			String s =  System.getProperty( "java.version" );
			retVal = (s.substring( 0, 3 ).compareTo("1.4") >= 0);
		}
		catch( Exception ex ) {
			retVal = false;		
		}
		return retVal;
	}


	// dynamically loads ROIs: loads first the ROIs that are nearest to the direction of view
	void loadROI_dyn(){
		boolean done;
		int nLoaded = 0;
		
		computeRoiYaw();
		computeRoiPitch();
		
		do {
			done = true;
			int iVisible = -1;		// index of the nearest visible roi
			int iNotVisible = -1;	// index of the nearest not visible roi
			double minDistVisible = 10000, minDistNotVisible = 10000;
			// looks for the ROI that is nearest to the direction of view
			for( int k = 0; k < numroi; k++ ){
				if( !roi_loaded[k] ) {
					done = false;
					double distX = Math.abs( renderer.yaw - roi_yaw[k] );
					if( distX > 180 ) distX = 360 - distX;
					double distY = Math.abs( renderer.pitch - roi_pitch[k] );
					double dist = Math.sqrt( distX*distX + distY*distY );
					// computes the nearest visible and not visible tile in order to load first all visible tiles
					if( isROIVisible(k) ) {
						if( dist < minDistVisible ) {
							minDistVisible = dist;
							iVisible = k;
						}
					}
					else {
						if( dist < minDistNotVisible ) {
							minDistNotVisible = dist;
							iNotVisible = k;
						}
					}
				}
			}
			// if there is a visible roi chooses it, else chooses an invisible one
			int i;
			if( iVisible >= 0 ) {
				i = iVisible;
			}
			else {
				i = iNotVisible;
			}

			if( i >= 0 ) {
				loadROI(i);
				nLoaded++;
				// updates the progress bar
				if( showToolbar ) ((toolbar) tlbObj).setBarPerc( nLoaded*100/numroi );
				
//				// repaints only if the loaded ROI is visible
				if( !isROIVisible(i) ) onlyPaintToolbar = true; 

				paintDone = false;
				forceBilIntepolator = true;		// to speed up ROI drawing
				repaint();		// we always call repaint() to draw the toolbar

				// stops execution until paint() is executed
				int counter = 0;	// emergency exit
				while( !paintDone && counter < 100 ) {
					try {
						Thread.sleep(10L);
					} catch (Exception _ex) {}
					counter++;
				}
//				System.out.println( "Loaded ROI " + i + " pan=" + roi_pan[i] + " pano_pan=" + yaw);
			}
		} while( !done );

		// clears the progress bar
		if( showToolbar ) ((toolbar) tlbObj).setBarPerc( 0 );
		renderer.dirty = true;
		repaint();

	}
	

	/*
	 * returns true if the roi is currently visible in ptviewer
	 */
	protected boolean isROIVisible( int nRoi ) {
		boolean visible = true;
		// distance in degrees between the center of the current view and the center of the loaded ROI
		double yawDist = Math.abs(renderer.yaw - roi_yaw[nRoi]);
		if( yawDist > 180 ) yawDist = 360 - yawDist;
		double pitchDist = Math.abs( renderer.pitch - roi_pitch[nRoi] );
		if( yawDist > (renderer.hfov + roi_wdeg[nRoi])/2 ) visible = false;
		if( visible ) {
			if( pitchDist > (Utils.math_fovy( renderer.hfov, renderer.vwidth, renderer.vheight) + roi_hdeg[nRoi])/2)
				visible = false;
		}
		return visible;
	}

	

	public void loadROI(int i, int j) {
		for (int k = i; k <= j; k++)
			loadROI(k);

	}

	//		public void loadROI(int i)
	//		{
	//				Image image;
	//				Image image1;
	//				if(i >= numroi || roi_loaded[i])
	//						break MISSING_BLOCK_LABEL_180;
	//				image1 = loadImage(roi_im[i]);
	//				image1;
	//				image = image1;
	//				JVM INSTR ifnull 180;
	//					 goto _L1 _L2
	//_L1:
	//				break MISSING_BLOCK_LABEL_32;
	//_L2:
	//				break MISSING_BLOCK_LABEL_180;
	//				ptinsertImage(pdata, roi_xp[i], roi_yp[i], image, (pheight + 99) / 100);
	//				if(hsready)
	//				{
	//						for(int j = 0; j < numhs; j++)
	//								if((hs_imode[j] & 4) > 0)
	//								{
	//										int k = (int)hs_up[j];
	//										int l = (int)hs_vp[j];
	//										int i1 = (int)hs_xp[j] - (k >> 1);
	//										int j1 = (int)hs_yp[j] - (l >> 1);
	//										im_extractRect(pdata, i1, j1, (int[])hs_him[j], k, 0, l, k, l);
	//								}
	//
	//				}
	//				roi_loaded[i] = true;
	//		}

	// copied from version 2.1
	public void loadROI(int i) {
		if (i < numroi && !roi_loaded[i]) {

			loadingROI = true;
		
			Image r = null;
			r = loadImage(roi_im[i]);
			if (r != null) {
				ptinsertImage(
						renderer.imagePixels,
					roi_xp[i],
					roi_yp[i],
					r,
					(renderer.pheight + 99) / 100);

				// Update warped hotspots
				if (hsready) {
					int k;
					for (k = 0; k < renderer.numhs; k++) {
						if ((renderer.hs_imode[k] & IMODE_WARP) > 0) { // warped hotspot
							int w = (int) renderer.hs_up[k];
							int h = (int) renderer.hs_vp[k];
							int xp = (int) renderer.hs_xp[k] - w / 2;
							int yp = (int) renderer.hs_yp[k] - h / 2;
							im_extractRect(
									renderer.imagePixels,
								xp,
								yp,
								(int[]) renderer.hs_him[k],
								w,
								0,
								h,
								w,
								h);
						}
					}
				}
				roi_loaded[i] = true;
				roi_w[i] = r.getWidth( null );
				roi_wdeg[i] = 1.0*roi_w[i]*360/renderer.pwidth;
				roi_hdeg[i] = 1.0*roi_h[i]*360/renderer.pwidth;
				r = null;
			}

			loadingROI = false;
		
			// sleeps some time in order to let ptviewer perform a paint()
//			try {
//				Thread.sleep(100L);
//			} catch (Exception _ex) {}
		}
	}

	// computes the yaw angle of the middle of each ROI image
	void computeRoiYaw() {
		for( int k = 0; k < numroi; k++ ) {
			roi_yaw[k] = 360.0*(roi_xp[k] + roi_w[k]/2)/renderer.pwidth;
			if( roi_yaw[k] > 360 ) roi_yaw[k] -= 360;
			roi_yaw[k] -= 180;
		}
	}
	
	
	// computes the pitch angle of the middle of each ROI image
	void computeRoiPitch() {
		for( int k = 0; k < numroi; k++ ) {
			// y coord of the middle of the image if the pano had a 2:1 size ratio
			int y = renderer.pwidth/4 - renderer.pheight/2 + roi_yp[k] + roi_h[k]/2;
			double t = 90.0 - 180.0*y/(renderer.pwidth/2);
			roi_pitch[k] = t;
		}
	}
	
	

	

	int OverHotspot(int i, int j) {
		if (!hsready || i < 0 || i >= renderer.vwidth || j < 0 || j >= renderer.vheight)
			return -1;
		
		if( hsEnableVisibleOnly && !showhs )
			return -1;
		
		int k = renderer.hs_vdata[j * renderer.vwidth + i] & 0xff;
		if (filename != null && filename.toLowerCase().endsWith(".mov"))
			if (k == 0)
				return -1;
			else
				return k - 1;
		if (k != 255 && k < renderer.numhs)
			return k;
		if (hs_image != null)
			return -1;
		for (int l = 0; l < renderer.numhs; l++)
			if (renderer.hs_visible[l]
				&& hs_mask[l] == null
				&& hs_link[l] == -1
				&& renderer.hs_up[l] == -200D
				&& renderer.hs_vp[l] == -200D
				&& i < renderer.hs_xv[l] + 12
				&& i > renderer.hs_xv[l] - 12
				&& j < renderer.hs_yv[l] + 12
				&& j > renderer.hs_yv[l] - 12)
				return l;

		return -1;
	}

	public void waitWhilePanning() {
		while (renderer.lastframe > renderer.frames)
			try {
				Thread.sleep(200L);
			} catch (Exception _ex) {
				return;
			}
	}

	public void ZoomIn() {
		gotoView(renderer.yaw, renderer.pitch, renderer.hfov / 1.03D);
	}

	public void ZoomOut() {
		gotoView(renderer.yaw, renderer.pitch, renderer.hfov * 1.03D);
	}

	public void panUp() {
		gotoView(renderer.yaw, renderer.pitch + renderer.hfov / pan_steps, renderer.hfov);
	}

	public void panDown() {
		gotoView(renderer.yaw, renderer.pitch - renderer.hfov / pan_steps, renderer.hfov);
	}

	public void panLeft() {
		gotoView(renderer.yaw - renderer.hfov / pan_steps, renderer.pitch, renderer.hfov);
	}

	public void panRight() {
		gotoView(renderer.yaw + renderer.hfov / pan_steps, renderer.pitch, renderer.hfov);
	}

	public void showHS() {
		showhs = true;
		if( showToolbar ) ((toolbar) tlbObj).syncHSButton();
		repaint();
	}

	public void hideHS() {
		showhs = false;
		if( showToolbar ) ((toolbar) tlbObj).syncHSButton();
		repaint();
	}

	public void toggleHS() {
		showhs = !showhs;
		
		if( hsEnableVisibleOnly ) {
			// we need to change the mouse cursor if we are over an hotspot
			int i = lastMouseX;
			int j = lastMouseY;
			mouseInViewer = is_inside_viewer(i, j);
			if (mouseInWindow) {
				newx = i;
				newy = j;
			}
			PVSetCursor(i, j);
		}
		
		if( showToolbar ) ((toolbar) tlbObj).toggleHSButton();
		repaint();
	}

	public boolean isVisibleHS() {
		return showhs;
	}

	public double pan() {
		return renderer.yaw;
	}

	public double tilt() {
		return renderer.pitch;
	}

	public double fov() {
		return renderer.hfov;
	}

	public void setQuality(int i) {
		if (i >= 0 && i <= renderer.MAX_QUALITY) {
			quality = i;
			renderer.dirty = true;
			repaint();
		}
	}
	
	public int getQuality() {
		return quality;
	}
        
 	/**
 	* Moves from a specific position to another position using a specified amount of frames
    * @param p0 Pan angle of starting view
    * @param p1 Pan angle of target view
    * @param t0 Tilt angle of starting view
    * @param t1 Tilt angle of target view
    * @param f0 Field of View angle of starting view
    * @param f1 Field of View of target view
    * @param nframes the number of frames
    */
	public void moveFromTo(
		double p0,
		double p1,
		double t0,
		double t1,
		double f0,
		double f1,
		int nframes) {
			
	//	// to avoid weird behaviour when this function is called while loading ROIs
	//	if( loadingROI && dynLoadROIs ) return; 
			
	//	double d6 = 0.0D;
	//	double d7 = (d3 - d2) / (double) i;
	//	double d8 = Math.pow(d5 / d4, 1.0D / (double) i);
	//	if (Math.abs(d1 - d) < 180D || yaw_max != 180D || yaw_min != -180D)
	//		d6 = (d1 - d) / (double) i;
	//	else if (d1 > d)
	//		d6 = (d1 - d - 360D) / (double) i;
	//	else if (d1 < d)
	//		d6 = ((d1 - d) + 360D) / (double) i;
	//	gotoView(d, d2, d4);
	//	lastframe = frames + (long) i;
	//	startAutoPan(d6, d7, d8);
		moveFromTo(p0, p1, t0, t1, f0, f1, nframes, 0);
	}

	public void moveFromTo(
		double p0,
		double p1,
		double t0,
		double t1,
		double f0,
		double f1,
		int nframes,
		double autoTime) {
			
		// to avoid weird behaviour when this function is called while loading ROIs
		if( loadingROI && dynLoadROIs ) return; 
			
		double dp = 0.0D;
		double dt = (t1 - t0) / (double) nframes;
		double z = Math.pow(f1 / f0, 1.0D / (double) nframes);
		if (Math.abs(p1 - p0) < 180D || renderer.yaw_max != 180D || renderer.yaw_min != -180D)
			dp = (p1 - p0) / (double) nframes;
		else if (p1 > p0)
			dp = (p1 - p0 - 360D) / (double) nframes;
		else if (p1 < p0)
			dp = ((p1 - p0) + 360D) / (double) nframes;
		gotoView(p0, t0, f0);
		renderer.lastframe = renderer.frames + (long) nframes;
		startAutoPan(dp, dt, z, autoTime);
	}

        
	public void moveTo(double pan, double tilt, double fov, int nframes) {
		moveFromTo(renderer.yaw, pan, renderer.pitch, tilt, renderer.hfov, fov, nframes, 0);
	}

	public void moveTo(double pan, double tilt, double fov, int nframes, double autoTime) {
		moveFromTo(renderer.yaw, pan, renderer.pitch, tilt, renderer.hfov, fov, nframes, autoTime);
	}

	/**
 	* Starts autopanning.
    * @param p Pan angle increment per frame
    * @param t Tilt angle increment per frame
    * @param z Field of View angle factor per frame
    */
	public void startAutoPan(double p, double t, double z) {
//		autopan = d;
//		autotilt = d1;
//		zoom = d2;
//		if (lastframe <= frames)
//			lastframe = frames + 0x5f5e100L;
//		repaint();
		startAutoPan( p, t, z, 0 );
	}

	public void startAutoPan(double p, double t, double z, double autoTime) {
		renderer.autopan = p;
		autotilt = t;
		zoom = z;
		if( autoTime != 0 )
			autopanFrameTime = ComputeAutoTimeFrame( renderer.autopan, autoTime );
		if (renderer.lastframe <= renderer.frames)
			renderer.lastframe = renderer.frames + 0x5f5e100L;
		repaint();
	}

	public void stopAutoPan() {
		renderer.lastframe = 0L;
		renderer.autopan = 0.0D;
		autopanFrameTime = 0;
		autotilt = 0.0D;
		zoom = 1.0D;
	}

	void stopPan() {
		panning = false;
		oldspeedx = 0.0D;
		oldspeedy = 0.0D;
	}

	public boolean getAutoPan() {
		return renderer.lastframe > renderer.frames;
	}

	public String getFilename() {
		return filename;
	}

	public boolean getPanoIsLoaded() {
		return PanoIsLoaded;
	}

	public void gotoView(double pan, double tilt, double fov) {

		int flag = renderer.setCameraPos(pan, tilt, fov);
		
		
		if(flag == 1){
			repaint();
		} else if(flag == 2){
			stopAutoPan();
		}
	}

	public void gotoHS(int i) {
		if (i < 0 || i >= renderer.numhs) {
			return;
		} else {
			JumpToLink(hs_url[i], hs_target[i]);
			return;
		}
	}

	void gotoSHS(int i) {
		if (i < 0 || i >= numshs) {
			return;
		} else {
			JumpToLink(shs_url[i], shs_target[i]);
			return;
		}
	}

	void JumpToLink(String s, String s1) {
		if (s != null) {
			if (s.startsWith("ptviewer:")) {
				executePTViewerCommand(s.substring(s.indexOf(':') + 1));
				return;
			}
			if (s.startsWith("javascript:")) {
				executeJavascriptCommand(s.substring(s.indexOf(':') + 1));
				return;
			}
			URL url;
			try {
				url = new URL(getDocumentBase(), s);
			} catch (MalformedURLException _ex) {
				System.err.println("URL " + s + " ill-formed");
				return;
			}
			if (s1 == null) {
				getAppletContext().showDocument(url);
				return;
			}
			getAppletContext().showDocument(url, s1);
		}
	}

	public synchronized void newPanoFromList(
		int i,
		double d,
		double d1,
		double d2) {
		loadPanoFromList(i);
		renderer.yaw = d;
		renderer.pitch = d1;
		renderer.hfov = d2;
		repaint();
		start();
	}

	public synchronized void newPanoFromList(int i) {
		loadPanoFromList(i);
		repaint();
		start();
	}

	void loadPanoFromList(int i) {
		String s;
		if ((s = myGetParameter(null, "pano" + i)) != null) {
			stop();
			PV_reset();
			initialize();
			CurrentPano = i;
			if (PTViewer_Properties != null)
				ReadParameters(PTViewer_Properties);
			ReadParameters(s);
		}
	}

	public void newPano(String s) {
		stop();
		PV_reset();
		initialize();
		if (PTViewer_Properties != null)
			ReadParameters(PTViewer_Properties);
		ReadParameters(s);
//		if( dynLoadROIs ) computeRoiPan();
		repaint();
		start();
	}

	public void SetURL(String s) {
		newPano("{file=" + s + "}");
	}
	
	/*
	 * computes the value for	autopanFrameTime from the auto and autoTime parameters
	 */
	private double ComputeAutoTimeFrame( double auto, double autoTime ) {
		
		if( auto == 0 ) return 0;
		
		double nFrames = 360.0 / auto;
		double retVal = autoTime/nFrames*1000;
		return Math.abs(retVal);
	}

	void ReadParameters(String s) {
		String s1;
		if ((s1 = myGetParameter(s, "bgcolor")) != null)
			bgcolor = new Color(Integer.parseInt(s1, 16));
		if ((s1 = myGetParameter(s, "barcolor")) != null)
			pb_color = new Color(Integer.parseInt(s1, 16));
		if ((s1 = myGetParameter(s, "bar_x")) != null)
			pb_x = Integer.parseInt(s1);
		if ((s1 = myGetParameter(s, "bar_y")) != null)
			pb_y = Integer.parseInt(s1);
		if ((s1 = myGetParameter(s, "bar_width")) != null)
			pb_width = Integer.parseInt(s1);
		if ((s1 = myGetParameter(s, "bar_height")) != null)
			pb_height = Integer.parseInt(s1);
		if ((s1 = myGetParameter(s, "maxarray")) != null)
			im_maxarray = Integer.parseInt(s1);
		if ((s1 = myGetParameter(s, "view_width")) != null) {
			renderer.vwidth = Integer.parseInt(s1);
			vset = true;
		}
		if ((s1 = myGetParameter(s, "view_height")) != null) {
			renderer.vheight = Integer.parseInt(s1);
			org_vheight = renderer.vheight;	// needed because if we use the toolbar we will change vheight each time that a pano is loaded
			vset = true;
		}
		else {
			if( org_vheight != 0 )
				renderer.vheight = org_vheight;
		}
		if ((s1 = myGetParameter(s, "view_x")) != null)
			vx = Integer.parseInt(s1);
		if ((s1 = myGetParameter(s, "view_y")) != null)
			vy = Integer.parseInt(s1);
		if ((s1 = myGetParameter(s, "preload")) != null)
			preload = s1;
		if ((s1 = myGetParameter(s, "cache")) != null
			&& s1.equalsIgnoreCase("false"))
			file_cachefiles = false;
		if ((s1 = myGetParameter(s, "cursor")) != null)
			if (s1.equalsIgnoreCase("CROSSHAIR"))
				ptcursor = 1;
			else if (s1.equalsIgnoreCase("MOVE"))
				ptcursor = 13;
		if ((s1 = myGetParameter(s, "grid_bgcolor")) != null)
			grid_bgcolor = Integer.parseInt(s1, 16);
		if ((s1 = myGetParameter(s, "grid_fgcolor")) != null)
			grid_fgcolor = Integer.parseInt(s1, 16);
		if ((s1 = myGetParameter(s, "mass")) != null)
			MASS = Double.valueOf(s1).doubleValue();
		if (myGetParameter(s, "antialias") != null)
			renderer.antialias = true;
		if ((s1 = myGetParameter(s, "quality")) != null) {
			quality = Integer.parseInt(s1);
			if (quality < 0)
				quality = 0;
			if (quality > renderer.MAX_QUALITY)
				quality = renderer.MAX_QUALITY;
		}
		if ((s1 = myGetParameter(s, "inits")) != null)
			inits = s1;
		double d;
//		if ((s1 = myGetParameter(s, "tiltmin")) != null
//			&& (d = Double.valueOf(s1).doubleValue()) > -90D
//			&& d < 0.0D)
//			pitch_min = d;
		// modified to allow positive values for tiltmin
		if ((s1 = myGetParameter(s, "tiltmin")) != null
			&& (d = Double.valueOf(s1).doubleValue()) > -90D )
			renderer.pitch_min = d;
//		if ((s1 = myGetParameter(s, "tiltmax")) != null
//			&& (d = Double.valueOf(s1).doubleValue()) < 90D
//			&& d > 0.0D)
//			pitch_max = d;
		// modified to allow negative values for tiltmax (hard to believe, but somebody needed it!)
		if ((s1 = myGetParameter(s, "tiltmax")) != null
			&& (d = Double.valueOf(s1).doubleValue()) < 90D )
			renderer.pitch_max = d;
		if ((s1 = myGetParameter(s, "tilt")) != null
			&& (d = Double.valueOf(s1).doubleValue()) >= renderer.pitch_min
			&& d <= renderer.pitch_max)
			renderer.pitch = d;
		if ((s1 = myGetParameter(s, "tilt")) != null
			&& (d = Double.valueOf(s1).doubleValue()) >= renderer.pitch_min
			&& d <= renderer.pitch_max)
			renderer.pitch = d;
		if ((s1 = myGetParameter(s, "panmax")) != null)
			renderer.yaw_max = Double.valueOf(s1).doubleValue();
		if ((s1 = myGetParameter(s, "panmin")) != null)
			renderer.yaw_min = Double.valueOf(s1).doubleValue();
		if ((s1 = myGetParameter(s, "pan")) != null
			&& (d = Double.valueOf(s1).doubleValue()) >= renderer.yaw_min
			&& d <= renderer.yaw_max)
			renderer.yaw = d;
		if ((s1 = myGetParameter(s, "fovmax")) != null
			&& (d = Double.valueOf(s1).doubleValue()) <= 165D)
			renderer.hfov_max = d <= renderer.yaw_max - renderer.yaw_min ? d : renderer.yaw_max - renderer.yaw_min;
		if ((s1 = myGetParameter(s, "fovmin")) != null)
			renderer.hfov_min = Double.valueOf(s1).doubleValue();
		if ((s1 = myGetParameter(s, "fov")) != null
			&& (d = Double.valueOf(s1).doubleValue()) <= renderer.hfov_max
			&& d >= renderer.hfov_min)
			renderer.hfov = d;

		// must be before the "wait" parameter: the update() function must find an existing toolbar
		if ((s1 = myGetParameter(s, "showToolbar")) != null
			&& s1.equalsIgnoreCase("true"))
			showToolbar = true;
		if ((s1 = myGetParameter(s, "toolbarImage")) != null) {
			tlbImageName = s1;
		}
		if( showToolbar && tlbObj == null ) tlbObj = new toolbar( this, tlbImageName );

		if( showToolbar ) {
			if ((s1 = myGetParameter(s, "toolbarDescr_x")) != null)
				((toolbar)tlbObj).setToolbarDescrX( Integer.parseInt(s1) );
			if ((s1 = myGetParameter(s, "toolbarDescr_color")) != null)
				((toolbar)tlbObj).SetTextColor( s1 );
			if ((s1 = myGetParameter(s, "toolbarBoldText")) != null
					&& s1.equalsIgnoreCase("true"))
				((toolbar)tlbObj).setMsgBold( true );
		}
		
		if ((s1 = myGetParameter(s, "mouseSensitivity")) != null)
			mouseSensitivity = Double.valueOf(s1).doubleValue();

		if ((s1 = myGetParameter(s, "mouseQ6Threshold")) != null)
			mouseQ6Threshold = Double.valueOf(s1).doubleValue();

		if ((s1 = myGetParameter(s, "mousePanTime")) != null)
			mousePanTime = Double.valueOf(s1).doubleValue();

		if ((s1 = myGetParameter(s, "wait")) != null) {
			dwait = null;
			dwait = loadImage(s1);
			update(getGraphics());
		}
		if ((s1 = myGetParameter(s, "auto")) != null)
			renderer.autopan = Double.valueOf(s1).doubleValue();
			
		if( renderer.autopan != 0 ) {
			if ((s1 = myGetParameter(s, "autoTime")) != null) {
				double autoTime = Double.valueOf(s1).doubleValue();
				autopanFrameTime = ComputeAutoTimeFrame( renderer.autopan, autoTime );
			}
			if ((s1 = myGetParameter(s, "autoNumTurns")) != null) {
				autoNumTurns = Double.valueOf(s1).doubleValue();
			}
		}
			
		if ((s1 = myGetParameter(s, "mousehs")) != null)
			MouseOverHS = s1;
		if ((s1 = myGetParameter(s, "getview")) != null)
			GetView = s1;
		if ((s1 = myGetParameter(s, "frame")) != null) {
			frame = null;
			frame = loadImage(s1);
		}
		if ((s1 = myGetParameter(s, "waittime")) != null)
			waittime = Integer.parseInt(s1);
		if ((s1 = myGetParameter(s, "hsimage")) != null)
			hs_image = s1;
		if ((s1 = myGetParameter(s, "pwidth")) != null)
			renderer.pwidth = Integer.parseInt(s1);
		if ((s1 = myGetParameter(s, "pheight")) != null)
			renderer.pheight = Integer.parseInt(s1);
		if ((s1 = myGetParameter(s, "loadAllRoi")) != null
			&& s1.equalsIgnoreCase("false"))
			loadAllRoi = false;
		if ((s1 = myGetParameter(s, "file")) != null)
			filename = s1;
		if ((s1 = myGetParameter(s, "order")) != null)
			order = s1;
		if ((s1 = myGetParameter(s, "oversampling")) != null)
			renderer.max_oversampling = Double.valueOf(s1).doubleValue();
		for (int i = 0; i <= hotspots.size(); i++) {
			String s2;
			if ((s2 = myGetParameter(s, "hotspot" + i)) != null) {
				if (i < hotspots.size())
					hotspots.setSize(i);
				hotspots.addElement(s2);
			}
		}

		numroi = 0;
		int j1;
		for (j1 = 0; myGetParameter(s, "roi" + j1) != null; j1++);
		if (j1 > 0) {
			roi_allocate(j1);
			for (int j = 0; j < numroi; j++) {
				String s3;
				if ((s3 = myGetParameter(s, "roi" + j)) != null)
					ParseROILine(s3, j);
			}

		}
		for (int k = 0; k <= shotspots.size(); k++) {
			String s4;
			if ((s4 = myGetParameter(s, "shotspot" + k)) != null) {
				if (k < shotspots.size())
					shotspots.setSize(k);
				shotspots.addElement(s4);
			}
		}

		for (int l = 0; l <= sounds.size(); l++) {
			String s5;
			if ((s5 = myGetParameter(s, "sound" + l)) != null) {
				if (l < sounds.size())
					sounds.setSize(l);
				sounds.addElement(s5);
			}
		}

		for (int i1 = 0; i1 <= app_properties.size(); i1++) {
			String s6;
			if ((s6 = myGetParameter(s, "applet" + i1)) != null) {
				if (i1 < app_properties.size()) {
					stopApplets(i1);
					app_properties.setSize(i1);
				}
				app_properties.addElement(s6);
			}
		}

		if ((s1 = myGetParameter(s, "dynLoadROIs")) != null
			&& s1.equalsIgnoreCase("true"))
			dynLoadROIs = true;

		if ((s1 = myGetParameter(s, "hsShowDescrInStatusBar")) != null
				&& s1.equalsIgnoreCase("false"))
				hsShowDescrInStatusBar = false;

		if ((s1 = myGetParameter(s, "hsEnableVisibleOnly")) != null
			&& s1.equalsIgnoreCase("true"))
			hsEnableVisibleOnly = true;

		if ((s1 = myGetParameter(s, "shsEnableVisibleOnly")) != null
				&& s1.equalsIgnoreCase("true"))
				shsEnableVisibleOnly = true;
				
		if ((s1 = myGetParameter(s, "shsStopAutoPanOnClick")) != null
				&& s1.equalsIgnoreCase("false"))
				shsStopAutoPanOnClick = false;

		if ((s1 = myGetParameter(s, "popup_panning")) != null
				&& s1.equalsIgnoreCase("true"))
				popupPanning = true;

		if ((s1 = myGetParameter(s, "imgLoadFeedback")) != null
				&& s1.equalsIgnoreCase("false"))
				imgLoadFeedback = false;
				
		if ((s1 = myGetParameter(s, "outOfMemoryURL")) != null) {
			outOfMemoryURL = s1;
		}

		if ((s1 = myGetParameter(s, "authoringMode")) != null
				&& s1.equalsIgnoreCase("true"))
			authoringMode = true;

		if ((s1 = myGetParameter(s, "horizonposition")) != null)
			renderer.horizonPosition = Integer.parseInt(s1);

		if ((s1 = myGetParameter(s, "statusMessage")) != null) {
			statusMessage = s1;
		}
	}


	void executeJavascriptCommand(String s) {
		if (s != null)
			try {
				Class class1;
				Object obj =
					(class1 = Class.forName("netscape.javascript.JSObject"))
						.getMethod(
							"getWindow",
							new Class[] { java.applet.Applet.class })
						.invoke(class1, new Object[] { this });
				class1.getMethod(
					"eval",
					new Class[] { java.lang.String.class }).invoke(
					obj,
					new Object[] { s });
				return;
			} catch (Exception _ex) {
			}
	}

	void executePTViewerCommand(String s) {
		stopThread(ptviewerScript);
		ptviewerScript = new Thread(this);
		PTScript = s;
		ptviewerScript.start();
	}

	void PTViewerScript(String s) {
		int i;
		String s2;
		if ((i = getNumArgs(s, ';')) > 0) {
			for (int j = 0; j < i; j++) {
				String s1;
				s2 = stripWhiteSpace(getArg(j, s, ';'));
				s1 = s2;
				if( s2 != null ) {
					if (s2.equals("loop()"))
						j = -1;
					else
						PTViewerCommand(s1);
				}
			}

		}
	}

	void PTViewerCommand(String s) {
		String parsedNumberRange;
		int argCount;
		String s1 = s.substring(s.indexOf('(') + 1, s.indexOf(')'));
		if (s.startsWith("ZoomIn")) {
			ZoomIn();
			return;
		}
		if (s.startsWith("ZoomOut")) {
			ZoomOut();
			return;
		}
		if (s.startsWith("panUp")) {
			panUp();
			return;
		}
		if (s.startsWith("panDown")) {
			panDown();
			return;
		}
		if (s.startsWith("panLeft")) {
			panLeft();
			return;
		}
		if (s.startsWith("panRight")) {
			panRight();
			return;
		}
		if (s.startsWith("showHS")) {
			showHS();
			return;
		}
		if (s.startsWith("hideHS")) {
			hideHS();
			return;
		}
		if (s.startsWith("toggleHS")) {
			toggleHS();
			return;
		}
		if (s.startsWith("gotoView")) {
			if (getNumArgs(s1) == 3) {
				gotoView(
					Double.valueOf(getArg(0, s1)).doubleValue(),
					Double.valueOf(getArg(1, s1)).doubleValue(),
					Double.valueOf(getArg(2, s1)).doubleValue());
				return;
			}
		} else if (s.startsWith("startAutoPan")) {
			if (getNumArgs(s1) == 3) {
				startAutoPan(
					Double.valueOf(getArg(0, s1)).doubleValue(),
					Double.valueOf(getArg(1, s1)).doubleValue(),
					Double.valueOf(getArg(2, s1)).doubleValue());
				return;
			}
			if (getNumArgs(s1) == 4) {
				startAutoPan(
					Double.valueOf(getArg(0, s1)).doubleValue(),
					Double.valueOf(getArg(1, s1)).doubleValue(),
					Double.valueOf(getArg(2, s1)).doubleValue(),
					Double.valueOf(getArg(3, s1)).doubleValue());
				return;
			}
		} else {
			if (s.startsWith("stopAutoPan")) {
				stopAutoPan();
				return;
			}
			if (s.startsWith("newPanoFromList")) {
				if (getNumArgs(s1) == 1) {
					newPanoFromList(Integer.parseInt(s1));
					return;
				}
				if (getNumArgs(s1) == 4) {
					newPanoFromList(
						Integer.parseInt(getArg(0, s1)),
						Double.valueOf(getArg(1, s1)).doubleValue(),
						Double.valueOf(getArg(2, s1)).doubleValue(),
						Double.valueOf(getArg(3, s1)).doubleValue());
					return;
				}
			} else {
				if (s.startsWith("newPano")) {
					newPano(s1);
					return;
				}
				if (s.startsWith("SetURL")) {
					SetURL(s1);
					return;
				}
				if (s.startsWith("PlaySound")) {
					PlaySound(Integer.parseInt(s1));
					return;
				}
				if (s.startsWith("moveFromTo")) {
					if (getNumArgs(s1) == 7) {
						moveFromTo(
							Double.valueOf(getArg(0, s1)).doubleValue(),
							Double.valueOf(getArg(1, s1)).doubleValue(),
							Double.valueOf(getArg(2, s1)).doubleValue(),
							Double.valueOf(getArg(3, s1)).doubleValue(),
							Double.valueOf(getArg(4, s1)).doubleValue(),
							Double.valueOf(getArg(5, s1)).doubleValue(),
							Integer.valueOf(getArg(6, s1)).intValue(),
							0D);
						return;
					}else if(getNumArgs(s1) == 8){
						moveFromTo(
							Double.valueOf(getArg(0, s1)).doubleValue(),
							Double.valueOf(getArg(1, s1)).doubleValue(),
							Double.valueOf(getArg(2, s1)).doubleValue(),
							Double.valueOf(getArg(3, s1)).doubleValue(),
							Double.valueOf(getArg(4, s1)).doubleValue(),
							Double.valueOf(getArg(5, s1)).doubleValue(),
							Integer.valueOf(getArg(6, s1)).intValue(),
							Double.valueOf(getArg(7, s1)).doubleValue());
						return;
					}
				} else if (s.startsWith("moveTo")) {
					if (getNumArgs(s1) == 4) {
						moveTo(
							Double.valueOf(getArg(0, s1)).doubleValue(),
							Double.valueOf(getArg(1, s1)).doubleValue(),
							Double.valueOf(getArg(2, s1)).doubleValue(),
							Integer.valueOf(getArg(3, s1)).intValue(),
							0D);
						return;
					} else if (getNumArgs(s1) == 5) {
						moveTo(
							Double.valueOf(getArg(0, s1)).doubleValue(),
							Double.valueOf(getArg(1, s1)).doubleValue(),
							Double.valueOf(getArg(2, s1)).doubleValue(),
							Integer.valueOf(getArg(3, s1)).intValue(),
							Double.valueOf(getArg(4, s1)).doubleValue());
						return;
					}
				} else {
					if (s.startsWith("DrawSHSImage")) {
						parsedNumberRange = parseNumberRange(s1);
						if ((argCount = getNumArgs(parsedNumberRange)) > 0) {
							for (int argLoop = 0; argLoop < argCount; argLoop++) {
								DrawSHSImage(Integer.parseInt(stripWhiteSpace(getArg(argLoop, parsedNumberRange))));
							}
						}
						return;
					}
					if (s.startsWith("DrawSHSPopup")) {
						parsedNumberRange = parseNumberRange(s1);
						if ((argCount = getNumArgs(parsedNumberRange)) > 0) {
							for (int argLoop = 0; argLoop < argCount; argLoop++) {
								DrawSHSPopup(Integer.parseInt(stripWhiteSpace(getArg(argLoop, parsedNumberRange))));
							}
						}
						return;
					}
					if (s.startsWith("HideSHSImage")) {
						parsedNumberRange = parseNumberRange(s1);
						if ((argCount = getNumArgs(parsedNumberRange)) > 0) {
							for (int argLoop = 0; argLoop < argCount; argLoop++) {
								HideSHSImage(Integer.parseInt(stripWhiteSpace(getArg(argLoop, parsedNumberRange))));
							}
						}
						return;
					}
					if (s.startsWith("DrawHSImage")) {
						DrawHSImage(Integer.parseInt(s1));
						return;
					}
					if (s.startsWith("HideHSImage")) {
						HideHSImage(Integer.parseInt(s1));
						return;
					}
					if (s.startsWith("ToggleHSImage")) {
						ToggleHSImage(Integer.parseInt(s1));
						return;
					}
					if (s.startsWith("ToggleSHSImage")) {
						ToggleSHSImage(Integer.parseInt(s1));
						return;
					}
					if (s.startsWith("waitWhilePanning")) {
						waitWhilePanning();
						return;
					}
					if (s.startsWith("startApplet")) {
						startApplet(Integer.parseInt(s1));
						return;
					}
					if (s.startsWith("stopApplet")) {
						stopApplet(Integer.parseInt(s1));
						return;
					}
					if (s.startsWith("loadROI"))
						if (getNumArgs(s1) == 2) {
							loadROI(
								Integer.valueOf(getArg(0, s1)).intValue(),
								Integer.valueOf(getArg(1, s1)).intValue());
							return;
						} else {
							loadROI(Integer.parseInt(s1));
							return;
						}
					if (s.startsWith("setQuality"))
						setQuality(Integer.parseInt(s1));
				}
			}
		}
	}

	public synchronized void DrawSHSImage(int i) {
		if (i >= 0 && i < numshs && shs_imode[i] != 2) {
			shs_imode[i] = 2;
			repaint();
		}
	}

	public synchronized void DrawSHSPopup(int i) {
		if (i >= 0 && i < numshs && shs_imode[i] != 1) {
			shs_imode[i] = 1;
			repaint();
		}
	}

	public synchronized void HideSHSImage(int i) {
		if (i >= 0 && i < numshs && shs_imode[i] != 0) {
			shs_imode[i] = 0;
			repaint();
		}
	}

	public synchronized void ToggleSHSImage(int i) {
		if (i >= 0 && i < numshs) {
			if (shs_imode[i] != 0) {
				HideSHSImage(i);
				return;
			}
			if (shs_imode[i] != 2)
				DrawSHSImage(i);
		}
	}

	public synchronized void DrawHSImage(int i) {
		if (i >= 0 && i < renderer.numhs && (renderer.hs_imode[i] & 2) == 0) {
			renderer.hs_imode[i] |= 2;
			repaint();
		}
	}

	public synchronized void HideHSImage(int i) {
		if (i >= 0 && i < renderer.numhs && (renderer.hs_imode[i] & 2) != 0) {
			renderer.hs_imode[i] &= -3;
			repaint();
		}
	}

	public synchronized void ToggleHSImage(int i) {
		if (i >= 0 && i < renderer.numhs) {
			if ((renderer.hs_imode[i] & 2) != 0) {
				HideHSImage(i);
				return;
			}
			if ((renderer.hs_imode[i] & 2) == 0)
				DrawHSImage(i);
		}
	}

	public double get_x() {
		double d = -1D;
		if (click_x >= 0 && click_y >= 0)
			d =
				((double) math_int_view2pano(click_x - vx,
					click_y - vy,
					renderer.vwidth,
					renderer.vheight,
					renderer.pwidth,
					renderer.pheight,
					renderer.yaw,
					renderer.pitch,
					renderer.hfov)[0]
					* 100D)
					/ (double) renderer.pwidth;
		return d;
	}

	public double get_y() {
		double d = -1D;
		if (click_x >= 0 && click_y >= 0)
			d =
				((double) math_int_view2pano(click_x - vx,
					click_y - vy,
					renderer.vwidth,
					renderer.vheight,
					renderer.pwidth,
					renderer.pheight,
					renderer.yaw,
					renderer.pitch,
					renderer.hfov)[1]
					* 100D)
					/ (double) renderer.pheight;
		click_x = -1;
		click_y = -1;
		return d;
	}

	public int getPanoNumber() {
		return CurrentPano;
	}

	public void startCommunicating(Applet applet) {
		synchronized (sender) {
			if (applet != null)
				sender.put(applet, applet);
			else
				sender.clear();
		}
		renderer.dirty = true;
		repaint();
	}

	public void stopCommunicating(Applet applet) {
		if (applet != null) {
			synchronized (sender) {
				sender.remove(applet);
			}
			renderer.dirty = true;
			repaint();
		}
	}

	public String parseNumberRange(String numberRange) {
		int argCount;
		int subArgCount;
		String singleNum;
		String fromNum;
		String toNum;
		String returnString;

		returnString = "";
		if ((argCount = getNumArgs(numberRange)) > 0) {
			for (int argLoop = 0; argLoop < argCount; argLoop++) {
				singleNum = stripWhiteSpace(getArg(argLoop, numberRange));
				subArgCount = getNumArgs(singleNum, '-');
				if (subArgCount == 1) {
					returnString = addAnotherArg(returnString, singleNum);
				} else if (subArgCount == 2) {
					fromNum = stripWhiteSpace(getArg(0, singleNum, '-'));
					toNum = stripWhiteSpace(getArg(1, singleNum, '-'));
					for (int subArgLoop = Integer.parseInt(fromNum); subArgLoop <= Integer.parseInt(toNum); subArgLoop++) {
						returnString = addAnotherArg(returnString, String.valueOf(subArgLoop));
					}
				}
			}
		}
		return returnString;
	}

	public String addAnotherArg(String currentArgs, String newArg) {
		if (currentArgs == "") {
			currentArgs = newArg;
		} else {
			currentArgs = currentArgs + "," + newArg;
		}
		return currentArgs;
	}

	private String m1() {
		String s;
		int i;
		if ((i = (s = getDocumentBase().getFile()).indexOf(':')) != -1
			&& i + 1 < s.length())
			return s.substring(i + 1);
		if ((i = s.indexOf('|')) != -1 && i + 1 < s.length())
			return s.substring(i + 1);
		else
			return s;
	}

	void stopThread(Thread thread) {
		if (thread != null && thread.isAlive())
			try {
				thread.checkAccess();
				thread.stop();
				return;
			} catch (SecurityException _ex) {
				thread.destroy();
			}
	}

	void ptinsertImage(int pd[][], int xi, int yi, Image im, int ntiles) {
		if (im != null) {
			new ImageTo2DIntArrayExtractor(pd, xi, yi, im).doit();
			renderer.dirty = true;
		}
	}
	
//	void ptinsertImage(int pd[][], int xi, int yi, Image im, int ntiles) {
//		if (im != null) {
//			int w = im.getWidth(null);
//			int h = im.getHeight(null);
//			if (ntiles > h)
//				ntiles = h;
//			int ht = ((h + ntiles) - 1) / ntiles;
//			int idata[] = new int[w * ht];
//			for (int i = 0; i < ntiles; i++) {
//				int sheight = ht + i * ht <= h ? ht : h - i * ht;
//				PixelGrabber pixelgrabber =
//					new PixelGrabber(im, 0, i * ht, w, sheight, idata, 0, w);
//				try {
//					pixelgrabber.grabPixels();
//				} catch (InterruptedException _ex) {
//					return;
//				}
//				im_insertRect(pd, xi, yi + i * ht, idata, w, 0, 0, w, sheight);
//				dirty = true;
//				Thread.yield();	// to allow panning
//			}
//
//		}
//	}

	boolean is_inside_viewer(int i, int j) {
		return i >= vx && j >= vy && i < vx + renderer.vwidth && j < vy + renderer.vheight;
	}

	int[] get_cube_order(int i, int j) {
		int ai[];
		(ai = new int[6])[0] = 0;
		ai[1] = 1;
		ai[2] = 2;
		ai[3] = 3;
		ai[4] = 4;
		ai[5] = 5;
		if (j > 45) {
			ai[0] = 4;
			switch (i / 45) {
				case 0 : // '\0'
					ai[1] = 2;
					ai[2] = 3;
					ai[3] = 1;
					ai[4] = 0;
					ai[5] = 5;
					break;

				case -1 :
					ai[1] = 2;
					ai[2] = 1;
					ai[3] = 3;
					ai[4] = 0;
					ai[5] = 5;
					break;

				case 1 : // '\001'
					ai[1] = 3;
					ai[2] = 2;
					ai[3] = 1;
					ai[4] = 0;
					ai[5] = 5;
					break;

				case 2 : // '\002'
					ai[1] = 3;
					ai[2] = 0;
					ai[3] = 1;
					ai[4] = 2;
					ai[5] = 5;
					break;

				case 3 : // '\003'
					ai[1] = 0;
					ai[2] = 3;
					ai[3] = 1;
					ai[4] = 2;
					ai[5] = 5;
					break;

				case -2 :
					ai[1] = 1;
					ai[2] = 0;
					ai[3] = 3;
					ai[4] = 2;
					ai[5] = 5;
					break;

				case -3 :
					ai[1] = 1;
					ai[2] = 0;
					ai[3] = 3;
					ai[4] = 2;
					ai[5] = 5;
					break;

				default :
					ai[1] = 0;
					ai[2] = 1;
					ai[3] = 3;
					ai[4] = 2;
					ai[5] = 5;
					break;
			}
		} else if (j < -45) {
			ai[0] = 5;
			switch (i / 45) {
				case 0 : // '\0'
					ai[1] = 2;
					ai[2] = 3;
					ai[3] = 1;
					ai[4] = 0;
					ai[5] = 4;
					break;

				case -1 :
					ai[1] = 2;
					ai[2] = 1;
					ai[3] = 3;
					ai[4] = 0;
					ai[5] = 4;
					break;

				case 1 : // '\001'
					ai[1] = 3;
					ai[2] = 2;
					ai[3] = 1;
					ai[4] = 0;
					ai[5] = 4;
					break;

				case 2 : // '\002'
					ai[1] = 3;
					ai[2] = 0;
					ai[3] = 1;
					ai[4] = 2;
					ai[5] = 4;
					break;

				case 3 : // '\003'
					ai[1] = 0;
					ai[2] = 3;
					ai[3] = 1;
					ai[4] = 2;
					ai[5] = 4;
					break;

				case -2 :
					ai[1] = 1;
					ai[2] = 0;
					ai[3] = 3;
					ai[4] = 2;
					ai[5] = 4;
					break;

				case -3 :
					ai[1] = 1;
					ai[2] = 0;
					ai[3] = 3;
					ai[4] = 2;
					ai[5] = 4;
					break;

				default :
					ai[1] = 0;
					ai[2] = 1;
					ai[3] = 3;
					ai[4] = 2;
					ai[5] = 4;
					break;
			}
		} else {
			switch (i / 45) {
				case 0 : // '\0'
					ai[0] = 2;
					ai[1] = 3;
					ai[2] = j <= 0 ? 5 : 4;
					ai[3] = 1;
					ai[4] = 0;
					ai[5] = j <= 0 ? 4 : 5;
					break;

				case -1 :
					ai[0] = 2;
					ai[1] = 1;
					ai[2] = j <= 0 ? 5 : 4;
					ai[3] = 3;
					ai[4] = 0;
					ai[5] = j <= 0 ? 4 : 5;
					break;

				case 1 : // '\001'
					ai[0] = 3;
					ai[1] = 2;
					ai[2] = j <= 0 ? 5 : 4;
					ai[3] = 1;
					ai[4] = 0;
					ai[5] = j <= 0 ? 4 : 5;
					break;

				case 2 : // '\002'
					ai[0] = 3;
					ai[1] = 0;
					ai[2] = j <= 0 ? 5 : 4;
					ai[3] = 1;
					ai[4] = 2;
					ai[5] = j <= 0 ? 4 : 5;
					break;

				case 3 : // '\003'
					ai[0] = 0;
					ai[1] = 3;
					ai[2] = j <= 0 ? 5 : 4;
					ai[3] = 1;
					ai[4] = 2;
					ai[5] = j <= 0 ? 4 : 5;
					break;

				case -2 :
					ai[0] = 1;
					ai[1] = 0;
					ai[2] = j <= 0 ? 5 : 4;
					ai[3] = 3;
					ai[4] = 2;
					ai[5] = j <= 0 ? 4 : 5;
					break;

				case -3 :
					ai[0] = 1;
					ai[1] = 0;
					ai[2] = j <= 0 ? 5 : 4;
					ai[3] = 3;
					ai[4] = 2;
					ai[5] = j <= 0 ? 4 : 5;
					break;

				default :
					ai[0] = 0;
					ai[1] = 1;
					ai[2] = j <= 0 ? 5 : 4;
					ai[3] = 3;
					ai[4] = 2;
					ai[5] = j <= 0 ? 4 : 5;
					break;
			}
		}
		return ai;
	}

	public Image loadImage(String s) {
	
		Image image;
		byte readBuffer[];

		// first try to load the image from the jar file
		if ((image = readImageFromJAR(s)) != null ) {
			return image;
		}
		
		if ((readBuffer = file_read(s, null)) != null ) {
			if((image = bufferToImage(readBuffer)) != null)
				return image;
		}
		try {
			URL url = new URL(getDocumentBase(), s);
			Image image1 = getImage(url);
			MediaTracker mediatracker;
			(mediatracker = new MediaTracker(this)).addImage(image1, 0);
			mediatracker.waitForAll();
			if (image1 == null || image1.getWidth(null) <= 0)
				return null;
			else
				return image1;
		} catch (Exception _ex) {
			return null;
		}
	}

	Image loadImageProgress(String s) {
		percent[0] = 0;
		byte abyte0[];
		if ((abyte0 = file_read(s, percent)) != null) {
			Image image = bufferToImage(abyte0);
			percent[0] = 100;
			repaint();
			if (image != null)
				return image;
		}
		return loadImage(s);
	}

	Image bufferToImage(byte abyte0[]) {
		if (abyte0 == null)
			return null;
		Image image = Toolkit.getDefaultToolkit().createImage(abyte0);
		MediaTracker mediatracker;
		(mediatracker = new MediaTracker(this)).addImage(image, 0);
		try {
			mediatracker.waitForAll();
		} catch (InterruptedException _ex) {
			return null;
		}
		return image;
	}

	

	void ptImageTo2DArray(int ai[][], Image image) {
		if (image == null || ai == null)
			return;
		new ImageTo2DIntArrayExtractor (ai, image).doit();
	}
	
//	void ptImageTo2DArray(int ai[][], Image image) {
//		if (image == null || ai == null)
//			return;
//		int i;
//		if ((i = image.getHeight(null)) * image.getWidth(null) > im_maxarray)
//			i = im_maxarray / image.getWidth(null);
//		int ai1[] = new int[i * image.getWidth(null)];
//		for (int j = 0; j < image.getHeight(null); j += i) {
//			int j1 =
//				i >= image.getHeight(null) - j ? image.getHeight(null) - j : i;
//			PixelGrabber pixelgrabber =
//				new PixelGrabber(
//					image,
//					0,
//					j,
//					image.getWidth(null),
//					j1,
//					ai1,
//					0,
//					image.getWidth(null));
//			try {
//				pixelgrabber.grabPixels();
//			} catch (InterruptedException _ex) {
//				return;
//			}
//			for (int i1 = 0; i1 < j1; i1++) {
//				int w = image.getWidth(null);
//				int k = i1 * w;
//				for (int l = 0; l < w; l++)
//					ai[i1 + j][l] = ai1[k + l] | 0xff000000;
////					System.arraycopy( ai1, k, ai[i1 + j], 0, w );
//			}
//		}
//		System.gc();
//	}

	void ptImageToAlpha(int ai[][], Image image) {
		if (image == null || ai == null)
			return;
		int i;
		if ((i = image.getHeight(null)) * image.getWidth(null) > im_maxarray)
			i = im_maxarray / image.getWidth(null);
		int ai1[] = new int[i * image.getWidth(null)];
		for (int k = 0; k < image.getHeight(null); k += i) {
			int k1 =
				i >= image.getHeight(null) - k ? image.getHeight(null) - k : i;
			PixelGrabber pixelgrabber =
				new PixelGrabber(
					image,
					0,
					k,
					image.getWidth(null),
					k1,
					ai1,
					0,
					image.getWidth(null));
			try {
				pixelgrabber.grabPixels();
			} catch (InterruptedException _ex) {
				return;
			}
			for (int j1 = 0; j1 < k1; j1++) {
				int l = j1 * image.getWidth(null);
				for (int i1 = 0; i1 < image.getWidth(null); i1++) {
					int j = ((ai1[l + i1] & 0xff) << 24) + 0xffffff;
					ai[j1 + k][i1] &= j;
				}

			}

		}

		System.gc();
	}

	final void im_extractRect(
		int ai[][],
		int i,
		int j,
		int ai1[],
		int k,
		int l,
		int i1,
		int j1,
		int k1) {
		try {
			int i2 = 0;
			for (int j2 = j; i2 < k1; j2++) {
				int l1 = 0;
				for (int k2 = (i1 + i2) * k + l; l1 < j1; k2++) {
					ai1[k2] = renderer.imagePixels[j2][l1 + i] | 0xff000000;
					l1++;
				}

				i2++;
			}

			return;
		} catch (Exception _ex) {
			System.out.println("Invalid rectangle");
		}
	}

	// loads and parses a .ptvref file
	// returns the name of the preview file or null if there is no preview
	String loadPTVRefFile( String fname ) {
		
		byte[] buf = file_read( fname, null );
		if( buf == null ) {
			fatal = true;
			repaint();
			return null;
		}
		
		// extract the path to the file name to correctly locate the referenced files
		String path = "";
		int idx = fname.lastIndexOf('/');
		if( idx >= 0 ) {
			path = fname.substring( 0, idx + 1 );
		}
		
		ByteArrayInputStream ba = new ByteArrayInputStream( buf );
		InputStreamReader isr = new InputStreamReader( ba );
		BufferedReader br = new BufferedReader( isr );
		String previewName, s;
		
		try {
			// reads the preview name
			previewName = br.readLine();
			if( previewName.length() == 0 )
				previewName = null;
			else
				previewName = path + previewName;
			s = br.readLine();
			// pano width
			renderer.pwidth = Integer.valueOf(s).intValue();
			s = br.readLine();
			// pano height
			renderer.pheight = Integer.valueOf(s).intValue();
			s = br.readLine();
			// numbero of ROIs
			numroi = Integer.valueOf(s).intValue();
			roi_allocate( numroi );
			// reads the ROI lines
			int i = 0;
			while( (s = br.readLine()) != null ) {
				if( s.length() > 0 ) {
					ParseROILine( s, i );
				}
				if( i < numroi ) roi_im[i] = path + roi_im[i];	// adds the path
				i++;
			}
			dynLoadROIs = true;
			return previewName;
		}
		catch( Exception ex ) {
			return null;
		}
	}
	
	final int[][] im_loadPano(String fname, int pd[][], int pw, int ph) {
		
		System.out.println("loading");
		
		ptvf = null;
		boolean showGrid;
		boolean isPTVREF, isPTV;	// flags to see if the file is a .ptvref or a .ptv
		
		isPTV = isPTVREF = false;

		if( fname != null ) {
			if( fname.toUpperCase().endsWith(".PTVREF") ) isPTVREF = true;
			if( fname.toUpperCase().endsWith(".PTV") ) isPTV = true;
			// sees if the filename is in the form:
			//  name.ptvref.txt  or  name.ptv.jpg
			// to bypass web servers that ignore unknows extensions
			if( fname.toUpperCase().indexOf(".PTVREF.") >= 0 ) isPTVREF = true;
			if( fname.toUpperCase().indexOf(".PTV.") >= 0 ) isPTV = true;
		}
		
		// if we are using a .ptvref file open and parse it
		if( isPTVREF ) {
			fname = loadPTVRefFile( fname );
			// pwidth and pheight are set by loadPTVRefFile() 
			pw = renderer.pwidth;
			ph = renderer.pheight;
		}
		
		// let's see if we are using a .ptv file
		if( isPTV ) { 
			usingCustomFile = true;
			ptvf = new PTVFile( this, fname );
			showGrid = !ptvf.hasPreview;
			pw = ptvf.pWidth;
			ph = ptvf.pHeight;
		}
		else {
			usingCustomFile = false;
			showGrid = (fname == null || fname.equals("_PT_Grid"));
		}

		if ( showGrid ) {
			if (pw == 0)
				pw = 100;	// dummy background
			
			// create grid panorama
			int p[][] = Utils.im_allocate_pano(pd, pw, ph != 0 ? ph : pw >> 1);
			Utils.im_drawGrid(p, grid_bgcolor, grid_fgcolor);
			return p;
		}

		Image pano;
		if( usingCustomFile ) {
			pano = ptvf.loadPreviewImage();
		}
		else {
			//LOADING FILE
			pano = loadImageProgress(fname);
		}
		if (pano == null)
			return null;

		// At this point we have a valid panorama image
	 	// Check size:
		
		if (pw > pano.getWidth(null)) {
			if (ph == 0)
				ph = pw >> 1;
		} else {
			pw = pano.getWidth(null);
			ph = pano.getHeight(null);
		}
		// Set up data array for panorama pixels
		int p[][];
		if ((p = Utils.im_allocate_pano(pd, pw, ph)) == null)
			return null;
		ptImageTo2DArray(p, pano);
		
		
		
		
		
		if (pw != pano.getWidth(null)) {
			
			Utils.scaleImage(p, pano.getWidth(null), pano.getHeight(null));
			if( dynLoadROIs ) {
				// this is a low resolution preview: do not draw it with Lanczos2
				forceBilIntepolator = true;
			}
		}
		return p;
	}

	

	String resolveQuotes(String s) {
		if (s == null)
			return null;
		int j;
		if ((j = s.length()) < 6)
			return s;
		StringBuffer stringbuffer = new StringBuffer(0);
		int i;
		for (i = 0; i < j - 5; i++)
			if (s.substring(i, i + 6).equalsIgnoreCase("&quot;")) {
				stringbuffer.append('"');
				i += 5;
			} else {
				stringbuffer.append(s.charAt(i));
			}

		stringbuffer.append(s.substring(i, j));
		return stringbuffer.toString();
	}

	String stripWhiteSpace(String s) {
		if (s == null)
			return null;
		int i = 0;
		int j;
		int k = (j = s.length()) - 1;
		for (;
			i < j
				&& (s.charAt(i) == ' '
					|| s.charAt(i) == '\r'
					|| s.charAt(i) == '\n'
					|| s.charAt(i) == '\t');
			i++);
		if (i == j)
			return null;
		for (;
			k >= 0
				&& (s.charAt(k) == ' '
					|| s.charAt(k) == '\r'
					|| s.charAt(k) == '\n'
					|| s.charAt(k) == '\t');
			k--);
		if (k < 0 || k < i)
			return null;
		else
			return s.substring(i, k + 1);
	}

	Dimension string_textWindowSize(Graphics g, String s) {
		FontMetrics fontmetrics = g.getFontMetrics();
		int i = 0;
		int k = 1;
		int l = 0;
		int j;
		while ((j = s.indexOf('|', i)) != -1 && j < s.length() - 1) {
			int i1;
			if ((i1 = fontmetrics.stringWidth(s.substring(i, j))) > l)
				l = i1;
			k++;
			i = j + 1;
		}
		int j1;
		if ((j1 = fontmetrics.stringWidth(s.substring(i))) > l)
			l = j1;
		return new Dimension(
			l + 10,
			k * fontmetrics.getHeight() + (fontmetrics.getHeight() >> 1));
	}

	void string_drawTextWindow(
		Graphics g,
		int i,
		int j,
		Dimension dimension,
		Color color,
		String s,
		int k) {
		g.clearRect(i, j, dimension.width, dimension.height);
		if (color == null)
			g.setColor(Color.black);
		else
			g.setColor(color);
		FontMetrics fontmetrics = g.getFontMetrics();
		int l = 0;
		int j1 = 1;
		int i1;
		while ((i1 = s.indexOf('|', l)) != -1 && i1 < s.length() - 1) {
			g.drawString(
				s.substring(l, i1),
				i + 5,
				j + j1 * fontmetrics.getHeight());
			j1++;
			l = i1 + 1;
		}
		g.drawString(s.substring(l), i + 5, j + j1 * fontmetrics.getHeight());
		switch (k) {
			case 1 : // '\001'
				g.fillRect(i, (j + dimension.height) - 2, 2, 2);
				return;

			case 2 : // '\002'
				g.fillRect(i, j, 2, 2);
				return;

			case 3 : // '\003'
				g.fillRect(
					(i + dimension.width) - 2,
					(j + dimension.height) - 2,
					2,
					2);
				return;

			case 4 : // '\004'
				g.fillRect((i + dimension.width) - 2, j, 2, 2);
				break;
		}
	}

	//    public String myGetParameter(String s, String s1)
	//    {
	//        String s2;
	//        String s3;
	//        if(s != null)
	//            break MISSING_BLOCK_LABEL_20;
	//        s3 = resolveQuotes(getParameter(s1));
	//        s3;
	//        s2 = s3;
	//        JVM INSTR ifnull 33;
	//           goto _L1 _L2
	//_L1:
	//        break MISSING_BLOCK_LABEL_18;
	//_L2:
	//        break MISSING_BLOCK_LABEL_33;
	//        return s2;
	//        if((s2 = extractParameter(s, s1)) != null)
	//            return s2;
	//        return extractParameter(PTViewer_Properties, s1);
	//    }

	/** Read parameter values from a list of parameter tags.
	 * The list has the syntax <p>
	 *<CODE>{param1=value1} {param2=value2} {param3=value3}</CODE>
	 * @param p The list string.
	 * @param param The parameter name.
	 */
	// from version 2.1
	public String myGetParameter(String p, String param) {
		String r;

		if (p == null) {
			r = resolveQuotes(getParameter(param));
			if (r != null) {
				return r;
			}
		} else {
			r = extractParameter(p, param);
			if (r != null) {
				return r;
			}
		}

		return extractParameter(PTViewer_Properties, param);
	}

	String extractParameter(String s, String s1) {
		int j = 0;
		if (s == null || s1 == null)
			return null;
		int i;
		String s2;
		String s1u, s2u; // upper case versions of s1 and s2, used for case insensitivity
		s1u = s1.toUpperCase();
		while ((i = s.indexOf('{', j)) >= 0 && (j = s.indexOf('}', i)) >= 0) {
			s2 = stripWhiteSpace(s.substring(i + 1, j));
			s2u = s2.toUpperCase();
			if (s2u.startsWith(s1u + "=") )
				return resolveQuotes(
					stripWhiteSpace(s2.substring(s2.indexOf('=') + 1)));
		}
		return null;
	}

	int getNextWord(int i, String s, StringBuffer stringbuffer) {
		int j = i;
		int k = s.length();
		if (i >= k)
			return i;
		if (s.charAt(i) == '\'') {
			if (++i == k) {
				stringbuffer.setLength(0);
				return i;
			}
			j = i;
			for (; i < k && s.charAt(i) != '\''; i++);
			if (i < k) {
				stringbuffer.insert(0, s.substring(j, i));
				stringbuffer.setLength(s.substring(j, i).length());
			} else {
				stringbuffer.insert(0, s.substring(j));
				stringbuffer.setLength(s.substring(j).length());
			}
			return i;
		}
		if (s.charAt(i) == '$') {
			if (++i == k) {
				stringbuffer.setLength(0);
				return i;
			}
			char c = s.charAt(i);
			if (++i == k) {
				stringbuffer.setLength(0);
				return i;
			}
			j = i;
			for (; i < k && s.charAt(i) != c; i++);
			if (i < k) {
				stringbuffer.insert(0, s.substring(j, i));
				stringbuffer.setLength(s.substring(j, i).length());
			} else {
				stringbuffer.insert(0, s.substring(j));
				stringbuffer.setLength(s.substring(j).length());
			}
			return i;
		}
		for (;
			i < k
				&& s.charAt(i) != ' '
				&& s.charAt(i) != '\r'
				&& s.charAt(i) != '\n'
				&& s.charAt(i) != '\t';
			i++);
		if (i < k) {
			stringbuffer.insert(0, s.substring(j, i));
			stringbuffer.setLength(s.substring(j, i).length());
		} else {
			stringbuffer.insert(0, s.substring(j));
			stringbuffer.setLength(s.substring(j).length());
		}
		return i;
	}

	final String getArg(int i, String s, char c) {
		int k = 0;
		if (s == null)
			return null;
		for (int j = 0; j < i; j++) {
			if ((k = s.indexOf(c, k)) == -1)
				return null;
			k++;
		}

		int l;
		if ((l = s.indexOf(c, k)) == -1)
			return s.substring(k);
		else
			return s.substring(k, l);
	}

	final String getArg(int i, String s) {
		return getArg(i, s, ',');
	}

	final int getNumArgs(String s) {
		return getNumArgs(s, ',');
	}

	final int getNumArgs(String s, char c) {
		int j = 0;
		if (s == null)
			return 0;
		int i;
		for (i = 1;(j = s.indexOf(c, j)) != -1; i++)
			j++;

		return i;
	}

	void file_init() {
		file_cachefiles = true;
		file_Cache = new Hashtable();
	}

	void file_dispose() {
		if (file_Cache != null) {
			file_Cache.clear();
			file_Cache = null;
		}
	}

	// reads an image from the jar file containing the applet
	// returns null if not found
	Image readImageFromJAR( String name ) {
		byte readBuffer[];
		Image im;
		
		try {
			MediaTracker m = new MediaTracker( this );
			InputStream is = getClass().getResourceAsStream( name );
			if( is == null ) return null;
			readBuffer = new byte[is.available()];
			is.read( readBuffer );
			im = Toolkit.getDefaultToolkit().createImage(readBuffer);
			m.addImage( im, 0 );
			m.waitForAll();
		}
		catch( Exception e ) {
			im = null;
		}
			return im;
	}
	
	
	byte[] file_read(String name, int progress[]) {
		byte readBuffer[];
		if ((readBuffer = (byte[]) file_Cache.get(name)) != null) {
			if (progress != null) {
				progress[0] = 80;
				repaint();
			}
			return readBuffer;
		}
		try {
			URLConnection urlconnection;
			(
				urlconnection =
					(new URL(getDocumentBase(), name))
						.openConnection())
						.setUseCaches(
				true);
			int i;
			try {
				i = urlconnection.getContentLength();
			} catch (Exception _ex) {
				i = 0;
			}
			InputStream inputstream = urlconnection.getInputStream();
			readBuffer = file_read(inputstream, i, progress);
			inputstream.close();
			if (readBuffer != null) {
				m3(readBuffer, name);
				if (file_cachefiles)
					synchronized (file_Cache) {
						file_Cache.put(name, readBuffer);
					}
				return readBuffer;
			}
		} catch (Exception _ex) {
		}
		try {
			URLConnection urlconnection1;
			(
				urlconnection1 =
					(new URL(getCodeBase(), name)).openConnection()).setUseCaches(
				true);
			int j;
			try {
				j = urlconnection1.getContentLength();
			} catch (Exception _ex) {
				j = 0;
			}
			InputStream inputstream1 = urlconnection1.getInputStream();
			readBuffer = file_read(inputstream1, j, progress);
			inputstream1.close();
			if (readBuffer != null) {
				m3(readBuffer, name);
				if (file_cachefiles)
					synchronized (file_Cache) {
						file_Cache.put(name, readBuffer);
					}
				return readBuffer;
			}
		} catch (Exception _ex) {
		}
		try {
			InputStream inputstream2;
			if ((inputstream2 =
				Class.forName("ptviewer").getResourceAsStream(name))
				!= null) {
				readBuffer = file_read(inputstream2, 0, null);
				inputstream2.close();
			}
			if (readBuffer != null) {
				m3(readBuffer, name);
				if (file_cachefiles)
					synchronized (file_Cache) {
						file_Cache.put(name, readBuffer);
					}
				return readBuffer;
			}
		} catch (Exception _ex) {
		}
		return null;
	}

	byte[] file_read(InputStream is, int fsize, int progress[]) {
		int j = 0;
		int l = 0;
		int i1 = fsize <= 0 ? 50000 : fsize / 10 + 1;
		byte abyte0[] = new byte[fsize <= 0 ? 50000 : fsize];
		try {
			while (l != -1) {
				int k = 0;
				if (abyte0.length < j + i1) {
					byte abyte1[] = new byte[j + i1];
					System.arraycopy(abyte0, 0, abyte1, 0, j);
					abyte0 = abyte1;
				}
				while (k < i1
					&& (l = is.read(abyte0, j, i1 - k)) != -1) {
					k += l;
					j += l;
					if (fsize > 0 && progress != null) {
						progress[0] = ((800 * j) / fsize + 5) / 10;
						if (progress[0] > 100)
							progress[0] = 100;
						repaint();
					}
				}
			}
			if (abyte0.length > j) {
				byte abyte2[] = new byte[j];
				System.arraycopy(abyte0, 0, abyte2, 0, j);
				abyte0 = abyte2;
			}
		} catch (Exception _ex) {
			return null;
		}
		return abyte0;
	}

	private void m2(byte abyte0[], byte abyte1[]) {
		int i = 0;
		for (int k = 0; i < abyte0.length; k++) {
			if (k >= abyte1.length)
				k = 0;
			abyte0[i] ^= abyte1[k];
			i++;
		}

		int ai[] =
			{
				1,
				20,
				3,
				18,
				0,
				17,
				14,
				11,
				22,
				19,
				2,
				5,
				7,
				6,
				13,
				4,
				21,
				8,
				10,
				9,
				12,
				15,
				16 };
		int i1 = abyte0.length - ai.length;
		byte abyte2[] = new byte[ai.length];
		for (int j = 0; j < i1; j += ai.length) {
			System.arraycopy(abyte0, j, abyte2, 0, ai.length);
			for (int l = 0; l < ai.length; l++)
				abyte0[l + j] = abyte2[ai[l]];

		}

	}

	private void m3(byte abyte0[], String s) {
		if (abyte0 == null || s == null)
			return;
		int i;
		if ((i = s.lastIndexOf('.')) < 0 || i + 1 >= s.length())
			return;
		byte abyte1[] =
			{
				122,
				1,
				12,
				-78,
				-99,
				-33,
				-50,
				17,
				88,
				90,
				-117,
				119,
				30,
				20,
				10,
				33,
				27,
				114,
				121,
				3,
				-11,
				51,
				97,
				-59,
				-32,
				-28,
				0,
				83,
				37,
				43,
				-67,
				17,
				32,
				31,
				70,
				-70,
				-10,
				-39,
				-33,
				2,
				55,
				59,
				-88 };
		if (s.substring(i + 1).equalsIgnoreCase("jpa")) {
			m2(abyte0, abyte1);
			return;
		}
		if (s.substring(i + 1).equalsIgnoreCase("jpb")) {
			byte abyte2[] = m1().getBytes();
			byte abyte4[] = new byte[abyte1.length + abyte2.length];
			System.arraycopy(abyte1, 0, abyte4, 0, abyte1.length);
			System.arraycopy(abyte2, 0, abyte4, abyte1.length, abyte2.length);
			m2(abyte0, abyte4);
			return;
		}
		if (s.substring(i + 1).equalsIgnoreCase("jpc")) {
			byte abyte3[] = getDocumentBase().toString().getBytes();
			byte abyte5[] = new byte[abyte1.length + abyte3.length];
			System.arraycopy(abyte1, 0, abyte5, 0, abyte1.length);
			System.arraycopy(abyte3, 0, abyte5, abyte1.length, abyte3.length);
			m2(abyte0, abyte5);
		}
	}

	void pb_reset() {
		percent[0] = 0;
	}

	void pb_init() {
		percent = new int[1];
		percent[0] = 0;
	}

	void pb_draw(Graphics g, int i, int j) {
		if (pb_x == -1)
			pb_x = i >> 2;
		if (pb_y == -1)
			pb_y = j * 3 >> 2;
		if (pb_width == -1)
			pb_width = i >> 1;
		int k = 0;
		if (percent != null)
			k = percent[0];
		g.setColor(pb_color);
		g.fillRect(pb_x, pb_y, (pb_width * k) / 100, pb_height);
	}

	void shs_init() {
		shotspots = new Vector();
	}

	void shs_setup() {
		if (shotspots.size() > 0) {
			shs_allocate(shotspots.size());
			for (int i = 0; i < numshs; i++)
				ParseStaticHotspotLine((String) shotspots.elementAt(i), i);

		}
	}

	void shs_allocate(int i) {
		try {
			shs_x1 = new int[i];
			shs_x2 = new int[i];
			shs_y1 = new int[i];
			shs_y2 = new int[i];
			shs_url = new String[i];
			shs_target = new String[i];
			shs_him = new Object[i];
			shs_imode = new int[i];
			shs_active = new boolean[i];
			numshs = i;
			return;
		} catch (Exception _ex) {
			numshs = 0;
		}
	}

	void shs_dispose() {
		for (int i = 0; i < numshs; i++)
			if (shs_him[i] != null)
				shs_him[i] = null;

		numshs = 0;
	}

	void ParseStaticHotspotLine(String s, int i) {
		int j = 0;
		int k = s.length();
		StringBuffer stringbuffer = new StringBuffer();
		shs_x1[i] = 0;
		shs_x2[i] = 0;
		shs_y1[i] = 0;
		shs_y2[i] = 0;
		shs_url[i] = null;
		shs_target[i] = null;
		shs_him[i] = null;
		shs_imode[i] = 0;
		shs_active[i] = false;
		while (j < k)
			switch (s.charAt(j++)) {
				case 99 : // 'c'
				case 100 : // 'd'
				case 101 : // 'e'
				case 102 : // 'f'
				case 103 : // 'g'
				case 104 : // 'h'
				case 106 : // 'j'
				case 107 : // 'k'
				case 108 : // 'l'
				case 109 : // 'm'
				case 110 : // 'n'
				case 111 : // 'o'
				case 114 : // 'r'
				case 115 : // 's'
				case 118 : // 'v'
				case 119 : // 'w'
				default :
					break;

				case 120 : // 'x'
					j = getNextWord(j, s, stringbuffer);
					shs_x1[i] = Integer.parseInt(stringbuffer.toString());
					if( shs_x1[i] < 0 )
						shs_x1[i] += (renderer.vwidth == 0 ? getSize().width : renderer.vwidth);
					break;

				case 121 : // 'y'
					j = getNextWord(j, s, stringbuffer);
					shs_y1[i] = Integer.parseInt(stringbuffer.toString());
					if( shs_y1[i] < 0 )
						shs_y1[i] += (renderer.vheight == 0 ? getSize().height : renderer.vheight);
					break;

				case 97 : // 'a'
					j = getNextWord(j, s, stringbuffer);
					shs_x2[i] = Integer.parseInt(stringbuffer.toString());
					if( shs_x2[i] < 0 )
						shs_x2[i] += (renderer.vwidth == 0 ? getSize().width : renderer.vwidth);
					break;

				case 98 : // 'b'
					j = getNextWord(j, s, stringbuffer);
					shs_y2[i] = Integer.parseInt(stringbuffer.toString());
					if( shs_y2[i] < 0 )
						shs_y2[i] += (renderer.vheight == 0 ? getSize().height : renderer.vheight);
					break;

				case 117 : // 'u'
					j = getNextWord(j, s, stringbuffer);
					shs_url[i] = stringbuffer.toString();
					break;

				case 116 : // 't'
					j = getNextWord(j, s, stringbuffer);
					shs_target[i] = stringbuffer.toString();
					break;

				case 112 : // 'p'
					shs_imode[i] = 1;
					break;

				case 113 : // 'q'
					shs_imode[i] = 2;
					break;

				case 105 : // 'i'
					j = getNextWord(j, s, stringbuffer);
					if (stringbuffer.toString().startsWith("ptviewer:")
						|| stringbuffer.toString().startsWith("javascript:"))
						shs_him[i] = stringbuffer.toString();
					else
						shs_him[i] = loadImage(stringbuffer.toString());
					break;
			}
	}

	final void shs_draw(Graphics g) {
		for (int i = 0; i < numshs; i++)
			if (shs_him[i] != null) {
				if (((shs_imode[i] & 2) > 0
					|| shs_active[i]
					&& (shs_imode[i] & 1) > 0)
					&& (shs_him[i] instanceof Image))
					g.drawImage((Image) shs_him[i], shs_x1[i], shs_y1[i], this);
				if ((shs_him[i] instanceof String) && shs_active[i])
					JumpToLink((String) shs_him[i], null);
			}

	}

	final int OverStaticHotspot(int i, int j) {
		int l = -1;
		for (int k = 0; k < numshs; k++)
			if (shs_url[k] != null
				&& i >= shs_x1[k]
				&& i <= shs_x2[k]
				&& (j >= shs_y1[k]
					&& j <= shs_y2[k]
					|| j >= shs_y2[k]
					&& j <= shs_y1[k])) {
				if( shs_imode[k] == 0 && shsEnableVisibleOnly ) {
					shs_active[k] = false;
				}
				else {
					shs_active[k] = true;
					if (k > l)
						l = k;
				}
			} else {
				shs_active[k] = false;
			}

		return l;
	}

//	final int OverStaticHotspot(int i, int j) {
//		int l = -1;
//		for (int k = 0; k < numshs; k++)
//			if (shs_url[k] != null
//				&& i >= shs_x1[k]
//				&& i <= shs_x2[k]
//				&& (j >= shs_y1[k]
//					&& j <= shs_y2[k]
//					|| j >= shs_y2[k]
//					&& j <= shs_y1[k])) {
//				shs_active[k] = true;
//				if (k > l)
//					l = k;
//			} else {
//				shs_active[k] = false;
//			}
//
//		return l;
//	}
//
	
	

	

	
	

	

	

	

	
	

	final int[] math_int_view2pano(
		int i,
		int j,
		int k,
		int l,
		int i1,
		int j1,
		double d,
		double d1,
		double d2) {
		double ad[];
		if ((ad = renderer.math_view2pano(i, j, k, l, i1, j1, d, d1, d2))[0] < 0.0D)
			ad[0] = 0.0D;
		if (ad[0] >= (double) i1)
			ad[0] = i1 - 1;
		if (ad[1] < 0.0D)
			ad[1] = 0.0D;
		if (ad[1] >= (double) j1)
			ad[1] = j1 - 1;
		int ai[];
		(ai = new int[2])[0] = (int) ad[0];
		ai[1] = (int) ad[1];
		return ai;
	}

	

	static final boolean math_odd(int i) {
		int j = i / 2;
		return 2 * j != i;
	}

	void roi_allocate(int i) {
		try {
			roi_im = new String[i];
			roi_xp = new int[i];
			roi_yp = new int[i];
			roi_loaded = new boolean[i];
			roi_yaw = new double[i];
			roi_pitch = new double[i];
			roi_w = new int[i];
			roi_h = new int[i];
			roi_wdeg = new double[i];
			roi_hdeg = new double[i];
			numroi = i;
			return;
		} catch (Exception _ex) {
			numroi = 0;
		}
	}

	void roi_dispose() {
		for (int i = 0; i < numroi; i++)
			roi_im[i] = null;

		roi_im = null;
		roi_xp = null;
		roi_yp = null;
		roi_loaded = null;
		roi_yaw = null;
		roi_pitch = null;
		roi_w = null;
		roi_h = null;
		roi_wdeg = null;
		roi_hdeg = null;
		numroi = 0;
	}

	void ParseROILine(String s, int i) {
		int j = 0;
		int k = s.length();
		StringBuffer stringbuffer = new StringBuffer();
		roi_im[i] = null;
		roi_xp[i] = 0;
		roi_yp[i] = 0;
		roi_w[i] = 0;
		roi_h[i] = 0;
		roi_wdeg[i] = 0;
		roi_hdeg[i] = 0;
		roi_loaded[i] = false;
		while (j < k)
			switch (s.charAt(j++)) {
			case 120: // 'x'
				j = getNextWord(j, s, stringbuffer);
				roi_xp[i] = Integer.parseInt(stringbuffer.toString());
				break;

			case 121: // 'y'
				j = getNextWord(j, s, stringbuffer);
				roi_yp[i] = Integer.parseInt(stringbuffer.toString());
				break;

			case 105: // 'i'
				j = getNextWord(j, s, stringbuffer);
				roi_im[i] = stringbuffer.toString();
				break;

			case 'w': // image width
				j = getNextWord(j, s, stringbuffer);
				roi_w[i] = Integer.parseInt(stringbuffer.toString());
				break;

			case 'h': // image height
				j = getNextWord(j, s, stringbuffer);
				roi_h[i] = Integer.parseInt(stringbuffer.toString());
				break;
			}
	}

	void snd_init() {
		sounds = new Vector();
	}

	void snd_dispose() {
		sounds.removeAllElements();
	}

	public synchronized void PlaySound(int i) {
		if (i < sounds.size()
			&& sounds.elementAt(i) != null
			&& (sounds.elementAt(i) instanceof AudioClip))
			 ((AudioClip) sounds.elementAt(i)).play();
	}

	void SetupSounds() {
		for (int i = 0; i < sounds.size(); i++)
			if (sounds.elementAt(i) != null
				&& (sounds.elementAt(i) instanceof String)) {
				String s = (String) sounds.elementAt(i);
				try {
					URL url = new URL(getDocumentBase(), s);
					sounds.setElementAt(getAudioClip(url), i);
				} catch (Exception _ex) {
					try {
						URL url1 = Class.forName("ptviewer").getResource(s);
						sounds.setElementAt(getAudioClip(url1), i);
					} catch (Exception _ex2) {
						sounds.setElementAt(null, i);
					}
				}
			}

	}

	void app_init() {
		applets = new Hashtable();
		app_properties = new Vector();
	}

	public void startApplet(int i) {
		if (i < 0
			|| app_properties == null
			|| i >= app_properties.size()
			|| app_properties.elementAt(i) == null)
			return;
		if (applets.get(app_properties.elementAt(i)) != null)
			stopApplet(i);
		String s2;
		try {
			String s;
			s2 = myGetParameter((String) app_properties.elementAt(i), "code");
			s = s2;
			Applet applet =
				(Applet) Class
					.forName(s2.substring(0, s.lastIndexOf(".class")))
					.getConstructor(
						new Class[] {
							Class.forName("ptviewer"),
							java.lang.String.class })
					.newInstance(
						new Object[] { this, app_properties.elementAt(i)});
			applets.put(app_properties.elementAt(i), applet);
			applet.init();
			applet.start();
			return;
		} catch (Exception _ex) {
		}
		String s3;
		try {
			String s1;
			s3 = myGetParameter((String) app_properties.elementAt(i), "code");
			s1 = s3;
			Applet applet1 =
				(Applet) Class
					.forName(s3.substring(0, s1.lastIndexOf(".class")))
					.getConstructor(new Class[0])
					.newInstance(new Object[0]);
			applets.put(app_properties.elementAt(i), applet1);
			AppletStub appletstub =
				(AppletStub) Class
					.forName("ptstub")
					.getConstructor(
						new Class[] {
							Class.forName("ptviewer"),
							java.lang.String.class })
					.newInstance(
						new Object[] { this, app_properties.elementAt(i)});
			applet1.setStub(appletstub);
			applet1.init();
			applet1.start();
			return;
		} catch (Exception _ex) {
			return;
		}
	}

	public void stopApplet(int i) {
		if (i < 0
			|| app_properties == null
			|| i >= app_properties.size()
			|| app_properties.elementAt(i) == null)
			return;
		Applet applet;
		if ((applet = (Applet) applets.get(app_properties.elementAt(i)))
			!= null) {
			applet.stop();
			applets.remove(app_properties.elementAt(i));
		}
	}

	void stopApplets(int i) {
		for (int j = i; j < app_properties.size(); j++)
			stopApplet(j);

	}

	void hs_init() {
		hotspots = new Vector();
	}

	void hs_allocate(int i) {
		try {
			renderer.hs_xp = new double[i];
			renderer.hs_yp = new double[i];
			renderer.hs_up = new double[i];
			renderer.hs_vp = new double[i];
			renderer.hs_xv = new int[i];
			renderer.hs_yv = new int[i];
			hs_hc = new Color[i];
			hs_name = new String[i];
			hs_url = new String[i];
			hs_target = new String[i];
			renderer.hs_him = new Object[i];
			renderer.hs_visible = new boolean[i];
			renderer.hs_imode = new int[i];
			hs_mask = new String[i];
			hs_link = new int[i];
			renderer.numhs = i;
			return;
		} catch (Exception _ex) {
			renderer.numhs = 0;
		}
	}

	void hs_dispose() {
		for (int i = 0; i < renderer.numhs; i++) {
			if (renderer.hs_him[i] != null)
				renderer.hs_him[i] = null;
			hs_hc[i] = null;
			hs_name[i] = null;
			hs_url[i] = null;
			hs_target[i] = null;
			hs_mask[i] = null;
		}

		renderer.numhs = 0;
		hotspots.removeAllElements();
		renderer.hs_xp = null;
		renderer.hs_yp = null;
		renderer.hs_up = null;
		renderer.hs_vp = null;
		renderer.hs_xv = null;
		renderer.hs_yv = null;
		hs_hc = null;
		hs_name = null;
		hs_url = null;
		renderer.hs_him = null;
		renderer.hs_visible = null;
		hs_target = null;
		hs_mask = null;
		renderer.hs_imode = null;
		hs_link = null;
		hs_image = null;
	}

	void ParseHotspotLine(String s, int i) {
		int j = 0;
		int k = s.length();
		StringBuffer stringbuffer = new StringBuffer();
		renderer.hs_xp[i] = 0.0D;
		renderer.hs_yp[i] = 0.0D;
		renderer.hs_up[i] = -200D;
		renderer.hs_vp[i] = -200D;
		renderer.hs_xv[i] = 0;
		renderer.hs_yv[i] = 0;
		hs_hc[i] = null;
		hs_name[i] = null;
		hs_url[i] = null;
		hs_target[i] = null;
		renderer.hs_him[i] = null;
		renderer.hs_visible[i] = false;
		renderer.hs_imode[i] = 0;
		hs_mask[i] = null;
		hs_link[i] = -1;
		while (j < k)
			switch (s.charAt(j++)) {
				case 120 : // 'x'
					j = getNextWord(j, s, stringbuffer);
					renderer.hs_xp[i] =
						Double.valueOf(stringbuffer.toString()).doubleValue();
					break;

				case 88 : // 'X'
					j = getNextWord(j, s, stringbuffer);
					renderer.hs_xp[i] =
						-Double.valueOf(stringbuffer.toString()).doubleValue();
					break;

				case 121 : // 'y'
					j = getNextWord(j, s, stringbuffer);
					renderer.hs_yp[i] =
						Double.valueOf(stringbuffer.toString()).doubleValue();
					break;

				case 89 : // 'Y'
					j = getNextWord(j, s, stringbuffer);
					renderer.hs_yp[i] =
						-Double.valueOf(stringbuffer.toString()).doubleValue();
					break;

				case 97 : // 'a'
					j = getNextWord(j, s, stringbuffer);
					renderer.hs_up[i] =
						Double.valueOf(stringbuffer.toString()).doubleValue();
					break;

				case 65 : // 'A'
					j = getNextWord(j, s, stringbuffer);
					renderer.hs_up[i] =
						-Double.valueOf(stringbuffer.toString()).doubleValue();
					break;

				case 98 : // 'b'
					j = getNextWord(j, s, stringbuffer);
					renderer.hs_vp[i] =
						Double.valueOf(stringbuffer.toString()).doubleValue();
					break;

				case 66 : // 'B'
					j = getNextWord(j, s, stringbuffer);
					renderer.hs_vp[i] =
						-Double.valueOf(stringbuffer.toString()).doubleValue();
					break;

				case 99 : // 'c'
					j = getNextWord(j, s, stringbuffer);
					hs_hc[i] =
						new Color(
							Integer.parseInt(stringbuffer.toString(), 16));
					break;

				case 110 : // 'n'
					j = getNextWord(j, s, stringbuffer);
					hs_name[i] = stringbuffer.toString();
					break;

				case 109 : // 'm'
					j = getNextWord(j, s, stringbuffer);
					hs_mask[i] = stringbuffer.toString();
					break;

				case 112 : // 'p'
					renderer.hs_imode[i] |= 1;
					break;

				case 113 : // 'q'
					renderer.hs_imode[i] |= 2;
					break;

				case 119 : // 'w'
					renderer.hs_imode[i] |= 4;
					break;

				case 101 : // 'e'
					renderer.hs_imode[i] |= 0x10;
					break;

				case 117 : // 'u'
					j = getNextWord(j, s, stringbuffer);
					hs_url[i] = stringbuffer.toString();
					break;

				case 105 : // 'i'
					j = getNextWord(j, s, stringbuffer);
					renderer.hs_him[i] = stringbuffer.toString();
					break;

				case 116 : // 't'
					j = getNextWord(j, s, stringbuffer);
					hs_target[i] = stringbuffer.toString();
					break;
			}
	}

	void hs_read() {
		if (hotspots.size() != 0) {
			hs_allocate(hotspots.size());
			for (int i = 0; i < renderer.numhs; i++)
				ParseHotspotLine((String) hotspots.elementAt(i), i);

			hs_setLinkedHotspots();
		}
	}

	//    void hs_setup(int ai[][])
	//    {
	//        int i;
	//        int j;
	//        if(ai == null)
	//            break MISSING_BLOCK_LABEL_1322;
	//        i = ai.length;
	//        j = ai[0].length;
	//        hs_read();
	//        for(int k = 0; k < numhs; k++)
	//        {
	//            String s;
	//            if(hs_him[k] != null && (hs_imode[k] & 0x10) == 0 && !(s = (String)hs_him[k]).startsWith("ptviewer:") && !s.startsWith("javascript:"))
	//                hs_him[k] = loadImage(s);
	//        }
	//
	//        hs_rel2abs(j, i);
	//        if(hs_image != null)
	//            hs_image = loadImage((String)hs_image);
	//        if(hs_image == null || !(hs_image instanceof Image) || j != ((Image)hs_image).getWidth(null) || i != ((Image)hs_image).getHeight(null)) goto _L2; else goto _L1
	//_L1:
	//        ptImageToAlpha(ai, (Image)hs_image);
	//          goto _L3
	//_L2:
	//        int l = 0;
	//          goto _L4
	//_L7:
	//        Image image;
	//        Image image2;
	//        if(hs_link[l] != -1)
	//            continue; /* Loop/switch isn't completed */
	//        if(hs_up[l] != -200D && hs_vp[l] != -200D)
	//        {
	//            SetPAlpha((int)hs_xp[l], (int)hs_yp[l], (int)hs_up[l], (int)hs_vp[l], l, ai);
	//            if(hs_up[l] >= hs_xp[l])
	//            {
	//                hs_xp[l] += (hs_up[l] - hs_xp[l]) / 2D;
	//                hs_up[l] = hs_up[l] - hs_xp[l];
	//            } else
	//            {
	//                hs_xp[l] += ((hs_up[l] + (double)j) - hs_xp[l]) / 2D;
	//                hs_up[l] = (hs_up[l] + (double)j) - hs_xp[l];
	//            }
	//            hs_yp[l] = (hs_yp[l] + hs_vp[l]) / 2D;
	//            hs_vp[l] = Math.abs(hs_yp[l] - hs_vp[l]);
	//            continue; /* Loop/switch isn't completed */
	//        }
	//        if((hs_imode[l] & 4) > 0 && hs_him[l] != null && (hs_him[l] instanceof Image) && hs_mask[l] == null)
	//        {
	//            hs_up[l] = ((Image)hs_him[l]).getWidth(null);
	//            hs_vp[l] = ((Image)hs_him[l]).getHeight(null);
	//            SetPAlpha((int)(hs_xp[l] - hs_up[l] / 2D), (int)(hs_yp[l] - hs_vp[l] / 2D), (int)(hs_xp[l] + hs_up[l] / 2D), (int)(hs_yp[l] + hs_vp[l] / 2D), l, ai);
	//            continue; /* Loop/switch isn't completed */
	//        }
	//        if(hs_mask[l] == null)
	//            continue; /* Loop/switch isn't completed */
	//        image2 = loadImage(hs_mask[l]);
	//        image2;
	//        image = image2;
	//        JVM INSTR ifnull 928;
	//           goto _L5 _L6
	//_L5:
	//        break MISSING_BLOCK_LABEL_665;
	//_L6:
	//        continue; /* Loop/switch isn't completed */
	//        int ai1[] = new int[image.getWidth(null) * image.getHeight(null)];
	//        PixelGrabber pixelgrabber = new PixelGrabber(image, 0, 0, image.getWidth(null), image.getHeight(null), ai1, 0, image.getWidth(null));
	//        try
	//        {
	//            pixelgrabber.grabPixels();
	//        }
	//        catch(InterruptedException _ex)
	//        {
	//            continue; /* Loop/switch isn't completed */
	//        }
	//        int j2 = (int)hs_yp[l];
	//        int j3 = (l << 24) + 0xffffff;
	//        int l3 = 0;
	//        for(int l1 = 0; l1 < image.getHeight(null) && j2 < i; j2++)
	//        {
	//            int i2 = l1 * image.getWidth(null);
	//            int k1 = 0;
	//            for(int l2 = (int)hs_xp[l]; k1 < image.getWidth(null) && l2 < j; l2++)
	//            {
	//                if((ai1[i2 + k1] & 0xffffff) == 0xffffff)
	//                {
	//                    ai[j2][l2] &= j3;
	//                    l3++;
	//                }
	//                k1++;
	//            }
	//
	//            l1++;
	//        }
	//
	//        hs_yp[l] += image.getHeight(null) >> 1;
	//        hs_xp[l] += image.getWidth(null) >> 1;
	//        hs_up[l] = image.getWidth(null);
	//        hs_vp[l] = image.getHeight(null);
	//        l++;
	//_L4:
	//        if(l < numhs && l < 255) goto _L7; else goto _L3
	//_L3:
	//        int j4;
	//        for(int i1 = 0; i1 < numhs; i1++)
	//            if(hs_link[i1] != -1)
	//            {
	//                hs_xp[i1] = hs_xp[hs_link[i1]];
	//                hs_yp[i1] = hs_yp[hs_link[i1]];
	//                hs_up[i1] = hs_up[hs_link[i1]];
	//                hs_vp[i1] = hs_vp[hs_link[i1]];
	//            }
	//
	//        for(int j1 = 0; j1 < numhs; j1++)
	//        {
	//            if((hs_imode[j1] & 4) <= 0 || hs_him[j1] == null || !(hs_him[j1] instanceof Image))
	//                continue;
	//            Image image1;
	//            int k2 = (image1 = (Image)hs_him[j1]).getWidth(null);
	//            int i3 = image1.getHeight(null);
	//            int k3 = (int)hs_xp[j1] - (k2 >> 1);
	//            int i4 = (int)hs_yp[j1] - (i3 >> 1);
	//            if(k3 >= 0 && i4 >= 0 && k2 + k3 <= j && i3 + i4 <= i)
	//            {
	//                j4 = k2 * i3;
	//                int ai2[] = new int[j4 + j4];
	//                PixelGrabber pixelgrabber1 = new PixelGrabber(image1, 0, 0, k2, i3, ai2, 0, k2);
	//                try
	//                {
	//                    pixelgrabber1.grabPixels();
	//                }
	//                catch(InterruptedException _ex)
	//                {
	//                    continue;
	//                }
	//                im_extractRect(ai, k3, i4, ai2, k2, 0, i3, k2, i3);
	//                hs_him[j1] = ai2;
	//                hs_up[j1] = k2;
	//                hs_vp[j1] = i3;
	//            } else
	//            {
	//                System.out.println("Image for Hotspot No " + j1 + " outside main panorama");
	//            }
	//        }
	//
	//    }

	// from version 2.1
	void hs_setup(int[][] pd) {
		if (pd == null) {
			return;
		}
		int ph = pd.length, pw = pd[0].length;
		PixelGrabber pg;
		int i, x, y, cy;

		hs_read();
		
		int[] tdata;

		// Load Hotspotimages, if not done

		for (i = 0; i < renderer.numhs; i++) {
			if (renderer.hs_him[i] != null && ((renderer.hs_imode[i] & IMODE_TEXT) == 0)) {
				String s = (String) renderer.hs_him[i];

				if (!(s.startsWith("ptviewer:")
					|| s.startsWith("javascript:"))) {
					renderer.hs_him[i] = loadImage(s);
				}
			}
		}

		renderer.hs_rel2abs(pw, ph);
		
		System.out.println("wat fuck: "+hs_image);

		// Process global hotspot image

		if (hs_image != null) {
			hs_image = loadImage((String) hs_image);
		}
		if (hs_image != null && hs_image instanceof Image && pw == ((Image) hs_image).getWidth(null) && ph == ((Image) hs_image).getHeight(null)) {
			ptImageToAlpha(pd, (Image) hs_image);
		} else {
			// Set hotspot masks

			for (i = 0; i < renderer.numhs && i < 255; i++) { // only 255 indices
				if (hs_link[i] == -1) { // Linked Hotspots don't get masks
					if (renderer.hs_up[i] != NO_UV && renderer.hs_vp[i] != NO_UV) {
						Utils.SetPAlpha(
							(int) renderer.hs_xp[i],
							(int) renderer.hs_yp[i],
							(int) renderer.hs_up[i],
							(int) renderer.hs_vp[i],
							i,
							pd);
						if (renderer.hs_up[i] >= renderer.hs_xp[i]) {
							renderer.hs_xp[i] += (renderer.hs_up[i] - renderer.hs_xp[i]) / 2;
							renderer.hs_up[i] = renderer.hs_up[i] - renderer.hs_xp[i];
						} else {
							renderer.hs_xp[i] += (renderer.hs_up[i] + pw - renderer.hs_xp[i]) / 2;
							renderer.hs_up[i] = renderer.hs_up[i] + pw - renderer.hs_xp[i];
						}
						renderer.hs_yp[i] = (renderer.hs_yp[i] + renderer.hs_vp[i]) / 2;
						renderer.hs_vp[i] = Math.abs(renderer.hs_yp[i] - renderer.hs_vp[i]);
					} else if (
						(renderer.hs_imode[i] & IMODE_WARP) > 0
							&& (renderer.hs_him[i] != null)
							&& renderer.hs_him[i] instanceof Image
							&& hs_mask[i] == null) { // warped image without mask
						renderer.hs_up[i] = ((Image) renderer.hs_him[i]).getWidth(null);
						renderer.hs_vp[i] = ((Image) renderer.hs_him[i]).getHeight(null);
						Utils.SetPAlpha(
							(int) (renderer.hs_xp[i] - renderer.hs_up[i] / 2.0),
							(int) (renderer.hs_yp[i] - renderer.hs_vp[i] / 2.0),
							(int) (renderer.hs_xp[i] + renderer.hs_up[i] / 2.0),
							(int) (renderer.hs_yp[i] + renderer.hs_vp[i] / 2.0),
							i,
							pd);
					} else if (hs_mask[i] != null) {

						Image mim = loadImage(hs_mask[i]);
						if (mim != null) {
							tdata = new int[mim.getWidth(null) * mim.getHeight(null)];
							pg = new PixelGrabber(mim,
									0,0,mim.getWidth(null),mim.getHeight(null),tdata,
									0,
									mim.getWidth(null));
							try {
								pg.grabPixels();
							} catch (InterruptedException e) {
								continue;
							}

							int hs_y = (int) renderer.hs_yp[i], hs_x = (int) renderer.hs_xp[i];
							int hmask = (i << 24) + 0x00ffffff;
							int k = 0;

							for (y = 0;
								y < mim.getHeight(null) && hs_y < ph;
								y++, hs_y++) {
								cy = y * mim.getWidth(null);
								for (x = 0, hs_x = (int) renderer.hs_xp[i];
									x < mim.getWidth(null) && hs_x < pw;
									x++, hs_x++) {
									if ((tdata[cy + x] & 0x00ffffff)
										== 0x00ffffff) {
										// inside mask
										pd[hs_y][hs_x] &= hmask;
										k++;
									}
								}
							}
							renderer.hs_yp[i] += mim.getHeight(null) / 2;
							renderer.hs_xp[i] += mim.getWidth(null) / 2;
							renderer.hs_up[i] = mim.getWidth(null); // width
							renderer.hs_vp[i] = mim.getHeight(null); // height
							mim = null;
							tdata = null;
						}
					}
				}
			}
		}

		for (i = 0; i < renderer.numhs; i++) {
			if (hs_link[i] != -1) {
				renderer.hs_xp[i] = renderer.hs_xp[hs_link[i]];
				renderer.hs_yp[i] = renderer.hs_yp[hs_link[i]];
				renderer.hs_up[i] = renderer.hs_up[hs_link[i]];
				renderer.hs_vp[i] = renderer.hs_vp[hs_link[i]];
			}
		}

		// Get and set pixel data for warped hotspots

		for (i = 0; i < renderer.numhs; i++) {
			if ((renderer.hs_imode[i] & IMODE_WARP) > 0 && renderer.hs_him[i] != null) {
				if (renderer.hs_him[i] instanceof Image) {
					Image p = (Image) renderer.hs_him[i];

					int w = p.getWidth(null);
					int h = p.getHeight(null);
					int xp = (int) renderer.hs_xp[i] - w / 2;
					int yp = (int) renderer.hs_yp[i] - h / 2;

					// System.out.println( xp + " " +yp + " " +w+" "+h);

					if (xp >= 0 && yp >= 0 && w + xp <= pw && h + yp <= ph) {
						int[] buf = new int[w * h * 2];
						pg = new PixelGrabber(p, 0, 0, w, h, buf, 0, w);
						try {
							pg.grabPixels();
						} catch (InterruptedException e) {
							continue;
						}

						im_extractRect(pd, xp, yp, buf, w, 0, h, w, h);
						renderer.hs_him[i] = buf;
						renderer.hs_up[i] = w;
						renderer.hs_vp[i] = h;
					} else {
						System.out.println(
							"Image for Hotspot No "
								+ i
								+ " outside main panorama");
					}
				}

			}
		}

	}

	boolean hs_drawWarpedImages(int ai[][], int i, boolean flag) {
		boolean flag1 = false;
		if (ai == null)
			return false;
		for (int j = 0; j < renderer.numhs; j++)
			if ((renderer.hs_imode[j] & 4) > 0
				&& renderer.hs_him[j] != null
				&& (renderer.hs_him[j] instanceof int[])) {
				int k = (int) renderer.hs_up[j];
				int l = (int) renderer.hs_vp[j];
				int i1 = (int) renderer.hs_xp[j] - (k >> 1);
				int j1 = (int) renderer.hs_yp[j] - (l >> 1);
				if (flag
					|| (renderer.hs_imode[j] & 2) > 0
					|| j == i
					&& (renderer.hs_imode[j] & 1) > 0
					|| i >= 0
					&& hs_link[j] == i
					&& (renderer.hs_imode[j] & 1) > 0) {
					if ((renderer.hs_imode[j] & 8) == 0) {
						Utils.im_insertRect(
							ai,
							i1,
							j1,
							(int[]) renderer.hs_him[j],
							k,
							0,
							0,
							k,
							l);
						renderer.hs_imode[j] |= 8;
						flag1 = true;
					}
				} else if ((renderer.hs_imode[j] & 8) > 0) {
					Utils.im_insertRect(ai, i1, j1, (int[]) renderer.hs_him[j], k, 0, l, k, l);
					renderer.hs_imode[j] &= -9;
					flag1 = true;
				}
			}

		return flag1;
	}

	

	void hs_draw(
		Graphics g,
		int off_x,
		int off_y,
		int width,
		int height,
		int chs,
		boolean shs) {
		for (int i = 0; i < renderer.numhs; i++)
			if (renderer.hs_visible[i]
				&& (shs
					|| (renderer.hs_imode[i] & 2) > 0
					|| i == chs
					&& (renderer.hs_imode[i] & 1) > 0
					|| chs >= 0
					&& hs_link[i] == chs
					&& (renderer.hs_imode[i] & 1) > 0))
				if (renderer.hs_him[i] == null) {
					if (hs_hc[i] == null)
						g.setColor(Color.red);
					else
						g.setColor(hs_hc[i]);
					g.drawOval(
						(renderer.hs_xv[i] - 10) + off_x,
						(renderer.hs_yv[i] - 10) + off_y,
						20,
						20);
					g.fillOval(
						(renderer.hs_xv[i] - 5) + off_x,
						(renderer.hs_yv[i] - 5) + off_y,
						10,
						10);
				} else if (renderer.hs_him[i] instanceof Image) {
					Image image = (Image) renderer.hs_him[i];
					g.drawImage(
						image,
						(renderer.hs_xv[i] - (image.getWidth(null) >> 1)) + off_x,
						(renderer.hs_yv[i] - (image.getHeight(null) >> 1)) + off_y,
						this);
				} else if (
					(renderer.hs_imode[i] & 0x10) > 0
						&& (renderer.hs_him[i] instanceof String)) {
					String s = (String) renderer.hs_him[i];
					Dimension dimension = string_textWindowSize(g, s);
					if (renderer.hs_xv[i] >= 0
						&& renderer.hs_xv[i] < width
						&& renderer.hs_yv[i] >= 0
						&& renderer.hs_yv[i] < height) {
						int k1 = 0;
						int l1 = 0;
						byte byte0 = 0;
						if (renderer.hs_xv[i] + dimension.width < width) {
							if (renderer.hs_yv[i] - dimension.height > 0) {
								k1 = renderer.hs_xv[i];
								l1 = renderer.hs_yv[i] - dimension.height;
								byte0 = 1;
							} else if (renderer.hs_yv[i] + dimension.height < width) {
								k1 = renderer.hs_xv[i];
								l1 = renderer.hs_yv[i];
								byte0 = 2;
							}
						} else if (renderer.hs_xv[i] - dimension.width >= 0)
							if (renderer.hs_yv[i] - dimension.height > 0) {
								k1 = renderer.hs_xv[i] - dimension.width;
								l1 = renderer.hs_yv[i] - dimension.height;
								byte0 = 3;
							} else if (renderer.hs_yv[i] + dimension.height < width) {
								k1 = renderer.hs_xv[i] - dimension.width;
								l1 = renderer.hs_yv[i];
								byte0 = 4;
							}
						if (byte0 != 0)
							string_drawTextWindow(
								g,
								k1 + off_x,
								l1 + off_y,
								dimension,
								hs_hc[i],
								s,
								byte0);
					}
				}

	}

	final void hs_exec_popup(int i) {
		for (int j = 0; j < renderer.numhs; j++)
			if (renderer.hs_visible[j]
				&& renderer.hs_him[j] != null
				&& (j == i || i >= 0 && hs_link[j] == i)
				&& (renderer.hs_him[j] instanceof String)
				&& (renderer.hs_imode[j] & 0x10) == 0)
				JumpToLink((String) renderer.hs_him[j], null);

	}

	final void hs_setLinkedHotspots() {
		for (int i = 0; i < renderer.numhs; i++) {
			for (int j = i + 1; j < renderer.numhs; j++)
				if (renderer.hs_xp[i] == renderer.hs_xp[j]
					&& renderer.hs_yp[i] == renderer.hs_yp[j]
					&& hs_link[i] == -1)
					hs_link[j] = i;

		}

	}

	

	static final boolean debug = false;
	static final double HFOV_MIN = 10.5D;
	static final double HFOV_MAX = 165D;
	static final long TIME_PER_FRAME = 10L;
	static final long ETERNITY = 0x5f5e100L;
	int quality;
	boolean inited;
	Color bgcolor;
	long waittime;
	boolean WaitDisplayed;
	Image view;
	Image dwait;
	Image frame;
	Image offImage;
	Graphics offGraphics;
	int offwidth;
	int offheight;
	
	int awidth;
	int aheight;
	
	boolean vset;
	int vx;
	int vy;
	
	
	
	
	boolean show_pdata;
	boolean ready;
	boolean hsready;
	boolean PanoIsLoaded;
	boolean fatal;
	boolean mouseInWindow;
	boolean mouseInViewer;
	boolean panning;
	boolean keyPanning;		// true if we are panning or zooming with the keyboard
	
	boolean showhs;
	boolean showCoordinates;
	int oldx;
	int oldy;
	int newx;
	int newy;
	int ptcursor;
	
	
	
	
	

	
	
	double MASS;
	double oldspeedx;
	double oldspeedy;
	
	double autotilt;
	
	// time interval in milliseconds between frames when autopanning
	// if == 0 it is ignored
	double autopanFrameTime;

	// E.Gigi - 2005.06.12
	//   number of 360 degree turns before auto-panning stops (fractions are allowed)
	//   ignored if 0
	double autoNumTurns;
	
	double zoom;
	public double pan_steps;
	String filename;
	String inits;
	String MouseOverHS;
	String GetView;
	int click_x;
	int click_y;
	
	
	long ptimer;
	Thread loadPano;
	Thread ptviewerScript;
	String PTScript;
	String PTViewer_Properties;
	boolean loadAllRoi;
	int CurrentPano;
	Hashtable sender;
	Thread preloadthread;
	String preload;
	String order;
	
	
	
	int im_maxarray;
	int grid_bgcolor;
	int grid_fgcolor;
	Hashtable file_Cache;
	boolean file_cachefiles;
	Color pb_color;
	int pb_x;
	int pb_y;
	int pb_width;
	int pb_height;
	int percent[];
	int numshs;
	int curshs;
	int shs_x1[];
	int shs_x2[];
	int shs_y1[];
	int shs_y2[];
	String shs_url[];
	String shs_target[];
	Object shs_him[];
	boolean shs_active[];
	int shs_imode[];		// 0 - normal, 1 - popup, 2 - always visible
	Vector shotspots;
	

	
	
	
	// a message to be written in the browser's status bar
	protected String statusMessage; 
	
	// true if we can use an accelerated VolatileImage
	private boolean useVolatileImage;
	// back buffer used to draw the panorama frames
	public Image backBuffer;
	// vimage object used to handle accelerated graphics
	vimage vImgObj;
	
	// if true shows the toolbar
	boolean showToolbar;
	
	// image used to draw the toolbar
	String tlbImageName;
	
	// toolbar object
	Object tlbObj;
	boolean onlyPaintToolbar = false;
	
	// original value if view_height set as a parameter
	// needed because the toolbar changes the value of vheight
	int org_vheight;
	
	// false if we don't want img loading feedback
	protected boolean imgLoadFeedback;
	
	// link to an URL to open in case of out of memory error while loading the pano
	String outOfMemoryURL;
	
	
	
	
	int numroi;
	String roi_im[];
	int roi_xp[];
	int roi_yp[];
	boolean roi_loaded[];

	// true if we are using a custom format pano file (see PTVFile.java)
	boolean usingCustomFile;
	// object that handles the custom file format
	PTVFile ptvf;
	
	// yaw angle of the center of each ROI image
	// used only if the applet is created with the "dynLoadROIs" parameter
	double roi_yaw[];
	// pitch angle of the center of each ROI image
	double roi_pitch[];
	// size of each ROI image
	int roi_w[], roi_h[];
	// width in degrees of each ROI image
	double roi_wdeg[];
	// height in degrees of each ROI image
	double roi_hdeg[];
	boolean dynLoadROIs;	// true if the applet is created with the "dynLoadROIs" parameter
	boolean loadingROI;	// true while we are loading a ROI - used to avoid a lot of unnecessary paints
	// set to true at the end of paint(). Used for synchronization while dynamically loading ROIs
	boolean paintDone = true;
	// last position of the mouse cursor
	int lastMouseX, lastMouseY;
	// changes sensitivity to mouse panning: 
	// default 1.0 : no change
	// values < 1 : slower panning
	// values > 1 : faster panning
	double mouseSensitivity = 1.0;
	// only used for quality=6, default = 1.0
	// values > 1 will require a larger mouse movement to switch from bil to nn
	// values < 1 will require a smaller mouse movement to switch from bil to nn
	double mouseQ6Threshold = 1.0;
	
	// if set to true the next paint will be forced to use the bilinear interpolator 
	// instead of Lanczos2. Used to speed up dynLoading ROIs
	boolean forceBilIntepolator = false;
	
	// if false do not show the hotspots' description in the status bar
	boolean hsShowDescrInStatusBar;
	
	boolean hsEnableVisibleOnly;	// if true hotspots will be enabled only if visible
	boolean shsEnableVisibleOnly;	// if true static hotspots will be enabled only if visible
	boolean shsStopAutoPanOnClick;	// if false the autopan will not stop when clicking a static hotspot
	
	boolean popupPanning;			// if true hotspots will pop up also when panning with the mouse
	
	long lastPanningPaintTime = -1;	// time taken by the last paint while panning
	double mousePanTime = 0;		// minimum time for a full revolution, ignored if == 0
	
	
	
	// true if we want to use the applet in authoring mode
	// it enables the "o" key
	boolean authoringMode;
	
	Vector sounds;
	Hashtable applets;
	Vector app_properties;
	Vector hotspots;
	
	int curhs;
	Object hs_image;
	
	
	Color hs_hc[];
	String hs_name[];
	String hs_url[];
	String hs_target[];
	
	String hs_mask[];
	
	
	int hs_link[];
	static final double NO_UV = -200D;
	static final int HSIZE = 12;
	static final int IMODE_NORMAL = 0;
	static final int IMODE_POPUP = 1;
	static final int IMODE_ALWAYS = 2;
	static final int IMODE_WARP = 4;
	static final int IMODE_WHS = 8;
	static final int IMODE_TEXT = 16;

	

	

}
