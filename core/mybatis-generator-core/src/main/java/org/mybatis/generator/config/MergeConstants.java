/**
 *    Copyright 2006-2020 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.generator.config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * This class holds constants useful in the XML and Java merging operations.
 * 
 * @author Jeff Butler
 * 
 */
public class MergeConstants {

    /**
     * Utility class - no instances.
     * 
     */
    private MergeConstants() {
    }

    private static final String[] OLD_XML_ELEMENT_PREFIXES = {
            "ibatorgenerated_", "abatorgenerated_" }; //$NON-NLS-1$ //$NON-NLS-2$

    public static final String NEW_ELEMENT_TAG = "@mbg.generated"; //$NON-NLS-1$
    public static final String[] OLD_ELEMENT_TAGS = {
            "@ibatorgenerated", //$NON-NLS-1$
            "@abatorgenerated", //$NON-NLS-1$
            "@mbggenerated", //$NON-NLS-1$
            "autoGenerate",
            NEW_ELEMENT_TAG };
    public static final Set<String> OLD_XML_ELEMENT_IDS = new HashSet<>();

    static {
        OLD_XML_ELEMENT_IDS.add("countByExample");
        OLD_XML_ELEMENT_IDS.add("deleteByExample");
        OLD_XML_ELEMENT_IDS.add("deleteByPrimaryKey");
        OLD_XML_ELEMENT_IDS.add("insert");
        OLD_XML_ELEMENT_IDS.add("insertSelective");
        OLD_XML_ELEMENT_IDS.add("selectAll");
        OLD_XML_ELEMENT_IDS.add("selectByExample");
        OLD_XML_ELEMENT_IDS.add("selectByExampleWithBLOBs");
        OLD_XML_ELEMENT_IDS.add("selectByPrimaryKey");
        OLD_XML_ELEMENT_IDS.add("updateByExample");
        OLD_XML_ELEMENT_IDS.add("updateByExampleSelective");
        OLD_XML_ELEMENT_IDS.add("updateByExampleWithBLOBs");
        OLD_XML_ELEMENT_IDS.add("updateByPrimaryKey");
        OLD_XML_ELEMENT_IDS.add("updateByPrimaryKeySelective");
        OLD_XML_ELEMENT_IDS.add("updateByPrimaryKeyWithBLOBs");
        OLD_XML_ELEMENT_IDS.add("BaseResultMap");
        OLD_XML_ELEMENT_IDS.add("ResultMapWithBLOBs");
        OLD_XML_ELEMENT_IDS.add("Example_Where_Clause");
        OLD_XML_ELEMENT_IDS.add("Base_Column_List");
        OLD_XML_ELEMENT_IDS.add("Blob_Column_List");
        OLD_XML_ELEMENT_IDS.add("Update_By_Example_Where_Clause");
    }

    public static String[] getOldElementTags() {
        return OLD_ELEMENT_TAGS;
    }

    public static boolean idStartsWithPrefix(String id) {
        return Arrays.stream(OLD_XML_ELEMENT_PREFIXES)
                .anyMatch(id::startsWith);
    }

    public static boolean commentContainsTag(String comment) {
        return Arrays.stream(OLD_ELEMENT_TAGS)
                .anyMatch(comment::contains);
    }
}
