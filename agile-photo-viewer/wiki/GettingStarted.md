# Getting Started #


<h1>Agile Photo Viewer</h1>
<h2>Version 2.0 (18-Januar-2016)</h2>

<p>This is the lightweight pure-java Agile Photo Viewer.
It provides the following features:
<ul>
<li>Show picture locations in map image (haven't seen this feature elsewhere...)</li>
<li>Display Exif-Tags, especially caption, rating, date, exposure, vocal length, iso, camera model,
position, and keywords (using the library of Drew Noakes, see http://drewnoakes.com/code/exif/)</li>
<li>Control picture visibility by rating and keyword (with flexible positive and negative 
selection criteria)</li>
<li>Select picture by map position</li>
<li>Sort pictures by name and date</li>
<li>Export visible pictures with new indexed names preserving chosen order
(useful when using different cameras)</li>
<li>Support undecorated mode (more flexible than full-screen mode)</li>
<li>Support presenter mouse</li>
</ul>
</p>

<p><b>To install</b>, just copy the executable jar on your hard disk drive.
Make sure that your Java is up to date. By default, the FX Version of the viewer
is started. Additionally, two starter files (cmd-files) are available, which
allow to choose between FX and Swing Version. Each of them starts the viewer
with additional heap space from a shell showing error messages if any.</p>

<p><b>To start</b>, double-click the jar or cmd file, press 'Select' and select a
picture file in the file chooser. You can zoom
into the picture by mouse-wheel and pan with mouse-drag. Check sharpness with mouse-middle.
The viewer is directory-based. You can move back and forth with the
control panel buttons, keys 'n' and 'p', or the mouse wheel (focus in control pane). 
If you don't want to see all pictures
in the current directory, specify a rating filter or a keyword expression in the
Visibility Panel. Select what you want to see and what you don't using the 'Not'-Button.
The Info Panel shows you some metadata about the current picture. Text size for caption
display can be adjusted via the Control Panel context menu or keys plus and minus.
Lightroom and Picasa are suitable tools to add captions
and keywords ('poster', 'private', 'people', 'historic', ...) to your pictures.
When exiting the application, your personal settings are saved in your home directory
('.agilephotoviewer').</p>

<p><b>To export pictures</b> via the context menu of the control panel,
you can either just specify the destination directory (no renaming) or provide also
a file name like '2012-Scotland-0005'. The first copied picture will
then be named '2012-Scotland-0006.jpg' if it is a jpg file.</p>

<p><b>To use the Map View</b>, try the following:
<ul>
<li>Navigate to a picture showing a map (e.g. a photo of a map or a screenshot of
Google Earth possibly including track data) and press 'Use as Map' in the
context menu of the Photo View (Swing Version: Context menu of Control Panel).</li>
<li>Navigate to a (correctly!) geotagged picture and mark its position on the
map with 'Add Reference Point' (context menu of the Map View). Repeat with
another picture. Now a red marker should follow the locations of the pictures shown
in the Photo View. Accuracy improves when at least three reference points
(placed in a triangle) are set. Reference points can be removed by
'Remove Reference Point' (mouse-right on the reference point's position).
By mouse-left on the map you load the picture next to the current
mouse position into the Photo View.</li>
<li>Previously prepared map images can be opened using the context menu of the
Map View.</li>
</ul>
</p>


<p>Agile Photo Viewer is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.</p>

<p>Agile Photo Viewer is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.</p>

<p>You should have received a copy of the GNU General Public License
    along with Agile Photo Viewer.  If not, see http://www.gnu.org/licenses.</p>


<p>Have fun,<br/>Ruediger Lunde</p>


<p>If you are a Java developer, you find in NotesForDevelopers some comments on the
architecture. It should be quite easy to change and extend the software.</p>
