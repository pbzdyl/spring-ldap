/*
 * Copyright 2005 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */

package org.springframework.ldap.odm.typeconversion;

import java.util.List;
import java.util.Set;
import java.util.Collection;

public enum ValidContainerType
{
    ARRAY(Object[].class),
    COLLECTION(Collection.class),
    LIST(List.class),
    SET(Set.class);

    private final Class clazz;

    ValidContainerType(Class clazz)
    {
        this.clazz = clazz;
    }

    /**
     * Returns the enumeration of types as a human-friendly string.
     */
    public static String listTypes()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("VALID MULTI-VALUE CONTAINER TYPES:");
        for (int i = 0; i < values().length; i++)
        {
            ValidContainerType validType = values()[i];
            sb.append("\n");
            sb.append(validType.clazz.getSimpleName());
            if (i != values().length - 1)
            {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    /**
     * Returns true if the argument is a member of this enumeration.
     */
    public static boolean isValidContainerType(Class returnType)
    {
        for (ValidContainerType type : values())
        {
            if (returnType.equals(type.clazz))
            {
                return true;
            }
        }
        return false;
    }

    public static boolean isImplementedBy(Class returnType)
    {
        for (ValidContainerType type : values())
        {
            if (type.clazz.isAssignableFrom(returnType))
            {
                return true;
            }
        }
        return false;
    }


}
