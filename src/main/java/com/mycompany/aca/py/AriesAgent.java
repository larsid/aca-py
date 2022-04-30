/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.aca.py;

import com.google.gson.JsonObject;
import com.google.zxing.WriterException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import org.hyperledger.aries.AriesClient;
import org.hyperledger.aries.api.connection.ConnectionAcceptInvitationFilter;
import org.hyperledger.aries.api.connection.ConnectionAcceptRequestFilter;
import org.hyperledger.aries.api.connection.ConnectionReceiveInvitationFilter;
import org.hyperledger.aries.api.connection.ConnectionRecord;
import org.hyperledger.aries.api.connection.CreateInvitationRequest;
import org.hyperledger.aries.api.connection.CreateInvitationResponse;
import org.hyperledger.aries.api.connection.ReceiveInvitationRequest;
import org.hyperledger.aries.api.credential_definition.CredentialDefinition;
import org.hyperledger.aries.api.credential_definition.CredentialDefinition.CredentialDefinitionRequest;
import org.hyperledger.aries.api.credential_definition.CredentialDefinition.CredentialDefinitionResponse;
import org.hyperledger.aries.api.credential_definition.CredentialDefinitionFilter;
import org.hyperledger.aries.api.credentials.CredentialAttributes;
import org.hyperledger.aries.api.credentials.CredentialPreview;
import org.hyperledger.aries.api.issue_credential_v1.CredentialExchangeRole;
import org.hyperledger.aries.api.issue_credential_v1.CredentialExchangeState;
import org.hyperledger.aries.api.issue_credential_v1.IssueCredentialRecordsFilter;
import org.hyperledger.aries.api.issue_credential_v1.V1CredentialExchange;
import org.hyperledger.aries.api.issue_credential_v1.V1CredentialProposalRequest;
import org.hyperledger.aries.api.issue_credential_v1.V1CredentialStoreRequest;
import org.hyperledger.aries.api.ledger.TAAAccept;
import org.hyperledger.aries.api.ledger.TAAInfo;
import org.hyperledger.aries.api.present_proof.PresentProofRecordsFilter;
import org.hyperledger.aries.api.present_proof.PresentProofRequest;
import org.hyperledger.aries.api.present_proof.PresentationExchangeRecord;
import org.hyperledger.aries.api.present_proof.PresentationExchangeRole;
import org.hyperledger.aries.api.present_proof.PresentationExchangeState;
import org.hyperledger.aries.api.schema.SchemaSendRequest;
import org.hyperledger.aries.api.schema.SchemaSendResponse;
import org.hyperledger.aries.api.schema.SchemaSendResponse.Schema;
import org.hyperledger.aries.api.schema.SchemasCreatedFilter;

/**
 *
 * @author Emers
 */
public class AriesAgent {

    private static AriesClient ac;

    public static void main(String[] args) throws IOException, InterruptedException, WriterException {
        final String AGENT_ADDR = "localhost";
        final String AGENT_PORT = "8021";
        final String END_POINT = "http://ee87-45-164-135-26.ngrok.io";
        
        AriesClient ac = getAriesClient(AGENT_ADDR, AGENT_PORT);

        String did = ac.walletDidPublic().get().getDid(); //Did publico
        String presentationId = null;

        final Scanner scan = new Scanner(System.in);

        boolean menuControl = true;

        do {
            System.out.println("Menu Aries Agent\n");
            System.out.println("1 - Gerar url de Conexão");
            System.out.println("2 - Criar Definição de credencial");
            System.out.println("3 - Emitir Credencial");
            System.out.println("4 - Exibir Schemas Criados");
            System.out.println("5 - Exibir schema por ID");
            System.out.println("6 - Listar Conexões");
            System.out.println("7 - Listar Definições de credenciais");
            System.out.println("8 - Solicitar prova de credenciais");
            System.out.println("9 - Verificar apresentação de prova de credenciais");
            System.out.println("0 - Exit\n");

            switch (scan.nextInt()) {
                case 1:
                    createInvitation(ac, END_POINT);
                    break;
                case 2:
                    credentialDefinition(ac, scan);
                    break;
                case 3:
                    issueCredentialV1(ac, scan, did);
                    break;
                case 4:
                    getSchemas(ac);
                    break;
                case 5:
                    getSchemaById(ac, scan);
                    break;
                case 6:
                    getConnections(ac);
                    break;
                case 7:
                    getCredentialDefinition(did);
                    break;
                case 8:
                    presentationId = requestProofCredential(ac, did);
                    break;
                case 9:
                    verifyPresentation(ac, presentationId);
                    break;
                case 0:
                    menuControl = false;
                    break;
                default:
                    break;
            }
        } while (menuControl);
    }

