/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.supplemental.asset.tag;

import com.intel.mtwilson.supplemental.asset.tag.model.TagKvAttribute;
import org.junit.Test;
import com.intel.mtwilson.supplemental.asset.tag.TagSelectionBuilder;
import com.intel.mtwilson.supplemental.asset.tag.model.TagSelection;
import java.util.ArrayList;
/**
 *
 * @author gdosunat
 */
public class TagSelectionBuilderTest {
    
 
    @Test
    public void createTagSelectionTest(){
        TagSelectionBuilder builder = TagSelectionBuilder.factory();
        TagSelection tagSelection = builder.selectionName("Country").selectionDescription("This selection contains countries").textKvAttribute("Country", "Mexico").textKvAttribute("Country", "USA").build();
        System.out.println(tagSelection.getName());
        ArrayList<TagKvAttribute> selection = tagSelection.getSelection();
        
       /* for( int x=0; x< selection.size(); x++){
            System.out.println(selection.get(x).getName());
            System.out.println(selection.get(x).getValue());
        }*/
        
}
    
}

