package kituin.github.uinminingboard.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static kituin.github.uinminingboard.UinMiningBoard.LOGGER;

public class FileData {
    private static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping().setLenient().setPrettyPrinting()
            .create();
    private final HashMap<String, String> items;
    private final String fileName;

    public FileData(HashMap<String, String> items, String name) {
        this.items = items;
        this.fileName = name;
    }

    public static FileData load(String name) {
        try {
            FileData fileData;
            File dataFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), name + ".json");
            if (dataFile.exists()) {
                String json = IOUtils.toString(new InputStreamReader(new FileInputStream(dataFile), StandardCharsets.UTF_8));
                fileData = new FileData(json2Map(json), name);
            } else {
                fileData = new FileData(new HashMap<>(), name);
            }
            save(fileData, name);
            return fileData;
        } catch (IOException e) {
            e.printStackTrace();
            return new FileData(new HashMap<>(), name);
        }
    }

    public static void save(FileData fileData, String name) {
        File dataFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), name + ".json");
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dataFile), StandardCharsets.UTF_8));
            writer.write(map2Json(fileData.getItems()));
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static HashMap json2Map(String jsonString) {
        return GSON.fromJson(jsonString, HashMap.class);
    }

    public static String map2Json(HashMap<String, String> map) {
        return GSON.toJson(map);
    }

    public List<Map.Entry<String, String>> sort() {
        List<Map.Entry<String, String>> entryList2 = new ArrayList<>(items.entrySet());
        entryList2.sort((me1, me2) -> me2.getValue().compareTo(me1.getValue()));
        return entryList2.subList(0, Math.min(Math.max(entryList2.size() - 1, 0), 15));
    }

    public HashMap<String, String> getItems() {
        return items;
    }

    public void addItem(String key, String value) {
        items.put(key, value);
        save(this, this.fileName);
    }

    public void removeItem(String key) {
        if (containsKey(key)) {
            items.remove(key);
            save(this, this.fileName);
        }
    }

    public String getItem(String key) {
        if (items.containsKey(key)) {
            return items.get(key);
        } else {
            LOGGER.error("无法找到UUID:" + key + "的玩家名称");
            return null;
        }
    }

    public String getItemOrDefault(String key) {
        return items.getOrDefault(key, key);
    }

    public Boolean containsKey(String key) {
        return items.containsKey(key);
    }

    public Boolean containsValue(String value) {
        return items.containsValue(value);
    }

    public String getFileName() {
        return fileName;
    }
}