    //Obtem uma instância do Aries Cloud Agent
    public static AriesClient getAriesClient(String AGENT_ADDR, String AGENT_PORT) throws IOException {
        if (ac == null) {
            ac = AriesClient.builder().url("http://" + AGENT_ADDR + ":" + AGENT_PORT).build(); //Public network BuilderNetwork
            //ac = AriesClient.builder().url("http://localhost:8001").build(); //Local network VonNetwork
        }
        System.out.println("Agente Inicializado: " + ac.statusLive().get().isAlive());
        System.out.println("Public DID Information: " + ac.walletDidPublic().get().getDid());
        return ac;
    }

    //Gera um invitation url para conexão
    public static void createInvitation(AriesClient ac, String END_POINT) throws IOException, WriterException {
        Optional<CreateInvitationResponse> responseCI = ac.connectionsCreateInvitation(CreateInvitationRequest.builder().myLabel("Agent_Three").serviceEndpoint(END_POINT).build());
        System.out.println("Invitation URL: " + responseCI.get().getInvitationUrl());
        Util.generateQRCode(responseCI.get().getInvitationUrl());
    }

    //Gera o schema de forma estática
    public static Optional<SchemaSendResponse> createSchema(AriesClient ac, Scanner scan) throws IOException {
    	System.out.print("Informe o nome do Schema: ");
    	String schemaName = scan.next();
    	
    	System.out.print("Informe a versão do Schema: ");
    	String version = scan.next();
    	
        List<String> attributes = attributesSchema(scan);

        Optional<SchemaSendResponse> response = ac.schemas(
        		SchemaSendRequest.builder()
        		.schemaName(schemaName)
        		.schemaVersion(version)
        		.attributes(attributes)
        		.build()
    		);

        System.out.println("Schema:");
        System.out.println(response.get().toString());

        return response;
    }
    
    //Método para obter os atributos para o schema
    public static List<String> attributesSchema(Scanner scan) {
    	List<String> attributes = new LinkedList<String>();
    	
    	while(true) {
    		System.out.print("Informe o Nome do atributo ou digite 0 para encerrar: ");
    		String atr = scan.next();
    		
    		if(atr.equals("0")) {
    			break;
    		} else {
    			attributes.add(atr);
    		}
    	}
    	
    	return attributes;
    }

    // Envia uma definição de credencial para o ledger (blockchain), nesse caso utiliza a schema estático criado no método acima ( createSchema() )
    public static String credentialDefinition(AriesClient ac, Scanner scan) throws IOException {
        Optional<SchemaSendResponse> schema = createSchema(ac, scan);

        Optional<CredentialDefinitionResponse> response = ac.credentialDefinitionsCreate(CredentialDefinitionRequest.builder().schemaId(schema.get().getSchemaId()).supportRevocation(false).tag("Agent_Three").revocationRegistrySize(1000).build());

        System.out.println("Credential Definition:");
        System.out.println(response.get().toString());

        return response.get().getCredentialDefinitionId();
    }

    // Lista todos os schemas criados
    public static void getSchemas(AriesClient ac) throws IOException {
        Optional<List<String>> schemas = ac.schemasCreated(SchemasCreatedFilter.builder().build());
        System.out.println(schemas.get().toString());
    }

    // Lista um schema através do id informado (lista mais informações do id gerado)
    public static void getSchemaById(AriesClient ac, Scanner scan) throws IOException {
        System.out.print("Informe o SchemaId: ");
        Optional<Schema> schema = ac.schemasGetById(scan.next());

        System.out.println("\nId Schema: " + schema.get().getId());
        System.out.println("Name: " + schema.get().getName());
        System.out.println("Versão: " + schema.get().getVersion());
        System.out.println("Atributos: " + schema.get().getAttrNames().toString());
        System.out.println("seqNo: " + schema.get().getSeqNo() + "\n");
    }

    // Obtem o id de uma definição de credencial
    public static List<String> getCredentialDefinition(String did) throws IOException {

        CredentialDefinitionFilter filter = CredentialDefinitionFilter.builder().issuerDid(did).build();

        Optional<CredentialDefinition.CredentialDefinitionsCreated> response = ac.credentialDefinitionsCreated(filter);

        int i = 0;
        for (String credentialDefinitionId : response.get().getCredentialDefinitionIds()) {
            System.out.println("Number: " + i++);
            System.out.println(credentialDefinitionId);
        }
        System.out.println();

        return response.get().getCredentialDefinitionIds();
    }

