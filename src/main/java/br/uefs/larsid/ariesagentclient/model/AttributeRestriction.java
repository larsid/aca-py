/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.uefs.larsid.ariesagentclient.model;

import com.google.gson.JsonObject;

/**
 *
 * @author Emers
 */
public class AttributeRestriction {

    private String name;
    private String nameRestriction;
    private JsonObject restriction;

    public AttributeRestriction(String name, String value, String nameRestriction, String propertyRestriction) {
        this.name = name;
        this.nameRestriction = nameRestriction;
        this.restriction = new JsonObject();
        this.restriction.addProperty(propertyRestriction, value);
    }

    public String getName() {
        return name;
    }

    public String getNameRestriction() {
        return nameRestriction;
    }

    public JsonObject getRestriction() {
        return restriction;
    }

}
