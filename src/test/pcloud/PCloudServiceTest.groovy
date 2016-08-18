

import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


class PCloudServiceTest extends Specification {

    void "upload test"(){

        given:
        PCloudServiceFactory factory = new PCloudServiceFactory();
        PCloudService service = factory.createPCloudService("email", "password")


        Path path = Paths.get("dir");
        byte[] data = Files.readAllBytes(path);

        when:
        service.put("", data)

        then:
        noExceptionThrown()

    }

    void "download test"(){

        given:

        PCloudServiceFactory factory = Stub();
        PCloudService service = factory.createPCloudService("email", "password");


        when:
        service.take("a")

        then:
        noExceptionThrown()

    }

    void "contains"(){
        given:
        PCloudServiceFactory factory = new PCloudServiceFactory();
        PCloudService service = factory.createPCloudService("email", "password");

        when:
        def result = service.contains("/Nowe/test.jpg")
        then:
        result;
    }



    void "remove"(){
        given:
        PCloudServiceFactory factory = new PCloudServiceFactory();
        PCloudService service = factory.createPCloudService("email", "password");

        when:
        service.remove("/test.jpg")

        then:
        noExceptionThrown()


    }

    void "files in"(){
        given:
        PCloudServiceFactory factory = new PCloudServiceFactory();
        PCloudService service = factory.createPCloudService("email", "password");

        when:
        service.filesIn("/Nowe")

        then:
        noExceptionThrown()


    }

    void "folders in"(){

        PCloudServiceFactory factory =new PCloudServiceFactory();
        PCloudService service = factory.createPCloudService("email", "password");

        def folders = service.foldersIn("/Nowe")

        expect:
        folders as Set == ["Test"] as Set

    }

    void "move"(){
        given:
        PCloudServiceFactory factory = new PCloudServiceFactory();
        PCloudService service = factory.createPCloudService("email", "password");

        when:
        service.move("/Nowe/test.jpg", "/Nowe/Test/test.jpg")

        then:
        noExceptionThrown()
    }


}
