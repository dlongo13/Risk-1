package fr.fliizweb.risk.Class;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import fr.fliizweb.risk.Class.Prototype.MapFilePrototype;
import fr.fliizweb.risk.Class.Prototype.PlayerPrototype;
import fr.fliizweb.risk.Class.Prototype.UnitPrototype;
import fr.fliizweb.risk.Class.Prototype.ZonePrototype;
import fr.fliizweb.risk.Class.Unit.Unit;

/**
 * Created by rcdsm on 17/05/15.
 */
public final class GameSave {

    private static FileHandle fileCreated;

    private static final String FILE_PATH = "Risk/Partie";
    private static final String FULL_FILE_PATH = FILE_PATH + "/";
    private static final String FILENAME = "new_game";

    public static void newGame(FileHandle src){
        if(!Gdx.files.local(FILE_PATH).exists()){
            Gdx.files.local(FILE_PATH).file().mkdirs(); // On créé le dossier "Partie"
        }

        if(!fileExist()){ // Si aucune partie n'est en cours
            try {
                fileCreated = Gdx.files.local(FULL_FILE_PATH + FILENAME + ".json");
                fileCreated.file().createNewFile(); // On créé la partie
                src.copyTo(fileCreated); // On copie le fichier source dans le fichier de la partie
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else { // Si une partie existe déjà, on la supprime.
            delete(GameSave.getFile());
            try {
                fileCreated = Gdx.files.local(FULL_FILE_PATH + FILENAME + ".json");
                fileCreated.file().createNewFile(); // On créé la partie
                src.copyTo(fileCreated); // On copie le fichier source dans le fichier de la partie
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void delete(FileHandle file){
        // Si le fichier qu'on veut supprimer existe bel et bien
        if(file.exists())
            file.delete();
    }

    public static boolean fileExist(){
        if(Gdx.files.local(FULL_FILE_PATH).list(".json").length > 0){ // Si il existe déjà un fichier .json
            for (FileHandle entry: Gdx.files.local(FULL_FILE_PATH).list())
                fileCreated = Gdx.files.local(entry.toString()); // On récupère la partie
            return true;
        }
        return false;
    }

    public static FileHandle getFile(){
        return fileCreated;
    }

    public static void saveZone(int idZone, String colorZone, ArrayList<Unit> units){
        // On édite la zone du fichier selon l'id correspondant.
        // On remplace sa couleur et les unités qui y sont présentes avec les bonnes valeurs
        // Appeler cette méthode à la fin d'un tap sur une zone

        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        json.setElementType(MapFilePrototype.class, "zones", ZonePrototype.class);
        json.setElementType(ZonePrototype.class, "units", UnitPrototype.class);
        json.setElementType(MapFilePrototype.class, "players", PlayerPrototype.class);

        MapFilePrototype data;
        data = json.fromJson(MapFilePrototype.class, fileCreated);

        ZonePrototype zoneProto = (ZonePrototype) data.zones.get(idZone);
        zoneProto.color = colorZone;

        Zone zone = new Zone();
        zone.setUnits(units);
        zone.getSortedUnits();

        ArrayList listUnits = new ArrayList();
        Hashtable unitsHashtable = zone.getUnitsHashtable();

        for(Object key : unitsHashtable.keySet()) {
            UnitPrototype unitProto = new UnitPrototype();
            unitProto.number = (Integer)unitsHashtable.get(key);
            unitProto.type = key.toString();
            Json newJson = new Json();
            newJson.setOutputType(JsonWriter.OutputType.json);
            String text = newJson.toJson(unitProto, UnitPrototype.class);
            listUnits.add(new Json().fromJson(UnitPrototype.class, text));
        }

        zoneProto.units = listUnits;
        data.zones.set(idZone, zoneProto); // On modifie la zone

        // On écrase la précédente version avec les changements
        fileCreated.writeString(json.prettyPrint(data), false);
    }

}
