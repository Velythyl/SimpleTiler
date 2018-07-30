package SimpleTiler;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.*;
import java.util.ArrayList;
import java.util.Optional;


public class Main extends Application {
    Controller controller;
    ToggleButton toggleGridLines, toggleCoord, toggleHex;
    ImageView topImg, noTopImg, selectionView;
    Image transpImg;
    VBox tilesVB, stickersVB, effectsVB;
    Stage stage;
    StackPane gridHolder;
    Pane topGrid, botGrid, coordGrid, paneCanvas;
    ScrollPane mapScroll;
    ColorPicker colorPicker, colorPicker2;
    CheckBox useGradient;
    BorderPane topBot;

    String tileImg, effectStr;

    BorderPane root;

    ListView<String> tileList, assetList;
    private ImageView assetSelectionView;
    private String assetImg;
    int funcState;


    public static void main(String[] args) {    //Launch du stage
        Main.launch(args);
    }

    public void start(Stage stage) {
        this.stage = stage;

        /**
         * Generating static values: controller, zoom scale, default tile selection and Image
         */
        controller = new Controller();
        tileImg = "transp.png";
        assetImg = "transp.png";
        transpImg = new Image("transp.png", 50, 0, true, false);
        funcState = 0;

        /**
         * Creates the root, scene, and stage
         */
        root = new BorderPane();

        Scene scene = new Scene(root, 1280, 720);	//scene pour le jeu entier
        scene.getStylesheets().add(getClass().getResource("listStyles.css").toExternalForm());

        stage.setTitle("Simple Tiler");
        stage.setScene(scene);

        defineEvents();

        /**
         * Adds the root's two elements: the menu and the program's main holder
         */
        HBox mainHolder = new HBox();
        root.setTop(generateMenuBar(stage));
        root.setCenter(mainHolder);

        /**
         * Creates and places the map's holder (borderpane: scrollpane: map grids)
         */
        BorderPane mapHolder = generateMapHolder();
        mainHolder.setHgrow(mapHolder, Priority.ALWAYS);
        mainHolder.getChildren().add(mapHolder);

        /**
         * Creates and places the map's function holder
         */
        mainHolder.getChildren().add(generateFuncHolder());

        stage.show();

        getTiles();
    }

    int nbOfMod;    //nb of col/row to mod
    private void defineEvents() {
        nbOfMod = 1;

        root.setOnKeyPressed(event -> {
            if(!controller.mapExists()) {
                event.consume();
                return;
            }

            //general logic
            try{
                nbOfMod = Integer.valueOf(event.getText());
                if(nbOfMod == 0) nbOfMod = 1;
                return;
            } catch (Exception e){}

            if(event.getCode() == KeyCode.G) {
                controller.toggleGridLines();
                showHideGridLines();
            } else if(event.getCode() == KeyCode.C) {
                controller.toggleHasCoord();
                showHideCoord();
            } else if(event.getCode() == KeyCode.H) {
                controller.toggleIsSquare();
                updateGrid();
            }

            if(funcState == 0) {    //Logic for grid
                if(event.getCode() == KeyCode.UP || event.getCode() == KeyCode.W) {
                    controller.setIsTop(true);

                    refreshTopBot();

                    return;
                } else if(event.getCode() == KeyCode.DOWN || event.getCode() == KeyCode.S) {
                    controller.setIsTop(false);

                    refreshTopBot();

                    return;
                }
            } else {    //Logic for paneCanvas
                if(lastElementClicked == null) return;

                if(event.getCode() == KeyCode.UP || event.getCode() == KeyCode.W) {
                    int i = Integer.valueOf(lastElementClicked.getId());

                    if(event.isControlDown()) controller.eleToFront(i);
                    else controller.eleUp(i);

                    updatePaneCanvas();

                    return;
                } else if(event.getCode() == KeyCode.DOWN || event.getCode() == KeyCode.S) {
                    int i = Integer.valueOf(lastElementClicked.getId());

                    if(event.isControlDown()) controller.eleToBack(i);
                    else controller.eleDown(i);

                    updatePaneCanvas();

                    return;
                } else if(event.getCode() == KeyCode.DELETE || event.getCode() == KeyCode.BACK_SPACE) {
                    controller.removeCanvasElement(Integer.valueOf(lastElementClicked.getId()));
                    updatePaneCanvas();
                    hideEditShapes();
                    return;
                } else if(event.getCode() == KeyCode.ESCAPE) {
                    hideEditShapes();
                    return;
                }
            }
        });

        root.setOnKeyReleased(event -> {

        });
    }

    public void refreshTopBot() {
        topBot.getChildren().clear();
        if(controller.getIsTop()) {
            topBot.setCenter(topImg);
        } else {
            topBot.setCenter(noTopImg);
        }
    }

