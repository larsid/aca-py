/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.uefs.larsid.ariesagentclient.main;

import br.uefs.larsid.ariesagentclient.controller.Controller;
import br.uefs.larsid.ariesagentclient.model.AttributeRestriction;
import br.uefs.larsid.ariesagentclient.model.Credential;
import br.uefs.larsid.ariesagentclient.model.CredentialDefinition;
import br.uefs.larsid.ariesagentclient.model.Invitation;
import br.uefs.larsid.ariesagentclient.model.Schema;
import br.uefs.larsid.ariesagentclient.util.File;
import br.uefs.larsid.ariesagentclient.util.TimeRegister;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.zxing.WriterException;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import org.hyperledger.aries.api.connection.ConnectionRecord;
import org.hyperledger.aries.api.connection.CreateInvitationResponse;
import org.hyperledger.aries.api.present_proof.PresentationExchangeRecord;
import org.hyperledger.aries.api.present_proof.PresentationExchangeState;
import org.hyperledger.aries.api.schema.SchemaSendResponse;

/**
 *
 * @author Emers
 */
public class CLI {

    public static void main(String[] args) throws IOException, WriterException, InterruptedException {
        final String AGENT_ADDR = "localhost";
        final String AGENT_PORT = "8021";

        Controller controller = new Controller(AGENT_ADDR, AGENT_PORT);

        System.out.println("\nEnd Point: "+controller.getEndPoint()+"\n");
        /*Base to create issuer class*/
 /*Criar uma classe para servir de base para implementação de credenciais especificas*/
 /*Implementar demais métodos, verificação, recepção, ...*/
        int idConvite = controller.getConnections().size();
        int idSchema = controller.getSchemasCreated().size() + 11; //precisa automatizar o número baseado na persistencia
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

        //criando solicitação de prova
        String name = "Prove que você é aluno";
        String comment = "Você é um aluno?";
        String version = "1.0";
        String nameAttrRestriction = "nome";
        String nameRestriction = "cred_def_id";
        String propertyRestriction = "JU1jTydsRztc8XvjPHboAn:3:CL:63882:tag_1";

        AttributeRestriction attributeRestriction = new AttributeRestriction(nameAttrRestriction, nameRestriction, propertyRestriction);
        List<AttributeRestriction> attributesRestrictions = new ArrayList<>();
        attributesRestrictions.add(attributeRestriction);

        final Scanner scan = new Scanner(System.in);

        boolean menuControl = true;

        do {
            System.out.println("Menu Aries Agent\n");
            System.out.println("1  - Gerar url de Conexão");
            System.out.println("2  - Criar Definição de credencial");
            System.out.println("3  - Emitir Credencial");
            System.out.println("4  - Exibir Schemas Criados");
            System.out.println("5  - Listar Conexões");
            System.out.println("6  - Listar Definições de credenciais");
            System.out.println("7  - Solicitar prova de credenciais");
            System.out.println("8 - Receber Convite de Conexão");
            System.out.println("9 - Listar Credenciais Recebidas");
            System.out.println("10 - Gerar arquivo com tempos de verificação de prova");
            System.out.println("0  - Exit\n");

            switch (scan.nextInt()) {
                case 1: //Cria um convite de conexão
                    createInvitation(controller, ("Convite_" + idConvite++));
                    break;
                case 2: //Cria uma definição de credencial
                    createCredentialDefinition(controller, schema, credentialDefinition);
                    break;
                case 3: // Envia uma credencial pelo método V 1.0
                    issueCredentialV1(controller);
                    break;
                case 4: //Lista os id dos schemas criados
                    listSchemas(controller);
                    break;
                case 5: //Lista as conexões realizadas
                    listConnections(controller);
                    break;
                case 6:
                    listCredentialDefinitionsCreated(controller);
                    break;
                case 7:
                    sendRequestPresentationRequest(controller);
                    break;
                case 8:
                    receiveInvitation(controller);
                    break;
                case 9:

                    break;
                case 10:
                    sendRequestPresentationRequests(controller);
                    break;
                case 11:
                    testTimeRegister();
                    break;
                case 0:
                    menuControl = false;
                    break;
                default:
                    break;
            }
        } while (menuControl);
    }

    public static void createInvitation(Controller controller, String label) throws IOException, WriterException {
        System.out.println("\nCriando convite de conexão ...");

        CreateInvitationResponse createInvitationResponse = controller.createInvitation(label);

        String url = controller.getURLInvitation(createInvitationResponse);

        System.out.println("\nUrl: " + url);

        String json = controller.getJsonInvitation(createInvitationResponse);

        System.out.println("Json Invitation: " + json);

        System.out.println("\nGerando QR Code ...");

        controller.generateQRCodeInvitation(createInvitationResponse);

        System.out.print("\nConvite Criado!\n");
    }

