/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.aca.py;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import kotlin.collections.ArrayDeque;
import org.hyperledger.aries.AriesClient;
import static org.hyperledger.aries.api.AcaPyRequestFilter.log;
import org.hyperledger.aries.api.connection.ConnectionReceiveInvitationFilter;
import org.hyperledger.aries.api.connection.ConnectionRecord;
import org.hyperledger.aries.api.connection.ConnectionState;
import org.hyperledger.aries.api.connection.ReceiveInvitationRequest;
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
        AriesClient ac = getAriesClient();
        connect(ac);
        
        String connectionId = ac.connectionIds().get(ac.connectionIds().size()-1);
        
        System.out.println("\n******************\nconnectionIds: " + connectionId + "\n***********************\n");

        Optional <List<ConnectionRecord>> connections= ac.connections();
        
        List<String> attr = new ArrayDeque<>();
        
        attr.add("name");
        attr.add("email");
        
        SchemaSendRequest schema = SchemaSendRequest.builder().attributes(attr).schemaName("My Scheme").schemaVersion("1.0").build();
        
        ac.schemas(schema);
        
        for(ConnectionRecord c: connections.get()){
            
            System.out.println("Connection: "+c+"\n");
        }
       
        
        ac.actionMenuRequest(connectionId);
        
        
        //issueCredential(ac);
    }
    
    public static Schema getSchema(){
        Schema schema = new SchemaSendResponse.Schema();
        List<String> attr = new ArrayDeque<>();
        
        attr.add("name");
        attr.add("email");
        
        schema.setName("My Schema");
        schema.setVersion("1.0");
        schema.setAttrNames(attr);
        
        return schema;
    }

    public static AriesClient getAriesClient() {
        if (ac == null) {
            ac = AriesClient.builder().url("http://localhost:8021").build(); //Public network BuilderNetwork
            //ac = AriesClient.builder().url("http://localhost:8001").build(); //Local network VonNetwork
            
        }
        return ac;
    }

    public static void connect(AriesClient ac) throws IOException {

        String did = "P12Vzjv9jnVSyXS6uLtwQ9"; //Public network BuilderNetwork
        //String did = "UpFt248WuA5djSFThNjBhq"; //Local network VonNetwork
        String label = "test_conect";
        ac.connectionsReceiveInvitation(
                ReceiveInvitationRequest.builder()
                        .did(did)
                        .label(label)
                        .build(),
                ConnectionReceiveInvitationFilter
                        .builder()
                        .alias("alias")
                        .build())
                .ifPresent(connection -> {
                    log.debug("{}", connection.getConnectionId());
                });
    }

    public static void issueCredential(AriesClient ac) throws IOException {
        String connectionId = ac.connectionIds().get(0);

        String credentialdefinitionId = "6i7GFi2cDx524ZNfxmGWcp:3:CL:18:default";

        MyCredential myCredential = MyCredential
                .builder()
                .email("test@myexample.com")
                .build();


        ac.issueCredentialSend(
                new V1CredentialProposalRequest(connectionId, credentialdefinitionId, myCredential));
    }
}
