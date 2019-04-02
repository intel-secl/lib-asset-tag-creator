/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.supplemental.asset.tag;

import org.junit.Test;
import com.intel.mtwilson.supplemental.asset.tag.model.TagKvAttribute;
import static org.junit.Assert.assertEquals;


/**
 *
 * @author gdosunat
 */
public class TagKvAttributeTest {
    
    @Test
    public void createTagKvAttribute(){
        
        TagKvAttribute kvAttribute = new TagKvAttribute();
        kvAttribute = kvAttribute.createKvAttribute("Country", "Mexico");
        
        TagKvAttribute kvAttribute2 = new TagKvAttribute();
        kvAttribute2.setName("Country");
        kvAttribute2.setValue("Mexico");
        
        assertEquals(kvAttribute.getName(), kvAttribute2.getName());
        assertEquals(kvAttribute.getValue(), kvAttribute2.getValue());

    }
}
