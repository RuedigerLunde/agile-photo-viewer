rem allocates heap space, starts the Agile Photo Viewer (FX Version) and makes error messages visible

"C:\Program Files\Java\jdk-15.0.2\bin\java" --module-path "C:\Program Files\Java\javafx-sdk-15.0.1\lib" --add-modules javafx.controls,javafx.fxml -Xmx1500M -jar AgilePhotoViewerApp.jar

timeout /t 5