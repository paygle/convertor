package com.dodar;

import com.dodar.compiler.MemoryJavaCompiler;
import com.dodar.utils.Eval;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.Map;

import static com.dodar.utils.Log.logger;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 1080, 560);
        scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
        primaryStage.getIcons().add(new Image(MainApp.class.getResourceAsStream("/icon.png")));
        primaryStage.setResizable(false);
        primaryStage.setTitle("File to SQLite Tool");
        primaryStage.setScene(scene);
        // 传递 primaryStage 参数给 AppControl
        AppControl appControl = fxmlLoader.getController();
        appControl.setStage(primaryStage);

        primaryStage.show();
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                logger.info("主程序窗口关闭");
                appControl.closeConnection();
            }
        });
        // 测试编译
//        Eval.main(new String[]{"s"});
        final String source = "public class Hello {\n"
                + "public static String greeting(String name) {\n"
                + "\treturn \"Hello \" + name;\n" + "}\n}\n";
        MemoryJavaCompiler javaCompiler = new MemoryJavaCompiler();
        Class clazz = javaCompiler.getClass("Hello", source);

        if (clazz != null) {

        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
