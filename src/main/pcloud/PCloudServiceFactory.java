package net.elenx.raicoone.repository.pcloud;


import lombok.SneakyThrows;
import net.elenx.raicoone.repository.pcloud.api.PCloudAPI;

import java.util.Hashtable;

class PCloudServiceFactory {


    @SneakyThrows
    PCloudService createPCloudService(String email, String password){
        PCloudAPI connection = new PCloudAPI(true);
        Hashtable<String, Object> params = new Hashtable<>();
        params.put("username", email);
        params.put("password", password);
        connection.sendCommand("diff", params);

        return new PCloudService(connection, params);
    }
}
