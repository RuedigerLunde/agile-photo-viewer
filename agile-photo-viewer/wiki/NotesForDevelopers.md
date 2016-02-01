# Comments on Architecture #

The logical architecture of the AgilePhotoViewer comprises three layers. The base layer (package 'rl.util') provides general services for persistent property management and
error handling. The second layer (package 'r.photoviewer.model') provides specific 
functionality for reading exif-data from photo files and managing photo and map data.
Class 'PVModel' serves as facade for access from higher layer components.
The third layer comprises two partitions which provide two independent GUIs for the application.

The older GUI (package 'rl.photoviewer.swing') relies on Swing as
graphical framework and is dominated by the model-view-controller
pattern. Class 'AgilePhotoViewerApp' contains the static main method which creates
the application object. The application consists of three main components. The model
component defines the application state, the view component is responsible for state
visualisation, and the controller component for translating user events into
operations on model state.

The newer GUI (package 'rl.photoviewer.fx.view') relies on JavaFX. The architecture
is inspired by the model-view-presenter pattern. Again, class 'AgilePhotoViewerApp' contains the
static main method which creates the application object. The view layout is defined
by an XML file which was produced with the 'SceneBuilder' application
(file 'AgilePhotoViewer.fxml'). A cascading style sheet (file 'AgilePhotoViewer.css') is used to
draw the standard GUI components using a night theme. A presenter component
(class AgilePhotoViewerController) mediates between view and model.