    public Pane generatePMHolders(boolean isH) {

        Button plus = new Button("+");
        plus.setMaxSize(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        plus.setMinSize(40, 40);

        Button minus = new Button("-");
        minus.setMaxSize(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        minus.setMinSize(40, 40);

        if (isH) {
            plus.setOnAction(e -> {
                for(int i=0; i<nbOfMod; i++) {
                    controller.modColRow(false, true);
                }

                updateGrid();
                e.consume();
            });

            minus.setOnAction(e -> {
                for(int i=0; i<nbOfMod; i++) {
                    controller.modColRow(false, false);
                }

                updateGrid();
                e.consume();
            });

            HBox box = new HBox();
            box.setHgrow(plus, Priority.ALWAYS);
            box.setHgrow(minus, Priority.ALWAYS);

            box.getChildren().addAll(plus, minus);
            box.setBackground(new Background(new BackgroundFill(Color.web("#607D8B"), CornerRadii.EMPTY, Insets.EMPTY)));

            return box;
        } else {
            plus.setOnAction(e -> {
                for(int i=0; i<nbOfMod; i++) {
                    controller.modColRow(true, true);
                }

                updateGrid();
                e.consume();
            });

            minus.setOnAction(e -> {
                for(int i=0; i<nbOfMod; i++) {
                    controller.modColRow(true, false);
                }

                updateGrid();
                e.consume();
            });

            VBox box = new VBox();
            box.setVgrow(plus, Priority.ALWAYS);
            box.setVgrow(minus, Priority.ALWAYS);
            box.getChildren().addAll(plus, minus);

            return box;
        }
    }

    public ListView<String> generateImgList(boolean isTiles) {
        //https://stackoverflow.com/questions/33592308/javafx-how-to-put-imageview-inside-listview
        ListView<String> listView = new ListView<>();
        ObservableList<String> items =FXCollections.observableArrayList (isTiles? getTiles(): getAssets());
        if(isTiles) {
            items.add(0, "remove.png");
        }
        listView.setItems(items);

        listView.setCellFactory(param -> new ListCell<>() {
            private ImageView imageView = new ImageView();

            @Override
            public void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    if(name.equals("remove.png")) {
                        imageView.setImage(new Image(name));
                    } else {
                        imageView.setImage(new Image((isTiles ? "file:Tiles/" : "file:Assets/") + name));
                    }
                    imageView.setFitWidth(91);
                    imageView.setPreserveRatio(true);
                    setText("");
                    setGraphic(imageView);
                }
            }
        });

        listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.equals("remove.png")) {
                tileImg = "transp.png";
                selectionView.setImage(new Image("transp.png", 64, 0, true, false));
            } else if(isTiles) {
                tileImg = newValue;
                selectionView.setImage(new Image("file:Tiles/"+tileImg, 64, 0, true, false));
            } else {
                assetImg = newValue;
                assetSelectionView.setImage(new Image("file:Assets/"+assetImg, 64, 0, true, false));
            }

        });

        return listView;
    }

    public ArrayList<String> getTiles() {
        File[] listOfFiles = (new File("./Tiles")).listFiles();

        return getImgFiles(listOfFiles);
    }

    public ArrayList<String> getAssets() {
        File[] listOfFiles = (new File("./Assets")).listFiles();

        return getImgFiles(listOfFiles);
    }

    public ArrayList<String> getImgFiles(File[] listOfFiles) {
        ArrayList<String> list = new ArrayList<>();
        String[] extensions = new String[]{"png", "jpeg", "jpg", "bmp", "gif"};

        for (int i = 0; i < listOfFiles.length; i++) {
            String name = listOfFiles[i].getName();

            for(int j=0; j<extensions.length; j++) {
                if(name.endsWith("." + extensions[j])) {
                    list.add(name);
                }
            }
        }

        /*
            if (listOfFiles[i].isFile()) {
                System.out.println("File " + listOfFiles[i].getName());
            } else if (listOfFiles[i].isDirectory()) {
                System.out.println("Directory " + listOfFiles[i].getName());
            }*/
        return list;
    }

    public double getSize() {

        return controller.getScale();

    }

    public void updateMap() {
        refreshTranspImg();
        updateToggles();
        updateGrid();
        updatePaneCanvas();
    }

    public void updateToggles() {
        toggleHex.setSelected(!controller.getIsSquare());
        toggleGridLines.setSelected(controller.getHasGridLines());
        toggleCoord.setSelected(controller.getHasCoord());
    }

    public void updateGrid() {
        toggleHex.setSelected(!controller.getIsSquare());

        int[] xY = controller.getTileMapDim();
        botGrid.getChildren().clear();
        topGrid.getChildren().clear();
        coordGrid.getChildren().clear();

        int colCounter = 0;
        for(int x=0; x<xY[0]; x++) {
            for(int y=0; y<xY[1]; y++) {

                String botImg = controller.getCoord(x, y, false);
                String topImg = controller.getCoord(x, y, true);

                Shape botShape = new Rectangle(getSize(), getSize());
                if(!controller.getIsSquare()) botShape = new Polygon( new Hexagon(getSize()).getPoints());
                if(botImg.equals("transp.png")) {
                    botShape.setFill(new ImagePattern(transpImg));
                } else {
                    botShape.setFill(new ImagePattern(new Image("file:Tiles/" + botImg, getSize(), 0, true, false)));
                }

                Shape topShape = new Rectangle(getSize(), getSize());
                if(!controller.getIsSquare()) topShape = new Polygon( new Hexagon(getSize()).getPoints());
                if(topImg.equals("transp.png")) {
                    topShape.setFill(new ImagePattern(transpImg));
                } else {
                    topShape.setFill(new ImagePattern(new Image("file:Tiles/" + topImg, getSize(), 0, true, false)));
                }

                Label coordLabel = new Label(x+", "+y);
                coordLabel.setFont(new Font("Arial", getSize()/4.5));
                if(controller.getHasCoord()) {
                    coordLabel.setTextFill(Color.RED);
                } else {
                    coordLabel.setTextFill(Color.TRANSPARENT);
                }


                botGrid.getChildren().add(botShape);
                topGrid.getChildren().add(topShape);
                coordGrid.getChildren().add(coordLabel);

                botShape.setTranslateX(x*getSize());
                botShape.setTranslateY(y*getSize());

                topShape.setTranslateX(x*getSize());
                topShape.setTranslateY(y*getSize());

                coordLabel.setTranslateX(x*getSize());
                coordLabel.setTranslateY(y*getSize());

                botShape.setId(x+","+y);
                topShape.setId(x+","+y);

                if(!controller.getIsSquare()) {
                    Node[] hexes = new Node[]{botShape, topShape, coordLabel};

                    for(Node hex: hexes) {

                        double addTransX = getSize()/4*Math.tan(Math.toRadians(30))*-colCounter;
                        hex.setTranslateX(addTransX+x*getSize()+getSize()/2*Math.tan(Math.toRadians(30)));

                        double addTransY = 0;
                        if(!(x==0 || x%2==0)) addTransY = getSize()/2;
                        hex.setTranslateY(addTransY+y*getSize()+getSize()/10);  //a l'oeil 1/10 est ok
                    }
                }

                //topShape handles events and gridLines
                if(controller.getHasGridLines()) {
                    topShape.setStroke(Color.BLACK);
                } else {
                    topShape.setStroke(Color.TRANSPARENT);
                }

                Shape finalTopShape = topShape;
                EventHandler<MouseEvent> tileClickEvent = event -> {
                    String xyStr= finalTopShape.getId();
                    int posVirg = xyStr.indexOf(",");
                    int i = Integer.valueOf(xyStr.substring(0, posVirg));
                    int j = Integer.valueOf(xyStr.substring(posVirg+1, xyStr.length()));
                    controller.setCoord(tileImg, i, j);
                    updateCoord(i,j);
                    event.consume();
                };

                topShape.setOnMousePressed(tileClickEvent);
                topShape.setOnDragDetected(event -> finalTopShape.startFullDrag());
                topShape.setOnMouseDragEntered(tileClickEvent);
            }

            colCounter++;
        }

        double xOffset = 0;
        double yOffset = 0;
        if(!controller.getIsSquare()) {
            xOffset = getSize()/4*Math.tan(Math.toRadians(30));
            xOffset *= xY[0]-2;

            yOffset = getSize()/2+getSize()/10;
        }
        paneCanvas.setPrefSize(xY[0]*getSize()-xOffset, xY[1]*getSize()+yOffset);
    }

    public void updateCoord(int x, int y) {     //TODO remove node at x y...

        Pane thisPane = controller.getIsTop() ? topGrid : botGrid;
        String thisImg = controller.getIsTop() ? controller.getCoord(x, y, true) : controller.getCoord(x, y, false);

        for (Node node : thisPane.getChildren()) {
            if (node.getId().equals(x+","+y)) {

                if(thisImg.equals("transp.png")) {
                    ((Shape) node).setFill(new ImagePattern(transpImg));
                } else {
                    ((Shape) node).setFill(new ImagePattern(new Image("file:Tiles/" + thisImg, getSize(), 0, true, false)));
                }
            }
        }
    }

    Node lastElementClicked;
    Rectangle resizeRect;
    Circle rotateCirc;
    double editShapeSize;

    public void updatePaneCanvas() {
        paneCanvas.getChildren().clear();

        editShapeSize = 10;

        resizeRect = new Rectangle(editShapeSize, editShapeSize, Color.WHITE);
        resizeRect.setStroke(Color.BLACK);

        rotateCirc = new Circle(editShapeSize/2, Color.WHITE);
        rotateCirc.setStroke(Color.BLACK);

        paneCanvas.getChildren().addAll(resizeRect, rotateCirc);
        hideEditShapes();

        int scale = controller.getScale();



        ArrayList<CanvasElement> canvasElements = controller.getCanvasElements();
        for(int i=0; i<canvasElements.size(); i++) {
            CanvasElement ele = canvasElements.get(i);

            /**
             * Handles zooming
             */
            if(scale != ele.lastScale) {
                double zoom = ((double)scale)/ele.lastScale;

                ele.xy[0]*=zoom;
                ele.xy[1]*=zoom;
                ele.wh[0]*=zoom;
                ele.wh[1]*=zoom;

                ele.lastScale = scale;
            }

            Node thisEleNode = new ImageView(transpImg);    //False, will be initiated in switch. Used to save on duplicate code: all ele Nodes will use the same events...
            switch (ele.type) {
                case "img":
                    ImageView imageView = new ImageView(new Image("file:Assets/"+ele.info, ele.wh[0], ele.wh[1], false, false));
                    thisEleNode = imageView;
                    break;
                case "ellipse":
                    Ellipse ellipse = new Ellipse(ele.wh[0], ele.wh[1]);
                    thisEleNode = ellipse;
                    break;
                case "rect":
                    Rectangle rect = new Rectangle(ele.wh[0], ele.wh[1]);
                    thisEleNode = rect;
                    break;
            }

            if(ele.info != null && ele.info.equals("gradient")) {
                RadialGradient gradient = new RadialGradient(
                        0,
                        0,
                        0.5,
                        0.5,
                        1.0,
                        true,
                        CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web(ele.colors[0])),
                        new Stop(1, Color.web(ele.colors[1])));

                ((Shape) thisEleNode).setFill(gradient);
            } else if(!ele.type.equals("img")) {
                ((Shape) thisEleNode).setFill(Color.web(ele.colors[0]));
            }

            thisEleNode.setId(Integer.toString(i));     //assign an index if we need to delete the ele from the map
            paneCanvas.getChildren().add(thisEleNode);
            thisEleNode.setTranslateX(ele.xy[0]);
            thisEleNode.setTranslateY(ele.xy[1]);
            rotateOnCenter(ele, thisEleNode);

            /**
             * Resize and rotate logic
             */
            Node finalThisEleNode1 = thisEleNode;
            EventHandler<MouseEvent> adaptEditShapes = e -> {  //called whenever the current element is focused/moved
                showEditShapes(ele);

                /**
                 * Resize logic
                 */
                double[] vectXY = new double[2];
                resizeRect.setOnMousePressed(event -> {
                    vectXY[0] = ele.wh[0];
                    vectXY[1] = ele.wh[1];

                    paneCanvas.setMouseTransparent(true);
                });
                resizeRect.setOnDragDetected(event -> resizeRect.startDragAndDrop(TransferMode.ANY));
                double[] newWH = new double[2];
                resizeRect.setOnMouseDragged(event -> {     //TODO garder sur "rail" lorsque garder ratio
                    paneCanvas.setMouseTransparent(true);

                    double correctX = event.getSceneX() - 40;
                    double correctY = event.getSceneY() - 80;

                    double w = 0;
                    double h = 0;
                    if(ele.type.equals("ellipse")) {
                        w = ele.wh[0]/2;
                        h = ele.wh[1]/2;
                    }
                    if(correctY + h > paneCanvas.getHeight() || correctX + w > paneCanvas.getWidth() || correctX + w < ele.xy[0] || correctY + h < ele.xy[1]) {
                        return;
                    }

                    /*  origin: top left corner of Node;
                        vector: width, height
                        (b*a)/(a*a) https://gerardnico.com/linear_algebra/closest_point_line

                        creates a rail by calculating the closest point on the line going from the origin along the
                        vector that's the closest to the event's X and Y. Then, replaces these X and Y by that closest
                        point. This is all done in a coordinate system with the origin as (0,0), not the scene's origin.
                     */
                    if(event.isControlDown()) {
                        correctX = correctX - ele.xy[0];
                        correctY = correctY - ele.xy[1];

                        double num = correctX*vectXY[0] + correctY*vectXY[1];
                        double den = vectXY[0]*vectXY[0] + vectXY[1]*vectXY[1];
                        double vectScale = num/den;

                        correctX = vectXY[0]*vectScale+ele.xy[0];
                        correctY = vectXY[1]*vectScale+ele.xy[1];
                    }

                    ((Rectangle) event.getSource()).setTranslateX(correctX);
                    ((Rectangle) event.getSource()).setTranslateY(correctY);

                    newWH[0] = correctX - ele.xy[0];
                    newWH[1] = correctY - ele.xy[1];

                    ele.wh = newWH;

                    if(lastElementClicked instanceof ImageView) {
                        ((ImageView) lastElementClicked).setFitWidth(newWH[0]);
                        ((ImageView) lastElementClicked).setFitHeight(newWH[1]);
                    } else if(lastElementClicked instanceof Rectangle) {
                        ((Rectangle) lastElementClicked).setWidth(newWH[0]);
                        ((Rectangle) lastElementClicked).setHeight(newWH[1]);
                    } else if(lastElementClicked instanceof Ellipse) {
                        ((Ellipse) lastElementClicked).setRadiusX(newWH[0]);
                        ((Ellipse) lastElementClicked).setRadiusY(newWH[1]);
                    }
                });
                resizeRect.setOnMouseReleased(event -> {
                    paneCanvas.setMouseTransparent(false);

                    if(lastElementClicked instanceof  ImageView) {
                        ((ImageView) lastElementClicked).setImage(new Image("file:Assets/"+ele.info, newWH[0], newWH[1], false, false));    //redraw to possibly get better resolution
                    }
                });

                /**
                 * Rotate logic
                 */
                rotateCirc.setOnMousePressed(event -> paneCanvas.setMouseTransparent(true));
                rotateCirc.setOnDragDetected(event -> rotateCirc.startDragAndDrop(TransferMode.ANY));
                rotateCirc.setOnMouseDragged(event -> {     //TODO garder sur "rail"
                    paneCanvas.setMouseTransparent(true);

                    double correctX = event.getSceneX()-40;         //actual x and y events
                    double correctY = event.getSceneY()-80;
                    double newX = correctX-ele.xy[0]-ele.wh[0]/2;   //calculate vector with center of lastEleNode as origin
                    double newY = correctY-ele.xy[1]-ele.wh[1]/2;

                    /**
                     * Angle logic
                     */
                    double angle = Math.toDegrees(Math.atan2(newY, newX))+90;

                    if(event.isControlDown()) {
                        angle = roundAngle(angle);
                    }

                    ele.angle = angle;
                    rotateOnCenter(ele, finalThisEleNode1);

                    /**
                     * Rail logic
                     */
                    double normLen = ele.wh[1]/2+editShapeSize;
                    double vectLen = Math.sqrt(newX*newX+newY*newY);
                    if (vectLen<=1e-20) vectLen = 1;
                    double vectScale = normLen/vectLen;

                    newX = newX*vectScale+ele.xy[0]+ele.wh[0]/2;
                    newY = newY*vectScale+ele.xy[1]+ele.wh[1]/2;

                    ((Circle) event.getSource()).setTranslateX(newX);
                    ((Circle) event.getSource()).setTranslateY(newY);
                });
                rotateCirc.setOnMouseReleased(event -> paneCanvas.setMouseTransparent(false));
            };

            /**
             * Drag logic
             */
            Node finalThisEleNode = thisEleNode;    //dumb cast
            thisEleNode.setOnMousePressed(event -> {
                lastElementClicked = finalThisEleNode;
                paneCanvas.setMouseTransparent(true);
                adaptEditShapes.handle(event);
            });
            thisEleNode.setOnDragDetected(event -> finalThisEleNode.startDragAndDrop(TransferMode.ANY));
            thisEleNode.setOnMouseDragged(event -> {
                paneCanvas.setMouseTransparent(true);
                ((Node) event.getSource()).setTranslateX(event.getSceneX()-40); //WHY???? 40?
                ((Node) event.getSource()).setTranslateY(event.getSceneY()-80); //WHY???? 80?

                ele.xy[0] = ((Node) event.getSource()).getTranslateX();  //update ele
                ele.xy[1] = ((Node) event.getSource()).getTranslateY();

                adaptEditShapes.handle(event);
            });
            thisEleNode.setOnMouseReleased(event -> paneCanvas.setMouseTransparent(false));
        }
    }

    public double roundAngle(double angle) {
        if((-67.5>angle && angle>-90)
                ||
                (angle>245.5 && angle<270)) {
            return  270;
        }

        double k = 22.5;
        double bound1;
        double bound2;
        double nextAngle = 225;

        while(nextAngle > -46) {
            bound1 = nextAngle+k;
            bound2 = nextAngle-k;

            if(bound1>angle && angle>bound2) {
                return nextAngle;
            }

            nextAngle -= 45;
        }

        return angle;
    }

    public void rotateOnCenter(CanvasElement ele, Node thisEleNode) {
        double rotXR = ele.xy[0];
        double rotYR = ele.xy[1];
        double rotXL = ele.xy[0] - ele.wh[0]/2;
        double rotYL = ele.xy[1] - ele.wh[1]/2;

        rotateOnCenter(thisEleNode, rotXR, rotYR, rotXL, rotYL, ele.angle);
    }

    public void rotateOnCenter(Node thisEleNode, double rotXR, double rotYR, double rotXL, double rotYL, double angle) {
        thisEleNode.setTranslateX(rotXL);
        thisEleNode.setTranslateY(rotYL);

        thisEleNode.setRotate(angle);

        thisEleNode.setTranslateX(rotXR);
        thisEleNode.setTranslateY(rotYR);
    }

    public void showEditShapes(CanvasElement ele) {
        double x = ele.xy[0];
        double y = ele.xy[1];
        double w = ele.wh[0];
        double h = ele.wh[1];
        double c = editShapeSize;

        paneCanvas.getChildren().remove(resizeRect);
        paneCanvas.getChildren().add(resizeRect);
        resizeRect.setVisible(true);
        resizeRect.setMouseTransparent(false);

        if(!ele.type.equals("ellipse")) {
            resizeRect.setTranslateX(x + w + c);
            resizeRect.setTranslateY(y + h + c);

            paneCanvas.getChildren().remove(rotateCirc);
            paneCanvas.getChildren().add(rotateCirc);
            rotateCirc.setVisible(true);
            rotateCirc.setMouseTransparent(false);

            if(ele.angle == 0) {
                rotateCirc.setTranslateX(x + w / 2);
                rotateCirc.setTranslateY(y - c);
            } else {    //must rotate the circ too
                double vectLen = h/2+c;                         //calculate normal vect lenght, rotate it with https://matthew-brett.github.io/teaching/rotation_2d.html
                double rad = Math.toRadians((ele.angle-90));    //return to original coord system, and then translate

                double vectX = vectLen*Math.cos(rad);
                double vectY = vectLen*Math.sin(rad);

                vectX += w/2;
                vectY += h/2;

                rotateCirc.setTranslateX(x + vectX);
                rotateCirc.setTranslateY(y + vectY);
            }
            
        } else {
            resizeRect.setTranslateX(x + w);
            resizeRect.setTranslateY(y + h);
        }
    }

    public void hideEditShapes() {
        resizeRect.setVisible(false);
        resizeRect.setMouseTransparent(true);

        rotateCirc.setVisible(false);
        rotateCirc.setMouseTransparent(true);
    }

    public void makeNewMap() {
        controller.newTileMap();
        updateMap();
    }

    public void refreshTranspImg() {
        transpImg = new Image("transp.png", getSize(), 0, true, false);
    }

    public FileChooser generateMapChooser(String directory) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("./"+directory));

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Map files (*.til)", "*.til");
        fileChooser.getExtensionFilters().add(extFilter);

        return fileChooser;
    }

    public void alertMapIsNull() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning Dialog");
        alert.setHeaderText("File Error");
        alert.setContentText("The target map seems to be null");

        alert.showAndWait();
    }

    public MenuBar generateMenuBar(Stage stage) {
        MenuBar menuBar = new MenuBar();
        //menuBar.prefWidthProperty().bind(stage.widthProperty().multiply(0.5));
        menuBar.prefWidthProperty().bind(stage.widthProperty());
        //menuBar.setPadding(new Insets(15, 0, 0, 15));

        Menu fileMenu = new Menu("File");
        MenuItem newMap = new MenuItem("New map");
        newMap.setOnAction(actionEvent -> makeNewMap());
        MenuItem saveMap = new MenuItem("Save map");
        saveMap.setOnAction(actionEvent -> {
            if(controller.mapExists()) {
                FileChooser fileChooser = generateMapChooser("Maps");
                fileChooser.setTitle("Save map as...");
                File file = fileChooser.showSaveDialog(stage);
                controller.saveMap(file);
            } else {
                alertMapIsNull();
            }

        });
        SeparatorMenuItem fileSpacer = new SeparatorMenuItem();
        MenuItem openMap = new MenuItem("Open map");
        openMap.setOnAction(actionEvent -> {
            FileChooser fileChooser = generateMapChooser("Maps");
            fileChooser.setTitle("Open map...");
            File file = fileChooser.showOpenDialog(stage);
            if(file != null) {
                controller.openMap(file, this);
                updateMap();
            }

        });
        MenuItem refreshMap = new MenuItem("Refresh map");
        refreshMap.setOnAction(actionEvent -> updateMap());
        MenuItem resetScale = new MenuItem("Reset scale");
        resetScale.setOnAction(actionEvent -> {
            controller.setScale(50);
            updateMap();
        });
        fileMenu.getItems().addAll(newMap, saveMap, openMap, fileSpacer, refreshMap, resetScale);

        Menu portMenu = new Menu("Port");
        MenuItem exportMap = new MenuItem("Export map");
        exportMap.setOnAction(actionEvent -> {
            if(controller.mapExists()) {

                TextInputDialog dialog = new TextInputDialog("");
                dialog.setTitle("Export as...");
                dialog.setHeaderText("You will find the map in the Ports folder\n\nWILL overwrite exports of the same name");
                dialog.setContentText("Enter the export's name:");

                Optional<String> result = dialog.showAndWait();

                result.ifPresent(name -> {
                    controller.exportMap(name);
                });

            } else {
                alertMapIsNull();
            }

        });
        MenuItem importMap = new MenuItem("Import map");
        importMap.setOnAction(actionEvent -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Import Map");
            alert.setHeaderText("You will find the map in the Maps folder\n\nWILL overwrite maps of the same name\n\nA dependency solver will help you manage you tiles");
            alert.setContentText("Accept?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK){
                FileChooser fileChooser = generateMapChooser("Ports");
                fileChooser.setTitle("Import map...");
                File file = fileChooser.showOpenDialog(stage);
                if(file != null) {
                    controller.openMap(file, this);
                    controller.importMap(file, testDepConflict(file));
                    updateMap();
                    refreshImgList(true);
                }
            }
        });

        portMenu.getItems().addAll(exportMap, importMap);

        Menu printMenu = new Menu("Print");
        MenuItem printDefaultMap = new MenuItem("With default margins");
        printDefaultMap.setOnAction(actionEvent -> print(true));
        MenuItem printNoMap = new MenuItem("With no margins");
        printNoMap.setOnAction(actionEvent -> print(false));

        printMenu.getItems().addAll(printDefaultMap, printNoMap);

        menuBar.getMenus().addAll(fileMenu, portMenu, printMenu);

        return menuBar;
