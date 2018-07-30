package SimpleTiler;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;

public class Controller {
    private boolean isTop;
    private TileMap map;
    private FileActions fileActions;

    public Controller() {
        isTop = true;
        fileActions = new FileActions();
    }

    public void toggleIsTop() {
        isTop = !isTop;
    }

    public boolean getIsTop() {
        return isTop;
    }

    public void toggleHasCoord() { map.toggleCoord(); }

    public boolean getHasCoord() {
        return map.getHasCoord();
    }

    public void setIsTop(boolean isTop) {
        this.isTop = isTop;
    }

    public void setTileMap() {
        this.map = map;
    }

    public TileMap getTileMap() {
        return map;
    }

    public void newTileMap() {
        this.map = new TileMap();
    }

    public int[] getTileMapDim() {
        if(map == null) return new int[]{0,0};
        return map.getDim();
    }

    public String getCoord(int x, int y, boolean isTop) {
        return map.getCoord(x, y, isTop);
    }

    public void setCoord(String newImg, int x, int y) {
        //System.out.println(isTop);
        map.setCoord(newImg, x, y, isTop);
    }

    public void toggleGridLines() {
        map.toggleGridLines();
    }

    public boolean getHasGridLines() {
        if(map == null) return true;
        return map.getHasGridLines();
    }

    public boolean mapExists() {
        return !(map == null);
    }

    public void modColRow(boolean isCol, boolean adding) {
        map.modColRow(isCol, adding);
    }

    public void saveMap(File file) {
        fileActions.saveMap(file, map);
    }

    public void openMap(File file, Main callbacker) {
        map = fileActions.openMap(file);
        if(map == null) {
            callbacker.alertMapIsNull();
        }
    }

    public void exportMap(String name) {
        fileActions.exportMap(name, map);
    }

    public void importMap(File file, ArrayList<DoubleString> tileList) {  //TODO si tileList null
        map.setDependency(tileList);
        fileActions.importMap(file, tileList);
    }

    public HashSet<String> getTileDependency() {
        return map.getTileDependency();
    }

    public HashSet<String> getAssetDependency() { return map.getAssetDependency();}

    public int getScale() {
        return map.getScale();
    }

    public void setScale(int zoom) {
        map.setScale(zoom);
    }

    public void addAsset(CanvasElement e) {
        map.addAsset(e);
    }

    public ArrayList<CanvasElement> getCanvasElements() {
        return map.getCanvasElements();
    }

    public void removeCanvasElement(int i) {
        map.removeCanvasElement(i);
    }

    public void eleToFront(int i) {
        map.eleSwap(i, true, true);
    }

    public void eleToBack(int i) {
        map.eleSwap(i, true, false);
    }

    public void eleUp(int i) {
        map.eleSwap(i, false, true);
    }

    public void eleDown(int i) {
        map.eleSwap(i, false, false);
    }

    public boolean getIsSquare() { return map.getIsSquare(); }

    public void toggleIsSquare() { map.toggleIsSquare(); }
}
