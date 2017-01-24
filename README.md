A Java API for projecting panoramic images.

# Software Rendered
All of the calculations and projections are done in pure software, no dependencies to any native graphical libraries. (e.g OpenGL)

# Standalone
JPanoramaAPI is completely standalone. The representation and manipulation of the image data is handled with Integer arrays, making it very easy to port to other languages as there are no dependencies to any language specific image representations (e.g java.awt.Graphics)


# Based on PTViewer
JPanoramaAPI is an adaption and rework of [PTViewer 2.8](http://www.fsoft.it/panorama/PTViewer.htm), which is an open source Java applet for viewing Panoramic Images.  The first step was to strip away all of its heavily intertwined viewer and controller code from the bare logic, leaving behind a standalone API purely for calculating the projection of panoramic images, allowing the user to then display or control the panorama however they wish to.

The next step was to then clean, refactor and rework what was left. 
A lot of the existing PTViewer code is undocumented, lacks object-orientation and is very difficult to follow.  This makes it extremely challenging for users to adapt the viewer to their needs.  This project turns PTViewer into a user friendly, light-weight, standalone Java API to handle the maths and logic involved to project panoramic images.
