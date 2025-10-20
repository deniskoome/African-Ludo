package com.tomtomkenya.africanludo.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the API response returned by the new /gateways/active endpoint.
 */
public class PaymentGatewayModel {

    @SerializedName("gateways")
    private List<Gateway> gateways = new ArrayList<>();

    public List<Gateway> getGateways() {
        return gateways;
    }

    public void setGateways(List<Gateway> gateways) {
        this.gateways = gateways;
    }

    public static class Gateway {

        @SerializedName("id")
        private int id;

        @SerializedName("name")
        private String name;

        @SerializedName("displayName")
        private String displayName;

        @SerializedName("logo")
        private String logo;

        @SerializedName("supportedActions")
        private List<String> supportedActions = new ArrayList<>();

        @SerializedName("metadata")
        private Metadata metadata;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getLogo() {
            return logo;
        }

        public void setLogo(String logo) {
            this.logo = logo;
        }

        public List<String> getSupportedActions() {
            return supportedActions;
        }

        public void setSupportedActions(List<String> supportedActions) {
            this.supportedActions = supportedActions;
        }

        public Metadata getMetadata() {
            return metadata;
        }

        public void setMetadata(Metadata metadata) {
            this.metadata = metadata;
        }

        public boolean supportsAction(String action) {
            return supportedActions != null && supportedActions.contains(action);
        }
    }

    public static class Metadata {

        @SerializedName("configured")
        private boolean configured;

        public boolean isConfigured() {
            return configured;
        }

        public void setConfigured(boolean configured) {
            this.configured = configured;
        }
    }
}
