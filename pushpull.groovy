@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7.1')
import org.apache.http.entity.*
import groovyx.net.http.RESTClient
import hudson.model.*

def repo = "maven-releases"

name = "jenkins"
password = "jenkins"

request = new RESTClient("http://epbyminw2473/nexus/repository/")
request.auth.basic("${name}", "${password}")

def parseGAV(){
    def GAVinfo = [:]
    def group = args[1].split("-",3)[0]
    GAVinfo.groupID = group
    def app = args[1].split("-",3)[1]
    GAVinfo.artifactID = app
    def version = args[1].replaceAll("\\D+","")
    GAVinfo.version = version
    GAVinfo
}

println(parseGAV())

def encodeZipFile( Object data ) throws UnsupportedEncodingException {
    def entity = new FileEntity( (File) data, "application/zip" )
    entity.setContentType( "application/zip" )
    return entity
}

def push(GAV) {
    try {
        println("${args[1]}")
        request.encoder.'application/zip' = this.&encodeZipFile
        respons_up = request.put(
                uri: "http://epbyminw2473/nexus/repository/maven-releases/${GAV.groupID}/${GAV.artifactID}/${GAV.version}/${GAV.artifactID}-${GAV.version}.tar.gz",
                body: new File("${args[1]}"),
                requestContentType: 'application/zip'
        )
        assert respons_up.status == 201

    }
    catch (Exception ex) {
        println ex.getMessage()
    }
}

def pull(GAV) {
    try {
        def url = new URL("http://epbyminw2473/nexus/repository/maven-releases/${GAV.groupID}/${GAV.artifactID}/${GAV.version}/${GAV.artifactID}-${GAV.version}.tar.gz")

        def authString = "jenkins:jenkins".getBytes().encodeBase64().toString()
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      
        conn.setRequestProperty( "Authorization", "Basic ${authString}" )
        new File("./${GAV.artifactID}-${GAV.version}.tar.gz").withOutputStream { out ->
            out << conn.inputStream }
    }
    catch (Exception ex) {
        println ex.getMessage()
    }
}

if (args[0] == 'pull'){
    pull(parseGAV())
}
else {
    push(parseGAV())
}