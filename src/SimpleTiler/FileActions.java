package SimpleTiler;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;

public class FileActions {

    public FileActions() {}

    public void saveMap(File file, TileMap map) {
        try {
            FileOutputStream fileOut = new FileOutputStream(file);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);

            out.writeObject(map);

            out.close();
            fileOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public TileMap openMap(File file) {
        try {
            FileInputStream fis =  new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            TileMap map = (TileMap) ois.readObject();

            ois.close();
            fis.close();

            return map;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void exportMap(String name, TileMap map) {
        new File("./Ports/"+name).mkdirs();

        saveMap(new File("./Ports/"+name+"/"+name+".til"), map);

        ArrayList<String> dependency = new ArrayList<>(map.getTileDependency());
        ArrayList<String> assetDependency = new ArrayList<>(map.getAssetDependency());

        for(String str: dependency) {
            try {
                Files.copy(Paths.get("./Tiles/"+str), Paths.get("./Ports/"+name+"/"+str), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {}
        }

        for(String str: assetDependency) {
            try {
                Files.copy(Paths.get("./Assets/"+str), Paths.get("./Ports/"+name+"/"+str), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {}
        }

    }

    public void importMap(File file, ArrayList<DoubleString> depList) {
        String name = file.getName();
        String folder = name.substring(0, file.getName().length() - 4);

        try {
            Files.copy(Paths.get("./Ports/"+folder+"/"+name), Paths.get("./Maps/"+name), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(depList != null && depList.size()>=1) {
            for(DoubleString ds: depList) {
                if(ds.second != null) {
                    try {
                        Files.copy(Paths.get("./Ports/"+folder+"/"+ds.first), Paths.get((ds.isTile? "./Tiles/" : "./Assets/")+ds.second), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    try {
                        Files.copy(Paths.get("./Ports/"+folder+"/"+ds.first), Paths.get((ds.isTile? "./Tiles/" : "./Assets/")+ds.first), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
