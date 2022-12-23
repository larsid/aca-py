/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.uefs.larsid.ariesagentclient.main;

import br.uefs.larsid.ariesagentclient.controller.Controller;
import br.uefs.larsid.ariesagentclient.model.Credential;
import br.uefs.larsid.ariesagentclient.model.CredentialDefinition;
import br.uefs.larsid.ariesagentclient.model.Schema;
import br.uefs.larsid.ariesagentclient.mqtt.Listener;
import br.uefs.larsid.ariesagentclient.mqtt.MQTTClient;
import br.uefs.larsid.ariesagentclient.util.CLI;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.zxing.WriterException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import org.hyperledger.aries.api.connection.ConnectionRecord;
import org.hyperledger.aries.api.connection.CreateInvitationResponse;
import org.hyperledger.aries.api.schema.SchemaSendResponse;

/**
 *
 * @author Emers
 */
public class Main {

    public static boolean DEBUG_MODE = true;

    /* -------------------------- Topic constants ---------------------------- */
    private static final String CREATE_INVITATION = "POST CREATE_INVITATION";
    private static final String ACCEPT_INVITATION = "POST ACCEPT_INVITATION";
    private static final String CREDENTIAL_DEFINITIONS = "POST CREDENTIAL_DEFINITIONS";
    private static final String ISSUE_CREDENTIAL = "POST ISSUE_CREDENTIAL";
    /* ---------------------------------------------------------------------- */

    /* -------------------------- MQTT constants ---------------------------- */
    private static final int MQTT_QOS = 1;
    /* ---------------------------------------------------------------------- */

    /* ---------------------- Controller properties ------------------------- */
    private static String endpoint;
    private static String credentialDefinitionId;
    private static String brokerIp;
    private static String brokerPort;
    private static String brokerUsername;
    private static String brokerPassword;
    private static String agentIp;
    private static String agentPort;
    /* ---------------------------------------------------------------------- */

    public static Controller controller;
    private static MQTTClient mqttClient;

    public static void main(String[] args) throws IOException, WriterException {
        readProperties(args);

        mqttClient = new MQTTClient(DEBUG_MODE, brokerIp, brokerPort, brokerUsername, brokerPassword);

        mqttClient.connect();

        String[] topics = {
                CREATE_INVITATION,
                ACCEPT_INVITATION,
                CREDENTIAL_DEFINITIONS,
                ISSUE_CREDENTIAL
        };

        new Listener(
                mqttClient,
                topics,
                MQTT_QOS,
                DEBUG_MODE);

        // final String AGENT_ADDR = "localhost";
        // final String AGENT_PORT = "8021";
        // final String AGENT_END_POINT = "https://2f8e-177-99-172-106.sa.ngrok.io";

        controller = new Controller(agentIp, agentPort);

        /*Base to create issuer class*/
        
        /*Criar uma classe para servir de base para implementação de credenciais especificas*/
        
        /*Implementar demais métodos, verificação, recepção, ...*/
        
        int idSchema = controller.getSchemasCreated().size()+10; //precisa automatizar o número baseado na persistencia
        int idTag = 1;

        List<String> attributes = new ArrayList<>();
        attributes.add("nome");
        attributes.add("email");
        attributes.add("matricula");

        Schema schema = new Schema(("Schema_" + idSchema), (idSchema++ + ".0"));
        schema.addAttributes(attributes);

        Boolean revocable = false;
        int revocableSize = 1000;
        CredentialDefinition credentialDefinition = new CredentialDefinition(("tag_" + idTag++), revocable, 1000, schema);

        Boolean autoRemove = false;
        Credential credential = new Credential(credentialDefinition, autoRemove);
        Map<String, String> values = new HashMap<>();
        values.put("nome", "fulano");
        values.put("email", "fulano@gmail.com");
        values.put("matricula", "12345");
        credential.addValues(values);

        // final Scanner scan = new Scanner(System.in);

        // boolean menuControl = true;

        // do {
        //     System.out.println("Menu Aries Agent\n");
        //     System.out.println("1  - Gerar url de Conexão");
        //     System.out.println("2  - Criar Definição de credencial");
        //     System.out.println("3  - Emitir Credencial");
        //     System.out.println("4  - Exibir Schemas Criados");
        //     System.out.println("5  - Exibir schema por ID");
        //     System.out.println("6  - Listar Conexões");
        //     System.out.println("7  - Listar Definições de credenciais");
        //     System.out.println("8  - Solicitar prova de credenciais");
        //     System.out.println("9  - Verificar apresentação de prova de credenciais");
        //     System.out.println("10 - Aceitar Conexão");
        //     System.out.println("11 - Listar Credenciais Recebidas");
        //     System.out.println("12 - Revogar Credencial Emitida");
        //     System.out.println("0  - Exit\n");

        //     switch (scan.nextInt()) {
        //         case 1: //Cria um convite de conexão
        //             createInvitation(controller, ("Convite_" + idConvite++));
        //             break;
        //         case 2: //Cria uma definição de credencial
        //             createCredentialDefinition(controller, schema, credentialDefinition);
        //             break;
        //         case 3: // Envia uma credencial pelo método V 1.0
        //             ConnectionRecord connectionRecord = controller.getConnections().get(0);
        //             issueCredentialV1(controller, credential, connectionRecord);
        //             break;
        //         case 4: //Lista os id dos schemas criados
        //             listSchemas(controller);
        //             break;
        //         case 5: //Lista os schemas através de ID
        //             listSchemaById(controller, schema.getId());
        //             break;
        //         case 6: //Lista as conexões realizadas
        //             listConnections(controller);
        //             break;
        //         case 7:

        //             break;
        //         case 8:

        //             break;
        //         case 9:

        //             break;
        //         case 10:

        //             break;
        //         case 11:

        //             break;
        //         case 12:

        //             break;
        //         case 0:
        //             menuControl = false;
        //             break;
        //         default:
        //             break;
        //     }
        // } while (menuControl);
    }

