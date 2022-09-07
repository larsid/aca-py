/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.uefs.larsid.ariesagentclient.model;

import java.util.ArrayList;
import java.util.List;
import org.hyperledger.aries.api.schema.SchemaSendRequest;

/**
 *
 * @author Emers
 */
public final class Schema {

    private String name;
    private String version;
    private List<String> attributes;

    public Schema() {

    }

    public Schema(String name, String version) {
        this.name = name;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<String> addAttribute(String attrName) {
        if (attributes == null) {
            this.attributes = new ArrayList<>();
        }
        if (!attributes.contains(attrName)) {
            this.attributes.add(name);
        }
        return this.attributes;
    }

    public List<String> addAttributes(List<String> attributes) {
        for (String attribute : attributes) {
            addAttribute(attribute);
        }
        return this.attributes;
    }

    public List<String> getAttributes() {
        return this.attributes;
    }

    public SchemaSendRequest build() {
        if (this.name == null || this.version == null || this.attributes == null) {
            return null;
        }
        return SchemaSendRequest.builder()
                .schemaName(name)
                .schemaVersion(version)
                .attributes(attributes)
                .build();
    }
}
