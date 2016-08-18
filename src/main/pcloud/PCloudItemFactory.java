package net.elenx.raicoone.repository.pcloud;


import lombok.SneakyThrows;
import net.elenx.raicoone.repository.pcloud.api.PCloudAPI;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

class PCloudItemFactory {


    Set<String> allFiles(Hashtable<String, Object> params, PCloudAPI conn, String folderPath) {
        Set<String> filenames = new HashSet<>();

        try {
            params.put("path", folderPath);

            Hashtable res = (Hashtable) conn.sendCommand("listfolder", params);
            Object metadataO = res.get("metadata");
            if (metadataO != null) {
                Hashtable metadata = (Hashtable) metadataO;
                Object[] content = (Object[]) metadata.get("contents");

                if (content != null) {
                    for (Object entry : content) {
                        Hashtable hash = (Hashtable) entry;
                        boolean isFolder = (boolean) hash.get("isfolder");
                        if (!isFolder) {
                            Hashtable stringHash = (Hashtable) entry;
                            String filename = (String) stringHash.get("name");
                            filenames.add(filename);
                        }
                    }
                }
            }

        } catch (ClassCastException | InputMismatchException | IOException e) {
            //// TODO: 14.08.2016 implement catch block
        }

        return filenames;

    }


    HashMap<String, Long> allFolders(Hashtable<String, Object> params, PCloudAPI conn, String folderPath) {
        HashMap<String, Long> folders = new HashMap<>();

        try {
            params.put("path", folderPath);
            Hashtable res = (Hashtable) conn.sendCommand("listfolder", params);
            Object metadataO = res.get("metadata");
            if (metadataO != null) {
                Hashtable metadata = (Hashtable) metadataO;
                Object[] content = (Object[]) metadata.get("contents");

                if (content != null) {
                    for (Object entry : content) {
                        Hashtable hash = (Hashtable) entry;
                        boolean isFolder = (boolean) hash.get("isfolder");
                        if (isFolder) {
                            Hashtable stringHash = (Hashtable) entry;
                            String folderName = (String) stringHash.get("name");
                            Long folderId = (Long) stringHash.get("folderid");
                            folders.put(folderName, folderId);
                        }
                    }
                }
            }

        } catch (ClassCastException | InputMismatchException | IOException e) {
            //// TODO: 14.08.2016 implement catch block
        }

        return folders;

    }

    @SneakyThrows
    Long createFolder(String name, Long dirFolderId, Hashtable<String, Object> params, PCloudAPI connection) {
        Long folderId = 0L;
        params.put("folderid", dirFolderId);
        params.put("name", name);

        try {
            Hashtable res = (Hashtable) connection.sendCommand("createfolder", params);
            Object metadataO = res.get("metadata");
            if(metadataO != null){
                Hashtable metadata = (Hashtable) res.get("metadata");
                folderId = (Long) metadata.get("folderid");
            }

        } catch (ClassCastException | InputMismatchException e) {
            //// TODO: 14.08.2016 implement catch block
        }

        return folderId;
    }

    boolean isFolderExists(String folderPath, Hashtable<String, Object> params, PCloudAPI connection){
        boolean isExists = false;
        Path path = Paths.get(folderPath);
        String folderName = path.getFileName().toString();
        String parentPath = path.getParent().toString();
        parentPath = parentPath.replace("\\", "/");

        HashMap<String, Long> allFolders = this.allFolders(params, connection, parentPath);
        if(allFolders.containsKey(folderName))
            isExists = true;


        return isExists;
    }


}
