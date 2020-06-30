/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package deskspace;

import com.jfoenix.controls.JFXColorPicker;
import com.jfoenix.controls.JFXSlider;
import static deskspace.DeskSpace.cn;
import static deskspace.DeskSpace.jarPath;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import static java.util.concurrent.TimeUnit.SECONDS;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.binding.Bindings;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javax.imageio.ImageIO;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Scale;

/**
 *
 * @author mrbri
 */

public class FXMLDocumentController implements Initializable {
    
    @FXML
    private GridPane grid;
    @FXML
    private BorderPane parent;
    @FXML
    private ScrollPane scrollPane;
    @FXML 
    private JFXColorPicker colorPicker;
    
    private ArrayList<LocationPane> updatePaneList = new ArrayList<>();
    
    private ResultSet rs;
    
    private final ScheduledExecutorService scheduler =
       Executors.newScheduledThreadPool(1);
    
    private WindowsDesktop updateWDesktop = new WindowsDesktop();
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            initGrid();
            startBackground();
            
            JFXSlider slider = new JFXSlider(0.3,2,1);

            slider.setOrientation(Orientation.VERTICAL);
            slider.setStyle(jarPath);
            
            ZoomingPane zoomingPane = new ZoomingPane(scrollPane);
            zoomingPane.zoomFactorProperty().bind(slider.valueProperty());
            
