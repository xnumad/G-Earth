package gearth.ui.extensions;

import gearth.services.extensionhandler.ExtensionConnectedListener;
import gearth.services.extensionhandler.ExtensionHandler;
import gearth.services.extensionhandler.extensions.ExtensionListener;
import gearth.services.extensionhandler.extensions.GEarthExtension;
import gearth.services.extensionhandler.extensions.implementations.network.NetworkExtensionsProducer;
import gearth.services.extensionhandler.extensions.implementations.network.executer.ExecutionInfo;
import gearth.services.extensionhandler.extensions.implementations.network.executer.ExtensionRunner;
import gearth.services.extensionhandler.extensions.implementations.network.executer.ExtensionRunnerFactory;
import gearth.ui.SubForm;
import gearth.ui.extensions.logger.ExtensionLogger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;

/**
 * Created by Jonas on 06/04/18.
 */

public class ExtensionsController extends SubForm {


    public Button btn_install;
    public Button btn_remove;
    public TextField ext_port;
    public VBox extensioncontainer;
    public GridPane header_ext;
    public ScrollPane scroller;
    public Button btn_viewExtensionConsole;

    private ExtensionRunner extensionRunner = null;
    private ExtensionHandler extensionHandler;
    private NetworkExtensionsProducer networkExtensionsProducer; // needed for port
    private ExtensionLogger extensionLogger = null;




    public void initialize() {
        scroller.widthProperty().addListener(observable -> header_ext.setPrefWidth(scroller.getWidth()));
        extensionLogger = new ExtensionLogger();
    }

    protected void onParentSet() {
        ExtensionItemContainerProducer producer = new ExtensionItemContainerProducer(extensioncontainer, scroller);
        extensionHandler = new ExtensionHandler(getHConnection());
        extensionHandler.getObservable().addListener((e -> {
            Platform.runLater(() -> producer.extensionConnected(e));
        }));

        //noinspection OptionalGetWithoutIsPresent
        networkExtensionsProducer
                = (NetworkExtensionsProducer) extensionHandler.getExtensionProducers().stream()
                .filter(producer1 -> producer1 instanceof NetworkExtensionsProducer)
                .findFirst().get();


        producer.setPort(networkExtensionsProducer.getPort());
        ext_port.setText(networkExtensionsProducer.getPort()+"");
//        System.out.println("Extension server registered on port: " + extensionsRegistrer.getPort());

        extensionRunner = ExtensionRunnerFactory.get();
        extensionRunner.runAllExtensions(networkExtensionsProducer.getPort());


        extensionHandler.getObservable().addListener(e -> e.getExtensionObservable().addListener(new ExtensionListener() {
            @Override
            protected void log(String text) {
                extensionLogger.log(text);
            }
        }));
    }


    public void installBtnClicked(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Install extension");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("G-Earth extensions", ExecutionInfo.ALLOWEDEXTENSIONTYPES));
        File selectedFile = fileChooser.showOpenDialog(parentController.getStage());
        if (selectedFile != null) {
            extensionRunner.installAndRunExtension(selectedFile.getPath(), networkExtensionsProducer.getPort());
        }
    }

    public void extConsoleBtnClicked(ActionEvent actionEvent) {
        if (!extensionLogger.isVisible()) {
            extensionLogger.show();
        }
        else {
            extensionLogger.hide();
        }

    }
}
