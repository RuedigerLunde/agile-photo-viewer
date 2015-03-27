= Comments on Architecture =

The architecture of the AgilePhotoViewer is dominated by the model-view-controller
pattern. Class 'AgilePhotoViewerApp' contains the static main method which creates
the application object. The application consists of three main components. The model
component defines the application state, the view component is responsible for state
visualisation, and the controller component for translating user events into
operations on model state.

The classes to create the components are located in three packages called 'model',
'view', and 'controller'. Within those packages, the classes 'PVModel', 'PVView',
and 'PVController' are responsible for creating the concrete components. Especially
'PVModel' is important because it serves as facade for the two GUI-related components. 