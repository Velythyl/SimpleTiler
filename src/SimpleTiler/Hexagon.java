package SimpleTiler;
//https://stackoverflow.com/questions/27211517/how-can-i-draw-a-hexagon-in-java-scene-builder-2-0

public class Hexagon {
    double[] points;

    public Hexagon(double side){
        side = side/Math.sqrt(3);

        double center = ((Math.sqrt(3)/2)*side);
        points = new double[12];
        //     X                          Y
        points[0] = 0.0;           points[1] = 0.0;
        points[2] = side;          points[3] = 0.0;
        points[4] = side+(side/2); points[5] = center;
        points[6] = side;          points[7] = center * 2;
        points[8] = 0.0;           points[9] = center * 2;
        points[10] = -side/2;      points[11] = center;

    }

    public double[] getPoints(){
        return points;
    }
}
