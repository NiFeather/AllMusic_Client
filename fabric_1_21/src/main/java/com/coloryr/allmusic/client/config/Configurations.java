package com.coloryr.allmusic.client.config;

import com.coloryr.allmusic.client.hud.HudConfiguration;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Configurations
{
    public HudConfiguration currentConfig;

    public Configurations(Path path)
    {
        File configFile = new File(path.toFile(), "allmusic.json");
        if (configFile.exists())
        {
            try
            {
                InputStreamReader reader = new InputStreamReader(
                        Files.newInputStream(configFile.toPath()),
                        StandardCharsets.UTF_8);

                BufferedReader bf = new BufferedReader(reader);
                currentConfig = new Gson().fromJson(bf, HudConfiguration.class);
                bf.close();
                reader.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        if (currentConfig == null)
        {
            currentConfig = new HudConfiguration();
            currentConfig.picSize = 50;
            currentConfig.queueSize = 100;
            currentConfig.exitSize = 50;

            try
            {
                String data = new GsonBuilder().setPrettyPrinting()
                        .create()
                        .toJson(currentConfig);

                FileOutputStream out = new FileOutputStream(configFile);
                OutputStreamWriter write = new OutputStreamWriter(out, StandardCharsets.UTF_8);
                write.write(data);
                write.close();
                out.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