            grid.addEventFilter(MouseEvent.DRAG_DETECTED , new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    grid.startFullDrag();
                }
            });
            
            parent.setCenter(zoomingPane);
            parent.setLeft(slider);
        } catch (SQLException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void initGrid() throws SQLException{
        
        colorPicker.setValue(Color.BLACK);
        try{       
            String sql = "SELECT Color, Location FROM ds_grid";
            Statement stmt = cn.createStatement();
            rs = stmt.executeQuery(sql);
        } catch(Exception e){
            rs = null;
        }
        for(int i = 0; i < 100; i++){
            ColumnConstraints column = new ColumnConstraints(30);
            RowConstraints row = new RowConstraints(30);
            grid.getColumnConstraints().add(column);
            grid.getRowConstraints().add(row);
            
            //150x100
            if(i%2 == 1) grid.getColumnConstraints().add(column);
        }
        
        int row = 0, column = 0;
        //rs.next();
        do{
            LocationPane tmpPane = new LocationPane();
            tmpPane.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));
            //Label tmpLbl = new Label();
            //tmpLbl.setText(rs.getString("Location"));
            
            try{
                //String color = rs.getString("Color");
                String color = "black";
                tmpPane.setStyle("-fx-background-color: " + color);
                
                tmpPane.setOnMousePressed(new EventHandler<MouseEvent>(){
                        @Override
                        public void handle(MouseEvent t) {
                            UpdateSquare tmpUpdater = new UpdateSquare();
                            tmpUpdater.setPane(tmpPane);
                            Thread tempThread = new Thread(tmpUpdater);
                            tempThread.start();
                            
                        }
                });
                tmpPane.setOnMouseDragEntered(new EventHandler<MouseEvent>(){
                        @Override
                        public void handle(MouseEvent t) {
                            UpdateSquare tmpUpdater = new UpdateSquare();
                            tmpUpdater.setPane(tmpPane);
                            Thread tmp = new Thread(tmpUpdater);
                            tmp.start();
                        }
                });
                tmpPane.setStyle("-fx-background-color: black;");

            }catch ( Exception e){
                e.printStackTrace();
                tmpPane.setStyle("-fx-background-color: black;");
            }

            tmpPane.setRow(row);
            tmpPane.setColumn(column);
            grid.add(tmpPane, column, row);
            //grid.add(tmpLbl, column, row);
            
            if(column == 149) {
                row++;
                column = 0;
            } else {
                column ++;
            }
        } while (row < 150);
    }
    
    private int getLocation(int row, int column){
        return (row * 150) + column+1; //+1 for indexing
    }
    
    private void exportImage(){
        //Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        //int width = Integer.parseInt(String.valueOf(screenSize.getWidth()));
        //int height = Integer.parseInt(String.valueOf(screenSize.getHeight()));
        WritableImage wImage = new WritableImage(1500,1000);
        PixelWriter pixelWriter = wImage.getPixelWriter();
        
        try{
            rs.beforeFirst();
            int row = 0, column = 0;
            while(rs.next()){
                Color tmpColor = Color.color(0,0,0);
                try{
                    tmpColor = Color.web(String.valueOf(rs.getString("Color")));
                }catch ( Exception e){
                    System.out.println("Color empty wtf happened?");
                }
                for(int i = 0; i < 10; i++){
                    for(int j = 0; j < 10; j++){
                        pixelWriter.setColor(column + j, row + i, tmpColor);
                    }
                }

                if(column == 1490) {
                    row = row + 10;
                    column = 0;
                } else {
                    column = column + 10;
                }
            }
            File image = new File(jarPath + "\\DeskSpace.png");
            RenderedImage renderedImage = SwingFXUtils.fromFXImage(wImage, null);
            try {
                ImageIO.write(renderedImage, "png", image);
            } catch (IOException ex) {
                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }catch(Exception e){
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
    public void updateGrid(){
        try{
            int row = 0, column = 0;
            rs.beforeFirst();
            rs.next();
            do{
                LocationPane tmpPane = (LocationPane) getNodeFromGridPane(grid, column, row);
                try{
                    String color = rs.getString("Color");
                    tmpPane.setStyle("-fx-background-color: " + color);
                }catch ( Exception e){
                    e.printStackTrace();
                    tmpPane.setStyle("-fx-background-color: black;");
                }
                
                if(column == 149) {
                    row++;
                    column = 0;
                } else {
                    column ++;
                }
            } while (rs.next());
        }catch(Exception e){
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
    private Node getNodeFromGridPane(GridPane gridPane, int col, int row) {
        for (Node node : gridPane.getChildren()) {
            if (GridPane.getColumnIndex(node) == col && GridPane.getRowIndex(node) == row) {
                return node;
            }
        }
        return null;
    }

    private void startBackground() {
        System.out.println("Background Processes Started");
        final Runnable backgroundTasks = new Runnable() {
                public void run() {
                    try{       
                        String sql = "SELECT Color, Location FROM ds_grid";
                        Statement stmt = cn.createStatement();
                        rs = stmt.executeQuery(sql);
                    } catch(Exception e){
                        Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, e);
                    }
                    
                    exportImage();
                    if(OsUtils.isWindows()){
                        updateWDesktop.start();
                    } else {
                        updateMacDesktop();
                    } 
                }
            };
        final ScheduledFuture<?> backgroundTasksScheduler =
            scheduler.scheduleAtFixedRate(backgroundTasks, 10, 15, SECONDS);
    }

    private void updateMacDesktop(){
        String as[] = {
                "osascript", 
                "-e", "tell application \"Finder\"", 
                "-e", "set desktop picture to POSIX file \"" + jarPath + "\\DeskSpace.png" + "\"",
                "-e", "end tell"
        };
        Runtime runtime = Runtime.getRuntime();
        try{
            runtime.exec(as);
        }catch(Exception e){
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    class LocationPane extends Pane{
        int row;
        int column;
        String hex;
        
        public void setRow(int row){
            this.row = row;
        }
        
        public void setHex(String hex){
            this.hex = hex;
        }
        
        public String getHex(){
            return hex;
        }
        public void setColumn(int column){
            this.column = column;
        }
        
        public int getRow(){
            return this.row;
        }
        
        public int getColumn(){
            return this.column;
        }
    }


    private class ZoomingPane extends Pane {
        Node content;
        private DoubleProperty zoomFactor = new SimpleDoubleProperty(1);

        private ZoomingPane(Node content) {
            this.content = content;
            getChildren().add(content);
            Scale scale = new Scale(1, 1);
            content.getTransforms().add(scale);

            zoomFactor.addListener(new ChangeListener<Number>() {
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    scale.setX(newValue.doubleValue());
                    scale.setY(newValue.doubleValue());
                    requestLayout();
                }
            });
        }

        protected void layoutChildren() {
            Pos pos = Pos.TOP_LEFT;
            double width = getWidth();
            double height = getHeight();
            double top = getInsets().getTop();
            double right = getInsets().getRight();
            double left = getInsets().getLeft();
            double bottom = getInsets().getBottom();
            double contentWidth = (width - left - right)/zoomFactor.get();
            double contentHeight = (height - top - bottom)/zoomFactor.get();
            layoutInArea(content, left, top,
                    contentWidth, contentHeight,
                    0, null,
                    pos.getHpos(),
                    pos.getVpos());
        }

        public final Double getZoomFactor() {
            return zoomFactor.get();
        }
        public final void setZoomFactor(Double zoomFactor) {
            this.zoomFactor.set(zoomFactor);
        }
        public final DoubleProperty zoomFactorProperty() {
            return zoomFactor;
        }
    }
    
    public static final class OsUtils{
        private static String OS = null;
        public static String getOsName()
        {
           if(OS == null) { OS = System.getProperty("os.name"); }
           return OS;
        }
        public static boolean isWindows()
        {
           return getOsName().startsWith("Windows");
        }
    }
    
    public class UpdateSquare implements Runnable{
        private LocationPane pane;
        
        public void UpdateSquare(){
        }
        
        public void setPane(LocationPane pane){
            this.pane = pane;
        }

        @Override
        public void run() {
            try{                
                Color color = colorPicker.getValue();
                String css = "-fx-background-color: rgb(" + color.getRed() *255+","+color.getGreen()*255+","+color.getBlue()*255+")";

                String hex = String.format( "#%02X%02X%02X",
                        (int)( color.getRed() * 255 ),
                        (int)( color.getGreen() * 255 ),
                        (int)( color.getBlue() * 255 ) );
                System.out.println(hex + " at " + getLocation(pane.getRow(), pane.getColumn()));
                pane.setBackground((new Background(new BackgroundFill(Color.web(hex), CornerRadii.EMPTY, Insets.EMPTY))));
                pane.setHex(hex);
                pane.setStyle(css);
        
                Statement stmt = cn.createStatement();
                String sql = "UPDATE ds_grid SET Color='" + pane.getHex() + "',Edited_By='Brian' WHERE Location = " + getLocation(pane.getRow(), pane.getColumn());  
                stmt.executeUpdate(sql);
            }catch(Exception e){
                    e.printStackTrace();
            }
        }
    }
}


