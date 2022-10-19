/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.uefs.larsid.ariesagentclient.controller;

import br.uefs.larsid.ariesagentclient.util.Util;
import br.uefs.larsid.ariesagentclient.model.AttributeRestriction;
import br.uefs.larsid.ariesagentclient.model.Credential;
import br.uefs.larsid.ariesagentclient.model.CredentialDefinition;
import br.uefs.larsid.ariesagentclient.model.PresentProof;
import br.uefs.larsid.ariesagentclient.model.Schema;
import com.google.zxing.WriterException;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import org.hyperledger.aries.AriesClient;
import org.hyperledger.aries.api.connection.ConnectionRecord;
import org.hyperledger.aries.api.connection.CreateInvitationRequest;
import org.hyperledger.aries.api.connection.CreateInvitationResponse;
import org.hyperledger.aries.api.credential_definition.CredentialDefinition.CredentialDefinitionResponse;
import org.hyperledger.aries.api.credential_definition.CredentialDefinition.CredentialDefinitionsCreated;
import org.hyperledger.aries.api.credential_definition.CredentialDefinitionFilter;
import org.hyperledger.aries.api.issue_credential_v1.V1CredentialExchange;
import org.hyperledger.aries.api.present_proof.PresentationExchangeRecord;
import org.hyperledger.aries.api.schema.SchemaSendResponse;
import org.hyperledger.aries.api.schema.SchemasCreatedFilter;

/**
 *
 * @author Emers
 */
public class Controller {

    private final String AGENT_ADDR;
    private final String AGENT_PORT;
    private final String AGENT_END_POINT;

    private AriesClient ariesClient;

    public Controller(String AGENT_ADDR, String AGENT_PORT, String AGENT_END_POINT) {
        this.AGENT_ADDR = AGENT_ADDR;
        this.AGENT_PORT = AGENT_PORT;
        this.AGENT_END_POINT = AGENT_END_POINT;
    }

    private AriesClient getAriesClient() {
        if (ariesClient == null) {
            ariesClient = AriesClient.builder().url("http://" + AGENT_ADDR + ":" + AGENT_PORT).build();
        }
        return ariesClient;
    }

    public Boolean isAliveClient() throws IOException {
        return getAriesClient().statusLive().get().isAlive();
    }

    public String getDid() throws IOException {
        return getAriesClient().walletDidPublic().get().getDid();
    }

    public CreateInvitationResponse createInvitation(String label) throws IOException {
        CreateInvitationRequest createInvitationRequest = CreateInvitationRequest.builder()
                .myLabel(label)
                .serviceEndpoint(AGENT_END_POINT)
                .build();
        return getAriesClient().connectionsCreateInvitation(createInvitationRequest).get();
    }

    public String getURLInvitation(CreateInvitationResponse createInvitationResponse) {
        return createInvitationResponse.getInvitationUrl();
    }

    public String getJsonInvitation(CreateInvitationResponse createInvitationResponse) {
        String param = getURLInvitation(createInvitationResponse).replaceFirst(".*?c_i=", "");
        return new String(Base64.getDecoder().decode(param));
    }

    public void generateQRCodeInvitation(CreateInvitationResponse createInvitationResponse) throws WriterException, IOException {
        Util.generateQRCode(createInvitationResponse.getInvitationUrl());
    }

    public SchemaSendResponse createSchema(Schema schema) throws IOException {
        SchemaSendResponse schemaSendResponse = getAriesClient().schemas(schema.build()).get();
        schema.setId(schemaSendResponse.getSchemaId());
        
        return schemaSendResponse;
    }

    public CredentialDefinitionResponse createCredendentialDefinition(CredentialDefinition credentialDefinition) throws IOException {
        CredentialDefinitionResponse credentialDefinitionResponse = getAriesClient().credentialDefinitionsCreate(credentialDefinition.build()).get();
        credentialDefinition.setId(credentialDefinitionResponse.getCredentialDefinitionId());
        
        return credentialDefinitionResponse;
    }

    public V1CredentialExchange issueCredentialV1(String connectionId, Credential credential) throws IOException {
        V1CredentialExchange v1CredentialExchange = getAriesClient().issueCredentialSend(credential.buildV1(connectionId)).get();
        credential.setId(v1CredentialExchange.getCredentialId());
        
        return v1CredentialExchange;
    }

    public List<String> getSchemasCreated() throws IOException {
        return getAriesClient().schemasCreated(SchemasCreatedFilter.builder().build()).get();
    }

    public SchemaSendResponse.Schema getSchemaById(String schemaId) throws IOException {
        return getAriesClient().schemasGetById(schemaId).get();
    }

    public List<ConnectionRecord> getConnections() throws IOException {
        return getAriesClient().connections().get();
    }

    public CredentialDefinitionsCreated getCredentialDefinitionsCreated() throws IOException {
        CredentialDefinitionFilter credentialDefinitionFilter = CredentialDefinitionFilter.builder().issuerDid(getDid()).build();
        return getAriesClient().credentialDefinitionsCreated(credentialDefinitionFilter).get();
    }

    public String sendRequestPresentationRequest(String name, String comment, String version, String connectionId, List<AttributeRestriction> attributesRestrictions) throws IOException {
        PresentProof presentProof = new PresentProof(name, comment, version);
        presentProof.addAttributesRestrictions(attributesRestrictions);

        return getAriesClient().presentProofSendRequest(presentProof.build(connectionId)).get().getPresentationExchangeId();
    }
    
    public PresentationExchangeRecord getPresentation(String id) throws IOException{
        return getAriesClient().presentProofRecordsGetById(id).get();
    }

}
