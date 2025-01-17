package io.advantageous.qbit.meta.swagger.builders;

import io.advantageous.qbit.meta.swagger.Definition;
import io.advantageous.qbit.meta.swagger.Schema;

import java.util.LinkedHashMap;
import java.util.Map;

public class DefinitionBuilder {


    private  Map<String, Schema> properties;


    public Map<String, Schema> getProperties() {
        if (properties == null) {
            properties = new LinkedHashMap<>();
        }
        return properties;
    }

    public void addProperty(String name, Schema schema) {

        getProperties().put(name, schema);
    }

    public void setProperties(Map<String, Schema> properties) {
        this.properties = properties;
    }


    public Definition build() {
        return new Definition(getProperties());
    }
}