    /**
     * Realiza leitura das propriedades passadas por parâmetro ou resgata 
     * valores presentes no arquivo de propriedade. 
     * 
     * @param args String[] - Dados passados na execução do projeto.
     */
    public static void readProperties(String[] args) {
        try (InputStream input = AriesAgent.class.getResourceAsStream("controller.properties")) {
            if (input == null) {
                printlnDebug("Sorry, unable to find controller.properties.");
                return;
            }
            Properties props = new Properties();
            props.load(input);

            endpoint = CLI.getEndpoint(args)
                    .orElse(props.getProperty("endpoint"));

            credentialDefinitionId = CLI.getCredentialDefinitionId(args)
                    .orElse(props.getProperty("credentialDefinitionId"));

            brokerIp = CLI.getBrokerIp(args)
                    .orElse(props.getProperty("brokerIp"));

            brokerPort = CLI.getBrokerPort(args)
                    .orElse(props.getProperty("brokerPort"));

            brokerPassword = CLI.getBrokerPassword(args)
                    .orElse(props.getProperty("brokerPassword"));

            brokerUsername = CLI.getBrokerUsername(args)
                    .orElse(props.getProperty("brokerUsername"));

            agentIp = CLI.getAgentIp(args)
                    .orElse(props.getProperty("agentIp"));

            agentPort = CLI.getAgentPort(args)
                    .orElse(props.getProperty("agentPort"));

        } catch (IOException ex) {
            printlnDebug("Sorry, unable to find sensors.json or not create pesistence file.");
        }
    }

    public static JsonObject createInvitation(String nodeUri) throws IOException, WriterException {
        int idConvite = controller.getConnections().size();
        return createInvitation(controller, ("Convite_" + idConvite++), nodeUri);
    }

