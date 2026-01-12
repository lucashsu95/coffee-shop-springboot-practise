package tw.edu.ntub.imd.birc.coffeeshop.util.json;

import com.fasterxml.jackson.databind.JsonNode;

@FunctionalInterface
public interface ResponseData {
    JsonNode getData();
}

