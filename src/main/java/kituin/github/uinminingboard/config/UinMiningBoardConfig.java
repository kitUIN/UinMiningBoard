package kituin.github.uinminingboard.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class UinMiningBoardConfig {
    private static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping().setLenient().setPrettyPrinting()
            .create();

    public String displayName= "挖掘榜";

    public UinMiningBoardConfig()
    {

    }
    public static UinMiningBoardConfig loadConfig() {
        try {
            UinMiningBoardConfig config;
            File configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "UinMiningBoardConfig.json");
            if (configFile.exists()) {
                String json = IOUtils.toString(new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8));
                config = GSON.fromJson(json, UinMiningBoardConfig.class);
            } else {
                config = new UinMiningBoardConfig();
            }
            saveConfig(config);
            return config;
        }
        catch(IOException e) {
            e.printStackTrace();
            return new UinMiningBoardConfig();
        }
    }

    public static void saveConfig(UinMiningBoardConfig config) {
        File configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "UinMiningBoardConfig.json");
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8));
            writer.write(GSON.toJson(config));
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
