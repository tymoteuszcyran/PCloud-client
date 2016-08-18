package net.elenx.raicoone.repository.pcloud;


import lombok.Data;
import lombok.SneakyThrows;
import net.elenx.raicoone.repository.StorageService;
import net.elenx.raicoone.repository.pcloud.api.PCloudAPI;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Data
public class PCloudService implements StorageService {


    private final PCloudAPI conn;
    private final Hashtable<String, Object> params;


    PCloudService(PCloudAPI conn, Hashtable<String, Object> params) {
        this.conn = conn;
        this.params = params;
    }


    @Override
    @SneakyThrows
    public void put(String filePath, byte[] data) {
        String dirTmp = new File(filePath).getParent();
        String dir = dirTmp.replace("\\", "/") + "/";
        String filename = FilenameUtils.getName(filePath);

        Long parentFolderId = this.createDirectoryTree(dir);

        try (InputStream inputStream = new ByteArrayInputStream(data)) {
            params.put("filename", filename);
            params.put("folderid", parentFolderId);
            conn.sendCommand("uploadfile", params, inputStream);
        }
    }

    @Override
    @SneakyThrows
    public byte[] take(String filePath) {
        URL url = new URL(this.takeLink(filePath));
        InputStream is = url.openStream();
        byte[] data = IOUtils.toByteArray(is);
        is.close();

        return data;
    }

    @Override
    @SneakyThrows
    public void remove(String filePath) {
        params.put("folderid", 0);
        params.put("path", filePath);
        conn.sendCommand("deletefile", params);
    }

    @Override
    @SneakyThrows
    public boolean contains(String filePath) {
        String folderPath = "/" + FilenameUtils.getPath(filePath);
        String filename = FilenameUtils.getName(filePath);
        Set<String> filenames = this.filesIn(folderPath);

        return filenames.contains(filename);
    }

    @Override
    @SneakyThrows
    public Set<String> filesIn(String folderPath) {

        if (folderPath != null && folderPath.length() > 1 && folderPath.charAt(folderPath.length() - 1) == '/')
            folderPath = folderPath.substring(0, folderPath.length() - 1);

        PCloudItemFactory itemFactory = new PCloudItemFactory();

        return itemFactory.allFiles(params, conn, folderPath);
    }

    @Override
    @SneakyThrows
    public Set<String> foldersIn(String folderPath) {
        if (folderPath != null && folderPath.length() > 1 && folderPath.charAt(folderPath.length() - 1) == '/') {
            folderPath = folderPath.substring(0, folderPath.length() - 1);
        }

        PCloudItemFactory itemFactory = new PCloudItemFactory();
        HashMap<String, Long> foldersMap = itemFactory.allFolders(params, conn, folderPath);

        return foldersMap.keySet();
    }

    @Override
    @SneakyThrows
    public void move(String fromFilePath, String toFilePath) {

        params.put("path", fromFilePath);
        params.put("topath", toFilePath);
        conn.sendCommand("copyfile", params);
        this.remove(fromFilePath);

    }

    private String takeLink(String filepath) {
        params.put("path", filepath);
        String host = null;
        String path = null;
        try {
            Hashtable hashtable = (Hashtable) conn.sendCommand("getfilelink", params);
            Object[] hosts = (Object[]) hashtable.get("hosts");
            host = (String) hosts[0];
            path = (String) hashtable.get("path");

        } catch (ClassCastException | IOException | InputMismatchException e) {
            e.printStackTrace();
        }
        return "https://" + host + path;
    }

    @SneakyThrows
    private Long createDirectoryTree(String directoryPath) {
        PCloudItemFactory itemFactory = new PCloudItemFactory();
        Long actualFolderId = 0L;
        if (directoryPath.length() > 1) {
            String[] directories = directoryPath.split("/");
            String actualPath = "";
            for (String folderName : directories) {
                if (folderName.isEmpty()) {
                    continue;
                }

                actualPath += "\\" + folderName;
                boolean isDirectoryExists = itemFactory.isFolderExists(actualPath, params, conn);

                if (isDirectoryExists){
                    Path path = Paths.get(actualPath);
                    String parentPath = path.getParent().toString();
                    parentPath = parentPath.replace("\\", "/");
                    HashMap<String, Long> folders = itemFactory.allFolders(params, conn, parentPath);
                    actualFolderId = folders.get(folderName);
                }else
                    actualFolderId = itemFactory.createFolder(folderName, actualFolderId, params, conn);

            }
        }
        return actualFolderId;
    }

}