    private static void listCredentialDefinitionsCreated(Controller controller) throws IOException {
        System.out.println("Consultando IDs de definições de credenciais ...");

        List<String> crendentialsDefinitionIds = controller.getCredentialDefinitionsCreated().getCredentialDefinitionIds();

        System.out.println("\nListando definições de credenciais ...\n");

        for (int i = 0; i < crendentialsDefinitionIds.size(); i++) {
            System.out.println("Number: " + i);
            System.out.println(controller.getCredentialDefinitionById(crendentialsDefinitionIds.get(i)) + "\n");
        }

        System.out.println("\nFim da lista de definições de credenciais!\n");
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

    private static void issueCredentialV1(Controller controller) throws IOException {
        Scanner scan = new Scanner(System.in);

        //listando definições de credenciais
        listCredentialDefinitionsCreated(controller);

        System.out.println("Número da definição de credencial: ");
        int numberCredentialDefinition = scan.nextInt();

        String credentialDefinitionId = controller.getCredentialDefinitionsCreated().getCredentialDefinitionIds().get(numberCredentialDefinition);
        CredentialDefinition credentialDefinition = controller.getCredentialDefinitionById(credentialDefinitionId);

        //Coletando valores dos atributos
        System.out.println("\nInforme ...");

        Map<String, String> values = new HashMap<>();

        for (String attr : credentialDefinition.getSchema().getAttributes()) {
            System.out.print(attr + ": ");
            values.put(attr, scan.next());
        }

        //criando credencial
        Boolean autoRemove = false;
        Credential credential = new Credential(credentialDefinition, autoRemove);
        credential.addValues(values);

        //listando conexões
        listConnections(controller);

        System.out.println("Número da conexão: ");
        int numberConnection = scan.nextInt();
        ConnectionRecord connectionRecord = controller.getConnections().get(numberConnection);

        //emitindo credenciais
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

        for (int i = 0; i < connectionsRecords.size(); i++) {
            System.out.println("\nNumber: " + i);
            System.out.println("\nConexão ID: " + connectionsRecords.get(i).getConnectionId());
            System.out.println("State: " + connectionsRecords.get(i).getState());
            System.out.println("RFC State: " + connectionsRecords.get(i).getRfc23Sate());
            System.out.println("Alias: " + connectionsRecords.get(i).getAlias());
            System.out.println("Invitation Key: " + connectionsRecords.get(i).getInvitationKey());
            System.out.println("Their Label: " + connectionsRecords.get(i).getTheirLabel());
            System.out.println("Their DID: " + connectionsRecords.get(i).getTheirDid());
            System.out.println("Created At: " + connectionsRecords.get(i).getCreatedAt());
            System.out.println("Updated At: " + connectionsRecords.get(i).getUpdatedAt());
            System.out.println("Msg error: " + connectionsRecords.get(i).getErrorMsg());
        }

        System.out.println("\nFim da lista de conexões!\n");
    }

    private static void sendRequestPresentationRequest(Controller controller) throws IOException, InterruptedException {
        Scanner scan = new Scanner(System.in);
        String name = "Prove que você é você";
        String comment = "Essa é uma verificação de prova do larsid";
        String version = "1.0";
        String nameAttrRestriction = "";
        String nameRestriction = "cred_def_id";
        String propertyRestriction = "";

        //listando definições de credenciais
        listCredentialDefinitionsCreated(controller);
        System.out.println("Número da definição de credencial: ");
        int numberCredentialDefinition = scan.nextInt();

        CredentialDefinition credentialDefinition = controller.getCredentialDefinitionById(
                controller.getCredentialDefinitionsCreated().getCredentialDefinitionIds().get(numberCredentialDefinition));

        nameAttrRestriction = credentialDefinition.getSchema().getAttributes().get(0);
        propertyRestriction = credentialDefinition.getId();

        AttributeRestriction attributeRestriction = new AttributeRestriction(nameAttrRestriction, nameRestriction, propertyRestriction);
        List<AttributeRestriction> attributesRestrictions = new ArrayList<>();
        attributesRestrictions.add(attributeRestriction);

        //listando conexões
        listConnections(controller);
        System.out.println("Número da conexão: ");
        int numberConnection = scan.nextInt();
        ConnectionRecord connectionRecord = controller.getConnections().get(numberConnection);

        //Guardando timestamp do inicio da solicitação de prova
        Timestamp timeSend = new Timestamp(System.currentTimeMillis());

        String presentationExchangeId = controller.sendRequestPresentationRequest(name, comment, version, connectionRecord.getConnectionId(), attributesRestrictions);

        System.out.println("\nEnviando solicitação de prova ...");

        PresentationExchangeRecord presentationExchangeRecord;

        do {
            presentationExchangeRecord = controller.getPresentation(presentationExchangeId);
            System.out.println("UpdateAt: " + presentationExchangeRecord.getUpdatedAt());
            System.out.println("Presentation: " + presentationExchangeRecord.getPresentation());
            System.out.println("Verificada: " + presentationExchangeRecord.isVerified());
            System.out.println("State: " + presentationExchangeRecord.getState());
            System.out.println("Auto Presentation: " + presentationExchangeRecord.getAutoPresent());
            //Thread.sleep(2 * 1000);
        } while (!presentationExchangeRecord.getState().equals(PresentationExchangeState.REQUEST_RECEIVED) && !presentationExchangeRecord.getState().equals(PresentationExchangeState.VERIFIED));

        System.out.println("\nSolicitação de prova recebida!\n");

        verifyProofPresentation(controller, presentationExchangeId);

        System.out.println("\nCalculando time stamp ...\n");

        Timestamp timeReceive = new Timestamp(System.currentTimeMillis());
        System.out.println("Calculando time stamp ...");
        System.out.println("Tempo Inicial: " + timeSend);
        System.out.println("Tempo Final: " + timeReceive);
        System.out.println("Diferença: " + (timeReceive.getTime() - timeSend.getTime()));
    }

    private static void verifyProofPresentation(Controller controller, String presentationExchangeId) throws IOException, InterruptedException {
        System.out.println("\nVerificando solicitação de prova ...");

        //Thread.sleep(5 * 1000);
        if (controller.getPresentation(presentationExchangeId).getVerified()) {
            System.out.println("\nCredencial verificada!\n");
        } else {
            System.err.println("\nCredencial não verificada!\n");
        }
    }

    private static void receiveInvitation(Controller controller) throws IOException {
        Scanner scan = new Scanner(System.in);
        System.out.println("\nJSON de conexão:");
        String invitation = scan.nextLine();

        JsonObject invitationJson = new Gson().fromJson(invitation, JsonObject.class);
        Invitation invitationObj = new Invitation(invitationJson);

        System.out.println("\nRecebendo convite de conexão ...");

        ConnectionRecord connectionRecord = controller.receiveInvitation(invitationObj);

        System.out.println("\nConexão:\n" + connectionRecord.toString());
    }

    private static void saveTimeRegister(String data) throws IOException {
        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH'h'mm'min'ss's'"));
        File.write("times_"+dateTime, "json", data);
    }

    private static void testTimeRegister() throws InterruptedException, IOException {
        Timestamp t1 = new Timestamp(System.currentTimeMillis());
        Thread.sleep(2 * 1000);
        Timestamp t2 = new Timestamp(System.currentTimeMillis());

        TimeRegister tr = new TimeRegister(t1, t2);

        JsonArray timeRegisters = new JsonArray();
        timeRegisters.add(tr.getJson());

        saveTimeRegister(timeRegisters.toString());
    }

    private static void sendRequestPresentationRequests(Controller controller) throws IOException, InterruptedException {
        Scanner scan = new Scanner(System.in);
        String name = "Prove que você é você";
        String comment = "Essa é uma verificação de prova do larsid";
        String version = "1.0";
        String nameAttrRestriction = "";
        String nameRestriction = "cred_def_id";
        String propertyRestriction = "";

        //listando definições de credenciais
        listCredentialDefinitionsCreated(controller);
        System.out.println("Número da definição de credencial: ");
        int numberCredentialDefinition = scan.nextInt();

        CredentialDefinition credentialDefinition = controller.getCredentialDefinitionById(
                controller.getCredentialDefinitionsCreated().getCredentialDefinitionIds().get(numberCredentialDefinition));

        nameAttrRestriction = credentialDefinition.getSchema().getAttributes().get(0);
        propertyRestriction = credentialDefinition.getId();

        AttributeRestriction attributeRestriction = new AttributeRestriction(nameAttrRestriction, nameRestriction, propertyRestriction);
        List<AttributeRestriction> attributesRestrictions = new ArrayList<>();
        attributesRestrictions.add(attributeRestriction);

        //listando conexões
        listConnections(controller);
        System.out.println("Número da conexão: ");
        int numberConnection = scan.nextInt();
        ConnectionRecord connectionRecord = controller.getConnections().get(numberConnection);

        //Guardando timestamp do inicio da solicitação de prova
        Timestamp timeSend = null;
        Timestamp timeReceive = null;
        String presentationExchangeId = null;
        JsonArray timeRegisters = new JsonArray();

        System.out.print("Numero de verificações de provas: ");
        int limit = scan.nextInt();

        System.out.println("\nEnviando " + limit + " solicitações de prova ...");

        for (int i = 0; i < limit; i++) {
            timeSend = new Timestamp(System.currentTimeMillis());
            presentationExchangeId = controller.sendRequestPresentationRequest(name, comment, version, connectionRecord.getConnectionId(), attributesRestrictions);

            PresentationExchangeRecord presentationExchangeRecord;

            do {
                presentationExchangeRecord = controller.getPresentation(presentationExchangeId);
            } while (!presentationExchangeRecord.getState().equals(PresentationExchangeState.REQUEST_RECEIVED) && !presentationExchangeRecord.getState().equals(PresentationExchangeState.VERIFIED));

            verifyProofPresentation(controller, presentationExchangeId);

            timeReceive = new Timestamp(System.currentTimeMillis());
            
            timeRegisters.add(new TimeRegister(timeSend, timeReceive).getJson());
            Thread.sleep(10 * 1000); //evitar cache
        }
        
        System.out.println("\n" + limit + " solicitações de prova enviadas!");
        System.out.println("\nSalvando " + limit + " solicitações de provas ...");
        saveTimeRegister(timeRegisters.toString());
        System.out.println("\n" + limit + " solicitações de prova salvas!");

    }

}
