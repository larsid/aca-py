/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.aca.py;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

import kotlin.collections.ArrayDeque;
import lombok.NonNull;

import org.hyperledger.aries.AriesClient;
import static org.hyperledger.aries.api.AcaPyRequestFilter.log;
import org.hyperledger.aries.api.connection.ConnectionReceiveInvitationFilter;
import org.hyperledger.aries.api.connection.ConnectionRecord;
import org.hyperledger.aries.api.connection.CreateInvitationRequest;
import org.hyperledger.aries.api.connection.CreateInvitationResponse;
import org.hyperledger.aries.api.connection.ReceiveInvitationRequest;
import org.hyperledger.aries.api.credential_definition.CredentialDefinition.CredentialDefinitionRequest;
import org.hyperledger.aries.api.credential_definition.CredentialDefinition.CredentialDefinitionResponse;
import org.hyperledger.aries.api.credentials.CredentialAttributes;
import org.hyperledger.aries.api.credentials.CredentialPreview;
import org.hyperledger.aries.api.issue_credential_v1.V1CredentialExchange;
import org.hyperledger.aries.api.issue_credential_v1.V1CredentialProposalRequest;
import org.hyperledger.aries.api.schema.SchemaSendRequest;
import org.hyperledger.aries.api.schema.SchemaSendResponse;
import org.hyperledger.aries.api.schema.SchemaSendResponse.Schema;
import org.hyperledger.aries.api.schema.SchemasCreatedFilter;


/**
 *
 * @author Emers
 */
public class Test {

    private static AriesClient ac;

    public static void main(String[] args) throws IOException, InterruptedException {
    	final Scanner scan  = new Scanner(System.in);
    	
        AriesClient ac = getAriesClient();
        
        boolean menuControl = true;
        
        do {
        	System.out.println("Menu Aries Agent\n");
        	System.out.println("1 - Gerar url de Conexão");
        	System.out.println("2 - Criar Schema");
        	System.out.println("3 - Emitir Credencial");
        	System.out.println("4 - Exibir Schemas Criados");
        	System.out.println("5 - Exibir schema por ID");
        	System.out.println("6 - Listar Conexões");
        	System.out.println("0 - Exit\n");
        	
        	switch(scan.nextInt()) {
        		case 1:
        			createInvitation(ac);
        			break;
        		case 2:
        			credentialDefinition(ac);
        			break;
        		case 3:
        			issueCredentialV1(ac, scan);
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
        		case 0:
        			menuControl = false;
        			break;
        		default:
        			break;
        	}
        } while(menuControl);
    }

    //Obtem uma instância do Aries Cloud Agent
    public static AriesClient getAriesClient() throws IOException {
        if (ac == null) {
            ac = AriesClient.builder().url("http://localhost:8021").build(); //Public network BuilderNetwork
            //ac = AriesClient.builder().url("http://localhost:8001").build(); //Local network VonNetwork
        }
        System.out.println("Agente Inicializado: " + ac.statusLive().get().isAlive());
        System.out.println("Public DID Information: " + ac.walletDidPublic().get().getDid());
        return ac;
    }
    
    //Gera um invitation url para conexão
    public static void createInvitation(AriesClient ac) throws IOException {
    	 Optional<CreateInvitationResponse> responseCI = ac.connectionsCreateInvitation(CreateInvitationRequest.builder().myLabel("Agent_One").serviceEndpoint("http://0.0.0.0:8020").build());
    	 System.out.println("Invitation URL: " + responseCI.get().getInvitationUrl() );
    }
    
    //Gera o schema de forma estática / TODO: tonar criação de schema de forma dinâmica
    public static Optional<SchemaSendResponse> createSchema(AriesClient ac) throws IOException {   	
    	List<String> attributes = new LinkedList<String>();
    	attributes.add("nome");
    	attributes.add("email");
    	attributes.add("matricula");
    	
    	Optional<SchemaSendResponse> response = ac.schemas( SchemaSendRequest.builder().schemaName("aluno").schemaVersion("1.0").attributes(attributes).build() );
    	
    	System.out.println("Schema:");
    	System.out.println(response.get().toString());
    	
		return response;
    }
    
    // Envia uma definição de credencial para o ledger (blockchain), nesse caso utiliza a schema estático criado no método acima ( createSchema() )
    public static void credentialDefinition(AriesClient ac) throws IOException {
    	Optional<SchemaSendResponse> schema = createSchema(ac);
    	
    	Optional<CredentialDefinitionResponse> response = ac.credentialDefinitionsCreate( CredentialDefinitionRequest.builder().schemaId(schema.get().getSchemaId()).supportRevocation(false).tag("Agent_One").revocationRegistrySize(1000).build() );
    
    	System.out.println("Credential Definition:");
    	System.out.println(response.get().toString());
    }
    
    // Lista todos os schemas criados
    public static void getSchemas(AriesClient ac) throws IOException {
    	 Optional<List<String>> schemas = ac.schemasCreated(SchemasCreatedFilter.builder().build());
    	 System.out.println( schemas.get().toString() );
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
    public static void getCredentialDefinition() {
    	// TODO: fazer opção de receber todos os ids de credenciais ou filtrar
    }
    
    // Lista todas as conexões realizadas
    public static void getConnections(AriesClient ac) throws IOException {
    	Optional<List<ConnectionRecord>> records = ac.connections();
    	
    	for (int i = 0; i < records.get().size(); i++) {
    		System.out.println("State: " + records.get().get(i).getState());
    		System.out.println("RFC23 State: " + records.get().get(i).getRfc23Sate());
    		System.out.println("Created at: " + records.get().get(i).getCreatedAt());
    		System.out.println("Connection Id: " + records.get().get(i).getConnectionId());
    		System.out.println("Their Label: " + records.get().get(i).getTheirLabel());
    		System.out.println("Their DID: " + records.get().get(i).getTheirDid());
    		System.out.println("Their Role: " + records.get().get(i).getTheirRole());
    		System.out.println("Invitation Key: " + records.get().get(i).getInvitationKey());
    		System.out.println("Connection Id: " + records.get().get(i).getConnectionId() + "\n");
    	}
    }

    // Envia uma credencial no modelo v1.0
    public static void issueCredentialV1(AriesClient ac, Scanner scan) throws IOException {
    	System.out.print("Informe o Connection Id: ");
    	String conId = scan.next();
    	
    	System.out.print("\nInforme o Credential Definition Id: ");
    	String cred_def_id = scan.next();
    	
    	List<CredentialAttributes> attributes = new LinkedList<CredentialAttributes>();
    	attributes.add(new CredentialAttributes("nome", "Aluno 1"));
    	attributes.add(new CredentialAttributes("email", "aluno@ecomp.uefs.br"));
    	attributes.add(new CredentialAttributes("matricula", "12345678"));
    	
    	CredentialPreview credPrev = new CredentialPreview(attributes);
    	
    	Optional<V1CredentialExchange> response = ac.issueCredentialSend( 
										    			V1CredentialProposalRequest.builder()
										    				.connectionId(conId)
										    				.credentialDefinitionId(cred_def_id)
										    				.autoRemove(true)
										    				.credentialProposal(credPrev)
										    				.build()
									    			);
    	
    	System.out.println("\n" + response.get().toString());
    }
}