/*
        HBox menuHolder = new HBox();
        menuHolder.getChildren().add(menuBar);

        Region spacer = new Region();
        spacer.getStyleClass().add("menu-bar");
        HBox.setHgrow(spacer, Priority.SOMETIMES);
        menuHolder.getChildren().add(spacer);

        gridImg = new ImageView(new Image("grid.png", 40, 0, true, true));
        noGridImg = new ImageView(new Image("noGrid.png", 40, 0, true, true));
        gridButton = new Pane(gridImg);
        gridButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {

            if(controller.mapExists()) {
                gridButton.getChildren().clear();
                if(controller.getHasGridLines()) {
                    gridButton.getChildren().add(gridImg);
                } else {
                    gridButton.getChildren().add(noGridImg);
                }

                controller.toggleGridLines();
                showHideGridLines();
            }

            event.consume();
        });

        ImageView nbImg = new ImageView(new Image("coord.png", 40, 0, true, true));
        ImageView noNbImg = new ImageView(new Image("noCoord.png", 40, 0, true, true));
        Pane nbButton = new Pane(noNbImg);
        nbButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {


            if(controller.mapExists()) {

                nbButton.getChildren().clear();
                if(controller.getHasCoord()) {
                    nbButton.getChildren().add(nbImg);
                } else {
                    nbButton.getChildren().add(noNbImg);
                }

                controller.toggleHasCoord();
                showHideCoord();
            }

            event.consume();

        });

        menuHolder.getChildren().addAll(nbButton, gridButton);
        menuHolder.getStyleClass().add("menu-bar");
        menuHolder.setPadding(new Insets(0, 0, 0, 0));

        return menuHolder;*/
    }

    public void showHideGridLines() {
        toggleGridLines.setSelected(controller.getHasGridLines());

        for(Node child: topGrid.getChildren()) {
            if(controller.getHasGridLines()) {
                ((Shape) child).setStroke(Color.BLACK);
            } else {
                ((Shape) child).setStroke(Color.TRANSPARENT);
            }
        }
    }

    public void showHideCoord() {
        toggleCoord.setSelected(controller.getHasCoord());

        for(Node child: coordGrid.getChildren()) {
            if(controller.getHasCoord()) {
                ((Label) child).setTextFill(Color.RED);
            } else {
                ((Label) child).setTextFill(Color.TRANSPARENT);
            }
        }
    }

    public ArrayList<DoubleString> testDepConflict(File map) {
        ArrayList<String> inCommonTile = new ArrayList<>(controller.getTileDependency());
        ArrayList<String> notInCommonTile = new ArrayList<>(inCommonTile);

        ArrayList<String> inCommonAsset = new ArrayList<>(controller.getAssetDependency());
        ArrayList<String> notInCommonAsset = new ArrayList<>(inCommonAsset);

        inCommonTile.retainAll(getTiles());
        notInCommonTile.removeAll(inCommonTile);

        inCommonAsset.retainAll(getAssets());
        notInCommonAsset.removeAll(inCommonTile);

        ArrayList<DoubleString> notInCommonDSTile = DoubleString.transformStringArrayList(notInCommonTile, true);
        ArrayList<DoubleString> notInCommonDSAsset = DoubleString.transformStringArrayList(notInCommonTile, false);

        ArrayList<DoubleString> notInCommonDS = new ArrayList<>(notInCommonDSTile);
        notInCommonDS.addAll(notInCommonDSAsset);

        if(inCommonTile.size()==0 && inCommonAsset.size()==0) {
            if(notInCommonDS.size()>=1) {
                return notInCommonDS;
            }
            else return null;
        }

        ArrayList<DoubleString> inCommonDS = DoubleString.transformStringArrayList(inCommonTile, true);
        inCommonDS.addAll(DoubleString.transformStringArrayList(inCommonAsset, false));

        return bootDepSolver(inCommonDS, notInCommonDS, map.getName().substring(0, map.getName().length()-4));
    }

    public void refreshImgList(boolean isTiles) {
        if(isTiles) {
            tileList.setItems(null);
            tileList.setItems(FXCollections.observableArrayList (getTiles()));
        } else {
            assetList.setItems(null);
            assetList.setItems(FXCollections.observableArrayList(getAssets()));
        }

    }

    private int depCounter;
    private ImageView dep, imp;
    private String tilePrefix, assetPrefix, impPrefix, currentImpSuffix;
    private TextField newNameDep;
    private Label depCounterDisplay, currentImpSuffixDisplay;
    private ArrayList<DoubleString> inCommon;
    private Button deleteFromDep, renameFromDep;
    private Stage importDialog;

    public ArrayList<DoubleString> bootDepSolver(ArrayList<DoubleString> inCommon, ArrayList<DoubleString> notInCommon, String mapName)  {
        depCounter = 0;
        tilePrefix = "file:Tiles/";
        assetPrefix = "file:Assets/";
        impPrefix = "file:Ports/"+mapName+"/";
        currentImpSuffix = inCommon.get(depCounter).first.substring(inCommon.get(depCounter).first.length() - 4);
        this.inCommon = inCommon;

        BorderPane dependencySolveHolder = new BorderPane();

        Scene scene = new Scene(dependencySolveHolder, 1000, 500);
        importDialog = new Stage();
        importDialog.initModality(Modality.APPLICATION_MODAL);
        importDialog.setScene(scene);
        importDialog.initStyle(StageStyle.UNDECORATED);
        importDialog.setResizable(false);

        BorderPane tileDepHolder = new BorderPane();
        tileDepHolder.setPrefWidth(dependencySolveHolder.getWidth()/2);
        BorderPane impDepHolder = new BorderPane();
        impDepHolder.setPrefWidth(dependencySolveHolder.getWidth()/2);
        depCounterDisplay = new Label("Image 1 of "+inCommon.size());
        dependencySolveHolder.setAlignment(depCounterDisplay, Pos.CENTER_RIGHT);

        dependencySolveHolder.setTop(depCounterDisplay);
        dependencySolveHolder.setLeft(tileDepHolder);
        dependencySolveHolder.setRight(impDepHolder);

        Label tileLabel = new Label("Currently in your tiles:");
        tileLabel.setMaxSize(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        tileLabel.setMinSize(40, 40);
        Label impLabel = new Label("In the import's dependency:");
        impLabel.setMaxSize(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        impLabel.setMinSize(40, 40);

        tileDepHolder.setTop(tileLabel);
        impDepHolder.setTop(impLabel);

        //if tile: get file/tiles if not, get file/assets
        dep = new ImageView(new Image((inCommon.get(depCounter).isTile? tilePrefix : assetPrefix)+inCommon.get(depCounter).first, 100, 0, true, false));
        imp = new ImageView(new Image(impPrefix+inCommon.get(depCounter).first, 100, 0, true, false));

        tileDepHolder.setCenter(dep);
        impDepHolder.setCenter(imp);

        deleteFromDep = new Button("These tiles are the same image");
        deleteFromDep.setMaxSize(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        deleteFromDep.setMinSize(40, 40);
        renameFromDep = new Button("These tiles are not the same image, rename the import to this name:");
        renameFromDep.setMaxSize(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        renameFromDep.setMinSize(40, 40);
        newNameDep = new TextField();
        newNameDep.setMaxSize(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        newNameDep.setMinSize(40, 40);
        newNameDep.setText(inCommon.get(depCounter).first.substring(0, inCommon.get(depCounter).first.length()-4));
        newNameDep.setPromptText("Cannot be empty");
        currentImpSuffixDisplay = new Label(currentImpSuffix);
        currentImpSuffixDisplay.setMaxSize(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        currentImpSuffixDisplay.setMinHeight(40);

        deleteFromDep.setOnAction(e -> {
            inCommon.remove(depCounter);
            depCounter--;
            iterateDepSolver();
        });
        renameFromDep.setOnAction(e -> {
            if(!newNameDep.getText().equals("")) {
                inCommon.get(depCounter).second = newNameDep.getText()+currentImpSuffix;
                iterateDepSolver();
            }
        });

        HBox depButtonHolder = new HBox();
        depButtonHolder.setHgrow(deleteFromDep, Priority.ALWAYS);
        depButtonHolder.setHgrow(renameFromDep, Priority.ALWAYS);
        depButtonHolder.setHgrow(newNameDep, Priority.ALWAYS);
        depButtonHolder.getChildren().addAll(deleteFromDep, renameFromDep, newNameDep, currentImpSuffixDisplay);

        dependencySolveHolder.setBottom(depButtonHolder);

        importDialog.showAndWait();

        notInCommon.addAll(inCommon);

        return notInCommon;
    }

    public void iterateDepSolver() {
        depCounter++;

        if(depCounter< inCommon.size()) {
            dep.setImage(new Image((inCommon.get(depCounter).isTile? tilePrefix : assetPrefix)+ inCommon.get(depCounter).first, 100, 0, true, false));
            imp.setImage(new Image(impPrefix+ inCommon.get(depCounter).first, 100, 0, true, false));

            depCounterDisplay.setText("Image "+depCounter+" of "+ inCommon.size());
            currentImpSuffix = inCommon.get(depCounter).first.substring(inCommon.get(depCounter).first.length() - 4);
            newNameDep.setText(inCommon.get(depCounter).first.substring(0, inCommon.get(depCounter).first.length()-4));
            currentImpSuffixDisplay.setText(currentImpSuffix);
        } else {
            importDialog.close();
        }

    }

    public BorderPane generateMapHolder() {
        BorderPane mapHolder = new BorderPane();
        mapHolder.setBackground(new Background(new BackgroundFill(Color.web("#ffffff"), CornerRadii.EMPTY, Insets.EMPTY)));

        mapHolder.setBottom(generatePMHolders(true));
        mapHolder.setRight(generatePMHolders(false));

        mapScroll = new ScrollPane();
        gridHolder = new StackPane();
        botGrid = new Pane();
        topGrid = new Pane();
        coordGrid = new Pane();
        paneCanvas = new Pane();      //paneCanvas holds off-grid images and effects

        /*
        botGrid = new GridPane();
        topGrid = new GridPane();
        coordGrid = new GridPane();

        coordGrid.widthProperty().addListener(listener -> paneCanvas.setPrefWidth(coordGrid.getWidth()));       //when the grid changes, so will the paneCanvas
        coordGrid.heightProperty().addListener(listener -> paneCanvas.setPrefHeight(coordGrid.getHeight()));*/

        paneCanvas.setOnMouseClicked(event -> {

            if(funcState == 1) {
                Image img = new Image("file:Assets/"+assetImg);
                CanvasElement ele = new CanvasElement(
                        "img",
                        assetImg,
                        new double[]{event.getX(), event.getY()},
                        new double[]{img.getWidth(), img.getHeight()},
                        controller.getScale());
                controller.addAsset(ele);
                updatePaneCanvas();
            } else if(funcState == 2) {
                CanvasElement ele = new CanvasElement(
                        effectStr,
                        useGradient.isSelected()? "gradient": null,
                        new String[]{colorPicker.getValue().toString(), colorPicker2.getValue().toString()},
                        new double[]{event.getX(), event.getY()},
                        new double[]{controller.getScale(), controller.getScale()},
                        controller.getScale());
                controller.addAsset(ele);
                updatePaneCanvas();
            }
        });

        mapScroll.addEventFilter(ScrollEvent.SCROLL, event -> {
            if(event.isControlDown()) {

                int zoom = controller.getScale();

                if(event.getDeltaY()<0) {
                    zoom *= 1/1.2;
                } else {
                    zoom *= 1.2;
                }

                //test if grid too small (from 4 downwards can't zoom out because int)
                if(zoom <= 4) {
                    event.consume();
                    return;
                }

                controller.setScale(zoom);
                updateMap();

                event.consume();
            } else if(event.isAltDown()) {                                  //TODO marche pas bien...
                double delta = mapScroll.getWidth()/mapScroll.getHeight();

                if(mapScroll.getWidth() < mapScroll.getHeight()) {
                    delta = 1/delta;
                }

                mapScroll.setHvalue(mapScroll.getHvalue() - delta*event.getDeltaY());

                event.consume();
            }
        });

        mapScroll.setPadding(new Insets(40, 40, 40, 40));

        //gridHolder.getChildren().addAll(botGrid, topGrid, botGrid, topGrid, paneCanvas, coordGrid, coordGrid);
        gridHolder.getChildren().addAll(botGrid, topGrid, coordGrid, paneCanvas);
        mapScroll.setContent(gridHolder);

        mapHolder.setCenter(mapScroll);

        return mapHolder;
    }

    public VBox generateFuncHolder() {
        VBox funcHolder = new VBox();
        funcHolder.setPrefSize(120,200);

        toggleHex = new ToggleButton("");
        toggleGridLines = new ToggleButton("");
        toggleCoord = new ToggleButton("");

        Polygon hex = new Polygon(new Hexagon(30).getPoints());
        hex.setFill(Color.TRANSPARENT);
        hex.setStroke(Color.BLACK);
        toggleHex.setGraphic(hex);
        toggleHex.setMinSize(40, 40);

        toggleGridLines.setGraphic(new ImageView(new Image("grid.png", 40, 0, true, true)));
        toggleCoord.setGraphic(new ImageView(new Image("coord.png", 40, 0, true, true)));

        toggleHex.setPadding(Insets.EMPTY);
        toggleGridLines.setPadding(Insets.EMPTY);
        toggleCoord.setPadding(Insets.EMPTY);

        toggleHex.setMaxSize(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        toggleGridLines.setMaxSize(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        toggleCoord.setMaxSize(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

        toggleHex.setOnAction(event -> {
            if(controller.mapExists()) {
                controller.toggleIsSquare();
                updateGrid();
            }
        });
        toggleGridLines.setOnAction(event -> {
            if(controller.mapExists()) {
                controller.toggleGridLines();
                showHideGridLines();
            }
        });
        toggleCoord.setOnAction(event -> {
            if(controller.mapExists()) {
                controller.toggleHasCoord();
                showHideCoord();
            }
        });

        funcHolder.getChildren().add(new HBox(toggleHex, toggleGridLines, toggleCoord));

        ToggleButton toggleTiles = new ToggleButton("");
        ToggleButton toggleStickers = new ToggleButton("");
        ToggleButton toggleEffects = new ToggleButton("");

        ToggleGroup toggleGroup = new ToggleGroup();
        toggleTiles.setToggleGroup(toggleGroup);
        toggleTiles.setSelected(true);
        toggleStickers.setToggleGroup(toggleGroup);
        toggleEffects.setToggleGroup(toggleGroup);

        toggleTiles.setOnAction((ActionEvent e) -> switchFunction(0, funcHolder));
        toggleStickers.setOnAction((ActionEvent e) -> switchFunction(1, funcHolder));
        toggleEffects.setOnAction((ActionEvent e) -> switchFunction(2, funcHolder));

        toggleTiles.setGraphic(new ImageView(new Image("tile.png", 80/3, 0, true, true)));
        toggleTiles.setPadding(Insets.EMPTY);
        toggleStickers.setGraphic(new ImageView(new Image("sticker.png", 80/3, 0, true, true)));
        toggleStickers.setPadding(Insets.EMPTY);
        toggleEffects.setGraphic(new ImageView(new Image("effect.png", 80/3, 0, true, true)));
        toggleEffects.setPadding(Insets.EMPTY);

        toggleTiles.setMaxSize(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        toggleStickers.setMaxSize(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        toggleEffects.setMaxSize(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

        HBox toggleHolder = new HBox(toggleTiles, toggleStickers, toggleEffects);
        toggleHolder.setHgrow(toggleTiles, Priority.ALWAYS);
        toggleHolder.setHgrow(toggleStickers, Priority.ALWAYS);
        toggleHolder.setHgrow(toggleEffects, Priority.ALWAYS);

        funcHolder.getChildren().add(toggleHolder);

        switchFunction(0, funcHolder);
        return funcHolder;
    }

    public void switchFunction(int i, VBox funcHolder) {
        funcState = i;
        VBox temp = null;

        try{
            funcHolder.getChildren().remove(tilesVB);
        } catch (Exception e) {}
        try{
            funcHolder.getChildren().remove(stickersVB);
        } catch (Exception e) {}
        try{
            funcHolder.getChildren().remove(effectsVB);
        } catch (Exception e) {}

        switch (i) {
            case 0:
                enableGridEvents(true);  //need to click on labels to edit grids

                if(tilesVB != null) {
                    temp = tilesVB;
                } else {
                    tilesVB = new VBox();

                    selectionView = new ImageView(new Image("transp.png", 64, 0, true, false));
                    BorderPane selectionHolder = new BorderPane();
                    selectionHolder.setCenter(selectionView);
                    selectionHolder.setPadding(new Insets(8, 8, 0, 8));
                    tilesVB.getChildren().add(selectionHolder);

                    topImg = new ImageView(new Image("top.png", 40, 0, true, false));
                    noTopImg = new ImageView(new Image("bot.png", 40, 0, true, false));
                    topBot = new BorderPane();
                    topBot.setCenter(topImg);
                    topBot.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                        controller.toggleIsTop();

                        refreshTopBot();
                        event.consume();
                    });
                    tilesVB.getChildren().addAll(topBot);

                    tileList = generateImgList(true);
                    tileList.setMaxSize(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
                    tilesVB.setVgrow(tileList, Priority.ALWAYS);
                    tilesVB.getChildren().add(tileList);

                    temp = tilesVB;
                }
                break;
            case 1:
                enableGridEvents(false);  //need to click through labels to edit paneCanvas

                if(stickersVB != null) {
                    temp = stickersVB;
                } else {
                    stickersVB = new VBox();

                    assetSelectionView = new ImageView(new Image("transp.png", 64, 0, true, false));
                    BorderPane selectionHolder = new BorderPane();
                    selectionHolder.setCenter(assetSelectionView);
                    selectionHolder.setPadding(new Insets(8, 8, 8, 8));
                    stickersVB.getChildren().add(selectionHolder);

                    assetList = generateImgList(false);
                    assetList.setMaxSize(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
                    stickersVB.setVgrow(assetList, Priority.ALWAYS);
                    stickersVB.getChildren().add(assetList);

                    temp = stickersVB;
                }
                break;
            case 2:
                enableGridEvents(false);  //need to click through labels to edit paneCanvas

                if(effectsVB != null) {
                    temp = effectsVB;
                } else {
                    effectsVB = new VBox();
                    effectsVB.setMinWidth(80);

                    VBox colorBox1 = new VBox();
                    effectsVB.getChildren().add(colorBox1);

                    Label colorLabel = new Label("Color:");
                    colorPicker = new ColorPicker(Color.GOLD);
                    Label spacer = new Label("");
                    spacer.setMaxWidth(Double.MAX_VALUE);
                    spacer.setStyle("-fx-background-color: gold; -fx-border-color: black");

                    Label colorPick = new Label(Color.GOLD.toString());
                    colorPick.setMaxWidth(Double.MAX_VALUE);
                    colorPick.setAlignment(Pos.BASELINE_RIGHT);
                    colorPick.setTextFill(Color.GOLD);

                    Button useDefaultColor = new Button("Torchlight");
                    useDefaultColor.setMaxWidth(Double.MAX_VALUE);

                    useGradient = new CheckBox("Gradient");
                    useGradient.setMaxWidth(Double.MAX_VALUE);

                    useDefaultColor.setOnAction(event -> {
                        colorPick.setText(Color.GOLD.toString());
                        colorPick.setTextFill(Color.GOLD);

                        spacer.setStyle("-fx-background-color: gold; -fx-border-color: black");

                        colorPicker.setValue(Color.GOLD);
                    });
                    colorPicker.setOnAction(event -> {
                        colorPick.setText(colorPicker.getValue().toString());
                        colorPick.setTextFill(colorPicker.getValue());

                        spacer.setStyle("-fx-background-color: #"+colorPicker.getValue().toString().substring(2)+"; -fx-border-color: black");
                    });

                    colorBox1.getChildren().addAll(colorLabel, colorPick, spacer, colorPicker, useDefaultColor, useGradient, new Label(""));

                    VBox colorBox2 = new VBox();
                    effectsVB.getChildren().add(colorBox2);
                    colorBox2.setVisible(false);
                    colorBox2.setManaged(false);
                    useGradient.setOnAction(event -> {
                        colorBox2.setVisible(useGradient.isSelected());
                        colorBox2.setManaged(useGradient.isSelected());
                    });

                    Label colorLabel2 = new Label("Color 2:");
                    colorPicker2 = new ColorPicker(Color.TRANSPARENT);
                    Label spacer2 = new Label("");
                    spacer2.setMaxWidth(Double.MAX_VALUE);
                    spacer2.setStyle("-fx-background-color: transparent; -fx-border-color: black");

                    Label colorPick2 = new Label(Color.TRANSPARENT.toString());
                    colorPick2.setMaxWidth(Double.MAX_VALUE);
                    colorPick2.setAlignment(Pos.BASELINE_RIGHT);
                    colorPick2.setTextFill(Color.TRANSPARENT);

                    Button useDefaultColor2 = new Button("Transparent");
                    useDefaultColor2.setMaxWidth(Double.MAX_VALUE);

                    useDefaultColor2.setOnAction(event -> {
                        colorPick2.setText(Color.TRANSPARENT.toString());
                        colorPick2.setTextFill(Color.TRANSPARENT);
                        colorPicker2.setValue(Color.TRANSPARENT);
                    });
                    colorPicker2.setOnAction(event -> {
                        colorPick2.setText(colorPicker2.getValue().toString());
                        colorPick2.setTextFill(colorPicker2.getValue());

                        spacer2.setStyle("-fx-background-color: #"+colorPicker2.getValue().toString().substring(2)+"; -fx-border-color: black");
                    });

                    colorBox2.getChildren().addAll(colorLabel2, colorPick2, spacer2, colorPicker2, useDefaultColor2, new Label(""));

                    VBox selectionBox = new VBox();
                    effectsVB.getChildren().add(selectionBox);

                    ToggleGroup radioGroup = new ToggleGroup();
                    RadioButton useEllipse = new RadioButton("Ellipse");
                    RadioButton useRect = new RadioButton("Rectangle");
                    RadioButton useTri = new RadioButton("Triangle");

                    useEllipse.setToggleGroup(radioGroup);
                    useRect.setToggleGroup(radioGroup);
                    useTri.setToggleGroup(radioGroup);

                    useEllipse.setSelected(true);
                    effectStr = "ellipse";

                    useEllipse.setOnAction(event -> effectStr = "ellipse");
                    useRect.setOnAction(event -> effectStr = "rect");
                    useTri.setOnAction(event -> effectStr = "tri");

                    selectionBox.getChildren().addAll(useEllipse, useRect, useTri);

                    temp = effectsVB;
                }
                break;
        }

        funcHolder.getChildren().add(temp);
        funcHolder.setVgrow(temp, Priority.ALWAYS);
    }

    public void enableGridEvents(boolean transp) {
        coordGrid.setMouseTransparent(transp);
        paneCanvas.setMouseTransparent(transp);
    }

    public void print(Boolean defaultMargins) {
        PrinterJob job = PrinterJob.createPrinterJob();
        job.showPrintDialog(stage);

        if(defaultMargins) {
            job.printPage(gridHolder);
        } else {
            PageLayout pageLayout = job.getPrinter().createPageLayout(Paper.A4, PageOrientation.PORTRAIT,Printer.MarginType.HARDWARE_MINIMUM);
            job.printPage(pageLayout, gridHolder);
        }

        job.endJob();
    }


}



/*BorderPane test = new BorderPane();

                Scene scene = new Scene(test, 300, 200);
                Stage importDialog = new Stage();
                importDialog.initModality(Modality.APPLICATION_MODAL);
                importDialog.setScene(scene);
                importDialog.showAndWait();*/