    // Lista todas as conexões realizadas
    public static List<ConnectionRecord> getConnections(AriesClient ac) throws IOException {
        Optional<List<ConnectionRecord>> records = ac.connections();

        for (int i = 0; i < records.get().size(); i++) {
            System.out.println("Number: " + i + "\n");
            System.out.println("State: " + records.get().get(i).getState());
            System.out.println("RFC23 State: " + records.get().get(i).getRfc23Sate());
            System.out.println("Created at: " + records.get().get(i).getCreatedAt());
            System.out.println("Connection Id: " + records.get().get(i).getConnectionId());
            System.out.println("Their Label: " + records.get().get(i).getTheirLabel());
            System.out.println("Their DID: " + records.get().get(i).getTheirDid());
            System.out.println("Their Role: " + records.get().get(i).getTheirRole());
            System.out.println("Invitation Key: " + records.get().get(i).getInvitationKey() + "\n");
        }
        return records.get();
    }

    // Envia uma credencial no modelo v1.0
    public static void issueCredentialV1(AriesClient ac, Scanner scan, String did) throws IOException {
        List<ConnectionRecord> connections = getConnections(ac);
        System.out.println("Informe o Numero da Conexão: ");
        int conNum = scan.nextInt();
        String conId = connections.get(conNum).getConnectionId();

        List<String> credentialsDefinitionsIds = getCredentialDefinition(did);

        System.out.println("\nInforme o number da Definition: ");
        int credDefNumber = scan.nextInt();
        String credDefId = credentialsDefinitionsIds.get(credDefNumber);

        List<CredentialAttributes> attributes = new LinkedList<CredentialAttributes>();
        attributes.add(new CredentialAttributes("nome", "Aluno 3"));
        attributes.add(new CredentialAttributes("email", "aluno@ecomp.uefs.br"));
        attributes.add(new CredentialAttributes("matricula", "12345678"));

        CredentialPreview credPrev = new CredentialPreview(attributes);

        Optional<V1CredentialExchange> response = ac.issueCredentialSend(
                V1CredentialProposalRequest.builder()
                        .connectionId(conId)
                        .credentialDefinitionId(credDefId)
                        .autoRemove(true)
                        .credentialProposal(credPrev)
                        .build()
        );

        System.out.println("\n" + response.get().toString());
    }

    //Solicita uma apresentação de credencial
    public static String requestProofCredential(AriesClient ac, String did) throws IOException, InterruptedException {
        String connectionId = getConnections(ac).get(0).getConnectionId();
        String comment = "Prove que é aluno";
        String nameOfProofRequest = "Prova de educação";
        String nameOfAttrRequest = "nome";
        String version = "1.0";
        String valueOfAttrRequest = "nome";
        String restrictionName = "cred_def_id";
        String restrictionValue = getCredentialDefinition(did).get(0);

        JsonObject restriction = new JsonObject();
        restriction.addProperty(restrictionName, restrictionValue);
        PresentProofRequest.ProofRequest.ProofRequestedAttributes requestedAttributeValue = PresentProofRequest.ProofRequest.ProofRequestedAttributes.builder().restriction(restriction).name(valueOfAttrRequest).build();
        PresentProofRequest.ProofRequest proofRequest = PresentProofRequest.ProofRequest.builder().requestedAttribute(nameOfAttrRequest, requestedAttributeValue).name(nameOfProofRequest).version(version).build();

        PresentProofRequest presentProofRequest = PresentProofRequest.builder().comment(comment).connectionId(connectionId).proofRequest(proofRequest).build();

        System.out.println(presentProofRequest);
        System.out.println(presentProofRequest.getProofRequest());
        Optional<PresentationExchangeRecord> presentationExchangeRecord = ac.presentProofSendRequest(presentProofRequest);

        Optional<PresentationExchangeRecord> pres;
        String presentationId = presentationExchangeRecord.get().getPresentationExchangeId();
        PresentationExchangeRecord presentation;

        do {
            pres = ac.presentProofRecordsGetById(presentationId);
            presentation = pres.get();
            System.out.println("UpdateAt: " + presentation.getUpdatedAt());
            System.out.println("Presentation: " + presentation.getPresentation());
            System.out.println("Verificada: " + presentation.isVerified());
            System.out.println("State: " + presentation.getState());
            System.out.println("Auto Presentation: " + presentation.getAutoPresent());
            Thread.sleep(2 * 1000);
        } while (!presentation.getState().equals(PresentationExchangeState.PRESENTATION_RECEIVED));

        return presentationId;
    }

