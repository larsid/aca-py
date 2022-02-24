/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.aca.py;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hyperledger.aries.pojo.AttributeGroupName;
import org.hyperledger.aries.pojo.AttributeName;

/**
 *
 * @author Emers
 */
@Data @NoArgsConstructor @Builder @AllArgsConstructor
@AttributeGroupName("referent") // the referent that should be matched in the proof request
public final class MyCredential {
   private String street;

   @AttributeName("e-mail")
   private String email;       // schema attribute name is e-mail

   @AttributeName(excluded = true)
   private String comment;     // internal field
}