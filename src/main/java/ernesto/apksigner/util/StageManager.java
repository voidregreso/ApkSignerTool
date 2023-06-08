package ernesto.apksigner.util;

import javafx.stage.Stage;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Administrador de escena
 */
public class StageManager {

    /**
     * Colección de escenas
     */
    private static Map<String, Stage> stageMap = new ConcurrentHashMap<>();

    /**
     * Almacenar escena por tecla
     *
     * @param key
     * @param stage
     */
    public static void put(String key, Stage stage) {
        if (Utils.isEmpty(key)) {
            throw new RuntimeException("Key is null");
        }
        if (Objects.isNull(stage)) {
            throw new RuntimeException("Scene is null");
        }
        stageMap.put(key, stage);
    }

    /**
     * Obtener escena por tecla
     *
     * @param key
     * @return
     */
    public static Stage getStage(String key) {
        if (Utils.isEmpty(key)) {
            throw new RuntimeException("Key is null");
        }
        return stageMap.get(key);
    }

}