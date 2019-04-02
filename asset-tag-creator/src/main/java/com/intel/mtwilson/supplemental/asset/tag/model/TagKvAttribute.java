/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.supplemental.asset.tag.model;

import com.intel.mtwilson.jaxrs2.Document;

/**
 *
 * @author gdosunat
 */
public class TagKvAttribute extends Document{
    private String name;
    private String value;
    
    public TagKvAttribute(String name, String value){
        this.name = name;
        this.value = value;
    }
    
    public TagKvAttribute(){}
    
    public String getName(){
        return name;
    }
    
    public void setName(String name){
        this.name = name;
    }
    
    public String getValue(){
        return value;
    }
    
    public void setValue(String value){
        this.value = value;
    }
    
    /**
     * This method creates a TagKvAttribute object and returns it
     * @param name the name of the kvAttribute
     * @param value the value for the kvAttribute
     * @return TagKvAttribute object
     * @since CIT Next Gen
     * Sample Call:
     * <pre>
     * TagKvAttribute kvAttrb = TagKvAttribute.createKvAttribute("department","hr");
     * </pre>
     */
    public static TagKvAttribute createKvAttribute(String name, String value){
        
        TagKvAttribute tagKvAttribute = new TagKvAttribute();
        tagKvAttribute.setName(name);
        tagKvAttribute.setValue(value);
        
        return tagKvAttribute;
    }
}
