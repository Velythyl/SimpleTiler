package SimpleTiler;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

public class TextShape extends StackPane {
    private final Shape shape;
    private final Label label;

    TextShape(String str, int size, boolean isSquare) {
        label = new Label(str);

        if(isSquare) {
            shape = new Rectangle(size, size);
        } else {
            shape = new Polygon(new Hexagon(size).getPoints());
        }
        shape.setFill(Color.TRANSPARENT);

        getChildren().addAll(shape, this.label);
    }

    public Label getLabel() {
        return label;
    }

    public String getText() {
        return label.getText();
    }

    public Shape getInnerShape() {
        return shape;
    }
}