    //verificação da apresentação
    public static boolean verifyPresentation(AriesClient ac, String presentationId) throws IOException {
        if (presentationId != null) {
            boolean response = ac.presentProofRecordsVerifyPresentation(presentationId).get().getVerified();
            System.out.println("Apresentada: " + response);
            return response;
        }
        return false;
    }

    //recepção de convite
    public static void receiveInvitation(AriesClient ac) throws IOException {
        String type = "";
        String id = "";
        String did = "";
        List<String> recipientKeys = new ArrayList<>();
        String label = "";
        String serviceUrl = "";
        List<String> routingKeys = new ArrayList<>();
        String imageUrl = "";

        ReceiveInvitationRequest invite = ReceiveInvitationRequest.builder().
                type(type).id(id).did(did).recipientKeys(recipientKeys).
                label(label).serviceEndpoint(serviceUrl).routingKeys(routingKeys).
                imageUrl(imageUrl).build();

        String alias = "";
        Boolean autoAccept = true;
        String mediationId = "";

        ConnectionReceiveInvitationFilter filter = ConnectionReceiveInvitationFilter.builder().alias(alias).autoAccept(autoAccept).mediationId(mediationId).build();

        Optional<ConnectionRecord> connectionRecord = ac.connectionsReceiveInvitation(invite, filter);

        ConnectionRecord connectionRecord1 = connectionRecord.get();
    }

    //aceitação de convite
    public static void acceptInvitation(AriesClient ac) throws IOException {
        String connectionId = "";
        String mediationId = "";
        String myEndPoint = "";
        String myLabel = "";

        ConnectionAcceptInvitationFilter filter = ConnectionAcceptInvitationFilter.builder().
                mediationId(mediationId).myEndpoint(myEndPoint).myLabel(myLabel).build();

        Optional<ConnectionRecord> connectionRecord = ac.connectionsAcceptInvitation(connectionId, filter);

        ConnectionRecord connectionRecord1 = connectionRecord.get();

    }

    //confirmando a aceitação
    public static void acceptRequest(AriesClient ac) throws IOException {
        String connectionId = "";
        String myEndPoint = "";

        ConnectionAcceptRequestFilter filter = ConnectionAcceptRequestFilter.builder().myEndpoint(myEndPoint).build();

        Optional<ConnectionRecord> connectionRecord = ac.connectionsAcceptRequest(connectionId, filter);

        ConnectionRecord connectionRecord1 = connectionRecord.get();

    }

    //recuperando credenciais gravadas
    public static void getCredentialExchange(AriesClient ac) throws IOException {
        String connectionId = "";
        CredentialExchangeRole credentialExchangeRole = CredentialExchangeRole.HOLDER;
        CredentialExchangeState credentialExchangeState = CredentialExchangeState.CREDENTIAL_RECEIVED;
        String threadId = "";
        
        IssueCredentialRecordsFilter filter = IssueCredentialRecordsFilter.builder().connectionId(connectionId).
                role(credentialExchangeRole).state(credentialExchangeState).threadId(connectionId).build();
        
        Optional <List<V1CredentialExchange>> credentialsExchange = ac.issueCredentialRecords(filter);
        
        List<V1CredentialExchange> credentialsExchange1 = credentialsExchange.get();
    }
    
    //armazenar credencial na carteira
    public static void storeCredentialWallet(AriesClient ac) throws IOException{
        String credentialExchangeId = "";
        String credentialId = "";
        
        V1CredentialStoreRequest request = V1CredentialStoreRequest.builder().credentialId(credentialId).build();
        
        Optional <V1CredentialExchange> credentialExchange = ac.issueCredentialRecordsStore(credentialExchangeId, request);
        
        V1CredentialExchange credentialExchange1 = credentialExchange.get();
    }
    
    //verificar recepção de prova
    public static void presentProof(AriesClient ac) throws IOException{
        String connectionId = "";
        PresentationExchangeRole presentationExchangeRole = PresentationExchangeRole.PROVER;
        PresentationExchangeState presentationExchangeState = PresentationExchangeState.PRESENTATION_RECEIVED;
        String threadId = "";
        
        PresentProofRecordsFilter filter = PresentProofRecordsFilter.builder().connectionId(connectionId).
                role(presentationExchangeRole).state(presentationExchangeState).threadId(threadId).build();
        
        Optional <List<PresentationExchangeRecord>> presentationExchangeRecords = ac.presentProofRecords(filter);
        
        List<PresentationExchangeRecord> presentationExchangeRecords1 = presentationExchangeRecords.get();
        
    }

}
