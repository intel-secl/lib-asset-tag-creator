/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.supplemental.asset.tag.model;

import com.intel.mtwilson.supplemental.asset.tag.TagSelectionBuilder;
import java.util.ArrayList;
import com.intel.mtwilson.jaxrs2.Document;


/**
 *
 * @author gdosunat
 */
public class TagSelection extends Document{

    private ArrayList<TagKvAttribute> selection = new ArrayList<>();
    private String name;
    private String description;
    
    public TagSelection(TagSelectionBuilder builder){
        this.name = builder.getSelectionName();
        this.description = builder.getSelectionDescription();
        this.selection = builder.getSelection();
    }

    public TagSelection() {}
    
    public void setName(String selectionName){
        this.name = selectionName;
    }
    
    public String getName(){
        return this.name;
    }

    public void setDescription(String selectionDescription){
        this.description = selectionDescription;
    }
    
    public String getDescription(){
        return this.description;
    }
    
    public ArrayList<TagKvAttribute> getSelection(){
        return this.selection;
    }
    
    public void setSelection(ArrayList<TagKvAttribute> selection){
        this.selection = selection;
    }
    
}