package SimpleTiler;

import java.io.Serializable;

public class CanvasElement implements Serializable {
    public String type, info;
    public String[] colors;
    public double[] xy, wh;        //xy: center for circle, top left corner for rect/img/etc
    public int lastScale;
    public double angle;

    public CanvasElement(String type, String info, double[] xy, double[] wh, int lastScale) {
        this.type = type;
        this.info = info;
        this.xy = xy;
        this.wh = wh;
        this.lastScale = lastScale;
        this.angle = 0;
    }

    public CanvasElement(String type, String info, String[] colors, double[] xy, double[] wh, int lastScale) {
        this.type = type;
        this.info = info;
        this.colors = colors;
        this.xy = xy;
        this.wh = wh;
        this.lastScale = lastScale;
        this.angle = 0;
    }

    public CanvasElement(String type, double[] xy, double[] wh, int lastScale) {
        this.type = type;
        this.info = null;
        this.xy = xy;
        this.wh = wh;
        this.lastScale = lastScale;
        this.angle = 0;
    }
}