    public static JsonObject createInvitation(Controller controller, String label, String nodeUri) throws IOException, WriterException {
        System.out.println("\nCriando convite de conexão ...");

        CreateInvitationResponse createInvitationResponse = controller.createInvitation(label);

        String url = controller.getURLInvitation(createInvitationResponse);

        System.out.println("\nUrl: " + url);

        String json = controller.getJsonInvitation(createInvitationResponse);

        System.out.println("Json Invitation: " + json);

        // System.out.println("\nGerando QR Code ...");

        // controller.generateQRCodeInvitation(createInvitationResponse);

        System.out.print("\nConvite Criado!\n");

        JsonObject jsonInvitation = new Gson().fromJson(json, JsonObject.class);

        jsonInvitation.addProperty("connectionId", createInvitationResponse.getConnectionId());
        jsonInvitation.addProperty("nodeUri", nodeUri);

        printlnDebug("Final JSON: " + jsonInvitation.toString());

        return jsonInvitation;
    }

    private static void createCredentialDefinition(Controller controller, Schema schema, CredentialDefinition credentialDefinition) throws IOException {
        System.out.println("\nCriando Schema ...");

        SchemaSendResponse schemaSendResponse = controller.createSchema(schema);
        schema.setId(schemaSendResponse.getSchemaId());

        System.out.println("\nSchema ID: " + schema.getId());

        System.out.println("\nSchema Criado!");

        System.out.println("\nCriando definição de credencial ...");

        controller.createCredendentialDefinition(credentialDefinition);

        System.out.println("\nDefinição de Credencial ID: " + credentialDefinition.getId());

        System.out.println("\nDefinição de Credencial Criada!\n");
    }

    private static void issueCredentialV1(Controller controller, Credential credential, ConnectionRecord connectionRecord) throws IOException {
        System.out.println("\nEmitindo Credencial ...");

        controller.issueCredentialV1(connectionRecord.getConnectionId(), credential);

        System.out.println("Credencial ID: " + credential.getId());

        System.out.println("\nCredencial Emitinda!\n");
    }

    private static void listSchemas(Controller controller) throws IOException {
        System.out.println("\nConsultando schemas ...");

        List<String> schemas = controller.getSchemasCreated();

        System.out.println("\nListando schemas ...");

        for (String schema : schemas) {
            System.out.println("Schema: " + schema);
        }

        System.out.println("\nFim da lista de schemas!\n");
    }

    private static void listSchemaById(Controller controller, String schemaId) throws IOException {
        System.out.println("\nConsultando schemas ...");

        SchemaSendResponse.Schema schemaResponse = controller.getSchemaById(schemaId);

        System.out.println("\nListando schema ...");

        System.out.println("Name: " + schemaResponse.getName());
        System.out.println("Version: " + schemaResponse.getVersion());
        System.out.println("Attributes: " + schemaResponse.getAttrNames());

        System.out.println("\nFim da lista de schemas!\n");

    }

    private static void listConnections(Controller controller) throws IOException {
        System.out.println("\nConsultando conexões ...");

        List<ConnectionRecord> connectionsRecords = controller.getConnections();

        System.out.println("\nListando conexões...");
        for (ConnectionRecord connectionRecord : connectionsRecords) {
            System.out.println("\nConexão ID: " + connectionRecord.getConnectionId());
            System.out.println("State: " + connectionRecord.getState());
            System.out.println("RFC State: " + connectionRecord.getRfc23Sate());
            System.out.println("Alias: " + connectionRecord.getAlias());
            System.out.println("Invitation Key: " + connectionRecord.getInvitationKey());
            System.out.println("Their Label: " + connectionRecord.getTheirLabel());
            System.out.println("Their DID: " + connectionRecord.getTheirDid());
            System.out.println("Created At: " + connectionRecord.getCreatedAt());
            System.out.println("Updated At: " + connectionRecord.getUpdatedAt());
            System.out.println("Msg error: " + connectionRecord.getErrorMsg());
        }

        System.out.println("\nFim da lista de conexões!\n");
    }

    private static void printlnDebug(String str) {
        if (isDebugModeValue()) {
            System.out.println(str);
        }
    }

    public static boolean isDebugModeValue() {
        return DEBUG_MODE;
    }

}
