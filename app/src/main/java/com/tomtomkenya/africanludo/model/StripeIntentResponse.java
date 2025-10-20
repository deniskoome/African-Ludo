package com.tomtomkenya.africanludo.model;

import com.google.gson.annotations.SerializedName;

public class StripeIntentResponse {

    @SerializedName("clientSecret")
    private String clientSecret;

    @SerializedName("publishableKey")
    private String publishableKey;

    @SerializedName("currency")
    private String currency;

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getPublishableKey() {
        return publishableKey;
    }

    public void setPublishableKey(String publishableKey) {
        this.publishableKey = publishableKey;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
