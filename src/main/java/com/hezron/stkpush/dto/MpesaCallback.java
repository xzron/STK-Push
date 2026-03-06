package com.hezron.stkpush.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class MpesaCallback {

    @JsonProperty("Body")
    private Body body;

    @Data
    public static class Body {

        @JsonProperty("stkCallback")
        private StkCallback stkCallback;
    }

    @Data
    public static class StkCallback {

        @JsonProperty("MerchantRequestID")
        private String merchantRequestID;

        @JsonProperty("CheckoutRequestID")
        private String checkoutRequestID;

        @JsonProperty("ResultCode")
        private String resultCode;

        @JsonProperty("ResultDesc")
        private String resultDesc;

        @JsonProperty("CallbackMetadata")
        private CallbackMetaData callbackMetaData;

        @Data
        public static class CallbackMetaData {

            @JsonProperty("Item")
            private List<CallbackItem> items;
        }

        @Data
        public static class CallbackItem {

            @JsonProperty("Name")
            private String name;

            @JsonProperty("Value")
            private Object value;
        }
    }
}