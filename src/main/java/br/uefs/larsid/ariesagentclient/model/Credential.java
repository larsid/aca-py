/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.uefs.larsid.ariesagentclient.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.hyperledger.aries.api.credentials.CredentialAttributes;
import org.hyperledger.aries.api.credentials.CredentialPreview;
import org.hyperledger.aries.api.issue_credential_v1.V1CredentialProposalRequest;

/**
 *
 * @author Emers
 */
public class Credential {

    private String credentialDefinitionID;
    private Boolean autoRemovable;
    private Map<String, String> values;

    public Credential() {

    }

    public Credential(String credentialDefinitionID, Boolean autoRemovable) {
        this.credentialDefinitionID = credentialDefinitionID;
        this.autoRemovable = autoRemovable;
    }

    public String getCredentialDefinitionID() {
        return credentialDefinitionID;
    }

    public void setCredentialDefinitionID(String credentialDefinitionID) {
        this.credentialDefinitionID = credentialDefinitionID;
    }

    public Boolean getAutoRemovable() {
        return autoRemovable;
    }

    public void setAutoRemovable(Boolean autoRemovable) {
        this.autoRemovable = autoRemovable;
    }

    public Map<String, String> addValue(String key, String value) {
        if (values == null) {
            values = new HashMap<>();
        }
        if (key != null && value != null) {
            values.put(key, value);
        }
        return values;
    }

    public Map<String, String> addValues(Map<String, String> values) {
        if (values == null) {
            values = new HashMap<>();
        }
        this.values.putAll(values);
        return this.values;
    }

    private CredentialPreview getCredentialPreview() {
        List<CredentialAttributes> attributes = new LinkedList<>();
        for (String key : values.keySet()) {
            attributes.add(new CredentialAttributes(key, values.get(key)));
        }
        return new CredentialPreview(attributes);
    }

    public V1CredentialProposalRequest buildV1(String connectionID) {
        if (credentialDefinitionID == null || autoRemovable == null || values == null) {
            return null;
        }
        return V1CredentialProposalRequest.builder()
                .connectionId(connectionID)
                .credentialDefinitionId(credentialDefinitionID)
                .autoRemove(autoRemovable)
                .credentialProposal(getCredentialPreview())
                .build();
    }
}
