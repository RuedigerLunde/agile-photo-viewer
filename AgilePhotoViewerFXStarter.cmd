rem allocates heap space, starts the Agile Photo Viewer (FX Version) and makes error messages visible

"C:\Program Files\Java\jdk-11.0.14.1+1\bin\java" --module-path "C:\Program Files\Java\javafx-sdk-18.0.1\lib" --add-modules javafx.controls,javafx.fxml -Xmx1500M -jar AgilePhotoViewerApp.jar

timeout /t 1