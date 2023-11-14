package com.daniel.jsoneditor.controller.impl.json;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class VariableHelper
{
    public static Set<String> findVariables(String text)
    {
        Pattern pattern = Pattern.compile("<(.*?)>");
        Matcher matcher = pattern.matcher(text);
        Set<String> variables = new HashSet<>();
        while (matcher.find())
        {
            variables.add(matcher.group());
        }
        return variables;
    }
    
    public static String replaceVariables(String text, Map<String, String> replacements)
    {
        for (Map.Entry<String, String> entry : replacements.entrySet())
        {
            if (!entry.getKey().equals(entry.getValue()))
            {
                text = text.replace(entry.getKey(), entry.getValue());
            }
        }
        return text;
    }
}
