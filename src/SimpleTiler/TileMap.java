package SimpleTiler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class TileMap implements Serializable {
    public ArrayList<ArrayList<String>> botMatrix, topMatrix;
    public HashSet<String> tileDependency, assetDependency;
    private ArrayList<CanvasElement> canvasElements;
    public String backgroundImage;
    private String defaut = "transp.png";
    private boolean hasGridLines, hasCoord, isSquare;
    private int size;

    public TileMap() {
        botMatrix = new ArrayList<>();
        for(int i=0; i<10; i++) {
            ArrayList<String> col = new ArrayList<>();
            for(int j=0; j<5; j++) {
                col.add(defaut);
            }
            botMatrix.add(col);
        }
        topMatrix = new ArrayList<>();
        for(int i=0; i<10; i++) {
            ArrayList<String> col = new ArrayList<>();
            for(int j=0; j<5; j++) {
                col.add(defaut);
            }
            topMatrix.add(col);
        }
        tileDependency = new HashSet<>();

        canvasElements = new ArrayList<>();
        assetDependency = new HashSet<>();

        backgroundImage = defaut;
        hasGridLines = true;
        hasCoord = false;
        isSquare = true;
        size = 50;
    }

    public int[] getDim() {
        return new int[]{botMatrix.size(), botMatrix.get(0).size()};
    }

    public String getCoord(int x, int y, boolean isTop) {
        if(isTop) {
            return topMatrix.get(x).get(y);
        } else {
            return botMatrix.get(x).get(y);
        }
    }

    public void setCoord(String newImg, int x, int y, boolean isTop) {
        if(isTop) {
            topMatrix.get(x).set(y, newImg);
        } else {
            botMatrix.get(x).set(y, newImg);
        }

        generateTileDependency();
    }

    public void generateTileDependency() {
        int[] xY = getDim();
        tileDependency = new HashSet<>();

        for(int i=0; i<xY[0]; i++) {
            for(int j=0; j<xY[1]; j++) {
                tileDependency.add(botMatrix.get(i).get(j));
                tileDependency.add(topMatrix.get(i).get(j));
            }
        }

        tileDependency.remove("transp.png");
    }

    public void toggleGridLines() {
        hasGridLines = !hasGridLines;
    }

    public boolean getHasGridLines() {
        return hasGridLines;
    }

    public void toggleCoord() {hasCoord = !hasCoord;}

    public boolean getHasCoord() { return hasCoord;}

    public void modColRow(boolean isCol, boolean adding) {
        int[] xY = getDim();

        if(isCol) {
            if(adding) {

                ArrayList<String> newCol = new ArrayList<>();
                for(int y=0; y<xY[1]; y++) {
                    newCol.add(defaut);
                }
                botMatrix.add(newCol);

                newCol = new ArrayList<>();
                for(int y=0; y<xY[1]; y++) {
                    newCol.add(defaut);
                }
                topMatrix.add(newCol);
            } else if(xY[0]>1){
                botMatrix.remove(xY[0]-1);
                topMatrix.remove(xY[0]-1);
            }

        } else {
            for(int x=0; x<xY[0]; x++) {
                if(adding) {
                    botMatrix.get(x).add(defaut);
                    topMatrix.get(x).add(defaut);
                } else if (xY[1]>1){
                    botMatrix.get(x).remove(xY[1] - 1);
                    topMatrix.get(x).remove(xY[1] - 1);
                }
            }
        }
    }

    public HashSet<String> getTileDependency() {
        return tileDependency;
    }

    public void setDependency(ArrayList<DoubleString> list) {
        ArrayList<DoubleString> tileList = new ArrayList<>();
        ArrayList<DoubleString> assetList = new ArrayList<>();

        for(DoubleString ds: list) {
            if(ds.isTile) {
                tileList.add(ds);
            } else {
                assetList.add(ds);
            }
        }

        setTileDependency(tileList);
        setAssetDependency(assetList);
    }

    public void setTileDependency(ArrayList<DoubleString> tileDependency) {
        int[] xY =  getDim();

        for(int x=0; x<xY[0]; x++) {
            for(int y=0; y<xY[1]; y++) {
                String botElement = botMatrix.get(x).get(y);
                String topElement = topMatrix.get(x).get(y);

                for(int i=0; i<tileDependency.size(); i++) {
                    String secondString = tileDependency.get(i).second;

                    if(botElement.equals(tileDependency.get(i).first) && secondString != null) {
                        botMatrix.get(x).set(y, secondString);
                    }

                    if(topElement.equals(tileDependency.get(i).first) && secondString != null) {
                        topMatrix.get(x).set(y, secondString);
                    }
                }
            }
        }

        this.tileDependency = compareTileDependency(tileDependency);
    }

    public void setAssetDependency(ArrayList<DoubleString> assetDependency) {
        for(CanvasElement ele: canvasElements) {
            for(DoubleString ds: assetDependency) {
                if(ele.type.equals("img") && !ele.info.equals("transp.png")) {
                    if(ele.info.equals(ds.first) && ds.second != null) {
                        ele.info = ds.second;
                    }
                }
            }
        }
    }

    public HashSet<String> compareTileDependency(ArrayList<DoubleString> nextDependency) {
        ArrayList<String> standIn = new ArrayList<>(tileDependency);
        HashSet<String> result = new HashSet<>();

        for(int i=0; i<nextDependency.size(); i++) {
            for(int j=0; j<standIn.size(); j++) {
                if(nextDependency.get(i).first.equals(standIn.get(j))) {
                    if(nextDependency.get(i).second != null) {
                        result.add(nextDependency.get(i).second);
                    } else {
                        result.add(standIn.get(j));
                    }
                } else {
                    result.add(standIn.get(j));
                }
            }
        }

        return result;
    }

    public int getScale() {
        return size;
    }

    public void setScale(int zoom) {
        this.size = zoom;
    }

    public void addAsset(CanvasElement e) {
        if(e.info != null && e.info.equals("transp.png")) return;

        canvasElements.add(e);
        generateAssetDependency();
    }

    public void generateAssetDependency() {
        assetDependency = new HashSet<>();

        for(CanvasElement ele: canvasElements) {
            if(ele.type.equals("img") && ele.info != null) {
                assetDependency.add(ele.info);
            }
        }

        assetDependency.remove("transp.png");
    }

    public ArrayList<CanvasElement> getCanvasElements() {       //TODO canvas dep
        return canvasElements;
    }

    public void removeCanvasElement(int i) {
        canvasElements.remove(i);
        generateAssetDependency();
    }

    public HashSet<String> getAssetDependency() {
        return assetDependency;
    }

    /**
     * Swaps a CanvasElement in CanvasElements.
     *
     * If goingUp: place ele later in the list. Vice-versa.
     *
     * @param index     ele to swap
     * @param full      swapping to index 0/length-1 or just +1/-1?
     * @param goingUp   swapping towards 0 or length-1?
     */
    public void eleSwap(int index, boolean full, boolean goingUp) {
        if(full) {
            CanvasElement ele = canvasElements.get(index);
            canvasElements.remove(index);

            if(goingUp) canvasElements.add(ele);
            else canvasElements.add(0, ele);
        } else {
            int newIndex;
            if(goingUp) newIndex = index+1;
            else newIndex = index-1;

            //handles edge cases
            try{
                Collections.swap(canvasElements, index, newIndex);
            } catch (Exception e) {}
        }

    }

    public boolean getIsSquare() {
        return isSquare;
    }

    public void toggleIsSquare() {
        isSquare = !isSquare;
    }
}
