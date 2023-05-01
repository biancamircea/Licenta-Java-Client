package ro.mta.sdk.repository;

import com.google.gson.JsonParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.mta.sdk.FeatureToggle;
import ro.mta.sdk.ToggleSystemConfig;
import ro.mta.sdk.ToggleSystemException;

import java.io.*;
import java.util.Collections;
import java.util.List;


public class ToggleBackupHandler implements BackupHandler<ToggleCollection>{
    private static final Logger LOG = LoggerFactory.getLogger(ToggleBackupHandler.class);

    private final String backupFile;

    public ToggleBackupHandler(ToggleSystemConfig systemConfig) {
        this.backupFile = systemConfig.getBackupFilePath();
    }

    @Override
    public ToggleCollection read() {
        LOG.info("Toggle System will try to load feature toggle states from temporary backup");
        try (FileReader reader = new FileReader(backupFile)) {
            BufferedReader br = new BufferedReader(reader);
            ToggleCollection toggleCollection = JsonToggleParser.fromJson(br);
            return toggleCollection;
        } catch (FileNotFoundException e) {
            LOG.info(
                    " Toggle System could not find the backup-file '"
                            + backupFile
                            + "'. \n"
                            + "This is expected behavior the first time system runs in a new environment.");
        } catch (IOException | IllegalStateException | JsonParseException e) {
            throw new ToggleSystemException("Failed to read backup file: " + backupFile, e);
        }
        List<FeatureToggle> emptyList = Collections.emptyList();
        return new ToggleCollection(emptyList);
    }

    @Override
    public void write(ToggleCollection toggleCollection) {
        try (FileWriter writer = new FileWriter(backupFile)) {
            writer.write(JsonToggleParser.toJsonString(toggleCollection));
        } catch (IOException e) {
            throw new ToggleSystemException("Toggle System was unable to backup feature toggles to file: " + backupFile, e);
        }
    }
}
