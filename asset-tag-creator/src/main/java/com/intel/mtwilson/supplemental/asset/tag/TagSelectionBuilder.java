/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.supplemental.asset.tag;

import com.intel.mtwilson.supplemental.asset.tag.model.TagKvAttribute;
import com.intel.mtwilson.supplemental.asset.tag.model.TagSelection;
import java.util.ArrayList;
/**
 *
 * @author gdosunat
 */
public class TagSelectionBuilder {
    
    private String selectionName;
    private String selectionDescription;
    private ArrayList<TagKvAttribute> selection = new ArrayList<TagKvAttribute>();
    
    public static TagSelectionBuilder factory(){return new TagSelectionBuilder();}
    public TagSelectionBuilder selectionName(String selectionName){
        this.selectionName = selectionName;
        return this;
    }
    
    public String getSelectionName(){
        return this.selectionName;
    }
    
    public TagSelectionBuilder selectionDescription(String selectionDescription){
        this.selectionDescription = selectionDescription;
        return this;
    }
    
    public String getSelectionDescription(){
        return this.selectionDescription;
    }
    
    public TagSelectionBuilder textKvAttribute(String kvAttributeName, String kvAttributeValue){
        selection.add(new TagKvAttribute(kvAttributeName, kvAttributeValue));
        return this;
    }
    
    public ArrayList<TagKvAttribute> getSelection(){
        return this.selection;
    }
    
    public TagSelection build(){
        return new TagSelection(this);
    }
}
