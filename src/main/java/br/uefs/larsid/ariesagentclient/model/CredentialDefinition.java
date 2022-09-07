/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.uefs.larsid.ariesagentclient.model;

import org.hyperledger.aries.api.credential_definition.CredentialDefinition.CredentialDefinitionRequest;

/**
 *
 * @author Emers
 */
public class CredentialDefinition {

    private String tag;
    private Boolean revocable;
    private Integer revocationRegistrySize;
    private String schemaId;

    public CredentialDefinition() {

    }

    public CredentialDefinition(String tag, Boolean revocable, Integer revocationRegistrySize, String schemaId) {
        this.tag = tag;
        this.revocable = revocable;
        this.revocationRegistrySize = revocationRegistrySize;
        this.schemaId = schemaId;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Boolean getRevocable() {
        return revocable;
    }

    public void setRevocable(Boolean revocable) {
        this.revocable = revocable;
    }

    public Integer getRevocationRegistrySize() {
        return revocationRegistrySize;
    }

    public void setRevocationRegistrySize(Integer revocationRegistrySize) {
        this.revocationRegistrySize = revocationRegistrySize;
    }

    public CredentialDefinitionRequest build() {
        if (this.tag == null || this.revocable == null || this.revocationRegistrySize == null || this.schemaId == null) {
            return null;
        }
        return CredentialDefinitionRequest.builder()
                .schemaId(schemaId)
                .supportRevocation(revocable)
                .tag(tag)
                .revocationRegistrySize(revocationRegistrySize)
                .build();
    }

}
